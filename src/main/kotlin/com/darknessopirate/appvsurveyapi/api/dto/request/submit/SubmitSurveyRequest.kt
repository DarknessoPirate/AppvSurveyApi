package com.darknessopirate.appvsurveyapi.api.dto.request.submit

data class SubmitSurveyRequest(
    val answers: List<AnswerRequest>
)