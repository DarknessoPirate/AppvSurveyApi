package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")

class QuestionController(
    private val questionService: IQuestionService,
    private val questionMapper: QuestionMapper
) {
    @Operation(
        summary = "Create a new question",
        description = "The question can be of types: OPEN,DROPDOWN,CHECKBOX. It will later be linked by relation to created surveys." +
                      "The open question should not have any answers in the list, later when using get they will be ignored in client if any are present.")
    @PostMapping
    fun createQuestion(@Valid @RequestBody request: QuestionRequest): ResponseEntity<QuestionResponse> {
        val question = questionMapper.toEntity(request)
        val createdQuestion = questionService.createQuestion(question)
        return ResponseEntity.ok(questionMapper.toResponse(createdQuestion))
    }

    @PutMapping("/{id}")
    fun updateQuestion(
        @PathVariable id: Long,
        @RequestBody request: QuestionRequest
    ): ResponseEntity<QuestionResponse> {
        val question = questionMapper.toEntity(request)
        val updatedQuestion = questionService.updateQuestion(id, question)
        return ResponseEntity.ok(questionMapper.toResponse(updatedQuestion))
    }
    @DeleteMapping("/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Void> {
        questionService.deleteQuestion(id)
        return ResponseEntity.noContent().build()
    }
    @GetMapping("/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.getQuestionById(id)
        return ResponseEntity.ok(questionMapper.toResponse(question))
    }

    @GetMapping("type/{type}")
    fun getQuestionsByType(@PathVariable type: QuestionType): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.getQuestionsByType(type)
        return ResponseEntity.ok(questionMapper.toResponseList(questions))
    }
    @GetMapping
    fun getQuestions(): ResponseEntity<List<QuestionResponse>>
    {
        val questions = questionService.getAllQuestions()
        val mappedQuestions = questionMapper.toResponseList(questions)
        return ResponseEntity.ok(mappedQuestions)
    }
}