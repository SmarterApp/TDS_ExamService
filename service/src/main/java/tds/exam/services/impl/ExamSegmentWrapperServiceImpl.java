package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.ExamSegment;
import tds.exam.services.ExamPageWrapperService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;

@Service
public class ExamSegmentWrapperServiceImpl implements ExamSegmentWrapperService {
    private final ExamSegmentService examSegmentService;
    private final ExamPageWrapperService examPageService;

    @Autowired
    public ExamSegmentWrapperServiceImpl(final ExamSegmentService examSegmentService, final ExamPageWrapperService examPageService) {
        this.examSegmentService = examSegmentService;
        this.examPageService = examPageService;
    }

    @Override
    public List<ExamSegmentWrapper> findAllExamSegments(final UUID examId) {
        Map<String, List<ExamPageWrapper>> examPageWrappersBySegmentKey = examPageService.findPagesWithItems(examId)
            .stream()
            .collect(Collectors.groupingBy(examPageWrapper -> examPageWrapper.getExamPage().getSegmentKey()));

        return examSegmentService.findExamSegments(examId)
            .stream()
            .map(examSegment -> new ExamSegmentWrapper(examSegment, examPageWrappersBySegmentKey.get(examSegment.getSegmentKey())))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ExamSegmentWrapper> findExamSegment(final UUID examId, final int segmentPosition) {
        Optional<ExamSegment> maybeExamSegment = examSegmentService.findByExamIdAndSegmentPosition(examId, segmentPosition);

        return maybeExamSegment.map(examSegment -> new ExamSegmentWrapper(examSegment, examPageService.findPagesForExamSegment(examId, examSegment.getSegmentKey())));
    }

    @Override
    public Optional<ExamSegmentWrapper> findExamSegmentWithPageAtPosition(final UUID examId, final int segmentPosition, final int pagePosition) {
        Optional<ExamSegment> maybeExamSegment = examSegmentService.findByExamIdAndSegmentPosition(examId, segmentPosition);
        Optional<ExamPageWrapper> maybeExamPageWrapper = examPageService.findPageWithItems(examId, pagePosition);

        if (!maybeExamSegment.isPresent() || !maybeExamPageWrapper.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new ExamSegmentWrapper(maybeExamSegment.get(), Collections.singletonList(maybeExamPageWrapper.get())));
    }
}
