package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.api.dto.PaginatedResponse
import com.darknessopirate.appvsurveyapi.api.dto.request.question.ClosedQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.OpenQuestionRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.question.QuestionRequest
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

    // Create a shared question (manually created, available for copying)
    override fun createSharedOpenQuestion(request: OpenQuestionRequest): OpenQuestion {
        val question = OpenQuestion(
            text = request.text,
            description = request.description,
            required = request.required,
            isShared = true
        )
        return openQuestionRepository.save(question)
    }

    // Create a shared closed question with possible answers
    override fun createSharedClosedQuestion(request: ClosedQuestionRequest): ClosedQuestion {
        val question = ClosedQuestion(
            text = request.text,
            description = request.description,
            required = request.required,
            isShared = true,
            selectionType = request.selectionType
        )

        request.possibleAnswers.forEachIndexed { index, answerText ->
            val answer = QuestionAnswer(
                text = answerText,
                displayOrder = index + 1
            )
            question.addPossibleAnswer(answer)
        }

        return closedQuestionRepository.save(question)
    }

    override fun updateQuestion(id: Long, request: QuestionRequest): Question {
        val existingQuestion = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        when (request) {
            is OpenQuestionRequest -> {
                if (existingQuestion !is OpenQuestion) {
                    throw IllegalArgumentException("Cannot change question type")
                }
                existingQuestion.text = request.text
                existingQuestion.description = request.description
                existingQuestion.required = request.required
            }
            is ClosedQuestionRequest -> {
                if (existingQuestion !is ClosedQuestion) {
                    throw IllegalArgumentException("Cannot change question type")
                }
                existingQuestion.text = request.text
                existingQuestion.description = request.description
                existingQuestion.required = request.required
                existingQuestion.selectionType = request.selectionType

                // Update possible answers
                existingQuestion.possibleAnswers.clear()
                request.possibleAnswers.forEachIndexed { index, answerText ->
                    val answer = QuestionAnswer(
                        text = answerText,
                        displayOrder = index + 1
                    )
                    existingQuestion.addPossibleAnswer(answer)
                }
            }
        }

        return questionRepository.save(existingQuestion)
    }

    override fun duplicateQuestion(id: Long): Question {
        val originalQuestion = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        val duplicatedQuestion = originalQuestion.copy()
        duplicatedQuestion.text = "${duplicatedQuestion.text} (Copy)"
        duplicatedQuestion.survey = null // Make it shared by default
        duplicatedQuestion.isShared = true

        return questionRepository.save(duplicatedQuestion)
    }

    override fun deleteQuestion(id: Long) {
        val question = questionRepository.findById(id).orElseThrow {
            EntityNotFoundException("Question not found with id: $id")
        }

        // Check if question is being used in any surveys
        if (question.survey != null) {
            throw InvalidOperationException("Cannot delete question that is part of a survey")
        }

        questionRepository.deleteById(id)
    }

    // Copy a shared question to a survey
    override fun copyQuestionToSurvey(questionId: Long, surveyId: Long, displayOrder: Int): Question {
        val originalQuestion = questionRepository.findById(questionId).orElseThrow {
            EntityNotFoundException("Question not found with id: $questionId")
        }

        require(originalQuestion.isShared) {
            "Only shared questions can be copied to surveys"
        }

        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        // Create a copy of the question
        val copiedQuestion = originalQuestion.copy()
        copiedQuestion.displayOrder = displayOrder

        // Add to survey
        survey.addQuestion(copiedQuestion)
        surveyRepository.save(survey)

        return copiedQuestion
    }

    // Find all shared questions available for copying
    override fun findSharedQuestions(): List<Question> {
        val questions = questionRepository.findShared()
        questions.forEach { question ->
            if (question is ClosedQuestion) {
                Hibernate.initialize(question.possibleAnswers)
            }
        }

        return questions
    }

    override fun findClosedSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse>
    {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = closedQuestionRepository.findSharedPage(pageable)
        return questionMapper.toPageResponse(page)
    }

    override fun findOpenSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse>
    {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = openQuestionRepository.findSharedPage(pageable)
        return questionMapper.toPageResponse(page)
    }

    override fun findSharedQuestionsPage(pageNumber: Int, pageSize: Int, sortFromOldest: Boolean): PaginatedResponse<QuestionResponse>
    {
        val sortDirection = if (sortFromOldest) {
            Sort.by("id").ascending()
        } else {
            Sort.by("id").descending()
        }

        val pageable = PageRequest.of(pageNumber, pageSize, sortDirection)

        val page = questionRepository.findSharedPage(pageable)
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
        surveyRepository.save(survey)

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
            val answer = QuestionAnswer(
                text = answerText,
                displayOrder = index + 1
            )
            question.addPossibleAnswer(answer)
        }

        survey.addQuestion(question)
        surveyRepository.save(survey)

        return question
    }
}