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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamItemResponseScoreBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
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
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamItemQueryRepository examItemQueryRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    private Exam mockExam = new ExamBuilder().build();
    private ExamPage mockPage = new ExamPageBuilder()
        .withExamId(mockExam.getId())
        .build();

    @Before
    public void SetUp() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        ExamSegmentCommandRepository examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(jdbcTemplate);
        ExamPageCommandRepository examPageCommandRepository = new ExamPageCommandRepositoryImpl(jdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(jdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(jdbcTemplate);
        examItemQueryRepository = new ExamItemQueryRepositoryImpl(jdbcTemplate);

        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withSegmentKey(mockPage.getSegmentKey())
            .withExamId(mockExam.getId())
            .build();

        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Collections.singletonList(mockExamSegment));
        examPageCommandRepository.insert(mockPage);
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

        ExamItemResponse response = new ExamItemResponseBuilder()
            .withExamItemId(savedExamItem.getId())
            .build();

        examItemCommandRepository.insertResponses(response);
    }

    @Test
    public void shouldReturnHighestItemPosition() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        final UUID item1Id = UUID.randomUUID();
        final UUID item2Id = UUID.randomUUID();
        final UUID item3Id = UUID.randomUUID();

        MapSqlParameterSource testParams = new MapSqlParameterSource("examId", exam.getId().toString())
            .addValue("item1Id", item1Id.toString())
            .addValue("item2Id", item2Id.toString())
            .addValue("item3Id", item3Id.toString());

        final String insertSegmentSQL =
            "INSERT INTO exam_segment(exam_id, segment_key, segment_id, segment_position, created_at)" +
                "VALUES (:examId, 'segment-key-1', 'segment-id-1', 1, UTC_TIMESTAMP() )";
        final String insertPageSQL =
            "INSERT INTO exam_page (id, page_position, segment_key, item_group_key, exam_id, created_at) " +
                "VALUES (805, 1, 'segment-key-1', 'GroupKey1', :examId, UTC_TIMESTAMP()), (806, 2, 'segment-key-1', 'GroupKey2', :examId, UTC_TIMESTAMP())";
        final String insertPageEventSQL = // Create two pages, second page is deleted
            "INSERT INTO exam_page_event (exam_page_id, started_at, deleted_at, created_at) " +
                "VALUES (805, now(), NULL, UTC_TIMESTAMP()), (806, UTC_TIMESTAMP(), UTC_TIMESTAMP(), UTC_TIMESTAMP())";
        final String insertItemSQL = // Two items on first page, 1 item on deleted (second) page
            "INSERT INTO exam_item (id, item_key, assessment_item_bank_key, assessment_item_key, item_type, exam_page_id, position, item_file_Path, created_at, group_id)" +
                "VALUES " +
                "(:item1Id, '187-1234', 187, 1234, 'MS', 805, 1, '/path/to/item/187-1234.xml', UTC_TIMESTAMP(), 'group-id-123')," +
                "(:item2Id, '187-1235', 187, 1235, 'MS', 805, 2, '/path/to/item/187-1235.xml', UTC_TIMESTAMP(), 'group-id-123')," +
                "(:item3Id, '187-1236', 187, 1236, 'ER', 806, 3, '/path/to/item/187-1236.xml', UTC_TIMESTAMP(), 'group-id-123')";

        jdbcTemplate.update(insertSegmentSQL, testParams);
        jdbcTemplate.update(insertPageSQL, testParams);
        jdbcTemplate.update(insertPageEventSQL, testParams);
        jdbcTemplate.update(insertItemSQL, testParams);

        ExamItemResponse examItem1Response = new ExamItemResponseBuilder()
            .withExamItemId(item1Id)
            .withResponse("response1")
            .withSequence(1)
            .withScore(new ExamItemResponseScoreBuilder()
                .withScore(100)
                .withScoringStatus(ExamScoringStatus.SCORED)
                .withScoringRationale("Scored by unit test")
                .withScoringDimensions("scoring dimension")
                .withScoredAt(Instant.now().minus(20000))
                .build())
            .build();

        ExamItemResponse examItem2Response = new ExamItemResponseBuilder()
            .withExamItemId(item2Id)
            .withResponse("response2")
            .withSequence(2)
            .build();

        ExamItemResponse examDeletedItemResponse = new ExamItemResponseBuilder()
            .withExamItemId(item3Id)
            .withResponse("response3")
            .withSequence(3)
            .build();

        examItemCommandRepository.insertResponses(examItem1Response, examItem2Response, examDeletedItemResponse);
        int currentPosition = examItemQueryRepository.getCurrentExamItemPosition(exam.getId());
        assertThat(currentPosition).isEqualTo(2);
    }


    /**
     * Convenience method for getting the {@link tds.exam.ExamItem}s that were created as part of the integration
     * test.
     *
     * @param pageId The id of the {@link tds.exam.ExamPage} to which the item(s) belong
     * @return A collection of {@link tds.exam.ExamItem}s for an {@link tds.exam.ExamPage}
     */
    private List<ExamItem> getInsertedExamItems(UUID pageId) {
        SqlParameterSource parameters = new MapSqlParameterSource("pageId", pageId.toString());
        final String SQL = "SELECT * FROM exam_item WHERE exam_page_id = :pageId";

        return jdbcTemplate.query(SQL, parameters, (rs, row) -> new ExamItem.Builder(UUID.fromString(rs.getString("id")))
            .withItemKey(rs.getString("item_key"))
            .withAssessmentItemBankKey(rs.getLong("assessment_item_bank_key"))
            .withAssessmentItemKey(rs.getLong("assessment_item_key"))
            .withItemType(rs.getString("item_type"))
            .withExamPageId(UUID.fromString(rs.getString("exam_page_id")))
            .withPosition(rs.getInt("position"))
            .withFieldTest(rs.getBoolean("is_fieldtest"))
            .withRequired(rs.getBoolean("is_required"))
            .withItemFilePath(rs.getString("item_file_path"))
            .withStimulusFilePath(rs.getString("stimulus_file_path"))
            .withGroupId(rs.getString("group_id"))
            .build());
    }
}
