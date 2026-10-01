package com.kris007iron.modulithimpl.catalog.internal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CatalogExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ErrorResponse handle(ProductNotFoundException exception) {
        return ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ErrorResponse handle(ProductAlreadyExistsException exception) {
        return ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build();
    }
}
