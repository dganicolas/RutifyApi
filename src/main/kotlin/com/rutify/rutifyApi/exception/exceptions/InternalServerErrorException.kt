package com.rutify.rutifyApi.exception.exceptions

class InternalServerErrorException (message: String)
    : Exception("Internal Server Error (500). $message")