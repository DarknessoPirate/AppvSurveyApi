package com.darknessopirate.appvsurveyapi.infrastructure.service

import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.CreateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.UpdateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode
import com.darknessopirate.appvsurveyapi.domain.exception.AccessCodeGenerationException
import com.darknessopirate.appvsurveyapi.domain.repository.survey.AccessCodeRepository
import com.darknessopirate.appvsurveyapi.domain.repository.survey.SurveyRepository
import com.darknessopirate.appvsurveyapi.domain.service.IAccessCodeService
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional // todo: does it really need transactional?
class AccessCodeServiceImpl(
    private val accessCodeRepository: AccessCodeRepository,
    private val surveyRepository: SurveyRepository
) : IAccessCodeService {

    override fun createAccessCode(surveyId: Long, request: CreateAccessCodeRequest): AccessCode {
        val survey = surveyRepository.findById(surveyId).orElseThrow {
            EntityNotFoundException("Survey not found: $surveyId")
        }

        val code = request.code ?: generateRandomCode()

        val accessCode = AccessCode(
            code = code,
            title = request.title,
            description = request.description,
            expiresAt = request.expiresAt,
            maxUses = request.maxUses
        )

        survey.addAccessCode(accessCode)
        return accessCodeRepository.save(accessCode)
    }

    override fun updateAccessCode(codeId: Long, request: UpdateAccessCodeRequest): AccessCode {
        val accessCode = accessCodeRepository.findById(codeId).orElseThrow {
            EntityNotFoundException("Access code not found: $codeId")
        }

        accessCode.title = request.title
        if(request.code != null ) {
            accessCode.code = request.code
        }
        accessCode.description = request.description
        accessCode.isActive = request.isActive
        accessCode.expiresAt = request.expiresAt
        accessCode.maxUses = request.maxUses

        return accessCodeRepository.save(accessCode)
    }

    override fun deleteAccessCode(codeId: Long) {
        if (!accessCodeRepository.existsById(codeId)) {
            throw EntityNotFoundException("Access code not found: $codeId")
        }
        accessCodeRepository.deleteById(codeId)
    }

    override fun getAccessCodesBySurvey(surveyId: Long): List<AccessCode> {
        return accessCodeRepository.findBySurveyId(surveyId)
    }

    override fun validateAccessCode(code: String): AccessCode? {
        return accessCodeRepository.findValidAccessCode(code, LocalDateTime.now())
    }

    override fun incrementUsage(codeId: Long): AccessCode {
        val accessCode = accessCodeRepository.findById(codeId).orElseThrow {
            EntityNotFoundException("Access code not found: $codeId")
        }

        accessCode.usageCount++
        return accessCodeRepository.save(accessCode)
    }

    private fun generateRandomCode(): String {
        return UUID.randomUUID().toString().substring(0, 16)
    }
}