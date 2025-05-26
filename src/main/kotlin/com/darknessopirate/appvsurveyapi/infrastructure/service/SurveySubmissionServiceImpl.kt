package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.AnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.ClosedAnswerRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.OpenAnswerRequest
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import com.darknessopirate.appvsurveyapi.domain.model.SubmissionSummary
import com.darknessopirate.appvsurveyapi.domain.repository.question.QuestionAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.repository.question.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.userAnswer.ClosedUserAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.repository.userAnswer.OpenUserAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySubmissionService
import jakarta.persistence.EntityNotFoundException
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
    private val questionAnswerRepository: QuestionAnswerRepository
) : ISurveySubmissionService {

    /**
     * Submit responses to a survey
     */
    override fun submitSurvey(
        surveyId: Long,
        answers: List<AnswerRequest>
    ): SubmittedSurvey {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        // Validate survey accepts submissions
        if (!survey.isActive) {
            throw IllegalStateException("Survey is not active")
        }
        if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDateTime.now())) {
            throw IllegalStateException("Survey has expired")
        }

        // Validate all required questions are answered
        validateRequiredQuestions(survey, answers)

        // Create submitted survey
        val submittedSurvey = SubmittedSurvey(survey = survey)
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

    /**
     * Submit by access code
     */
    override fun submitByAccessCode(
        accessCode: String,
        answers: List<AnswerRequest>
    ): SubmittedSurvey {
        val survey = surveyRepository.findByAccessCodeWithQuestions(accessCode)
            ?: throw EntityNotFoundException("Survey not found with access code: $accessCode")

        return submitSurvey(survey.id!!, answers)
    }

    /**
     * Get submission by ID
     */
    override fun getSubmission(submissionId: Long): SubmittedSurvey? {
        return submittedSurveyRepository.findByIdWithSurveyAndAnswers(submissionId)
    }

    /**
     * Get all submissions for a survey
     */
    override fun getSubmissions(surveyId: Long): List<SubmittedSurvey> {
        return submittedSurveyRepository.findBySurveyIdWithSurvey(surveyId)
    }

    /**
     * Get submissions in date range
     */
    override fun getSubmissions(
        surveyId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<SubmittedSurvey> {
        return submittedSurveyRepository.findBySurveyIdWithSurvey(surveyId)
            .filter { it.submittedAt.isAfter(startDate) && it.submittedAt.isBefore(endDate) }
    }

    /**
     * Get user answers for a question
     */
    override fun getAnswersForQuestion(questionId: Long): List<UserAnswer> {
        return when (val question = questionRepository.findById(questionId).orElse(null)) {
            is OpenQuestion -> openUserAnswerRepository.findByQuestionId(questionId)
            is ClosedQuestion -> closedUserAnswerRepository.findByQuestionWithSelections(questionId)
            else -> emptyList()
        }
    }

    /**
     * Get answer statistics for a closed question
     */
    override fun getAnswerStatistics(questionId: Long): List<AnswerStatistic> {
        val question = questionRepository.findById(questionId).orElse(null)
            ?: throw EntityNotFoundException("Question not found: $questionId")

        if (question !is ClosedQuestion) {
            throw IllegalArgumentException("Statistics only available for closed questions")
        }

        val stats = closedUserAnswerRepository.getStatistics(questionId)
        return stats.map { stat ->
            AnswerStatistic(
                answerId = stat[0] as Long,
                answerText = stat[1] as String,
                count = (stat[2] as Long).toInt()
            )
        }
    }

    /**
     * Search text responses
     */
    override fun searchTextResponses(searchText: String): List<OpenUserAnswer> {
        return openUserAnswerRepository.searchByText(searchText)
    }

    /**
     * Get submission summary
     */
    override fun getSubmissionSummary(surveyId: Long): SubmissionSummary {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }

        val submissions = submittedSurveyRepository.findBySurveyId(surveyId)
        val now = LocalDateTime.now()
        val last24Hours = submissions.count { it.submittedAt.isAfter(now.minusDays(1)) }
        val last7Days = submissions.count { it.submittedAt.isAfter(now.minusDays(7)) }

        return SubmissionSummary(
            surveyId = surveyId,
            surveyTitle = survey.title,
            totalSubmissions = submissions.size,
            submissionsLast24Hours = last24Hours,
            submissionsLast7Days = last7Days,
            averagePerDay = if (submissions.isEmpty()) 0.0
            else submissions.size / ChronoUnit.DAYS.between(survey.createdAt, now).coerceAtLeast(1).toDouble()
        )
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
