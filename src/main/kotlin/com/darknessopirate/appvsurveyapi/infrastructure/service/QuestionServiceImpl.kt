package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionCountsResponse
import com.darknessopirate.appvsurveyapi.api.dto.response.question.QuestionResponse
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.exception.InvalidOperationException
import com.darknessopirate.appvsurveyapi.domain.repository.question.ClosedQuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.question.OpenQuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.question.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import com.darknessopirate.appvsurveyapi.infrastructure.mappers.QuestionMapper
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

@Service
@Transactional
class QuestionServiceImpl(
    private val questionRepository: QuestionRepository,
    private val openQuestionRepository: OpenQuestionRepository,
    private val closedQuestionRepository: ClosedQuestionRepository,
    private val surveyRepository: SurveyRepository,
    private val questionMapper: QuestionMapper
) : IQuestionService {

    private val logger = LoggerFactory.getLogger(QuestionServiceImpl::class.java)

    // Create a shared question (manually created, available for copying)
    override fun createSharedOpenQuestion(request: OpenQuestionRequest): OpenQuestion {
        val question = OpenQuestion(
            text = request.text,
            description = request.description,
            required = request.required,
            isShared = true
        )

        return try {
            openQuestionRepository.save(question)
        } catch (e: DataAccessException) {
            logger.error("Failed to save shared open question: ${request.text}", e)
            throw IllegalStateException("Failed to create shared open question", e)
        }
    }

    // Create a shared closed question with possible answers
    override fun createSharedClosedQuestion(request: ClosedQuestionRequest): ClosedQuestion {
        if (request.possibleAnswers.isEmpty()) {
            throw IllegalArgumentException("Closed question must have at least one possible answer")
        }

        val question = ClosedQuestion(
            text = request.text,
            description = request.description,
            required = request.required,
            isShared = true,
            selectionType = request.selectionType
        )

        request.possibleAnswers.forEachIndexed { index, answerText ->
            if (answerText.isBlank()) {
                throw IllegalArgumentException("Answer text cannot be blank at position $index")
            }
            val answer = QuestionAnswer(
                text = answerText,
                displayOrder = index + 1
            )
            question.addPossibleAnswer(answer)
        }

        return try {
            closedQuestionRepository.save(question)
        } catch (e: DataAccessException) {
            logger.error("Failed to save shared closed question: ${request.text}", e)
            throw IllegalStateException("Failed to create shared closed question", e)
        }
    }

    override fun updateQuestion(id: Long, request: QuestionRequest): Question {
        val existingQuestion = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        when (request) {
            is OpenQuestionRequest -> {
                if (existingQuestion !is OpenQuestion) {
                    throw IllegalArgumentException("Cannot change question type from ${existingQuestion::class.simpleName} to OpenQuestion")
                }
                existingQuestion.text = request.text
                existingQuestion.description = request.description
                existingQuestion.required = request.required
            }
            is ClosedQuestionRequest -> {
                if (existingQuestion !is ClosedQuestion) {
                    throw IllegalArgumentException("Cannot change question type from ${existingQuestion::class.simpleName} to ClosedQuestion")
                }

                if (request.possibleAnswers.isEmpty()) {
                    throw IllegalArgumentException("Closed question must have at least one possible answer")
                }

                existingQuestion.text = request.text
                existingQuestion.description = request.description
                existingQuestion.required = request.required
                existingQuestion.selectionType = request.selectionType

                // Update possible answers
                existingQuestion.possibleAnswers.clear()
                request.possibleAnswers.forEachIndexed { index, answerText ->
                    if (answerText.isBlank()) {
                        throw IllegalArgumentException("Answer text cannot be blank at position $index")
                    }
                    val answer = QuestionAnswer(
                        text = answerText,
                        displayOrder = index + 1
                    )
                    existingQuestion.addPossibleAnswer(answer)
                }
            }
        }

        return try {
            questionRepository.save(existingQuestion)
        } catch (e: DataAccessException) {
            logger.error("Failed to update question $id", e)
            throw IllegalStateException("Failed to update question", e)
        }
        catch (e: DataIntegrityViolationException) {
            logger.error("Cannot update question with user responses present $id", e)
            throw IllegalStateException("Failed to update question, it has existing user responses", e)
        }

    }

    override fun duplicateQuestion(id: Long): Question {
        val originalQuestion = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        val duplicatedQuestion = originalQuestion.copy()
        duplicatedQuestion.text = "${duplicatedQuestion.text} (Copy)"
        duplicatedQuestion.survey = null // Make it shared by default
        duplicatedQuestion.isShared = true

        return try {
            questionRepository.save(duplicatedQuestion)
        } catch (e: DataAccessException) {
            logger.error("Failed to duplicate question $id", e)
            throw IllegalStateException("Failed to create question duplicate", e)
        }
    }

    override fun deleteQuestion(id: Long) {
        val question = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        // Check if question is being used in any surveys
        if (question.survey != null) {
            throw InvalidOperationException("Cannot delete question that is part of a survey")
        }

        try {
            questionRepository.deleteById(id)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation while deleting question $id", e)
            throw InvalidOperationException("Cannot delete question due to existing references")
        } catch (e: DataAccessException) {
            logger.error("Failed to delete question $id", e)
            throw IllegalStateException("Failed to delete question", e)
        }
    }

    // Copy a shared question to a survey
    override fun copyQuestionToSurvey(questionId: Long, surveyId: Long, displayOrder: Int): Question {
        val originalQuestion = questionRepository.findById(questionId).orElseThrow {
            EntityNotFoundException("Question not found with id: $questionId")
        }

        if (!originalQuestion.isShared) {
            throw IllegalArgumentException("Only shared questions can be copied to surveys")
        }

        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        // Create a copy of the question
        val copiedQuestion = originalQuestion.copy()
        copiedQuestion.displayOrder = displayOrder

        // Add to survey
        survey.addQuestion(copiedQuestion)

        try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to copy question $questionId to survey $surveyId", e)
            throw IllegalStateException("Failed to add question to survey", e)
        }

        return copiedQuestion
    }

    override fun getQuestionCounts(): QuestionCountsResponse {
        try {
            val sharedQuestions = questionRepository.countSharedQuestions()
            val openQuestions = openQuestionRepository.countAllOpenQuestions()
            val closedQuestions = closedQuestionRepository.countAllClosedQuestions()
            val checkboxQuestions = closedQuestionRepository.countBySelectionType(SelectionType.MULTIPLE)
            val dropdownQuestions = closedQuestionRepository.countBySelectionType(SelectionType.SINGLE)

            return QuestionCountsResponse(
                sharedQuestions = sharedQuestions,
                openQuestions = openQuestions,
                closedQuestions = closedQuestions,
                checkboxQuestions = checkboxQuestions,
                dropdownQuestions = dropdownQuestions
            )
        } catch (e: DataAccessException) {
            logger.error("Failed to get question counts", e)
            throw IllegalStateException("Failed to retrieve question counts", e)
        }
    }

    // Find all shared questions available for copying
    override fun findSharedQuestions(): List<Question> {
        val questions = try {
            questionRepository.findShared()
        } catch (e: DataAccessException) {
            logger.error("Failed to load shared questions", e)
            throw IllegalStateException("Failed to retrieve shared questions", e)
        }

        try {
            questions.forEach { question ->
                if (question is ClosedQuestion) {
                    Hibernate.initialize(question.possibleAnswers)
                }
            }
        } catch (e: LazyInitializationException) {
            logger.error("Failed to initialize question data", e)
            throw IllegalStateException("Failed to load complete question data", e)
        }

        return questions
    }



    override fun findClosedSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            closedQuestionRepository.findSharedPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load closed shared questions page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve closed shared questions", e)
        }

        return questionMapper.toPageResponse(page)
    }

    override fun findSharedCheckboxQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            closedQuestionRepository.findSharedPageByType(SelectionType.MULTIPLE, pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load checkbox shared questions page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve checkbox shared questions", e)
        }

        return questionMapper.toPageResponse(page)
    }

    override fun findSharedDropdownQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            closedQuestionRepository.findSharedPageByType(SelectionType.SINGLE, pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load dropdown shared questions page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve dropdown shared questions", e)
        }

        return questionMapper.toPageResponse(page)
    }

    override fun findOpenSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            openQuestionRepository.findSharedPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load open shared questions page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve open shared questions", e)
        }

        return questionMapper.toPageResponse(page)
    }

    override fun findSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse> {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = try {
            questionRepository.findSharedPage(pageable)
        } catch (e: DataAccessException) {
            logger.error("Failed to load shared questions page: page=$pageNumber, size=$pageSize", e)
            throw IllegalStateException("Failed to retrieve shared questions", e)
        }

        return questionMapper.toPageResponse(page)
    }


    // Create survey-specific question directly (not shared)
    override fun createSurveySpecificOpenQuestion(
        surveyId: Long,
        text: String,
        description: String?,
        required: Boolean,
        displayOrder: Int
    ): OpenQuestion {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        val question = OpenQuestion(
            text = text,
            description = description,
            required = required,
            displayOrder = displayOrder,
            isShared = false
        )

        survey.addQuestion(question)

        try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to create survey-specific open question for survey $surveyId", e)
            throw IllegalStateException("Failed to create question", e)
        }

        return question
    }

    // Create survey-specific closed question directly (not shared)
    override fun createSurveySpecificClosedQuestion(
        surveyId: Long,
        text: String,
        description: String?,
        required: Boolean,
        displayOrder: Int,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion {
        if (possibleAnswers.isEmpty()) {
            throw IllegalArgumentException("Closed question must have at least one possible answer")
        }

        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        val question = ClosedQuestion(
            text = text,
            description = description,
            required = required,
            displayOrder = displayOrder,
            isShared = false,
            selectionType = selectionType
        )

        possibleAnswers.forEachIndexed { index, answerText ->
            if (answerText.isBlank()) {
                throw IllegalArgumentException("Answer text cannot be blank at position $index")
            }
            val answer = QuestionAnswer(
                text = answerText,
                displayOrder = index + 1
            )
            question.addPossibleAnswer(answer)
        }

        survey.addQuestion(question)

        try {
            surveyRepository.save(survey)
        } catch (e: DataAccessException) {
            logger.error("Failed to create survey-specific closed question for survey $surveyId", e)
            throw IllegalStateException("Failed to create question", e)
        }

        return question
    }
}