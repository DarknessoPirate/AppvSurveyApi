package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.SubmitByAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.SubmitSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.ApiResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.submit.*
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySubmissionService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/submissions")
@Validated
class SubmissionController(
    private val submissionService: ISurveySubmissionService
) {

    /*
    @PostMapping("/survey/{surveyId}")
    fun submitSurvey(@PathVariable surveyId: Long, @Valid @RequestBody request: SubmitSurveyRequest): ResponseEntity<ApiResponse<SubmittedSurveyResponse>> {
        return try {
            val submission = submissionService.submitSurvey(surveyId, request.answers)
            ResponseEntity.ok(ApiResponse.success(submission.toResponse()))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Survey not available for submission"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Invalid submission data"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to submit survey"))
        }
    }

    @PostMapping("/access-code")
    fun submitByAccessCode(@Valid @RequestBody request: SubmitByAccessCodeRequest): ResponseEntity<ApiResponse<SubmittedSurveyResponse>> {
        return try {
            val submission = submissionService.submitByAccessCode(request.accessCode, request.answers)
            ResponseEntity.ok(ApiResponse.success(submission.toResponse()))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Survey not available for submission"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Invalid submission data"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to submit survey"))
        }
    }

    @GetMapping("/{id}")
    fun getSubmission(@PathVariable id: Long): ResponseEntity<ApiResponse<SubmissionDetailResponse>> {
        return submissionService.getSubmission(id)?.let { submission ->
            ResponseEntity.ok(ApiResponse.success(submission.toDetailResponse()))
        } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/survey/{surveyId}")
    fun getSubmissions(@PathVariable surveyId: Long): ResponseEntity<ApiResponse<List<SubmittedSurveyResponse>>> {
        val submissions = submissionService.getSubmissions(surveyId).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(submissions))
    }

    @GetMapping("/survey/{surveyId}/range")
    fun getSubmissionsInRange(
        @PathVariable surveyId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<ApiResponse<List<SubmittedSurveyResponse>>> {
        val submissions = submissionService.getSubmissions(surveyId, startDate, endDate).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(submissions))
    }

    @GetMapping("/question/{questionId}/answers")
    fun getAnswersForQuestion(@PathVariable questionId: Long): ResponseEntity<ApiResponse<List<UserAnswerResponse>>> {
        val answers = submissionService.getAnswersForQuestion(questionId).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(answers))
    }

    @GetMapping("/question/{questionId}/statistics")
    fun getAnswerStatistics(@PathVariable questionId: Long): ResponseEntity<ApiResponse<List<AnswerStatistic>>> {
        return try {
            val statistics = submissionService.getAnswerStatistics(questionId)
            ResponseEntity.ok(ApiResponse.success(statistics))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Statistics not available for this question type"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to get statistics"))
        }
    }


    @GetMapping("/search")
    fun searchTextResponses(@RequestParam query: String): ResponseEntity<ApiResponse<List<OpenUserAnswerResponse>>> {
        val answers = submissionService.searchTextResponses(query).map { it.toResponse() }
        return ResponseEntity.ok(ApiResponse.success(answers))
    }

    @GetMapping("/survey/{surveyId}/summary")
    fun getSubmissionSummary(@PathVariable surveyId: Long): ResponseEntity<ApiResponse<SubmissionSummaryResponse>> {
        return try {
            val summary = submissionService.getSubmissionSummary(surveyId)
            ResponseEntity.ok(ApiResponse.success(summary.toResponse()))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to get submission summary"))
        }
    }

     */
}