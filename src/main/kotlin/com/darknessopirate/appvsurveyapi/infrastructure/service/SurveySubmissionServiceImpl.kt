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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    override fun submitByAccessCode(
        accessCode: String,
        answers: List<AnswerRequest>
    ): SubmittedSurvey {
        val accessCodeEntity = accessCodeService.validateAccessCode(accessCode)
            ?: throw EntityNotFoundException("Invalid or expired access code: $accessCode")

        val survey = surveyRepository.findByIdWithQuestions(accessCodeEntity.survey?.id!!)
            ?: throw EntityNotFoundException("Survey not found for access code: $accessCode")

        if (!survey.isActive) {
            throw IllegalStateException("Survey is not active")
        }

        if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDateTime.now())) {
            throw IllegalStateException("Survey has expired")
        }

        // Increment usage count
        accessCodeEntity.id?.let { accessCodeService.incrementUsage(it) }

        // Pass the access code entity to link it properly
        return submitSurveyWithAccessCode(survey.id!!, answers, accessCodeEntity)
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
            if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDateTime.now())) {
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
        val savedSubmission = submittedSurveyRepository.save(submittedSurvey)

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

        return submittedSurveyRepository.save(savedSubmission)
    }

    // Get submission by ID
    override fun getSubmission(submissionId: Long): SubmittedSurvey? {
        val submission = submittedSurveyRepository.findByIdWithSurveyAndAnswers(submissionId)
            ?: return null

        // Initialize lazy-loaded collections
        Hibernate.initialize(submission.userAnswers)

        // For each user answer, if it's a closed answer, initialize selected answers
        submission.userAnswers.forEach { userAnswer ->
            if (userAnswer is ClosedUserAnswer) {
                try {
                    Hibernate.initialize(userAnswer.selectedAnswers)
                    println("✅ Loaded ${userAnswer.selectedAnswers.size} selected answers for question ${userAnswer.question.id}")
                } catch (e: Exception) {
                    println("❌ Failed to load selected answers for question ${userAnswer.question.id}: ${e.message}")
                }
            }
        }

        return submission
    }

    override fun getSubmissionsByAccessCode(accessCode: String): List<SubmittedSurvey> {
        return submittedSurveyRepository.findByAccessCodeWithAnswers(accessCode)
    }

    // Helper methods
    private fun validateRequiredQuestions(survey: Survey, answers: List<AnswerRequest>) {
        val requiredQuestionIds = survey.questions.filter { it.required }.map { it.id }
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
            throw IllegalArgumentException("Text answer required for question ${question.id}")
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
        if (answerRequest.selectedAnswerIds.isEmpty() && question.required) {
            throw IllegalArgumentException("Answer selection required for question ${question.id}")
        }

        val selectedAnswers = question.possibleAnswers.filter {
            it.id in answerRequest.selectedAnswerIds
        }.toMutableList()

        // Validate selection type constraints
        if (question.selectionType == SelectionType.SINGLE && selectedAnswers.size > 1) {
            throw IllegalArgumentException("Only one answer allowed for question ${question.id}")
        }

        val answer = ClosedUserAnswer(
            submittedSurvey = submittedSurvey,
            question = question
        )

        selectedAnswers.forEach { answer.addSelectedAnswer(it) }
        return answer
    }
}
