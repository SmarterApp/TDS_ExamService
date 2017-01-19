package tds.exam.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tds.exam.ExamStatusCode;

/**
 * A utility class for validating transitions between various exam states
 */
public class StatusTransitionValidator {
    // Create private constructor to ensure this class is never instantiated
    private StatusTransitionValidator() {}

    // Build a map of current states -> valid next states
    /* This logic is contained in the legacy application in CommonDLL._IsValidStatusTransition_FN() */
    private static final Map<String, Set<String>> stateMap = ImmutableMap.<String, Set<String>>builder()
        .put(ExamStatusCode.STATUS_PENDING, Sets.newHashSet(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SUSPENDED, Sets.newHashSet(
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_STARTED, Sets.newHashSet(
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY,
            ExamStatusCode.STATUS_SEGMENT_EXIT,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_APPROVED, Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_REVIEW, Sets.newHashSet(
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY,
            ExamStatusCode.STATUS_SEGMENT_EXIT
        ))
        .put(ExamStatusCode.STATUS_PAUSED, Sets.newHashSet(
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_DENIED, Sets.newHashSet(
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_COMPLETED, Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SCORED, Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SUBMITTED, Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_REPORTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_REPORTED, Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_EXPIRED, Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_INVALIDATED, Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_RESCORED, Sets.newHashSet(
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_ENTRY, Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_EXIT, Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_FORCE_COMPLETED, Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_INITIALIZING, Sets.newHashSet(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
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
