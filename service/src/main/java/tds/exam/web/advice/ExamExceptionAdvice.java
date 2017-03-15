package tds.exam.web.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import tds.common.web.advice.ExceptionAdvice;
import tds.common.web.resources.ExceptionMessageResource;
import tds.exam.web.exceptions.ValidationException;

/**
 * Adds ExamService specific handling by extending the Common exception handling
 */
@ControllerAdvice
public class ExamExceptionAdvice extends ExceptionAdvice {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleValidationException(final ValidationException ex) {
        return new ResponseEntity<>(
            new ExceptionMessageResource(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
