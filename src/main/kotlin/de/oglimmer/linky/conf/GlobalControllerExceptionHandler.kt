package de.oglimmer.linky.conf

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class GlobalControllerExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleConflict(e: HttpMessageNotReadableException): ResponseEntity<ApiErrorResult> =
        responseEntity(e, HttpStatus.BAD_REQUEST)

    private fun responseEntity(e: HttpMessageNotReadableException, status: HttpStatus) =
        ResponseEntity.status(status).body(ApiErrorResult(status.value(), status.reasonPhrase, e.message))

}

data class ApiErrorResult(private val status: Int, private val statusPhrase: String, private val message: String?) {
}