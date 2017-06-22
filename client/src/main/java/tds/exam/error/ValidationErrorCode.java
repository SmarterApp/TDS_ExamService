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

package tds.exam.error;

public class ValidationErrorCode {
    //Open Exam Error codes
    public static final String MAX_OPPORTUNITY_EXCEEDED = "maxOpportunityPassed";
    public static final String NOT_ENOUGH_DAYS_PASSED = "notEnoughDaysPassed";
    public static final String CURRENT_EXAM_OPEN = "examAlreadyOpen";
    public static final String PREVIOUS_SESSION_NOT_FOUND = "previousSessionNotFound";
    public static final String PREVIOUS_EXAM_NOT_CLOSED = "previousExamNotClosed";
    public static final String NO_OPEN_ASSESSMENT_WINDOW = "noOpenAssessmentWindow";
    public static final String ANONYMOUS_STUDENT_NOT_ALLOWED = "anonymousStudentNotAllowed";
    public static final String SESSION_NOT_OPEN = "sessionNotOpen";

    // Exam approval validation error codes
    public static final String EXAM_APPROVAL_BROWSER_ID_MISMATCH = "browserIdMismatch";
    public static final String EXAM_APPROVAL_SESSION_ID_MISMATCH = "sessionIdMismatch";
    public static final String EXAM_APPROVAL_SESSION_CLOSED = "sessionClosed";
    public static final String EXAM_APPROVAL_TA_CHECKIN_TIMEOUT = "TACheckin TIMEOUT";

    // Exam status transition error codes
    public static final String EXAM_STATUS_TRANSITION_FAILURE = "badStatusTransition";

    // Save responses error codes
    public static final String EXAM_INTERRUPTED = "examInterrupted";

    // Review Exam error codes
    public static final String EXAM_INCOMPLETE = "examIncomplete";

    // Exam acccommodations approval validation errors
    public static final String EXAM_DOES_NOT_EXIST = "examDoesNotExist";
    public static final String EXAM_NOT_ENROLLED_IN_SESSION = "examNotEnrolledInSession";
    public static final String STUDENT_SELF_APPROVE_UNPROCTORED_SESSION = "studentSelfApproveUnproctoredSession";

    // Exam Segments
    public static final String EXAM_SEGMENT_DOES_NOT_EXIST = "examSegmentDoesNotExist";

    // Exam Items/Responses
    public static final String EXAM_ITEM_DOES_NOT_EXIST = "examItemDoesNotExist";
    public static final String EXAM_ITEM_RESPONSE_DOES_NOT_EXIST = "examItemResponseDoesNotExist";
}
