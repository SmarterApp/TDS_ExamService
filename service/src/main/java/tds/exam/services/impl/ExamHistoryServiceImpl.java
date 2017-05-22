package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.ExamHistoryService;

@Service
public class ExamHistoryServiceImpl implements ExamHistoryService {
    private final HistoryQueryRepository historyQueryRepository;

    @Autowired
    public ExamHistoryServiceImpl(final HistoryQueryRepository historyQueryRepository) {
        this.historyQueryRepository = historyQueryRepository;
    }

    @Override
    public List<ItemGroupHistory> findPreviousItemGroups(final long studentId, final UUID excludedExamId, final String assessmentId) {
        return historyQueryRepository.findPreviousItemGroups(studentId, excludedExamId, assessmentId);
    }
}
