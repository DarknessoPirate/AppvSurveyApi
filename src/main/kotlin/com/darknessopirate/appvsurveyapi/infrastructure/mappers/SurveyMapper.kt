package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyDetailResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import org.springframework.stereotype.Component

@Component
class SurveyMapper(private val questionMapper: QuestionMapper) {
    /*
    * REQUESTS
    */

    // CreateSurveyRequest -> Survey
    fun toEntity(request: CreateSurveyRequest) : Survey {
        return Survey(
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt ,
        )

    }

    // CreateSurveyWithQuestionsRequest -> (Survey,List<Question>)
    fun toEntity(request: CreateSurveyWithQuestionsRequest): Pair<Survey, List<Question>> {
        val surveyEntity = Survey(
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt ,
        )
        val questions = questionMapper.toEntity(request.questions)
        return Pair(surveyEntity, questions)
    }

    /*
    * RESPONSES
    */

    // Survey -> SurveyResponse
    fun toResponse(entity: Survey): SurveyResponse = SurveyResponse(
        id = entity.id!!,
        title = entity.title,
        description = entity.description,
        createdAt = entity.createdAt,
        expiresAt = entity.expiresAt,
        isActive = entity.isActive,
        accessCode = entity.accessCode,
        questionCount = entity.questions.size
    )

    // Survey -> SurveyDetailResponse
    fun toDetailResponse(entity: Survey): SurveyDetailResponse = SurveyDetailResponse(
        id = entity.id!!,
        title = entity.title,
        description = entity.description,
        createdAt = entity.createdAt,
        expiresAt = entity.expiresAt,
        isActive = entity.isActive,
        accessCode = entity.accessCode,
        questions = entity.questions.map { questionMapper.toResponse(it) }
    )

    fun toResponse(entity: SurveyStatistics): SurveyStatisticsResponse = SurveyStatisticsResponse(
        surveyId = entity.surveyId,
        title = entity.title,
        totalSubmissions = entity.totalSubmissions,
        questionCounts = entity.questionCounts,
        isActive = entity.isActive,
        expiresAt = entity.expiresAt,
        createdAt = entity.createdAt
    )
}