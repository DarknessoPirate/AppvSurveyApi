package com.darknessopirate.appvsurveyapi.domain.entity.question

import jakarta.persistence.*

@Entity
@Table(name = "question_answers")
data class QuestionAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: ClosedQuestion? = null, // Now specifically typed

    var text: String,

    var displayOrder: Int = 0
)
