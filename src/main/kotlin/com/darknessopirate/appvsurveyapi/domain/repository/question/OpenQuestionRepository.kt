package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OpenQuestionRepository : JpaRepository<OpenQuestion, Long> {
    // Find by survey
    fun findBySurveyIdOrderByDisplayOrder(surveyId: Long): List<OpenQuestion>

    // Find shared questions
    @Query("SELECT q FROM OpenQuestion q WHERE q.isShared = true AND q.survey IS NULL")
    fun findShared(): List<OpenQuestion>

    // Search shared questions
    @Query("SELECT q FROM OpenQuestion q WHERE q.isShared = true AND q.survey IS NULL AND LOWER(q.text) LIKE LOWER(CONCAT('%', :text, '%'))")
    fun searchShared(@Param("text") text: String): List<OpenQuestion>
}