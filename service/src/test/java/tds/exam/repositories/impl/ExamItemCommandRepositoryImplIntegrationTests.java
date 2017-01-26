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

import java.util.Arrays;
import java.util.List;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.ExamSegment;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemResponseCommandRepository;
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
    private ExamItemResponseCommandRepository examItemResponseCommandRepository;
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
        examItemResponseCommandRepository = new ExamItemResponseCommandRepositoryImpl(commandJdbcTemplate);

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
            .build();

        examItemCommandRepository.insert(mockExamItem);

        ExamItem savedExamItem = getInsertedExamItems(insertedPage.getId()).get(0);

        ExamItemResponse response = new ExamItemResponse.Builder()
            .withExamItemId(savedExamItem.getId())
            .withResponse("unit test response")
            .withValid(true)
            .withCreatedAt(Instant.now().minus(20000))
            .build();

        examItemResponseCommandRepository.insertResponses(response);
    }

    /**
     * Convenience method for getting the {@link tds.exam.ExamItem}s that were created as part of the integration
     * test.
     *
     * @param pageId The id of the {@link tds.exam.ExamPage} to which the item(s) belong
     * @return A collection of {@link tds.exam.ExamItem}s for an {@link tds.exam.ExamPage}
     */
    private List<ExamItem> getInsertedExamItems(long pageId) {
        SqlParameterSource parameters = new MapSqlParameterSource("pageId", pageId);
        final String SQL = "SELECT * FROM exam_item WHERE exam_page_id = :pageId";

        return commandJdbcTemplate.query(SQL, parameters, (rs, row) -> new ExamItem.Builder()
            .withId(rs.getLong("id"))
            .withItemKey(rs.getString("item_key"))
            .withAssessmentItemBankKey(rs.getLong("assessment_item_bank_key"))
            .withAssessmentItemKey(rs.getLong("assessment_item_key"))
            .withItemType(rs.getString("item_type"))
            .withExamPageId(rs.getLong("exam_page_id"))
            .withPosition(rs.getInt("position"))
            .withFieldTest(rs.getBoolean("is_fieldtest"))
            .withRequired(rs.getBoolean("is_required"))
            .withSelected(rs.getBoolean("is_selected"))
            .withMarkedForReview(rs.getBoolean("is_marked_for_review"))
            .withItemFilePath(rs.getString("item_file_path"))
            .withStimulusFilePath(rs.getString("stimulus_file_path"))
            .build());
    }
}
