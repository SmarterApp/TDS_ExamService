package tds.exam.web.exceptions;

import tds.common.ValidationError;

public class ValidationException extends Exception {
    private String code;
    private String message;

    /**
     * @param validationError   ValidationError to base this exception on
     */
    public ValidationException(ValidationError validationError) {
        this(validationError.getCode(), validationError.getMessage());
    }

    /**
     * @param code              error code for the error type
     * @param message           error message to describe why error occurred
     */
    public ValidationException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @return error code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return description why error occurred
     */
    public String getMessage() {
        return message;
    }
}
