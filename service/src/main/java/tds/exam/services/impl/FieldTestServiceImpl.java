package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;
import tds.exam.services.FieldTestService;
import tds.exam.services.ItemPoolService;
import tds.session.ExternalSessionConfiguration;

@Service
public class FieldTestServiceImpl implements FieldTestService {
//    private final FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository;
//    private final ItemPoolService itemPoolService;
//
//    @Autowired TODO: Autowire this once FieldTestServiceImpl.selectItemGroups is implemented
//    public FieldTestServiceImpl(FieldTestItemGroupQueryRepository queryRepository, ItemPoolService itemPoolService) {
//        this. fieldTestItemGroupQueryRepository = queryRepository;
//        this.itemPoolService = itemPoolService;
//    }

    @Override
    public boolean isFieldTestEligible(Exam exam, Assessment assessment, String segmentKey, String languageCode) {
        boolean isEligible = false;
        Segment currentSegment = assessment.getSegment(segmentKey);

        /* StudentDLL [4453] - ftminitems must be non-zero */
        if (currentSegment.getFieldTestMinItems() > 0) {
            // Check if there exists at least one field test item in the segment with the selected language
            /* StudentDLL [4430] */
            Optional<Item> fieldTestItem = currentSegment.getItems(languageCode).stream()
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
    public int selectItemGroups(Exam exam, Assessment assessment, String segmentKey, String language) {
        //TODO: Implement. Adding stub to prevent compilation errors
        return 0;
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
