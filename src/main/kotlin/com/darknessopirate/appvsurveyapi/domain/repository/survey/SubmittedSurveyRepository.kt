package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

@Repository
interface SubmittedSurveyRepository : JpaRepository<SubmittedSurvey, Long> {
    fun findBySurveyId(surveyId: Long): List<SubmittedSurvey>
    @Query("SELECT s FROM SubmittedSurvey s JOIN FETCH s.survey LEFT JOIN FETCH s.userAnswers ua LEFT JOIN FETCH ua.question WHERE s.survey.id = :surveyId")
    fun findBySurveyIdWithSurvey(@Param("surveyId") surveyId: Long): List<SubmittedSurvey>

    @Query("SELECT s FROM SubmittedSurvey s JOIN FETCH s.survey WHERE s.id = :id")
    fun findByIdWithSurvey(@Param("id") id: Long): SubmittedSurvey?

    @Query("SELECT s FROM SubmittedSurvey s JOIN FETCH s.survey LEFT JOIN FETCH s.userAnswers ua LEFT JOIN FETCH ua.question WHERE s.id = :id")
    fun findByIdWithSurveyAndAnswers(@Param("id") id: Long): SubmittedSurvey?
}