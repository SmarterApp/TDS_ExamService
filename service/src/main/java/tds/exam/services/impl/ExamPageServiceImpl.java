package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamPageService;

@Service
public class ExamPageServiceImpl implements ExamPageService {
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamItemQueryRepository examItemQueryRepository;


    @Autowired
    public ExamPageServiceImpl(final ExamPageQueryRepository examPageQueryRepository,
                               final ExamPageCommandRepository examPageCommandRepository,
                               final ExamItemQueryRepository examItemQueryRepository) {
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examItemQueryRepository = examItemQueryRepository;
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
        final List<ExamPage> examPages = examPageQueryRepository.findAll(examId);
        final List<ExamItem> examItems = examItemQueryRepository.findExamItemAndResponses(examId);

        // Associate each exam item collection to its parent page
        final Map<UUID, List<ExamItem>> itemsToPageMap = examItems.stream()
            .collect(Collectors.groupingBy(ExamItem::getExamPageId));

        return examPages.stream()
            .map(examPage -> {
                List<ExamItem> itemsForPage = itemsToPageMap.containsKey(examPage.getId())
                    ? itemsToPageMap.get(examPage.getId())
                    : new ArrayList<>();

                return ExamPage.Builder
                    .fromExamPage(examPage)
                    .withExamItems(itemsForPage)
                    .build();
                })
            .collect(Collectors.toList());
    }


    @Override
    public ExamPage getPage(final UUID examId, final int pageNumber) {
        ExamPage examPage = examPageQueryRepository.findPageWithItems(examId, pageNumber)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page for exam id %s and page number/position %s", examId, pageNumber)));

        // Update the exam page to record the started_at time, which can be used for measuring the amount of time a
        // student spends responding to items on a page.  Consider the time this page of items was fetched from the
        // database as the start time.
        examPageCommandRepository.update(examPage);

        return examPage;
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
