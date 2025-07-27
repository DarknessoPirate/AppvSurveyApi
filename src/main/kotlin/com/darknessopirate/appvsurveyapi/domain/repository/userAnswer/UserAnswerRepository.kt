package com.darknessopirate.appvsurveyapi.domain.repository.userAnswer

import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAnswerRepository : JpaRepository<UserAnswer, Long> {

    fun findBySubmittedSurveyId(submittedSurveyId: Long): List<UserAnswer>


    fun findByQuestionId(questionId: Long): List<UserAnswer>


    fun findBySubmittedSurveyIdAndQuestionId(submittedSurveyId: Long, questionId: Long): List<UserAnswer>
}