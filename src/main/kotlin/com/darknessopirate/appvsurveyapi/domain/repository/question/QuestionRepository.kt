package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

// Base Question Repository (for polymorphic queries)
@Repository
interface QuestionRepository : JpaRepository<Question, Long> {

    // Find all questions for a survey
    fun findBySurveyIdOrderByDisplayOrder(surveyId: Long): List<Question>

    // Find shared questions (available for copying)
    @Query("SELECT q FROM Question q WHERE q.isShared = true AND q.survey IS NULL")
    fun findShared(): List<Question>

    // Search shared questions
    @Query("SELECT q FROM Question q WHERE q.isShared = true AND q.survey IS NULL AND LOWER(q.text) LIKE LOWER(CONCAT('%', :text, '%'))")
    fun searchShared(@Param("text") text: String): List<Question>

    // Find questions with specific IDs
    fun findByIdIn(ids: List<Long>): List<Question>
}