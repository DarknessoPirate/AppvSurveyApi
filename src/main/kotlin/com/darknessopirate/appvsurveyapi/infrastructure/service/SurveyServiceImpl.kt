package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.domain.exception.ResourceNotFoundException
import com.darknessopirate.appvsurveyapi.domain.model.Question
import com.darknessopirate.appvsurveyapi.domain.model.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.model.Survey
import com.darknessopirate.appvsurveyapi.domain.repository.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val questionRepository: QuestionRepository
) : ISurveyService {

    // TODO: FIX DOUBLE QUESTION CREATION WHEN DEFAULT QUESTION PROVIDED IN ENDPOINT
    @Transactional
    override fun createSurvey(survey: Survey): Survey {
        survey.accessCode = UUID.randomUUID().toString()
        return surveyRepository.save(survey)
    }

    // TODO: FIX QUESTIONS NOT UPDATING
    @Transactional
    override fun updateSurvey(id: Long, survey: Survey): Survey {
        val existingSurvey = getSurveyById(id)

        existingSurvey.title = survey.title
        existingSurvey.description = survey.description
        existingSurvey.expiresAt = survey.expiresAt
        existingSurvey.isActive = survey.isActive

        existingSurvey.questions.forEach { question -> existingSurvey.removeQuestion(question) }
        survey.questions.forEach { question -> existingSurvey.addQuestion(question) }

        return surveyRepository.save(existingSurvey)
    }

    // TODO: FIX QUESTIONS BEING DELETED WITH SURVEY, IF THEY ARE UNLINKED BEFOREHAND THEY DONT GET DELETED
    @Transactional
    override fun deleteSurvey(id: Long) {
        val survey = getSurveyById(id)
        surveyRepository.delete(survey)
    }

    @Transactional(readOnly = true)
    override fun getSurveyById(id: Long): Survey {
        val survey = surveyRepository.findByIdWithQuestions(id)

        if(survey == null)
            throw ResourceNotFoundException("Survey not found for id: $id");

        // Force initialization of the possible answers collections
        survey.questions.forEach { question ->
            Hibernate.initialize(question.possibleAnswers)
        }

        return survey
    }

    @Transactional
    override fun getSurveyByAccessCode(accessCode: String): Survey {
        val survey = surveyRepository.findByAccessCodeWithQuestions(accessCode)

        if (survey == null)
            throw ResourceNotFoundException("Survey not found with access code: $accessCode")

        survey.questions.forEach { question ->
            Hibernate.initialize(question.possibleAnswers)
        }

        return survey
    }

    override fun getAllSurveys(): List<Survey> {
        return surveyRepository.findAll()
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

        if(question == null)
            throw ResourceNotFoundException("Question not found with id: $questionId")

        survey.removeQuestion(question)
        return surveyRepository.save(survey)
    }

    @Transactional
    override fun generateSurveyLink(surveyId: Long): String {
        val survey = surveyRepository.findSurveyById(surveyId)

        if(survey == null)
            throw ResourceNotFoundException("Survey not found for id: $surveyId")

        return "http://something.com/survey/${survey.accessCode}"
    }
}