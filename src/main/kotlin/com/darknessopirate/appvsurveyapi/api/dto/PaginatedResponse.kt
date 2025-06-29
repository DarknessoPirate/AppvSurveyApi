package com.darknessopirate.appvsurveyapi.api.dto

data class PaginatedResponse<T>(
    val data: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val pageSize: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
)
