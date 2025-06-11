package com.darknessopirate.appvsurveyapi.domain.entity.survey

import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_code_id")
    val accessCode: AccessCode? = null,


    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "submittedSurvey", cascade = [CascadeType.ALL])
    val userAnswers: MutableList<UserAnswer> = mutableListOf()
) {
    fun addUserAnswer(userAnswer: UserAnswer) {
        userAnswers.add(userAnswer)
        userAnswer.submittedSurvey = this
    }
}