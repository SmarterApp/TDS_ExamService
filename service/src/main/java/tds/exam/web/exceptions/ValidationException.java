/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.web.exceptions;

import tds.common.ValidationError;

/**
 * Exception used to throw a ValisdationError instead of passing it back in the response
 */
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
