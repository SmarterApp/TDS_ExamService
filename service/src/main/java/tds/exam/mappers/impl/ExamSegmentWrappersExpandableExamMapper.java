package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamSegmentWrapper;

@Component
public class ExamSegmentWrappersExpandableExamMapper implements ExpandableExamMapper {
    private final ExamSegmentWrapperService examSegmentWrapperService;

    @Autowired
    public ExamSegmentWrappersExpandableExamMapper(final ExamSegmentWrapperService examSegmentWrapperService) {
        this.examSegmentWrapperService = examSegmentWrapperService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.EXAM_SEGMENT_WRAPPERS)) {
            return;
        }

        examBuilders.forEach((examId, builder) -> {
            List<ExamSegmentWrapper> examSegmentWrappers = examSegmentWrapperService.findAllExamSegments(examId);
            builder.withExamSegmentWrappers(examSegmentWrappers);
        });
    }
}
