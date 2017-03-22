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

import java.util.Collections;
import java.util.Optional;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamItemQueryRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamItemQueryRepository examItemQueryRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    private Exam mockExam = new ExamBuilder().build();
    private ExamPage mockPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .build();

    @Before
    public void SetUp() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(jdbcTemplate);
        ExamPageCommandRepository examPageCommandRepository = new ExamPageCommandRepositoryImpl(jdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(jdbcTemplate);
        examItemQueryRepository = new ExamItemQueryRepositoryImpl(jdbcTemplate);

        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withSegmentId(mockPage.getSegmentId())
            .withSegmentKey(mockPage.getSegmentKey())
            .withSegmentPosition(mockPage.getSegmentPosition())
            .withExamId(mockExam.getId())
            .build();

        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Collections.singletonList(mockExamSegment));
        examPageCommandRepository.insert(mockPage);
    }

    @Test
    public void shouldFindItemAndResponseByExamAndPosition() {
        ExamItem examItem = new ExamItemBuilder().
            withExamPageId(mockPage.getId()).
            build();

        examItemCommandRepository.insert(examItem);

        ExamItemResponse examItemResponse = new ExamItemResponse.Builder()
            .withResponse("test")
            .withExamItemId(examItem.getId())
            .withSelected(false)
            .withSequence(1)
            .build();

        ExamItemResponse examItemScoredResponse = new ExamItemResponse.Builder()
            .fromExamItemResponse(examItemResponse)
            .withScore(new ExamItemResponseScore.Builder()
                .withScore(1)
                .withScoredAt(Instant.now())
                .withScoringDimensions("dimensions")
                .withScoringRationale("rationale")
                .withScoringStatus(ExamScoringStatus.SCORED)
                .build())
            .build();

        examItemCommandRepository.insertResponses(examItemResponse, examItemScoredResponse);

        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(mockExam.getId(), examItem.getPosition());

        assertThat(maybeExamItem).isPresent();
    }
}
