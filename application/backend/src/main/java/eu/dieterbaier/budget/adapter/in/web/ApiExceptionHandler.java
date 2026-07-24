package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.service.UnknownCategoryException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Maps application exceptions to HTTP responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnknownCategoryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnknownCategory(UnknownCategoryException exception) {
        return Map.of("error", exception.getMessage());
    }
}
