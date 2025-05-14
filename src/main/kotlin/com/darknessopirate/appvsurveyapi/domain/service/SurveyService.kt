package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.domain.model.Question
import com.darknessopirate.appvsurveyapi.domain.model.Survey

interface SurveyService {
    fun createSurvey(survey: Survey): Survey
    fun getSurveyById(id: Long): Survey
    fun getSurveyByAccessCode(accessCode: String): Survey
    fun updateSurvey(id: Long, survey: Survey): Survey
    fun deleteSurvey(id: Long)
    fun addQuestionToSurvey(surveyId: Long, question: Question): Survey
    fun addExistingQuestionToSurvey(surveyId: Long, questionId: Long): Survey
    fun removeQuestionFromSurvey(surveyId: Long, questionId: Long): Survey
    fun getAllSurveys(): List<Survey>
    fun generateSurveyLink(surveyId: Long): String
}