package tds.exam.services;

import java.util.Optional;

import tds.assessment.Assessment;

/**
 * Service handles assessment interaction
 */
public interface AssessmentService {
    /**
     * Finds the {@link tds.assessment.Assessment}
     * @param key unique key for the assessment
     * @return {@link tds.assessment.Assessment the assessment}
     */
    Optional<Assessment> findAssessmentByKey(String key);
}
