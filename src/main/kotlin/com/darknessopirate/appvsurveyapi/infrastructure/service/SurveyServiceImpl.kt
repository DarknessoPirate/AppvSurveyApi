package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.domain.exception.ResourceNotFoundException
import com.darknessopirate.appvsurveyapi.domain.model.Question
import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.model.Survey
import com.darknessopirate.appvsurveyapi.domain.repository.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.SurveyService
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val questionRepository: QuestionRepository
) : SurveyService {

    @Transactional
    override fun createSurvey(survey: Survey): Survey {
        survey.accessCode = generateUniqueAccessCode()
        return surveyRepository.save(survey)
    }

    @Transactional(readOnly = true)
    override fun getSurveyById(id: Long): Survey {
        val survey = surveyRepository.findByIdWithQuestions(id).orElseThrow {
            ResourceNotFoundException("Survey not found with id: $id")
        }

        // Force initialization of the possible answers collections
        survey.questions.forEach { question ->
            Hibernate.initialize(question.possibleAnswers)
        }

        return survey
    }

    override fun getSurveyByAccessCode(accessCode: String): Survey {
        return surveyRepository.findByAccessCode(accessCode)
            ?: throw ResourceNotFoundException("Survey not found with access code: $accessCode")
    }

    @Transactional
    override fun updateSurvey(id: Long, survey: Survey): Survey {
        val existingSurvey = getSurveyById(id)

        existingSurvey.title = survey.title
        existingSurvey.description = survey.description
        existingSurvey.expiresAt = survey.expiresAt
        existingSurvey.isActive = survey.isActive

        return surveyRepository.save(existingSurvey)
    }

    @Transactional
    override fun deleteSurvey(id: Long) {
        val survey = getSurveyById(id)
        surveyRepository.delete(survey)
    }

    @Transactional
    override fun addQuestionToSurvey(surveyId: Long, question: Question): Survey {
        val survey = getSurveyById(surveyId)
        survey.addQuestion(question)
        return surveyRepository.save(survey)
    }

    @Transactional
    override fun addExistingQuestionToSurvey(surveyId: Long, questionId: Long): Survey {
        val survey = getSurveyById(surveyId)
        val question = questionRepository.findById(questionId).orElseThrow {
            ResourceNotFoundException("Question not found with id: $questionId")
        }

        // Create a deep copy of the question with answers
        val questionCopy = Question(
            text = question.text,
            questionType = question.questionType,
            required = question.required,
            displayOrder = survey.questions.size + 1
        )

        // Copy possible answers
        question.possibleAnswers.forEach { answer ->
            questionCopy.addPossibleAnswer(
                QuestionAnswer(
                    text = answer.text,
                    displayOrder = answer.displayOrder
                )
            )
        }

        survey.addQuestion(questionCopy)
        return surveyRepository.save(survey)
    }

    @Transactional
    override fun removeQuestionFromSurvey(surveyId: Long, questionId: Long): Survey {
        val survey = getSurveyById(surveyId)
        val question = survey.questions.find { it.id == questionId }
            ?: throw ResourceNotFoundException("Question not found in survey with id: $questionId")

        survey.removeQuestion(question)
        return surveyRepository.save(survey)
    }

    override fun getAllSurveys(): List<Survey> {
        return surveyRepository.findAll()
    }

    override fun generateSurveyLink(surveyId: Long): String {
        val survey = getSurveyById(surveyId)
        return "http://something.com/survey/${survey.accessCode}"
    }

    private fun generateUniqueAccessCode(): String {
        var accessCode: String
        do {
            accessCode = UUID.randomUUID().toString()
        } while (surveyRepository.findByAccessCode(accessCode) != null)

        return accessCode
    }
}