package com.miguel_mejia.fincostos_backend.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException exception, HttpServletRequest request) {
        return error(exception.getStatusCode(), exception.getReason(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + validationMessage(error.getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String detail = "El parámetro '" + exception.getName() + "' tiene un formato inválido";
        if (exception.getRequiredType() != null
                && exception.getRequiredType().equals(java.time.LocalDate.class)) {
            detail += ". Use el formato AAAA-MM-DD";
        }
        return error(HttpStatus.BAD_REQUEST, "No se pudo interpretar un parámetro", request, List.of(detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es válido. Revise los tipos y el formato de los datos",
                request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + exception.getParameterName() + "'",
                request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(
            AuthenticationException exception, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "El usuario o la contraseña son incorrectos", request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", request, details);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ApiError> handleDateTime(
            DateTimeException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "La fecha o el período indicado no es válido", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente nuevamente más tarde", request, List.of());
    }

    private ResponseEntity<ApiError> error(
            org.springframework.http.HttpStatusCode status,
            String message,
            HttpServletRequest request,
            List<String> details) {
        ApiError body = new ApiError(
                LocalDateTime.now(), status.value(), message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }

    private static String validationMessage(String message) {
        return switch (message) {
            case "must not be blank", "must not be null" -> "es obligatorio";
            case "must be greater than or equal to 0.00" -> "debe ser mayor o igual a cero";
            case "must be greater than or equal to 1" -> "debe ser mayor o igual a uno";
            default -> message;
        };
    }
}