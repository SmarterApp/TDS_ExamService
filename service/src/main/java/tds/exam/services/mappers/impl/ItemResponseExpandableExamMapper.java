package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamItemService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class ItemResponseExpandableExamMapper implements ExpandableExamMapper {
    private final ExamItemService examItemService;

    @Autowired
    public ItemResponseExpandableExamMapper(final ExamItemService examItemService) {
        this.examItemService = examItemService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT)) {
            return;
        }

        Map<UUID, Integer> itemResponseCounts = examItemService.getResponseCounts(examBuilders.keySet().toArray(new UUID[examBuilders.size()]));

        itemResponseCounts.forEach((examId, responseCount) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withItemsResponseCount(responseCount);
        });
    }
}
