package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyStatisticsResponse
import com.darknessopirate.appvsurveyapi.domain.entity.answer.ClosedUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.answer.OpenUserAnswer
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey
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
import org.hibernate.LazyInitializationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SurveySummaryServiceImpl(
    private val surveySummaryPasswordRepository: SurveySummaryPasswordRepository,
    private val surveyRepository: SurveyRepository,
    private val submittedSurveyRepository: SubmittedSurveyRepository
) : ISurveySummaryService {

    private val logger = LoggerFactory.getLogger(SurveySummaryServiceImpl::class.java)

    override fun setSummaryPassword(surveyId: Long, password: String): SurveySummaryPassword {
        try {
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
        } catch (e: DataAccessException) {
            logger.error("Database error while setting summary password for survey $surveyId", e)
            throw IllegalStateException("Failed to set summary password due to database error", e)
        }
    }

    override fun deleteSummaryPassword(surveyId: Long) {
        try {
            val passwordEntity = surveySummaryPasswordRepository.findBySurveyId(surveyId).orElseThrow {
                EntityNotFoundException("No summary password found for survey with id: $surveyId")
            }
            surveySummaryPasswordRepository.delete(passwordEntity)
        } catch (e: DataAccessException) {
            logger.error("Database error while deleting summary password for survey $surveyId", e)
            throw IllegalStateException("Failed to delete summary password due to database error", e)
        }
    }

    override fun getSummaryPassword(surveyId: Long): SurveySummaryPassword? {
        return try {
            surveySummaryPasswordRepository.findBySurveyId(surveyId).orElse(null)
        } catch (e: DataAccessException) {
            logger.error("Database error while getting summary password for survey $surveyId", e)
            throw IllegalStateException("Failed to retrieve summary password due to database error", e)
        }
    }

    override fun passwordExists(surveyId: Long): Boolean {
        return try {
            surveySummaryPasswordRepository.existsBySurveyId(surveyId)
        } catch (e: DataAccessException) {
            logger.error("Database error while checking password existence for survey $surveyId", e)
            throw IllegalStateException("Failed to check password existence due to database error", e)
        }
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


        try {
            submissions.forEach { submission ->
                Hibernate.initialize(submission.userAnswers)
                submission.userAnswers.forEach { answer ->
                    if (answer is ClosedUserAnswer) {
                        Hibernate.initialize(answer.selectedAnswers)
                    }
                }
            }
        } catch (e: LazyInitializationException) {
            logger.error("Lazy initialization error while loading survey data for survey $surveyId", e)
            throw IllegalStateException("Failed to load survey data: survey session may have been closed", e)
        } catch (e: Exception) {
            logger.error("Unexpected error while initializing survey data for survey $surveyId", e)
            throw IllegalStateException("Failed to load survey data", e)
        }

        val questionStatistics = survey.questions
            .sortedBy { it.displayOrder }
            .map { question ->
                when (question) {
                    is OpenQuestion -> generateOpenQuestionStatistics(question, submissions)
                    is ClosedQuestion -> generateClosedQuestionStatistics(question, submissions)
                    else -> throw IllegalStateException("Unknown question type: ${question::class.simpleName}")
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
        val questionId = question.id
            ?: throw IllegalStateException("Question ID cannot be null for question: ${question.text}")

        val responses = submissions.mapNotNull { submission ->
            val submissionId = submission.id
                ?: throw IllegalStateException("Submission ID cannot be null")

            submission.userAnswers
                .filterIsInstance<OpenUserAnswer>()
                .find { it.question.id == questionId }
                ?.takeIf { it.textValue.isNotBlank() }
                ?.let { answer ->
                    OpenResponse(
                        submissionId = submissionId,
                        textValue = answer.textValue,
                        submittedAt = submission.submittedAt
                    )
                }
        }

        return OpenQuestionStatistic(
            questionId = questionId,
            questionText = question.text,
            totalResponses = responses.size,
            responses = responses
        )
    }

    private fun generateClosedQuestionStatistics(
        question: ClosedQuestion,
        submissions: List<com.darknessopirate.appvsurveyapi.domain.entity.survey.SubmittedSurvey>
    ): ClosedQuestionStatistic {
        val questionId = question.id
            ?: throw IllegalStateException("Question ID cannot be null for question: ${question.text}")

        // Initialize possible answers
        try {
            Hibernate.initialize(question.possibleAnswers)
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize possible answers for question $questionId", e)
            throw IllegalStateException("Failed to load question answers", e)
        }

        val answerCounts = mutableMapOf<Long, Int>()
        val submissionsWithAnswer = mutableSetOf<Long>()

        // Count votes for each answer option and track unique submissions
        submissions.forEach { submission ->
            val submissionId = submission.id
                ?: throw IllegalStateException("Submission ID cannot be null")

            val closedAnswer = submission.userAnswers
                .filterIsInstance<ClosedUserAnswer>()
                .find { it.question.id == questionId }

            if (closedAnswer != null && closedAnswer.selectedAnswers.isNotEmpty()) {
                submissionsWithAnswer.add(submissionId)
                closedAnswer.selectedAnswers.forEach { selectedAnswer ->
                    val answerId = selectedAnswer.id
                    if (answerId != null) {
                        answerCounts[answerId] = answerCounts.getOrDefault(answerId, 0) + 1
                    } else {
                        logger.warn("Selected answer has null ID for question $questionId")
                    }
                }
            }
        }

        val totalVotes = answerCounts.values.sum()

        val answerStatistics = question.possibleAnswers
            .filter { it.id != null } // Filter out answers with null IDs
            .sortedBy { it.displayOrder }
            .map { possibleAnswer ->
                val answerId = possibleAnswer.id!!
                val voteCount = answerCounts.getOrDefault(answerId, 0)
                val percentage = try {
                    if (totalVotes > 0) {
                        (voteCount.toDouble() / totalVotes.toDouble()) * 100
                    } else {
                        0.0
                    }
                } catch (e: ArithmeticException) {
                    logger.warn("Arithmetic error calculating percentage for answer $answerId", e)
                    0.0
                }


                val formattedPercentage = try {
                    kotlin.math.round(percentage * 100) / 100.0
                } catch (e: ArithmeticException) {
                    logger.warn("Arithmetic error for percentage $percentage", e)
                    0.0
                }

                AnswerStatistic(
                    answerId = answerId,
                    answerText = possibleAnswer.text,
                    voteCount = voteCount,
                    percentage = formattedPercentage
                )
            }

        return ClosedQuestionStatistic(
            questionId = questionId,
            questionText = question.text,
            totalResponses = submissionsWithAnswer.size,
            selectionType = question.selectionType.name,
            answerOptions = answerStatistics
        )
    }
}