package com.example.hackathon_team3_be.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateExperienceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateExperienceException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.CONFLICT.value(),
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(ExperienceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ExperienceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.NOT_FOUND.value(),
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(UnseenNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUnseenNotFound(UnseenNotFoundException e){return error(HttpStatus.NOT_FOUND,e.getMessage());}

    @ExceptionHandler(NoNegotiationMatchException.class)
    public ResponseEntity<Map<String,Object>> handleNoMatch(NoNegotiationMatchException e){return error(HttpStatus.UNPROCESSABLE_ENTITY,e.getMessage());}

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleAppointmentNotFound(AppointmentNotFoundException e){return error(HttpStatus.NOT_FOUND,e.getMessage());}

    @ExceptionHandler(AppointmentSlotUnavailableException.class)
    public ResponseEntity<Map<String,Object>> handleUnavailable(AppointmentSlotUnavailableException e){return error(HttpStatus.CONFLICT,e.getMessage());}

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String,Object>> handleBadRequest(Exception e){return error(HttpStatus.BAD_REQUEST,e.getMessage());}

    private ResponseEntity<Map<String,Object>> error(HttpStatus status,String message){return ResponseEntity.status(status).body(Map.of(
            "timestamp",Instant.now().toString(),"status",status.value(),"message",message));}
}
