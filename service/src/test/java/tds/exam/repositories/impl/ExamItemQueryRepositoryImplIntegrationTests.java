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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamItemQueryRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamItemQueryRepository examItemQueryRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    private Exam mockExam = new ExamBuilder().build();
    private ExamPage mockPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .build();

    private ExamPageCommandRepository examPageCommandRepository;

    @Before
    public void SetUp() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(jdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(jdbcTemplate);
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
            .withMarkedForReview(true)
            .build();

        ExamItemResponseScore initialScore = new ExamItemResponseScore.Builder()
            .withScore(1)
            .withScoredAt(Instant.now())
            .withScoringDimensions("dimensions")
            .withScoringRationale("rationale")
            .withScoringStatus(ExamScoringStatus.SCORED)
            .withScoreLatency(1000)
            .withScoreSentAt(Instant.now().minus(10000))
            .withScoredAt(Instant.now())
            .withScoreMark(UUID.randomUUID())
            .build();

        ExamItemResponse examItemScoredResponse = ExamItemResponse.Builder
            .fromExamItemResponse(examItemResponse)
            .withScore(initialScore)
            .build();

        examItemCommandRepository.insertResponses(examItemResponse, examItemScoredResponse);

        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(mockExam.getId(), examItem.getPosition());

        assertThat(maybeExamItem).isPresent();

        ExamItem fetchedItem = maybeExamItem.get();
        assertThat(fetchedItem.getItemType()).isEqualTo(examItem.getItemType());
        assertThat(fetchedItem.getAssessmentItemBankKey()).isEqualTo(examItem.getAssessmentItemBankKey());
        assertThat(fetchedItem.getAssessmentItemKey()).isEqualTo(examItem.getAssessmentItemKey());
        assertThat(fetchedItem.getExamPageId()).isEqualTo(examItem.getExamPageId());
        assertThat(fetchedItem.getPosition()).isEqualTo(examItem.getPosition());

        assertThat(fetchedItem.getResponse().isPresent()).isTrue();

        ExamItemResponse response = fetchedItem.getResponse().get();

        assertThat(response.getResponse()).isEqualTo("test");
        assertThat(response.getSequence()).isEqualTo(1);
        assertThat(response.getScore().isPresent()).isTrue();
        assertThat(response.isMarkedForReview()).isTrue();
        assertThat(response.getExamItemId()).isEqualTo(fetchedItem.getId());
        assertThat(response.getCreatedAt()).isLessThanOrEqualTo(Instant.now());

        ExamItemResponseScore score = response.getScore().get();

        assertThat(score.getScore()).isEqualTo(1);
        assertThat(score.getScoredAt()).isNotNull();
        assertThat(score.getScoringDimensions()).isEqualTo("dimensions");
        assertThat(score.getScoringRationale()).isEqualTo("rationale");
        assertThat(score.getScoringStatus()).isEqualTo(ExamScoringStatus.SCORED);
        assertThat(score.getScoreLatency()).isEqualTo(1000);
        assertThat(score.getScoreSentAt()).isEqualTo(initialScore.getScoreSentAt());
        assertThat(score.getScoredAt()).isEqualTo(initialScore.getScoredAt());
        assertThat(score.getScoreMark()).isEqualTo(initialScore.getScoreMark());
    }

    @Test
    public void shouldFindItemWithoutResponse() {
        ExamItem examItem = new ExamItemBuilder().
            withExamPageId(mockPage.getId()).
            build();

        examItemCommandRepository.insert(examItem);

        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(mockExam.getId(), examItem.getPosition());

        assertThat(maybeExamItem).isPresent();

        ExamItem fetchedItem = maybeExamItem.get();
        assertThat(fetchedItem.getItemType()).isEqualTo(examItem.getItemType());
        assertThat(fetchedItem.getAssessmentItemBankKey()).isEqualTo(examItem.getAssessmentItemBankKey());
        assertThat(fetchedItem.getAssessmentItemKey()).isEqualTo(examItem.getAssessmentItemKey());
        assertThat(fetchedItem.getExamPageId()).isEqualTo(examItem.getExamPageId());
        assertThat(fetchedItem.getPosition()).isEqualTo(examItem.getPosition());

        assertThat(fetchedItem.getResponse().isPresent()).isFalse();
    }

    @Test
    public void shouldBeEmptyIfItemCannotBeFound() {
        ExamItem examItem = new ExamItemBuilder().
            withExamPageId(mockPage.getId()).
            build();

        examItemCommandRepository.insert(examItem);

        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(mockExam.getId(), examItem.getPosition());

        assertThat(maybeExamItem).isPresent();

        examPageCommandRepository.deleteAll(mockExam.getId());

        maybeExamItem = examItemQueryRepository.findExamItemAndResponse(mockExam.getId(), examItem.getPosition());

        assertThat(maybeExamItem).isNotPresent();
    }

    @Test
    public void shouldFindItemAndResponseByExam() {
        ExamItem examItem = new ExamItemBuilder().
            withExamPageId(mockPage.getId()).
            build();

        examItemCommandRepository.insert(examItem);

        ExamItemResponse examItemResponse = new ExamItemResponse.Builder()
            .withResponse("test")
            .withExamItemId(examItem.getId())
            .withSelected(false)
            .withSequence(1)
            .withMarkedForReview(true)
            .build();

        ExamItemResponseScore initialScore = new ExamItemResponseScore.Builder()
            .withScore(1)
            .withScoredAt(Instant.now())
            .withScoringDimensions("dimensions")
            .withScoringRationale("rationale")
            .withScoringStatus(ExamScoringStatus.SCORED)
            .withScoreLatency(1000)
            .withScoreSentAt(Instant.now().minus(10000))
            .withScoredAt(Instant.now())
            .withScoreMark(UUID.randomUUID())
            .build();

        ExamItemResponse examItemScoredResponse = ExamItemResponse.Builder
            .fromExamItemResponse(examItemResponse)
            .withScore(initialScore)
            .build();

        examItemCommandRepository.insertResponses(examItemResponse, examItemScoredResponse);

        List<ExamItem> examItems = examItemQueryRepository.findExamItemAndResponses(mockExam.getId());

        assertThat(examItems).hasSize(1);

        ExamItem fetchedItem = examItems.get(0);
        assertThat(fetchedItem.getItemType()).isEqualTo(examItem.getItemType());
        assertThat(fetchedItem.getAssessmentItemBankKey()).isEqualTo(examItem.getAssessmentItemBankKey());
        assertThat(fetchedItem.getAssessmentItemKey()).isEqualTo(examItem.getAssessmentItemKey());
        assertThat(fetchedItem.getExamPageId()).isEqualTo(examItem.getExamPageId());
        assertThat(fetchedItem.getPosition()).isEqualTo(examItem.getPosition());

        assertThat(fetchedItem.getResponse().isPresent()).isTrue();

        ExamItemResponse response = fetchedItem.getResponse().get();

        assertThat(response.getResponse()).isEqualTo("test");
        assertThat(response.getSequence()).isEqualTo(1);
        assertThat(response.getScore().isPresent()).isTrue();
        assertThat(response.isMarkedForReview()).isTrue();

        ExamItemResponseScore score = response.getScore().get();

        assertThat(score.getScore()).isEqualTo(1);
        assertThat(score.getScoredAt()).isNotNull();
        assertThat(score.getScoringDimensions()).isEqualTo("dimensions");
        assertThat(score.getScoringRationale()).isEqualTo("rationale");
        assertThat(score.getScoringStatus()).isEqualTo(ExamScoringStatus.SCORED);
        assertThat(score.getScoreLatency()).isEqualTo(1000);
        assertThat(score.getScoreSentAt()).isEqualTo(initialScore.getScoreSentAt());
        assertThat(score.getScoredAt()).isEqualTo(initialScore.getScoredAt());
        assertThat(score.getScoreMark()).isEqualTo(initialScore.getScoreMark());
    }
}
