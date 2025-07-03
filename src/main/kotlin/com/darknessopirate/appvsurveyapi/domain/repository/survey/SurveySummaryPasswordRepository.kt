package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SurveySummaryPassword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface SurveySummaryPasswordRepository : JpaRepository<SurveySummaryPassword, Long> {

    @Query("SELECT s FROM SurveySummaryPassword s WHERE s.survey.id = :surveyId")
    fun findBySurveyId(@Param("surveyId") surveyId: Long): Optional<SurveySummaryPassword>

    fun existsBySurveyId(surveyId: Long): Boolean
}