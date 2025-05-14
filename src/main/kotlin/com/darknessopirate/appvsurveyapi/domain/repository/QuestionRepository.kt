package com.darknessopirate.appvsurveyapi.domain.repository

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.model.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    // Find questions that have been used in other surveys
    @Query("SELECT DISTINCT q FROM Question q WHERE q.id NOT IN (SELECT q2.id FROM Question q2 WHERE q2.survey.id = :surveyId)")
    fun findQuestionsNotInSurvey(surveyId: Long): List<Question>

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.possibleAnswers WHERE q.id = :id")
    fun findByIdWithAnswers(id: Long): Optional<Question>

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.possibleAnswers WHERE q.id NOT IN (SELECT q2.id FROM Question q2 WHERE q2.survey.id = :surveyId)")
    fun findQuestionsNotInSurveyWithAnswers(surveyId: Long): List<Question>

    // Find questions by type that could be reused
    fun findByQuestionType(questionType: QuestionType): List<Question>

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.possibleAnswers WHERE q.questionType = :questionType")
    fun findByQuestionTypeWithAnswers(questionType: QuestionType): List<Question>

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.possibleAnswers")
    fun findAllWithPossibleAnswers(): List<Question>
}