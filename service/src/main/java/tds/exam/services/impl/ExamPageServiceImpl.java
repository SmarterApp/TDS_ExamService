package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.common.Response;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExpandableExamService;

@Service
public class ExamPageServiceImpl implements ExamPageService {
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExpandableExamService expandableExamService;

    @Autowired
    public ExamPageServiceImpl(final ExamPageQueryRepository examPageQueryRepository,
                               final ExamPageCommandRepository examPageCommandRepository,
                               final ExpandableExamService expandableExamService) {
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.expandableExamService = expandableExamService;
    }

    @Transactional
    @Override
    public void insertPages(final ExamPage... examPages) {
        examPageCommandRepository.insert(examPages);
    }

    @Transactional
    @Override
    public void deletePages(final UUID examId) {
        examPageCommandRepository.deleteAll(examId);
    }

    @Override
    public List<ExamPage> findAllPages(final UUID examId) {
        return examPageQueryRepository.findAll(examId);
    }

    @Override
    public List<ExamPage> findAllPagesWithItems(final UUID examId) {
        final ExpandableExam expandableExam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page(s) and item(s) for exam id %s", examId)));

        // Associate each exam item to its parent page
        return expandableExam.getExamPages().stream()
            .map(examPage -> {
                List<ExamItem> examItems = expandableExam.getExamItems().stream()
                    .filter(examItem -> examItem.getExamPageId().equals(examPage.getId()))
                    .collect(Collectors.toList());

                return ExamPage.Builder
                    .fromExamPage(examPage)
                    .withExamItems(examItems)
                    .build();
            })
            .collect(Collectors.toList());
    }


    @Override
    public Response<ExamPage> getPage(final UUID examId, final int pageNumber) {
        ExamPage examPage = examPageQueryRepository.findPageWithItems(examId, pageNumber)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page for exam id %s and page number/position %s", examId, pageNumber)));

        // Update the exam page to record the started_at time, which can be used for measuring the amount of time a
        // student spends responding to items on a page.  Consider the time this page of items was fetched from the
        // database as the start time.
        examPageCommandRepository.update(examPage);

        return new Response<>(examPage);
    }

    @Override
    public Optional<ExamPage> find(final UUID id) {
        return examPageQueryRepository.find(id);
    }

    @Override
    public void update(final ExamPage... examPages) {
        examPageCommandRepository.update(examPages);
    }
}
