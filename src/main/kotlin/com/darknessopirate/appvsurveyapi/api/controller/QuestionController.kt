package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.ApiResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.ClosedQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.OpenQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import io.swagger.v3.oas.annotations.Operation
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

    @GetMapping("/shared")
    fun getSharedQuestions(): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.findSharedQuestions()
        val questionsResponse = questions.map { questionMapper.toResponse(it) }

        return ResponseEntity.ok(questionsResponse)
    }

    @GetMapping("/shared/open")
    fun getSharedOpenQuestions(): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.findSharedOpenQuestionsWithAnswers()
        val questionsResponse = questions.map { questionMapper.toResponse(it) as OpenQuestionResponse }

        return ResponseEntity.ok(questionsResponse)
    }

    @GetMapping("/shared/closed")
    fun getSharedClosedQuestions(): ResponseEntity<List<ClosedQuestionResponse>> {
        val questions = questionService.findSharedClosedQuestionsWithAnswers()
        val questionsResponse = questions.map { questionMapper.toResponse(it) as ClosedQuestionResponse }

        return ResponseEntity.ok(questionsResponse)
    }

    // TODO : FIX LAZY INIT
    /*
    @GetMapping("/shared/search")
    fun searchSharedQuestions(@RequestParam query: String): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.searchSharedQuestions(query)
        val questionsResponse = questions.map { questionMapper.toResponse(it) }

        return ResponseEntity.ok(questionsResponse)
    }

     */

    @PostMapping("/{id}/make-shared")
    fun makeQuestionShared(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.makeQuestionShared(id)
        val questionResponse = questionMapper.toResponse(question)

        return ResponseEntity.ok(questionResponse)
    }

    // Survey-specific question operations moved from SurveyController
    @PostMapping("/survey/{surveyId}")
    fun addQuestionToSurvey(@PathVariable surveyId: Long, @Valid @RequestBody request: QuestionRequest): ResponseEntity<QuestionResponse> {
        val question = questionService.addQuestionToSurvey(surveyId, request)
        val questionResponse = questionMapper.toResponse(question)

        return ResponseEntity.ok(questionResponse)
    }

    @PostMapping("/survey/{surveyId}/shared/{questionId}")
    fun addSharedQuestionToSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<Void> {
        val question = surveyService.addSharedQuestion(surveyId, questionId)

        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/survey/{surveyId}/{questionId}")
    fun removeQuestionFromSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<Void> {
        surveyService.removeQuestion(surveyId, questionId)

        return ResponseEntity.noContent().build()
    }

}