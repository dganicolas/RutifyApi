package com.rutify.rutifyApi.exception.exceptions

class NotFoundException (message: String)
    : Exception("Not found exception (404). $message")