package com.example.bug_tracker.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.bug_tracker.bug.exception.BugNotFoundException;

import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 統一エラー形式（code/message/details）
    public record ApiError(String code, String message, List<FieldDetail> details) {
    }

    public record FieldDetail(String field, String reason) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleValidation(MethodArgumentNotValidException ex) {
        List<FieldDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();

        return new ApiError(
                "VALIDATION_ERROR",
                "Request validation failed",
                details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 不正なJSON 400
    public ApiError handleNotReadable(HttpMessageNotReadableException ex) {
        return new ApiError(
                "INVALID_JSON",
                "Request body is invalid (malformed JSON or invalid value)",
                List.of());
    }

    @ExceptionHandler(BugNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ApiError handleNotFound(BugNotFoundException ex) {
        return new ApiError(
                "NOT_FOUND",
                "Id is Not Found",
                List.of());
    }

    private FieldDetail toDetail(FieldError e) {
        return new FieldDetail(e.getField(), e.getDefaultMessage());
    }
}