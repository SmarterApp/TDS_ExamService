package tds.exam.web.advice;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger LOG = LoggerFactory.getLogger(ExamExceptionAdvice.class);

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleValidationException(final ValidationException ex) {
        LOG.warn("Validation Exception", ex);
        return new ResponseEntity<>(
            new ExceptionMessageResource(ex.getCode(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReturnStatusException.class)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleReturnStatusException(final ReturnStatusException ex) {
        LOG.warn("Return Status Exception", ex);

        HttpStatus status = HttpStatus.valueOf(ex.getReturnStatus().getHttpStatusCode());
        // By default a ReturnStatusException has a response code of 200 (OK).
        // Since we should never return an error as a 200, convert 200 responses
        // to a default code of 500 (Internal Server Error).
        status = status == HttpStatus.OK ? HttpStatus.INTERNAL_SERVER_ERROR : status;

        return new ResponseEntity<>(
            new ExceptionMessageResource(ex.getReturnStatus().getStatus(), ex.getReturnStatus().getReason()),
            status);
    }
}
