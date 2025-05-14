package com.darknessopirate.appvsurveyapi.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "user_answers")
data class UserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_survey_id")
    var submittedSurvey: SubmittedSurvey? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    val question: Question,

    // For text/open-ended questions
    var textValue: String? = null,

    // For questions with single or multiple choices
    @ManyToMany
    @JoinTable(
        name = "user_answer_selections",
        joinColumns = [JoinColumn(name = "user_answer_id")],
        inverseJoinColumns = [JoinColumn(name = "question_answer_id")]
    )
    val selectedAnswers: MutableList<QuestionAnswer> = mutableListOf()
)