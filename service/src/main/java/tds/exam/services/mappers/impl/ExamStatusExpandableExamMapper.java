package tds.exam.services.mappers.impl;

import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamStatusService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class ExamStatusExpandableExamMapper implements ExpandableExamMapper {
    private final ExamStatusService examStatusService;

    @Autowired
    public ExamStatusExpandableExamMapper(final ExamStatusService examStatusService) {
        this.examStatusService = examStatusService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes,
                                       final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_STATUS_DATES)) {
            return;
        }

        examBuilders.forEach((examId, examBuilder) -> {
            Optional<Instant> maybeStartedAt = examStatusService.findDateLastTimeStatus(examId, ExamStatusCode.STATUS_STARTED);

            // If the exam was never started for whatever reason, no need to check for other statuses
            if (!maybeStartedAt.isPresent()) {
                return;
            }

            Optional<Instant> maybeCompletedAt = examStatusService.findDateLastTimeStatus(examId, ExamStatusCode.STATUS_COMPLETED);
            Optional<Instant> maybeForceCompletedAt = examStatusService.findDateLastTimeStatus(examId, ExamStatusCode.STATUS_FORCE_COMPLETED);

            ExpandableExam.Builder builder = examBuilders.get(examId);

            builder.withStartedAt(maybeStartedAt.get());
            builder.withCompletedAt(maybeCompletedAt.isPresent() ? maybeCompletedAt.get() : null);
            builder.withForceCompletedAt(maybeForceCompletedAt.isPresent() ? maybeForceCompletedAt.get() : null);
        });
    }
}
