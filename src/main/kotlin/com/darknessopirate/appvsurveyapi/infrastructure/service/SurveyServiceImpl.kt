package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.survey.CreateSurveyWithQuestionsRequest
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class SurveyServiceImpl(
    private val surveyRepository: SurveyRepository,
    private val surveyMapper: SurveyMapper,
    private val questionService: IQuestionService,
    private val accessCodeService: IAccessCodeService,
    private val submittedSurveyRepository: SubmittedSurveyRepository,
) : ISurveyService {

    // Create survey with questions
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

    // Find survey by access code
    override fun findByAccessCode(accessCode: String): Survey {

        val accessCodeEntity = accessCodeService.validateAccessCode(accessCode)
            ?: throw EntityNotFoundException("Invalid or expired access code")

        val survey = surveyRepository.findByIdWithQuestions(accessCodeEntity.survey?.id!!)
            ?: throw EntityNotFoundException("Survey not found")

        if (!survey.isActive) {
            throw IllegalStateException("Survey is not active")
        }

        if (survey.expiresAt != null && survey.expiresAt!!.isBefore(LocalDate.now())) {
            throw IllegalStateException("Survey has expired")
        }

        // Initialize the lazy loaded answers
        survey.questions.forEach { question ->
            if (question is ClosedQuestion) {
                Hibernate.initialize(question.possibleAnswers)
            }
        }

        Hibernate.initialize(survey.accessCodes)

        return survey
    }

    // Find survey with all questions and answers
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

    //  Add question to survey by copying from shared questions
    @Transactional
    override fun addSharedQuestion(surveyId: Long, sharedQuestionId: Long): Question {
        val maxOrder = getMaxQuestionOrder(surveyId)

        // Copy the question to the survey
        questionService.copyQuestionToSurvey(sharedQuestionId, surveyId, maxOrder + 1)

        // Reload the survey to get the newly added question with proper ID
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")

        // Find the newly added question by its display order
        val newQuestion = survey.questions.find { it.displayOrder == maxOrder + 1 }
            ?: throw IllegalStateException("Failed to find the newly added question with order ${maxOrder + 1}")

        // Initialize lazy-loaded properties if it's a closed question
        if (newQuestion is ClosedQuestion) {
            Hibernate.initialize(newQuestion.possibleAnswers)
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
        surveyRepository.save(survey)

        // Reorder remaining questions
        reorderQuestions(surveyId)
    }

    // Reorder questions in survey
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
            survey.questions.sortedBy { it.displayOrder }.forEachIndexed { index, question ->
                question.displayOrder = index + 1
            }
        }

        surveyRepository.save(survey)
    }

    // Activate/deactivate survey
    override fun toggleActive(surveyId: Long) {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found with id: $surveyId")

        // Toggle the current state
        survey.isActive = !survey.isActive

        // Initialize access codes to prevent lazy loading issues
        Hibernate.initialize(survey.accessCodes)

        surveyRepository.save(survey)

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
        

        return surveyRepository.save(copiedSurvey)
    }


    override fun findAllSurveys(): List<Survey> {
        val surveys = surveyRepository.findAllWithQuestions()
        surveys.forEach { survey -> Hibernate.initialize(survey.accessCodes)}
        return surveys
    }
    // Delete survey and all related data
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

    @Transactional
    override fun getSurveysPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean) : PaginatedResponse<SurveyResponse>
    {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = surveyRepository.findSurveysPage(pageable)

        return surveyMapper.toPageResponse(page)
    }
    // Helper methods
    private fun getMaxQuestionOrder(surveyId: Long): Int {
        val survey = surveyRepository.findByIdWithQuestions(surveyId)
            ?: throw EntityNotFoundException("Survey not found: $surveyId")
        return survey.questions.maxOfOrNull { it.displayOrder } ?: 0
    }


}
