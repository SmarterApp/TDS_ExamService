package tds.exam.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tds.exam.ExamStatusCode;


/**
 * Verifies that an {@link tds.exam.Exam} can transition from one status to another.
 */
class ExamStatusTransitionHandler {
    private static final List<String> PENDING_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_INITIALIZING, ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> SUSPENDED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_SUSPENDED, ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> STARTED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_STARTED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_REVIEW, ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_SEGMENT_ENTRY, ExamStatusCode.STATUS_SEGMENT_EXIT, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> APPROVED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_STARTED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> REVIEW_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_REVIEW, ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED, ExamStatusCode.STATUS_SEGMENT_ENTRY);
    private static final List<String> PAUSED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_SUSPENDED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> DENIED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_SUSPENDED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> COMPLETED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_SCORED, ExamStatusCode.STATUS_SUBMITTED, ExamStatusCode.STATUS_INVALIDATED);
    private static final List<String> SCORED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_RESCORED, ExamStatusCode.STATUS_SUBMITTED, ExamStatusCode.STATUS_INVALIDATED);
    private static final List<String> SUBMITTED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_RESCORED, ExamStatusCode.STATUS_REPORTED, ExamStatusCode.STATUS_INVALIDATED);
    private static final List<String> RESCORED_TRANSITIONS = Collections.singletonList(ExamStatusCode.STATUS_SCORED);

    // CommonDLL._IsValidStatusTransition_FN(), @ Line 619:  The transitions for ExamStatusCode.STATUS_REPORTED,
    // ExamStatusCode.STATUS_EXPIRED and ExamStatusCode.STATUS_INVALIDATED status are the same
    private static final List<String> REPORTED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_RESCORED, ExamStatusCode.STATUS_INVALIDATED);
    private static final List<String> EXPIRED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_RESCORED, ExamStatusCode.STATUS_INVALIDATED);
    private static final List<String> INVALIDATED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_RESCORED, ExamStatusCode.STATUS_INVALIDATED);

    // CommonDLL._IsValidStatusTransition_FN(), Line 671:  The transitions for ExamStatusCode.STATUS_SEGMENT_EXIT status
    // are the same as ExamStatusCode.STATUS_SEGMENT_ENTRY status
    private static final List<String> SEGMENT_ENTRY_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);
    private static final List<String> SEGMENT_EXIT_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);

    private static final List<String> FORCE_COMPLETED_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_SCORED);
    private static final List<String> INITIALIZING_TRANSITIONS = Arrays.asList(ExamStatusCode.STATUS_INITIALIZING, ExamStatusCode.STATUS_PENDING, ExamStatusCode.STATUS_DENIED, ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_PAUSED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_INVALIDATED, ExamStatusCode.STATUS_FORCE_COMPLETED);

    // Replace the large switch statement in CommonDLL._IsValidStatusTransition_FN(), line 474 with a map of every
    // status and every possible "downstream" status that can be transitioned to
    private static final Map<String, List<String>> STATUS_TRANSITION_MAP;
    static {
        Map<String, List<String>> map = new HashMap<>();
        map.put(ExamStatusCode.STATUS_PENDING, PENDING_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_SUSPENDED, SUSPENDED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_STARTED, STARTED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_APPROVED, APPROVED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_REVIEW, REVIEW_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_PAUSED, PAUSED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_DENIED, DENIED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_COMPLETED, COMPLETED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_SCORED, SCORED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_SUBMITTED, SUBMITTED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_RESCORED, RESCORED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_REPORTED, REPORTED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_EXPIRED, EXPIRED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_INVALIDATED, INVALIDATED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_SEGMENT_ENTRY, SEGMENT_ENTRY_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_SEGMENT_EXIT, SEGMENT_EXIT_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_FORCE_COMPLETED, FORCE_COMPLETED_TRANSITIONS);
        map.put(ExamStatusCode.STATUS_INITIALIZING, INITIALIZING_TRANSITIONS);

        STATUS_TRANSITION_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Determine if an {@link tds.exam.Exam} can transition from one status to another.
     *
     * @param currentStatus The current status of the exam
     * @param newStatus The status to which the exam should be set to
     * @return True if the current status allows a transition to the new status; otherwise false
     */
    static boolean canTransition(String currentStatus, String newStatus) {
        // ASSUMPTION:  All the status constants contained within tds.exam.ExamStatusCode are in lower-case.
        return STATUS_TRANSITION_MAP.containsKey(currentStatus.toLowerCase())
            && STATUS_TRANSITION_MAP.get(currentStatus.toLowerCase()).contains(newStatus.toLowerCase());
    }
}