package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClosedQuestionRepository : JpaRepository<ClosedQuestion, Long> {
    // Find by survey with answers
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.survey.id = :surveyId ORDER BY q.displayOrder")
    fun findBySurveyWithAnswers(@Param("surveyId") surveyId: Long): List<ClosedQuestion>

    // Find shared questions with answers
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.isShared = true AND q.survey IS NULL")
    fun findSharedWithAnswers(): List<ClosedQuestion>

    // Find by selection type
    fun findBySelectionType(selectionType: SelectionType): List<ClosedQuestion>

    // Find with answers
    @Query("SELECT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.id = :id")
    fun findByIdWithAnswers(@Param("id") id: Long): ClosedQuestion?

    // Find shared by selection type
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.isShared = true AND q.survey IS NULL AND q.selectionType = :selectionType")
    fun findSharedByType(@Param("selectionType") selectionType: SelectionType): List<ClosedQuestion>

    // Search shared questions
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.isShared = true AND q.survey IS NULL AND LOWER(q.text) LIKE LOWER(CONCAT('%', :text, '%'))")
    fun searchSharedWithAnswers(@Param("text") text: String): List<ClosedQuestion>
}