package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;

@Component
public class ExamPageItemResponseExpandableExamMapper implements ExpandableExamMapper {
    private final ExamPageService examPageService;
    private final ExamItemService examItemService;

    @Autowired
    public ExamPageItemResponseExpandableExamMapper(final ExamPageService examPageService,
                                                    final ExamItemService examItemService) {
        this.examPageService = examPageService;
        this.examItemService = examItemService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (expandableAttributes.contains(ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)) {
            examBuilders.forEach((examId, examBuilder) -> {
                List<ExamPage> examPages = examPageService.findAllPages(examId);
                examBuilder.withExamPages(examPages);
                examBuilder.withExamItems(examItemService.findExamItemAndResponses(examId));
            });
        }
    }
}
