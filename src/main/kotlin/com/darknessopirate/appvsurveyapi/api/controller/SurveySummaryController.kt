package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.survey.GetSummaryRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.SetSummaryPasswordRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.PasswordExistsResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SummaryPasswordResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySummaryService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/surveys")
@Validated
class SurveySummaryController(
    private val surveySummaryService: ISurveySummaryService
) {

    @PostMapping("/{id}/summary-password")
    fun setSummaryPassword(
        @PathVariable id: Long,
        @Valid @RequestBody request: SetSummaryPasswordRequest
    ): ResponseEntity<SummaryPasswordResponse> {
        val passwordEntity = surveySummaryService.setSummaryPassword(id, request.password)

        val response = SummaryPasswordResponse(
            exists = true,
            createdAt = passwordEntity.createdAt,
            updatedAt = passwordEntity.updatedAt
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}/summary-password")
    fun getSummaryPassword(@PathVariable id: Long): ResponseEntity<SummaryPasswordResponse> {
        val passwordEntity = surveySummaryService.getSummaryPassword(id)

        return if (passwordEntity != null) {
            val response = SummaryPasswordResponse(
                exists = true,
                password = passwordEntity.password,
                createdAt = passwordEntity.createdAt,
                updatedAt = passwordEntity.updatedAt
            )
            ResponseEntity.ok(response)
        } else {
            val response = SummaryPasswordResponse(exists = false)
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("/{id}/summary-password/exists")
    fun checkPasswordExists(@PathVariable id: Long): ResponseEntity<PasswordExistsResponse> {
        val exists = surveySummaryService.passwordExists(id)
        val response = PasswordExistsResponse(exists = exists)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/summary")
    fun getSurveyStatistics(
        @PathVariable id: Long,
        @Valid @RequestBody request: GetSummaryRequest
    ): ResponseEntity<SurveyStatisticsResponse> {
        return try {
            val statistics = surveySummaryService.getSurveyStatistics(id, request.password)
            ResponseEntity.ok(statistics)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    // Optional: Admin endpoint to get statistics without password TODO: ensure auth works here
    @GetMapping("/{id}/summary/admin")
    fun getSurveyStatisticsAdmin(@PathVariable id: Long): ResponseEntity<SurveyStatisticsResponse> {
        return try {
            val statistics = surveySummaryService.generateSurveyStatistics(id)
            ResponseEntity.ok(statistics)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}