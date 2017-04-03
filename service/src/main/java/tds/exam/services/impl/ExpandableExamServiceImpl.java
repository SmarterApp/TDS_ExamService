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
import tds.exam.ExpandableExamParameters;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.mappers.ExpandableExamMapper;
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
                                                     final ExpandableExamParameters... embed) {
        final Set<ExpandableExamParameters> expandableExamAttributes = embed == null ? new HashSet<>() : Sets.newHashSet(embed);
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
    public Optional<ExpandableExam> findExam(final UUID examId, final ExpandableExamParameters... embed) {
        final Set<ExpandableExamParameters> expandableExamAttributes = embed == null ? new HashSet<>() : Sets.newHashSet(embed);
        Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);

        if (!maybeExam.isPresent()) {
            return Optional.empty();
        }

        Exam exam = maybeExam.get();
        ExpandableExam.Builder builder = new ExpandableExam.Builder(exam);

        expandableExamMappers.forEach(mapper -> mapper.updateExpandableMapper(expandableExamAttributes,
            ImmutableMap.of(examId, builder), exam.getSessionId())
        );

        return Optional.of(builder.build());
    }
}
