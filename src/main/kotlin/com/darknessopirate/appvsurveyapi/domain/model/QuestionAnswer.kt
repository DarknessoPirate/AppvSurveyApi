package com.darknessopirate.appvsurveyapi.domain.model

import jakarta.persistence.*

@Entity
@Table(name = "question_answers")
data class QuestionAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question? = null,

    var text: String,

    var displayOrder: Int = 0
)