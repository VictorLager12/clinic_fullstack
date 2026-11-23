package kigali.clinic.rw.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidation(MethodArgumentNotValidException exception) {
        List<String> messages = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            messages.add(error.getDefaultMessage());
        }
        return new ResponseEntity<>(messages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<List<String>> handleUnreadable(HttpMessageNotReadableException exception) {
        List<String> messages = new ArrayList<>();
        messages.add("The request body is not valid. Check that numeric fields contain whole numbers.");
        return new ResponseEntity<>(messages, HttpStatus.BAD_REQUEST);
    }

}