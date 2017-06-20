/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services;

import java.util.List;
import java.util.Optional;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.assessment.AssessmentInfo;
import tds.assessment.AssessmentWindow;
import tds.assessment.SegmentItemInformation;
import tds.session.ExternalSessionConfiguration;

/**
 * Service handles assessment interaction
 */
public interface AssessmentService {
    /**
     * Finds the {@link tds.assessment.Assessment}
     *
     * @param clientName The name of the client (e.g. SBAC or SBAC_PT)
     * @param key        unique key for the assessment
     * @return {@link tds.assessment.Assessment the assessment}
     */
    Optional<Assessment> findAssessment(final String clientName, final String key);

    /**
     * Finds the assessment windows for an exam
     *
     * @param clientName    environment's client name
     * @param assessmentId  the assessment id for the assessment
     * @param guestStudent  a flag indicating whether the student is a guest student
     * @param configuration {@link tds.session.ExternalSessionConfiguration} for the environment
     * @return array of {@link tds.assessment.AssessmentWindow}
     */
    List<AssessmentWindow> findAssessmentWindows(final String clientName, final String assessmentId, final boolean guestStudent, final ExternalSessionConfiguration configuration);

    /**
     * Finds the {@link tds.accommodation.Accommodation} for the assessment key
     *
     * @param clientName    the client name associated with the assessment
     * @param assessmentKey the assessment key
     * @return {@link tds.accommodation.Accommodation} for the assessment key
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey);

    /**
     * Finds the {@link tds.accommodation.Accommodation} for the assessment id
     *
     * @param clientName   the client name associated with the assessment
     * @param assessmentId the assessment id
     * @return {@link tds.accommodation.Accommodation} for the assessment id
     */
    List<Accommodation> findAssessmentAccommodationsByAssessmentId(final String clientName, final String assessmentId);

    /**
     * Finds the {@link tds.assessment.SegmentItemInformation}
     *
     * @param segmentKey the segment key
     * @return optional with the {@link tds.assessment.SegmentItemInformation} if found
     */
    Optional<SegmentItemInformation> findSegmentItemInformation(final String segmentKey);

    /**
     * Fetches the list of {@link tds.assessment.AssessmentInfo} for a given collection of assessmentKeys
     *
     * @param clientName     The client name of the TDS environment
     * @param assessmentKeys The collection of keys to obtain {@link tds.assessment.AssessmentInfo} for
     * @return A list of {@link tds.assessment.AssessmentInfo}
     */
    List<AssessmentInfo> findAssessmentInfosForAssessments(final String clientName, final String... assessmentKeys);

    /**
     * Finds all {@link tds.assessment.AssessmentInfo}s for the specified grade
     *
     * @param clientName the client name associated with the assessment
     * @param grade      the grade to fetch all {@link tds.assessment.AssessmentInfo} for
     * @return {@link tds.assessment.AssessmentInfo} if found otherwise empty
     */
    List<AssessmentInfo> findAssessmentInfosForGrade(final String clientName, final String grade);
}
