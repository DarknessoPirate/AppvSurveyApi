package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val surveyMapper: SurveyMapper,
    private val questionService: IQuestionService,
    private val submittedSurveyRepository: SubmittedSurveyRepository,
) : ISurveyService {

    /**
     * Create a new survey
     */
    override fun createSurvey(
        request: CreateSurveyRequest
    ): Survey {
        val surveyEntity = surveyMapper.toEntity(request)
        return surveyRepository.save(surveyEntity)
    }

    /**
     * Create survey with questions
     */
    override fun createSurveyWithQuestions(
        request: CreateSurveyWithQuestionsRequest
    ): Survey {
        val (surveyEntity,questions) = surveyMapper.toEntity(request)
        val survey = surveyRepository.save(surveyEntity)
        // Add questions to survey (they'll be copied and marked as non-shared)
        questions.forEachIndexed { index, question ->
            val questionCopy = question.copy()
            questionCopy.displayOrder = index + 1
            survey.addQuestion(questionCopy)
        }

        return surveyRepository.save(survey)
    }

    /**
     * Find survey by access code
     */
    override fun findByAccessCode(accessCode: String): Survey? {
        return surveyRepository.findByAccessCodeWithQuestions(accessCode)
    }

    /**
     * Find survey with all questions and answers
     */
    override fun findWithQuestions(surveyId: Long): Survey? {
        return surveyRepository.findByIdWithQuestions(surveyId)
    }

    /**
     * Add question to survey by copying from shared questions
     */
    override fun addSharedQuestion(surveyId: Long, sharedQuestionId: Long): Question {
        val maxOrder = getMaxQuestionOrder(surveyId)
        return questionService.copyQuestionToSurvey(sharedQuestionId, surveyId, maxOrder + 1)
    }

    /**
     * Add new question directly to survey
     */
    override fun addOpenQuestion(
        surveyId: Long,
        text: String,
        required: Boolean
    ): OpenQuestion {
        val maxOrder = getMaxQuestionOrder(surveyId)
        return questionService.createSurveySpecificOpenQuestion(
            surveyId, text, required, maxOrder + 1
        )
    }

    /**
     * Add new closed question directly to survey
     */
    override fun addClosedQuestion(
        surveyId: Long,
        text: String,
        required: Boolean,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion {
        val maxOrder = getMaxQuestionOrder(surveyId)
        return questionService.createSurveySpecificClosedQuestion(
            surveyId, text, required, maxOrder + 1, selectionType, possibleAnswers
        )
    }

    /**
     * Remove question from survey
     */
    override fun removeQuestion(surveyId: Long, questionId: Long) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        val question = survey.questions.find { it.id == questionId }
            ?: throw EntityNotFoundException("Question not found in survey: $questionId")

        survey.removeQuestion(question)
        surveyRepository.save(survey)

        // Reorder remaining questions
        reorderQuestions(surveyId)
    }

    /**
     * Reorder questions in survey
     */
    override fun reorderQuestions(surveyId: Long, questionIds: List<Long>?) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        if (questionIds != null) {
            // Reorder according to provided list
            questionIds.forEachIndexed { index, questionId ->
                survey.questions.find { it.id == questionId }?.displayOrder = index + 1
            }
        } else {
            // Just fix the ordering
            survey.questions.sortedBy { it.displayOrder }.forEachIndexed { index, question ->
                question.displayOrder = index + 1
            }
        }

        surveyRepository.save(survey)
    }

    /**
     * Activate/deactivate survey
     */
    override fun setActive(surveyId: Long, isActive: Boolean): Survey {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }
        survey.isActive = isActive
        return surveyRepository.save(survey)
    }

    /**
     * Set survey expiration
     */
    override fun setExpiration(surveyId: Long, expiresAt: LocalDateTime?): Survey {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }
        survey.expiresAt = expiresAt
        return surveyRepository.save(survey)
    }

    /**
     * Generate unique access code
     */
    override fun generateAccessCode(surveyId: Long): Survey {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }

        var accessCode: String
        do {
            accessCode = generateRandomCode()
        } while (surveyRepository.findByAccessCodeWithQuestions(accessCode) != null)

        survey.accessCode = accessCode
        return surveyRepository.save(survey)
    }

    /**
     * Copy survey (with all questions)
     */
    override fun copySurvey(surveyId: Long, newTitle: String, includeAccessCode: Boolean): Survey {
        val originalSurvey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        val copiedSurvey = Survey(
            title = newTitle,
            description = originalSurvey.description,
            expiresAt = originalSurvey.expiresAt,
            accessCode = if (includeAccessCode) originalSurvey.accessCode else null,
            isActive = false // Copies start inactive
        )

        // Copy all questions
        originalSurvey.questions.forEach { question ->
            val copiedQuestion = question.copy()
            copiedSurvey.addQuestion(copiedQuestion)
        }

        return surveyRepository.save(copiedSurvey)
    }

    /**
     * Get survey statistics
     */
    override fun getStatistics(surveyId: Long): SurveyStatistics {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }

        val submissions = submittedSurveyRepository.findBySurveyId(surveyId)
        val questionCount = surveyRepository.countQuestionsByType(surveyId)

        return SurveyStatistics(
            surveyId = surveyId,
            title = survey.title,
            totalSubmissions = submissions.size,
            questionCounts = questionCount.associate {
                it[0].toString() to (it[1] as Long).toInt()
            },
            isActive = survey.isActive,
            expiresAt = survey.expiresAt,
            createdAt = survey.createdAt
        )
    }

    /**
     * Find active surveys
     */
    override fun findActiveSurveys(): List<Survey> {
        return surveyRepository.findByIsActiveOrderByCreatedAtDesc(true)
    }

    /**
     * Find expiring surveys
     */
    override fun findExpiringSoon(days: Int): List<Survey> {
        val now = LocalDateTime.now()
        val endDate = now.plusDays(days.toLong())
        return surveyRepository.findExpiringBetween(now, endDate)
    }

    /**
     * Check if survey accepts submissions
     */
    override fun acceptsSubmissions(surveyId: Long): Boolean {
        val survey = surveyRepository.findById(surveyId).orElse(null) ?: return false
        return survey.isActive && (survey.expiresAt == null || survey.expiresAt!!.isAfter(LocalDateTime.now()))
    }

    /**
     * Delete survey and all related data
     */
    override fun deleteSurvey(surveyId: Long) {
        if (!surveyRepository.existsById(surveyId)) {
            throw EntityNotFoundException("Survey not found: $surveyId")
        }

        // Check if survey has submissions
        val submissions = submittedSurveyRepository.findBySurveyId(surveyId)
        if (submissions.isNotEmpty()) {
            throw IllegalStateException("Cannot delete survey with existing submissions")
        }

        surveyRepository.deleteById(surveyId)
    }

    // Helper methods
    private fun getMaxQuestionOrder(surveyId: Long): Int {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")
        return survey.questions.maxOfOrNull { it.displayOrder } ?: 0
    }

    private fun generateRandomCode(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
