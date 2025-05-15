package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.domain.enums.QuestionType
import com.darknessopirate.appvsurveyapi.domain.exception.ResourceNotFoundException
import com.darknessopirate.appvsurveyapi.domain.model.Question
import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.repository.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuestionServiceImpl(
    private val questionRepository: QuestionRepository
) : IQuestionService {

    @Transactional
    override fun createQuestion(question: Question): Question {
        return questionRepository.save(question)
    }

    @Transactional
    override fun updateQuestion(id: Long, question: Question): Question {
        val existingQuestion = getQuestionById(id)

        existingQuestion.text = question.text
        existingQuestion.required = question.required
        existingQuestion.displayOrder = question.displayOrder

        // Clear existing answers and add new ones
        existingQuestion.possibleAnswers.clear()
        question.possibleAnswers.forEach { answer ->
            existingQuestion.addPossibleAnswer(
                QuestionAnswer(
                    text = answer.text,
                    displayOrder = answer.displayOrder
                )
            )
        }

        return questionRepository.save(existingQuestion)
    }

    @Transactional
    override fun deleteQuestion(id: Long) {
        val question = getQuestionById(id)
        questionRepository.delete(question)
    }

    override fun getQuestionById(id: Long): Question {
        val question = questionRepository.findByIdWithAnswers(id)

        if(question == null)
            throw ResourceNotFoundException("Question not found with id: $id")

        return question
    }

    @Transactional(readOnly = true)
    override fun getAllQuestions(): List<Question> {
        return questionRepository.findAllWithPossibleAnswers()
    }

    @Transactional(readOnly = true)
    override fun getReusableQuestions(surveyId: Long): List<Question> {
        return questionRepository.findQuestionsNotInSurveyWithAnswers(surveyId)
    }

    @Transactional(readOnly = true)
    override fun getQuestionsByType(questionType: QuestionType): List<Question> {
        return questionRepository.findByQuestionTypeWithAnswers(questionType)
    }




}