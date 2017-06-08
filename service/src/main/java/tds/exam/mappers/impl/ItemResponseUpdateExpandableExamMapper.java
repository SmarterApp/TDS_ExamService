package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamItemService;

@Component
public class ItemResponseUpdateExpandableExamMapper implements ExpandableExamMapper {
    private final ExamItemService examItemService;

    @Autowired
    public ItemResponseUpdateExpandableExamMapper(final ExamItemService examItemService) {
        this.examItemService = examItemService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes,
                                       final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.ITEM_RESPONSE_UPDATES)) {
            return;
        }

        examBuilders.forEach((examId, builder) -> {
            // Get the count of all response updates for each exam item
            builder.withItemResponseUpdates(examItemService.getResponseUpdateCounts(examId));
        });
    }
}
