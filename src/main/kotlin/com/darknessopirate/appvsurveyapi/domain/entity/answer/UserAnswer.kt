package com.darknessopirate.appvsurveyapi.domain.entity.answer

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import jakarta.persistence.*

@Entity
@Table(name = "user_answers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "answer_type", discriminatorType = DiscriminatorType.STRING)
abstract class UserAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_survey_id")
    var submittedSurvey: SubmittedSurvey? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    val question: Question
)
