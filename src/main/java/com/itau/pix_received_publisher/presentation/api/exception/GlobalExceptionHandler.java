package com.itau.pix_received_publisher.presentation.api.exception;

import com.itau.pix_received_publisher.core.application.exceptions.ValidationException;
import com.itau.pix_received_publisher.presentation.api.presenters.ErrorResponsePresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponsePresenter> buildErrorResponse(Exception ex, HttpStatus status) {
        ErrorResponsePresenter errorResponsePresenter = new ErrorResponsePresenter(status.getReasonPhrase(), ex.getMessage());
        return new ResponseEntity<>(errorResponsePresenter, status);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponsePresenter> handleValidationException(ValidationException exception) {
        log.warn("Evento rejeitado por validação: {}", exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponsePresenter> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.warn("Argumento inválido: {}", exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponsePresenter> handleGlobalException(Exception exception) {
        log.error("Erro ao processar requisição", exception);
        return buildErrorResponse(new RuntimeException("An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
