package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ClosedQuestionRepository : JpaRepository<ClosedQuestion, Long> {
    // Find shared questions with answers
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.isShared = true AND q.survey IS NULL")
    fun findSharedWithAnswers(): List<ClosedQuestion>

    @Query("""SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE 
        q.isShared = true AND q.survey IS NULL""")
    fun findSharedPage(pageable: Pageable): Page<ClosedQuestion>

    @Query("""SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers 
        WHERE q.isShared = true AND q.survey IS NULL AND q.selectionType = :selectionType""")
    fun findSharedPageByType(selectionType: SelectionType, pageable: Pageable): Page<ClosedQuestion>


}