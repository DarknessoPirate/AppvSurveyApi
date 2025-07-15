package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SurveySummaryPassword
import com.darknessopirate.appvsurveyapi.domain.model.AnswerStatistic
import com.darknessopirate.appvsurveyapi.domain.model.ClosedQuestionStatistic
import com.darknessopirate.appvsurveyapi.domain.model.OpenQuestionStatistic
import com.darknessopirate.appvsurveyapi.domain.model.OpenResponse
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveySummaryPasswordRepository
import com.darknessopirate.appvsurveyapi.domain.service.ISurveySummaryService
import jakarta.persistence.EntityNotFoundException
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SurveySummaryServiceImpl(
    private val surveySummaryPasswordRepository: SurveySummaryPasswordRepository,
    private val surveyRepository: SurveyRepository,
    private val submittedSurveyRepository: SubmittedSurveyRepository
) : ISurveySummaryService {

    override fun setSummaryPassword(surveyId: Long, password: String): SurveySummaryPassword {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        val existingPassword = surveySummaryPasswordRepository.findBySurveyId(surveyId)

        return if (existingPassword.isPresent) {
            // Update existing password
            val passwordEntity = existingPassword.get()
            passwordEntity.password = password
            surveySummaryPasswordRepository.save(passwordEntity)
        } else {
            // Create new password
            val newPassword = SurveySummaryPassword(
                survey = survey,
                password = password
            )
            surveySummaryPasswordRepository.save(newPassword)
        }
    }

    override fun deleteSummaryPassword(surveyId: Long) {
        val passwordEntity = surveySummaryPasswordRepository.findBySurveyId(surveyId).orElseThrow {
            EntityNotFoundException("No summary password found for survey with id: $surveyId")
        }
        surveySummaryPasswordRepository.delete(passwordEntity)
    }

    override fun getSummaryPassword(surveyId: Long): SurveySummaryPassword? {
        return surveySummaryPasswordRepository.findBySurveyId(surveyId).orElse(null)
    }

    override fun passwordExists(surveyId: Long): Boolean {
        return surveySummaryPasswordRepository.existsBySurveyId(surveyId)
    }

    override fun getSurveyStatistics(surveyId: Long, password: String): SurveyStatisticsResponse {
        val passwordEntity = surveySummaryPasswordRepository.findBySurveyId(surveyId).orElseThrow {
            EntityNotFoundException("No summary password set for this survey")
        }

        if (passwordEntity.password != password) {
            throw IllegalArgumentException("Invalid password")
        }

        return generateSurveyStatistics(surveyId)
    }

    override fun generateSurveyStatistics(surveyId: Long): SurveyStatisticsResponse {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        val submissions = submittedSurveyRepository.findBySurveyIdWithAnswers(surveyId)

        // Initialize lazy collections
        submissions.forEach { submission ->
            Hibernate.initialize(submission.userAnswers)
            submission.userAnswers.forEach { answer ->
                if (answer is ClosedUserAnswer) {
                    Hibernate.initialize(answer.selectedAnswers)
                }
            }
        }

        val questionStatistics = survey.questions
            .sortedBy { it.displayOrder }
            .map { question ->
                when (question) {
                    is OpenQuestion -> generateOpenQuestionStatistics(question, submissions)
                    is ClosedQuestion -> generateClosedQuestionStatistics(question, submissions)
                    else -> throw IllegalStateException("Unknown question type")
                }
            }

        return SurveyStatisticsResponse(
            surveyId = surveyId,
            surveyTitle = survey.title,
            surveyDescription = survey.description,
            totalSubmissions = submissions.size,
            questionStatistics = questionStatistics
        )
    }

    private fun generateOpenQuestionStatistics(
        question: OpenQuestion,
        submissions: List<com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey>
    ): OpenQuestionStatistic {
        val responses = submissions.mapNotNull { submission ->
            submission.userAnswers
                .filterIsInstance<OpenUserAnswer>()
                .find { it.question.id == question.id }
                ?.takeIf { it.textValue.isNotBlank() }
                ?.let { answer ->
                    OpenResponse(
                        submissionId = submission.id!!,
                        textValue = answer.textValue,
                        submittedAt = submission.submittedAt
                    )
                }
        }

        return OpenQuestionStatistic(
            questionId = question.id!!,
            questionText = question.text,
            totalResponses = responses.size,
            responses = responses
        )
    }

    private fun generateClosedQuestionStatistics(
        question: ClosedQuestion,
        submissions: List<com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey>
    ): ClosedQuestionStatistic {
        // Initialize possible answers
        Hibernate.initialize(question.possibleAnswers)

        val answerCounts = mutableMapOf<Long, Int>()
        val submissionsWithAnswer = mutableSetOf<Long>()

        // Count votes for each answer option and track unique submissions
        submissions.forEach { submission ->
            val closedAnswer = submission.userAnswers
                .filterIsInstance<ClosedUserAnswer>()
                .find { it.question.id == question.id }

            if (closedAnswer != null && closedAnswer.selectedAnswers.isNotEmpty()) {
                submissionsWithAnswer.add(submission.id!!)
                closedAnswer.selectedAnswers.forEach { selectedAnswer ->
                    selectedAnswer.id?.let { answerId ->
                        answerCounts[answerId] = answerCounts.getOrDefault(answerId, 0) + 1
                    }
                }
            }
        }

        val totalVotes = answerCounts.values.sum()

        val answerStatistics = question.possibleAnswers
            .sortedBy { it.displayOrder }
            .map { possibleAnswer ->
                val voteCount = answerCounts.getOrDefault(possibleAnswer.id!!, 0)
                val percentage = if (totalVotes > 0) {
                    (voteCount.toDouble() / totalVotes.toDouble()) * 100
                } else {
                    0.0
                }

                AnswerStatistic(
                    answerId = possibleAnswer.id!!,
                    answerText = possibleAnswer.text,
                    voteCount = voteCount,
                    percentage = String.format("%.2f", percentage).toDouble()
                )
            }

        return ClosedQuestionStatistic(
            questionId = question.id!!,
            questionText = question.text,
            totalResponses = submissionsWithAnswer.size,
            selectionType = question.selectionType.name,
            answerOptions = answerStatistics
        )
    }
}