package org.ac.cst8277.baimakov.dmitriy.exception;

import org.ac.cst8277.baimakov.dmitriy.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<List<Object>>> handle(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus().value())
                .body(ApiResponse.error(
                        String.valueOf(ex.getStatus().value()),
                        List.of(),
                        ex.getReason()
                ));
    }
}
