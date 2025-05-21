package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SubmittedSurveyRepository : JpaRepository<SubmittedSurvey, Long> {
    fun findBySurveyId(surveyId: Long): List<SubmittedSurvey>
    fun findBySubmittedAtBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<SubmittedSurvey>
}