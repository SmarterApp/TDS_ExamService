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
