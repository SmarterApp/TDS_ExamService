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
import tds.exam.models.ExamSegment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by emunoz on 10/28/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
//@Transactional
public class ExamSegmentRepositoryImplIntegrationTests {

    private ExamSegmentCommandRepositoryImpl commandRepository;
    private ExamSegmentQueryRepositoryImpl queryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;

    @Before
    public void setUp() {
        commandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        queryRepository = new ExamSegmentQueryRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldCreateAndRetrieveSingleExamSegment() {
        final String segmentId1 = "Segment-ID-1";
        final String segmentKey1 = "Segment-key-1";
        final int segmentPos1 = 1;
        final String segmentId2 = "Segment-ID-2";
        final String segmentKey2 = "Segment-key-2";
        final int segmentPos2 = 2;
        final String algorithm = "fixedform";
        final Instant dateExited = Instant.now();
        final UUID examId = UUID.randomUUID();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final boolean permeable = false;
        final boolean satisfied = false;
        final int poolCount = 12;
        final String item = "item1";
        List<String> itemPool = new ArrayList<>();
        itemPool.add(item);
        final String reason = "on pause";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId1)
                .withAssessmentSegmentKey(segmentKey1)
                .withSegmentPosition(segmentPos1)
                .withAlgorithm(algorithm)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(permeable)
                .withIsSatisfied(satisfied)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withRestorePermeableOn(reason)
                .withFormKey(formKey)
                .withFormId(formId)
                .build();

        ExamSegment segment2 = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId2)
                .withAssessmentSegmentKey(segmentKey2)
                .withSegmentPosition(segmentPos2)
                .withAlgorithm(algorithm)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(permeable)
                .withIsSatisfied(satisfied)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withFormKey(formKey)
                .withFormId(formId)
                .withRestorePermeableOn(reason)
                .build();

        commandRepository.insert(segment1);
        commandRepository.insert(segment2);

        Optional<ExamSegment> maybeRetrievedSegment = queryRepository.findByExamIdAndSegmentPosition(examId, segmentPos2);
        assertThat(maybeRetrievedSegment).isPresent();
        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentId()).isEqualTo(segmentId2);
        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentKey()).isEqualTo(segmentKey2);
        assertThat(maybeRetrievedSegment.get().getSegmentPosition()).isEqualTo(segmentPos2);
        assertThat(maybeRetrievedSegment.get().getAlgorithm()).isEqualTo(algorithm);
        assertThat(maybeRetrievedSegment.get().getCreatedAt()).isNotNull();
//        assertThat(maybeRetrievedSegment.get().getDateExited()).isEqualTo(dateExited);
        assertThat(maybeRetrievedSegment.get().getExamId()).isEqualTo(examId);
        assertThat(maybeRetrievedSegment.get().getExamItemCount()).isEqualTo(examItemCount);
        assertThat(maybeRetrievedSegment.get().getItemPool()).contains(item);
        assertThat(maybeRetrievedSegment.get().getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(maybeRetrievedSegment.get().getFormId()).isEqualTo(formId);
        assertThat(maybeRetrievedSegment.get().getFormKey()).isEqualTo(formKey);
        assertThat(maybeRetrievedSegment.get().getFormCohort()).isEqualTo(cohort);
        assertThat(maybeRetrievedSegment.get().getRestorePermeableOn()).isEqualTo(reason);
        assertThat(maybeRetrievedSegment.get().getFieldTestItems()).hasSize(0);
        assertThat(maybeRetrievedSegment.get().getPoolCount()).isEqualTo(poolCount);
        assertThat(maybeRetrievedSegment.get().isPermeable()).isEqualTo(permeable);
        assertThat(maybeRetrievedSegment.get().isSatisfied()).isEqualTo(satisfied);
    }

    @Test
    public void shouldCreateAndRetrieveLatestExamSegment() {
        final String segmentId = "Segment-ID-1";
        final String segmentKey = "Segment-key-1";
        final int segmentPos = 1;
        final String algorithm = "fixedform";
        final Instant dateExited = Instant.now();
        final UUID examId = UUID.randomUUID();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final int poolCount = 12;
        final String item = "item1";
        List<String> itemPool = new ArrayList<>();
        itemPool.add(item);
        final String reason = "on pause";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId)
                .withAssessmentSegmentKey(segmentKey)
                .withSegmentPosition(segmentPos)
                .withAlgorithm(algorithm)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(false)
                .withIsSatisfied(false)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withRestorePermeableOn(reason)
                .withFormKey(formKey)
                .withFormId(formId)
                .build();

        commandRepository.insert(segment1);
        itemPool.add("item2");
        final boolean newSatisfied = true;
        final boolean newPermeable = true;

        ExamSegment updatedSegment = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId)
                .withAssessmentSegmentKey(segmentKey)
                .withSegmentPosition(segmentPos)
                .withAlgorithm(algorithm)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(newSatisfied)
                .withIsSatisfied(newPermeable)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withFormKey(formKey)
                .withFormId(formId)
                .withRestorePermeableOn("new reason")
                .build();

        commandRepository.update(updatedSegment);

        Optional<ExamSegment> maybeRetrievedSegment = queryRepository.findByExamIdAndSegmentPosition(examId, segmentPos);
        assertThat(maybeRetrievedSegment).isPresent();
        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentId()).isEqualTo(segmentId);
        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentKey()).isEqualTo(segmentKey);
        assertThat(maybeRetrievedSegment.get().getSegmentPosition()).isEqualTo(segmentPos);
        assertThat(maybeRetrievedSegment.get().getAlgorithm()).isEqualTo(algorithm);
        assertThat(maybeRetrievedSegment.get().getCreatedAt()).isNotNull();
//        assertThat(maybeRetrievedSegment.get().getDateExited()).isEqualTo(dateExited);
        assertThat(maybeRetrievedSegment.get().getExamId()).isEqualTo(examId);
        assertThat(maybeRetrievedSegment.get().getExamItemCount()).isEqualTo(examItemCount);
        assertThat(maybeRetrievedSegment.get().getItemPool()).contains(item);
        assertThat(maybeRetrievedSegment.get().getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(maybeRetrievedSegment.get().getFormId()).isEqualTo(formId);
        assertThat(maybeRetrievedSegment.get().getFormKey()).isEqualTo(formKey);
        assertThat(maybeRetrievedSegment.get().getFormCohort()).isEqualTo(cohort);
        assertThat(maybeRetrievedSegment.get().getRestorePermeableOn()).isEqualTo("new reason");
        assertThat(maybeRetrievedSegment.get().getFieldTestItems()).hasSize(0);
        assertThat(maybeRetrievedSegment.get().getPoolCount()).isEqualTo(poolCount);
        assertThat(maybeRetrievedSegment.get().isPermeable()).isEqualTo(newPermeable);
        assertThat(maybeRetrievedSegment.get().isSatisfied()).isEqualTo(newSatisfied);
    }

    @Test
    public void shouldCreateAndRetrieveAllSegmentsForExam() {
        final String segmentId1 = "Segment-ID-1";
        final String segmentKey1 = "Segment-key-1";
        final int segmentPos1 = 1;
        final String segmentId2 = "Segment-ID-2";
        final String segmentKey2 = "Segment-key-2";
        final int segmentPos2 = 2;
        final String algorithm1 = "adaptive2";
        final String algorithm2 = "fixedform";
        final Instant dateExited = Instant.now();
        final UUID examId = UUID.randomUUID();
        final int examItemCount = 3;
        final int ftItemCount = 0;
        final String cohort = "Default";
        final boolean permeable = false;
        final boolean satisfied = false;
        final int poolCount = 12;
        final String item = "item1";
        List<String> itemPool = new ArrayList<>();
        itemPool.add(item);
        final String reason = "on pause";
        final String formId = "form-id-1";
        final String formKey = "form-key-1";

        ExamSegment segment1 = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId1)
                .withAssessmentSegmentKey(segmentKey1)
                .withSegmentPosition(segmentPos1)
                .withAlgorithm(algorithm1)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(permeable)
                .withIsSatisfied(satisfied)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withRestorePermeableOn(reason)
                .withFormKey(formKey)
                .withFormId(formId)
                .build();

        ExamSegment segment2 = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId2)
                .withAssessmentSegmentKey(segmentKey2)
                .withSegmentPosition(segmentPos2)
                .withAlgorithm(algorithm2)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(permeable)
                .withIsSatisfied(satisfied)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withFormKey(formKey)
                .withFormId(formId)
                .withRestorePermeableOn(reason)
                .build();

        commandRepository.insert(segment1);
        commandRepository.insert(segment2);

        ExamSegment segment1Updated = new ExamSegment.Builder()
                .withAssessmentSegmentId(segmentId1)
                .withAssessmentSegmentKey(segmentKey1)
                .withSegmentPosition(segmentPos1)
                .withAlgorithm(algorithm1)
                .withDateExited(dateExited)
                .withExamId(examId)
                .withExamItemCount(examItemCount)
                .withFieldTestItemCount(ftItemCount)
                .withFormCohort(cohort)
                .withFieldTestItems(new ArrayList<>())
                .withIsPermeable(true)
                .withIsSatisfied(true)
                .withPoolCount(poolCount)
                .withItemPool(itemPool)
                .withRestorePermeableOn(reason)
                .withFormKey(formKey)
                .withFormId(formId)
                .build();

        commandRepository.update(segment1Updated);

        List<ExamSegment> retrievedSegments = queryRepository.findByExamId(examId);
        assertThat(retrievedSegments).hasSize(2);
//        assertThat(maybeRetrievedSegment).isPresent();
//        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentId()).isEqualTo(segmentId2);
//        assertThat(maybeRetrievedSegment.get().getAssessmentSegmentKey()).isEqualTo(segmentKey2);
//        assertThat(maybeRetrievedSegment.get().getSegmentPosition()).isEqualTo(segmentPos2);
//        assertThat(maybeRetrievedSegment.get().getAlgorithm()).isEqualTo(algorithm1);
//        assertThat(maybeRetrievedSegment.get().getCreatedAt()).isNotNull();
////        assertThat(maybeRetrievedSegment.get().getDateExited()).isEqualTo(dateExited);
//        assertThat(maybeRetrievedSegment.get().getExamId()).isEqualTo(examId);
//        assertThat(maybeRetrievedSegment.get().getExamItemCount()).isEqualTo(examItemCount);
//        assertThat(maybeRetrievedSegment.get().getItemPool()).contains(item);
//        assertThat(maybeRetrievedSegment.get().getFieldTestItemCount()).isEqualTo(ftItemCount);
//        assertThat(maybeRetrievedSegment.get().getFormId()).isEqualTo(formId);
//        assertThat(maybeRetrievedSegment.get().getFormKey()).isEqualTo(formKey);
//        assertThat(maybeRetrievedSegment.get().getFormCohort()).isEqualTo(cohort);
//        assertThat(maybeRetrievedSegment.get().getRestorePermeableOn()).isEqualTo(reason);
//        assertThat(maybeRetrievedSegment.get().getFieldTestItems()).hasSize(0);
//        assertThat(maybeRetrievedSegment.get().getPoolCount()).isEqualTo(poolCount);
//        assertThat(maybeRetrievedSegment.get().isPermeable()).isEqualTo(permeable);
//        assertThat(maybeRetrievedSegment.get().isSatisfied()).isEqualTo(satisfied);
    }
}
