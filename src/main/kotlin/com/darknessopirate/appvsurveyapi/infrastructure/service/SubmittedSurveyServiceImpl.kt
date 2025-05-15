package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.domain.exception.ResourceNotFoundException
import com.darknessopirate.appvsurveyapi.domain.model.SubmittedSurvey
import com.darknessopirate.appvsurveyapi.domain.model.UserAnswer
import com.darknessopirate.appvsurveyapi.domain.repository.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.UserAnswerRepository
import com.darknessopirate.appvsurveyapi.domain.service.ISubmittedSurveyService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SubmittedSurveyServiceImpl(
    private val submittedSurveyRepository: SubmittedSurveyRepository,
    private val surveyRepository: SurveyRepository,
    private val userAnswerRepository: UserAnswerRepository
) : ISubmittedSurveyService {

    @Transactional
    override fun createSubmission(surveyId: Long, userAnswers: List<UserAnswer>): SubmittedSurvey {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            ResourceNotFoundException("Survey not found with id: $surveyId")
        }

        val submittedSurvey = SubmittedSurvey(survey = survey)

        userAnswers.forEach { userAnswer ->
            submittedSurvey.addUserAnswer(userAnswer)
        }

        return submittedSurveyRepository.save(submittedSurvey)
    }

    override fun getSubmissionById(id: Long): SubmittedSurvey {
        return submittedSurveyRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Response not found with id: $id")
        }
    }

    override fun getSubmissionsBySurveyId(surveyId: Long): List<SubmittedSurvey> {
        return submittedSurveyRepository.findBySurveyId(surveyId)
    }

    override fun getSubmissionsByTimeRange(startTime: LocalDateTime, endTime: LocalDateTime): List<SubmittedSurvey> {
        return submittedSurveyRepository.findBySubmittedAtBetween(startTime, endTime)
    }

    override fun getUserAnswersByQuestionId(questionId: Long): List<UserAnswer> {
        return userAnswerRepository.findByQuestionId(questionId)
    }


    fun getUserAnswersBySubmittedSurveyId(submittedSurveyId: Long): List<UserAnswer> {
        return userAnswerRepository.findBySubmittedSurveyId(submittedSurveyId)
    }
}