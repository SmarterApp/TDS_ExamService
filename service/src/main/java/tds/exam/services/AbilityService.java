package tds.exam.services;

import java.util.Optional;

import tds.assessment.Assessment;
import tds.exam.Exam;

/**
 * handles ability related actions
 */
public interface AbilityService {
    /**
     * Retrieves the initial ability value for an {@link tds.exam.Exam}.
     *
     * @param exam       the exam to retrieve an ability for.
     * @param assessment the {@link tds.assessment.Assessment} associated with the exam
     * @return the initial ability for an {@link tds.exam.Exam}.
     */
    Optional<Double> getInitialAbility(final Exam exam, final Assessment assessment);
}
