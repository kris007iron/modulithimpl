package com.kris007iron.modulithimpl.inventory.internal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryExceptionHandler {
    @ExceptionHandler(InsufficientStockException.class)
    public ErrorResponse handle(InsufficientStockException exception) {
        return ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build();
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ErrorResponse handle(StockNotFoundException exception) {
        return ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage()).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handle(IllegalArgumentException exception) {
        return ErrorResponse.builder(
                exception,
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        ).build();
    }
}
