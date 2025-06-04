package com.darknessopirate.appvsurveyapi.infrastructure.mappers

import com.darknessopirate.appvsurveyapi.api.dto.response.accessCode.AccessCodeResponse
import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode
import org.springframework.stereotype.Component

@Component
class AccessCodeMapper {

    fun toResponse(entity: AccessCode): AccessCodeResponse = AccessCodeResponse(
        id = entity.id!!,
        code = entity.code,
        title = entity.title,
        description = entity.description,
        createdAt = entity.createdAt,
        expiresAt = entity.expiresAt,
        isActive = entity.isActive,
        usageCount = entity.usageCount,
        maxUses = entity.maxUses
    )
}