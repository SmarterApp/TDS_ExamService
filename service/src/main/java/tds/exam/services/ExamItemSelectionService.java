package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.item.PageGroupRequest;
import tds.student.sql.data.OpportunityItem;

public interface ExamItemSelectionService {
    /**
     * Creates the next page group
     *
     * @param examId  exam id
     * @param request {@link tds.exam.item.PageGroupRequest} containing information to build the next page
     * @return a {@link tds.student.services.data.PageGroup}
     */
    List<OpportunityItem> createNextPageGroup(UUID examId, PageGroupRequest request);
}
