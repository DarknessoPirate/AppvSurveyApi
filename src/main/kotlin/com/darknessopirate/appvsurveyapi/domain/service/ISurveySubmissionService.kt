package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.AnswerRequest
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import com.darknessopirate.appvsurveyapi.domain.model.SubmissionSummary
import java.time.LocalDateTime

interface ISurveySubmissionService {

    /**
     * Submit responses to a survey
     */
    fun submitSurvey(
        surveyId: Long,
        answers: List<AnswerRequest>
    ): SubmittedSurvey

    /**
     * Submit by access code
     */
    fun submitByAccessCode(
        accessCode: String,
        answers: List<AnswerRequest>
    ): SubmittedSurvey

    /**
     * Get submission by ID
     */
    fun getSubmission(submissionId: Long): SubmittedSurvey?

    /**
     * Get all submissions for a survey
     */
    fun getSubmissions(surveyId: Long): List<SubmittedSurvey>

    /**
     * Get submissions in date range
     */
    fun getSubmissions(
        surveyId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<SubmittedSurvey>

    /**
     * Get user answers for a question
     */
    fun getAnswersForQuestion(questionId: Long): List<UserAnswer>

    /**
     * Get answer statistics for a closed question
     */
    fun getAnswerStatistics(questionId: Long): List<AnswerStatistic>


    fun searchTextResponses(searchText: String): List<OpenUserAnswer>

    /**
     * Get submission summary
     */
    fun getSubmissionSummary(surveyId: Long): SubmissionSummary
}