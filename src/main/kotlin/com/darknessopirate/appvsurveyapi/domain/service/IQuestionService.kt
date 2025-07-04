package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

interface IQuestionService {

    fun createSharedOpenQuestion(request: OpenQuestionRequest): OpenQuestion
    fun createSharedClosedQuestion(request: ClosedQuestionRequest): ClosedQuestion
    fun updateQuestion(id: Long, request: QuestionRequest): Question
    fun duplicateQuestion(id: Long): Question
    fun deleteQuestion(id: Long)
    fun copyQuestionToSurvey(questionId: Long, surveyId: Long, displayOrder: Int): Question
    fun findSharedQuestions(): List<Question>
    fun findSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse>

    // TODO: remove?
    fun createSurveySpecificOpenQuestion(
        surveyId: Long,
        text: String,
        description: String?,
        required: Boolean = false,
        displayOrder: Int
    ): OpenQuestion

    // todo: remove?
    fun createSurveySpecificClosedQuestion(
        surveyId: Long,
        text: String,
        description: String?,
        required: Boolean = false,
        displayOrder: Int,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion


    fun findClosedSharedQuestionsPage(
        pageNumber: Int,
        pageSize: Int,
        sortFromOldest: Boolean
    ): PaginatedResponse<QuestionResponse>

    fun findOpenSharedQuestionsPage(
        pageNumber: Int,
        pageSize: Int,
        sortFromOldest: Boolean
    ): PaginatedResponse<QuestionResponse>
}