package com.darknessopirate.appvsurveyapi.domain.entity.survey

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import jakarta.persistence.*
import java.time.LocalDate
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
    var expiresAt: LocalDate? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val accessCodes: MutableList<AccessCode> = mutableListOf(),

    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<Question> = mutableListOf()
)
{
    fun addQuestion(question: Question) {
        questions.add(question)
        question.survey = this
    }

    fun removeQuestion(question: Question) {
        questions.remove(question)
        question.survey = null
    }

    fun addAccessCode(accessCode: AccessCode) {
        accessCodes.add(accessCode)
        accessCode.survey = this
    }

    fun removeAccessCode(accessCode: AccessCode) {
        accessCodes.remove(accessCode)
        accessCode.survey = null
    }

}
