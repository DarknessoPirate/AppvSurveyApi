package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AccessCodeRepository : JpaRepository<AccessCode, Long> {

    fun findByCode(code: String): AccessCode?

    fun findBySurveyId(surveyId: Long): List<AccessCode>

    @Query("SELECT ac FROM AccessCode ac WHERE ac.survey.id = :surveyId AND ac.isActive = true")
    fun findActiveBySurveyId(@Param("surveyId") surveyId: Long): List<AccessCode>

    @Query("SELECT ac FROM AccessCode ac WHERE ac.code = :code AND ac.isActive = true AND (ac.expiresAt IS NULL OR ac.expiresAt > :now) AND (ac.maxUses IS NULL OR ac.usageCount < ac.maxUses)")
    fun findValidAccessCode(@Param("code") code: String, @Param("now") now: LocalDateTime): AccessCode?

    fun existsByCode(code: String): Boolean
}