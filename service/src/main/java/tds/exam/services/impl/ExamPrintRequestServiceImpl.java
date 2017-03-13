package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.services.ExamPrintRequestService;

@Service
public class ExamPrintRequestServiceImpl implements ExamPrintRequestService {
    private final ExamPrintRequestCommandRepository examPrintRequestCommandRepository;
    private final ExamPrintRequestQueryRepository examPrintRequestQueryRepository;

    @Autowired
    public ExamPrintRequestServiceImpl(final ExamPrintRequestCommandRepository examPrintRequestCommandRepository,
                                       final ExamPrintRequestQueryRepository examPrintRequestQueryRepository) {
        this.examPrintRequestCommandRepository = examPrintRequestCommandRepository;
        this.examPrintRequestQueryRepository = examPrintRequestQueryRepository;
    }

    @Override
    @Transactional
    public void insert(final ExamPrintRequest examPrintRequest) {
        examPrintRequestCommandRepository.insert(examPrintRequest);
    }

    @Override
    public Map<UUID, Integer> findRequestCountsForExamIds(final UUID sessionId, final UUID... examIds) {
        return examPrintRequestQueryRepository.findRequestCountsForExamIds(sessionId, examIds);
    }

    @Override
    public List<ExamPrintRequest> findUnfulfilledRequests(final UUID examId, final UUID sessionId) {
        return examPrintRequestQueryRepository.findUnfulfilledRequests(examId, sessionId);
    }

    @Override
    public Optional<ExamPrintRequest> updateAndGetRequest(final ExamPrintRequestStatus status, final UUID id, final String reason) {
        final Optional<ExamPrintRequest> maybePrintRequest = examPrintRequestQueryRepository.findExamPrintRequest(id);

        if (!maybePrintRequest.isPresent()) {
            return Optional.empty();
        }

        final ExamPrintRequest deniedRequest = new ExamPrintRequest.Builder(id)
            .fromExamPrintRequest(maybePrintRequest.get())
            .withStatus(status)
            .withReasonDenied(reason)
            .build();

        examPrintRequestCommandRepository.update(deniedRequest);

        return Optional.of(deniedRequest);
    }

    @Override
    public List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) {
        return examPrintRequestQueryRepository.findApprovedRequests(sessionId);
    }
}
