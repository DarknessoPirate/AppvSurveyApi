package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.model.Question

interface IQuestionService {
    fun createQuestion(question: Question): Question
    fun getQuestionById(id: Long): Question
    fun updateQuestion(id: Long, question: Question): Question
    fun deleteQuestion(id: Long)
    fun getReusableQuestions(surveyId: Long): List<Question>
    fun getQuestionsByType(questionType: QuestionType): List<Question>
    fun getAllQuestions(): List<Question>
}