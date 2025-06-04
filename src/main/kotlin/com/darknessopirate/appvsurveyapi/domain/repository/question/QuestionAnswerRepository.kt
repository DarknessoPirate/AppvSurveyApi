package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface QuestionAnswerRepository : JpaRepository<QuestionAnswer, Long> {

}