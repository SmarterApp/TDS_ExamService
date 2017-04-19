package tds.exam.services.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.item.PageGroupRequest;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamItemResponseScoringService;
import tds.exam.services.ExamItemService;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.services.ItemSelectionService;
import tds.student.services.data.PageGroup;
import tds.student.sql.data.OpportunityItem;

import static tds.exam.error.ValidationErrorCode.EXAM_ITEM_DOES_NOT_EXIST;
import static tds.exam.error.ValidationErrorCode.EXAM_ITEM_RESPONSE_DOES_NOT_EXIST;

@Service
public class ExamItemServiceImpl implements ExamItemService {
    private final ExamItemQueryRepository examItemQueryRepository;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamQueryRepository examQueryRepository;
    private final ExamItemResponseScoringService examItemResponseScoringService;
    private final ItemSelectionService itemSelectionService;

    @Autowired
    public ExamItemServiceImpl(final ExamItemQueryRepository examItemQueryRepository,
                               final ExamItemCommandRepository examItemCommandRepository,
                               final ExamPageCommandRepository examPageCommandRepository,
                               final ExamPageQueryRepository examPageQueryRepository,
                               final ExamQueryRepository examQueryRepository,
                               final ExamItemResponseScoringService examItemResponseScoringService,
                               final ItemSelectionService itemSelectionService) {
        this.examItemQueryRepository = examItemQueryRepository;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examQueryRepository = examQueryRepository;
        this.examItemResponseScoringService = examItemResponseScoringService;
        this.itemSelectionService = itemSelectionService;
    }

    @Transactional
    @Override
    public Response<ExamPage> insertResponses(final UUID examId, final int mostRecentPagePosition, final ExamItemResponse... responses) {
        Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam for exam id %s", examId)));


        // RULE:  An exam must be in the "started" or "review" status for responses to be saved.  Legacy rule location:
        // StudentDLL.T_UpdateScoredResponse_common, line 2031
        if (!(exam.getStatus().getCode().equals(ExamStatusCode.STATUS_STARTED)
            || exam.getStatus().getCode().equals(ExamStatusCode.STATUS_REVIEW))) {
            return new Response<>(new ValidationError(ValidationErrorCode.EXAM_INTERRUPTED, "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test."));
        }

        // Get the current page, which will be used as the basis for creating the next page
        ExamPage currentPage = examPageQueryRepository.find(examId, mostRecentPagePosition)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find exam page for id %s and position %d", examId, mostRecentPagePosition)));

        // Score each response
        // TODO:  Revisit this once scoring logic has been ported over; getting a score may be more complex and/or require more data
        ExamItemResponse[] scoredResponses = Stream.of(responses).map(response -> {
            ExamItemResponseScore score = examItemResponseScoringService.getScore(response);

            return ExamItemResponse.Builder
                .fromExamItemResponse(response)
                .withScore(score)
                .build();
        }).toArray(ExamItemResponse[]::new);

        examItemCommandRepository.insertResponses(scoredResponses);

        ExamPage nextPage = ExamPage.Builder
            .fromExamPage(currentPage)
            .withPagePosition(mostRecentPagePosition + 1)
            .build();

        examPageCommandRepository.insert(nextPage);

        return new Response<>(nextPage);
    }

    @Override
    public int getExamPosition(final UUID examId) {
        return examItemQueryRepository.getCurrentExamItemPosition(examId);
    }

    @Override
    public Map<UUID, Integer> getResponseCounts(final UUID... examIds) {
        return examItemQueryRepository.getResponseCounts(examIds);
    }

    @Override
    public Optional<ValidationError> markForReview(final UUID examId, final int position, final Boolean mark) {
        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(examId, position);

        if (!maybeExamItem.isPresent()) {
            return Optional.of(new ValidationError(EXAM_ITEM_DOES_NOT_EXIST,
                String.format("No exam item found for exam id '%s' at position '%s'.", examId, position)));
        }

        ExamItem examItem = maybeExamItem.get();

        if (!examItem.getResponse().isPresent()) {
            return Optional.of(new ValidationError(EXAM_ITEM_RESPONSE_DOES_NOT_EXIST,
                String.format("No exam response found for exam id '%s' at item position '%s'.", examId, position)));
        }

        ExamItemResponse updatedResponse = ExamItemResponse.Builder
            .fromExamItemResponse(examItem.getResponse().get())
            .withMarkedForReview(mark)
            .build();

        examItemCommandRepository.insertResponses(updatedResponse);

        return Optional.empty();
    }

    @Override
    public List<ExamItem> findExamItemAndResponses(final UUID examId) {
        return examItemQueryRepository.findExamItemAndResponses(examId);
    }

    @Override
    public Optional<ExamItem> findExamItemAndResponse(final UUID examId, final int position) {
        return examItemQueryRepository.findExamItemAndResponse(examId, position);
    }

    @Transactional
    @Override
    public PageGroup createNextPageGroup(UUID examid, PageGroupRequest request) throws ReturnStatusException {
        PageGroup pageGroup = null;

        ItemResponse<ItemGroup> response = itemSelectionService.getNextItemGroup(examid, request.isMsb());

        if(response.getResponseData().isPresent()) {
            throw new RuntimeException("Failed to create item group: " + response.getErrorMessage());
        } else if (!response.getResponseData().isPresent()) {
            throw new IllegalStateException("No error nor item information was returned from selection.  Please check configuration");
        }

        ItemGroup itemGroup = response.getResponseData().get();

        UUID pageId = UUID.randomUUID();

        ExamPage page = new ExamPage.Builder()
            .withId(pageId)
            .withPagePosition(request.getLastPage() + 1)
            .withExamId(examid)
            .withItemGroupKey(itemGroup.getGroupID())
            .withSegmentKey(itemGroup.getSegmentKey())
            .withSegmentId(itemGroup.getSegmentID())
            .withSegmentPosition(itemGroup.getSegmentPosition())
            .build();

        List<ExamItem> examItems = itemGroup.getItems().stream()
            .map(testItem -> new ExamItem.Builder(UUID.randomUUID())
                .withItemKey(testItem.getItemID())
                .withExamPageId(pageId)
                .withFieldTest(testItem.isFieldTest())
                .withPosition(testItem.position)
                .withRequired(testItem.isRequired())
                .build())
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

        pageGroup = PageGroup.Create(opportunityItems);
        return pageGroup;
    }
}
