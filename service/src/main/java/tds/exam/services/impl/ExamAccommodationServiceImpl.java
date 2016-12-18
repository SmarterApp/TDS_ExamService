package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes) {
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
        List<Accommodation> assessmentAccommodations = configService.findAssessmentAccommodationsByKey(exam.getClientName(), exam.getAssessmentKey());

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
                accommodationCodes.add(accommodationCode.length() > 100 ? accommodationCode.substring(0, 100) : accommodationCode);
            }
        }

        return accommodationCodes;
    }

    private List<ExamAccommodation> initializePreviousAccommodations(Exam exam, Assessment assessment, int segmentPosition, boolean restoreRts, String guestAccommodations, List<ExamAccommodation> existingExamAccommodations) {
    /*
        This block replaces CommonDLL._UpdateOpportunityAccommodations_SP.
     */

        //CommonDLL line 2590 - gets the accommodation codes based on guest accommodations and the accommodation family for the assessment
        List<String> accommodationCodes = splitAccommodationCodes(assessment.getAccommodationFamily(), guestAccommodations);

        // CommonDLL line 2593 fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
        // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
        List<Accommodation> assessmentAccommodations = configService.findAssessmentAccommodationsByKey(exam.getClientName(), exam.getAssessmentKey());

        List<Accommodation> accommodationsToAdd = assessmentAccommodations.stream()
            .filter(accommodation ->
                accommodationCodes.contains(accommodation.getCode())
                    && accommodation.getSegmentPosition() == segmentPosition
                    && accommodation.isEntryControl()
                    && (exam.getDateStarted() == null || !accommodation.isAllowChange())
                    && (!restoreRts || accommodation.isSelectable())
            ).collect(Collectors.toList());

        Set<String> accommodationTypes = accommodationsToAdd.stream()
            .map(Accommodation::getType)
            .collect(Collectors.toSet());

        List<ExamAccommodation> examAccommodationsToDelete = existingExamAccommodations.stream()
            .filter(accommodation -> accommodationTypes.contains(accommodation.getType()))
            .collect(Collectors.toList());

        //CommonDLL line 2677.  We delete the exam accommodations because this seems like the only
        //way in the current system to update the exam accommodations between exam runs.
        if (!examAccommodationsToDelete.isEmpty()) {
            examAccommodationCommandRepository.delete(examAccommodationsToDelete);
        }

        ExamAccommodation otherExamAccommodation = null;

        for (String code : accommodationCodes) {
            if (code.startsWith(OTHER_ACCOMMODATION_VALUE)) {
                otherExamAccommodation = new ExamAccommodation.Builder()
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

        final Optional<ExamAccommodation> maybeOtherExamAccommodation = Optional.ofNullable(otherExamAccommodation);

        /*
           CommonDLL line 2684 - 2716 - Convert the Accommodations to ExamAccommodations and remove the exam accommodation that
           matches the other accommodation value type.
         */
        List<ExamAccommodation> examAccommodationsToInsert = accommodationsToAdd.stream()
            .map(accommodation -> new ExamAccommodation.Builder()
                .withExamId(exam.getId())
                .withCode(accommodation.getCode())
                .withType(accommodation.getType())
                .withDescription(accommodation.getValue())
                .withSegmentKey(accommodation.getSegmentKey())
                .withAllowChange(accommodation.isAllowChange())
                .withSelectable(accommodation.isSelectable())
                .withValue(accommodation.getValue())
                .withSegmentPosition(segmentPosition)
                .withMultipleToolTypes(accommodation.getTypeTotal() > 1)
                .build())
            .filter(accommodation -> !maybeOtherExamAccommodation.isPresent() ||
                accommodation.getType().equals(maybeOtherExamAccommodation.get().getType()))
            .collect(Collectors.toList());

        if (!examAccommodationsToInsert.isEmpty()) {
            examAccommodationCommandRepository.insert(examAccommodationsToInsert);
        }

        return examAccommodationsToInsert;
    }
}
