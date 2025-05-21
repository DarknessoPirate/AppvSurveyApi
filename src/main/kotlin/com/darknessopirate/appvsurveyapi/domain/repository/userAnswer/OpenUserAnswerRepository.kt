package com.darknessopirate.appvsurveyapi.domain.repository.userAnswer

import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OpenUserAnswerRepository : JpaRepository<OpenUserAnswer, Long> {
    // Find by submitted survey
    fun findBySubmittedSurveyId(submittedSurveyId: Long): List<OpenUserAnswer>

    // Find by question
    fun findByQuestionId(questionId: Long): List<OpenUserAnswer>

    // Search by text content
    @Query("SELECT a FROM OpenUserAnswer a WHERE LOWER(a.textValue) LIKE LOWER(CONCAT('%', :text, '%'))")
    fun searchByText(@Param("text") text: String): List<OpenUserAnswer>

    // Count non-empty answers
    @Query("SELECT COUNT(a) FROM OpenUserAnswer a WHERE a.question.id = :questionId AND a.textValue IS NOT NULL AND a.textValue != ''")
    fun countNonEmpty(@Param("questionId") questionId: Long): Long

    // Find by text length
    @Query("SELECT a FROM OpenUserAnswer a WHERE a.question.id = :questionId AND LENGTH(a.textValue) > :minLength")
    fun findByTextLength(@Param("questionId") questionId: Long, @Param("minLength") minLength: Int): List<OpenUserAnswer>

    // Get average text length
    @Query("SELECT AVG(LENGTH(a.textValue)) FROM OpenUserAnswer a WHERE a.question.id = :questionId AND a.textValue IS NOT NULL")
    fun getAvgTextLength(@Param("questionId") questionId: Long): Double?
}