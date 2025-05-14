package com.darknessopirate.appvsurveyapi.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "submitted_surveys")
data class SubmittedSurvey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    val survey: Survey,

    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "submittedSurvey", cascade = [CascadeType.ALL])
    val userAnswers: MutableList<UserAnswer> = mutableListOf()
) {
    fun addUserAnswer(userAnswer: UserAnswer) {
        userAnswers.add(userAnswer)
        userAnswer.submittedSurvey = this
    }
}