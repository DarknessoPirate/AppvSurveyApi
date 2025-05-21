package com.darknessopirate.appvsurveyapi.domain.repository.question

import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface QuestionAnswerRepository : JpaRepository<QuestionAnswer, Long> {
    // Find by question
    fun findByQuestionIdOrderByDisplayOrder(questionId: Long): List<QuestionAnswer>

    // Find with usage count
    @Query("""
        SELECT qa, COUNT(cua.id) as usageCount
        FROM QuestionAnswer qa 
        LEFT JOIN ClosedUserAnswer cua ON qa MEMBER OF cua.selectedAnswers 
        WHERE qa.question.id = :questionId 
        GROUP BY qa.id 
        ORDER BY qa.displayOrder
    """)
    fun findWithUsageCount(@Param("questionId") questionId: Long): List<Array<Any>>

    // Find most popular
    @Query("""
        SELECT qa 
        FROM QuestionAnswer qa 
        LEFT JOIN ClosedUserAnswer cua ON qa MEMBER OF cua.selectedAnswers 
        WHERE qa.question.id = :questionId 
        GROUP BY qa.id 
        ORDER BY COUNT(cua.id) DESC
    """)
    fun findMostPopular(@Param("questionId") questionId: Long): List<QuestionAnswer>

    // Check if being used
    @Query("SELECT COUNT(cua) > 0 FROM ClosedUserAnswer cua WHERE :answer MEMBER OF cua.selectedAnswers")
    fun isBeingUsed(@Param("answer") answer: QuestionAnswer): Boolean
}