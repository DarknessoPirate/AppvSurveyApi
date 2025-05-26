package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CopySurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.ApiResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.AccessCodeResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyDetailResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
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
    fun createSurvey(@Valid @RequestBody request: CreateSurveyRequest): ResponseEntity<SurveyResponse> {
        val survey = surveyService.createSurvey(request)
        val createdSurvey = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(createdSurvey)
    }

    @PostMapping("/with-questions")
    fun createSurveyWithQuestions(@Valid @RequestBody request: CreateSurveyWithQuestionsRequest): ResponseEntity<SurveyDetailResponse> {
        val survey = surveyService.createSurveyWithQuestions(request)
        val createdSurveyDetailed = surveyMapper.toDetailResponse(survey)

        return ResponseEntity.ok(createdSurveyDetailed)

    }

    @GetMapping("/{id}")
    fun getSurvey(@PathVariable id: Long): ResponseEntity<SurveyDetailResponse> {
        val surveyFound = surveyService.findWithQuestions(id)
        val surveyDetailedResponse = surveyMapper.toDetailResponse(surveyFound)

        return ResponseEntity.ok(surveyDetailedResponse)
    }

    @GetMapping("/access-code/{accessCode}")
    fun getSurveyByAccessCode(@PathVariable accessCode: String): ResponseEntity<SurveyDetailResponse> {
        val survey = surveyService.findByAccessCode(accessCode)
        val detailedSurveyResponse = surveyMapper.toDetailResponse(survey)

        return ResponseEntity.ok(detailedSurveyResponse)
    }

    @GetMapping
    fun getActiveSurveys(): ResponseEntity<List<SurveyResponse>> {
        val activeSurveys = surveyService.findActiveSurveys()
        val response = activeSurveys.map { surveyMapper.toResponse(it) }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/expiring")
    fun getExpiringSurveys(@RequestParam(defaultValue = "7") days: Int): ResponseEntity<List<SurveyResponse>> {
        val surveys = surveyService.findExpiringSoon(days)
        val response = surveys.map { surveyMapper.toResponse(it) }

        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    fun updateSurvey(@PathVariable id: Long, @Valid @RequestBody request: CreateSurveyRequest): ResponseEntity<SurveyResponse> {
        val updatedSurvey = surveyService.updateSurvey(id, request)
        val updatedSurveyResponse = surveyMapper.toResponse(updatedSurvey)

        return ResponseEntity.ok(updatedSurveyResponse)
    }

    @PostMapping("/{id}/access-code")
    fun generateAccessCode(@PathVariable id: Long): ResponseEntity<AccessCodeResponse> {
            val accessCode = surveyService.generateAccessCode(id)

            return ResponseEntity.ok(AccessCodeResponse(accessCode))
    }

    @PostMapping("/{id}/copy")
    fun copySurvey(@PathVariable id: Long, @Valid @RequestBody request: CopySurveyRequest): ResponseEntity<SurveyResponse> {
        val survey = surveyService.copySurvey(id, request.newTitle, request.includeAccessCode)
        val copiedSurveyResponse = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(copiedSurveyResponse)
    }

    @GetMapping("/{id}/statistics")
    fun getSurveyStatistics(@PathVariable id: Long): ResponseEntity<SurveyStatisticsResponse> {
        val stats = surveyService.getStatistics(id)
        val statisticsResponse = surveyMapper.toResponse(stats)

        return ResponseEntity.ok(statisticsResponse)
    }

    @PutMapping("/{id}/active")
    fun setActive(@PathVariable id: Long, @RequestParam isActive: Boolean): ResponseEntity<SurveyResponse> {
        val survey = surveyService.setActive(id, isActive)
        val updatedSurveyResponse = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(updatedSurveyResponse)
    }

    @DeleteMapping("/{id}")
    fun deleteSurvey(@PathVariable id: Long): ResponseEntity<String> {
        surveyService.deleteSurvey(id)

        return ResponseEntity.noContent().build()
    }

}