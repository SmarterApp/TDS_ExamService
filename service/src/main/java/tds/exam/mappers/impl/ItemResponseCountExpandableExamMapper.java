package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExamItemService;
import tds.exam.mappers.ExpandableExamMapper;

@Component
public class ItemResponseCountExpandableExamMapper implements ExpandableExamMapper {
    private final ExamItemService examItemService;

    @Autowired
    public ItemResponseCountExpandableExamMapper(final ExamItemService examItemService) {
        this.examItemService = examItemService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.ITEM_RESPONSE_COUNT)) {
            return;
        }

        Map<UUID, Integer> itemResponseCounts = examItemService.getResponseCounts(examBuilders.keySet().toArray(new UUID[examBuilders.size()]));

        itemResponseCounts.forEach((examId, responseCount) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withItemsResponseCount(responseCount);
        });
    }
}
