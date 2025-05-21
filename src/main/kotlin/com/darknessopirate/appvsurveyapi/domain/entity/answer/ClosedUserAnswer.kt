package com.darknessopirate.appvsurveyapi.domain.entity.answer

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import jakarta.persistence.*

@Entity
@Table(name = "closed_user_answers")
@DiscriminatorValue("CLOSED")
class ClosedUserAnswer(
    submittedSurvey: SubmittedSurvey? = null,
    question: Question,

    @ManyToMany
    @JoinTable(
        name = "user_answer_selections",
        joinColumns = [JoinColumn(name = "user_answer_id")],
        inverseJoinColumns = [JoinColumn(name = "question_answer_id")]
    )
    val selectedAnswers: MutableList<QuestionAnswer> = mutableListOf()
) : UserAnswer(
    submittedSurvey = submittedSurvey,
    question = question
) {
    fun addSelectedAnswer(answer: QuestionAnswer) {
        selectedAnswers.add(answer)
    }

    fun clearSelectedAnswers() {
        selectedAnswers.clear()
    }
}