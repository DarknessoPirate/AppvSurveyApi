package com.darknessopirate.appvsurveyapi.infrastructure.service
import com.darknessopirate.appvsurveyapi.domain.enums.SelectionType
import com.darknessopirate.appvsurveyapi.domain.entity.question.ClosedQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.OpenQuestion
import com.darknessopirate.appvsurveyapi.domain.entity.question.Question
import com.darknessopirate.appvsurveyapi.domain.entity.question.QuestionAnswer
import com.darknessopirate.appvsurveyapi.domain.repository.question.ClosedQuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.question.OpenQuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.question.QuestionRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IQuestionService
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class QuestionServiceImpl(
    private val questionRepository: QuestionRepository,
    private val openQuestionRepository: OpenQuestionRepository,
    private val closedQuestionRepository: ClosedQuestionRepository,
    private val surveyRepository: SurveyRepository
) : IQuestionService {

    /**
     * Create a shared question (manually created, available for copying)
     */
    override fun createSharedOpenQuestion(text: String, required: Boolean): OpenQuestion {
        val question = OpenQuestion(
            text = text,
            required = required,
            isShared = true
        )
        return openQuestionRepository.save(question)
    }

    /**
     * Create a shared closed question with possible answers
     */
    override fun createSharedClosedQuestion(
        text: String,
        required: Boolean,
        selectionType: SelectionType,
        possibleAnswers: List<String>
    ): ClosedQuestion {
        val question = ClosedQuestion(
            text = text,
            required = required,
            isShared = true,
            selectionType = selectionType
        )

        possibleAnswers.forEachIndexed { index, answerText ->
            val answer = QuestionAnswer(
                text = answerText,
                displayOrder = index + 1
            )
            question.addPossibleAnswer(answer)
        }

        return closedQuestionRepository.save(question)
    }

    /**
     * Copy a shared question to a survey
     */
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

    /**
     * Find all shared questions available for copying
     */
    override fun findSharedQuestions(): List<Question> {
        return questionRepository.findShared()
    }

    /**
     * Find shared questions by search text
     */
    override fun searchSharedQuestions(searchText: String): List<Question> {
        return questionRepository.searchShared(searchText)
    }

    /**
     * Find shared closed questions with their answers
     */
    override fun findSharedClosedQuestionsWithAnswers(): List<ClosedQuestion> {
        return closedQuestionRepository.findSharedWithAnswers()
    }

    /**
     * Create survey-specific question directly (not shared)
     */
    override fun createSurveySpecificOpenQuestion(
        surveyId: Long,
        text: String,
        required: Boolean,
        displayOrder: Int
    ): OpenQuestion {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found with id: $surveyId")
        }

        val question = OpenQuestion(
            text = text,
            required = required,
            displayOrder = displayOrder,
            isShared = false
        )

        survey.addQuestion(question)
        surveyRepository.save(survey)

        return question
    }

    /**
     * Create survey-specific closed question directly (not shared)
     */
    override fun createSurveySpecificClosedQuestion(
        surveyId: Long,
        text: String,
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

     /*
         Convert an existing survey question to a shared question
     */
     override fun makeQuestionShared(questionId: Long): Question {
        val question = questionRepository.findById(questionId).orElseThrow {
            EntityNotFoundException("Question not found with id: $questionId")
        }

        require(!question.isShared) {
            "Question is already shared"
        }

        // Create a copy and mark it as shared
        val sharedQuestion = question.copy()
        sharedQuestion.isShared = true
        sharedQuestion.survey = null // Shared questions don't belong to any survey

        return questionRepository.save(sharedQuestion)
    }
}