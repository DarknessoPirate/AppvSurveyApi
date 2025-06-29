package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import org.springframework.data.domain.Page

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

// Base Question Repository (for polymorphic queries)
@Repository
interface QuestionRepository : JpaRepository<Question, Long> {

    // Find shared questions (available for copying)
    @Query("SELECT q FROM Question q WHERE q.isShared = true AND q.survey IS NULL")
    fun findShared(): List<Question>

    @Query("SELECT q FROM Question q WHERE q.isShared = true AND q.survey IS NULL")
    fun findSharedPage(pageable:Pageable) : Page<Question>

}