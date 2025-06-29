package com.darknessopirate.appvsurveyapi.domain.entity.question

import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import jakarta.persistence.*

@Entity
@Table(name = "closed_questions")
@DiscriminatorValue("CLOSED")
class ClosedQuestion(
    text: String,
    description: String?,
    required: Boolean = false,
    displayOrder: Int = 0,
    isShared: Boolean = false,

    @Enumerated(EnumType.STRING)
    var selectionType: SelectionType = SelectionType.SINGLE,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], orphanRemoval = true)
    val possibleAnswers: MutableList<QuestionAnswer> = mutableListOf()
) : Question(
    text = text,
    description = description,
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
            description = this.description,
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