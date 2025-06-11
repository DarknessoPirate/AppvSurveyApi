package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.question.ClosedQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.OpenQuestionResponse
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

    @GetMapping("/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.findById(id)
        val questionResponse = questionMapper.toResponse(question)
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

    @PostMapping("/{id}/make-shared")
    fun makeQuestionShared(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.makeQuestionShared(id)
        val questionResponse = questionMapper.toResponse(question)

        return ResponseEntity.ok(questionResponse)
    }

    @PostMapping("/survey/{surveyId}")
    fun addQuestionToSurvey(@PathVariable surveyId: Long, @Valid @RequestBody request: QuestionRequest): ResponseEntity<QuestionResponse> {
        val question = questionService.addQuestionToSurvey(surveyId, request)
        val questionResponse = questionMapper.toResponse(question)

        return ResponseEntity.ok(questionResponse)
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