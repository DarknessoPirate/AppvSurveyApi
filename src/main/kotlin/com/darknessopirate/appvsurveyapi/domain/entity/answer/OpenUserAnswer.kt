package com.darknessopirate.appvsurveyapi.domain.entity.answer

import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "open_user_answers")
@DiscriminatorValue("OPEN")
class OpenUserAnswer(
    submittedSurvey: SubmittedSurvey? = null,
    question: Question,
    var textValue: String = "" // No longer nullable!
) : UserAnswer(
    submittedSurvey = submittedSurvey,
    question = question
)