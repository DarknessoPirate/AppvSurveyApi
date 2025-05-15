package com.darknessopirate.appvsurveyapi.domain.model

data class ClosedQuestion : Question()
{
    fun addPossibleAnswer(answer: QuestionAnswer) {
        possibleAnswers.add(answer)
        answer.question = this
    }

    fun removePossibleAnswer(answer: QuestionAnswer) {
        possibleAnswers.remove(answer)
        answer.question = null
    }
}