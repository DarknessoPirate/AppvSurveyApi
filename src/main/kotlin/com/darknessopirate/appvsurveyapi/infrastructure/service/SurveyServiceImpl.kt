package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.UpdateSurveyRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.survey.SurveyResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.survey.Survey
import com.darknessopirate.appvsurveyapi.domain.exception.InvalidOperationException
import com.darknessopirate.appvsurveyapi.domain.model.QuestionStatistic
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SubmittedSurveyRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.domain.service.ISurveyService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.SurveyMapper
import jakarta.persistence.EntityNotFoundException
import org.hibernate.Hibernate
import org.hibernate.LazyInitializationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val surveyMapper: SurveyMapper,
    private val questionService: IQuestionService,
    private val accessCodeService: IAccessCodeService,
    private val submittedSurveyRepository: SubmittedSurveyRepository,
) : ISurveyService {

    private val logger = LoggerFactory.getLogger(SurveyServiceImpl::class.java)

    // Create survey with questions
    override fun createSurveyWithSelectedQuestions(
        request: CreateSurveyWithQuestionsRequest
    ): Survey {
        val surveyEntity = surveyMapper.toEntity(request)
        val survey = try {
            surveyRepository.save(surveyEntity)
        } catch (e: DataAccessException) {
            logger.error("Failed to save new survey: ${request.title}", e)
            throw IllegalStateException("Failed to create survey", e)
        }

        val surveyId = survey.id
            ?: throw IllegalStateException("Survey ID cannot be null after saving")

        // Add selected questions to survey by copying them
        try {
            request.questionIds.forEachIndexed { index, questionId ->
                questionService.copyQuestionToSurvey(questionId, surveyId, index + 1)
            }
        } catch (e: Exception) {
            logger.error("Failed to copy questions to survey $surveyId", e)
            throw IllegalStateException("Failed to add questions to survey", e)
        }

        return try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to save survey with questions for survey $surveyId", e)
            throw IllegalStateException("Failed to finalize survey creation", e)
        }
    }


    override fun updateSurvey(surveyId: Long, request: UpdateSurveyRequest): SurveyResponse {
        // Find the existing survey
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        // Update the survey fields
        survey.title = request.title
        survey.description = request.description
        survey.expiresAt = request.expiresAt

        // Initialize access codes to prevent lazy loading issues
        try {
            Hibernate.initialize(survey.accessCodes)
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize access codes for survey $surveyId", e)
            throw IllegalStateException("Failed to load survey access codes", e)
        }

        // Save the updated survey
        val updatedSurvey = try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to update survey $surveyId", e)
            throw IllegalStateException("Failed to save survey updates", e)
        }

        // Convert to response and return
        return surveyMapper.toResponse(updatedSurvey)
    }

    // Find survey by access code
    override fun findByAccessCode(accessCode: String): Survey {
        val accessCodeEntity = accessCodeService.validateAccessCode(accessCode)
            ?: throw EntityNotFoundException("Invalid or expired access code")

        val surveyId = accessCodeEntity.survey?.id
            ?: throw IllegalStateException("Access code is not associated with a survey")

        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found")

        if (!survey.isActive) {
            throw IllegalStateException("Survey is not active")
        }

        if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDate.now())) {
            throw IllegalStateException("Survey has expired")
        }

        // Initialize the lazy loaded answers
        try {
            survey.questions.forEach { question ->
                if (question is ClosedQuestion) {
                    Hibernate.initialize(question.possibleAnswers)
                }
            }
            Hibernate.initialize(survey.accessCodes)
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize survey data for access code $accessCode", e)
            throw IllegalStateException("Failed to load complete survey data", e)
        }

        return survey
    }

    // Find survey with all questions and answers
    override fun findWithQuestions(surveyId: Long): Survey {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey with this ID does not exist")

        try {
            survey.questions.forEach { question ->
                if (question is ClosedQuestion) {
                    Hibernate.initialize(question.possibleAnswers)
                }
            }
            Hibernate.initialize(survey.accessCodes)
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize survey questions for survey $surveyId", e)
            throw IllegalStateException("Failed to load complete survey data", e)
        }

        return survey
    }

    //  Add question to survey by copying from shared questions
    @Transactional
    override fun addSharedQuestion(surveyId: Long, sharedQuestionId: Long): Question {
        val maxOrder = getMaxQuestionOrder(surveyId)

        // Copy the question to the survey
        try {
            questionService.copyQuestionToSurvey(sharedQuestionId, surveyId, maxOrder + 1)
        } catch (e: Exception) {
            logger.error("Failed to copy question $sharedQuestionId to survey $surveyId", e)
            throw IllegalStateException("Failed to add question to survey", e)
        }

        // Reload the survey to get the newly added question with proper ID
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        // Find the newly added question by its display order
        val newQuestion = survey.questions.find { it.displayOrder == maxOrder + 1 }
            ?: throw IllegalStateException("Failed to find the newly added question with order ${maxOrder + 1}")

        // Initialize lazy-loaded properties if it's a closed question
        try {
            if (newQuestion is ClosedQuestion) {
                Hibernate.initialize(newQuestion.possibleAnswers)
            }
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize possible answers for new question", e)
            throw IllegalStateException("Failed to load complete question data", e)
        }

        return newQuestion
    }


    // Remove question from survey
    override fun removeQuestion(surveyId: Long, questionId: Long) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        val question = survey.questions.find { it.id == questionId }
            ?: throw EntityNotFoundException("Question not found in survey: $questionId")

        survey.removeQuestion(question)

        try {
            surveyRepository.save(survey)
            reorderQuestions(surveyId)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Cannot remove question $questionId from survey $surveyId - has user responses", e)
            throw InvalidOperationException("Cannot remove question that has user responses")
        } catch (e: DataAccessException) {
            logger.error("Failed to remove question $questionId from survey $surveyId", e)
            throw IllegalStateException("Failed to remove question from survey", e)
        }
    }

    // Reorder questions in survey
    override fun reorderQuestions(surveyId: Long, questionIds: List<Long>?) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        if (questionIds != null) {
            // Reorder according to provided list
            questionIds.forEachIndexed { index, questionId ->
                survey.questions.find { it.id == questionId }?.displayOrder = index + 1
            }
        } else {
            survey.questions.sortedBy { it.displayOrder }.forEachIndexed { index, question ->
                question.displayOrder = index + 1
            }
        }

        try {
            surveyRepository.save(survey)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Cannot reorder questions for survey $surveyId - constraint violation", e)
            throw InvalidOperationException("Cannot reorder questions due to existing user responses")
        } catch (e: DataAccessException) {
            logger.error("Failed to reorder questions for survey $surveyId", e)
            throw IllegalStateException("Failed to reorder questions", e)
        }
    }


    // Activate/deactivate survey
    override fun toggleActive(surveyId: Long) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        // Toggle the current state
        survey.isActive = !survey.isActive

        // Initialize access codes to prevent lazy loading issues
        try {
            Hibernate.initialize(survey.accessCodes)
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize access codes for survey $surveyId", e)
            throw IllegalStateException("Failed to load survey access codes", e)
        }

        try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to toggle active status for survey $surveyId", e)
            throw IllegalStateException("Failed to update survey status", e)
        }
    }

    // Copy survey (with all questions)
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

        return try {
            surveyRepository.save(copiedSurvey)
        } catch (e: DataAccessException) {
            logger.error("Failed to save copied survey: $newTitle", e)
            throw IllegalStateException("Failed to create survey copy", e)
        }
    }


    override fun findAllSurveys(): List<Survey> {
        val surveys = try {
            surveyRepository.findAllWithQuestions()
        } catch (e: DataAccessException) {
            logger.error("Failed to load all surveys", e)
            throw IllegalStateException("Failed to retrieve surveys", e)
        }

        try {
            surveys.forEach { survey ->
                Hibernate.initialize(survey.accessCodes)
            }
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize access codes for surveys", e)
            throw IllegalStateException("Failed to load complete survey data", e)
        }

        return surveys
    }
    // Delete survey and all related data
    override fun deleteSurvey(surveyId: Long) {
        if (!surveyRepository.existsById(surveyId)) {
            throw EntityNotFoundException("Survey not found: $surveyId")
        }

        // Check if survey has submissions
        val submissions = try {
            submittedSurveyRepository.findBySurveyId(surveyId)
        } catch (e: DataAccessException) {
            logger.error("Failed to check submissions for survey $surveyId", e)
            throw IllegalStateException("Failed to verify survey deletion eligibility", e)
        }

        if (submissions.isNotEmpty()) {
            throw InvalidOperationException("Cannot delete survey with existing submissions")
        }

        try {
            surveyRepository.deleteById(surveyId)
        } catch (e: DataAccessException) {
            logger.error("Failed to delete survey $surveyId", e)
            throw IllegalStateException("Failed to delete survey", e)
        }
    }

    override fun getSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<SurveyResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            surveyRepository.findSurveysPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load surveys page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve surveys", e)
        }

        return surveyMapper.toPageResponse(page)
    }

    override fun getActiveSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<SurveyResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            surveyRepository.findActiveSurveysPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load active surveys page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve active surveys", e)
        }

        return surveyMapper.toPageResponse(page)
    }

    override fun getInactiveSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<SurveyResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            surveyRepository.findInactiveSurveysPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load inactive surveys page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve inactive surveys", e)
        }

        return surveyMapper.toPageResponse(page)
    }

    override fun getExpiredSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<SurveyResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            surveyRepository.findExpiredSurveysPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load expired surveys page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve expired surveys", e)
        }

        return surveyMapper.toPageResponse(page)
    }

    override fun getExpiringSurveysPage(
        startDate: LocalDate, endDate: LocalDate,
        pageNumber: Int, pageSize: Int, sortFromOldest: Boolean
    ): PaginatedResponse<SurveyResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("createdAt").ascending()
        } else {
            Sort.by("createdAt").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            surveyRepository.findExpiringBetweenPage(startDate, endDate, pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load expiring surveys page: page=$pageNumber, size=$pageSize, start=$startDate, end=$endDate", e)
            throw IllegalStateException("Failed to retrieve expiring surveys", e)
        }

        return surveyMapper.toPageResponse(page)
    }


    private fun getMaxQuestionOrder(surveyId: Long): Int {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")
        return survey.questions.maxOfOrNull { it.displayOrder } ?: 0
    }
}
