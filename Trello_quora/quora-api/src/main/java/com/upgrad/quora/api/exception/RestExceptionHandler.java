package com.upgrad.quora.api.exception;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(SignUpRestrictedException.class)
    public ResponseEntity<ErrorResponse> signUpRestrictedException(SignUpRestrictedException exe, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> authenticationFailedException(AuthenticationFailedException exe, WebRequest request){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthorizationFailedException.class)
    public ResponseEntity<ErrorResponse> authorizationFailedException(AuthorizationFailedException exe, WebRequest request) {
        if (exe.getCode().compareTo("SGR-001") == 0) {
            return new ResponseEntity<ErrorResponse>(
                    new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.UNAUTHORIZED
            );
        }
        else if (exe.getCode().compareTo("ATHR-001" ) == 0 || exe.getCode().compareTo("ATHR-003" ) == 0 || exe.getCode().compareTo("ATHR-002" ) == 0 ) {
            return new ResponseEntity<ErrorResponse>(
                    new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.FORBIDDEN
            );
        }
        else {
            return new ResponseEntity<ErrorResponse>(
                    new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.NOT_FOUND
            );
        }}

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> resourceNotFoundException (UserNotFoundException unf, WebRequest webRequest)
        {
            return new ResponseEntity<ErrorResponse>(
                    new ErrorResponse().code(unf.getCode()).message(unf.getErrorMessage()), HttpStatus.NOT_FOUND);
        }

    @ExceptionHandler(InvalidQuestionException.class)
    public ResponseEntity<ErrorResponse> invalidQuestionException(
            InvalidQuestionException exception, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exception.getCode()).message(exception.getErrorMessage()),
                HttpStatus.NOT_FOUND);
    }
    }


