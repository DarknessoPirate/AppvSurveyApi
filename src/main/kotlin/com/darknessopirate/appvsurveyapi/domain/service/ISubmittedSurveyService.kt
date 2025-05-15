package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.domain.model.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.model.UserAnswer
import java.time.LocalDateTime

interface ISubmittedSurveyService {
    fun createSubmission(surveyId: Long, userAnswers: List<UserAnswer>): SubmittedSurvey
    fun getSubmissionById(id: Long): SubmittedSurvey
    fun getSubmissionsBySurveyId(surveyId: Long): List<SubmittedSurvey>
    fun getSubmissionsByTimeRange(startTime: LocalDateTime, endTime: LocalDateTime): List<SubmittedSurvey>
    fun getUserAnswersByQuestionId(questionId: Long): List<UserAnswer>
}