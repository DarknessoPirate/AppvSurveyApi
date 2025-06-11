package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.SubmitByAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.submit.SubmitSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.submit.*
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySubmissionService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SubmissionMapper
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
    private val submissionService: ISurveySubmissionService,
    private val submissionMapper: SubmissionMapper
) {

    @PostMapping("/access-code")
    fun submitByAccessCode(@Valid @RequestBody request: SubmitByAccessCodeRequest): ResponseEntity<SubmittedSurveyResponse> {
        val submission = submissionService.submitByAccessCode(request.accessCode, request.answers)
        val submissionResponse = submissionMapper.toResponse(submission)

        return ResponseEntity.ok(submissionResponse)
    }

    @GetMapping("/{id}")
    fun getSubmission(@PathVariable id: Long): ResponseEntity<SubmissionDetailResponse> {
        val submission = submissionService.getSubmission(id)
            ?: return ResponseEntity.notFound().build()

        val submissionDetailResponse = submissionMapper.toDetailResponse(submission)
        return ResponseEntity.ok(submissionDetailResponse)
    }

    @GetMapping("/survey/{surveyId}")
    fun getSubmissions(@PathVariable surveyId: Long): ResponseEntity<List<SubmittedSurveyResponse>> {
        val submissions = submissionService.getSubmissions(surveyId)
        val submissionsResponse = submissions.map { submissionMapper.toResponse(it) }

        return ResponseEntity.ok(submissionsResponse)
    }

    @GetMapping("/survey/{surveyId}/range")
    fun getSubmissionsInRange(
        @PathVariable surveyId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<SubmittedSurveyResponse>> {
        val submissions = submissionService.getSubmissions(surveyId, startDate, endDate)
        val submissionsResponse = submissions.map { submissionMapper.toResponse(it) }

        return ResponseEntity.ok(submissionsResponse)
    }

    @GetMapping("/access-code/{accessCode}/detailed")
    fun getSubmissionsByAccessCodeWithAnswers(@PathVariable accessCode: String): ResponseEntity<List<SubmissionDetailResponse>> {
        val submissions = submissionService.getSubmissionsByAccessCodeWithAnswers(accessCode)
        val submissionsResponse = submissions.map { submissionMapper.toDetailResponse(it) }

        return ResponseEntity.ok(submissionsResponse)
    }

    @GetMapping("/access-code/{accessCode}")
    fun getSubmissionsByAccessCode(@PathVariable accessCode: String): ResponseEntity<List<SubmittedSurveyResponse>> {
        val submissions = submissionService.getSubmissionsByAccessCode(accessCode)
        val submissionsResponse = submissions.map { submissionMapper.toResponse(it) }

        return ResponseEntity.ok(submissionsResponse)
    }

    @GetMapping("/question/{questionId}/answers")
    fun getAnswersForQuestion(@PathVariable questionId: Long): ResponseEntity<List<UserAnswerResponse>> {
        val answers = submissionService.getAnswersForQuestion(questionId)
        val answersResponse = answers.map { submissionMapper.toResponse(it) }

        return ResponseEntity.ok(answersResponse)
    }

    @GetMapping("/survey/{surveyId}/summary")
    fun getSubmissionSummary(@PathVariable surveyId: Long): ResponseEntity<SubmissionSummaryResponse> {
        val summary = submissionService.getSubmissionSummary(surveyId)
        val summaryResponse = submissionMapper.toResponse(summary)

        return ResponseEntity.ok(summaryResponse)
    }
}