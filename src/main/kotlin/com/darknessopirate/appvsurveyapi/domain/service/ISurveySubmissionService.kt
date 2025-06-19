package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.submit.AnswerRequest
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import java.time.LocalDateTime

interface ISurveySubmissionService {
    fun submitByAccessCode(accessCode: String, answers: List<AnswerRequest>): SubmittedSurvey
    fun getSubmission(submissionId: Long): SubmittedSurvey?
    fun getSubmissionsByAccessCode(accessCode: String): List<SubmittedSurvey>
}