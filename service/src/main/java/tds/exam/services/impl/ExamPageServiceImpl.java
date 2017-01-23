package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamResponseQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamPageService;

/**
 * A service for handling interactions with exam items, pages, and responses.
 */
@Service
public class ExamPageServiceImpl implements ExamPageService {
    private final ExamQueryRepository examQueryRepository;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamResponseQueryRepository examResponseQueryRepository;
    private final AssessmentService assessmentService;

    @Autowired
    public ExamPageServiceImpl(ExamPageQueryRepository examPageQueryRepository,
                               ExamPageCommandRepository examPageCommandRepository,
                               ExamResponseQueryRepository examResponseQueryRepository,
                               ExamQueryRepository examQueryRepository,
                               AssessmentService assessmentService) {
        this.examQueryRepository = examQueryRepository;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examResponseQueryRepository = examResponseQueryRepository;
        this.assessmentService = assessmentService;
    }

    @Override
    public void insertPages(final List<ExamPage> examPages) {
        examPageCommandRepository.insert(examPages);
    }

    @Override
    public void deletePages(final UUID examId) {
        examPageCommandRepository.deleteAll(examId);
    }

    @Override
    public int getExamPosition(final UUID examId) {
        return examResponseQueryRepository.getCurrentExamItemPosition(examId);
    }

    @Override
    public List<ExamPage> findAllPages(final UUID examId) {
        return examPageQueryRepository.findAll(examId);
    }

    @Override
    public ExamPage getPage(UUID examId, int pageNumber) {
        // TODO: Validate testee access equivalent?
        // Collect data
        Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Could not getPage an exam for id %s", examId)));

        Assessment assessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an assessment for client id %s and key %s", exam.getClientName(), exam.getAssessmentKey())));

        ExamPage examPage = examPageQueryRepository.findPageWithItems(examId, pageNumber)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page for exam id %s and page number/position %s", examId, pageNumber)));

        // Associate the Assessment item to each Exam item
        Map<String, Item> assessmentItemsForSegmentMap = assessment.getSegment(examPage.getSegmentKey())
            .getItems(exam.getLanguageCode())
            .stream()
            .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<ExamItem> examItemsWithAssessmentItems = examPage.getExamItems().stream()
            .map((examItem) -> new ExamItem.Builder()
                .fromExamItem(examItem)
                .withAssessmentItem(assessmentItemsForSegmentMap.get(examItem.getItemKey()))
                .build())
            .collect(Collectors.toList());

        // Build up the completed exam page with all its components
        ExamPage completeExamPage = new ExamPage.Builder()
            .fromExamPage(examPage)
            .withExamItems(examItemsWithAssessmentItems)
            .withStartedAt(Instant.now())
            .build();

        // Update the exam page to record the started_at time, which can be used for measuring the amount of time a
        // student spends responding to items on a page.  Consider the time this page of items was fetched from the
        // database as the start time.
        examPageCommandRepository.update(completeExamPage);

        return completeExamPage;
    }
}
