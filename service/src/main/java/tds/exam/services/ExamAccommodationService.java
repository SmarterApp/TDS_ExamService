package tds.exam.services;

import org.joda.time.Instant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;

/**
 * Handles interaction with {@link tds.exam.ExamAccommodation}s associated to an {@link tds.exam.Exam}
 */
public interface ExamAccommodationService {
    /**
     * Find the {@link tds.exam.ExamAccommodation}(s) of the specified types that is/are approved for an {@link tds.exam.Exam}.
     *
     * @param examId             The ID of the {@link tds.exam.Exam}
     * @param segmentId          The id of the segment to which the {@link tds.exam.ExamAccommodation}s apply
     * @param accommodationTypes The types of {@link tds.exam.ExamAccommodation}s to find
     * @return An {@link tds.exam.ExamAccommodation} if one exists for the specified exam id and accommodation type; otherwise empty
     */
    List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String... accommodationTypes);

    /**
     * Find the {@link tds.exam.ExamAccommodation}(s) of the specified types that is/are approved for an {@link tds.exam.Exam}.
     *
     * @param examId The ID of the {@link tds.exam.Exam}
     * @return An {@link tds.exam.ExamAccommodation} if one exists for the specified exam id; otherwise empty
     */
    List<ExamAccommodation> findAllAccommodations(UUID examId);

    /**
     * Initializes and inserts exam accommodations for the exam;
     *
     * @param exam exam to use to initialize the {@link tds.exam.ExamAccommodation}
     * @param studentAccommodationCodes the separated list of accommodations from the student package retrieved from ART
     */
    List<ExamAccommodation> initializeExamAccommodations(Exam exam, String studentAccommodationCodes);

    /**
     * Finds the approved {@link tds.exam.ExamAccommodation}
     *
     * @param examIds the exam ids
     * @return list of approved {@link tds.exam.ExamAccommodation}s
     */
    List<ExamAccommodation> findApprovedAccommodations(UUID... examIds);

    /**
     * Initializes accommodations on a previous exam
     *
     * @param exam                {@link tds.exam.Exam}
     * @param assessment          {@link tds.assessment.Assessment}
     * @param segmentPosition     the segment position for the accommodations
     * @param restoreRts          {@code true} if the restore rts
     * @param studentAccommodationCodes the student accommodations String
     *
     * @return list of {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> initializeAccommodationsOnPreviousExam(Exam exam, Assessment assessment, int segmentPosition, boolean restoreRts, String studentAccommodationCodes);
    
    /**
     * Approves {@link tds.exam.ExamAccommodation}s for an exam and set of accommodation codes.
     *
     * @param examId  the id of the {@link tds.exam.Exam} to approve accommodations for
     * @param request the {@link tds.exam.ApproveAccommodationsRequest} containing request datazz
     * @return an optional {@link tds.common.ValidationError}
     */
    Optional<ValidationError> approveAccommodations(UUID examId, ApproveAccommodationsRequest request);

    /**
     * Denies all {@link tds.exam.ExamAccommodation}s for an exam
     *
     * @param examId The id of the exam for which to deny accommodations
     * @param deniedAt
     */
    void denyAccommodations(final UUID examId, final Instant deniedAt);
}
