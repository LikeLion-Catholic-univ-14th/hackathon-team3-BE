package com.example.hackathon_team3_be.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> handleConflict(Exception exception, HttpServletRequest request) {
        String message = exception instanceof ConflictException
                ? exception.getMessage()
                : "이미 예약된 시간입니다. 다른 시간을 선택해 주세요.";
        return response(HttpStatus.CONFLICT, message, request, Map.of());
    }

    @ExceptionHandler(InvalidStateException.class)
    ResponseEntity<ApiError> handleInvalidState(InvalidStateException exception, HttpServletRequest request) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "요청값을 확인해 주세요.", request, fields);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ApiError> handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "요청 형식 또는 필수 파라미터를 확인해 주세요.", request, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiError> handleUploadTooLarge(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "음성 파일은 15MB 이하로 업로드해 주세요.", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다.", request, Map.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> fields
    ) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), fields
        ));
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fields
    ) {
    }
}
