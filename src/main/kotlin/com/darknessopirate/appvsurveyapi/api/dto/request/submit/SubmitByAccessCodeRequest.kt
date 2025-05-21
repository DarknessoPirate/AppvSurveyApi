package com.darknessopirate.appvsurveyapi.api.dto.request.submit

data class SubmitByAccessCodeRequest(
    val accessCode: String,
    val answers: List<AnswerRequest>
)