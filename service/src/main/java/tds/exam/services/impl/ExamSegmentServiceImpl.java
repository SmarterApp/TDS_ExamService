package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Form;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.ExamSegment;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.repositories.ExamSegmentCommandRepository;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.FieldTestService;
import tds.exam.services.FormSelector;
import tds.exam.services.SegmentPoolService;

@Service
public class ExamSegmentServiceImpl implements ExamSegmentService {
    private final ExamSegmentCommandRepository commandRepository;
    private final SegmentPoolService segmentPoolService;
    private final FormSelector formSelector;
    private final FieldTestService fieldTestService;

    @Autowired
    public ExamSegmentServiceImpl(ExamSegmentCommandRepository commandRepository,
                                  SegmentPoolService segmentPoolService,
                                  FormSelector formSelector,
                                  FieldTestService fieldTestService) {
        this.commandRepository = commandRepository;
        this.segmentPoolService = segmentPoolService;
        this.fieldTestService = fieldTestService;
        this.formSelector = formSelector;
    }

    /*
        This method is a rewrite of StudentDLL._InitializeTestSegments_SP() [starts line 4535].
        In legacy, this method is called from TestOpportunityServiceImpl line [435].
        In summary, initializeExamSegments() does the following:

         1. Loops over each Segment in the selected assessment, creating an exam-specific representation of the segment
         2. ExamSegment is populated with various pieces of data that are dependent on the selection algorithm
            a. fixed form segments contain form data, and the number of items is fixed based on the language
            b. adaptive segments contain a segment pool containing all possible items, as well as the number of items
               that need to be selected from the segment pool
         3. Inserts a record into exam_segments for each segment initialized
     */
    @Override
    public int initializeExamSegments(final Exam exam, final Assessment assessment) {
        /* StudentDLL [4538 - 4545] Checks if there are already exam_segments that exist for this examId. This method is the only
            place where the exam_segment table is inserted into, so this case is never possible. Skipping this logic.  */
        List<ExamSegment> examSegments = new ArrayList<>(); // The exam segments being initialized
        String formCohort = null;
        int totalItems = 0;
        //TODO: Check if this is a SIMULATION [4571]
        /* [4589] Skip language retrieval - now part of Exam */
        /* [4623-4636] and [4642-4648] can be skipped as we already have all the segments and assessment data we need */
        /* Segment loop starts at [4651] */
        for (Segment segment : assessment.getSegments()) {
            boolean isSatisfied = false;
            Set<String> itemPoolIds = new HashSet<>();
            int fieldTestItemCount = 0;
            int poolCount;
            Form selectedForm = null;
            SegmentPoolInfo segmentPoolInfo = null;

            /* NOTE: Skipping [4660-4678]. This segment of code just increments the position and re-iterates to the next
                segment. This is done in case the minimumSegmentPosition is not found in the temp table they create.
                That can never happen with our data structure, as it is always 1-based segment positioning. */
            if (Algorithm.FIXED_FORM == segment.getSelectionAlgorithm()) {
                /* If no form cohort is defined, this is likely the first form being selected */
                if (formCohort == null) {
                    Optional<Form> maybeSelectedForm = formSelector.selectForm(segment, exam.getLanguageCode());

                    if (!maybeSelectedForm.isPresent()) {
                        throw new IllegalStateException(String.format("Could not select a form for segment '%s' and language '%s'.",
                            segment.getKey(), exam.getLanguageCode()));
                    }

                    selectedForm = maybeSelectedForm.get();
                    formCohort = selectedForm.getCohort();
                } else {
                    com.google.common.base.Optional<Form> maybeSelectedForm;
                    maybeSelectedForm = segment.getForm(exam.getLanguageCode(), formCohort);

                    if (!maybeSelectedForm.isPresent()) {
                        throw new IllegalStateException(String.format("Could not select a form for segment '%s' with " +
                            "language '%s' and cohort '%s'.", segment.getKey(), exam.getLanguageCode(), formCohort));
                    }
                    selectedForm = maybeSelectedForm.get();
                }

                poolCount = selectedForm.getLength();
            } else { // Algorithm is adaptive2
                segmentPoolInfo = segmentPoolService.computeSegmentPool(exam.getId(),
                    segment, assessment.getItemConstraints(), exam.getLanguageCode());
                itemPoolIds = segmentPoolInfo.getItemPool().stream()
                    .map(item -> item.getId())
                    .collect(Collectors.toSet());
                poolCount = segmentPoolInfo.getPoolCount(); // poolCount does not always == itemPool.size

                /*  [4703] In legacy, opitemcnt = segment's max items. See lines [4624], [4630], [4672] */
                if (fieldTestService.isFieldTestEligible(exam, assessment, segment.getKey(), exam.getLanguageCode())
                    && segmentPoolInfo.getLength() == segment.getMaxItems()) {
                    fieldTestItemCount = fieldTestService.selectItemGroups(exam, assessment, segment.getKey(), exam.getLanguageCode());
                }

                isSatisfied = fieldTestItemCount + segmentPoolInfo.getLength() == 0;
            }

            // Keep track of the total number of items in the exam
            totalItems += (poolCount + fieldTestItemCount);
            /* Case statement within query in line [4712] - formLength is set to poolCount for fixed form on ln [4689] */
            int examItemCount = segment.getSelectionAlgorithm() == Algorithm.FIXED_FORM ? poolCount : segmentPoolInfo.getLength();

            examSegments.add(new ExamSegment.Builder()
                .withExamId(exam.getId())
                .withSegmentKey(segment.getKey())
                .withSegmentId(segment.getSegmentId())
                .withSegmentPosition(segment.getPosition())
                .withFormKey(selectedForm == null ? null : selectedForm.getKey())
                .withFormId(selectedForm == null ? null : selectedForm.getId())
                .withFormCohort(formCohort)
                .withAlgorithm(segment.getSelectionAlgorithm())
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(fieldTestItemCount)
                .withIsPermeable(false)
                .withIsSatisfied(isSatisfied)
                .withItemPool(itemPoolIds)
                .withPoolCount(poolCount)
                .build()
            );
        }
        /* Lines [4743-4750] */
        if (totalItems == 0) {
            throw new IllegalStateException("There are no items available in the item pool for any segment.");
        }
        /* Lines [4753-4764] */
        commandRepository.insert(examSegments);

        return totalItems;
    }
}
