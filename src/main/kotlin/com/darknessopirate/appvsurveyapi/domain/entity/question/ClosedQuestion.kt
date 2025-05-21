package com.darknessopirate.appvsurveyapi.domain.entity.question

import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "closed_questions")
@DiscriminatorValue("CLOSED")
class ClosedQuestion(
    text: String,
    required: Boolean = false,
    displayOrder: Int = 0,
    isShared: Boolean = false,

    @Enumerated(EnumType.STRING)
    val selectionType: SelectionType = SelectionType.SINGLE,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    val possibleAnswers: MutableList<QuestionAnswer> = mutableListOf()
) : Question(
    text = text,
    required = required,
    displayOrder = displayOrder,
    isShared = isShared
) {
    override fun createUserAnswer(submittedSurvey: SubmittedSurvey): UserAnswer {
        return ClosedUserAnswer(
            submittedSurvey = submittedSurvey,
            question = this
        )
    }

    override fun copy(): ClosedQuestion {
        val copiedQuestion = ClosedQuestion(
            text = this.text,
            required = this.required,
            displayOrder = this.displayOrder,
            isShared = false, // Copied questions are never shared
            selectionType = this.selectionType
        )

        // Copy all possible answers
        this.possibleAnswers.forEach { originalAnswer ->
            val copiedAnswer = QuestionAnswer(
                text = originalAnswer.text,
                displayOrder = originalAnswer.displayOrder
            )
            copiedQuestion.addPossibleAnswer(copiedAnswer)
        }

        return copiedQuestion
    }

    fun addPossibleAnswer(answer: QuestionAnswer) {
        possibleAnswers.add(answer)
        answer.question = this
    }
}