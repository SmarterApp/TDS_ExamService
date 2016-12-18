package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import java.util.Optional;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.services.FieldTestService;
import tds.session.ExternalSessionConfiguration;

@Component
public class FieldTestServiceImpl implements FieldTestService {

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
        This helper method is a null-tolerant Instant/date comparison for the test window
     */
    private boolean isWithinFieldTestWindow(Instant startTime, Instant endTime) {
        if (startTime != null && !startTime.isBeforeNow()) {
            return false;
        }

        return endTime == null ? true : endTime.isAfterNow();
    }
}
