package tds.exam.services.item.selection;

import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.common.Algorithm;
import tds.dll.api.IItemSelectionDLL;
import tds.exam.Exam;
import tds.exam.ExamItem;
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
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.services.ItemCandidatesService;

public class ItemCandidateServiceImpl implements ItemCandidatesService {
    private final ExpandableExamService expandableExamService;
    private final FieldTestService fieldTestService;
    private final ExamSegmentService examSegmentService;
    private final AssessmentService assessmentService;

    private final static String SATISFIED = "SATISFIED";
    private final static String FIXEDFORM = "fixedform";
    private final static String ADAPTIVE = "adaptive";
    private final static String FIELDTEST = "fieldtest";
    private final static String LIKESTRING_I = "I-";
    private final static String LANGUAGE = "Language";

    public ItemCandidateServiceImpl(final ExpandableExamService expandableExamService, final FieldTestService fieldTestService, final ExamSegmentService examSegmentService, final AssessmentService assessmentService) {
        this.expandableExamService = expandableExamService;
        this.fieldTestService = fieldTestService;
        this.examSegmentService = examSegmentService;
        this.assessmentService = assessmentService;
    }

    @Override
    public ItemCandidatesData getItemCandidates(UUID examId) throws ReturnStatusException {
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
            return new ItemCandidatesData(examId, IItemSelectionDLL.SATISFIED);
        }

        List<ExamSegment> satisfiedSegments = new ArrayList<>();
        List<ExamSegment> nonSatisfiedSegments = new ArrayList<>();
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
                nonSatisfiedSegments.add(examSegment);
            }
        }

        //Means that a segment has been satisfied but hasn't been updated to be satisfied
        if(!satisfiedSegments.isEmpty()) {
            List<ExamSegment> segmentsToUpdate = satisfiedSegments.stream()
                .map(examSegment -> ExamSegment.Builder
                    .fromSegment(examSegment)
                    .withSatisfied(true)
                    .build())
                .collect(Collectors.toList());

            examSegmentService.update(segmentsToUpdate.toArray(new ExamSegment[segmentsToUpdate.size()]));
        }

        return convert(exam.getExam(), nonSatisfiedSegments.subList(0, 1)).get(0);
    }

    private List<ItemCandidatesData> convert(final Exam exam, final Assessment assessment, boolean isFieldTest, final List<ExamSegment> segments) {
        String groupId = "";
        String blockId = "";

        if(assessment.getSelectionAlgorithm().equals(Algorithm.FIXED_FORM)) {

        } else if (assessment.getSelectionAlgorithm().equals(Algorithm.ADAPTIVE_2) && isFieldTest) {

        }

        return segments.stream()
            .map(examSegment -> new ItemCandidatesData(
                examSegment.getExamId(),
                examSegment.getAlgorithm().getType(),
                examSegment.getSegmentKey(),
                examSegment.getSegmentId(),
                examSegment.getSegmentPosition(),
                groupId,
                blockId,
                exam.getSessionId(),
                false,
                null
            )).collect(Collectors.toList());
    }

    @Override
    public List<ItemCandidatesData> getAllItemCandidates(UUID examId) throws ReturnStatusException {
        return null;
    }

    @Override
    public void cleanupDismissedItemCandidates(Long selectedSegmentPosition, UUID examId) throws ReturnStatusException {
        return;
    }

    @Override
    public ItemGroup getItemGroup(UUID examId, String segmentKey, String groupID, String blockID, Boolean isFieldTest) throws ReturnStatusException {
        return null;
    }

    @Override
    public StudentHistory2013 loadOppHistory(UUID examId, String segmentKey) throws ItemSelectionException {
        return null;
    }

    @Override
    public boolean setSegmentSatisfied(UUID examId, Integer segmentPosition, String reason) throws ReturnStatusException {
        return false;
    }

    @Override
    public String addOffGradeItems(UUID examId, String designation, String segmentKey, _Ref<String> reason) throws ReturnStatusException {
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
            return examSegment.getExamId().equals(examPage.getExamId())
                && examSegment.getSegmentPosition() == examPage.getSegmentPosition();
        }

        boolean isItemInSegment(final ExamItem examItem) {
            return pageIds.contains(examItem.getExamPageId());
        }
    }
}
