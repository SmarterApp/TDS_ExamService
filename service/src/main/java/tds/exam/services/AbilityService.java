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
