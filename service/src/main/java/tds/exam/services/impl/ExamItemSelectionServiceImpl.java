package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.services.ItemSelectionService;
import tds.student.sql.data.OpportunityItem;

import static tds.itemselection.model.ItemResponse.Status.SATISFIED;

@Service
public class ExamItemSelectionServiceImpl implements ExamItemSelectionService {
    private static final String CREATED_AT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private final ItemSelectionService itemSelectionService;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamService examService;
    private final ExamSegmentService examSegmentService;
    private final AssessmentService assessmentService;

    @Autowired
    public ExamItemSelectionServiceImpl(final ItemSelectionService itemSelectionService,
                                        final ExamPageCommandRepository examPageCommandRepository,
                                        final ExamItemCommandRepository examItemCommandRepository,
                                        final ExamService examService,
                                        final AssessmentService assessmentService,
                                        final ExamSegmentService examSegmentService) {
        this.itemSelectionService = itemSelectionService;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examService = examService;
        this.assessmentService = assessmentService;
        this.examSegmentService = examSegmentService;
    }

    @Transactional
    @Override
    public List<OpportunityItem> createNextPageGroup(final UUID examId, final int lastPage, final int lastPosition) {
        Exam exam = examService.findExam(examId).orElseThrow(() -> new IllegalArgumentException("Invalid exam id"));
        Assessment assessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())
            .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find assessment for %s and %s", exam.getClientName(), exam.getAssessmentKey())));

        ItemResponse<ItemGroup> response = itemSelectionService.getNextItemGroup(exam.getId(), assessment.isMultiStageBraille());

        if (response.getResponseStatus() == SATISFIED) {
            return Collections.emptyList();
        } else if (response.getErrorMessage().isPresent()) {
            throw new RuntimeException("Failed to create item group: " + response.getErrorMessage().get());
        } else if (!response.getResponseData().isPresent()) {
            throw new IllegalStateException("No error nor item information was returned from selection.  Please check configuration");
        }

        ItemGroup itemGroup = response.getResponseData().get();

        Segment segment = assessment.getSegment(itemGroup.getSegmentKey());
        Map<String, List<Item>> itemByKey = segment.getItems()
            .stream()
            .collect(Collectors.groupingBy(Item::getId));

        UUID pageId = UUID.randomUUID();

        ExamPage page = new ExamPage.Builder()
            .withId(pageId)
            .withPagePosition(lastPage + 1)
            .withExamId(examId)
            .withItemGroupKey(itemGroup.getGroupID())
            .withGroupItemsRequired(itemGroup.getNumRequired())
            .withSegmentKey(itemGroup.getSegmentKey())
            .withExamRestartsAndResumptions(exam.getRestartsAndResumptions())
            .withVisible(true)
            .build();

        int examItemPosition = lastPosition;
        List<ExamItem> examItems = new ArrayList<>();
        for(TestItem testItem : itemGroup.getItems()) {
            if (!itemByKey.containsKey(testItem.getItemID())) {
                throw new IllegalStateException(String.format("Could not find an assessment %s item with id %s",
                    assessment.getKey(),
                    testItem.getItemID()));
            }

            //Item position is the item position in the exam.  The TestItem or Item position is the position
            //within a segment.  So we take the last position by student and then increment.
            examItemPosition++;

            Item item = itemByKey.get(testItem.getItemID()).get(0);
            ExamItem examItem =new ExamItem.Builder(UUID.randomUUID())
                .withItemKey(item.getId())
                .withExamPageId(pageId)
                .withFieldTest(item.isFieldTest())
                .withPosition(examItemPosition)
                .withRequired(item.isRequired())
                .withItemType(item.getItemType())
                .withGroupId(item.getGroupId())
                .withItemFilePath(item.getItemFilePath())
                .withStimulusFilePath(item.getStimulusFilePath())
                .withAssessmentItemBankKey(item.getBankKey())
                .withAssessmentItemKey(item.getItemKey())
                .build();

            examItems.add(examItem);
        }

        examPageCommandRepository.insert(page);
        examItemCommandRepository.insert(examItems.toArray(new ExamItem[examItems.size()]));

        ExamSegment examSegment = examSegmentService.findByExamIdAndSegmentPosition(examId, itemGroup.getSegmentPosition())
            .orElseThrow(() -> new IllegalStateException(String.format("Could not find the exam segment for exam %s and segment position %s.",
                examId, itemGroup.getSegmentPosition())));

        // For assessments with a single page, we should mark the exam segment as satisfied if it is not already
        if (examSegment.getExamItemCount() == examItems.size() && !examSegment.isSatisfied()) {
            examSegmentService.update(ExamSegment.Builder
                .fromSegment(examSegment)
                .withSatisfied(true)
                .build()
            );
        }

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CREATED_AT_FORMAT);
        final String dateCreated = simpleDateFormat.format(new Date(Instant.now().toEpochMilli()));

        return examItems.stream()
            .map(examItem -> {
                OpportunityItem oppItem = new OpportunityItem();

                oppItem.setGroupID(page.getItemGroupKey());
                oppItem.setFormat(examItem.getItemType());
                oppItem.setIsRequired(examItem.isRequired());
                oppItem.setPage(page.getPagePosition());
                oppItem.setSegmentID(segment.getSegmentId());
                oppItem.setSegment(segment.getPosition());
                oppItem.setBankKey(examItem.getAssessmentItemBankKey());
                oppItem.setItemKey(examItem.getAssessmentItemKey());
                oppItem.setGroupItemsRequired(segment.getMinItems());
                oppItem.setPosition(examItem.getPosition());
                oppItem.setDateCreated(dateCreated);
                oppItem.setGroupItemsRequired(page.getGroupItemsRequired());

                // manually set data (taken from the legacy adaptive service impl. ResponseRepository.insertItems lin 159
                oppItem.setIsVisible(page.isVisible());
                oppItem.setIsSelected(false);
                oppItem.setIsValid(false);
                oppItem.setMarkForReview(false);
                oppItem.setSequence(0);
                oppItem.setStimulusFile(null);
                oppItem.setItemFile(null);

                return oppItem;
            }).collect(Collectors.toList());
    }
}
