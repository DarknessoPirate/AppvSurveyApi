package com.darknessopirate.appvsurveyapi.api.controller

import com.darknessopirate.appvsurveyapi.api.dto.request.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.SurveyCreateRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.QuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: ISurveyService,
    private val questionService: IQuestionService,
    private val surveyMapper: SurveyMapper,
    private val questionMapper: QuestionMapper
) {
    // TODO: CREATE QUESTIONS BY COPYING INSTEAD OF NEW QUESTIONS
    @PostMapping
    fun createSurvey(@RequestBody survey: SurveyCreateRequest): ResponseEntity<SurveyResponse> {
        val survey = surveyMapper.toEntity(survey)
        val createdSurvey = surveyService.createSurvey(survey)
        val response = surveyMapper.toResponse(createdSurvey)

        return ResponseEntity.ok(response)
    }

    @PutMapping("{surveyId}")
    fun updateSurvey(@RequestBody survey: SurveyCreateRequest, @PathVariable surveyId: Long): ResponseEntity<SurveyResponse> {
        val survey = surveyMapper.toEntity(survey)
        val updatedSurvey = surveyService.updateSurvey(surveyId, survey)
        val response = surveyMapper.toResponse(updatedSurvey)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("{surveyId}")
    fun deleteSurvey(@PathVariable surveyId: Long): ResponseEntity<Void> {
        surveyService.deleteSurvey(surveyId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{surveyId}")
    fun getSurvey(@PathVariable surveyId: Long): ResponseEntity<SurveyResponse> {
        val survey = surveyService.getSurveyById(surveyId)
        val response = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAllSurveys(): ResponseEntity<List<SurveyResponse>> {
        val surveys = surveyService.getAllSurveys()
        val response = surveyMapper.toResponseList(surveys)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/access/{code}")
    fun getSurveyByAccessCode(@PathVariable code: String): ResponseEntity<SurveyResponse> {
        val survey = surveyService.getSurveyByAccessCode(code)
        val response = surveyMapper.toResponse(survey)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{surveyId}/questions")
    fun addQuestionToSurvey(
        @PathVariable surveyId: Long,
        @RequestBody questionRequest: QuestionRequest
    ): ResponseEntity<SurveyResponse> {
        val question = questionMapper.toEntity(questionRequest)
        val updatedSurvey = surveyService.addQuestionToSurvey(surveyId, question)
        val response = surveyMapper.toResponse(updatedSurvey)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{surveyId}/questions/{questionId}")
    fun addExistingQuestionToSurvey(
        @PathVariable surveyId: Long,
        @PathVariable questionId: Long
    ): ResponseEntity<SurveyResponse> {
        val updatedSurvey = surveyService.addExistingQuestionToSurvey(surveyId, questionId)
        val response = surveyMapper.toResponse(updatedSurvey)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}")
    fun removeQuestionFromSurvey(
        @PathVariable surveyId: Long,
        @PathVariable questionId: Long
    ): ResponseEntity<SurveyResponse> {
        val updatedSurvey = surveyService.removeQuestionFromSurvey(surveyId, questionId)
        val response = surveyMapper.toResponse(updatedSurvey)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}/link")
    fun generateSurveyLink(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val link = surveyService.generateSurveyLink(id)

        return ResponseEntity.ok(mapOf("link" to link))
    }

    @GetMapping("/{surveyId}/questions/not-used")
    fun getReusableQuestions(@PathVariable surveyId: Long): ResponseEntity<List<QuestionResponse>> {
        val questions = questionService.getReusableQuestions(surveyId)
        val response = questionMapper.toResponseList(questions)

        return ResponseEntity.ok(response)
    }


}