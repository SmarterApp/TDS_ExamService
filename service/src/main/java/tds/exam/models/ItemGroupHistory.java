package tds.exam.models;

import java.util.Set;
import java.util.UUID;

/**
 * Contains all the item groups used for the exam
 */
public class ItemGroupHistory {
    private final UUID examId;
    private final Set<String> itemGroupIds;

    public ItemGroupHistory(final UUID examId, final Set<String> itemGroupIds) {
        this.examId = examId;
        this.itemGroupIds = itemGroupIds;
    }

    /**
     * @return the associated exam id
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return the item group ids for the exam
     */
    public Set<String> getItemGroupIds() {
        return itemGroupIds;
    }
}
