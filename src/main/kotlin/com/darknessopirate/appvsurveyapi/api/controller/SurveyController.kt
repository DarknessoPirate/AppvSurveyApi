package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CopySurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.UpdateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.ApiResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyDetailResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySubmissionService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import com.darknessopirate.appvsurveyapi.infrastructure.service.QuestionServiceImpl
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: ISurveyService,
    private val surveyMapper: SurveyMapper,
) {

    @PostMapping
    fun createSurvey(@Valid @RequestBody request: CreateSurveyRequest): ResponseEntity<ApiResponse<SurveyResponse>> {
        return try {
            val survey = surveyService.createSurvey(request)
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toResponse(survey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to create survey"))
        }
    }

    @PostMapping("/with-questions")
    fun createSurveyWithQuestions(@Valid @RequestBody request: CreateSurveyWithQuestionsRequest): ResponseEntity<ApiResponse<SurveyDetailResponse>> {
        return try {
            val survey = surveyService.createSurveyWithQuestions(request)
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toDetailResponse(survey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to create survey"))
        }
    }

    @GetMapping("/{id}")
    @Transactional
    fun getSurvey(@PathVariable id: Long): ResponseEntity<ApiResponse<SurveyDetailResponse>> {
        return surveyService.findWithQuestions(id)?.let { survey ->
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toDetailResponse(survey)))
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/access-code/{accessCode}")
    fun getSurveyByAccessCode(@PathVariable accessCode: String): ResponseEntity<ApiResponse<SurveyDetailResponse>> {
        return surveyService.findByAccessCode(accessCode)?.let { survey ->
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toDetailResponse(survey)))
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping
    fun getActiveSurveys(): ResponseEntity<ApiResponse<List<SurveyResponse>>> {
        val surveys = surveyService.findActiveSurveys().map { surveyMapper.toResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(surveys))
    }

    @GetMapping("/expiring")
    fun getExpiringSurveys(@RequestParam(defaultValue = "7") days: Int): ResponseEntity<ApiResponse<List<SurveyResponse>>> {
        val surveys = surveyService.findExpiringSoon(days).map { surveyMapper.toResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(surveys))
    }

    @PutMapping("/{id}")
    fun updateSurvey(@PathVariable id: Long, @Valid @RequestBody request: UpdateSurveyRequest): ResponseEntity<ApiResponse<SurveyResponse>> {
        return try {
            val survey = surveyService.findWithQuestions(id)
                ?: return ResponseEntity.notFound().build()

            // Update individual fields (implementation would need additional service methods)
            val updatedSurvey = survey.copy(
                title = request.title ?: survey.title,
                description = request.description ?: survey.description,
                expiresAt = request.expiresAt ?: survey.expiresAt,
                isActive = request.isActive ?: survey.isActive
            )

            ResponseEntity.ok(ApiResponse.success(surveyMapper.toResponse(updatedSurvey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to update survey"))
        }
    }

    @PostMapping("/{id}/access-code")
    fun generateAccessCode(@PathVariable id: Long): ResponseEntity<ApiResponse<SurveyResponse>> {
        return try {
            val survey = surveyService.generateAccessCode(id)
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toResponse(survey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to generate access code"))
        }
    }

    @PostMapping("/{id}/copy")
    fun copySurvey(@PathVariable id: Long, @Valid @RequestBody request: CopySurveyRequest): ResponseEntity<ApiResponse<SurveyResponse>> {
        return try {
            val survey = surveyService.copySurvey(id, request.newTitle, request.includeAccessCode)
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toResponse(survey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to copy survey"))
        }
    }

    @GetMapping("/{id}/statistics")
    fun getSurveyStatistics(@PathVariable id: Long): ResponseEntity<ApiResponse<SurveyStatisticsResponse>> {
        return try {
            val stats = surveyService.getStatistics(id)
            val response = surveyMapper.toResponse(stats)
            ResponseEntity.ok(ApiResponse.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to get statistics"))
        }
    }

    @PutMapping("/{id}/active")
    fun setActive(@PathVariable id: Long, @RequestParam isActive: Boolean): ResponseEntity<ApiResponse<SurveyResponse>> {
        return try {
            val survey = surveyService.setActive(id, isActive)
            ResponseEntity.ok(ApiResponse.success(surveyMapper.toResponse(survey)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to update survey status"))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteSurvey(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            surveyService.deleteSurvey(id)
            ResponseEntity.ok(ApiResponse.success("Survey deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to delete survey"))
        }
    }
}