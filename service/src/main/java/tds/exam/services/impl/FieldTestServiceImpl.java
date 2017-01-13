package tds.exam.services.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.FieldTestService;
import tds.exam.services.ItemPoolService;
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
    public int selectItemGroups(Exam exam, Assessment assessment, String segmentKey) {
        Segment currentSegment = assessment.getSegment(segmentKey);
        List<FieldTestItemGroup> previouslyAssignedFieldTestItemGroups = fieldTestItemGroupQueryRepository.find(exam.getId(), segmentKey);

        /* StudentDLL [3119-3126] ftcount is just fieldTestItemGroups.size()
           [3126] - Skip, debug is always = 0                                       */
        Integer startPosition = currentSegment.getFieldTestStartPosition();
        Integer endPosition = currentSegment.getFieldTestEndPosition();
        int maxItems = currentSegment.getFieldTestMaxItems();
        int minItems = currentSegment.getFieldTestMinItems();
        int ftItemCount = previouslyAssignedFieldTestItemGroups.size(); // Initialize it as the size of the existing item groups assigned to this student

        if (startPosition == null || endPosition == null) {
            throw new IllegalStateException("Field test start and end positions are not defined in the itembank database.");
        } else if (endPosition - startPosition < maxItems) {
            /* In the legacy app, this condition would result in a divide-by-zero error. */
            throw new IllegalStateException(String.format("The maximum number of field test items (%s) cannot be greater than the difference " +
                "between the ending and starting field test positions. (%s - %s)", maxItems, endPosition, startPosition));
        }

        /* [3131-3137] Note tht if endPos - startPos < numIntervals, the integer division below results in 0, which
        *  results in  a division-by-zero at line [3320] */
        int numIntervals = maxItems;
        int intervalSize = (endPosition - startPosition) / numIntervals;
        int intervalIndex = startPosition; // keeps track of position in exam to administer ft item

        /* [3212- 3224] Skip Cohort code - the loader script hardcodes "ratio" and "cohortIndex" as 1, targetcount = ftMaxItems */
        // Used to keep track of all the group ids that have already been assigned.
        Set<String> assignedGroupIds = previouslyAssignedFieldTestItemGroups.stream()
            .map(fieldTestItemGroup -> fieldTestItemGroup.getGroupId())
            .collect(Collectors.toSet());
        List<FieldTestItemGroup> selectedFieldTestItemGroups = fieldTestItemGroupSelector.selectItemGroupsLeastUsed(exam, assignedGroupIds, assessment,
            segmentKey, minItems);

        /* [3240-3242] endPos variable is never used - only read from in debug mode */
        /* [3244] no need to select an unused groupkey - we know our FieldTestGroupItems have unique groupkeys. */
        /* [3244-3246] Since we have list of unused items returned by selectItemgroupsRoundRobin(), no need to check that groupkey exists */
        List<FieldTestItemGroup> selectedItemGroups = new ArrayList<>();

        /* This loop begins at [3246] */
        for (FieldTestItemGroup fieldTestItemGroup : selectedFieldTestItemGroups) {
            /* Skip [3248-3274] - This code is just selecting a single item group that is unassigned and not frequently used
              (as sorted by FT_Prioritize_2012())
              Skip [3276-3285] - debug code */
            int numItems = fieldTestItemGroup.getNumItems();
            /* [3307] */
            if (numItems > 0 && ftItemCount + numItems <= maxItems) {
                /* [3308 - 3314] */
                int thisIntSize = (intervalSize < 0 || numItems == 1) ?
                    1 : intervalSize * (numItems - 1);

                /* [3318] Randomly select an item position for this ft item group */
                Random rng = new Random();
                int nextPosition = (rng.nextInt(1000) % thisIntSize) + intervalIndex;

                /* [3339] Mark item group as "used" */
                assignedGroupIds.add(fieldTestItemGroup.getGroupId());
                /* [3344] */
                ftItemCount += numItems;

                /* [3345-3349] */
                intervalIndex = (intervalSize == 0)
                    ? intervalIndex + numItems
                    : intervalIndex + numItems * intervalSize;

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
                        .withNumItems(numItems)
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
