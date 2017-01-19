package tds.exam.utils;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tds.exam.ExamStatusCode;

/**
 * A utility class for validating transitions between various exam states
 */
public class StatusTransitionValidator {
    // Build a map of current states -> valid next states
    private static final Map<String, Set<String>> stateMap = ImmutableMap.<String, Set<String>>builder()
        .put(ExamStatusCode.STATUS_PENDING, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_SUSPENDED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_STARTED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY,
            ExamStatusCode.STATUS_SEGMENT_EXIT,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_APPROVED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_REVIEW, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY,
            ExamStatusCode.STATUS_SEGMENT_EXIT
        )))
        .put(ExamStatusCode.STATUS_PAUSED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_DENIED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_COMPLETED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_SCORED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_SUBMITTED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_REPORTED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_REPORTED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_EXPIRED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_INVALIDATED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        )))
        .put(ExamStatusCode.STATUS_RESCORED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_SCORED
        )))
        .put(ExamStatusCode.STATUS_SEGMENT_ENTRY, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_SEGMENT_EXIT, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .put(ExamStatusCode.STATUS_FORCE_COMPLETED, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED
        )))
        .put(ExamStatusCode.STATUS_INITIALIZING, new HashSet<>(Arrays.asList(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        )))
        .build();

    /**
     * Validates that an exam can transition between the currentStatus and newStatus
     *
     * @param currentStatus the status the exam is currently in
     * @param newStatus     The status the exam is being transitioned to
     * @return true if this a valid transition - false otherwise
     */
    public static boolean isValidTransition(String currentStatus, String newStatus) {
        return stateMap.get(currentStatus) != null && stateMap.get(currentStatus).contains(newStatus);
    }
}
