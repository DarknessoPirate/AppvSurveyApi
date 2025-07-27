package com.darknessopirate.appvsurveyapi.domain.repository.userAnswer

import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClosedUserAnswerRepository : JpaRepository<ClosedUserAnswer, Long> {

    @Query("SELECT DISTINCT a FROM ClosedUserAnswer a LEFT JOIN FETCH a.selectedAnswers WHERE a.submittedSurvey.id = :submittedSurveyId")
    fun findBySurveyWithSelections(@Param("submittedSurveyId") submittedSurveyId: Long): List<ClosedUserAnswer>


    @Query("SELECT DISTINCT a FROM ClosedUserAnswer a LEFT JOIN FETCH a.selectedAnswers WHERE a.question.id = :questionId")
    fun findByQuestionWithSelections(@Param("questionId") questionId: Long): List<ClosedUserAnswer>


    @Query("SELECT a FROM ClosedUserAnswer a JOIN a.selectedAnswers sa WHERE sa.id = :answerId")
    fun findBySelectedAnswer(@Param("answerId") answerId: Long): List<ClosedUserAnswer>


    @Query("""
        SELECT qa.id, qa.text, COUNT(cua.id) 
        FROM QuestionAnswer qa 
        LEFT JOIN ClosedUserAnswer cua ON qa MEMBER OF cua.selectedAnswers 
        WHERE qa.question.id = :questionId 
        GROUP BY qa.id, qa.text 
        ORDER BY qa.displayOrder
    """)
    fun getStatistics(@Param("questionId") questionId: Long): List<Array<Any>>


    @Query("SELECT COUNT(DISTINCT a) FROM ClosedUserAnswer a WHERE a.question.id = :questionId")
    fun countResponses(@Param("questionId") questionId: Long): Long


    @Query("SELECT a FROM ClosedUserAnswer a WHERE a.question.id = :questionId AND SIZE(a.selectedAnswers) > 1")
    fun findMultipleSelections(@Param("questionId") questionId: Long): List<ClosedUserAnswer>
}