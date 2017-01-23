package tds.exam.repositories.impl;

import org.joda.time.Instant;
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

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tds.assessment.Algorithm;
import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;
import tds.exam.models.ExamPage;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamItemCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;

    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;

    private Exam mockExam = new ExamBuilder().build();
    private ExamPage mockPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .build();
    @Before
    public void SetUp() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        ExamPageCommandRepository examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);

        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withSegmentId(mockPage.getSegmentId())
            .withSegmentKey(mockPage.getSegmentKey())
            .withSegmentPosition(mockPage.getSegmentPosition())
            .withExamId(mockExam.getId())
            .build();

        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment));
        examPageCommandRepository.insert(Arrays.asList(mockPage));
    }

    @Test
    public void shouldInsertAnExamItem() {
        List<ExamPage> pages = examPageQueryRepository.findAll(mockExam.getId());
        assertThat(pages).hasSize(1);

        ExamPage insertedPage = pages.get(0);
        ExamItem mockExamItem = new ExamItemBuilder()
            .withExamPageId(insertedPage.getId())
            .build();

        examItemCommandRepository.insert(mockExamItem);
    }

    @Test
    public void shouldInsertAnExamItemResponse() {
        List<ExamPage> pages = examPageQueryRepository.findAll(mockExam.getId());
        assertThat(pages).hasSize(1);

        ExamPage insertedPage = pages.get(0);
        ExamItem mockExamItem = new ExamItemBuilder()
            .withExamPageId(insertedPage.getId())
            .withAssessmentItem(new tds.assessment.Item("UNIT-TEST"))
            .build();

        examItemCommandRepository.insert(mockExamItem);

        ExamItem savedExamItem = getInsertedExamItems(insertedPage.getId()).get(0);

        ExamItemResponse response = new ExamItemResponse.Builder()
            .withExamItemId(savedExamItem.getId())
            .withResponse("unit test response")
            .withCreatedAt(Instant.now().minus(20000))
            .build();

        examItemCommandRepository.insertResponses(response);
    }

    /**
     * Convenience method for getting the {@link tds.exam.models.ExamItem}s that were created as part of the integration
     * test.
     *
     * @param pageId The id of the {@link tds.exam.models.ExamPage} to which the item(s) belong
     * @return A collection of {@link tds.exam.models.ExamItem}s for an {@link tds.exam.models.ExamPage}
     */
    private List<ExamItem> getInsertedExamItems(long pageId) {
        SqlParameterSource parameters = new MapSqlParameterSource("pageId", pageId);
        final String SQL = "SELECT * FROM exam_item WHERE exam_page_id = :pageId";

        return commandJdbcTemplate.query(SQL, parameters, (rs, row) -> new ExamItem.Builder()
            .withId(rs.getLong("id"))
            .withExamPageId(rs.getLong("exam_page_id"))
            .withItemKey(rs.getString("item_key"))
            .withPosition(rs.getInt("position"))
            .withSelected(rs.getBoolean("is_selected"))
            .withMarkedForReview(rs.getBoolean("is_marked_for_review"))
            .withFieldTest(rs.getBoolean("is_fieldtest"))
            .build());
    }
}
