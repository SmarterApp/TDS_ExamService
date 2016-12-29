package tds.exam.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
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
                .withTotalTypeCount(accommodation.getTypeTotal())
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
            examAccommodations = initializeExamAccommodations(exam);
        } else {
            examAccommodations = initializePreviousAccommodations(exam, assessment, segmentPosition, restoreRts, guestAccommodations, examAccommodations);
        }

        //StudentDLL lines 6967 - 6875
        ExamAccommodation[] examAccommodationsToDenyApproval = examAccommodations.stream()
            .filter(examAccommodation -> examAccommodation.getTotalTypeCount() > 1)
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

    private List<String> splitAccommodationCodes(String accommodationFamily, String accommodationsAsConcatenatedString) {
        /*
            This replaces CommonDLL._SplitAccomCodes_FN.  It takes the accommodation family from an Assessment (via configs.client_testproperties)
            and the guest accommodations, which are both delimited Strings, and creates a List of code strings.  The existing code creates a
            temporary table with an additional 'idx' column that is never used upstream.
        */
        if (isEmpty(accommodationsAsConcatenatedString) || isEmpty(accommodationFamily)) {
            return new ArrayList<>();
        }

        accommodationFamily += ":";

        List<String> accommodationCodes = new ArrayList<>();
        for (String accommodation : accommodationsAsConcatenatedString.split(";")) {
            String accommodationCode = "";
            if (accommodation.indexOf(':') > -1 && !accommodation.contains(accommodationFamily)) {
                accommodationCode = accommodation;
            }

            if (accommodation.contains(accommodationFamily)) {
                accommodationCode = accommodation.substring(accommodationFamily.length());
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
        //This method replaces CommonDLL._UpdateOpportunityAccommodations_SP.


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

        for (String code : accommodationCodes) {
            if (code.startsWith(OTHER_ACCOMMODATION_VALUE)) {
                accommodationsToAdd = accommodationsToAdd.stream().
                    filter(examAccommodation -> examAccommodation.getCode().startsWith(OTHER_ACCOMMODATION_VALUE))
                    .collect(Collectors.toSet());

                accommodationsToAdd.add(new ExamAccommodation.Builder()
                    .withExamId(exam.getId())
                    .withType(OTHER_ACCOMMODATION_NAME)
                    .withCode(OTHER_ACCOMMODATION_CODE)
                    .withValue(getOtherAccommodationValue(code))
                    .withAllowChange(false)
                    .withSelectable(false)
                    .withSegmentPosition(segmentPosition)
                    .build()
                );

                break;
            }
        }

        List<ExamAccommodation> examAccommodationsToInsert = new ArrayList<>();
        List<ExamAccommodation> examAccommodationsToUpdate = new ArrayList<>();

        for (ExamAccommodation examAccommodation : accommodationsToAdd) {
            if (existingExamAccommodations.contains(examAccommodation)) {
                ExamAccommodation existingAccommodation = existingExamAccommodations.get(existingExamAccommodations.indexOf(examAccommodation));
                if (!isEqual(existingAccommodation, examAccommodation)) {
                    examAccommodationsToUpdate.add(examAccommodation);
                }
            } else {
                examAccommodationsToInsert.add(examAccommodation);
            }
        }

        if (!examAccommodationsToInsert.isEmpty()) {
            examAccommodationCommandRepository.insert(examAccommodationsToInsert);
        }

        if (!examAccommodationsToUpdate.isEmpty()) {
            examAccommodationCommandRepository.update(examAccommodationsToUpdate.toArray(new ExamAccommodation[examAccommodationsToUpdate.size()]));
        }

        Set<ExamAccommodation> examAccommodations = new HashSet<>(examAccommodationsToInsert);
        examAccommodations.addAll(examAccommodationsToUpdate);

        //Add all the exam accommodations that were not updated or inserted.
        examAccommodations.addAll(existingExamAccommodations);

        return examAccommodations.stream().collect(Collectors.toList());
    }

    private static boolean isEqual(ExamAccommodation ea1, ExamAccommodation ea2) {
        return ea1.getSegmentPosition() == ea2.getSegmentPosition()
            && StringUtils.equals(ea1.getSegmentKey(), ea2.getSegmentKey())
            && StringUtils.equals(ea1.getCode(), ea2.getCode())
            && StringUtils.equals(ea1.getValue(), ea2.getValue())
            && StringUtils.equals(ea1.getType(), ea2.getType())
            && ea1.getExamId().equals(ea2.getExamId())
            && ea1.getTotalTypeCount() == ea2.getTotalTypeCount()
            && isEqual(ea1.getDeniedAt(), ea2.getDeniedAt());
    }

    private static boolean isEqual(Instant instant, Instant instant2) {
        return (instant == null && instant2 == null)
            || !(instant != null && instant2 == null)
            && instant != null
            && instant.equals(instant2);
    }
}
