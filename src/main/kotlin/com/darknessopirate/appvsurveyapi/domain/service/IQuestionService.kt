package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType

interface IQuestionService {
    /**
     * Create a shared question (manually created, available for copying)
     */
    fun createSharedOpenQuestion(request: OpenQuestionRequest): OpenQuestion

    /**
     * Create a shared closed question with possible answers
     */
    fun createSharedClosedQuestion(request: ClosedQuestionRequest): ClosedQuestion
    /**
     * Copy a shared question to a survey
     */

    /**
     * Add question to survey (handles both open and closed questions)
     */
    fun addQuestionToSurvey(surveyId: Long, request: QuestionRequest): Question

    fun copyQuestionToSurvey(questionId: Long, surveyId: Long, displayOrder: Int): Question

    /**
     * Find all shared questions available for copying
     */
    fun findSharedQuestions(): List<Question>

    /**
     * Find shared questions by search text
     */
    fun searchSharedQuestions(searchText: String): List<Question>

    /**
     * Find shared closed questions with their answers
     */
    fun findSharedClosedQuestionsWithAnswers(): List<ClosedQuestion>

    fun findSharedOpenQuestionsWithAnswers(): List<OpenQuestion>
    /**
     * Create survey-specific question directly (not shared)
     */
    fun createSurveySpecificOpenQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false,
        displayOrder: Int
    ): OpenQuestion

    /**
     * Create survey-specific closed question directly (not shared)
     */
    fun createSurveySpecificClosedQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false,
        displayOrder: Int,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion

    /**
     * Convert an existing survey question to a shared question
     */
    fun makeQuestionShared(questionId: Long): Question
}