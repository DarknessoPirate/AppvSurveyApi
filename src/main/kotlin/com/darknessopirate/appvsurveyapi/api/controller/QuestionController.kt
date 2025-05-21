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
    fun createSharedOpenQuestion(@Valid @RequestBody request: OpenQuestionRequest): ResponseEntity<ApiResponse<OpenQuestionResponse>> {
        return try {
            val question = questionService.createSharedOpenQuestion(request.text, request.required)
            ResponseEntity.ok(ApiResponse.success(questionMapper.toResponse(question) as OpenQuestionResponse))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to create shared question"))
        }
    }

    @PostMapping("/shared/closed")
    fun createSharedClosedQuestion(@Valid @RequestBody request: ClosedQuestionRequest): ResponseEntity<ApiResponse<ClosedQuestionResponse>> {
        return try {
            val question = questionService.createSharedClosedQuestion(
                text = request.text,
                required = request.required,
                selectionType = request.selectionType,
                possibleAnswers = request.possibleAnswers
            )
            ResponseEntity.ok(ApiResponse.success(questionMapper.toResponse(question) as ClosedQuestionResponse))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to create shared question"))
        }
    }

    @GetMapping("/shared")
    fun getSharedQuestions(): ResponseEntity<ApiResponse<List<QuestionResponse>>> {
        val questions = questionService.findSharedQuestions().map { questionMapper.toResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(questions))
    }

    @GetMapping("/shared/search")
    fun searchSharedQuestions(@RequestParam query: String): ResponseEntity<ApiResponse<List<QuestionResponse>>> {
        val questions = questionService.searchSharedQuestions(query).map { questionMapper.toResponse(it) }
        return ResponseEntity.ok(ApiResponse.success(questions))
    }

    @GetMapping("/shared/closed")
    fun getSharedClosedQuestions(): ResponseEntity<ApiResponse<List<ClosedQuestionResponse>>> {
        val questions = questionService.findSharedClosedQuestionsWithAnswers()
            .map { questionMapper.toResponse(it) as ClosedQuestionResponse }
        return ResponseEntity.ok(ApiResponse.success(questions))
    }

    @PostMapping("/{id}/make-shared")
    fun makeQuestionShared(@PathVariable id: Long): ResponseEntity<ApiResponse<QuestionResponse>> {
        return try {
            val question = questionService.makeQuestionShared(id)
            ResponseEntity.ok(ApiResponse.success(questionMapper.toResponse(question)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to make question shared"))
        }
    }

    // Survey-specific question operations moved from SurveyController
    @PostMapping("/survey/{surveyId}")
    fun addQuestionToSurvey(@PathVariable surveyId: Long, @Valid @RequestBody request: QuestionRequest): ResponseEntity<ApiResponse<QuestionResponse>> {
        return try {
            val question = when (request) {
                is OpenQuestionRequest ->
                    surveyService.addOpenQuestion(surveyId, request.text, request.required)

                is ClosedQuestionRequest ->
                    surveyService.addClosedQuestion(
                        surveyId,
                        request.text,
                        request.required,
                        request.selectionType,
                        request.possibleAnswers
                    )
            }
            ResponseEntity.ok(ApiResponse.success(questionMapper.toResponse(question)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to add question"))
        }
    }

    @PostMapping("/survey/{surveyId}/shared/{questionId}")
    fun addSharedQuestionToSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<ApiResponse<QuestionResponse>> {
        return try {
            val question = surveyService.addSharedQuestion(surveyId, questionId)
            ResponseEntity.ok(ApiResponse.success(questionMapper.toResponse(question)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to add shared question"))
        }
    }

    @DeleteMapping("/survey/{surveyId}/{questionId}")
    fun removeQuestionFromSurvey(@PathVariable surveyId: Long, @PathVariable questionId: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            surveyService.removeQuestion(surveyId, questionId)
            ResponseEntity.ok(ApiResponse.success("Question removed successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Failed to remove question"))
        }
    }

}