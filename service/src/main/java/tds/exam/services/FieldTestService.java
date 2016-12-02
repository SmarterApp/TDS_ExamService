package tds.exam.services;

import tds.assessment.Assessment;
import tds.exam.Exam;

/**
 * Service for field test related interactions and checks
 */
public interface FieldTestService {
    /**
     *  This method checks whether the current segment contains field test items and is within a valid field test
     *  window.
     *
     * @param exam          the current {@link tds.exam.Exam}
     * @param assessment    the {@link tds.assessment.Assessment} for which to check eligibility for
     * @param segmentKey    the key of the {@link tds.assessment.Segment} for which to check eligibility for
     * @param languageCode  the code of the language for this {@link tds.exam.Exam}
     * @return  true if the the exam segment is eligible for a field test, false otherwise
     */
    boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey, String languageCode);
}
