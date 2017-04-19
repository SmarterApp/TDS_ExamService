package tds.exam.services;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

import tds.exam.item.PageGroupRequest;
import tds.student.services.data.PageGroup;

public interface ExamItemSelectionService {
    /**
     * Creates the next page group
     *
     * @param examId  exam id
     * @param request {@link tds.exam.item.PageGroupRequest} containing information to build the next page
     * @return a {@link tds.student.services.data.PageGroup}
     * @throws TDS.Shared.Exceptions.ReturnStatusException if anything goes wrong...
     */
    PageGroup createNextPageGroup(UUID examId, PageGroupRequest request) throws ReturnStatusException;
}
