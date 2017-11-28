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

import com.google.common.collect.ImmutableList;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.joda.time.Duration.standardDays;
import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamQueryRepositoryImplIntegrationTests {
    private ExamQueryRepository examQueryRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private ExamSegmentCommandRepository examSegmentCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private UUID currentExamId = UUID.fromString("af880054-d1d2-4c24-805c-1f0dfdb45980");
    private UUID mockSessionId = UUID.fromString("7595f73d-c0d2-4298-b7c8-045bacf1fe89");
    private Set<String> statusesThatCanTransitionToPaused;

    private List<Exam> examsInSession;

    @Before
    public void setUp() {
        examQueryRepository = new ExamQueryRepositoryImpl(jdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(jdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(jdbcTemplate);

        List<Exam> exams = new ArrayList<>();
        // Build a basic exam record
        exams.add(new ExamBuilder().build());

        exams.add(new ExamBuilder()
            .withCreatedAt(Instant.now().minus(99999))
            .build());

        // Build an exam record that has been marked as deleted
        exams.add(new ExamBuilder()
            .withId(UUID.fromString("ab880054-d1d2-4c24-805c-0dfdb45a0d24"))
            .withAssessmentId("assementId2")
            .withDeletedAt(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .build());

        // Build an exam record that is a subsequent attempt of an exam
        exams.add(new ExamBuilder()
            .withId(currentExamId)
            .withBrowserId(UUID.fromString("3C7254E4-34E1-417F-BC58-CFFC1E8D8006"))
            .withAssessmentId("assessmentId3")
            .withStudentId(9999L)
            .withAttempts(2)
            .withResumptions(2)
            .withRestartsAndResumptions(3)
            .withScoredAt(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .build());

        exams.forEach(exam -> {
            examCommandRepository.insert(exam);
            examAccommodationCommandRepository.insert(Collections.singletonList(
                new ExamAccommodationBuilder()
                    .withType("Language")
                    .withCode("ENU")
                    .withExamId(exam.getId())
                    .withSegmentPosition(0)
                    .build()
            ));
        });

        insertExamScoresData();

        // Build exams that belong to the same session
        examsInSession = new ArrayList<>();
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(5L)
            .withAssessmentId("assessmentId5")
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(6L)
            .withAssessmentId("assessmentId5")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.INACTIVE), Instant.now())
            .withStudentId(7L)
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.INACTIVE), Instant.now())
            .withStudentId(8L)
            .withAssessmentId("assessmentId6")
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(mockSessionId)
            .withStudentId(9L)
            .withAssessmentId("assessmentId6")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_FAILED, ExamStatusStage.INACTIVE), Instant.now())
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(UUID.randomUUID())
            .withStudentId(10L)
            .withAssessmentId("assessmentId7")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.INACTIVE), Instant.now())
            .build());
        examsInSession.add(new ExamBuilder().withSessionId(UUID.randomUUID())
            .withStudentId(10L)
            .withAssessmentId("assessmentId7")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.INACTIVE), Instant.now())
            .build());

        examsInSession.forEach(exam -> {
            examCommandRepository.insert(exam);
            examAccommodationCommandRepository.insert(Collections.singletonList(
                new ExamAccommodationBuilder()
                    .withType("Language")
                    .withCode("ENU")
                    .withExamId(exam.getId())
                    .withSegmentPosition(0)
                    .build()
            ));
        });

        statusesThatCanTransitionToPaused = new HashSet<>(Arrays.asList(ExamStatusCode.STATUS_PAUSED,
            ExamStatusCode.STATUS_PENDING,
            ExamStatusCode.STATUS_SUSPENDED,
            ExamStatusCode.STATUS_STARTED,
            ExamStatusCode.STATUS_APPROVED,
            ExamStatusCode.STATUS_REVIEW,
            ExamStatusCode.STATUS_INITIALIZING));
    }

    @Test
    public void shouldRetrieveExamForUniqueKey() {
        Optional<Exam> examOptional = examQueryRepository.getExamById(currentExamId);
        assertThat(examOptional.isPresent()).isTrue();
        assertThat(examOptional.get().getRestartsAndResumptions()).isEqualTo(3);
        assertThat(examOptional.get().getResumptions()).isEqualTo(2);
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundUniqueKey() {
        UUID examUniqueKey = UUID.fromString("12345678-d1d2-4c24-805c-0dfdb45a0d24");
        Optional<Exam> sessionOptional = examQueryRepository.getExamById(examUniqueKey);
        assertThat(sessionOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldRetrieveLatestExam() {
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1L, "assessmentId", "clientName");
        assertThat(examOptional.isPresent()).isTrue();
        Exam exam = examOptional.get();
        assertThat(exam.getId()).isNotNull();
        assertThat(exam.getStatus()).isNotNull();
    }

    @Test
    public void shouldNotReturnLatestExamWithNonNullDeletedDate() {
        Optional<Exam> examOptional = examQueryRepository.getLastAvailableExam(1L, "assessmentId2", "clientName");
        assertThat(examOptional.isPresent()).isFalse();
    }

    @Test
    public void shouldReturnEmptyListOfAbilities() {
        List<Ability> noAbilities = examQueryRepository.findAbilities(UUID.fromString("12345678-d1d2-4c24-805c-0dfdb45a0999"),
            "otherclient", "ELA", 9999L);
        assertThat(noAbilities).isEmpty();
    }

    @Test
    public void shouldFindExamsToExpireByStatusCodes() {
        UUID deletedAtExamId = UUID.randomUUID();
        UUID completedExamId = UUID.randomUUID();
        UUID pausedExamWithLongAgoChangeDateId = UUID.randomUUID();

        List<UUID> examIdsThatShouldNotExpire = Arrays.asList(deletedAtExamId, completedExamId, pausedExamWithLongAgoChangeDateId);

        List<Exam> examsForExpire = new ArrayList<>();
        examsForExpire.add(new ExamBuilder()
            .withId(deletedAtExamId)
            .withAssessmentId("assementId2")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.OPEN), Instant.now())
            .withDeletedAt(Instant.now().minus(Minutes.minutes(5).toStandardDuration()))
            .withChangedAt(Instant.now().minus(Days.days(5).toStandardDuration()))
            .build());

        examsForExpire.add(new ExamBuilder()
            .withId(completedExamId)
            .withAssessmentId("assementId2")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_COMPLETED, ExamStatusStage.CLOSED), Instant.now())
            .withCompletedAt(Instant.now())
            .withChangedAt(Instant.now().minus(Days.days(5).toStandardDuration()))
            .build());

        Exam examStarted = new ExamBuilder()
            .withId(pausedExamWithLongAgoChangeDateId)
            .withAssessmentId("assementId2")
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.OPEN), Instant.now())
            .withChangedAt(Instant.now().minus(Days.days(5).toStandardDuration()))
            .build();

        examsForExpire.add(examStarted);

        ExamSegment examSegment = new ExamSegmentBuilder().withExamId(pausedExamWithLongAgoChangeDateId).build();
        ExamPage examPage = new ExamPageBuilder().withId(UUID.randomUUID()).withExamId(pausedExamWithLongAgoChangeDateId).withPagePosition(1).withSegmentKey(examSegment.getSegmentKey()).build();
        ExamPage examPage2 = new ExamPageBuilder().withId(UUID.randomUUID()).withExamId(pausedExamWithLongAgoChangeDateId).withPagePosition(2).withSegmentKey(examSegment.getSegmentKey()).build();

        examsForExpire.forEach(exam -> {
            Instant changedAtDate = exam.getChangedAt();
            examCommandRepository.insert(exam);
            examAccommodationCommandRepository.insert(Collections.singletonList(
                new ExamAccommodationBuilder()
                    .withType("Language")
                    .withCode("ENU")
                    .withExamId(exam.getId())
                    .withSegmentPosition(0)
                    .build()
            ));

            updateEventCreatedAt(exam.getId(), changedAtDate);
        });

        examSegmentCommandRepository.insert(Collections.singletonList(examSegment));
        examPageCommandRepository.insert(examPage, examPage2);

        List<Exam> examsToExpire = examQueryRepository.findExamsToExpire(Arrays.asList(ExamStatusCode.STATUS_APPROVED, ExamStatusCode.STATUS_CLOSED));

        boolean foundStarted = false;

        for(Exam exam : examsToExpire) {
            if(exam.getId().equals(pausedExamWithLongAgoChangeDateId)) {
                foundStarted = true;
                continue;
            }

            if(examIdsThatShouldNotExpire.contains(exam.getId())) {
                fail("Found an exam id that should not be expired " + exam.getId());
            }
        }

        assertThat(foundStarted).isTrue();
    }

    private void updateEventCreatedAt(UUID examId, Instant changedAt) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("changedAt", mapJodaInstantToTimestamp(changedAt))
            .addValue("examId", examId.toString());


        String SQL = "update exam.exam_event set changed_at = :changedAt where exam_id = :examId";

        assertThat(jdbcTemplate.update(SQL, parameterSource)).isEqualTo(1);
    }

    @Test
    public void shouldReturnLastPausedDate() {
        Instant now = Instant.now();
        Instant pausedAt = now.minus(99999);

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), pausedAt)
            .build();
        examCommandRepository.insert(exam);

        Exam approvedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED, ExamStatusStage.IN_PROGRESS), now.minus(5000))
            .build();

        examCommandRepository.update(approvedExam);

        Optional<Instant> maybeLastTimePaused = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimePaused).isPresent();
        assertThat(maybeLastTimePaused.get()).isGreaterThanOrEqualTo(now);
    }

    @Test
    public void shouldFindExamWithLatestLanguage() throws InterruptedException {
        final Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        final ExamAccommodation enuAcc = new ExamAccommodationBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withType(Accommodation.ACCOMMODATION_TYPE_LANGUAGE)
            .withCode("ENU")
            .withCreatedAt(Instant.now().minus(standardDays(1)))
            .build();
        examAccommodationCommandRepository.insert(ImmutableList.of(enuAcc));

        final Optional<Exam> maybeExam = examQueryRepository.getExamById(exam.getId());
        assertThat(maybeExam).isPresent();
        assertThat(maybeExam.orElse(null).getLanguageCode()).isEqualTo("ENU");

        final ExamAccommodation esnAcc = new ExamAccommodationBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withType(Accommodation.ACCOMMODATION_TYPE_LANGUAGE)
            .withCode("ESN")
            .withCreatedAt(Instant.now())
            .build();
        examAccommodationCommandRepository.insert(ImmutableList.of(esnAcc));

        final Optional<Exam> maybeUpdatedExam = examQueryRepository.getExamById(exam.getId());
        assertThat(maybeUpdatedExam).isPresent();
        assertThat(maybeUpdatedExam.orElse(null).getLanguageCode()).isEqualTo("ESN");
    }

    @Test
    public void shouldReturnLastResponseDate() {
        Instant pausedAt = Instant.now().minus(50000);
        Instant pageCreatedAt = Instant.now().minus(70000);
        Instant lastResponseSubmittedAt = Instant.now().minus(20000);
        Instant earlierResponseSubmittedAt = Instant.now().minus(30000);

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), pausedAt)
            .build();
        examCommandRepository.insert(exam);

        insertTestDataForResponses(pageCreatedAt, lastResponseSubmittedAt, earlierResponseSubmittedAt, exam);

        Optional<Instant> maybeLastTimeStudentResponded = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimeStudentResponded).isPresent();
        assertThat(maybeLastTimeStudentResponded.get()).isGreaterThanOrEqualTo(lastResponseSubmittedAt);
    }

    @Test
    public void shouldReturnLastPageCreated() {
        Instant pausedAt = Instant.now().minus(50000);
        Instant pageCreatedAt = Instant.now().minus(20000);
        Instant lastResponseSubmittedAt = Instant.now().minus(40000);
        Instant earlierResponseSubmittedAt = Instant.now().minus(30000);

        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE), pausedAt)
            .withChangedAt(pausedAt)
            .build();
        examCommandRepository.insert(exam);

        insertTestDataForResponses(pageCreatedAt, lastResponseSubmittedAt, earlierResponseSubmittedAt, exam);

        Optional<Instant> maybeLastTimeStudentResponded = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimeStudentResponded).isPresent();
        assertThat(maybeLastTimeStudentResponded.get()).isGreaterThanOrEqualTo(pageCreatedAt);
    }

    @Test
    public void shouldReturnEmptyForNullLastActivity() {
        Exam exam = new ExamBuilder()
          .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
          .build();
        examCommandRepository.insert(exam);

        Optional<Instant> maybeLastTimeStudentResponded = examQueryRepository.findLastStudentActivity(exam.getId());
        assertThat(maybeLastTimeStudentResponded).isNotPresent();
    }

    private void insertTestDataForResponses(Instant pageCreatedAt, Instant lastResponseSubmittedAt, Instant earlierResponseSubmittedAt, Exam exam) {
        MapSqlParameterSource testParams = new MapSqlParameterSource("examId", exam.getId().toString())
            .addValue("pageCreatedAt", new Timestamp(pageCreatedAt.getMillis()))
            .addValue("lastResponseSubmittedAt", new Timestamp(lastResponseSubmittedAt.getMillis()))
            .addValue("earlierResponseSubmittedAt", new Timestamp(earlierResponseSubmittedAt.getMillis()));

        final String insertSegmentSQL =
            "INSERT INTO exam_segment(exam_id, segment_key, segment_id, segment_position, created_at)" +
                "VALUES (:examId, 'segment-key-1', 'segment-id-1', 1, UTC_TIMESTAMP())";
        final String insertPageSQL =
            "INSERT INTO exam_page (id, page_position, segment_key, item_group_key, exam_id, created_at) " +
                "VALUES (805, 1, 'segment-key-1', 'GroupKey1', :examId, :pageCreatedAt)";
        final String insertPageEventSQL =
            "INSERT INTO exam_page_event (exam_page_id, exam_id, started_at, created_at) VALUES (805, :examId, UTC_TIMESTAMP(), UTC_TIMESTAMP())";
        final String insertItemSQL =
            "INSERT INTO exam_item (id, item_key, assessment_item_bank_key, assessment_item_key, item_type, exam_page_id, position, item_file_path, created_at, group_id)" +
                "VALUES (2112, '187-1234', 187, 1234, 'MS', 805, 1, '/path/to/item/187-1234.xml', UTC_TIMESTAMP(), 'group-123')";
        final String insertResponsesSQL =
            "INSERT INTO exam_item_response (id, exam_item_id, exam_id, response, sequence, created_at) " +
                "VALUES " +
                "(1337, 2112, :examId, 'Response 1', 1, :lastResponseSubmittedAt), " +
                "(1338, 2112, :examId, 'Response 2', 1, :earlierResponseSubmittedAt)";

        jdbcTemplate.update(insertSegmentSQL, testParams);
        jdbcTemplate.update(insertPageSQL, testParams);
        jdbcTemplate.update(insertPageEventSQL, testParams);
        jdbcTemplate.update(insertItemSQL, testParams);
        jdbcTemplate.update(insertResponsesSQL, testParams);
    }

    @Test
    public void shouldReturnSingleAbility() {
        UUID examId = UUID.randomUUID();
        List<Ability> oneAbility = examQueryRepository.findAbilities(examId,
            "clientName", "ELA", 9999L);
        assertThat(oneAbility).hasSize(1);
        Ability myAbility = oneAbility.get(0);
        // Should not be the same exam
        assertThat(myAbility.getExamId()).isNotEqualTo(examId);
        assertThat(myAbility.getAssessmentId()).isEqualTo("assessmentId3");
        assertThat(myAbility.getAttempts()).isEqualTo(2);
        assertThat(myAbility.getDateScored()).isLessThan(java.time.Instant.now());
    }

    @Test
    public void shouldGetAllExamsInASession() {
        List<Exam> exams = examQueryRepository.findAllExamsInSessionWithStatus(mockSessionId, statusesThatCanTransitionToPaused);

        assertThat(exams).hasSize(3);
        assertThat(exams).doesNotContain(examsInSession.stream().filter(exam -> exam.getStatus().getCode().equals(ExamStatusCode.STATUS_FAILED)).findAny().get());
    }

    @Test
    public void shouldReturnAnEmptyListWhenFindingAllExamsForASessionIdThatDoesNotExist() {
        List<Exam> exams = examQueryRepository.findAllExamsInSessionWithStatus(UUID.randomUUID(), statusesThatCanTransitionToPaused);

        assertThat(exams).isEmpty();
    }

    private void insertExamScoresData() {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", currentExamId.toString())
            .addValue("measureLabel", "Measure-Label")
            .addValue("value", 50)
            .addValue("measureOf", "measure-of")
            .addValue("useForAbility", 1);

        final String SQL =
            "INSERT INTO" +
                "   exam_scores (exam_id, measure_label, value, measure_of, use_for_ability) " +
                "VALUES(:examId, :measureLabel, :value, :measureOf, :useForAbility)";

        jdbcTemplate.update(SQL, parameters);
    }

    @Test
    public void shouldReturnListOfExamsPendingApproval() {
        List<Exam> examsPendingApproval = examQueryRepository.getExamsPendingApproval(mockSessionId);
        assertThat(examsPendingApproval.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnListOfExamsForStudent() {
        final long studentId = 10;
        List<Exam> exams = examQueryRepository.findAllExamsForStudent(studentId);
        assertThat(exams).hasSize(2);
    }
}
