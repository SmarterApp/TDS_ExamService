package tds.exam.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

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
        .put(ExamStatusCode.STATUS_PENDING.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_INITIALIZING,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SUSPENDED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_STARTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(),
            ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase(),
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_APPROVED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_REVIEW.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED,
            ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(),
            ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase()
        ))
        .put(ExamStatusCode.STATUS_PAUSED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_DENIED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_COMPLETED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SCORED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_SUBMITTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_SUBMITTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_REPORTED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_REPORTED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_EXPIRED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_INVALIDATED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_RESCORED,
            ExamStatusCode.STATUS_INVALIDATED
        ))
        .put(ExamStatusCode.STATUS_RESCORED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_ENTRY.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_SEGMENT_EXIT.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_DENIED,
            ExamStatusCode.STATUS_EXPIRED,
            ExamStatusCode.STATUS_INVALIDATED,
            ExamStatusCode.STATUS_FORCE_COMPLETED
        ))
        .put(ExamStatusCode.STATUS_FORCE_COMPLETED.toLowerCase(), Sets.newHashSet(
            ExamStatusCode.STATUS_COMPLETED,
            ExamStatusCode.STATUS_SCORED
        ))
        .put(ExamStatusCode.STATUS_INITIALIZING.toLowerCase(), Sets.newHashSet(
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
    public static boolean isValidTransition(final String currentStatus, final String newStatus) {
        return stateMap.get(currentStatus.toLowerCase()) != null
          && stateMap.get(currentStatus.toLowerCase()).contains(newStatus.toLowerCase());
    }
}
