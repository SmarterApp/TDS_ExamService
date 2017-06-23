/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.item.selection;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.models.ExamAccommodationFilter;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.models.ItemGroupHistory;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamHistoryService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamService;
import tds.exam.services.ExpandableExamService;
import tds.exam.services.FieldTestService;
import tds.exam.services.ItemPoolService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.model.OffGradeResponse;
import tds.itemselection.services.ItemCandidatesService;

@Service
public class ItemCandidateServiceImpl implements ItemCandidatesService {
    private final ExpandableExamService expandableExamService;
    private final FieldTestService fieldTestService;
    private final ExamSegmentService examSegmentService;
    private final AssessmentService assessmentService;
    private final ExamHistoryService examHistoryService;
    private final ExamAccommodationService examAccommodationService;
    private final ItemPoolService itemPoolService;
    private final ExamService examService;

    private final static String ADAPTIVE = "adaptive";
    private final static String FIELD_TEST = "fieldtest";
    private final static String OFF_GRADE_ACCOMMODATION_TYPE = "TDSPoolFilter";

    @Autowired
    public ItemCandidateServiceImpl(final ExpandableExamService expandableExamService,
                                    final FieldTestService fieldTestService,
                                    final ExamSegmentService examSegmentService,
                                    final AssessmentService assessmentService,
                                    final ExamHistoryService examHistoryService,
                                    final ExamAccommodationService examAccommodationService,
                                    final ItemPoolService itemPoolService,
                                    final ExamService examService) {
        this.expandableExamService = expandableExamService;
        this.fieldTestService = fieldTestService;
        this.examSegmentService = examSegmentService;
        this.assessmentService = assessmentService;
        this.examHistoryService = examHistoryService;
        this.examAccommodationService = examAccommodationService;
        this.itemPoolService = itemPoolService;
        this.examService = examService;
    }

    @Override
    public ItemCandidatesData getItemCandidates(UUID examId) throws ReturnStatusException {
        // This method will only return the ItemCandidates for the first unsatisfied segment
        // Port of ItemSelectionDLL.AA_GetNextItemCandidates_SP
        ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new ReturnStatusException("Could not find Exam for id" + examId));

        Assessment assessment = assessmentService.findAssessment(exam.getExam().getClientName(), exam.getExam().getAssessmentKey())
            .orElseThrow(() -> new ReturnStatusException("Could not find assessment for " + exam.getExam().getAssessmentKey()));

        final List<FieldTestItemGroup> fieldTestItemGroups = fieldTestService.findUsageInExam(examId);
        ExamSegmentInfo info = findExamSegmentsNotYetSatisfied(exam, fieldTestItemGroups);

        //Means that a segment has been satisfied but hasn't been updated to be satisfied
        if (!info.getNewlySatisfiedSegments().isEmpty()) {
            updateSatisfiedSegments(info.getNewlySatisfiedSegments());
        }

        return info.getUnsatisfiedSegments().stream()
            .map(examSegmentWrapper -> convertSegmentHolderToItemCandidateData(exam, assessment, fieldTestItemGroups, examSegmentWrapper))
            .findFirst().orElse(new ItemCandidatesData(examId, AlgorithmType.SATISFIED.getType()));
    }

    @Override
    public List<ItemCandidatesData> getAllItemCandidates(UUID examId) throws ReturnStatusException {
        // This method will need to return all item candidates, regardless of whether or not these segments are satisfied.
        List<ExamSegmentWrapper> examSegments = Collections.emptyList();
        final ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS, ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS)
            .orElseThrow(() -> new ReturnStatusException("Could not find Exam for id" + examId));

        final Assessment assessment = assessmentService.findAssessment(exam.getExam().getClientName(), exam.getExam().getAssessmentKey())
            .orElseThrow(() -> new ReturnStatusException("Could not find assessment for " + exam.getExam().getAssessmentKey()));
        final List<FieldTestItemGroup> fieldTestItemGroups = fieldTestService.findUsageInExam(exam.getExam().getId());
        ExamSegmentInfo info = findExamSegmentsNotYetSatisfied(exam, fieldTestItemGroups);

        //Means that a segment has been satisfied but hasn't been updated to be satisfied
        if (!info.getNewlySatisfiedSegments().isEmpty()) {
            examSegments = updateSatisfiedSegments(info.getNewlySatisfiedSegments());
        }

        List<ExamSegmentWrapper> nonSatisfiedSegments = info.getUnsatisfiedSegments();
        nonSatisfiedSegments.addAll(examSegments);

        return nonSatisfiedSegments.stream()
            .sorted(Comparator.comparingInt(o -> o.getExamSegment().getSegmentPosition()))
            .map(examSegmentWrapper -> convertSegmentHolderToItemCandidateData(exam, assessment, fieldTestItemGroups, examSegmentWrapper))
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
        ExpandableExam exam = expandableExamService.findExam(examId, ExpandableExamAttributes.EXAM_SEGMENTS)
            .orElseThrow(() -> new ReturnStatusException("Could not find Exam for id" + examId));

        ExamSegment examSegment = exam.getExamSegments().stream()
            .filter(segment -> segment.getSegmentKey().equals(segmentKey))
            .findFirst()
            .orElseThrow(() -> new ReturnStatusException(String.format("Could not find exam segment with exam id %s and segment %s", examId, segmentKey)));

        Assessment assessment = assessmentService.findAssessment(exam.getExam().getClientName(), exam.getExam().getAssessmentKey())
            .orElseThrow(() -> new ReturnStatusException("Could not find assessment for " + exam.getExam().getAssessmentKey()));

        Segment segment = assessment.getSegment(segmentKey);

        Optional<tds.assessment.ItemGroup> maybeItemGroup = segment.getItemGroups().stream()
            .filter(itemGroup1 -> itemGroup1.getGroupId().equals(groupID))
            .findFirst();

        ItemGroup itemGroup = null;

        if (maybeItemGroup.isPresent()) {
            itemGroup = new ItemGroup();
            itemGroup.setGroupID(maybeItemGroup.get().getGroupId());
            itemGroup.setNumberOfItemsRequired(maybeItemGroup.get().getRequiredItemCount());
            itemGroup.setMaximumNumberOfItems(maybeItemGroup.get().getMaxItems());
        }

        if (segment.getSelectionAlgorithm().equals(Algorithm.FIXED_FORM)) {
            itemGroup = getItemGroupForFixedForm(groupID, exam, examSegment, segment, itemGroup);
        }

        return itemGroup;
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

        history.set_previousTestItemGroups(getPreviousItemGroups(examId, exam.getExam().getStudentId(), exam.getExam().getAssessmentId()));

        HashSet<String> ftFieldGroups = new HashSet<>();
        ftFieldGroups.addAll(fieldTestItemGroups);
        history.set_previousFieldTestItemGroups(ftFieldGroups);

        List<ExamSegmentWrapper> segmentHolders = mapSegmentToItems(exam);

        ArrayList<ItemResponse> itemResponses = new ArrayList<>();
        for (final ExamSegmentWrapper segmentHolder : segmentHolders) {
            List<ItemResponse> responses = segmentHolder.getItems().stream()
                .filter(examItem -> examItem.getItemKey() != null)
                .map(examItem -> {
                    ItemResponse response = new ItemResponse();
                    response.segmentPosition = segmentHolder.getExamSegment().getSegmentPosition();
                    response.itemID = examItem.getItemKey();
                    response.groupID = examItem.getGroupId();
                    response.itemPosition = examItem.getPosition();
                    response.isFieldTest = examItem.isFieldTest();

                    if (examItem.getResponse().isPresent()) {
                        ExamItemResponse examItemResponse = examItem.getResponse().get();

                        if (examItemResponse.getScore().isPresent()) {
                            ExamItemResponseScore score = examItemResponse.getScore().get();
                            response.setScore(score.getScore());
                            response.setScoreDimensions(score.getScoringDimensions());
                            response.loadDimensionScores(score.getScoringDimensions());
                        } else {
                            //Legacy scores are -1 not null
                            response.setScore(-1);
                        }
                    }

                    //Legacy doesn't handle null and expects non scored items to be -1 score
                    if(response.getScore() == null) {
                        response.setScore(-1);
                    }

                    return response;
                }).collect(Collectors.toList());

            itemResponses.addAll(responses);
        }

        history.set_previousResponses(itemResponses);

        return history;
    }

    /**
     * Legacy implementation conversion.  The legacy objects use implementation classes rather than interfaces
     */
    private ArrayList<HashSet<String>> getPreviousItemGroups(final UUID examId, final long studentId, final String assessmentId) {
        List<ItemGroupHistory> histories = examHistoryService.findPreviousItemGroups(studentId, examId, assessmentId);

        ArrayList<HashSet<String>> itemGroups = new ArrayList<>();

        for(ItemGroupHistory history : histories) {
            HashSet<String> itemGroupSet = new HashSet<>();
            itemGroupSet.addAll(history.getItemGroupIds());
            itemGroups.add(itemGroupSet);
        }

        return itemGroups;
    }

    @Override
    public boolean setSegmentSatisfied(UUID examId, Integer segmentPosition, String reason) throws ReturnStatusException {
        Optional<ExamSegment> maybeExamSegment = examSegmentService.findByExamIdAndSegmentPosition(examId, segmentPosition);
        if (!maybeExamSegment.isPresent()) {
            return false;
        }

        if (!maybeExamSegment.get().isSatisfied()) {
            ExamSegment examSegment = ExamSegment.Builder.fromSegment(maybeExamSegment.get()).withSatisfied(true).build();
            examSegmentService.update(examSegment);
        }

        return true;
    }

    @Override
    @Transactional
    public OffGradeResponse addOffGradeItems(UUID examId, String designation, String segmentKey) throws ReturnStatusException {
        //ItemSelectionDLL.AA_AddOffgradeItems_SP

        final Optional<OffGradeResponse> response = updateOffGradeAccommodations(examId, designation);
        if(response.isPresent()) {
            //If present it means that either off grade has already been done or that it isn't present.
            return response.get();
        }

        //The or else throw part should never happen and means the system is in a bad state.
        final Exam exam = examService.findExam(examId).orElseThrow(() -> new IllegalStateException("Partial exam data in DB.  Failed to find an exam for id " + examId));
        final List<ExamSegment> examSegments = examSegmentService.findExamSegments(examId);
        final Assessment assessment = assessmentService.findAssessment(exam.getClientName(), exam.getAssessmentKey())
            .orElseThrow(() -> new NotFoundException("Could not find assessment for id " +exam.getAssessmentKey()));

        final String languageCode = exam.getLanguageCode();

        Map<String, Segment> segmentKeyToSegment = assessment.getSegments().stream()
            .collect(Collectors.toMap(Segment::getKey, Function.identity()));

        ExamSegment[] segmentsToUpdate = examSegments.stream()
            .filter(examSegment -> !examSegment.isSatisfied() && examSegment.getAlgorithm().equals(Algorithm.ADAPTIVE_2))
            .map(examSegment -> {
                Segment segment = segmentKeyToSegment.get(examSegment.getSegmentKey());
                Set<Item> itemPool = itemPoolService.getItemPool(examId, assessment.getItemConstraints(), segment.getItems(languageCode));
                Set<String> itemPoolIds = itemPool.stream()
                    .map(Item::getId)
                    .collect(Collectors.toSet());

                itemPoolIds.addAll(examSegment.getItemPool());

                return ExamSegment.Builder.fromSegment(examSegment).withItemPool(itemPoolIds).build();
            }).toArray(ExamSegment[]::new);

        examSegmentService.update(segmentsToUpdate);

        return new OffGradeResponse(OffGradeResponse.SUCCESS, "");
    }

    private Optional<OffGradeResponse> updateOffGradeAccommodations(final UUID examId, final String designation) {
        String inAccommodationCode = designation + " IN";
        String outAccommodationCode = designation+ " OUT";

        Collection<ExamAccommodationFilter> filters = Arrays.asList(
          new ExamAccommodationFilter(inAccommodationCode, OFF_GRADE_ACCOMMODATION_TYPE),
          new ExamAccommodationFilter(outAccommodationCode, OFF_GRADE_ACCOMMODATION_TYPE)
        );

        List<ExamAccommodation> examAccommodations = examAccommodationService.findAccommodations(examId, filters);

        if(examAccommodations.isEmpty()) {
            return Optional.of(new OffGradeResponse(OffGradeResponse.FAILED, "offgrade accommodation not exists"));
        }

        Map<String, List<ExamAccommodation>> examAccommodationsByCode = examAccommodations.stream()
            .collect(Collectors.groupingBy(ExamAccommodation::getCode));

        if (examAccommodationsByCode.containsKey(inAccommodationCode)) {
            return Optional.of(new OffGradeResponse(OffGradeResponse.SUCCESS, "already set"));
        }

        //ItemSelectionDLL lin 3722
        if(examAccommodationsByCode.containsKey(outAccommodationCode)) {
            List<ExamAccommodation> accommodationsToUpdate = examAccommodationsByCode.get(outAccommodationCode).stream()
                .map(examAccommodation -> ExamAccommodation.Builder
                    .fromExamAccommodation(examAccommodation)
                    .withCode(inAccommodationCode)
                    .withValue(inAccommodationCode)
                    .build())
                .collect(Collectors.toList());

            examAccommodationService.update(accommodationsToUpdate);
        }

        return Optional.empty();
    }

    private List<ExamSegmentWrapper> mapSegmentToItems(ExpandableExam expandableExam) {
        List<ExamSegment> segments = expandableExam.getExamSegments();
        List<ExamItem> items = expandableExam.getExamItems();

        List<ExamSegmentWrapper> examSegments = segments.stream()
            .map(ExamSegmentWrapper::new)
            .collect(Collectors.toList());

        for (ExamPage page : expandableExam.getExamPages()) {
            for (ExamSegmentWrapper examSegment : examSegments) {
                if (examSegment.isPageInSegment(page)) {
                    examSegment.addPageId(page.getId());
                }
            }
        }

        for (ExamItem examItem : items) {
            for (ExamSegmentWrapper examSegment : examSegments) {
                if (examSegment.isItemInSegment(examItem)) {
                    examSegment.addItem(examItem);
                }
            }
        }

        return examSegments;
    }

    private GroupBlock findGroupBlock(final Assessment assessment,
                                      final List<FieldTestItemGroup> fieldTestItemGroups,
                                      final boolean isFieldTest,
                                      final String languageCode,
                                      final ExamSegmentWrapper examSegmentWrapper) {
        //Port of ItemSelectionDLL.ValidateAndReturnSegmentData get group and block data
        String groupId = "";
        String blockId = "";

        ExamSegment examSegment = examSegmentWrapper.getExamSegment();
        Segment segment = assessment.getSegment(examSegment.getSegmentKey());

        if (segment.getSelectionAlgorithm().equals(Algorithm.FIXED_FORM)) {
            com.google.common.base.Optional<Form> maybeForm = segment.getForm(languageCode, examSegment.getFormCohort());

            if (maybeForm.isPresent()) {
                Form form = maybeForm.get();
                int position = examSegmentWrapper.getItems().size();
                groupId = form.getItems().get(position).getGroupId();
                blockId = form.getItems().get(position).getBlockId();
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

    private ItemGroup getItemGroupForFixedForm(final String groupID, final ExpandableExam exam, final ExamSegment examSegment, final Segment segment, ItemGroup itemGroup) {
        com.google.common.base.Optional<Form> maybeForm = segment.getForm(exam.getExam().getLanguageCode(), examSegment.getFormCohort());
        if (!maybeForm.isPresent()) {
            throw new IllegalStateException(String.format("Could not find a form with language %s and cohort %s", exam.getExam().getLanguageCode(), examSegment.getFormCohort()));
        }


        List<TestItem> items = maybeForm.get().getItems().stream()
            .map(ItemSelectionMappingUtility::convertItem)
            .filter(testItem -> {
                String itemGroupId = testItem.getGroupID();

                if (StringUtils.isEmpty(itemGroupId)) {
                    itemGroupId = "I-".concat(testItem.getItemID());
                }

                return itemGroupId.equals(groupID);
            })
            .collect(Collectors.toList());

        if (itemGroup == null) {
            itemGroup = new ItemGroup();
            itemGroup.setGroupID(groupID);
        }

        items.forEach(itemGroup::addItem);
        return itemGroup;
    }

    private ItemCandidatesData convertSegmentHolderToItemCandidateData(final ExpandableExam exam, final Assessment assessment, final List<FieldTestItemGroup> fieldTestItemGroups, final ExamSegmentWrapper examSegmentWrapper) {
        ExamSegment examSegment = examSegmentWrapper.getExamSegment();

        boolean isFieldTest = examSegmentWrapper.getItems().stream()
            .filter(ExamItem::isFieldTest)
            .count() > 0;

        GroupBlock groupBlock = findGroupBlock(assessment,
            fieldTestItemGroups,
            isFieldTest,
            exam.getExam().getLanguageCode(),
            examSegmentWrapper);

        return new ItemCandidatesData(
            examSegment.getExamId(),
            isFieldTest ? FIELD_TEST : examSegment.getAlgorithm().getType(),
            examSegment.getSegmentKey(),
            examSegment.getSegmentId(),
            examSegment.getSegmentPosition(),
            groupBlock.getItemGroupId(),
            groupBlock.getBlock(),
            exam.getExam().getSessionId(),
            false,
            !examSegment.isSatisfied());
    }

    private List<ExamSegmentWrapper> updateSatisfiedSegments(final List<ExamSegmentWrapper> satisfiedSegments) {
        List<ExamSegment> segments = new ArrayList<>();

        for (ExamSegmentWrapper wrapper : satisfiedSegments) {
            ExamSegment newSegment = ExamSegment.Builder
                .fromSegment(wrapper.getExamSegment())
                .withSatisfied(true)
                .build();

            segments.add(newSegment);
            wrapper.setExamSegment(newSegment);
        }

        examSegmentService.update(segments.toArray(new ExamSegment[segments.size()]));

        return satisfiedSegments;
    }

    private ExamSegmentInfo findExamSegmentsNotYetSatisfied(final ExpandableExam exam, final List<FieldTestItemGroup> fieldTestItemGroups) throws ReturnStatusException {
        //Port of ItemSelectionDLL.AA_GetNextItemCandidates_SP
        List<ExamSegmentWrapper> segments = mapSegmentToItems(exam);
        List<ExamSegmentWrapper> unsatisfiedSegments = segments.stream()
            .filter(segmentHolder -> !segmentHolder.getExamSegment().isSatisfied())
            .collect(Collectors.toList());

        //Only care about non deleted nor administered field tests
        final Map<String, List<FieldTestItemGroup>> fieldTestItemsBySegmentKey = fieldTestItemGroups.stream()
            .filter(fieldTestItemGroup -> !fieldTestItemGroup.isDeleted() && fieldTestItemGroup.getAdministeredAt() == null)
            .collect(Collectors.groupingBy(FieldTestItemGroup::getSegmentKey));

        List<ExamSegmentWrapper> satisfiedSegments = new ArrayList<>();
        List<ExamSegmentWrapper> nonSatisfiedSegments = new ArrayList<>();
        for (ExamSegmentWrapper examSegmentWrapper : unsatisfiedSegments) {
            long nonFieldTestItemCount = examSegmentWrapper.getItems().stream()
                .filter(examItem -> !examItem.isFieldTest())
                .count();

            long fieldTestItemCount = examSegmentWrapper.getItems().stream()
                .filter(ExamItem::isFieldTest)
                .count();

            final ExamSegment examSegment = examSegmentWrapper.getExamSegment();
            boolean fieldTestItemsSatisfied = !fieldTestItemsBySegmentKey.containsKey(examSegment.getSegmentKey())
                || fieldTestItemsBySegmentKey.get(examSegment.getSegmentKey()).isEmpty();

            if (examSegment.getAlgorithm().getType().contains(ADAPTIVE)
                && examSegment.getExamItemCount() == nonFieldTestItemCount
                && fieldTestItemsSatisfied) {
                satisfiedSegments.add(examSegmentWrapper);
            } else if (examSegment.getAlgorithm().equals(Algorithm.FIXED_FORM)
                && examSegment.getExamItemCount() == (nonFieldTestItemCount + fieldTestItemCount)) {
                satisfiedSegments.add(examSegmentWrapper);
            } else {
                nonSatisfiedSegments.add(examSegmentWrapper);
            }
        }

        return new ExamSegmentInfo(satisfiedSegments, nonSatisfiedSegments);
    }

    /**
     * Contains Item group id and block
     */
    private static class GroupBlock {
        private final String itemGroupId;
        private final String block;

        GroupBlock(final String itemGroupId, final String block) {
            this.itemGroupId = itemGroupId;
            this.block = block;
        }

        String getItemGroupId() {
            return itemGroupId;
        }

        String getBlock() {
            return block;
        }
    }

    private class ExamSegmentInfo {
        private List<ExamSegmentWrapper> newlySatisfiedSegments;
        private List<ExamSegmentWrapper> unsatisfiedSegments;

        public ExamSegmentInfo(List<ExamSegmentWrapper> newlySatisfiedSegments,
                               List<ExamSegmentWrapper> unsatisfiedSegments) {
            this.newlySatisfiedSegments = newlySatisfiedSegments;
            this.unsatisfiedSegments = unsatisfiedSegments;
        }

        public List<ExamSegmentWrapper> getNewlySatisfiedSegments() {
            return newlySatisfiedSegments;
        }

        public List<ExamSegmentWrapper> getUnsatisfiedSegments() {
            return unsatisfiedSegments;
        }
    }
}
