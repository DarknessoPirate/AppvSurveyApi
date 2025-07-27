package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OpenQuestionRepository : JpaRepository<OpenQuestion, Long> {

    @Query("SELECT q FROM OpenQuestion q WHERE q.isShared = true AND q.survey IS NULL")
    fun findShared(): List<OpenQuestion>

    @Query("SELECT q FROM OpenQuestion q WHERE q.isShared = true AND q.survey IS NULL")
    fun findSharedPage(pageable: Pageable): Page<OpenQuestion>

    @Query("SELECT COUNT(q) FROM OpenQuestion q WHERE q.isShared = true AND q.survey IS NULL")
    fun countAllOpenQuestions(): Long
}