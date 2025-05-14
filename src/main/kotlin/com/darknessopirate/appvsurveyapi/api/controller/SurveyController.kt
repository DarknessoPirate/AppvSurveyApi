package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.SurveyCreateRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.service.QuestionService
import com.darknessopirate.appvsurveyapi.domain.service.SurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: SurveyService,
    private val questionService: QuestionService,
    private val surveyMapper: SurveyMapper,
    private val questionMapper: QuestionMapper
) {
    @PostMapping
    fun createSurvey(@RequestBody request: SurveyCreateRequest): ResponseEntity<SurveyResponse> {
        val survey = surveyMapper.toEntity(request)
        val createdSurvey = surveyService.createSurvey(survey)
        return ResponseEntity.ok(surveyMapper.toResponse(createdSurvey))
    }

    @GetMapping("/{id}")
    fun getSurvey(@PathVariable id: Long): ResponseEntity<SurveyResponse> {
        val survey = surveyService.getSurveyById(id)
        return ResponseEntity.ok(surveyMapper.toResponse(survey))
    }

    @GetMapping
    fun getAllSurveys(): ResponseEntity<List<SurveyResponse>> {
        val surveys = surveyService.getAllSurveys()
        return ResponseEntity.ok(surveyMapper.toResponseList(surveys))
    }

    @GetMapping("/access/{code}")
    fun getSurveyByAccessCode(@PathVariable code: String): ResponseEntity<SurveyResponse> {
        val survey = surveyService.getSurveyByAccessCode(code)
        return ResponseEntity.ok(surveyMapper.toResponse(survey))
    }

    @PostMapping("/{id}/questions")
    fun addQuestionToSurvey(
        @PathVariable id: Long,
        @RequestBody questionRequest: QuestionRequest
    ): ResponseEntity<SurveyResponse> {
        val question = questionMapper.toEntity(questionRequest)
        val updatedSurvey = surveyService.addQuestionToSurvey(id, question)
        return ResponseEntity.ok(surveyMapper.toResponse(updatedSurvey))
    }

    @PostMapping("/{surveyId}/questions/{questionId}")
    fun addExistingQuestionToSurvey(
        @PathVariable surveyId: Long,
        @PathVariable questionId: Long
    ): ResponseEntity<SurveyResponse> {
        val updatedSurvey = surveyService.addExistingQuestionToSurvey(surveyId, questionId)
        return ResponseEntity.ok(surveyMapper.toResponse(updatedSurvey))
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}")
    fun removeQuestionFromSurvey(
        @PathVariable surveyId: Long,
        @PathVariable questionId: Long
    ): ResponseEntity<SurveyResponse> {
        val updatedSurvey = surveyService.removeQuestionFromSurvey(surveyId, questionId)
        return ResponseEntity.ok(surveyMapper.toResponse(updatedSurvey))
    }

    @GetMapping("/{id}/link")
    fun generateSurveyLink(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val link = surveyService.generateSurveyLink(id)
        return ResponseEntity.ok(mapOf("link" to link))
    }

    @GetMapping("/questions/reusable/{surveyId}")
    fun getReusableQuestions(@PathVariable surveyId: Long): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.getReusableQuestions(surveyId)
        return ResponseEntity.ok(questionMapper.toResponseList(questions))
    }

    @GetMapping("/questions/type/{type}")
    fun getQuestionsByType(@PathVariable type: QuestionType): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.getQuestionsByType(type)
        return ResponseEntity.ok(questionMapper.toResponseList(questions))
    }

    @GetMapping("/questions")
    fun getAllQuestions(): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.getAllQuestions()
        return ResponseEntity.ok(questionMapper.toResponseList(questions))
    }

}