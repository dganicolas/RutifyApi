package com.rutify.rutifyApi.exception.exceptions

class FirebaseUnavailableException (message: String)
    : Exception("Bad Gateway (502). $message")