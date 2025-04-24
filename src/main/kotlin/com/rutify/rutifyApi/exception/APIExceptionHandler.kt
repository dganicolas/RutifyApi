package com.rutify.rutifyApi.exception

import com.rutify.rutifyApi.domain.Log
import com.rutify.rutifyApi.exception.exceptions.InternalServerErrorException
import com.rutify.rutifyApi.exception.exceptions.NotFoundException
import com.rutify.rutifyApi.exception.exceptions.UnauthorizedException
import com.rutify.rutifyApi.exception.exceptions.ValidationException
import com.rutify.rutifyApi.utils.LogUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class APIExceptionHandler {


    @ExceptionHandler(
        IllegalArgumentException::class
        , NumberFormatException::class
        , ValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleBadRequest(request: HttpServletRequest, e: Exception) : ErrorRespuesta {
        LogUtils.writeLog(Log(request.method, request.requestURI, false, HttpStatus.BAD_REQUEST))
        return ErrorRespuesta(e.message!!, request.requestURI)
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun handleUnauthorized(request: HttpServletRequest, e: UnauthorizedException): ErrorRespuesta {
        LogUtils.writeLog(Log(request.method, request.requestURI, false, HttpStatus.UNAUTHORIZED))
        return ErrorRespuesta(e.message ?: "Unauthorized", request.requestURI)
    }


    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(request: HttpServletRequest, e: Exception) : ErrorRespuesta {
        LogUtils.writeLog(Log(request.method, request.requestURI, false, HttpStatus.NOT_FOUND))
        return ErrorRespuesta(e.message!!, request.requestURI)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleInternalServerError(request: HttpServletRequest, e: Exception) : ErrorRespuesta {
        LogUtils.writeLog(Log(request.method, request.requestURI, false, HttpStatus.NOT_FOUND))
        return ErrorRespuesta(e.message!!, request.requestURI)
    }

    @ExceptionHandler(Exception::class, NullPointerException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun handleGeneric(request: HttpServletRequest, e: Exception) : ErrorRespuesta {
        LogUtils.writeLog(Log(request.method, request.requestURI, false, HttpStatus.INTERNAL_SERVER_ERROR))
        return ErrorRespuesta(e.message!!, request.requestURI)
    }


}