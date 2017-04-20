package tds.exam.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import tds.exam.item.PageGroupRequest;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamService;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.services.ItemSelectionService;
import tds.student.services.data.PageGroup;
import tds.student.sql.data.OpportunityItem;

@Service
public class ExamItemSelectionServiceImpl implements ExamItemSelectionService {
    private final ItemSelectionService itemSelectionService;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamService examService;
    private final AssessmentService assessmentService;

    @Autowired
    public ExamItemSelectionServiceImpl(final ItemSelectionService itemSelectionService, final ExamPageCommandRepository examPageCommandRepository, final ExamItemCommandRepository examItemCommandRepository, final ExamService examService, final AssessmentService assessmentService) {
        this.itemSelectionService = itemSelectionService;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examService = examService;
        this.assessmentService = assessmentService;
    }

    @Transactional
    @Override
    public PageGroup createNextPageGroup(UUID examId, PageGroupRequest request) throws ReturnStatusException {
        ItemResponse<ItemGroup> response = itemSelectionService.getNextItemGroup(examId, request.isMsb());

        if(response.getErrorMessage().isPresent()) {
            throw new RuntimeException("Failed to create item group: " + response.getErrorMessage());
        } else if (!response.getResponseData().isPresent()) {
            throw new IllegalStateException("No error nor item information was returned from selection.  Please check configuration");
        }

        Exam exam = examService.findExam(examId).orElseThrow(() -> new IllegalArgumentException("Invalid exam id"));
        Assessment assessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())
            .orElseThrow(() -> new IllegalArgumentException("bad assessment"));

        ItemGroup itemGroup = response.getResponseData().get();

        Segment segment = assessment.getSegment(itemGroup.getSegmentKey());
        Map<String, List<Item>> itemByKey = segment.getItems(exam.getLanguageCode())
            .stream()
            .collect(Collectors.groupingBy(Item::getId));

        UUID pageId = UUID.randomUUID();

        ExamPage page = new ExamPage.Builder()
            .withId(pageId)
            .withPagePosition(request.getLastPage() + 1)
            .withExamId(examId)
            .withItemGroupKey(itemGroup.getGroupID())
            .withSegmentKey(itemGroup.getSegmentKey())
            .withSegmentId(itemGroup.getSegmentID())
            .withSegmentPosition(itemGroup.getSegmentPosition())
            .build();

        List<ExamItem> examItems = itemGroup.getItems().stream()
            .map(testItem -> {
                Item item = itemByKey.get(testItem.getItemID()).get(0);
                return new ExamItem.Builder(UUID.randomUUID())
                    .withItemKey(item.getId())
                    .withExamPageId(pageId)
                    .withFieldTest(item.isFieldTest())
                    .withPosition(testItem.position)
                    .withRequired(testItem.isRequired())
                    .withItemType(testItem.getItemType())
                    .withItemFilePath(item.getItemFilePath())
                    .withStimulusFilePath(item.getStimulusFilePath())
                    .withAssessmentItemBankKey(item.getBankKey())
                    .withAssessmentItemKey(item.getItemKey())
                    .build();
            })
            .collect(Collectors.toList());

        examPageCommandRepository.insert(page);
        examItemCommandRepository.insert(examItems.toArray(new ExamItem[examItems.size()]));

        List<OpportunityItem> opportunityItems = examItems.stream()
            .map(examItem -> {
                OpportunityItem item = new OpportunityItem();
                item.setGroupID(page.getItemGroupKey());
//                item.setGroupItemsRequired();
//                item.setIsPrintable(examItem.isPr);
                item.setIsRequired(examItem.isRequired());
//                item.setIsVisible();
                item.setSegmentID(page.getSegmentId());
                item.setSegment(page.getSegmentPosition());
                item.setBankKey(examItem.getAssessmentItemBankKey());
                item.setItemKey(examItem.getAssessmentItemKey());
                return item;
            }).collect(Collectors.toList());

        PageGroup pageGroup = PageGroup.Create(opportunityItems);
        return pageGroup;
    }


}
