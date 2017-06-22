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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.services.ExamService;
import tds.exam.mappers.ExpandableExamPrintRequestMapper;

@Component
public class ExamExpandableExamPrintRequestMapper implements ExpandableExamPrintRequestMapper {
    private final ExamService examService;

    @Autowired
    public ExamExpandableExamPrintRequestMapper(final ExamService examService) {
        this.examService = examService;
    }

    @Override
    public void updateExpandableMapper(final Set<String> expandableAttributes, final ExpandableExamPrintRequest.Builder builder, final UUID examId) {
        if (expandableAttributes.contains(ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM)) {
            Optional<Exam> maybeExam = examService.findExam(examId);

            if (!maybeExam.isPresent()) {
                throw new IllegalStateException("Could not retrieve an exam for the request exam print request");
            }

            builder.withExam(maybeExam.get());
        }
    }
}
