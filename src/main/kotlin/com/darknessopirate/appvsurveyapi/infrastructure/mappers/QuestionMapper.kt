package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.question.ClosedQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.OpenQuestionResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionAnswerResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import org.springframework.stereotype.Component

@Component
class   QuestionMapper {
    fun toEntity(request: List<QuestionRequest>): List<Question> {
        return request.map { toEntity(it) }
    }

    fun toEntity(request: QuestionRequest): Question = when (request) {
        is OpenQuestionRequest -> OpenQuestion(
            text = request.text,
            required = request.required,
            isShared = false
        )
        is ClosedQuestionRequest -> ClosedQuestion(
            text = request.text,
            required = request.required,
            isShared = false,
            selectionType = request.selectionType
        ).apply {
            request.possibleAnswers.forEachIndexed { index, answerText ->
                addPossibleAnswer(QuestionAnswer(text = answerText, displayOrder = index + 1))
            }
        }
    }

    fun toResponse(entity: Question): QuestionResponse = when (entity) {
        is OpenQuestion -> OpenQuestionResponse(
            questionId = entity.id ?: throw IllegalStateException("Question ID is null - entity may not be persisted yet"),
            questionText = entity.text,
            isRequired = entity.required,
            order = entity.displayOrder,
            shared = entity.isShared
        )
        is ClosedQuestion -> ClosedQuestionResponse(
            questionId = entity.id ?: throw IllegalStateException("Question ID is null - entity may not be persisted yet"),
            questionText = entity.text,
            isRequired = entity.required,
            order = entity.displayOrder,
            shared = entity.isShared,
            selectionType = entity.selectionType,
            possibleAnswers = entity.possibleAnswers.map { toResponse(it) }
        )
        else -> throw IllegalArgumentException("Unknown question type")
    }

    fun toResponse(entity: QuestionAnswer): QuestionAnswerResponse = QuestionAnswerResponse(
        id = entity.id ?: throw IllegalStateException("QuestionAnswer ID is null - entity may not be persisted yet"),
        text = entity.text,
        displayOrder = entity.displayOrder
    )
}