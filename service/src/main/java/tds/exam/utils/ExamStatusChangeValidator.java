package tds.exam.utils;

import java.util.Optional;

import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;

/**
 * An interface to allow for conducting status transition business logic beyond the scope of the
 * {@link tds.exam.utils.StatusTransitionValidator}.
 */
public interface ExamStatusChangeValidator {
    /**
     * Validate that an {@link tds.exam.Exam} can transition from one state to another.
     *
     * @param exam           The {@link tds.exam.Exam} containing the current {@link tds.exam.ExamStatusCode}
     * @param intendedStatus The {@link tds.exam.ExamStatusCode} to which the {@link tds.exam.Exam} wants to change
     * @return True if the status transition is valid; otherwise false
     */
    Optional<ValidationError> validate(final Exam exam, final ExamStatusCode intendedStatus);
}
