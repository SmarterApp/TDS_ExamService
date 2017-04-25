package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.student.sql.data.OpportunityItem;

public interface ExamItemSelectionService {
    /**
     * Creates the next page group
     *
     * @param examId   exam id
     * @param lastPage the last page that had items
     * @return a {@link tds.student.services.data.PageGroup}
     */
    List<OpportunityItem> createNextPageGroup(UUID examId, int lastPage);
}
