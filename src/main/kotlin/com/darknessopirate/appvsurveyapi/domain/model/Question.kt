package com.darknessopirate.appvsurveyapi.domain.model

import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import jakarta.persistence.*

@Entity
@Table(name = "questions")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    var survey: Survey? = null,

    var text: String,

    @Enumerated(EnumType.STRING)
    val questionType: QuestionType,

    var required: Boolean = false,

    var displayOrder: Int = 0,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    val possibleAnswers: MutableList<QuestionAnswer> = mutableListOf()
) {
    fun addPossibleAnswer(answer: QuestionAnswer) {
        possibleAnswers.add(answer)
        answer.question = this
    }

    fun removePossibleAnswer(answer: QuestionAnswer) {
        possibleAnswers.remove(answer)
        answer.question = null
    }
}