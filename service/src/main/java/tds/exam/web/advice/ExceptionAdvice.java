package tds.exam.web.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.ExceptionMessageResource;

@ControllerAdvice
class ExceptionAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleNotFoundException(final NotFoundException ex) {
        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.NOT_FOUND.toString(), ex.getLocalizedMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleException(final Exception ex) {
        LOG.error("Unexpected error", ex);

        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleArgumentMismatchException(final MethodArgumentTypeMismatchException ex) {
        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.BAD_REQUEST.toString(), String.format("Invalid value: %s", ex.getName())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleIllegalStateException(final IllegalStateException ex) {
        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleInvalidParametersException(final IllegalArgumentException ex) {
        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.BAD_REQUEST.toString(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
