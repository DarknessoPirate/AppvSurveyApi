package com.darknessopirate.appvsurveyapi.domain.repository

import com.darknessopirate.appvsurveyapi.domain.model.Survey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SurveyRepository : JpaRepository<Survey, Long> {
    @Query("select s from Survey s left join fetch s.questions where s.accessCode = :accessCode")
    fun findByAccessCodeWithQuestions(accessCode: String): Survey?

    fun findSurveyById(id: Long): Survey?
    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    fun findByIdWithQuestions(@Param("id") id: Long): Survey?
}