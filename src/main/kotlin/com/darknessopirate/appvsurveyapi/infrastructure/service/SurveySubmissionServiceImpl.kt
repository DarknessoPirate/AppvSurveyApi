package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.AnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.ClosedAnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.OpenAnswerRequest
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.repository.question.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.userAnswer.ClosedUserAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.repository.userAnswer.OpenUserAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySubmissionService
import jakarta.persistence.EntityNotFoundException
import org.hibernate.Hibernate
import org.hibernate.LazyInitializationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
@Transactional
class SurveySubmissionServiceImpl(
    private val submittedSurveyRepository: SubmittedSurveyRepository,
    private val surveyRepository: SurveyRepository,
    private val openUserAnswerRepository: OpenUserAnswerRepository,
    private val closedUserAnswerRepository: ClosedUserAnswerRepository,
    private val questionRepository: QuestionRepository,
    private val accessCodeService: IAccessCodeService
) : ISurveySubmissionService {

    private val logger = LoggerFactory.getLogger(SurveySubmissionServiceImpl::class.java)

    override fun submitByAccessCode(
        accessCode: String,
        answers: List<AnswerRequest>
    ): SubmittedSurvey {
        val accessCodeEntity = accessCodeService.validateAccessCode(accessCode)
            ?: throw EntityNotFoundException("Invalid or expired access code: $accessCode")

        val surveyId = accessCodeEntity.survey?.id
            ?: throw IllegalStateException("Access code is not associated with a survey")

        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found for access code: $accessCode")

        if (!survey.isActive) {
            throw IllegalStateException("Survey is not active")
        }

        if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDate.now())) {
            throw IllegalStateException("Survey has expired")
        }

        // Increment usage count
        try {
            val codeId = accessCodeEntity.id
                ?: throw IllegalStateException("Access code ID cannot be null")
            accessCodeService.incrementUsage(codeId)
        } catch (e: DataAccessException) {
            logger.error("Failed to increment usage count for access code: $accessCode", e)
            throw IllegalStateException("Failed to update access code usage", e)
        }

        // Pass the access code entity to link it properly
        return submitSurveyWithAccessCode(surveyId, answers, accessCodeEntity)
    }

    private fun submitSurveyWithAccessCode(
        surveyId: Long,
        answers: List<AnswerRequest>,
        accessCode: AccessCode?
    ): SubmittedSurvey {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        // For admin/internal use, still validate survey state
        // (Anonymous access validation happens in submitByAccessCode)
        if (accessCode == null) {
            // Only validate for internal/admin usage
            if (!survey.isActive) {
                throw IllegalStateException("Survey is not active")
            }
            if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDate.now())) {
                throw IllegalStateException("Survey has expired")
            }
        }

        // Validate all required questions are answered
        validateRequiredQuestions(survey, answers)

        // Create submitted survey with access code reference
        val submittedSurvey = SubmittedSurvey(
            survey = survey,
            accessCode = accessCode
        )

        val savedSubmission = try {
            submittedSurveyRepository.save(submittedSurvey)
        } catch (e: DataAccessException) {
            logger.error("Failed to save submitted survey for survey $surveyId", e)
            throw IllegalStateException("Failed to save survey submission", e)
        }

        // Process each answer based on its type
        answers.forEach { answerRequest ->
            val question = survey.questions.find { it.id == answerRequest.questionId }
                ?: throw IllegalArgumentException("Question ${answerRequest.questionId} not found in survey")

            val userAnswer = when (answerRequest) {
                is OpenAnswerRequest -> {
                    if (question !is OpenQuestion) {
                        throw IllegalArgumentException("Open answer provided for non-open question ${question.id}")
                    }
                    createOpenAnswer(question, answerRequest, savedSubmission)
                }

                is ClosedAnswerRequest -> {
                    if (question !is ClosedQuestion) {
                        throw IllegalArgumentException("Closed answer provided for non-closed question ${question.id}")
                    }
                    createClosedAnswer(question, answerRequest, savedSubmission)
                }
            }

            savedSubmission.addUserAnswer(userAnswer)
        }

        return try {
            submittedSurveyRepository.save(savedSubmission)
        } catch (e: DataAccessException) {
            logger.error("Failed to save final submission with answers for survey $surveyId", e)
            throw IllegalStateException("Failed to save submission answers", e)
        }
    }

    // Get submission by ID
    override fun getSubmission(submissionId: Long): SubmittedSurvey? {
        val submission = submittedSurveyRepository.findByIdWithSurveyAndAnswers(submissionId)
            ?: return null

        // Initialize lazy-loaded collections with proper error handling
        try {
            Hibernate.initialize(submission.userAnswers)

            // For each user answer, if it's a closed answer, initialize selected answers
            submission.userAnswers.forEach { userAnswer ->
                if (userAnswer is ClosedUserAnswer) {
                    Hibernate.initialize(userAnswer.selectedAnswers)
                    logger.debug("Loaded {} selected answers for question {}",
                        userAnswer.selectedAnswers.size, userAnswer.question.id)
                }
            }
        } catch (e: LazyInitializationException) {
            logger.error("Lazy initialization error while loading submission $submissionId", e)
            throw IllegalStateException("Failed to load submission data: session may have been closed", e)
        } catch (e: Exception) {
            logger.error("Unexpected error while loading submission $submissionId", e)
            throw IllegalStateException("Failed to load submission data", e)
        }

        return submission
    }

    override fun getSubmissionsByAccessCode(accessCode: String): List<SubmittedSurvey> {
        return try {
            submittedSurveyRepository.findByAccessCodeWithAnswers(accessCode)
        } catch (e: DataAccessException) {
            logger.error("Database error while getting submissions for access code: $accessCode", e)
            throw IllegalStateException("Failed to retrieve submissions", e)
        }
    }

    // Helper methods
    private fun validateRequiredQuestions(survey: Survey, answers: List<AnswerRequest>) {
        val requiredQuestionIds = survey.questions
            .filter { it.required }
            .mapNotNull { it.id } // Filter out questions with null IDs

        val answeredQuestionIds = answers.map { it.questionId }

        val unansweredRequired = requiredQuestionIds - answeredQuestionIds.toSet()
        if (unansweredRequired.isNotEmpty()) {
            throw IllegalArgumentException("Required questions not answered: $unansweredRequired")
        }
    }

    private fun createOpenAnswer(
        question: OpenQuestion,
        answerRequest: OpenAnswerRequest,
        submittedSurvey: SubmittedSurvey
    ): OpenUserAnswer {
        if (answerRequest.textValue.isBlank() && question.required) {
            val questionId = question.id ?: "unknown"
            throw IllegalArgumentException("Text answer required for question $questionId")
        }

        return OpenUserAnswer(
            submittedSurvey = submittedSurvey,
            question = question,
            textValue = answerRequest.textValue
        )
    }

    private fun createClosedAnswer(
        question: ClosedQuestion,
        answerRequest: ClosedAnswerRequest,
        submittedSurvey: SubmittedSurvey
    ): ClosedUserAnswer {
        val questionId = question.id ?: "unknown"

        if (answerRequest.selectedAnswerIds.isEmpty() && question.required) {
            throw IllegalArgumentException("Answer selection required for question $questionId")
        }

        val selectedAnswers = question.possibleAnswers.filter {
            it.id in answerRequest.selectedAnswerIds
        }.toMutableList()

        // Validate selection type constraints
        if (question.selectionType == SelectionType.SINGLE && selectedAnswers.size > 1) {
            throw IllegalArgumentException("Only one answer allowed for question $questionId")
        }

        // Validate that all requested answer IDs were found
        val foundAnswerIds = selectedAnswers.mapNotNull { it.id }.toSet()
        val requestedAnswerIds = answerRequest.selectedAnswerIds.toSet()
        val missingAnswerIds = requestedAnswerIds - foundAnswerIds

        if (missingAnswerIds.isNotEmpty()) {
            throw IllegalArgumentException("Invalid answer IDs for question $questionId: $missingAnswerIds")
        }

        val answer = ClosedUserAnswer(
            submittedSurvey = submittedSurvey,
            question = question
        )

        selectedAnswers.forEach { answer.addSelectedAnswer(it) }
        return answer
    }
}
