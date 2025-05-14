package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.SurveyCreateRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.model.Survey
import org.springframework.stereotype.Component

@Component
class SurveyMapper(private val questionMapper: QuestionMapper) {

    fun toEntity(request: SurveyCreateRequest): Survey {
        val survey = Survey(
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt,
            isActive = request.isActive
        )

        request.questions?.forEach { questionRequest ->
            val question = questionMapper.toEntity(questionRequest)
            survey.addQuestion(question)
        }

        return survey
    }

    fun toResponse(survey: Survey, includeQuestions: Boolean = true): SurveyResponse {
        return SurveyResponse(
            id = survey.id,
            title = survey.title,
            description = survey.description,
            createdAt = survey.createdAt,
            expiresAt = survey.expiresAt,
            isActive = survey.isActive,
            accessCode = survey.accessCode,
            questions = if (includeQuestions) survey.questions.map { questionMapper.toResponse(it) } else null
        )
    }

    fun toResponseList(surveys: List<Survey>, includeQuestions: Boolean = false): List<SurveyResponse> {
        return surveys.map { toResponse(it, includeQuestions) }
    }
}