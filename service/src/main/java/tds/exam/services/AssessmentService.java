package tds.exam.services;

import java.util.Optional;

import tds.assessment.SetOfAdminSubject;

/**
 * Service handles assessment interaction
 */
public interface AssessmentService {
    /**
     * Finds the {@link tds.assessment.SetOfAdminSubject SetOfAdminSubject}
     * @param key unique key for the SetOfAdminSubject
     * @return {@link tds.assessment.SetOfAdminSubject SetOfAdminSubject}
     */
    Optional<SetOfAdminSubject> findSetOfAdminSubjectByKey(String key);
}
