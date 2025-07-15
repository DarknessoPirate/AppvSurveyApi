package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.UpdateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyDetailResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySummaryService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class SurveyMapper(
    private val questionMapper: QuestionMapper,
    private val accessCodeMapper: AccessCodeMapper,
    private val surveySummaryService: ISurveySummaryService
){
    /*
    * REQUESTS
    */

    // CreateSurveyRequest -> Survey
    fun toEntity(request: UpdateSurveyRequest): Survey {
        return Survey(
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt,
            isActive = false
        )
    }

    fun toEntity(request: CreateSurveyWithQuestionsRequest): Survey {
        return Survey(
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt,
            isActive = false
        )
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
        accessCodeCount = entity.accessCodes.size, // Changed from accessCode to accessCodeCount
        questionCount = entity.questions.size,
        hasSummaryPassword = surveySummaryService.passwordExists(entity.id!!)
    )

    // Survey -> SurveyDetailResponse
    fun toDetailResponse(entity: Survey): SurveyDetailResponse = SurveyDetailResponse(
        id = entity.id!!,
        title = entity.title,
        description = entity.description,
        createdAt = entity.createdAt,
        expiresAt = entity.expiresAt,
        isActive = entity.isActive,
        accessCodes = entity.accessCodes.map { accessCodeMapper.toResponse(it) }, // Changed to list of access codes
        questions = entity.questions.map { questionMapper.toResponse(it) },
        hasSummaryPassword = surveySummaryService.passwordExists(entity.id!!)
    )

    fun toPageResponse(page: Page<Survey>): PaginatedResponse<SurveyResponse> {
        val mappedContent = page.content.map { toResponse(it) }
        return PaginatedResponse(
            data = mappedContent,
            currentPage = page.number,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            pageSize = page.size,
            isFirst = page.isFirst,
            isLast = page.isLast,
        )
    }

}