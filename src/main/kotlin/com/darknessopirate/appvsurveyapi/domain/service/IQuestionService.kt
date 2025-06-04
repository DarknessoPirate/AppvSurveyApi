package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
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
    fun findById(id: Long): Question
    // todo: move to survey ?
    fun addQuestionToSurvey(surveyId: Long, request: QuestionRequest): Question
    fun copyQuestionToSurvey(questionId: Long, surveyId: Long, displayOrder: Int): Question
    fun findSharedQuestions(): List<Question>

    fun findSharedClosedQuestionsWithAnswers(): List<ClosedQuestion>
    fun findSharedOpenQuestionsWithAnswers(): List<OpenQuestion>

    // TODO: remove?
    fun createSurveySpecificOpenQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false,
        displayOrder: Int
    ): OpenQuestion

    // todo: remove?
    fun createSurveySpecificClosedQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false,
        displayOrder: Int,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion


    fun makeQuestionShared(questionId: Long): Question
}