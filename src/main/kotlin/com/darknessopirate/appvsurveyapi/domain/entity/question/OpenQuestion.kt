package com.darknessopirate.appvsurveyapi.domain.entity.question

import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "open_questions")
@DiscriminatorValue("OPEN")
class OpenQuestion(
    text: String,
    description: String?,
    required: Boolean = false,
    displayOrder: Int = 0,
    isShared: Boolean = false
) : Question(
    text = text,
    description = description,
    required = required,
    displayOrder = displayOrder,
    isShared = isShared
) {
    override fun createUserAnswer(submittedSurvey: SubmittedSurvey): UserAnswer {
        return OpenUserAnswer(
            submittedSurvey = submittedSurvey,
            question = this
        )
    }

    override fun copy(): OpenQuestion {
        return OpenQuestion(
            text = this.text,
            description = this.description,
            required = this.required,
            displayOrder = this.displayOrder,
            isShared = false // Copied questions are never shared
        )
    }
}