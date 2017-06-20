/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.repositories.impl;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.common.Algorithm;
import tds.exam.Exam;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamSegmentBuilder;
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
        final Instant exitedAt = Instant.now();
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
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(permeable)
            .withSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .withOffGradeItems("offGrade")
            .build();

        ExamSegment segment2 = new ExamSegment.Builder()
            .withSegmentId(segmentId2)
            .withSegmentKey(segmentKey2)
            .withSegmentPosition(segmentPos2)
            .withAlgorithm(algorithm)
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(permeable)
            .withSatisfied(satisfied)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withFormKey(formKey)
            .withFormId(formId)
            .withRestorePermeableCondition(condition)
            .withOffGradeItems("offGradeTwo")
            .build();

        commandRepository.insert(Arrays.asList(segment1, segment2));

        Optional<ExamSegment> maybeRetrievedSegment = queryRepository.findByExamIdAndSegmentPosition(examId, segmentPos2);
        assertThat(maybeRetrievedSegment).isPresent();

        ExamSegment retrievedSegment = maybeRetrievedSegment.get();
        assertThat(retrievedSegment.getSegmentId()).isEqualTo(segmentId2);
        assertThat(retrievedSegment.getSegmentKey()).isEqualTo(segmentKey2);
        assertThat(retrievedSegment.getSegmentPosition()).isEqualTo(segmentPos2);
        assertThat(retrievedSegment.getAlgorithm()).isEqualTo(algorithm);
        assertThat(retrievedSegment.getCreatedAt()).isNotNull();
        assertThat(retrievedSegment.getExitedAt()).isEqualTo(exitedAt);
        assertThat(retrievedSegment.getExamId()).isEqualTo(examId);
        assertThat(retrievedSegment.getExamItemCount()).isEqualTo(examItemCount);
        assertThat(retrievedSegment.getItemPool()).contains(item);
        assertThat(retrievedSegment.getFieldTestItemCount()).isEqualTo(ftItemCount);
        assertThat(retrievedSegment.getFormId()).isEqualTo(formId);
        assertThat(retrievedSegment.getFormKey()).isEqualTo(formKey);
        assertThat(retrievedSegment.getFormCohort()).isEqualTo(cohort);
        assertThat(retrievedSegment.getRestorePermeableCondition()).isEqualTo(condition);
        assertThat(retrievedSegment.getPoolCount()).isEqualTo(poolCount);
        assertThat(retrievedSegment.isPermeable()).isEqualTo(permeable);
        assertThat(retrievedSegment.isSatisfied()).isEqualTo(satisfied);
        assertThat(retrievedSegment.getOffGradeItems()).isEqualTo("offGradeTwo");
    }

    @Test
    public void shouldCreateAndRetrieveLatestExamSegment() {
        final String segmentId = "Segment-ID-1";
        final String segmentKey = "Segment-key-1";
        final int segmentPos = 1;
        final Algorithm algorithm = Algorithm.FIXED_FORM;
        final Instant exitedAt = Instant.now();
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
            .withPermeable(false)
            .withSatisfied(false)
            .withPoolCount(poolCount)
            .withItemPool(itemPool)
            .withRestorePermeableCondition(condition)
            .withFormKey(formKey)
            .withFormId(formId)
            .build();

        commandRepository.insert(Collections.singletonList(segment1));
        itemPool.add("item2");
        final boolean newSatisfied = true;
        final boolean newPermeable = true;

        ExamSegment updatedSegment = new ExamSegment.Builder()
            .withSegmentId(segmentId)
            .withSegmentKey(segmentKey)
            .withSegmentPosition(segmentPos)
            .withAlgorithm(algorithm)
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(newSatisfied)
            .withSatisfied(newPermeable)
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
        assertThat(maybeRetrievedSegment.get().getExitedAt()).isEqualTo(exitedAt);
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
        final Instant exitedAt = Instant.now();
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
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(permeable)
            .withSatisfied(satisfied)
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
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(permeable)
            .withSatisfied(satisfied)
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
            .withExitedAt(exitedAt)
            .withExamId(differentExamid)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(permeable)
            .withSatisfied(satisfied)
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
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(true)
            .withSatisfied(true)
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
            .withExitedAt(exitedAt)
            .withExamId(examId)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(ftItemCount)
            .withFormCohort(cohort)
            .withPermeable(true)
            .withSatisfied(false)
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
        assertThat(retSegment1.getExitedAt()).isEqualTo(exitedAt);
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
        assertThat(retSegment2.getExitedAt()).isEqualTo(exitedAt);
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

    @Test
    public void shouldReturnCountOfUnsatisfiedSegments() {
        EnhancedRandom rand = EnhancedRandomBuilder.aNewEnhancedRandomBuilder().stringLengthRange(3, 10).build();

        ExamSegment satisfiedSegment1 = ExamSegment.Builder
            .fromSegment(rand.nextObject(ExamSegment.class))
            .withExamId(exam.getId())
            .withSatisfied(true)
            .build();
        ExamSegment satisfiedSegment2 = ExamSegment.Builder
            .fromSegment(rand.nextObject(ExamSegment.class))
            .withExamId(exam.getId())
            .withSatisfied(true)
            .build();
        ExamSegment unsatisfiedSegment = ExamSegment.Builder
            .fromSegment(rand.nextObject(ExamSegment.class))
            .withExamId(exam.getId())
            .withSatisfied(false)
            .build();

        commandRepository.insert(Arrays.asList(satisfiedSegment1, satisfiedSegment2, unsatisfiedSegment));

        assertThat(queryRepository.findCountOfUnsatisfiedSegments(exam.getId())).isEqualTo(1);

        ExamSegment nowSatisfiedSegment = ExamSegment.Builder
            .fromSegment(unsatisfiedSegment)
            .withExamId(exam.getId())
            .withSatisfied(true)
            .build();
        commandRepository.update(nowSatisfiedSegment);

        assertThat(queryRepository.findCountOfUnsatisfiedSegments(exam.getId())).isEqualTo(0);
    }

    @Test
    public void shouldFindExamSegmentByExamIdAndSegmentKey() {
        ExamSegment segment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .build();

        commandRepository.insert(Collections.singletonList(segment));


        ExamSegment persistedSegment = queryRepository.findByExamIdAndSegmentKey(segment.getExamId(), segment.getSegmentKey()).get();

        assertThat(persistedSegment.getSegmentKey()).isEqualTo(segment.getSegmentKey());
        assertThat(persistedSegment.getSegmentPosition()).isEqualTo(segment.getSegmentPosition());
        assertThat(persistedSegment.getExamId()).isEqualTo(exam.getId());
    }
}
