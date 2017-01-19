package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;

/**
 * Service for field test related interactions and checks
 */
public interface FieldTestService {
    /**
     * This method checks whether the current segment contains field test items and is within a valid field test
     * window.
     *
     * @param exam       the current {@link tds.exam.Exam}
     * @param assessment the {@link tds.assessment.Assessment} for which to check eligibility for
     * @param segmentKey the key of the {@link tds.assessment.Segment} for which to check eligibility for
     * @return true if the the exam segment is eligible for a field test, false otherwise
     */
    boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey);

    /**
     * This method selects {@link tds.exam.models.FieldTestItemGroup}s for the exam based on the {@link tds.assessment.Assessment}s
     * field test requirements.
     *
     * @param exam       the current {@link tds.exam.Exam}
     * @param assessment the {@link tds.assessment.Assessment} of the {@link tds.exam.Exam}
     * @param segmentKey the key of the {@link tds.assessment.Segment} to select {@link tds.exam.models.FieldTestItemGroup}s
     * @return The total number of field test items for this {@link tds.assessment.Segment}
     */
    int selectItemGroups(Exam exam, Assessment assessment, String segmentKey);
}
