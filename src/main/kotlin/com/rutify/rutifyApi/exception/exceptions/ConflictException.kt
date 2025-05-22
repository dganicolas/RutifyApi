package com.rutify.rutifyApi.exception.exceptions

class ConflictException(message: String)
    : Exception("Conflicto (409). $message")