package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.exception.AccessCodeGenerationException
import com.darknessopirate.appvsurveyapi.domain.exception.InvalidOperationException
import com.darknessopirate.appvsurveyapi.domain.model.SurveyStatistics
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import jakarta.persistence.EntityNotFoundException
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val surveyMapper: SurveyMapper,
    private val questionService: IQuestionService,
    private val accessCodeService: IAccessCodeService,
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
    override fun createSurveyWithSelectedQuestions(
        request: CreateSurveyWithQuestionsRequest
    ): Survey {
        val surveyEntity = surveyMapper.toEntity(request)
        val survey = surveyRepository.save(surveyEntity)

        // Add selected questions to survey by copying them
        request.questionIds.forEachIndexed { index, questionId ->
            questionService.copyQuestionToSurvey(questionId, survey.id!!, index + 1)
        }

        return surveyRepository.save(survey)
    }

    override fun updateSurvey(id: Long, request: CreateSurveyRequest): Survey {
        val survey = surveyRepository.findByIdWithQuestions(id)

        if(survey == null)
            throw EntityNotFoundException("Survey not found with id: $id")

        survey.title = request.title
        survey.description = request.description ?: survey.description
        survey.expiresAt = request.expiresAt ?: survey.expiresAt

        return surveyRepository.save(survey)
    }


    /**
     * Find survey by access code
     */
    override fun findByAccessCode(accessCode: String): Survey {

        val accessCodeEntity = accessCodeService.validateAccessCode(accessCode)
            ?: throw EntityNotFoundException("Invalid or expired access code")

        val survey = surveyRepository.findByIdWithQuestions(accessCodeEntity.survey?.id!!)
            ?: throw EntityNotFoundException("Survey not found")

        // Increment usage count
        accessCodeService.incrementUsage(accessCodeEntity.id!!)

        // Initialize the lazy loaded answers
        survey.questions.forEach { question ->
            if (question is ClosedQuestion) {
                Hibernate.initialize(question.possibleAnswers)
            }
        }

        return survey
    }

    /**
     * Find survey with all questions and answers
     */
    override fun findWithQuestions(surveyId: Long): Survey {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)

        if(survey == null)
            throw EntityNotFoundException("Survey with this ID does not exist")

        survey.questions.forEach { question -> if(question is ClosedQuestion){
            Hibernate.initialize(question.possibleAnswers)
        }
        }

        Hibernate.initialize(survey.accessCodes)

        return survey
    }


    // TODO: ADD FIND ALL SURVEYS ( ACTIVE + INACTIVE)
    // TODO: ADD FIND ALL INACTIVE
    // TODO: CHANGE DEFAULT FROM ACTIVE TO NOT ACTIVE
    // TODO: CHANGE COPYING LOGIC
    // TODO: CHANGE ACCESS CODE COPYING LOGIC (CURRENTLY DUPLICATE KEY EXCEPTION - JUST GENERATE NEW)
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

        if (survey == null)
            throw EntityNotFoundException("Survey not found with id : $surveyId")

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
    override fun toggleActive(surveyId: Long) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        // Toggle the current state
        survey.isActive = !survey.isActive

        // Initialize access codes to prevent lazy loading issues
        Hibernate.initialize(survey.accessCodes)

        surveyRepository.save(survey)

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
     * Copy survey (with all questions)
     */
    override fun copySurvey(surveyId: Long, newTitle: String): Survey {
        val originalSurvey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        val copiedSurvey = Survey(
            title = newTitle,
            description = originalSurvey.description,
            expiresAt = originalSurvey.expiresAt,
            isActive = false
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

        // Map ugly class names to readable question types
        val readableQuestionCounts = questionCount.associate { row ->
            val className = row[0].toString()
            val count = (row[1] as Long).toInt()

            val readableType = when {
                className.contains("OpenQuestion") -> "OPEN"
                className.contains("ClosedQuestion") -> "CLOSED"
                else -> className.substringAfterLast(".") // fallback to just class name
            }

            readableType to count
        }

        return SurveyStatistics(
            surveyId = surveyId,
            title = survey.title,
            totalSubmissions = submissions.size,
            questionCounts = readableQuestionCounts,
            isActive = survey.isActive,
            expiresAt = survey.expiresAt,
            createdAt = survey.createdAt
        )
    }

    override fun findAllSurveys(): List<Survey> {
        val surveys = surveyRepository.findAllWithQuestions()
        surveys.forEach { survey -> Hibernate.initialize(survey.accessCodes)}
        return surveys
    }


    override fun findActiveSurveys(): List<Survey> {
        val surveys = surveyRepository.findActiveWithQuestions()
        surveys.forEach { survey -> Hibernate.initialize(survey.accessCodes)}
        return surveys
    }


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
            throw InvalidOperationException("Cannot delete survey with existing submissions")
        }

        surveyRepository.deleteById(surveyId)
    }

    // Helper methods
    private fun getMaxQuestionOrder(surveyId: Long): Int {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")
        return survey.questions.maxOfOrNull { it.displayOrder } ?: 0
    }


}
