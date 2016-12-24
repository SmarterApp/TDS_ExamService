package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Algorithm;
import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link ExamSegmentQueryRepositoryImpl and {@link ExamSegmentCommandRepositoryImpl}}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamSegmentRepositoryImplIntegrationTests {

    private ExamSegmentCommandRepositoryImpl commandRepository;
    private ExamSegmentQueryRepositoryImpl queryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;

    private Exam exam;
    private Exam otherExam;

    @Before
    public void setUp() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        commandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        queryRepository = new ExamSegmentQueryRepositoryImpl(commandJdbcTemplate);

        exam = new ExamBuilder().withId(UUID.randomUUID()).build();
        otherExam = new ExamBuilder().withId(UUID.randomUUID()).build();

        examCommandRepository.insert(exam);
        examCommandRepository.insert(otherExam);
    }

    @Test
    public void shouldCreateAndRetrieveSingleExamSegment() {
        final String segmentId1 = "Segment-ID-1";
        final String segmentKey1 = "Segment-key-1";
        final int segmentPos1 = 1;
        final String segmentId2 = "Segment-ID-2";
        final String segmentKey2 = "Segment-key-2";
        final int segmentPos2 = 2;
        final Algorithm algorithm = Algorithm.FIXED_FORM;
        final Instant dateExited = Instant.now(Clock.systemUTC());
        final UUID examId = exam.getId();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final boolean permeable = false;
        final boolean satisfied = false;
        final int poolCount = 12;
        final String item = "item1";
        Set<String> itemPool = new HashSet<>();
        itemPool.add(item);
        final String condition = "on pauseExam";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
            .withSegmentId(segmentId1)
            .withSegmentKey(segmentKey1)
            .withSegmentPosition(segmentPos1)
            .withAlgorithm(algorithm)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(permeable)
            .withIsSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        ExamSegment segment2 = new ExamSegment.Builder()
            .withSegmentId(segmentId2)
            .withSegmentKey(segmentKey2)
            .withSegmentPosition(segmentPos2)
            .withAlgorithm(algorithm)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(permeable)
            .withIsSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withFormKey(formKey)
            .withFormId(formId)
            .withRestorePermeableCondition(condition)
            .build();

        commandRepository.insert(Arrays.asList(segment1, segment2));

        Optional<ExamSegment> maybeRetrievedSegment = queryRepository.findByExamIdAndSegmentPosition(examId, segmentPos2);
        assertThat(maybeRetrievedSegment).isPresent();
        assertThat(maybeRetrievedSegment.get().getSegmentId()).isEqualTo(segmentId2);
        assertThat(maybeRetrievedSegment.get().getSegmentKey()).isEqualTo(segmentKey2);
        assertThat(maybeRetrievedSegment.get().getSegmentPosition()).isEqualTo(segmentPos2);
        assertThat(maybeRetrievedSegment.get().getAlgorithm()).isEqualTo(algorithm);
        assertThat(maybeRetrievedSegment.get().getCreatedAt()).isNotNull();
        assertThat(maybeRetrievedSegment.get().getDateExited()).isEqualTo(dateExited);
        assertThat(maybeRetrievedSegment.get().getExamId()).isEqualTo(examId);
        assertThat(maybeRetrievedSegment.get().getExamItemCount()).isEqualTo(examItemCount);
        assertThat(maybeRetrievedSegment.get().getItemPool()).contains(item);
        assertThat(maybeRetrievedSegment.get().getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(maybeRetrievedSegment.get().getFormId()).isEqualTo(formId);
        assertThat(maybeRetrievedSegment.get().getFormKey()).isEqualTo(formKey);
        assertThat(maybeRetrievedSegment.get().getFormCohort()).isEqualTo(cohort);
        assertThat(maybeRetrievedSegment.get().getRestorePermeableCondition()).isEqualTo(condition);
        assertThat(maybeRetrievedSegment.get().getPoolCount()).isEqualTo(poolCount);
        assertThat(maybeRetrievedSegment.get().isPermeable()).isEqualTo(permeable);
        assertThat(maybeRetrievedSegment.get().isSatisfied()).isEqualTo(satisfied);
    }

    @Test
    public void shouldCreateAndRetrieveLatestExamSegment() {
        final String segmentId = "Segment-ID-1";
        final String segmentKey = "Segment-key-1";
        final int segmentPos = 1;
        final Algorithm algorithm = Algorithm.FIXED_FORM;
        final Instant dateExited = Instant.now(Clock.systemUTC());
        final UUID examId = exam.getId();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final int poolCount = 12;
        final String item = "item1";
        Set<String> itemPool = new HashSet<>();
        itemPool.add(item);
        final String condition = "on pauseExam";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
            .withSegmentId(segmentId)
            .withSegmentKey(segmentKey)
            .withSegmentPosition(segmentPos)
            .withAlgorithm(algorithm)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(false)
            .withIsSatisfied(false)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        commandRepository.insert(Arrays.asList(segment1));
        itemPool.add("item2");
        final boolean newSatisfied = true;
        final boolean newPermeable = true;

        ExamSegment updatedSegment = new ExamSegment.Builder()
            .withSegmentId(segmentId)
            .withSegmentKey(segmentKey)
            .withSegmentPosition(segmentPos)
            .withAlgorithm(algorithm)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(newSatisfied)
            .withIsSatisfied(newPermeable)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withFormKey(formKey)
            .withFormId(formId)
            .withRestorePermeableCondition("new condition")
            .build();

        commandRepository.update(updatedSegment);

        Optional<ExamSegment> maybeRetrievedSegment = queryRepository.findByExamIdAndSegmentPosition(examId, segmentPos);
        assertThat(maybeRetrievedSegment).isPresent();
        assertThat(maybeRetrievedSegment.get().getSegmentId()).isEqualTo(segmentId);
        assertThat(maybeRetrievedSegment.get().getSegmentKey()).isEqualTo(segmentKey);
        assertThat(maybeRetrievedSegment.get().getSegmentPosition()).isEqualTo(segmentPos);
        assertThat(maybeRetrievedSegment.get().getAlgorithm()).isEqualTo(algorithm);
        assertThat(maybeRetrievedSegment.get().getCreatedAt()).isNotNull();
        assertThat(maybeRetrievedSegment.get().getDateExited()).isEqualTo(dateExited);
        assertThat(maybeRetrievedSegment.get().getExamId()).isEqualTo(examId);
        assertThat(maybeRetrievedSegment.get().getExamItemCount()).isEqualTo(examItemCount);
        assertThat(maybeRetrievedSegment.get().getItemPool()).contains(item);
        assertThat(maybeRetrievedSegment.get().getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(maybeRetrievedSegment.get().getFormId()).isEqualTo(formId);
        assertThat(maybeRetrievedSegment.get().getFormKey()).isEqualTo(formKey);
        assertThat(maybeRetrievedSegment.get().getFormCohort()).isEqualTo(cohort);
        assertThat(maybeRetrievedSegment.get().getRestorePermeableCondition()).isEqualTo("new condition");
        assertThat(maybeRetrievedSegment.get().getPoolCount()).isEqualTo(poolCount);
        assertThat(maybeRetrievedSegment.get().isPermeable()).isEqualTo(newPermeable);
        assertThat(maybeRetrievedSegment.get().isSatisfied()).isEqualTo(newSatisfied);
    }

    @Test
    public void shouldCreateAndRetrieveAllLatestSegments() {
        final String segmentId1 = "Segment-ID-1";
        final String segmentKey1 = "Segment-key-1";
        final int segmentPos1 = 1;
        final String segmentId2 = "Segment-ID-2";
        final String segmentKey2 = "Segment-key-2";
        final int segmentPos2 = 2;
        final Algorithm algorithm1 = Algorithm.ADAPTIVE_2;
        final Algorithm algorithm2 = Algorithm.FIXED_FORM;
        final Instant dateExited = Instant.now(Clock.systemUTC());
        final UUID examId = exam.getId();
        final UUID differentExamid = otherExam.getId();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final boolean permeable = false;
        final boolean satisfied = false;
        final int poolCount = 12;
        final String item = "item1";
        Set<String> itemPool = new HashSet<>();
        itemPool.add(item);
        final String condition = "on pauseExam";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
            .withSegmentId(segmentId1)
            .withSegmentKey(segmentKey1)
            .withSegmentPosition(segmentPos1)
            .withAlgorithm(algorithm1)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(permeable)
            .withIsSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        ExamSegment segment2 = new ExamSegment.Builder()
            .withSegmentId(segmentId2)
            .withSegmentKey(segmentKey2)
            .withSegmentPosition(segmentPos2)
            .withAlgorithm(algorithm2)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(permeable)
            .withIsSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withFormKey(formKey)
            .withFormId(formId)
            .withRestorePermeableCondition(condition)
            .build();

        ExamSegment segment3 = new ExamSegment.Builder()
            .withSegmentId(segmentId2)
            .withSegmentKey(segmentKey2)
            .withSegmentPosition(segmentPos2)
            .withAlgorithm(algorithm2)
            .withDateExited(dateExited)
            .withExamId(differentExamid)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(permeable)
            .withIsSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withFormKey(formKey)
            .withFormId(formId)
            .withRestorePermeableCondition(condition)
            .build();

        commandRepository.insert(Arrays.asList(segment1, segment2, segment3));

        ExamSegment segment1Updated = new ExamSegment.Builder()
            .withSegmentId(segmentId1)
            .withSegmentKey(segmentKey1)
            .withSegmentPosition(segmentPos1)
            .withAlgorithm(algorithm1)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(true)
            .withIsSatisfied(true)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        itemPool.add("another item");

        ExamSegment segment2Updated = new ExamSegment.Builder()
            .withSegmentId(segmentId2)
            .withSegmentKey(segmentKey2)
            .withSegmentPosition(segmentPos2)
            .withAlgorithm(algorithm2)
            .withDateExited(dateExited)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withIsPermeable(true)
            .withIsSatisfied(false)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        commandRepository.update(Arrays.asList(segment1Updated, segment2Updated));

        List<ExamSegment> retrievedSegments = queryRepository.findByExamId(examId);
        assertThat(retrievedSegments).hasSize(2);
        ExamSegment retSegment1 = retrievedSegments.get(0);
        ExamSegment retSegment2 = retrievedSegments.get(1);
        assertThat(retSegment1.getSegmentId()).isEqualTo(segmentId1);
        assertThat(retSegment1.getSegmentKey()).isEqualTo(segmentKey1);
        assertThat(retSegment1.getSegmentPosition()).isEqualTo(segmentPos1);
        assertThat(retSegment1.getAlgorithm()).isEqualTo(algorithm1);
        assertThat(retSegment1.getCreatedAt()).isNotNull();
        assertThat(retSegment1.getDateExited()).isEqualTo(dateExited);
        assertThat(retSegment1.getExamId()).isEqualTo(examId);
        assertThat(retSegment1.getExamItemCount()).isEqualTo(examItemCount);
        assertThat(retSegment1.getItemPool()).contains(item);
        assertThat(retSegment1.getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(retSegment1.getFormId()).isEqualTo(formId);
        assertThat(retSegment1.getFormKey()).isEqualTo(formKey);
        assertThat(retSegment1.getFormCohort()).isEqualTo(cohort);
        assertThat(retSegment1.getRestorePermeableCondition()).isEqualTo(condition);
        assertThat(retSegment1.getPoolCount()).isEqualTo(poolCount);
        assertThat(retSegment1.isPermeable()).isEqualTo(true);
        assertThat(retSegment1.isSatisfied()).isEqualTo(true);

        assertThat(retSegment2.getSegmentId()).isEqualTo(segmentId2);
        assertThat(retSegment2.getSegmentKey()).isEqualTo(segmentKey2);
        assertThat(retSegment2.getSegmentPosition()).isEqualTo(segmentPos2);
        assertThat(retSegment2.getAlgorithm()).isEqualTo(algorithm2);
        assertThat(retSegment2.getCreatedAt()).isNotNull();
        assertThat(retSegment2.getDateExited()).isEqualTo(dateExited);
        assertThat(retSegment2.getExamId()).isEqualTo(examId);
        assertThat(retSegment2.getExamItemCount()).isEqualTo(examItemCount);
        assertThat(retSegment2.getItemPool()).contains(item);
        assertThat(retSegment2.getItemPool()).contains("another item");
        assertThat(retSegment2.getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(retSegment2.getFormId()).isEqualTo(formId);
        assertThat(retSegment2.getFormKey()).isEqualTo(formKey);
        assertThat(retSegment2.getFormCohort()).isEqualTo(cohort);
        assertThat(retSegment2.getRestorePermeableCondition()).isEqualTo(condition);
        assertThat(retSegment2.getPoolCount()).isEqualTo(poolCount);
        assertThat(retSegment2.isPermeable()).isEqualTo(true);
        assertThat(retSegment2.isSatisfied()).isEqualTo(false);
    }
}
