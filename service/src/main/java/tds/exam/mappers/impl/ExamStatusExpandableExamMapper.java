package tds.exam.mappers.impl;

import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamStatusService;

@Component
public class ExamStatusExpandableExamMapper implements ExpandableExamMapper {
    private final ExamStatusService examStatusService;

    @Autowired
    public ExamStatusExpandableExamMapper(final ExamStatusService examStatusService) {
        this.examStatusService = examStatusService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes,
                                       final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.EXAM_STATUS_DATES)) {
            return;
        }

        examBuilders.forEach((examId, builder) -> {
            Optional<Instant> maybeForceCompletedAt = examStatusService.findRecentTimeAtStatus(examId, ExamStatusCode.STATUS_FORCE_COMPLETED);
            builder.withForceCompletedAt(maybeForceCompletedAt.isPresent() ? maybeForceCompletedAt.get() : null);
        });
    }
}
