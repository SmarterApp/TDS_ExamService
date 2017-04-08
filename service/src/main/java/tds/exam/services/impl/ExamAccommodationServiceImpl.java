package tds.exam.services.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamAccommodationService;
import tds.session.Session;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
class ExamAccommodationServiceImpl implements ExamAccommodationService {
    private static final String OTHER_ACCOMMODATION_VALUE = "TDS_Other#";
    private static final String OTHER_ACCOMMODATION_CODE = "TDS_Other";
    private static final String OTHER_ACCOMMODATION_NAME = "Other";

    private final ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private final ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private final AssessmentService assessmentService;

    @Autowired
    public ExamAccommodationServiceImpl(final ExamAccommodationQueryRepository examAccommodationQueryRepository,
                                        final ExamAccommodationCommandRepository examAccommodationCommandRepository,
                                        final AssessmentService assessmentService) {
        this.examAccommodationQueryRepository = examAccommodationQueryRepository;
        this.examAccommodationCommandRepository = examAccommodationCommandRepository;
        this.assessmentService = assessmentService;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(final UUID examId, final String segmentId, final String... accommodationTypes) {
        return examAccommodationQueryRepository.findAccommodations(examId, segmentId, accommodationTypes);
    }

    @Override
    public List<ExamAccommodation> findAllAccommodations(final UUID examId) {
        return examAccommodationQueryRepository.findAccommodations(examId);
    }

    @Transactional
    @Override
    public List<ExamAccommodation> initializeExamAccommodations(final Exam exam, final String studentAccommodationCodes) {
        Instant now = Instant.now();
        // This method replaces StudentDLL._InitOpportunityAccommodations_SP.  One note is that the calls to testopportunity_readonly were not implemented because
        // these tables are only used for proctor and that is handled via the proctor related endpoints.

        // StudentDLL fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
        // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
        Map<String, Accommodation> assessmentAccommodations = assessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey())
            .stream().collect(Collectors.toMap(Accommodation::getCode, a -> a, (a1, a2) -> a1));

        // Get the list of accommodations from the student package, for the particular Exam subject
        //  Take the accommodation code, lookup the accommodation and put into a Map by type for easy lookup
        Multimap<String, Accommodation> studentAccommodations = ArrayListMultimap.create();
        splitAccommodationCodes(exam.getSubject(), studentAccommodationCodes).forEach(code -> {
            if (assessmentAccommodations.containsKey(code)) {
                Accommodation accommodation = assessmentAccommodations.get(code);
                studentAccommodations.put(accommodation.getType(), accommodation);
            } else if (code.startsWith(OTHER_ACCOMMODATION_VALUE)) {
                Accommodation otherAccommodation = new Accommodation.Builder()
                    .withAccommodationType(OTHER_ACCOMMODATION_NAME)
                    .withAccommodationCode(OTHER_ACCOMMODATION_CODE)
                    .withAccommodationValue(StringUtils.substringAfter(code, OTHER_ACCOMMODATION_VALUE))
                    .build();
                studentAccommodations.put(OTHER_ACCOMMODATION_NAME, otherAccommodation);
            }
        });

        // StudentDLL line 6645 - the query filters the results of the temporary table fetched above by these two values.
        // It was decided the record usage and report usage values that are also queried are not actually used.
        // Exclude accommodations that are included in the student accommodations from the student package
        List<Accommodation> accommodations = assessmentAccommodations.values().stream().filter(
            accommodation ->
                accommodation.isDefaultAccommodation()
                    && accommodation.getDependsOnToolType() == null
                    && !studentAccommodations.containsKey(accommodation.getType())
        ).collect(Collectors.toList());

        accommodations.addAll(studentAccommodations.values());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        accommodations.forEach(accommodation -> {
            ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
                .withExamId(exam.getId())
                .withCode(accommodation.getCode())
                .withType(accommodation.getType())
                .withDescription(accommodation.getValue())
                .withSegmentPosition(accommodation.getSegmentPosition())
                .withSegmentKey(accommodation.getSegmentKey() != null ? accommodation.getSegmentKey() : exam.getAssessmentKey())
                .withValue(accommodation.getValue())
                .withTotalTypeCount(accommodation.getTypeTotal())
                .withCustom(!accommodation.isDefaultAccommodation())
                .withAllowChange(accommodation.isAllowChange())
                .withSelectable(accommodation.isSelectable())
                .withVisible(accommodation.isVisible())
                .withStudentControlled(accommodation.isStudentControl())
                .withDisabledOnGuestSession(accommodation.isDisableOnGuestSession())
                .withDefaultAccommodation(accommodation.isDefaultAccommodation())
                .withAllowCombine(accommodation.isAllowCombine())
                .withDependsOn(accommodation.getDependsOnToolType())
                .withSortOrder(accommodation.getToolTypeSortOrder())
                .withFunctional(accommodation.isFunctional())
                .withCreatedAt(now)
                .build();

            examAccommodations.add(examAccommodation);
        });

        //Inserts the accommodations into the exam system.
        examAccommodationCommandRepository.insert(examAccommodations);

        return examAccommodations;
    }

    @Override
    public List<ExamAccommodation> findApprovedAccommodations(final UUID... examIds) {
        return examAccommodationQueryRepository.findApprovedAccommodations(examIds);
    }

    @Transactional
    @Override
    public List<ExamAccommodation> initializeAccommodationsOnPreviousExam(final Exam exam,
                                                                          final Assessment assessment,
                                                                          final int segmentPosition,
                                                                          final boolean restoreRts,
                                                                          final String studentAccommodationCodes) {
        /*
         This replaces the functionality of the following bits of code
         - StudentDLL 6834 - 6843
         - StudentDLL _InitOpportunityAccommodations_SP
         - CommonDLL _UpdateOpportunityAccommodations_SP
         */
        List<ExamAccommodation> examAccommodations = findAllAccommodations(exam.getId());
        if (examAccommodations.isEmpty()) {
            examAccommodations = initializeExamAccommodations(exam, studentAccommodationCodes);
        } else {
            //CommonDLL line 2590 - gets the accommodation codes based on guest accommodations and the accommodation family for the assessment
            Set<String> accommodationCodes = splitAccommodationCodes(assessment.getAccommodationFamily(), studentAccommodationCodes);

            // CommonDLL line 2593 fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
            // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
            List<Accommodation> assessmentAccommodations = assessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());
            examAccommodations = initializePreviousAccommodations(exam, segmentPosition, restoreRts, examAccommodations, accommodationCodes, assessmentAccommodations, false);
        }

        return examAccommodations;
    }

    @Transactional
    @Override
    public List<ExamAccommodation> approveAccommodations(final Exam exam, final Session session,
                                                           final ApproveAccommodationsRequest request) {
        // Get the list of current exam accomms in case we need to update them (for example, if a pre-initialized default was changed by the guest user)
        List<ExamAccommodation> currentAccommodations = examAccommodationQueryRepository.findApprovedAccommodations(exam.getId());
        List<Accommodation> assessmentAccommodations = assessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), exam.getAssessmentKey());
        // Store all the updated exam accommodations
        List<ExamAccommodation> updatedExamAccommodations = new ArrayList<>();
        // For each assessment and segments separately, initialize their respective accommodations
        request.getAccommodationCodes().forEach((segmentPosition, guestAccommodationCodes) ->
            updatedExamAccommodations.addAll(
                initializePreviousAccommodations(exam, segmentPosition, false, currentAccommodations, guestAccommodationCodes,
                assessmentAccommodations, true)
            )
        );

        return updatedExamAccommodations;
    }

    @Override
    @Transactional
    public void denyAccommodations(final UUID examId, final Instant deniedAt) {
        final List<ExamAccommodation> pendingAccommodations = examAccommodationQueryRepository.findAccommodations(examId);
        final ExamAccommodation[] deniedAccommodations = pendingAccommodations.stream()
            .map(accommodation ->
                new ExamAccommodation.Builder(accommodation.getId())
                    .fromExamAccommodation(accommodation)
                    .withDeniedAt(deniedAt)
                    .build())
            .collect(Collectors.toList()).toArray(new ExamAccommodation[pendingAccommodations.size()]);

        examAccommodationCommandRepository.update(deniedAccommodations);
    }

    private static String getOtherAccommodationValue(String formattedValue) {
        return formattedValue.substring("TDS_Other#".length());
    }

    private Set<String> splitAccommodationCodes(String accommodationFamily, String accommodationsAsConcatenatedString) {
        /*
            This replaces CommonDLL._SplitAccomCodes_FN.  It takes the accommodation family from an Assessment (via configs.client_testproperties)
            and the guest accommodations, which are both delimited Strings, and creates a List of code strings.  The existing code creates a
            temporary table with an additional 'idx' column that is never used upstream.
        */
        if (isEmpty(accommodationsAsConcatenatedString) || isEmpty(accommodationFamily)) {
            return new HashSet<>();
        }

        accommodationFamily += ":";

        Set<String> accommodationCodes = new HashSet<>();
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
                                                                     int segmentPosition,
                                                                     boolean restoreRts,
                                                                     List<ExamAccommodation> existingExamAccommodations,
                                                                     Set<String> accommodationCodes,
                                                                     List<Accommodation> assessmentAccommodations,
                                                                     boolean isGuestAccommodations) {
        //This method replaces CommonDLL._UpdateOpportunityAccommodations_SP.
        Instant now = Instant.now();

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
                    && (exam.getStartedAt() == null || accommodation.isAllowChange())
                    && (!restoreRts || accommodation.isSelectable())
            ).map(accommodation -> {
                //Conditional below is due to StudentDLL lines 6967 - 6875
                //We need to mark the exam accommodation as not approved if the type total is greater than 1 forcing the
                //proctor to approve it.
                // Note: this conditional is exclusive to logic in  _OpenExistingOpportunity_SP
                Instant deniedAt = null;
                if (!isGuestAccommodations && accommodation.getTypeTotal() > 1) {
                    deniedAt = now;
                }
                return new ExamAccommodation.Builder(UUID.randomUUID())
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
                    .withCustom(!accommodation.isDefaultAccommodation())
                    .withVisible(accommodation.isVisible())
                    .withStudentControlled(accommodation.isStudentControl())
                    .withDisabledOnGuestSession(accommodation.isDisableOnGuestSession())
                    .withDefaultAccommodation(accommodation.isDefaultAccommodation())
                    .withAllowCombine(accommodation.isAllowCombine())
                    .withDependsOn(accommodation.getDependsOnToolType())
                    .withSortOrder(accommodation.getToolTypeSortOrder())
                    .withFunctional(accommodation.isFunctional())
                    .withDeniedAt(deniedAt)
                    .withCreatedAt(now)
                    .build();
            })
            .filter(examAccommodation -> !existingExamAccommodations.contains(examAccommodation))
            .distinct()
            .collect(Collectors.toSet());

        for (String code : accommodationCodes) {
            if (code.startsWith(OTHER_ACCOMMODATION_VALUE)) {
                accommodationsToAdd = accommodationsToAdd.stream().
                    filter(examAccommodation -> examAccommodation.getCode().startsWith(OTHER_ACCOMMODATION_VALUE))
                    .collect(Collectors.toSet());

                accommodationsToAdd.add(new ExamAccommodation.Builder(UUID.randomUUID())
                    .withExamId(exam.getId())
                    .withType(OTHER_ACCOMMODATION_NAME)
                    .withCode(OTHER_ACCOMMODATION_CODE)
                    .withValue(getOtherAccommodationValue(code))
                    .withAllowChange(false)
                    .withSelectable(false)
                    .withSegmentPosition(segmentPosition)
                    .withCreatedAt(now)
                    .build()
                );

                break;
            }
        }

        List<ExamAccommodation> examAccommodationsToInsert = new ArrayList<>();
        List<ExamAccommodation> examAccommodationsToDelete = new ArrayList<>();

        for (ExamAccommodation examAccommodation : accommodationsToAdd) {
            // Find an existing accommodation for the same type
            //  This might have the same code or not, so can't use contains since it checks for equality
            Optional<ExamAccommodation> existingAccommodation = existingExamAccommodations.stream()
                .filter(acc ->
                    acc.getExamId().equals(examAccommodation.getExamId())
                        && acc.getSegmentPosition() == examAccommodation.getSegmentPosition()
                        && acc.getType().equals(examAccommodation.getType())
                ).findFirst();

            if (existingAccommodation.isPresent()) {
                // check to see if the ExamAccommodations are logically the same
                //  if they changed then the existing one is removed and replaced with the new code
                if (!isEquivalent(existingAccommodation.get(), examAccommodation)) {
                    examAccommodationsToDelete.add(existingAccommodation.get());
                    examAccommodationsToInsert.add(examAccommodation);
                }
            } else {
                examAccommodationsToInsert.add(examAccommodation);
            }
        }

        if (!examAccommodationsToDelete.isEmpty()) {
            examAccommodationCommandRepository.delete(examAccommodationsToDelete);
        }

        if (!examAccommodationsToInsert.isEmpty()) {
            examAccommodationCommandRepository.insert(examAccommodationsToInsert);
        }

        Set<ExamAccommodation> examAccommodations = new HashSet<>(examAccommodationsToInsert);

        //Add all the exam accommodations that were not updated or inserted.
        examAccommodations.addAll(existingExamAccommodations);

        return examAccommodations.stream().collect(Collectors.toList());
    }

    /**
     * Used to see if two ExamAccommodations's are logically the same.  The {@link tds.exam.ExamAccommodation} equals() method cannot be used
     * since one the accommodations is fetched from the database and the other is provided by the UI so not all fields will match.
     *
     * @param ea1 first {@link tds.exam.ExamAccommodation}
     * @param ea2 second {@link tds.exam.ExamAccommodation}
     * @return true if the {@link tds.exam.ExamAccommodation}s are logically the same
     */
    private static boolean isEquivalent(ExamAccommodation ea1, ExamAccommodation ea2) {
        return ea1.getSegmentPosition() == ea2.getSegmentPosition()
            && StringUtils.equals(ea1.getSegmentKey(), ea2.getSegmentKey())
            && StringUtils.equals(ea1.getCode(), ea2.getCode())
            && StringUtils.equals(ea1.getValue(), ea2.getValue())
            && StringUtils.equals(ea1.getType(), ea2.getType());
    }
}
