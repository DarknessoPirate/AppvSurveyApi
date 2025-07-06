package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface SurveyRepository : JpaRepository<Survey, Long> {

    @Query("""
        SELECT DISTINCT s FROM Survey s 
        LEFT JOIN FETCH s.questions 
        WHERE s.id IN (
            SELECT ac.survey.id FROM AccessCode ac 
            WHERE ac.code = :accessCode
        )
    """)
    fun findByAccessCodeWithQuestions(@Param("accessCode") accessCode: String): Survey?

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions ORDER BY s.createdAt DESC")
    fun findAllWithQuestions(): List<Survey>

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    fun findByIdWithQuestions(@Param("id") id: Long): Survey?

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.isActive = true ORDER BY s.createdAt DESC")
    fun findActiveWithQuestions(): List<Survey>

    // separate method for fetching access codes because spring boot doesn't want to fetch multiple
    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.accessCodes WHERE s.id IN :surveyIds")
    fun findSurveysWithAccessCodes(@Param("surveyIds") surveyIds: List<Long>): List<Survey>

    // Count questions by type for a survey
    @Query("SELECT TYPE(q), COUNT(q) FROM Survey s JOIN s.questions q WHERE s.id = :surveyId GROUP BY TYPE(q)")
    fun countQuestionsByType(@Param("surveyId") surveyId: Long): List<Array<Any>>


    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions")
    fun findSurveysPage(pageable: Pageable): Page<Survey>

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.isActive = true")
    fun findActiveSurveysPage(pageable: Pageable): Page<Survey>

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.isActive = false")
    fun findInactiveSurveysPage(pageable: Pageable): Page<Survey>

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.expiresAt IS NOT NULL AND s.expiresAt < CURRENT_DATE ")
    fun findExpiredSurveysPage(pageable: Pageable): Page<Survey>

    // Find surveys expiring soon
    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.expiresAt IS NOT NULL AND s.expiresAt BETWEEN :startDate AND :endDate")
    fun findExpiringBetweenPage(@Param("startDate") startDate: LocalDate,
                                @Param("endDate") endDate: LocalDate,
                                pageable: Pageable): Page<Survey>



}