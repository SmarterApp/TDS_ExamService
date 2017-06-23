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

package tds.exam.repositories;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.models.ExamAccommodationFilter;

/**
 * Interface for reading {@link tds.exam.ExamAccommodation} data for an {@link tds.exam.Exam}.
 */
public interface ExamAccommodationQueryRepository {
    /**
     * Retrieve a list of {@link tds.exam.ExamAccommodation}s for the specified exam id and a collection of accommodation types.
     *
     * @param examId             The id of the {@link tds.exam.Exam} that owns the {@link tds.exam.ExamAccommodation}s
     * @param segmentKey         The key of the segment to which the {@link tds.exam.ExamAccommodation}s apply
     * @param accommodationTypes a list of types of {@link tds.exam.ExamAccommodation}s to find
     * @return A list of {@link tds.exam.ExamAccommodation}s that correspond to the specified accommodation types
     */
    List<ExamAccommodation> findAccommodations(final UUID examId, final String segmentKey, final String... accommodationTypes);

    /**
     * Retrieves all the accommodations associated with an exam
     *
     * @param examIds the exam ids to find exam accommodations for
     * @return list of {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> findAccommodations(final UUID... examIds);

    /**
     * Retrieves all the approved {@link tds.exam.ExamAccommodation} associated with the exam
     *
     * @param examIds the unique ids for the exams
     * @return list containing approved {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> findApprovedAccommodations(final UUID... examIds);

    /**
     * Finds non deleted {@link tds.exam.ExamAccommodation}
     *
     * @param examId                   the exam id
     * @param examAccommodationFilters a way to filter exam accommodations
     * @return list of matching of {@link tds.exam.ExamAccommodation}
     */
    List<ExamAccommodation> findAccommodations(final UUID examId, final Collection<ExamAccommodationFilter> examAccommodationFilters);
}
