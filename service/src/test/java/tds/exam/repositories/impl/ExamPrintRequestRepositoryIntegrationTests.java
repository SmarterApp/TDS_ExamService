package tds.exam.repositories.impl;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamPrintRequestRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamPrintRequestQueryRepository examPrintRequestQueryRepository;
    private ExamPrintRequestCommandRepository examPrintRequestCommandRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamSegmentCommandRepository examSegmentCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    @Before
    public void setUp() {
        examPrintRequestQueryRepository = new ExamPrintRequestQueryRepositoryImpl(commandJdbcTemplate);
        examPrintRequestCommandRepository = new ExamPrintRequestCommandRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldReturnExamPrintRequestNoResponses() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withExamId(exam.getId())
            .build();

        examPrintRequestCommandRepository.insert(request);

        Optional<ExamPrintRequest> maybeRequest = examPrintRequestQueryRepository.findExamPrintRequest(request.getId());
        assertThat(maybeRequest).isPresent();
        assertThat(maybeRequest.get()).isEqualTo(request);
    }

    @Test
    public void shouldReturnExamPrintRequestWithMultipleResponses() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        ExamSegment segment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .build();

        examSegmentCommandRepository.insert(Arrays.asList(segment));

        ExamPage page = new ExamPage.Builder()
            .fromExamPage(random(ExamPage.class))
            .withExamId(exam.getId())
            .withDeletedAt(null)
            .withSegmentKey(segment.getSegmentKey())
            .withItemGroupKey("itemgroupkey1")
            .build();

        examPageCommandRepository.insert(page);

        ExamItem itemWithoutResponses = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(page.getId())
            .withPosition(1)
            .withItemKey("itemkey1")
            .build();
        ExamItem itemWithResponses = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(page.getId())
            .withPosition(2)
            .withItemKey("itemkey2")
            .build();
        examItemCommandRepository.insert(itemWithoutResponses, itemWithResponses);

        ExamItemResponse examItemResponse1 = new ExamItemResponse.Builder()
            .fromExamItemResponse(random(ExamItemResponse.class))
            .withCreatedAt(Instant.now().minus(20000))
            .withExamItemId(itemWithResponses.getId())
            .build();

        examItemCommandRepository.insertResponses(examItemResponse1);

        ExamItemResponse examItemResponse2 = new ExamItemResponse.Builder()
            .fromExamItemResponse(random(ExamItemResponse.class))
            .withExamItemId(itemWithResponses.getId())
            .build();

        examItemCommandRepository.insertResponses(examItemResponse2);

        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withExamId(exam.getId())
            .withItemPosition(itemWithResponses.getPosition())
            .build();

        examPrintRequestCommandRepository.insert(request);

        Optional<ExamPrintRequest> maybeRequest = examPrintRequestQueryRepository.findExamPrintRequest(request.getId());
        assertThat(maybeRequest).isPresent();
        assertThat(maybeRequest.get()).isEqualTo(request);
        assertThat(maybeRequest.get().getItemResponse()).isEqualTo(examItemResponse2.getResponse());
    }

    @Test
    public void shouldReturnEmptyExamPrintRequest() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        ExamPrintRequest request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withExamId(exam.getId())
            .build();

        examPrintRequestCommandRepository.insert(request);

        Optional<ExamPrintRequest> maybeRequest = examPrintRequestQueryRepository.findExamPrintRequest(UUID.randomUUID());
        assertThat(maybeRequest).isNotPresent();
    }

    @Test
    public void shouldReturnUnfulfilledPrintRequestsForExamAndSession() {
        UUID sessionId = UUID.randomUUID();
        // Has 2 requests
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        // Has 1 request
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();
        Exam diffSessionExam = new ExamBuilder().build();

        examCommandRepository.insert(exam1);
        examCommandRepository.insert(exam2);
        examCommandRepository.insert(diffSessionExam);

        // Exam 1
        ExamPrintRequest exam1Request1 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM)
            .build();
        ExamPrintRequest exam1Request2 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 2
        ExamPrintRequest exam2Request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();
        // Should not be included in count - fulfilled
        ExamPrintRequest exam2FulfilledRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withChangedAt(Instant.now().minus(99999))
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 3 - different session, should not be included
        ExamPrintRequest diffSessionExamRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(diffSessionExam.getSessionId())
            .withExamId(diffSessionExam.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        examPrintRequestCommandRepository.insert(exam1Request1);
        examPrintRequestCommandRepository.insert(exam1Request2);
        examPrintRequestCommandRepository.insert(exam2Request);
        examPrintRequestCommandRepository.insert(exam2FulfilledRequest);
        examPrintRequestCommandRepository.insert(diffSessionExamRequest);

        List<ExamPrintRequest> exam1Requests = examPrintRequestQueryRepository.findUnfulfilledRequests(exam1.getId(), sessionId);
        assertThat(exam1Requests).containsExactlyInAnyOrder(exam1Request1, exam1Request2);
        List<ExamPrintRequest> exam2Requests = examPrintRequestQueryRepository.findUnfulfilledRequests(exam2.getId(), sessionId);
        assertThat(exam2Requests).containsExactlyInAnyOrder(exam2Request);
    }

    @Test
    public void shouldReturnApprovedPrintRequestsForSession() {
        UUID sessionId = UUID.randomUUID();
        // Has 2 requests
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        // Has 1 request
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();
        Exam diffSessionExam = new ExamBuilder().build();

        examCommandRepository.insert(exam1);
        examCommandRepository.insert(exam2);
        examCommandRepository.insert(diffSessionExam);

        // Exam 1
        ExamPrintRequest exam1Request1 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(Instant.now().minus(33333))
            .withChangedAt(Instant.now().minus(4444))
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM)
            .build();
        ExamPrintRequest exam1Request2 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(Instant.now().minus(55555))
            .withChangedAt(Instant.now())
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();
        ExamPrintRequest exam1RequestNotApproved = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(Instant.now().minus(55555))
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 2
        ExamPrintRequest exam2Request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(Instant.now().minus(55555))
            .withChangedAt(Instant.now())
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();
        // Should not be included in count - denied
        ExamPrintRequest exam2DeniedRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(Instant.now())
            .withChangedAt(Instant.now())
            .withStatus(ExamPrintRequestStatus.DENIED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 3 - different session, should not be included
        ExamPrintRequest diffSessionExamRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(diffSessionExam.getSessionId())
            .withExamId(diffSessionExam.getId())
            .withCreatedAt(null)
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withChangedAt(Instant.now())
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        examPrintRequestCommandRepository.insert(exam1Request1);
        examPrintRequestCommandRepository.insert(exam1Request2);
        examPrintRequestCommandRepository.insert(exam1RequestNotApproved);
        examPrintRequestCommandRepository.insert(exam2Request);
        examPrintRequestCommandRepository.insert(exam2DeniedRequest);
        examPrintRequestCommandRepository.insert(diffSessionExamRequest);

        List<ExamPrintRequest> exam1Requests = examPrintRequestQueryRepository.findApprovedRequests(sessionId);
        assertThat(exam1Requests).containsExactlyInAnyOrder(exam1Request1, exam1Request2, exam2Request);
    }

    @Test
    public void shouldReturnCountsForExamsInSession() {
        UUID sessionId = UUID.randomUUID();
        // Has 2 requests
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        // Has 1 request
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();
        Exam diffSessionExam = new ExamBuilder().build();

        examCommandRepository.insert(exam1);
        examCommandRepository.insert(exam2);
        examCommandRepository.insert(diffSessionExam);

        // Exam 1
        ExamPrintRequest exam1Request1 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM)
            .build();
        ExamPrintRequest exam1Request2 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 2
        ExamPrintRequest exam2Request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();
        // Should not be included in count - fulfilled
        ExamPrintRequest exam2FulfilledRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withStatus(ExamPrintRequestStatus.APPROVED)
            .withChangedAt(Instant.now().minus(99999))
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 3 - different session, should not be included
        ExamPrintRequest diffSessionExamRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(diffSessionExam.getSessionId())
            .withExamId(diffSessionExam.getId())
            .withCreatedAt(null)
            .withChangedAt(null)
            .withStatus(ExamPrintRequestStatus.SUBMITTED)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        examPrintRequestCommandRepository.insert(exam1Request1);
        examPrintRequestCommandRepository.insert(exam1Request2);
        examPrintRequestCommandRepository.insert(exam2Request);
        examPrintRequestCommandRepository.insert(exam2FulfilledRequest);
        examPrintRequestCommandRepository.insert(diffSessionExamRequest);

        Map<UUID, Integer> requestCounts = examPrintRequestQueryRepository.findRequestCountsForExamIds(sessionId,
            exam1.getId(), exam2.getId(), diffSessionExam.getId());
        assertThat(requestCounts).hasSize(2);
        assertThat(requestCounts.get(exam1.getId())).isEqualTo(2);
        assertThat(requestCounts.get(exam2.getId())).isEqualTo(1);
        assertThat(requestCounts.containsKey(diffSessionExam.getId())).isFalse();
    }

}
