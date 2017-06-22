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

package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.ExamAccommodation;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExamAccommodationService;
import tds.exam.mappers.ExpandableExamMapper;

@Component
public class ExamAccommodationsExpandableExamMapper implements ExpandableExamMapper {
    private final ExamAccommodationService examAccommodationService;

    @Autowired
    public ExamAccommodationsExpandableExamMapper(final ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.EXAM_ACCOMMODATIONS)) {
            return;
        }

        List<ExamAccommodation> examAccommodations =
            examAccommodationService.findApprovedAccommodations(examBuilders.keySet().toArray(new UUID[examBuilders.size()]));

        // list exam accoms grouped by the examId
        Map<UUID, List<ExamAccommodation>> sortedAccommodations = examAccommodations.stream()
            .collect(Collectors.groupingBy(ExamAccommodation::getExamId));

        // Assign each sub-list of exam accommodations to their respective exam ids
        sortedAccommodations.forEach((examId, sortedExamAccommodations) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamAccommodations(sortedExamAccommodations);
        });
    }
}
