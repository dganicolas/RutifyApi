package com.rutify.rutifyApi.exception.exceptions

class UnauthorizedException (message: String)
    : Exception("Unauthorized (401). $message")