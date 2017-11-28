package tds.exam.services.impl;


import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.common.EntityUpdate;
import tds.config.TimeLimitConfiguration;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExpiredExamInformation;
import tds.exam.ExpiredExamResponse;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.AssessmentService;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamService;
import tds.exam.services.TimeLimitConfigurationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamExpirationServiceImplTest {
    private ExamExpirationServiceImpl examExpirationService;

    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private ExamService mockExamService;

    @Mock
    private TimeLimitConfigurationService mockTimeLimitConfigurationService;

    @Mock
    private AssessmentService mockAssessmentService;

    @Mock
    private ConfigService mockConfigService;

    private TimeLimitConfiguration clientTimeLimitConfiguration;

    @Captor
    private ArgumentCaptor<List<EntityUpdate<Exam>>> entityUpdateCaptor;

    private ExamServiceProperties examServiceProperties;

    @Before
    public void setUp() {
        examServiceProperties = new ExamServiceProperties();
        examServiceProperties.setExpireExamLimit(2);
        examExpirationService = new ExamExpirationServiceImpl(mockExamService, mockExamQueryRepository, mockTimeLimitConfigurationService, mockAssessmentService, examServiceProperties, mockConfigService);
        clientTimeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withClientName("SBAC")
            .withExamExpireDays(3)
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenConfigurationNotFoundForClient() {
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC")).thenReturn(Optional.empty());
        examExpirationService.expireExams("SBAC");
    }

    @Test
    public void shouldExpireExamUsingClientTimeLimitConfiguration() {
        Exam exam = new ExamBuilder()
            .withClientName("SBAC")
            .withChangedAt(Instant.now().toDateTime().minusDays(5).toInstant())
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(exam.getAssessmentId())
            .withKey(exam.getAssessmentKey())
            .withForceComplete(true)
            .build();

        when(mockConfigService.findForceCompleteAssessmentIds("SBAC")).thenReturn(Collections.singletonList("assessmentId1"));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC")).thenReturn(Optional.of(clientTimeLimitConfiguration));
        when(mockExamQueryRepository.findExamsToExpire(ExamExpirationServiceImpl.STATUSES_TO_IGNORE_FOR_EXPIRATION, examServiceProperties.getExpireExamLimit() + 1, Collections.singletonList("assessmentId1"))).thenReturn(Collections.singletonList(exam));
        when(mockAssessmentService.findAssessment("SBAC", assessment.getKey())).thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC", assessment.getAssessmentId())).thenReturn(Optional.empty());

        ExpiredExamResponse expireExamResponse = examExpirationService.expireExams("SBAC");
        Collection<ExpiredExamInformation> expireExams = expireExamResponse.getExpiredExams();
        verify(mockExamService, times(2)).updateExams(entityUpdateCaptor.capture());

        assertThat(expireExams).hasSize(1);

        ExpiredExamInformation info = expireExams.iterator().next();
        assertThat(info.getAssessmentId()).isEqualTo(exam.getAssessmentId());
        assertThat(info.getAssessmentKey()).isEqualTo(exam.getAssessmentKey());
        assertThat(info.getExamId()).isEqualTo(exam.getId());
        assertThat(info.getStudentId()).isEqualTo(exam.getStudentId());

        List<EntityUpdate<Exam>> completed = entityUpdateCaptor.getAllValues().get(0);
        assertThat(completed).hasSize(1);
        EntityUpdate<Exam> completedExam = completed.get(0);
        assertThat(completedExam.getUpdatedEntity().getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_COMPLETED);
        assertThat(completedExam.getUpdatedEntity().getCompletedAt()).isNotNull();
        assertThat(completedExam.getUpdatedEntity().getStatusChangeReason()).isNotEmpty();

        List<EntityUpdate<Exam>> expired = entityUpdateCaptor.getAllValues().get(0);
        assertThat(expired).hasSize(1);
        EntityUpdate<Exam> expiredExam = completed.get(0);
        assertThat(expiredExam.getUpdatedEntity().getStatus().getCode()).isEqualTo(ExamStatusCode.STATUS_COMPLETED);
        assertThat(expiredExam.getUpdatedEntity().getStatusChangeReason()).isNotEmpty();
    }

    @Test
    public void shouldNotExpireExamsIfAssessmentIsNotForceComplete() {
        Exam exam = new ExamBuilder()
            .withClientName("SBAC")
            .withChangedAt(Instant.now().toDateTime().minusDays(5).toInstant())
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(exam.getAssessmentId())
            .withKey(exam.getAssessmentKey())
            .withForceComplete(false)
            .build();

        when(mockConfigService.findForceCompleteAssessmentIds("SBAC")).thenReturn(Collections.singletonList("assessmentId1"));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC")).thenReturn(Optional.of(clientTimeLimitConfiguration));
        when(mockExamQueryRepository.findExamsToExpire(ExamExpirationServiceImpl.STATUSES_TO_IGNORE_FOR_EXPIRATION, examServiceProperties.getExpireExamLimit() + 1, Collections.singletonList("assessmentId"))).thenReturn(Collections.singletonList(exam));
        when(mockAssessmentService.findAssessment("SBAC", assessment.getKey())).thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC", assessment.getAssessmentId())).thenReturn(Optional.empty());

        ExpiredExamResponse expireExamResponse = examExpirationService.expireExams("SBAC");
        Collection<ExpiredExamInformation> expireExams = expireExamResponse.getExpiredExams();

        verifyZeroInteractions(mockExamService);

        assertThat(expireExams).isEmpty();
    }

    @Test
    public void shouldNotExpireExamsIfAssessmentIfAssessmentTimeLimitExpireDaysNotMet() {
        Exam exam = new ExamBuilder()
            .withClientName("SBAC")
            .withChangedAt(Instant.now().toDateTime().minusDays(5).toInstant())
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(exam.getAssessmentId())
            .withKey(exam.getAssessmentKey())
            .withForceComplete(true)
            .build();

        TimeLimitConfiguration assessmentTimeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withAssessmentId(assessment.getAssessmentId())
            .withExamExpireDays(99)
            .build();

        when(mockConfigService.findForceCompleteAssessmentIds("SBAC")).thenReturn(Collections.singletonList("assessmentId1"));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC")).thenReturn(Optional.of(clientTimeLimitConfiguration));
        when(mockExamQueryRepository.findExamsToExpire(ExamExpirationServiceImpl.STATUSES_TO_IGNORE_FOR_EXPIRATION, examServiceProperties.getExpireExamLimit() + 1, Collections.singletonList("assessmentId"))).thenReturn(Collections.singletonList(exam));
        when(mockAssessmentService.findAssessment("SBAC", assessment.getKey())).thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC", assessment.getAssessmentId())).thenReturn(Optional.of(assessmentTimeLimitConfiguration));

        ExpiredExamResponse expireExamResponse = examExpirationService.expireExams("SBAC");
        Collection<ExpiredExamInformation> expireExams = expireExamResponse.getExpiredExams();

        verifyZeroInteractions(mockExamService);

        assertThat(expireExams).isEmpty();
    }

    @Test
    public void shouldExpireExamsWithTheSameAssessment() {
        Exam exam = new ExamBuilder()
            .withClientName("SBAC")
            .withId(UUID.randomUUID())
            .withChangedAt(Instant.now().toDateTime().minusDays(5).toInstant())
            .build();

        Exam exam2 = new ExamBuilder()
            .withId(UUID.randomUUID())
            .withClientName("SBAC")
            .withChangedAt(Instant.now().toDateTime().minusDays(5).toInstant())
            .build();

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(exam.getAssessmentId())
            .withKey(exam.getAssessmentKey())
            .withForceComplete(true)
            .build();

        TimeLimitConfiguration assessmentTimeLimitConfiguration = new TimeLimitConfiguration.Builder()
            .withAssessmentId(assessment.getAssessmentId())
            .withExamExpireDays(1)
            .build();

        when(mockConfigService.findForceCompleteAssessmentIds("SBAC")).thenReturn(Collections.singletonList("assessmentId1"));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC")).thenReturn(Optional.of(clientTimeLimitConfiguration));
        when(mockExamQueryRepository.findExamsToExpire(ExamExpirationServiceImpl.STATUSES_TO_IGNORE_FOR_EXPIRATION, examServiceProperties.getExpireExamLimit() + 1, Collections.singletonList("assessmentId1"))).thenReturn(Arrays.asList(exam, exam2));
        when(mockAssessmentService.findAssessment("SBAC", assessment.getKey())).thenReturn(Optional.of(assessment));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration("SBAC", assessment.getAssessmentId())).thenReturn(Optional.of(assessmentTimeLimitConfiguration));

        ExpiredExamResponse expireExamResponse = examExpirationService.expireExams("SBAC");
        Collection<ExpiredExamInformation> expireExams = expireExamResponse.getExpiredExams();

        verify(mockExamService, times(2)).updateExams(entityUpdateCaptor.capture());

        assertThat(expireExams).hasSize(2);

        assertThat(expireExams.stream().map(ExpiredExamInformation::getExamId).collect(Collectors.toSet())).containsOnly(exam.getId(), exam2.getId());
    }
}