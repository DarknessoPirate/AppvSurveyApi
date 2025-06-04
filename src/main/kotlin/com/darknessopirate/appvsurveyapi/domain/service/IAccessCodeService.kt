package com.darknessopirate.appvsurveyapi.domain.service

import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.CreateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.api.dto.request.accessCode.UpdateAccessCodeRequest
import com.darknessopirate.appvsurveyapi.domain.entity.survey.AccessCode

interface IAccessCodeService {
    fun createAccessCode(surveyId: Long, request: CreateAccessCodeRequest): AccessCode
    fun updateAccessCode(codeId: Long, request: UpdateAccessCodeRequest): AccessCode
    fun deleteAccessCode(codeId: Long)
    fun getAccessCodesBySurvey(surveyId: Long): List<AccessCode>
    fun validateAccessCode(code: String): AccessCode?
    fun incrementUsage(codeId: Long): AccessCode
    fun findByCode(code: String): AccessCode?
}