package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClosedQuestionRepository : JpaRepository<ClosedQuestion, Long> {
    // Find shared questions with answers
    @Query("SELECT DISTINCT q FROM ClosedQuestion q LEFT JOIN FETCH q.possibleAnswers WHERE q.isShared = true AND q.survey IS NULL")
    fun findSharedWithAnswers(): List<ClosedQuestion>


}