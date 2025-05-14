package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.service.QuestionService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/questions")
class QuestionController(
    private val questionService: QuestionService,
    private val questionMapper: QuestionMapper
) {
    @PostMapping
    fun createQuestion(@RequestBody request: QuestionRequest): ResponseEntity<QuestionResponse> {
        val question = questionMapper.toEntity(request)
        val createdQuestion = questionService.createQuestion(question)
        return ResponseEntity.ok(questionMapper.toResponse(createdQuestion))
    }

    @GetMapping("/{id}")
    fun getQuestion(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val question = questionService.getQuestionById(id)
        return ResponseEntity.ok(questionMapper.toResponse(question))
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
}