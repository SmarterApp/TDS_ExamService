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

package tds.exam.mappers;

import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExamPrintRequest;

/**
 * Interface for a service that maps an {@link tds.exam.ExpandableExamPrintRequest} with optional attributes
 */
public interface ExpandableExamPrintRequestMapper {

    /**
     * Updates an {@link tds.exam.ExpandableExamPrintRequest.Builder} based on the expandable attributes provided
     *
     * @param expandableAttributes A set of optional exam print request attributes
     * @param builder              The builder of the {@link tds.exam.ExpandableExamPrintRequest}
     * @param examId               The id of the exam that the {@link tds.exam.ExamPrintRequest} belongs to
     */
    void updateExpandableMapper(final Set<String> expandableAttributes, final ExpandableExamPrintRequest.Builder builder,
                                final UUID examId);
}
