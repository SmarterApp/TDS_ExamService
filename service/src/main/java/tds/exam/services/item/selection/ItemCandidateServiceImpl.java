package tds.exam.services.item.selection;

import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.dll.api.IItemSelectionDLL;
import tds.exam.ExamItem;
import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExpandableExamService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.services.ItemCandidatesService;

public class ItemCandidateServiceImpl implements ItemCandidatesService {
    private final ExpandableExamService expandableExamService;

    public ItemCandidateServiceImpl(final ExpandableExamService expandableExamService) {
        this.expandableExamService = expandableExamService;
    }

    @Override
    public ItemCandidatesData getItemCandidates(UUID examId) throws ReturnStatusException {
        ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new ReturnStatusException("Could not find Exam for id" + examId));

        List<ExamSegment> unsatisfiedSegments = exam.getExamSegments().stream()
            .filter(examSegment -> !examSegment.isSatisfied()).collect(Collectors.toList());

        if (unsatisfiedSegments.isEmpty()) {
            return new ItemCandidatesData(examId, IItemSelectionDLL.SATISFIED);
        }


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

    private Map<ExamSegment, List<ExamItem>> mapSegmentToItems(ExpandableExam expandableExam) {
        List<ExamSegment> segments = expandableExam.getExamSegments();

        List<ExamItem> items = expandableExam.getExamItems();

        Map<UUID, List<ExamItem> itemsToPage = items.stream()
            .collect(Collectors.groupingBy(ExamItem::getExamPageId));

        Map<UUID, List<ExamItem>> itemsToSegment =
    }
}
