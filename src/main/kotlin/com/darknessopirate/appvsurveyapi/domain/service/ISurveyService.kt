package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.model.QuestionStatistic

interface ISurveyService {

    // Create survey with question ids
    fun createSurveyWithSelectedQuestions(request: CreateSurveyWithQuestionsRequest): Survey
    fun findByAccessCode(accessCode: String): Survey
    // Find survey with all questions and answers
    fun findWithQuestions(surveyId: Long): Survey
    // Add question to survey by copying from shared questions
    fun addSharedQuestion(surveyId: Long, sharedQuestionId: Long): Question
    // Remove question from survey
    fun removeQuestion(surveyId: Long, questionId: Long)
    // Reorder questions in survey
    fun reorderQuestions(surveyId: Long, questionIds: List<Long>? = null)
    fun toggleActive(surveyId: Long)
    // Copy survey (with all questions)
    fun copySurvey(surveyId: Long, newTitle: String): Survey

    fun findAllSurveys(): List<Survey>
    // Delete survey and all related data
    fun deleteSurvey(surveyId: Long)
    fun getSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<SurveyResponse>
}