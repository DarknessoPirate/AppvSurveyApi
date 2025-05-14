package com.darknessopirate.appvsurveyapi.domain.repository

import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionAnswerRepository : JpaRepository<QuestionAnswer, Long> {
    fun findByQuestionId(questionId: Long): List<QuestionAnswer>
}