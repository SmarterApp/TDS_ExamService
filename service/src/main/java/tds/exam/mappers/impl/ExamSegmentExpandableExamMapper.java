package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExamSegmentService;
import tds.exam.mappers.ExpandableExamMapper;

@Component
public class ExamSegmentExpandableExamMapper implements ExpandableExamMapper {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public ExamSegmentExpandableExamMapper(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.EXAM_SEGMENTS)) {
            return;
        }

        examBuilders.forEach((examId, examBuilder) -> {
            List<ExamSegment> examSegments = examSegmentService.findExamSegments(examId);
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamSegments(examSegments);
        });
    }
}
