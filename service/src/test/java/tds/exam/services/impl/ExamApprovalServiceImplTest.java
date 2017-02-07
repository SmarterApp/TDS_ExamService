package tds.exam.services.impl;

import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.config.TimeLimitConfiguration;
import tds.exam.ExamInfo;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalStatus;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExternalSessionConfigurationBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.SessionService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.session.ExternalSessionConfiguration;
import tds.session.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusCode.STATUS_PENDING;
import static tds.exam.ExamStatusStage.OPEN;

@RunWith(MockitoJUnitRunner.class)
public class ExamApprovalServiceImplTest {
    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private TimeLimitConfigurationService mockTimeLimitConfigurationService;

    private ExamApprovalService examApprovalService;

    @Before
    public void setUp() {
        examApprovalService = new ExamApprovalServiceImpl(mockExamQueryRepository,
            mockSessionService,
            mockTimeLimitConfigurationService);
    }

    @Test
    public void shouldReturnExamApprovalBecauseAllRulesAreSatisfied() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withClientName(clientName)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfiguration(clientName, mockEnvironment, 0, 0, 0, 0)));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isFalse();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnExamApprovalWithWaitingStatusBecauseEnvironmentIsDevelopment() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "development";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withClientName(clientName)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(STATUS_PENDING, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isFalse();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
    }

    @Test
    public void shouldReturnExamApprovalWithApprovedStatusBecauseEnvironmentIsSimulation() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "SimUlaTIon";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withClientName(clientName)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(10).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(11).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isFalse();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnExamApprovalWithCorrectExamStatusBecauseSessionIsProctorless() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "development";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withClientName(clientName)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(55).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(null)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isFalse();
        assertThat(result.getData().isPresent()).isTrue();
        assertThat(result.getData().get().getExamId()).isEqualTo(examId);
        assertThat(result.getData().get().getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
    }

    @Test
    public void shouldReturnValidationErrorDueToBrowserKeyMismatch() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withClientName(clientName)
                .withBrowserId(UUID.randomUUID())
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isTrue();
        assertThat(result.getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_BROWSER_ID_MISMATCH);
    }

    @Test
    public void shouldReturnValidationErrorDueToSessionKeyMismatch() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(UUID.randomUUID())
                .withClientName(clientName)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(60).toStandardDuration()))
                .withDateVisited(Instant.now())
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isTrue();
        assertThat(result.getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_ID_MISMATCH);
    }

    @Test
    public void shouldReturnValidationErrorDueToClosedSession() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withClientName(clientName)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().minus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("closed")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isTrue();
        assertThat(result.getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED);
    }

    @Test
    public void shouldReturnValidationErrorDueToTaCheckinTimeExpired() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withClientName(clientName)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        Response<ExamApproval> result = examApprovalService.getApproval(examInfo);

        assertThat(result.hasError()).isTrue();
        assertThat(result.getError().get().getCode()).isEqualTo(ValidationErrorCode.EXAM_APPROVAL_TA_CHECKIN_TIMEOUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenExamIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.empty());
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        examApprovalService.getApproval(examInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenSessionIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withClientName(clientName)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.empty());
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        examApprovalService.getApproval(examInfo);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenExternalSessionConfigurationIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withSessionId(sessionId)
                .withClientName(clientName)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.empty());
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.of(new TimeLimitConfiguration.Builder()
                .withClientName(clientName)
                .withEnvironment(mockEnvironment)
                .withTaCheckinTimeMinutes(20)
                .build()));

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        examApprovalService.getApproval(examInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTimeLimitConfigurationIsNotPresent() {
        UUID examId = UUID.randomUUID();
        UUID browserKey = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String clientName = "UNIT_TEST";
        String mockEnvironment = "Unit Test";
        String mockAssessmentId = "unit test assessment";

        when(mockExamQueryRepository.getExamById(examId))
            .thenReturn(Optional.of(new Exam.Builder()
                .withId(examId)
                .withClientName(clientName)
                .withSessionId(sessionId)
                .withBrowserId(browserKey)
                .withAssessmentId(mockAssessmentId)
                .build()));
        when(mockSessionService.findSessionById(sessionId))
            .thenReturn(Optional.of(new Session.Builder()
                .withId(sessionId)
                .withDateBegin(Instant.now().minus(Minutes.minutes(60).toStandardDuration()))
                .withDateEnd(Instant.now().plus(Minutes.minutes(30).toStandardDuration()))
                .withDateVisited(Instant.now().minus(Minutes.minutes(45).toStandardDuration()))
                .withStatus("open")
                .withProctorId(42L)
                .build()));
        when(mockSessionService.findExternalSessionConfigurationByClientName(clientName))
            .thenReturn(Optional.of(new ExternalSessionConfigurationBuilder().withEnvironment(mockEnvironment).build()));
        when(mockTimeLimitConfigurationService.findTimeLimitConfiguration(clientName, mockAssessmentId))
            .thenReturn(Optional.empty());

        ExamInfo examInfo = new ExamInfo(examId, sessionId, browserKey);

        examApprovalService.getApproval(examInfo);
    }
}
