package com.rutify.rutifyApi.exception.exceptions

class ValidationException(message: String)
    : Exception("Error en la validacion (400). $message")