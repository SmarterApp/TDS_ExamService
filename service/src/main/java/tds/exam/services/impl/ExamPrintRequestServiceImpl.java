/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.impl;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tds.exam.mappers.ExpandableExamPrintRequestMapper;

@Service
public class ExamPrintRequestServiceImpl implements ExamPrintRequestService {
    private static final Logger log = LoggerFactory.getLogger(ExamPrintRequestServiceImpl.class);
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
        final int unfulfilledRequestCountForItem = examPrintRequestQueryRepository
            .findCountOfUnfulfilledRequestsForExamAndItemPosition(examPrintRequest.getExamId(), examPrintRequest.getItemPosition(),
                examPrintRequest.getPagePosition());

        if (unfulfilledRequestCountForItem == 0) {
            examPrintRequestCommandRepository.insert(examPrintRequest);
        } else {
            log.debug("Print request for examId {} and item/page {}/{} was received, but not sent as there is an existing unfulfilled request.",
                examPrintRequest.getExamId(), examPrintRequest.getItemPosition(), examPrintRequest.getPagePosition());
        }
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
                                                                    final String... expandableProperties) {
        final Set<String> expandableExamAttributes = (expandableProperties == null ? new HashSet<>() : Sets.newHashSet(expandableProperties));
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
