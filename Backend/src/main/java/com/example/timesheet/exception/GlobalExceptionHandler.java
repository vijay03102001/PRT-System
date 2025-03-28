package com.example.timesheet.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        // Return a 404 Not Found response with the exception message
        return ResponseEntity.status(404).body(new ErrorResponse("Resource Not Found", ex.getMessage()));
    }

    // Handle BusinessValidationException
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<?> handleBusinessValidation(BusinessValidationException ex) {
        // Return a 400 Bad Request response with the exception message
        return ResponseEntity.badRequest().body(new ErrorResponse("Business Validation Error", ex.getMessage()));
    }

    // Handle MethodArgumentNotValidException (Validation errors)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        // Return a 400 Bad Request response with a list of error messages
        return ResponseEntity.badRequest().body(new ErrorResponse("Validation Errors", errorMessages));
    }

    // Error response structure with Lombok annotations
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String errorType;
        private Object message;
    }
}
