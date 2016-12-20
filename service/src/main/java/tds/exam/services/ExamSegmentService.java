package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;

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
    int initializeExamSegments(Exam exam, Assessment assessment);
}
