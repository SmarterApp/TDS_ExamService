package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamPrintRequestService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class PrintRequestsExpandableExamMapper implements ExpandableExamMapper {
    private final ExamPrintRequestService examPrintRequestService;

    @Autowired
    public PrintRequestsExpandableExamMapper(final ExamPrintRequestService examPrintRequestService) {
      this.examPrintRequestService = examPrintRequestService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_UNFULFILLED_REQUEST_COUNT)) {
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
