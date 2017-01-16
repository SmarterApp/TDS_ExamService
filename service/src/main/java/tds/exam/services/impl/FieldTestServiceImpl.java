package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.FieldTestService;
import tds.session.ExternalSessionConfiguration;

@Service
public class FieldTestServiceImpl implements FieldTestService {
    private final FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository;
    private final FieldTestItemGroupCommandRepository fieldTestItemGroupCommandRepository;
    private final FieldTestItemGroupSelector fieldTestItemGroupSelector;

    @Autowired
    public FieldTestServiceImpl(FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository,
                                FieldTestItemGroupCommandRepository fieldTestItemGroupCommandRepository,
                                FieldTestItemGroupSelector fieldTestItemGroupSelector) {
        this.fieldTestItemGroupQueryRepository = fieldTestItemGroupQueryRepository;
        this.fieldTestItemGroupCommandRepository = fieldTestItemGroupCommandRepository;
        this.fieldTestItemGroupSelector = fieldTestItemGroupSelector;
    }


    @Override
    public boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey) {
        boolean isEligible = false;
        Segment currentSegment = assessment.getSegment(segmentKey);

        /* StudentDLL [4453] - ftminitems must be non-zero */
        if (currentSegment.getFieldTestMinItems() > 0) {
            // Check if there exists at least one field test item in the segment with the selected language
            /* StudentDLL [4430] */
            Optional<Item> fieldTestItem = currentSegment.getItems(exam.getLanguageCode()).stream()
                .filter(item -> item.isFieldTest())
                .findFirst();

             /* [4430 - 4442] checks to see if the segment contains at least one FT item */
            if (fieldTestItem.isPresent()) {
                if (ExternalSessionConfiguration.SIMULATION_ENVIRONMENT.equalsIgnoreCase(exam.getEnvironment())) {
                    isEligible = true;
                } else {
                    /* Line [4473 - 4471] : In legacy code, client_testproperties is queried to retrieve the assessment field test
                     date window. However, these properties are already included in Assessment object. In the legacy query,
                     a "null" field test start or end date is considered a valid and open field test window. */
                    boolean assessmentEligible = isWithinFieldTestWindow(assessment.getFieldTestStartDate(),
                        assessment.getFieldTestEndDate());
                    /* parentKey == testKey when the assessment is non-segmented */
                    if (!assessment.isSegmented() || !assessmentEligible) {
                        return assessmentEligible;
                    }
                    /* Line [4491] : In legacy code, client_segmentproperties is queried to retrieve the segment field test
                     date window. However, these properties are already included in Segment object. */
                    isEligible = isWithinFieldTestWindow(currentSegment.getFieldTestStartDate(),
                        currentSegment.getFieldTestEndDate());
                }
            }
        }

        return isEligible;
    }

    /*
        This code covers legacy StudentDLL._FT_SelectItemgroups_SP [line 3033] and is called by _InitializeTestSegments_SP [4704]
     */
    @Override
    public int selectItemGroups(final Exam exam, final Assessment assessment, final String segmentKey) {
        Random rng = new Random();
        Segment currentSegment = assessment.getSegment(segmentKey);
        List<FieldTestItemGroup> previouslyAssignedFieldTestItemGroups = fieldTestItemGroupQueryRepository.find(exam.getId(), segmentKey);

        /* StudentDLL [3119-3126] ftcount is just fieldTestItemGroups.size()
           [3126] - Skip, debug is always = 0                                       */
        Integer startPosition = currentSegment.getFieldTestStartPosition();
        Integer endPosition = currentSegment.getFieldTestEndPosition();
        int maxItems = currentSegment.getFieldTestMaxItems();
        int minItems = currentSegment.getFieldTestMinItems();
        int ftItemCount = previouslyAssignedFieldTestItemGroups.size(); // Initialize it as the size of the existing item groups assigned to this student

        /* [3131-3137] Note tht if endPos - startPos < numIntervals, the integer division below results in 0, which
        *  results in  a division-by-zero at line [3320] */
        int intervalSize = (endPosition - startPosition) / maxItems;
        int intervalIndex = startPosition; // keeps track of position in exam to administer ft item

        /* [3212- 3224] Skip Cohort code - the loader script hardcodes "ratio" and "cohortIndex" as 1, targetcount = ftMaxItems */
        // Used to keep track of all the group ids that have already been assigned.
        Set<String> assignedGroupIds = previouslyAssignedFieldTestItemGroups.stream()
            .map(fieldTestItemGroup -> fieldTestItemGroup.getGroupId())
            .collect(Collectors.toSet());
        List<FieldTestItemGroup> selectedFieldTestItemGroups = fieldTestItemGroupSelector.selectLeastUsedItemGroups(exam, assignedGroupIds, assessment,
            currentSegment, minItems);

        /* [3240-3242] endPos variable is never used again, no need to increment it - only read from in debug mode */
        /* [3244] no need to select an unused groupkey - we know our FieldTestGroupItems have unique groupkeys. */
        /* [3244-3246] Since we have list of unused items returned by selectItemgroupsRoundRobin(), no need to check that groupkey exists */
        List<FieldTestItemGroup> selectedItemGroups = new ArrayList<>();

        /* This loop begins at [3246] - We can loop over selectedFieldTestItemGroups because the list already contains
         as many items as are necessary. In legacy code, every possible field test item group (sorted by least used) is returned */
        for (FieldTestItemGroup fieldTestItemGroup : selectedFieldTestItemGroups) {
            /* Skip [3248-3274] - This code is just selecting a single item group that is unassigned and not frequently used
              (as sorted by FT_Prioritize_2012())
              Skip [3276-3285] - debug code */
            int itemCount = fieldTestItemGroup.getItemCount();
            /* [3307] */
            if (itemCount > 0 && ftItemCount + itemCount <= maxItems) {
                /* [3308 - 3314] */
                int thisIntSize = itemCount == 1 ? 1 : intervalSize * (itemCount - 1);

                /* [3318] Randomly select an item position for this ft item group */
                int nextPosition = (rng.nextInt(1000) % thisIntSize) + intervalIndex;
                /* [3344] */
                ftItemCount += itemCount;

                /* [3345-3349] */
                intervalIndex = (intervalSize == 0)
                    ? intervalIndex + itemCount
                    : intervalIndex + itemCount * intervalSize;

                /* Ignore cohort code [3357] */
                /* Ignore delete on [3362] - Only selected items will be returned from this loop */
                /* Note that we are not persisting the intervalSize/Start/number of intervals - these values are never read from again
                   anywhere in the application and are simply saved for debug and testing purposes. */
                selectedItemGroups.add(
                    new FieldTestItemGroup.Builder()
                        .fromFieldTestItemGroup(fieldTestItemGroup)
                        .withExamId(exam.getId())
                        .withSessionId(exam.getSessionId())
                        .withLanguageCode(exam.getLanguageCode())
                        .withPosition(nextPosition)
                        .withSegmentKey(segmentKey)
                        .withSegmentId(currentSegment.getSegmentId())
                        .build()
                );
            }
        }

        /* This insert is at [3378] */
        fieldTestItemGroupCommandRepository.insert(selectedItemGroups);

        /* [3386] No need to get the count of field test items again - this count is maintained in loop above */
        return ftItemCount;
    }

    /*
        This helper method is a null-tolerant Instant/date comparison for the test window
     */
    private boolean isWithinFieldTestWindow(Instant startTime, Instant endTime) {
        if (startTime != null && !startTime.isBeforeNow()) {
            return false;
        }

        return endTime == null ? true : endTime.isAfterNow();
    }
}
