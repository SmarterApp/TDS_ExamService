package tds.exam.services.impl;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.services.ExamPrintRequestService;
import tds.exam.services.ExpandableExamPrintRequestMapper;

@Service
public class ExamPrintRequestServiceImpl implements ExamPrintRequestService {
    private final ExamPrintRequestCommandRepository examPrintRequestCommandRepository;
    private final ExamPrintRequestQueryRepository examPrintRequestQueryRepository;
    private final Collection<ExpandableExamPrintRequestMapper> expandableExamPrintRequestMappers;

    @Autowired
    public ExamPrintRequestServiceImpl(final ExamPrintRequestCommandRepository examPrintRequestCommandRepository,
                                       final ExamPrintRequestQueryRepository examPrintRequestQueryRepository,
                                       final Collection<ExpandableExamPrintRequestMapper> expandableExamPrintRequestMappers) {
        this.examPrintRequestCommandRepository = examPrintRequestCommandRepository;
        this.examPrintRequestQueryRepository = examPrintRequestQueryRepository;
        this.expandableExamPrintRequestMappers = expandableExamPrintRequestMappers;
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

        final ExamPrintRequest updatedRequest = new ExamPrintRequest.Builder(id)
            .fromExamPrintRequest(maybePrintRequest.get())
            .withStatus(status)
            .withReasonDenied(reason)
            .build();

        examPrintRequestCommandRepository.update(updatedRequest);

        return Optional.of(updatedRequest);
    }

    @Override
    public Optional<ExpandableExamPrintRequest> updateAndGetRequest(final ExamPrintRequestStatus status, final UUID id, final String reason,
                                                                    final String... embed) {
        final Set<String> expandableExamAttributes = (embed == null ? new HashSet<>() : Sets.newHashSet(embed));
        final Optional<ExamPrintRequest> maybePrintRequest = examPrintRequestQueryRepository.findExamPrintRequest(id);

        if (!maybePrintRequest.isPresent()) {
            return Optional.empty();
        }

        final ExamPrintRequest updatedRequest = new ExamPrintRequest.Builder(id)
            .fromExamPrintRequest(maybePrintRequest.get())
            .withStatus(status)
            .withReasonDenied(reason)
            .build();

        examPrintRequestCommandRepository.update(updatedRequest);

        ExpandableExamPrintRequest.Builder builder = new ExpandableExamPrintRequest.Builder(updatedRequest);

        expandableExamPrintRequestMappers.forEach(mapper -> mapper.updateExpandableMapper(expandableExamAttributes, builder, updatedRequest.getExamId()));

        return Optional.of(builder.build());
    }

    @Override
    public List<ExamPrintRequest> findApprovedRequests(final UUID sessionId) {
        return examPrintRequestQueryRepository.findApprovedRequests(sessionId);
    }
}
