package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.SubmittedSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.SubmittedSurveyResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.UserAnswerResponse
import com.darknessopirate.appvsurveyapi.domain.service.QuestionService
import com.darknessopirate.appvsurveyapi.domain.service.SubmittedSurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SubmittedSurveyMapper
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.UserAnswerMapper
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/submissions")
class SubmittedSurveyController(
    private val submittedSurveyService: SubmittedSurveyService,
    private val questionService: QuestionService,
    private val submittedSurveyMapper: SubmittedSurveyMapper,
    private val userAnswerMapper: UserAnswerMapper
) {
    @PostMapping
    fun submitSurvey(@RequestBody request: SubmittedSurveyRequest): ResponseEntity<SubmittedSurveyResponse> {
        // Process user answers
        val userAnswers = request.userAnswers.map { userAnswerRequest ->
            val question = questionService.getQuestionById(userAnswerRequest.questionId)

            // Get selected answers from the question
            val selectedAnswers = if (userAnswerRequest.selectedAnswerIds.isNotEmpty()) {
                question.possibleAnswers.filter { it.id in userAnswerRequest.selectedAnswerIds }
            } else {
                emptyList()
            }

            userAnswerMapper.toEntity(userAnswerRequest, question, selectedAnswers)
        }

        // Create response
        val response = submittedSurveyService.createSubmission(request.surveyId, userAnswers)
        return ResponseEntity.ok(submittedSurveyMapper.toResponse(response))
    }

    @GetMapping("/{id}")
    fun getSubmission(@PathVariable id: Long): ResponseEntity<SubmittedSurveyResponse> {
        val response = submittedSurveyService.getSubmissionById(id)
        return ResponseEntity.ok(submittedSurveyMapper.toResponse(response))
    }

    @GetMapping("/survey/{surveyId}")
    fun getSubmissionsBySurvey(@PathVariable surveyId: Long): ResponseEntity<List<SubmittedSurveyResponse>> {
        val responses = submittedSurveyService.getSubmissionsBySurveyId(surveyId)
        return ResponseEntity.ok(submittedSurveyMapper.toResponseList(responses))
    }

    @GetMapping("/timerange")
    fun getSubmissionsByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startTime: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endTime: LocalDateTime
    ): ResponseEntity<List<SubmittedSurveyResponse>> {
        val responses = submittedSurveyService.getSubmissionsByTimeRange(startTime, endTime)
        return ResponseEntity.ok(submittedSurveyMapper.toResponseList(responses))
    }

    @GetMapping("/question/{questionId}/answers")
    fun getUserAnswersByQuestion(@PathVariable questionId: Long): ResponseEntity<List<UserAnswerResponse>> {
        val userAnswers = submittedSurveyService.getUserAnswersByQuestionId(questionId)
        return ResponseEntity.ok(userAnswerMapper.toResponseList(userAnswers))
    }
}