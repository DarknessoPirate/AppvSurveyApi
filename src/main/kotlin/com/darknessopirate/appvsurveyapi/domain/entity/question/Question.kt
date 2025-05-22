package com.darknessopirate.appvsurveyapi.domain.entity.question

import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import jakarta.persistence.*

@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "question_type", discriminatorType = DiscriminatorType.STRING)
abstract class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    var survey: Survey? = null,

    var text: String,

    var required: Boolean = false,

    var displayOrder: Int = 0,

    // Flag to indicate if this question can be reused (shared) or is survey-specific
    var isShared: Boolean = false
) {
    abstract fun createUserAnswer(submittedSurvey: SubmittedSurvey): UserAnswer
    abstract fun copy(): Question // Method to create a copy for survey use
}
