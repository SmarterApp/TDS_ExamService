package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;

import java.util.List;

/**
 * Service that handles interactions with exam segments.
 */
public interface ExamSegmentService {

    /**
     * Initializes the {@link ExamSegment}s for the {@link Exam}.
     *
     * @param exam    The {@link Exam} to initialize segments for
     * @param assessment The {@link tds.assessment.Assessment} containing the {@link tds.assessment.Segment}s to initialize
     */
     void initializeExamSegments(Exam exam, Assessment assessment);
}
