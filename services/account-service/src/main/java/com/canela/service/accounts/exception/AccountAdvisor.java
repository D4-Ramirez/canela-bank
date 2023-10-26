package com.canela.service.accounts.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountAdvisor {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleNotFound(NotFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ResponseEntity<String> handleAlreadyExist(AlreadyExistException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error.getMessage());
    }
}
