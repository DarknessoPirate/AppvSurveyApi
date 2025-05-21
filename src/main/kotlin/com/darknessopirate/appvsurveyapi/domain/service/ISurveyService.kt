package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import java.time.LocalDateTime

interface ISurveyService {

    /**
     * Create a new survey
     */
    fun createSurvey(
        request: CreateSurveyRequest
    ): Survey

    /**
     * Create survey with questions
     */
    fun createSurveyWithQuestions(
        request: CreateSurveyWithQuestionsRequest
    ): Survey

    /**
     * Find survey by access code
     */
    fun findByAccessCode(accessCode: String): Survey?

    /**
     * Find survey with all questions and answers
     */
    fun findWithQuestions(surveyId: Long): Survey?

    /**
     * Add question to survey by copying from shared questions
     */
    fun addSharedQuestion(surveyId: Long, sharedQuestionId: Long): Question

    /**
     * Add new open question directly to survey
     */
    fun addOpenQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false
    ): OpenQuestion

    /**
     * Add new closed question directly to survey
     */
    fun addClosedQuestion(
        surveyId: Long,
        text: String,
        required: Boolean = false,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion

    /**
     * Remove question from survey
     */
    fun removeQuestion(surveyId: Long, questionId: Long)

    /**
     * Reorder questions in survey
     */
    fun reorderQuestions(surveyId: Long, questionIds: List<Long>? = null)

    /**
     * Activate/deactivate survey
     */
    fun setActive(surveyId: Long, isActive: Boolean): Survey

    /**
     * Set survey expiration
     */
    fun setExpiration(surveyId: Long, expiresAt: LocalDateTime?): Survey

    /**
     * Generate unique access code
     */
    fun generateAccessCode(surveyId: Long): Survey

    /**
     * Copy survey (with all questions)
     */
    fun copySurvey(surveyId: Long, newTitle: String, includeAccessCode: Boolean = false): Survey

    /**
     * Get survey statistics
     */
    fun getStatistics(surveyId: Long): SurveyStatistics

    /**
     * Find active surveys
     */
    fun findActiveSurveys(): List<Survey>

    /**
     * Find expiring surveys
     */
    fun findExpiringSoon(days: Int = 7): List<Survey>

    /**
     * Check if survey accepts submissions
     */
    fun acceptsSubmissions(surveyId: Long): Boolean

    /**
     * Delete survey and all related data
     */
    fun deleteSurvey(surveyId: Long)
}