package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.assessment.Assessment;
import tds.common.Algorithm;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AbilityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.ExamStatusStage.OPEN;

@RunWith(MockitoJUnitRunner.class)
public class AbilityServiceImplTest {
    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private HistoryQueryRepository mockHistoryQueryRepository;

    private AbilityService abilityService;

    @Before
    public void setUp() {
        abilityService = new AbilityServiceImpl(mockHistoryQueryRepository, mockExamQueryRepository);
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForSameAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST1";
        final long studentId = 9898L;
        final double assessmentAbilityVal = 99D;

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .build();

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), assessmentId, 1, java.time.Instant.now(), 50D);

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);

        Optional<Double> maybeAbilityReturned = abilityService.getInitialAbility(thisExam, assessment);

        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithoutSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST4";
        final long studentId = 9897L;

        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .withAssessmentId(assessmentId)
            .withAbilitySlope(1)
            .withAbilityIntercept(0)
            .withInitialAbilityBySubject(true)
            .build();
        // Null slope/intercept for this test case

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);
        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);

        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryQueryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = abilityService.getInitialAbility(thisExam, assessment);
        verify(mockHistoryQueryRepository).findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId);
        assertThat(maybeAbilityReturned.get()).isEqualTo(maybeAbility.get());
    }

    @Test
    public void shouldGetInitialAbilityFromItembank() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST6";
        final long studentId = 9898L;
        final float assessmentAbilityVal = 99F;

        Assessment assessment = new Assessment();
        assessment.setKey("(SBAC)SBAC ELA 3-ELA-3-Spring-2112a");
        assessment.setAssessmentId(assessmentId);
        assessment.setSelectionAlgorithm(Algorithm.FIXED_FORM);
        assessment.setStartAbility(assessmentAbilityVal);

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        List<Ability> abilities = new ArrayList<>();
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryQueryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(Optional.empty());
        Optional<Double> maybeAbilityReturned = abilityService.getInitialAbility(thisExam, assessment);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    @Test
    public void shouldGetInitialAbilityFromHistoryWithSlopeIntercept() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST3";
        final long studentId = 9898L;
        final float slope = 2f;
        final float intercept = 1f;

        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Assessment assessment = new AssessmentBuilder()
            .withSubject("ELA")
            .withAbilitySlope(slope)
            .withAbilityIntercept(intercept)
            .withInitialAbilityBySubject(true)
            .build();

        List<Ability> abilities = new ArrayList<>();
        Optional<Double> maybeAbility = Optional.of(66D);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        when(mockHistoryQueryRepository.findAbilityFromHistoryForSubjectAndStudent(clientName, "ELA", studentId))
            .thenReturn(maybeAbility);
        Optional<Double> maybeAbilityReturned = abilityService.getInitialAbility(thisExam, assessment);
        // y=mx+b
        double abilityCalculated = maybeAbility.get() * slope + intercept;
        assertThat(maybeAbilityReturned.get()).isEqualTo((float) abilityCalculated);
    }

    @Test
    public void shouldGetInitialAbilityFromScoresForDifferentAssessment() {
        final UUID sessionId = UUID.randomUUID();
        final UUID thisExamId = UUID.randomUUID();
        final String assessmentId = "SBAC ELA 3-ELA-3";
        final String clientName = "SBAC_TEST2";
        final long studentId = 9899L;
        final double assessmentAbilityVal = 75D;


        Exam thisExam = createExam(sessionId, thisExamId, assessmentId, clientName, studentId);

        Ability sameAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), assessmentAbilityVal);
        Ability differentAssessmentAbility = new Ability(
            UUID.randomUUID(), "assessmentid-2", 1, java.time.Instant.now(), 50D);

        Assessment assessment = new AssessmentBuilder()
            .withAssessmentId(assessmentId)
            .withInitialAbilityBySubject(true)
            .withSubject("ELA")
            .build();

        List<Ability> abilities = new ArrayList<>();
        abilities.add(sameAssessmentAbility);
        abilities.add(differentAssessmentAbility);
        when(mockExamQueryRepository.findAbilities(thisExamId, clientName, "ELA", studentId)).thenReturn(abilities);
        Optional<Double> maybeAbilityReturned = abilityService.getInitialAbility(thisExam, assessment);
        assertThat(maybeAbilityReturned.get()).isEqualTo(assessmentAbilityVal);
    }

    private Exam createExam(UUID sessionId, UUID thisExamId, String assessmentId, String clientName, long studentId) {
        return new Exam.Builder()
            .withId(thisExamId)
            .withClientName(clientName)
            .withSessionId(sessionId)
            .withAssessmentId(assessmentId)
            .withSubject("ELA")
            .withStudentId(studentId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, OPEN), Instant.now())
            .withChangedAt(Instant.now())
            .withScoredAt(Instant.now())
            .build();
    }
}