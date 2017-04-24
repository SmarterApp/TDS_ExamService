package tds.exam.services.item.selection;

import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.dll.api.IItemSelectionDLL;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExpandableExamService;
import tds.exam.services.FieldTestService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;

@Service
public class ItemCandidateServiceImpl implements ItemCandidatesService {
    private final ExpandableExamService expandableExamService;
    private final FieldTestService fieldTestService;
    private final ExamSegmentService examSegmentService;
    private final AssessmentService assessmentService;
    private final SegmentService segmentService;

    private final static String ADAPTIVE = "adaptive";
    private final static String FIELD_TEST = "fieldtest";

    @Autowired
    public ItemCandidateServiceImpl(final ExpandableExamService expandableExamService, final FieldTestService fieldTestService, final ExamSegmentService examSegmentService, final AssessmentService assessmentService, final SegmentService segmentService) {
        this.expandableExamService = expandableExamService;
        this.fieldTestService = fieldTestService;
        this.examSegmentService = examSegmentService;
        this.assessmentService = assessmentService;
        this.segmentService = segmentService;
    }

    @Override
    public ItemCandidatesData getItemCandidates(UUID examId) throws ReturnStatusException {
        //Port of ItemSelectionDLL.AA_GetNextItemCandidates_SP
        return getAllItemCandidates(examId).get(0);
    }

    @Override
    public List<ItemCandidatesData> getAllItemCandidates(UUID examId) throws ReturnStatusException {
        //Port of ItemSelectionDLL.AA_GetNextItemCandidates_SP
        ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new ReturnStatusException("Could not find Exam for id" + examId));

        Assessment assessment = assessmentService.findAssessment(exam.getExam().getClientName(), exam.getExam().getAssessmentKey())
            .orElseThrow(() -> new ReturnStatusException("Could not find assessment for " + exam.getExam().getAssessmentKey()));

        List<SegmentHolder> segments = mapSegmentToItems(exam);

        List<SegmentHolder> unsatisfiedSegments = segments.stream()
            .filter(segmentHolder -> !segmentHolder.examSegment.isSatisfied())
            .collect(Collectors.toList());

        final List<FieldTestItemGroup> fieldTestItemGroups = fieldTestService.findUsageInExam(examId);

        //Only care about non deleted nor administered field tests
        final Map<String, List<FieldTestItemGroup>> fieldTestItemsBySegmentKey = fieldTestItemGroups.stream()
            .filter(fieldTestItemGroup -> !fieldTestItemGroup.isDeleted() && fieldTestItemGroup.getAdministeredAt() == null)
            .collect(Collectors.groupingBy(FieldTestItemGroup::getSegmentKey));

        if (unsatisfiedSegments.isEmpty()) {
            return Collections.singletonList(new ItemCandidatesData(examId, IItemSelectionDLL.SATISFIED));
        }

        List<ExamSegment> satisfiedSegments = new ArrayList<>();
        List<SegmentHolder> nonSatisfiedSegments = new ArrayList<>();
        for (SegmentHolder segment : unsatisfiedSegments) {
            long nonFieldTestItemCount = segment.items.stream()
                .filter(examItem -> !examItem.isFieldTest())
                .count();

            long fieldTestItemCount = segment.items.stream()
                .filter(ExamItem::isFieldTest)
                .count();

            final ExamSegment examSegment = segment.examSegment;
            boolean fieldTestItemsSatisfied = fieldTestItemsBySegmentKey.containsKey(examSegment.getSegmentKey())
                && fieldTestItemsBySegmentKey.get(examSegment.getSegmentKey()).isEmpty();

            if (examSegment.getAlgorithm().getType().contains(ADAPTIVE)
                && examSegment.getExamItemCount() == nonFieldTestItemCount
                && fieldTestItemsSatisfied) {
                satisfiedSegments.add(examSegment);
            } else if (examSegment.getAlgorithm().equals(Algorithm.FIXED_FORM)
                && examSegment.getExamItemCount() == (nonFieldTestItemCount + fieldTestItemCount)) {
                satisfiedSegments.add(examSegment);
            } else {
                nonSatisfiedSegments.add(segment);
            }
        }

        //Means that a segment has been satisfied but hasn't been updated to be satisfied
        if (!satisfiedSegments.isEmpty()) {
            List<ExamSegment> segmentsToUpdate = satisfiedSegments.stream()
                .map(examSegment -> ExamSegment.Builder
                    .fromSegment(examSegment)
                    .withSatisfied(true)
                    .build())
                .collect(Collectors.toList());

            examSegmentService.update(segmentsToUpdate.toArray(new ExamSegment[segmentsToUpdate.size()]));
        }

        return nonSatisfiedSegments.stream()
            .map(segmentHolder -> {
                ExamSegment examSegment = segmentHolder.examSegment;

                boolean isFieldTest = segmentHolder.items.stream()
                    .filter(ExamItem::isFieldTest)
                    .count() > 0;

                GroupBlock groupBlock = findGroupBlock(examSegment.getSegmentKey(),
                    assessment,
                    fieldTestItemGroups,
                    isFieldTest,
                    exam.getExam().getLanguageCode(),
                    segmentHolder);

                return new ItemCandidatesData(
                    examSegment.getExamId(),
                    isFieldTest ? FIELD_TEST : examSegment.getAlgorithm().getType(),
                    examSegment.getSegmentKey(),
                    examSegment.getSegmentId(),
                    examSegment.getSegmentPosition(),
                    groupBlock.group,
                    groupBlock.block,
                    exam.getExam().getSessionId(),
                    false,
                    //TODO - Find out where isActive is coming from in legacy code
                    null);
            })
            .collect(Collectors.toList());
    }

    @Override
    public void cleanupDismissedItemCandidates(Long selectedSegmentPosition, UUID examId) throws ReturnStatusException {
        List<ExamSegment> segments = examSegmentService.findExamSegments(examId)
            .stream()
            .filter(examSegment -> !examSegment.isSatisfied())
            .filter(examSegment -> examSegment.getSegmentPosition() != selectedSegmentPosition.intValue())
            .map(examSegment -> ExamSegment.Builder.fromSegment(examSegment)
                .withSatisfied(true)
                .build())
            .collect(Collectors.toList());

        examSegmentService.update(segments.toArray(new ExamSegment[segments.size()]));
    }

    @Override
    public ItemGroup getItemGroup(UUID examId, String segmentKey, String groupID, String blockID, Boolean isFieldTest) throws ReturnStatusException {
        try {
            TestSegment testSegment = segmentService.getSegment(segmentKey);
            return testSegment.getPool().getItemGroup(groupID);
        } catch (Exception e) {
            throw new ReturnStatusException(e);
        }
    }

    @Override
    public StudentHistory2013 loadOppHistory(UUID examId, String segmentKey) throws ItemSelectionException {
        //AA_GetDataHistory2_SP
        ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new ItemSelectionException("Could not find Exam for id" + examId));

        Assessment assessment = assessmentService.findAssessment(exam.getExam().getClientName(), exam.getExam().getAssessmentKey())
            .orElseThrow(() -> new ItemSelectionException("Can't find assessment for the exam"));

        ExamSegment examSegment = exam.getExamSegments()
            .stream()
            .filter(examSegment1 -> examSegment1.getSegmentKey().equals(segmentKey))
            .findFirst()
            .orElseThrow(() -> new ItemSelectionException("Could not find exam segment"));

        Set<String> fieldTestItemGroups = fieldTestService.findUsageInExam(examId)
            .stream()
            .filter(fieldTestItemGroup -> !fieldTestItemGroup.isDeleted())
            .map(FieldTestItemGroup::getGroupId)
            .collect(Collectors.toSet());

        ArrayList<String> itemGroups = new ArrayList<>();
        itemGroups.addAll(examSegment.getItemPool());
        StudentHistory2013 history = new StudentHistory2013();
        history.setStartAbility(assessment.getStartAbility());
        history.set_itemPool(itemGroups);

        //TODO - we need to fetch past item group selections based on responses.  However, that is really a pain to do with our
        //current data model since you can't easily get from an exam to a item.  You always have to go through page.
        //Revisit this in the future.  Ignoring that part in the legacy code

        HashSet<String> ftFieldGroups = new HashSet<>();
        ftFieldGroups.addAll(fieldTestItemGroups);
        history.set_previousFieldTestItemGroups(ftFieldGroups);

        List<SegmentHolder> segmentHolders = mapSegmentToItems(exam);

        ArrayList<ItemResponse> itemResponses = new ArrayList<>();
        for(final SegmentHolder segmentHolder : segmentHolders) {
            List<ItemResponse> responses = segmentHolder.items.stream()
                .filter(examItem -> examItem.getItemKey() != null)
                .map(examItem -> {
                    ItemResponse response = new ItemResponse();
                    response.segmentPosition = segmentHolder.examSegment.getSegmentPosition();
                    response.itemID = examItem.getItemKey();
                    //TODO - add group id to items
//                    response.groupID = examItem.getGroupId();
                    response.itemPosition = examItem.getPosition();
                    response.isFieldTest = examItem.isFieldTest();

                    if(examItem.getResponse().isPresent()) {
                        ExamItemResponse examItemResponse = examItem.getResponse().get();

                        if(examItemResponse.getScore().isPresent()) {
                            ExamItemResponseScore score = examItemResponse.getScore().get();
                            response.setScore(score.getScore());
                            response.setScoreDimensions(score.getScoringDimensions());
                            response.loadDimensionScores(score.getScoringDimensions());
                        }
                    }

                    return response;
                }).collect(Collectors.toList());

            itemResponses.addAll(responses);
        }

        history.set_previousResponses(itemResponses);

        return history;
    }

    @Override
    public boolean setSegmentSatisfied(UUID examId, Integer segmentPosition, String reason) throws ReturnStatusException {
        Optional<ExamSegment> maybeExamSegment = examSegmentService.findByExamIdAndSegmentPosition(examId, segmentPosition);
        if(!maybeExamSegment.isPresent()) {
            return false;
        }

        if(!maybeExamSegment.get().isSatisfied()) {
            ExamSegment examSegment = ExamSegment.Builder.fromSegment(maybeExamSegment.get()).withSatisfied(true).build();
            examSegmentService.update(examSegment);
        }

        return true;
    }

    @Override
    public String addOffGradeItems(UUID examId, String designation, String segmentKey, _Ref<String> reason) throws ReturnStatusException {
        //AA_AddOffgradeItems_SP
        //TODO - figure out if this is really necessary
        return "";
    }

    private List<SegmentHolder> mapSegmentToItems(ExpandableExam expandableExam) {
        List<ExamSegment> segments = expandableExam.getExamSegments();
        List<ExamItem> items = expandableExam.getExamItems();

        List<SegmentHolder> segmentHolders = segments.stream()
            .map(SegmentHolder::new)
            .collect(Collectors.toList());

        for (ExamPage page : expandableExam.getExamPages()) {
            for (SegmentHolder holder : segmentHolders) {
                if (holder.isPageInSegment(page)) {
                    holder.addPageId(page.getId());
                }
            }
        }

        for (ExamItem examItem : items) {
            for (SegmentHolder holder : segmentHolders) {
                if (holder.isItemInSegment(examItem)) {
                    holder.addItem(examItem);
                }
            }
        }

        return segmentHolders;
    }

    private GroupBlock findGroupBlock(final String segmentKey,
                                      final Assessment assessment,
                                      final List<FieldTestItemGroup> fieldTestItemGroups,
                                      final boolean isFieldTest,
                                      final String languageCode,
                                      final SegmentHolder segmentHolder) {
        //Port of ItemSelectionDLL.ValidateAndReturnSegmentData get group and block data
        String groupId = "";
        String blockId = "";

        Segment segment = assessment.getSegment(segmentKey);

        //This needs ot look at exam items
        Optional<ExamItem> maybeMinItem = segmentHolder.items.stream()
            .min(Comparator.comparingInt(ExamItem::getPosition));

        Optional<ExamItem> maybeMaxItem = segmentHolder.items.stream()
            .max(Comparator.comparingInt(ExamItem::getPosition));

        int firstPosition = 0;
        int lastPosition = -1;
        if (maybeMaxItem.isPresent() && maybeMinItem.isPresent()) {
            firstPosition = maybeMinItem.get().getPosition();
            lastPosition = maybeMaxItem.get().getPosition();
        }

        int relativePosition = lastPosition + 1 - firstPosition + 1;

        if (segment.getSelectionAlgorithm().equals(Algorithm.FIXED_FORM)) {
            Optional<Item> maybeItem = segment.getItems().stream().filter(item -> item.getPosition() == relativePosition).findFirst();

            if (maybeItem.isPresent()) {
                groupId = maybeItem.get().getGroupId();
                blockId = maybeItem.get().getBlockId();
            }
        } else if (segment.getSelectionAlgorithm().equals(Algorithm.ADAPTIVE_2) && isFieldTest) {
            Optional<FieldTestItemGroup> maybeFieldTestItem = fieldTestItemGroups.stream()
                .filter(fieldTestItemGroup -> fieldTestItemGroup.getLanguageCode().equals(languageCode))
                .findFirst();

            if (maybeFieldTestItem.isPresent()) {
                groupId = maybeFieldTestItem.get().getGroupId();
                blockId = maybeFieldTestItem.get().getBlockId();
            }
        }

        return new GroupBlock(groupId, blockId);
    }

    private class SegmentHolder {
        private final ExamSegment examSegment;
        private Set<UUID> pageIds = new HashSet<>();
        private List<ExamItem> items = new ArrayList<>();

        SegmentHolder(final ExamSegment examSegment) {
            this.examSegment = examSegment;
        }

        void addPageId(final UUID pageId) {
            pageIds.add(pageId);
        }

        void addItem(final ExamItem item) {
            items.add(item);
        }

        boolean isPageInSegment(final ExamPage examPage) {
            return examSegment.getSegmentKey().equals(examPage.getSegmentKey())
                && examSegment.getSegmentPosition() == examPage.getSegmentPosition();
        }

        boolean isItemInSegment(final ExamItem examItem) {
            return pageIds.contains(examItem.getExamPageId());
        }
    }

    private class GroupBlock {
        private final String group;
        private final String block;

        GroupBlock(final String group, final String block) {
            this.group = group;
            this.block = block;
        }
    }
}
