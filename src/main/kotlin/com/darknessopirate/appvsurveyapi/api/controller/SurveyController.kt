package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.CreateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.UpdateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CopySurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.accessCode.AccessCodeListResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.accessCode.AccessCodeResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyDetailResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.AccessCodeMapper
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: ISurveyService,
    private val surveyMapper: SurveyMapper,
    private val accessCodeService: IAccessCodeService,
    private val accessCodeMapper: AccessCodeMapper,
) {

    @PostMapping("/with-questions")
    fun createSurveyWithQuestions(@Valid @RequestBody request: CreateSurveyWithQuestionsRequest): ResponseEntity<SurveyDetailResponse> {
        val survey = surveyService.createSurveyWithSelectedQuestions(request)
        val createdSurveyDetailed = surveyMapper.toDetailResponse(survey)

        return ResponseEntity.ok(createdSurveyDetailed)
    }

    @GetMapping("/{id}")
    fun getSurvey(@PathVariable id: Long): ResponseEntity<SurveyDetailResponse> {
        val surveyFound = surveyService.findWithQuestions(id)
        val surveyDetailedResponse = surveyMapper.toDetailResponse(surveyFound)

        return ResponseEntity.ok(surveyDetailedResponse)
    }


    @GetMapping("/all")
    fun getAllSurveys(): ResponseEntity<List<SurveyResponse>> {
        val allSurveys = surveyService.findAllSurveys()
        val response = allSurveys.map { surveyMapper.toResponse(it) }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/all/page")
    fun getAllSurveysPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ) : ResponseEntity<PaginatedResponse<SurveyResponse>>
    {
        val pageResponse = surveyService.getSurveysPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }


    @PostMapping("/{id}/copy")
    fun copySurvey(@PathVariable id: Long, @Valid @RequestBody request: CopySurveyRequest): ResponseEntity<SurveyResponse> {
        val survey = surveyService.copySurvey(id, request.newTitle)
        val copiedSurveyResponse = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(copiedSurveyResponse)
    }

    @PatchMapping("/{id}/active")
    fun toggleActive(@PathVariable id: Long): ResponseEntity<Unit> {
        surveyService.toggleActive(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun deleteSurvey(@PathVariable id: Long): ResponseEntity<String> {
        surveyService.deleteSurvey(id)

        return ResponseEntity.noContent().build()
    }

    //
    // ACCESS CODE ENDPOINTS
    //
    @PostMapping("/{id}/access-codes")
    fun createAccessCode(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateAccessCodeRequest
    ): ResponseEntity<AccessCodeResponse> {
        val accessCode = accessCodeService.createAccessCode(id, request)
        val response = accessCodeMapper.toResponse(accessCode)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}/access-codes")
    fun getAccessCodes(@PathVariable id: Long): ResponseEntity<AccessCodeListResponse> {
        val accessCodes = accessCodeService.getAccessCodesBySurvey(id)
        val response = AccessCodeListResponse(
            accessCodes = accessCodes.map { accessCodeMapper.toResponse(it) }
        )
        return ResponseEntity.ok(response)
    }

    @PutMapping("/access-codes/{codeId}")
    fun updateAccessCode(
        @PathVariable codeId: Long,
        @Valid @RequestBody request: UpdateAccessCodeRequest
    ): ResponseEntity<AccessCodeResponse> {
        val accessCode = accessCodeService.updateAccessCode(codeId, request)
        val response = accessCodeMapper.toResponse(accessCode)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/access-codes/{codeId}")
    fun deleteAccessCode(@PathVariable codeId: Long): ResponseEntity<Void> {
        accessCodeService.deleteAccessCode(codeId)
        return ResponseEntity.noContent().build()
    }


    @GetMapping("/access-code/{accessCode}")
    fun getSurveyByAccessCode(@PathVariable accessCode: String): ResponseEntity<SurveyDetailResponse> {

        val survey = surveyService.findByAccessCode(accessCode)
        val detailedSurveyResponse = surveyMapper.toDetailResponse(survey)
        return ResponseEntity.ok(detailedSurveyResponse)
    }

}