package tds.exam.error;

public class ValidationErrorCode {
    //Open Exam Error codes
    public static final String MAX_OPPORTUNITY_EXCEEDED = "maxOpportunityPassed";
    public static final String NOT_ENOUGH_DAYS_PASSED = "notEnoughDaysPassed";
    public static final String SIMULATION_ENVIRONMENT_REQUIRED = "simulationEnvironmentRequired";
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
}
