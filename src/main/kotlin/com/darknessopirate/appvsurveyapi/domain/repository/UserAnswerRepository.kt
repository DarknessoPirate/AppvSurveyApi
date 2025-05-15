package com.darknessopirate.appvsurveyapi.domain.repository

import com.darknessopirate.appvsurveyapi.domain.model.UserAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAnswerRepository : JpaRepository<UserAnswer, Long> {
    fun findBySubmittedSurveyId(surveyId: Long): List<UserAnswer>
    fun findByQuestionId(questionId: Long): List<UserAnswer>
}