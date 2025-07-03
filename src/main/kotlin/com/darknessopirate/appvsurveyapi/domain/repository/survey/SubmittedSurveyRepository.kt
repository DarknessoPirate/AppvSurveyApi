package com.darknessopirate.appvsurveyapi.domain.repository.survey

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SubmittedSurveyRepository : JpaRepository<SubmittedSurvey, Long> {
    fun findBySurveyId(surveyId: Long): List<SubmittedSurvey>
    @Query("SELECT s FROM SubmittedSurvey s JOIN FETCH s.survey LEFT JOIN FETCH s.userAnswers ua LEFT JOIN FETCH ua.question WHERE s.survey.id = :surveyId")
    fun findBySurveyIdWithSurvey(@Param("surveyId") surveyId: Long): List<SubmittedSurvey>

    @Query("SELECT s FROM SubmittedSurvey s JOIN FETCH s.survey LEFT JOIN FETCH s.userAnswers ua LEFT JOIN FETCH ua.question WHERE s.id = :id")
    fun findByIdWithSurveyAndAnswers(@Param("id") id: Long): SubmittedSurvey?

    @Query("""
    SELECT s FROM SubmittedSurvey s 
    JOIN FETCH s.survey 
    LEFT JOIN FETCH s.accessCode 
    WHERE s.accessCode.id = :accessCodeId
    """)
    fun findByAccessCodeId(accessCodeId: Long): List<SubmittedSurvey>

    @Query("""
    SELECT s FROM SubmittedSurvey s 
    JOIN FETCH s.survey 
    LEFT JOIN FETCH s.accessCode 
    WHERE s.accessCode.code = :accessCode
    """)
    fun findByAccessCode(accessCode: String): List<SubmittedSurvey>

    @Query("""
    SELECT s FROM SubmittedSurvey s 
    JOIN FETCH s.survey 
    LEFT JOIN FETCH s.accessCode 
    LEFT JOIN FETCH s.userAnswers 
    WHERE s.accessCode.code = :accessCode
    """)
    fun findByAccessCodeWithAnswers(accessCode: String): List<SubmittedSurvey>

    @Query("""
    SELECT DISTINCT s FROM SubmittedSurvey s 
    LEFT JOIN FETCH s.userAnswers ua 
    LEFT JOIN FETCH ua.question 
    WHERE s.survey.id = :surveyId
    """)
    fun findBySurveyIdWithAnswers(@Param("surveyId") surveyId: Long): List<SubmittedSurvey>

}