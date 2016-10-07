package tds.exam.error;

public class ValidationErrorCode {
    public static final String MAX_OPPORTUNITY_EXCEEDED = "maxOpportunityPassed";
    public static final String NOT_ENOUGH_DAYS_PASSED = "notEnoughDaysPassed";
    public static final String SIMULATION_ENVIRONMENT_REQUIRED = "simulationEnvironmentRequired";
    public static final String SESSION_TYPE_MISMATCH = "sessionTypeMismatch";
    public static final String CURRENT_EXAM_OPEN = "examAlreadyOpen";

    // Exam approval validation error codes
    public static final String EXAM_APPROVAL_BROWSER_ID_MISMATCH = "browserIdMismatch";
    public static final String EXAM_APPROVAL_SESSION_ID_MISMATCH = "sessionIdMismatch";
    public static final String EXAM_APPROVAL_SESSION_CLOSED = "sessionClosed";
    public static final String EXAM_APPROVAL_TA_CHECKIN_TIMEOUT = "TACheckin TIMEOUT";
}
