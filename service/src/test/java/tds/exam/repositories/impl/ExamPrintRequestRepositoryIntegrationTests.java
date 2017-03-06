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

import java.util.Map;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;

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

    @Before
    public void setUp() {
        examPrintRequestQueryRepository = new ExamPrintRequestQueryRepositoryImpl(commandJdbcTemplate);
        examPrintRequestCommandRepository = new ExamPrintRequestCommandRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldReturnCountsForExamsInSession() {
        UUID sessionId = UUID.randomUUID();
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
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
            .withDeniedAt(null)
            .withApprovedAt(null)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_ITEM)
            .build();
        ExamPrintRequest exam1Request2 = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam1.getId())
            .withCreatedAt(null)
            .withDeniedAt(null)
            .withApprovedAt(null)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 2
        ExamPrintRequest exam2Request = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withDeniedAt(null)
            .withApprovedAt(null)
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();
        // Should not be included in count - fulfilled
        ExamPrintRequest exam2FulfilledRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(sessionId)
            .withExamId(exam2.getId())
            .withCreatedAt(null)
            .withDeniedAt(null)
            .withApprovedAt(Instant.now().minus(99999))
            .withType(ExamPrintRequest.REQUEST_TYPE_EMBOSS_PASSAGE)
            .build();

        // Exam 3 - different session, should not be included
        ExamPrintRequest diffSessionExamRequest = new ExamPrintRequest.Builder(UUID.randomUUID())
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .withSessionId(diffSessionExam.getSessionId())
            .withExamId(diffSessionExam.getId())
            .withCreatedAt(null)
            .withDeniedAt(null)
            .withApprovedAt(null)
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
