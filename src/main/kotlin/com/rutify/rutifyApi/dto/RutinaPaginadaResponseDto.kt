package com.rutify.rutifyApi.dto

data class RutinaPaginadaResponseDto(
    val rutinas: List<RutinaBuscadorDto>,
    val totalItems: Long,
    val totalPages: Int,
    val currentPage: Int
)
