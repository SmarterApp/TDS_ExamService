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

package tds.exam.services.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.Exam;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExpandableExamService;

@Service
public class ExpandableExamServiceImpl implements ExpandableExamService {
    private final Collection<ExpandableExamMapper> expandableExamMappers;
    private final ExamQueryRepository examQueryRepository;

    @Autowired
    public ExpandableExamServiceImpl(final Collection<ExpandableExamMapper> expandableExamMappers, final ExamQueryRepository examQueryRepository) {
        this.expandableExamMappers = expandableExamMappers;
        this.examQueryRepository = examQueryRepository;
    }

    @Override
    public List<ExpandableExam> findExamsBySessionId(final UUID sessionId, final Set<String> invalidStatuses,
                                                     final ExpandableExamAttributes... expandableAttributes) {
        final Set<ExpandableExamAttributes> expandableExamAttributes = expandableAttributes == null
            ? new HashSet<>() : Sets.newHashSet(expandableAttributes);
        final List<Exam> exams = examQueryRepository.findAllExamsInSessionWithoutStatus(sessionId, invalidStatuses);
        final Map<UUID, ExpandableExam.Builder> examBuilders = exams.stream()
            .collect(Collectors.toMap(Exam::getId, ExpandableExam.Builder::new));
        final UUID[] examIds = examBuilders.keySet().toArray(new UUID[examBuilders.size()]);

        if (examIds.length == 0) {
            return new ArrayList<>();
        }

        expandableExamMappers.forEach(mapper -> mapper.updateExpandableMapper(expandableExamAttributes, examBuilders, sessionId));

        // Build each exam and return
        return examBuilders.values().stream()
            .map(ExpandableExam.Builder::build)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ExpandableExam> findExam(final UUID examId, final ExpandableExamAttributes... expandableAttributes) {
        final Set<ExpandableExamAttributes> expandableExamAttributes = expandableAttributes == null
            ? new HashSet<>() : Sets.newHashSet(expandableAttributes);
        Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);

        if (!maybeExam.isPresent()) {
            return Optional.empty();
        }

        Exam exam = maybeExam.get();
        ExpandableExam.Builder builder = new ExpandableExam.Builder(exam);
        Map<UUID, ExpandableExam.Builder> examMap = ImmutableMap.of(examId, builder);

        expandableExamMappers.forEach(mapper -> mapper.updateExpandableMapper(expandableExamAttributes,
            examMap, exam.getSessionId())
        );

        return Optional.of(builder.build());
    }
}
