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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamPrintRequestService;

@Component
public class PrintRequestsExpandableExamMapper implements ExpandableExamMapper {
    private final ExamPrintRequestService examPrintRequestService;

    @Autowired
    public PrintRequestsExpandableExamMapper(final ExamPrintRequestService examPrintRequestService) {
        this.examPrintRequestService = examPrintRequestService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.UNFULFILLED_REQUEST_COUNT)) {
            return;
        }

        Map<UUID, Integer> examRequestCounts = examPrintRequestService.findRequestCountsForExamIds(sessionId,
            examBuilders.keySet().toArray(new UUID[examBuilders.size()]));

        examRequestCounts.forEach((examId, requestCount) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withRequestCount(requestCount);
        });
    }
}
