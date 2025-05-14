package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.response.SubmittedSurveyResponse
import com.darknessopirate.appvsurveyapi.domain.model.SubmittedSurvey
import org.springframework.stereotype.Component

@Component
class SubmittedSurveyMapper(private val userAnswerMapper: UserAnswerMapper) {

    fun toResponse(submittedSurvey: SubmittedSurvey): SubmittedSurveyResponse {
        return SubmittedSurveyResponse(
            id = submittedSurvey.id,
            surveyId = submittedSurvey.survey.id!!,
            submittedAt = submittedSurvey.submittedAt,
            userAnswers = submittedSurvey.userAnswers.map { userAnswerMapper.toResponse(it) }
        )
    }

    fun toResponseList(submittedSurveys: List<SubmittedSurvey>): List<SubmittedSurveyResponse> {
        return submittedSurveys.map { toResponse(it) }
    }
}