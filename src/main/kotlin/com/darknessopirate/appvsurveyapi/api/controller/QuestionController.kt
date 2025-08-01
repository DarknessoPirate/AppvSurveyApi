package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.question.ClosedQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.OpenQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionCountsResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")
@Validated
class QuestionController(
    private val questionService: IQuestionService,
    private val surveyService: ISurveyService,
    private val questionMapper: QuestionMapper
) {

    @PostMapping("/shared/open")
    fun createSharedOpenQuestion(@Valid @RequestBody request: OpenQuestionRequest): ResponseEntity<OpenQuestionResponse> {
        val question = questionService.createSharedOpenQuestion(request)
        val questionResponse = questionMapper.toResponse(question) as OpenQuestionResponse

        return ResponseEntity.ok(questionResponse)
    }

    @PostMapping("/shared/closed")
    fun createSharedClosedQuestion(@Valid @RequestBody request: ClosedQuestionRequest): ResponseEntity<ClosedQuestionResponse> {
        val question = questionService.createSharedClosedQuestion(request)
        val questionResponse = questionMapper.toResponse(question) as ClosedQuestionResponse

        return ResponseEntity.ok(questionResponse)
    }

    @PutMapping("/{id}")
    fun updateQuestion(@PathVariable id: Long, @Valid @RequestBody request: QuestionRequest): ResponseEntity<QuestionResponse> {
        val question = questionService.updateQuestion(id, request)
        val questionResponse = questionMapper.toResponse(question)
        return ResponseEntity.ok(questionResponse)
    }

    @PostMapping("/{id}/duplicate")
    fun duplicateQuestion(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.duplicateQuestion(id)
        val questionResponse = questionMapper.toResponse(question)
        return ResponseEntity.ok(questionResponse)
    }

    @DeleteMapping("/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        questionService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/counts")
    fun getQuestionCounts(): ResponseEntity<QuestionCountsResponse> {
        val counts = questionService.getQuestionCounts()
        return ResponseEntity.ok(counts)
    }

    @GetMapping("/shared")
    fun getSharedQuestions(): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.findSharedQuestions()
        val questionsResponse = questions.map { questionMapper.toResponse(it) }

        return ResponseEntity.ok(questionsResponse)
    }


    @GetMapping("/shared/page")
    fun getSharedQuestionsPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ): ResponseEntity<PaginatedResponse<QuestionResponse>> {
        val pageResponse = questionService.findSharedQuestionsPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/shared/open/page")
    fun getOpenSharedQuestionsPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ): ResponseEntity<PaginatedResponse<QuestionResponse>> {
        val pageResponse = questionService.findOpenSharedQuestionsPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/shared/closed/page")
    fun getClosedSharedQuestionsPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ): ResponseEntity<PaginatedResponse<QuestionResponse>> {
        val pageResponse = questionService.findClosedSharedQuestionsPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/shared/checkbox/page")
    fun getCheckboxSharedQuestionsPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ): ResponseEntity<PaginatedResponse<QuestionResponse>> {
        val pageResponse = questionService.findSharedCheckboxQuestionsPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }

    @GetMapping("/shared/dropdown/page")
    fun getDropdownSharedQuestionsPage(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "false") sortFromOldest: Boolean
    ): ResponseEntity<PaginatedResponse<QuestionResponse>> {
        val pageResponse = questionService.findSharedDropdownQuestionsPage(pageNumber, pageSize, sortFromOldest)
        return ResponseEntity.ok(pageResponse)
    }


    @PostMapping("/survey/{surveyId}/shared/{questionId}")
    fun addSharedQuestionToSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<QuestionResponse> {
        val question = surveyService.addSharedQuestion(surveyId, questionId)
        val questionResponse = questionMapper.toResponse(question)

        return ResponseEntity.ok(questionResponse);
    }

    @DeleteMapping("/survey/{surveyId}/{questionId}")
    fun removeQuestionFromSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<Void> {
        surveyService.removeQuestion(surveyId, questionId)

        return ResponseEntity.noContent().build()
    }

}