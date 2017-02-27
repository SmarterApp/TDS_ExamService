package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.services.ExamPrintRequestService;

@Service
public class ExamPrintRequestServiceImpl implements ExamPrintRequestService{
    private final ExamPrintRequestCommandRepository examPrintRequestCommandRepository;
    private final ExamPrintRequestQueryRepository examPrintRequestQueryRepository;

    @Autowired
    public ExamPrintRequestServiceImpl(final ExamPrintRequestCommandRepository examPrintRequestCommandRepository,
                                       final ExamPrintRequestQueryRepository examPrintRequestQueryRepository) {
        this.examPrintRequestCommandRepository = examPrintRequestCommandRepository;
        this.examPrintRequestQueryRepository = examPrintRequestQueryRepository;
    }

    @Override
    public void insert(final ExamPrintRequest examPrintRequest) {
        examPrintRequestCommandRepository.insert(examPrintRequest);
    }

    @Override
    public Map<UUID, Integer> findRequestCountsForExamIds(final UUID sessionId, final UUID... examIds) {
        return examPrintRequestQueryRepository.findRequestCountsForExamIds(sessionId, examIds);
    }
}
