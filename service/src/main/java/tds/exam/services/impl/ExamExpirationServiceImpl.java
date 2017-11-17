package tds.exam.services.impl;

import org.joda.time.Days;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.common.EntityUpdate;
import tds.config.TimeLimitConfiguration;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExpiredExamInformation;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ExamExpirationService;
import tds.exam.services.ExamService;
import tds.exam.services.TimeLimitConfigurationService;

public class ExamExpirationServiceImpl implements ExamExpirationService {
    /**
     * This was pulled from the original expiration logic within DmDll
     */
    static final List<String> STATUSES_TO_IGNORE_FOR_EXPIRATION = Arrays.asList(ExamStatusCode.STATUS_COMPLETED, ExamStatusCode.STATUS_SUBMITTED, ExamStatusCode.STATUS_SCORED, ExamStatusCode.STATUS_EXPIRED, ExamStatusCode.STATUS_REPORTED, ExamStatusCode.STATUS_INVALIDATED);

    private final ExamService examService;
    private final ExamQueryRepository examQueryRepository;
    private final TimeLimitConfigurationService timeLimitConfigurationService;
    private final AssessmentService assessmentService;

    @Autowired
    ExamExpirationServiceImpl(final ExamService examService, final ExamQueryRepository examQueryRepository, final TimeLimitConfigurationService timeLimitConfigurationService, final AssessmentService assessmentService) {
        this.examService = examService;
        this.examQueryRepository = examQueryRepository;
        this.timeLimitConfigurationService = timeLimitConfigurationService;
        this.assessmentService = assessmentService;
    }

    @Override
    public Collection<ExpiredExamInformation> expireExams(final String clientName) {
        //Retrieve the timelimits so we know how many days need to pass before expiration.  The system will be misconfigured
        //if this is missing hence why it throws
        TimeLimitConfiguration clientTimeLimitConfiguration = timeLimitConfigurationService.findTimeLimitConfiguration(clientName)
            .orElseThrow(() -> new IllegalStateException("Could not find time limit configuration for client " + clientName));

        //Find the exams to to expire
        List<Exam> examsToExpire = examQueryRepository.findExamsToExpire(STATUSES_TO_IGNORE_FOR_EXPIRATION);

        //None to expire return
        if(examsToExpire.isEmpty()) {
            return new HashSet<>();
        }

        //Get all the assessments for the exams to be expired since the "forceComplete" must be true
       final  Map<String, Assessment> assessmentsByKey = examsToExpire.stream().map(Exam::getAssessmentKey)
            .map(assessmentKey -> assessmentService.findAssessment(clientName, assessmentKey).get())
            .collect(Collectors.toMap(Assessment::getKey, Function.identity()));

        //Take the assessment ids from assessmentsByKey and get the timelimit configurations
        final Map<String, TimeLimitConfiguration> assessmentIdToTimeLimits = assessmentsByKey.values().stream()
            .map(Assessment::getAssessmentId)
            .map(assessmentId -> timeLimitConfigurationService.findTimeLimitConfiguration(clientName, assessmentId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toMap(TimeLimitConfiguration::getAssessmentId, Function.identity()));

        /*
         - Filter out any exam that has an assessment if isn't force complete
         - If time limit and last change aren't long enough apart
         - create entity update
         */
        final Instant now = Instant.now();
        List<EntityUpdate<Exam>> examUpdates = examsToExpire.stream()
            .filter(exam -> {
                Assessment assessment = assessmentsByKey.get(exam.getAssessmentKey());
                return assessment.isForceComplete();
        }).filter(exam -> {
            TimeLimitConfiguration timeLimitConfiguration = assessmentIdToTimeLimits.getOrDefault(exam.getAssessmentId(), clientTimeLimitConfiguration);
            return Days.daysBetween(exam.getChangedAt(), Instant.now()).isGreaterThan(Days.days(timeLimitConfiguration.getExamExpireDays()));
        }).map(exam -> {
            //Complete the exam
            Exam completedExam = new Exam.Builder().fromExam(exam)
                .withCompletedAt(now)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED), now)
                .withStatusChangeReason("Completing exam via the force submit process")
                .build();

            return new EntityUpdate<>(exam, completedExam);
        }).collect(Collectors.toList());

        if(examUpdates.isEmpty()) {
            return Collections.emptySet();
        }

        //Update the exams for completion
        examService.updateExams(examUpdates);

        //Now we need to expire the exams
        List<EntityUpdate<Exam>> expiringExams = examUpdates.stream().map(examEntityUpdate ->
        {
            //Take the previously completed exam and expire it as done in DmDll
            Exam expiringExam = new Exam.Builder()
                .fromExam(examEntityUpdate.getUpdatedEntity())
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_EXPIRED), now)
                .withStatusChangeReason("Expiring exam via the force submittal process")
                .build();

            return new EntityUpdate<>(examEntityUpdate.getUpdatedEntity(), expiringExam);
        }).collect(Collectors.toList());

        //Update the exams and set them to expired
        examService.updateExams(expiringExams);

        return expiringExams.stream().map(examEntityUpdate -> {
            Exam exam = examEntityUpdate.getUpdatedEntity();
            return new ExpiredExamInformation(
                exam.getStudentId(),
                exam.getAssessmentKey(),
                exam.getAssessmentId(),
                exam.getId());
        }).collect(Collectors.toSet());
    }
}
