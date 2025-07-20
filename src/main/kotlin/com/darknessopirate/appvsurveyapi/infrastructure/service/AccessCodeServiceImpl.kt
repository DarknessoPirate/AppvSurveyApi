package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.CreateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.UpdateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode
import com.darknessopirate.appvsurveyapi.domain.exception.AccessCodeGenerationException
import com.darknessopirate.appvsurveyapi.domain.repository.survey.AccessCodeRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AccessCodeServiceImpl(
    private val accessCodeRepository: AccessCodeRepository,
    private val surveyRepository: SurveyRepository
) : IAccessCodeService {

    private val logger = LoggerFactory.getLogger(AccessCodeServiceImpl::class.java)

    override fun createAccessCode(surveyId: Long, request: CreateAccessCodeRequest): AccessCode {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }

        val code = if (request.code != null) {
            // Validate that the provided code is unique
            if (accessCodeRepository.existsByCode(request.code)) {
                throw IllegalArgumentException("Access code '${request.code}' already exists")
            }
            request.code
        } else {
            generateUniqueRandomCode()
        }

        val accessCode = AccessCode(
            code = code,
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt,
            maxUses = request.maxUses
        )

        survey.addAccessCode(accessCode)

        return try {
            accessCodeRepository.save(accessCode)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation while saving access code for survey $surveyId", e)
            throw IllegalArgumentException("Access code could not be created due to constraint violation")
        } catch (e: DataAccessException) {
            logger.error("Database error while saving access code for survey $surveyId", e)
            throw IllegalStateException("Failed to create access code", e)
        }
    }

    override fun updateAccessCode(codeId: Long, request: UpdateAccessCodeRequest): AccessCode {
        val accessCode = accessCodeRepository.findById(codeId).orElseThrow {
            EntityNotFoundException("Access code not found: $codeId")
        }


        when {
            // If code is explicitly set to empty string, generate a new unique code
            request.code == "" -> {
                val newCode = generateUniqueRandomCode()
                accessCode.code = newCode
            }
            // If code is provided and different from current, validate uniqueness
            request.code != null && request.code != accessCode.code -> {
                if (accessCodeRepository.existsByCode(request.code)) {
                    throw IllegalArgumentException("Access code '${request.code}' already exists")
                }
                accessCode.code = request.code
            }

        }

        accessCode.title = request.title
        accessCode.description = request.description
        accessCode.isActive = request.isActive
        accessCode.expiresAt = request.expiresAt
        accessCode.maxUses = request.maxUses

        return try {
            accessCodeRepository.save(accessCode)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation while updating access code $codeId", e)
            throw IllegalArgumentException("Access code could not be updated due to constraint violation")
        } catch (e: DataAccessException) {
            logger.error("Database error while updating access code $codeId", e)
            throw IllegalStateException("Failed to update access code", e)
        }
    }

    override fun deleteAccessCode(codeId: Long) {
        if (!accessCodeRepository.existsById(codeId)) {
            throw EntityNotFoundException("Access code not found: $codeId")
        }

        try {
            accessCodeRepository.deleteById(codeId)
        } catch (e: DataIntegrityViolationException) {
            logger.error("Data integrity violation while deleting access code $codeId", e)
            throw IllegalArgumentException("Cannot delete access code due to existing references")
        } catch (e: DataAccessException) {
            logger.error("Database error while deleting access code $codeId", e)
            throw IllegalStateException("Failed to delete access code", e)
        }
    }

    override fun getAccessCodesBySurvey(surveyId: Long): List<AccessCode> {
        return try {
            accessCodeRepository.findBySurveyId(surveyId)
        } catch (e: DataAccessException) {
            logger.error("Database error while getting access codes for survey $surveyId", e)
            throw IllegalStateException("Failed to retrieve access codes", e)
        }
    }

    override fun validateAccessCode(code: String): AccessCode? {
        return try {
            accessCodeRepository.findValidAccessCode(code, LocalDateTime.now())
        } catch (e: DataAccessException) {
            logger.error("Database error while validating access code: $code", e)
            throw IllegalStateException("Access code validation service temporarily unavailable", e)
        }
    }

    override fun incrementUsage(codeId: Long): AccessCode {
        val accessCode = accessCodeRepository.findById(codeId).orElseThrow {
            EntityNotFoundException("Access code not found: $codeId")
        }

        // Check if the access code has reached its maximum uses
        if (accessCode.maxUses != null && accessCode.usageCount >= accessCode.maxUses!!) {
            throw IllegalStateException("Access code has reached maximum usage limit")
        }

        accessCode.usageCount++

        return try {
            accessCodeRepository.save(accessCode)
        } catch (e: DataAccessException) {
            logger.error("Database error while incrementing usage for access code $codeId", e)
            throw IllegalStateException("Failed to update access code usage", e)
        }
    }

    private fun generateUniqueRandomCode(): String {
        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            val code = generateRandomCode()

            val exists = try {
                accessCodeRepository.existsByCode(code)
            } catch (e: DataAccessException) {
                logger.error("Database error while checking code uniqueness", e)
                throw IllegalStateException("Failed to validate code uniqueness", e)
            }

            if (!exists) {
                return code
            }

            attempts++
        }

        logger.error("Failed to generate unique access code after $maxAttempts attempts")
        throw AccessCodeGenerationException("Failed to generate unique access code after $maxAttempts attempts")
    }

    private fun generateRandomCode(): String {
        return UUID.randomUUID().toString().substring(0, 16)
    }
}