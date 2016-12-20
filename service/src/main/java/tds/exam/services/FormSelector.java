package tds.exam.services;

import java.util.Optional;

import tds.assessment.Form;
import tds.assessment.Segment;

/**
 * A service for selecting test forms.
 */
public interface FormSelector {

    /**
     * Selects a test form for an exam segment.
     *
     * @param segment      The {@link tds.assessment.Segment} to select a form from
     * @param languageCode The languageCode of the exam
     * @return The selected test forms
     */
    Optional<Form> selectForm(Segment segment, String languageCode);
}
