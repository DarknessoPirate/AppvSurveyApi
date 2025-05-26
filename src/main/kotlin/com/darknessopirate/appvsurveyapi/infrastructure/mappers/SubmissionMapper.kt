package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.response.submit.*
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.model.SubmissionSummary
import org.hibernate.LazyInitializationException
import org.springframework.stereotype.Component

@Component
class SubmissionMapper(private val questionMapper: QuestionMapper) {
    fun toResponse(entity: SubmittedSurvey): SubmittedSurveyResponse = SubmittedSurveyResponse(
        id = entity.id!!,
        surveyId = entity.survey.id!!,
        surveyTitle = entity.survey.title,
        submittedAt = entity.submittedAt,
        answerCount = entity.userAnswers.size
    )

    fun toDetailResponse(entity: SubmittedSurvey): SubmissionDetailResponse = SubmissionDetailResponse(
        id = entity.id!!,
        surveyId = entity.survey.id!!,
        surveyTitle = entity.survey.title,
        submittedAt = entity.submittedAt,
        answers = entity.userAnswers.map { toResponse(it) }
    )

    fun toResponse(entity: UserAnswer): UserAnswerResponse = when (entity) {
        is OpenUserAnswer -> OpenUserAnswerResponse(
            id = entity.id!!,
            questionName = entity.question.text,
            textValue = entity.textValue
        )
        is ClosedUserAnswer -> {
            val closedQuestion = entity.question as ClosedQuestion
            ClosedUserAnswerResponse(
                id = entity.id!!,
                questionName = entity.question.text,
                selectedAnswers = try {
                    entity.selectedAnswers.map { questionMapper.toResponse(it) }
                } catch (e: LazyInitializationException) {
                    emptyList() // fallback if selectedAnswers not loaded
                },
                selectionType = closedQuestion.selectionType
            )
        }
        else -> throw IllegalArgumentException("Unknown answer type")
    }

    fun toResponse(entity: OpenUserAnswer): OpenUserAnswerResponse = OpenUserAnswerResponse(
        id = entity.id!!,
        questionName = entity.question.text,
        textValue = entity.textValue
    )

    fun toResponse(entity: SubmissionSummary): SubmissionSummaryResponse = SubmissionSummaryResponse(
        surveyId = entity.surveyId,
        surveyTitle = entity.surveyTitle,
        totalSubmissions = entity.totalSubmissions,
        submissionsLast24Hours = entity.submissionsLast24Hours,
        submissionsLast7Days = entity.submissionsLast7Days,
        averagePerDay = entity.averagePerDay
    )
}