package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.common.Response;
import tds.exam.Exam;
import tds.exam.ExamSegment;

/**
 * Service that handles interactions with exam segments.
 */
public interface ExamSegmentService {
    
    /**
     * Initializes the {@link ExamSegment}s for the {@link Exam}.
     *
     * @param exam       The {@link Exam} to initialize segments for
     * @param assessment The {@link tds.assessment.Assessment} containing the {@link tds.assessment.Segment}s to initialize
     * @return The number of total items for all exam segments initialized.
     */
    int initializeExamSegments(final Exam exam, final Assessment assessment);

    /**
     * Fetches the {@link tds.exam.ExamSegment}s for the exam id after validating the exam and session.
     *
     * @param examId    The id of the exam to fetch the exam segments for
     * @param sessionId The id of the session this exam belongs to
     * @param browserId The id of the browser this exam belongs to
     * @return The list of {@link tds.exam.ExamSegment}s for the exam
     */
    Response<List<ExamSegment>> findExamSegments(final UUID examId, final UUID sessionId, final UUID browserId);
}
