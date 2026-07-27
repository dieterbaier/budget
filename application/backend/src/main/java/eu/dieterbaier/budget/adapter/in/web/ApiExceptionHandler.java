package eu.dieterbaier.budget.adapter.in.web;

import eu.dieterbaier.budget.application.port.in.DuplicateNameException;
import eu.dieterbaier.budget.application.port.in.NameInUseException;
import eu.dieterbaier.budget.application.port.in.UnknownCategoryException;
import eu.dieterbaier.budget.application.port.in.UnknownNameException;
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

    @ExceptionHandler(UnknownNameException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUnknownName(UnknownNameException exception) {
        return Map.of("error", exception.getMessage());
    }

    // 409 rather than 400: the request is well formed, it conflicts with data
    // that already exists. The owner's fix is to choose another name, not to
    // correct the request.
    @ExceptionHandler(DuplicateNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateName(DuplicateNameException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(NameInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleNameInUse(NameInUseException exception) {
        return Map.of("error", exception.getMessage());
    }
}
