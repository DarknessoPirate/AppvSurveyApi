package com.darknessopirate.appvsurveyapi.domain.entity.survey

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "surveys")
data class Survey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var title: String,

    var description: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(unique = true)
    var accessCode: String? = null,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<Question> = mutableListOf()
) {
    fun addQuestion(question: Question) {
        questions.add(question)
        question.survey = this
    }

    fun removeQuestion(question: Question) {
        questions.remove(question)
        question.survey = null
    }

    fun addQuestions(newQuestions: List<Question>) {
        newQuestions.forEach { question ->
            // Ensure the question is not shared when added to a survey
            question.isShared = false
            addQuestion(question)
        }
        // Reorder display orders
        questions.forEachIndexed { index, question ->
            question.displayOrder = index + 1
        }
    }
}
