package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
class ExamAccommodationServiceImpl implements ExamAccommodationService {
    private static final String OTHER_ACCOMMODATION_VALUE = "TDS_Other#";
    private static final String OTHER_ACCOMMODATION_CODE = "TDS_Other";
    private static final String OTHER_ACCOMMODATION_NAME = "Other";

    private final ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private final ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private final ConfigService configService;

    @Autowired
    public ExamAccommodationServiceImpl(ExamAccommodationQueryRepository examAccommodationQueryRepository, ExamAccommodationCommandRepository examAccommodationCommandRepository, ConfigService configService) {
        this.examAccommodationQueryRepository = examAccommodationQueryRepository;
        this.examAccommodationCommandRepository = examAccommodationCommandRepository;
        this.configService = configService;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String... accommodationTypes) {
        return examAccommodationQueryRepository.findAccommodations(examId, segmentId, accommodationTypes);
    }

    @Override
    public List<ExamAccommodation> findAllAccommodations(UUID examId) {
        return examAccommodationQueryRepository.findAccommodations(examId);
    }

    @Override
    public List<ExamAccommodation> initializeExamAccommodations(Exam exam) {
        // This method replaces StudentDLL._InitOpportunityAccommodations_SP.  One note is that the calls to testopporunity_readonly were not implemented because
        // these tables are only used for proctor and that is handled via the proctor related endpoints.

        // StudentDLL fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
        // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
        List<Accommodation> assessmentAccommodations = configService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());

        // StudentDLL line 6645 - the query filters the results of the temporary table fetched above by these two values.
        // It was decided the record usage and report usage values that are also queried are not actually used.
        List<Accommodation> accommodations = assessmentAccommodations.stream().filter(accommodation ->
            accommodation.isDefaultAccommodation() && accommodation.getDependsOnToolType() == null).collect(Collectors.toList());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        accommodations.forEach(accommodation -> {
            ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
                .withExamId(exam.getId())
                .withCode(accommodation.getCode())
                .withType(accommodation.getType())
                .withDescription(accommodation.getValue())
                .withSegmentKey(accommodation.getSegmentKey())
                .withValue(accommodation.getValue())
                .withMultipleToolTypes(accommodation.getTypeTotal() > 1)
                .build();

            examAccommodations.add(examAccommodation);
        });

        //Inserts the accommodations into the exam system.
        examAccommodationCommandRepository.insert(examAccommodations);

        return examAccommodations;
    }

    @Override
    public List<ExamAccommodation> findApprovedAccommodations(UUID examId) {
        return examAccommodationQueryRepository.findApprovedAccommodations(examId);
    }

    @Override
    public void initializeAccommodationsOnPreviousExam(Exam exam, Assessment assessment, int segmentPosition, boolean restoreRts, String guestAccommodations) {
        /*
         This replaces the functionality of the following bits of code
         - StudentDLL 6834 - 6843
         - StudentDLL _InitOpportunityAccommodations_SP
         - CommonDLL _UpdateOpportunityAccommodations_SP
         */
        List<ExamAccommodation> examAccommodations = findAllAccommodations(exam.getId());
        if (examAccommodations.isEmpty()) {
            initializeExamAccommodations(exam);
        } else {
            initializePreviousAccommodations(exam, assessment, segmentPosition, restoreRts, guestAccommodations, examAccommodations);
        }

        //Fetch the updated exam accommodations
        examAccommodations = findAllAccommodations(exam.getId());

        //StudentDLL lines 6967 - 6875
        ExamAccommodation[] examAccommodationsToDenyApproval = examAccommodations.stream()
            .filter(ExamAccommodation::isMultipleToolTypes)
            .map(accommodation -> new ExamAccommodation
                .Builder()
                .fromExamAccommodation(accommodation)
                .withDeniedAt(Instant.now())
                .build())
            .toArray(ExamAccommodation[]::new);

        examAccommodationCommandRepository.update(examAccommodationsToDenyApproval);
    }

    private static String getOtherAccommodationValue(String formattedValue) {
        return formattedValue.substring("TDS_Other#".length());
    }

    private List<String> splitAccommodationCodes(String accommodationFamily, String guestAccommodations) {
        /*
            This replaces CommonDLL._SplitAccomCodes_FN.  It takes the accommodation family from an Assessment (via configs.client_testproperties)
            and the guest accommodations, which are both delimited Strings, and creates a List of code strings.  The existing code creates a
            temporary table with an additional 'idx' column that is never used upstream.
        */
        if (isEmpty(guestAccommodations) || isEmpty(accommodationFamily)) {
            return new ArrayList<>();
        }

        accommodationFamily += ":";

        //TODO - Guest accommodations isn't correct.  This should be found student package

        List<String> accommodationCodes = new ArrayList<>();
        for (String guestAccommodation : guestAccommodations.split(";")) {
            String accommodationCode = "";
            if (guestAccommodation.indexOf(':') > -1 && !guestAccommodation.contains(accommodationFamily)) {
                accommodationCode = guestAccommodation;
            }

            if (guestAccommodation.contains(accommodationFamily)) {
                accommodationCode = guestAccommodation.substring(accommodationFamily.length());
            }

            if (isNotEmpty(accommodationCode)) {
                accommodationCodes.add(accommodationCode);
            }
        }

        return accommodationCodes;
    }

    private List<ExamAccommodation> initializePreviousAccommodations(Exam exam,
                                                                     Assessment assessment,
                                                                     int segmentPosition,
                                                                     boolean restoreRts,
                                                                     String guestAccommodations,
                                                                     List<ExamAccommodation> existingExamAccommodations) {
    /*
        This block replaces CommonDLL._UpdateOpportunityAccommodations_SP.
     */

        //TODO - Find out what `customAccommodations`

        //CommonDLL line 2590 - gets the accommodation codes based on guest accommodations and the accommodation family for the assessment
        List<String> accommodationCodes = splitAccommodationCodes(assessment.getAccommodationFamily(), guestAccommodations);

        // CommonDLL line 2593 fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
        // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
        List<Accommodation> assessmentAccommodations = configService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());

        /*
        This is the accumulation of many different queries on lines CommonDLL.UpdateOpportunityAccommodations_SP()
        2616 - 2667.  Accommodations are kept if:

        1. Accommodation code is in the accommodation codes based on the split accommodation logic
        2. segment position must be the same as the passed in position
        3. isEntryControl must be false
        4. if the exam is started the accommodation must allow change.
        5. If restoreRts is true then the accommodation must be selectable.
        6. Exam accommodations must be distinct
         */
        Set<ExamAccommodation> accommodationsToAdd = assessmentAccommodations.stream()
            .filter(accommodation ->
                accommodationCodes.contains(accommodation.getCode())
                    && accommodation.getSegmentPosition() == segmentPosition
                    && !accommodation.isEntryControl()
                    && (exam.getDateStarted() == null || accommodation.isAllowChange())
                    && (!restoreRts || accommodation.isSelectable())
            ).map(accommodation -> new ExamAccommodation.Builder()
                .withExamId(exam.getId())
                .withCode(accommodation.getCode())
                .withType(accommodation.getType())
                .withDescription(accommodation.getValue())
                .withSegmentKey(accommodation.getSegmentKey())
                .withAllowChange(accommodation.isAllowChange())
                .withSelectable(accommodation.isSelectable())
                .withValue(accommodation.getValue())
                .withSegmentPosition(segmentPosition)
                .withTotalTypeCount(accommodation.getTypeTotal())
                .build())
            .distinct()
            .collect(Collectors.toSet());

//Do these steps
//1. Find the default accommodations
//2. If ExamAccommodations aren't present then add
//3. If ExamAccommodations are present but no longer in accommodations remove
// Three lists - insert new, replace existing, delete those no longer on it

        List<ExamAccommodation> examAccommodationsToDelete = new ArrayList<>();
        List<ExamAccommodation> examAccommodationsToInsert = new ArrayList<>();
        List<ExamAccommodation> examAccommodationsToUpdate = new ArrayList<>();

        for (ExamAccommodation examAccommodation : accommodationsToAdd) {
            if (existingExamAccommodations.contains(examAccommodation)) {
                ExamAccommodation existingAccommodation = existingExamAccommodations.get(existingExamAccommodations.indexOf(examAccommodation));
                Comparator.comparing(existingAccommodation -> examAccommodation.getSegmentPosition())
            }
        }

        //CommonDLL line 2677.  We delete the exam accommodations because this seems like the only
        //way in the current system to update the exam accommodations between exam runs.
        if (!examAccommodationsToDelete.isEmpty()) {
            examAccommodationCommandRepository.delete(examAccommodationsToDelete);
        }

        ExamAccommodation otherAccommodation = null;

        for (String code : accommodationCodes) {
            if (code.startsWith(OTHER_ACCOMMODATION_VALUE)) {
                otherAccommodation = new ExamAccommodation.Builder()
                    .withExamId(exam.getId())
                    .withType(OTHER_ACCOMMODATION_NAME)
                    .withCode(OTHER_ACCOMMODATION_CODE)
                    .withValue(getOtherAccommodationValue(code))
                    .withAllowChange(false)
                    .withSelectable(false)
                    .withSegmentPosition(segmentPosition)
                    .build();

                break;
            }
        }

        /*
           CommonDLL line 2684 - 2716 - Convert the Accommodations to ExamAccommodations and remove the exam accommodation that
           matches the other accommodation value type.
         */
        final ExamAccommodation otherExamAccommodation = otherAccommodation;
        List<ExamAccommodation> examAccommodationsToInsert = accommodationsToAdd.stream()
            .filter(accommodation -> otherExamAccommodation == null ||
                accommodation.getType().equals(otherExamAccommodation.getType()))
            .collect(Collectors.toList());

        //add the other accommodation to insert.  The maybe other accommodation never gets added to the list to insert
        if (otherExamAccommodation != null) {
            examAccommodationsToInsert.add(otherAccommodation);
        }

        if (!examAccommodationsToInsert.isEmpty()) {
            examAccommodationCommandRepository.insert(examAccommodationsToInsert);
        }

        return examAccommodationsToInsert;
    }
}
