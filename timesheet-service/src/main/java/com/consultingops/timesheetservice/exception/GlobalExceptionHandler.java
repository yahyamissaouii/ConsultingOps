package com.consultingops.timesheetservice.exception;

import com.consultingops.timesheetservice.dto.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler({BusinessRuleException.class, ExternalServiceException.class})
    public ResponseEntity<ApiError> handleBusiness(RuntimeException exception, HttpServletRequest request) {
        HttpStatus status = exception instanceof ExternalServiceException ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        String code = exception instanceof ExternalServiceException ? "UPSTREAM_ERROR" : "BUSINESS_RULE_VIOLATION";
        return build(status, code, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> handleValidation(Exception exception, HttpServletRequest request) {
        List<String> details = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream().map(this::formatFieldError).toList()
                : List.of(exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details, request);
    }

    @ExceptionHandler({UnauthorizedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> handleSecurity(Exception exception, HttpServletRequest request) {
        HttpStatus status = exception instanceof AccessDeniedException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        String code = exception instanceof AccessDeniedException ? "FORBIDDEN" : "UNAUTHORIZED";
        return build(status, code, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnhandled(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", exception.getMessage(), List.of(), request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status,
                                           String code,
                                           String message,
                                           List<String> details,
                                           HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                OffsetDateTime.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                details
        ));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
