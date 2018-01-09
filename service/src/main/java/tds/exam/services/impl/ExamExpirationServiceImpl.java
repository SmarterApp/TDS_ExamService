package tds.exam.services.impl;

import org.joda.time.Days;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import tds.common.EntityUpdate;
import tds.config.TimeLimitConfiguration;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExpiredExamInformation;
import tds.exam.ExpiredExamResponse;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamExpirationService;
import tds.exam.services.ExamService;
import tds.exam.services.TimeLimitConfigurationService;

@Service
public class ExamExpirationServiceImpl implements ExamExpirationService {
    /**
     * This was pulled from the original expiration logic within DmDll
     */
    static final List<String> STATUSES_TO_IGNORE_FOR_EXPIRATION = Arrays.asList(ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_SUBMITTED, ExamStatusCode.STATUS_SCORED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_REPORTED, ExamStatusCode.STATUS_INVALIDATED);

    private final ExamService examService;
    private final ExamQueryRepository examQueryRepository;
    private final TimeLimitConfigurationService timeLimitConfigurationService;
    private final ExamServiceProperties examServiceProperties;
    private final ConfigService configService;

    @Autowired
    ExamExpirationServiceImpl(final ExamService examService,
                              final ExamQueryRepository examQueryRepository,
                              final TimeLimitConfigurationService timeLimitConfigurationService,
                              final ExamServiceProperties examServiceProperties,
                              final ConfigService configService) {
        this.examService = examService;
        this.examQueryRepository = examQueryRepository;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.examServiceProperties = examServiceProperties;
        this.configService = configService;
    }

    @Override
    public ExpiredExamResponse expireExams(final String clientName) {
        boolean moreExamsToExpire = false;
        final Collection<EntityUpdate<Exam>> modifiedExams = new ArrayList<>();

        //Retrieve the time limits so we know how many days need to pass before expiration.  The system will be mis-configured
        //if this is missing hence why it throws
        TimeLimitConfiguration clientTimeLimitConfiguration = timeLimitConfigurationService.findTimeLimitConfiguration(clientName)
            .orElseThrow(() -> new IllegalStateException("Could not find time limit configuration for client " + clientName));

        //Find the exams to to expire
        List<Exam> examsToExpire = examQueryRepository.findExamsToExpire(STATUSES_TO_IGNORE_FOR_EXPIRATION, (examServiceProperties.getExpireExamLimit() + 1));

        //None to expire return
        if (examsToExpire.isEmpty()) {
            return new ExpiredExamResponse(false, Collections.emptyList());
        }

        //If there are more than the expire limit it means that the caller needs to call again to expire the rest of the exams
        if (examsToExpire.size() > examServiceProperties.getExpireExamLimit()) {
            moreExamsToExpire = true;

            //Remove the extra expire exam
            examsToExpire = new ArrayList<>(examsToExpire.subList(0, examServiceProperties.getExpireExamLimit()));
        }

        //Get all assessment ids that have force complete enabled.
        Collection<String> forceCompleteAssessmentIds = configService.findForceCompleteAssessmentIds(clientName);

        //Get all the assessment ids for the exams so we can fetch the timelimit configurations for them
        Set<String> expiringExamsAssessmentIds = examsToExpire.stream()
            .map(Exam::getAssessmentId)
            .collect(Collectors.toSet());

        //Take the assessment ids from assessmentsByKey and get the timelimit configurations.  The filter with null check is
        //to remove the overall client time limits since the configuration service returns it regardless.
        final Map<String, TimeLimitConfiguration> assessmentIdToTimeLimits = expiringExamsAssessmentIds.stream()
            .map(assessmentId -> timeLimitConfigurationService.findTimeLimitConfiguration(clientName, assessmentId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(timeLimitConfiguration -> timeLimitConfiguration.getAssessmentId() != null)
            .collect(Collectors.toMap(TimeLimitConfiguration::getAssessmentId, Function.identity()));

        /*
        1. Filter out any exam that is not ready to be expired based on the time limit configuration for that exam (or the default system wide configuration)
        2. Create two lists: one list contains those exams which assessment is configured for force complete and the other is not.  The ones that aren't force complete will be expired.
         */
        Map<Boolean, List<Exam>> submitAndExpireExams = examsToExpire.stream().filter(exam -> {
            TimeLimitConfiguration timeLimitConfiguration = assessmentIdToTimeLimits.getOrDefault(exam.getAssessmentId(), clientTimeLimitConfiguration);
            return Days.daysBetween(exam.getExpiresAt(), Instant.now()).isGreaterThan(Days.days(timeLimitConfiguration.getExamExpireDays()));
        }).collect(Collectors.partitioningBy(exam -> forceCompleteAssessmentIds.contains(exam.getAssessmentId())));

        List<Exam> examsToSubmit = submitAndExpireExams.get(true);
        List<Exam> expiringExams = submitAndExpireExams.get(false);

        //Submit all exams that are set for forceComplete
        final Instant statusUpdateTime = Instant.now();
        if (!examsToSubmit.isEmpty()) {
            List<EntityUpdate<Exam>> examUpdates = examsToSubmit.stream()
                .map(exam -> {
                    //Complete the exam
                    Exam completedExam = new Exam.Builder().fromExam(exam)
                        .withCompletedAt(statusUpdateTime)
                        .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED), statusUpdateTime)
                        .withStatusChangeReason("Completing exam via the force submit process")
                        .build();

                    return new EntityUpdate<>(exam, completedExam);
                }).collect(Collectors.toList());

            //Update the exams for completion
            examService.updateExams(examUpdates);
            modifiedExams.addAll(examUpdates);
        }

        //Expire all exams that do not have force complete configured
        if (!expiringExams.isEmpty()) {
            //Now we need to expire the exams
            List<EntityUpdate<Exam>> expiredExams = expiringExams.stream().map(exam ->
            {
                Exam expiringExam = new Exam.Builder()
                    .fromExam(exam)
                    .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), statusUpdateTime)
                    .withStatusChangeReason("Expiring exam via the force submittal process")
                    .build();

                return new EntityUpdate<>(exam, expiringExam);
            }).collect(Collectors.toList());

            //Update the exams and set them to expired
            examService.updateExams(expiredExams);
            modifiedExams.addAll(expiredExams);
        }

        //Collect all the modified exams and send the list of data as a response
        Collection<ExpiredExamInformation> expiredExamInformations = modifiedExams.stream().map((EntityUpdate<Exam> entityUpdate) -> new ExpiredExamInformation(
            entityUpdate.getUpdatedEntity().getStudentId(),
            entityUpdate.getUpdatedEntity().getAssessmentKey(),
            entityUpdate.getUpdatedEntity().getAssessmentId(),
            entityUpdate.getUpdatedEntity().getId(),
            entityUpdate.getUpdatedEntity().getStatus().getCode())).collect(Collectors.toSet());

        return new ExpiredExamResponse(moreExamsToExpire, expiredExamInformations);
    }
}
