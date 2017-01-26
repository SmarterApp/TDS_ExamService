package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.Exam;
import tds.exam.ExamItemResponse;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemResponseCommandRepository;
import tds.exam.repositories.ExamResponseQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamItemResponseRepositoryIntegrationTests {
    private ExamResponseQueryRepository examResponseQueryRepository;
    private ExamItemResponseCommandRepository examItemResponseCommandRepository;
    private ExamCommandRepository examCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;


    @Before
    public void setUp() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examItemResponseCommandRepository = new ExamItemResponseCommandRepositoryImpl(jdbcTemplate);
        examResponseQueryRepository = new ExamResponseQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldReturnHighestItemPosition() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        final long item1Id = 2112;
        final long item2Id = 2113;
        final long item3Id = 2114;

        MapSqlParameterSource testParams = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(exam.getId()))
            .addValue("item1Id", item1Id)
            .addValue("item2Id", item2Id)
            .addValue("item3Id", item3Id);

        final String insertSegmentSQL =
            "INSERT INTO exam_segment(exam_id, segment_key, segment_id, segment_position, created_at)" +
                "VALUES (:examId, 'segment-key-1', 'segment-id-1', 1, CURRENT_TIMESTAMP )";
        final String insertPageSQL =
            "INSERT INTO exam_page (id, page_position, exam_segment_key, item_group_key, exam_id) " +
                "VALUES (805, 1, 'segment-key-1', 'GroupKey1', :examId), (806, 2, 'segment-key-1', 'GroupKey2', :examId)";
        final String insertPageEventSQL = // Create two pages, second page is deleted
            "INSERT INTO exam_page_event (exam_page_id, started_at, deleted_at) VALUES (805, now(), NULL), (806, now(), now())";
        final String insertItemSQL = // Two items on first page, 1 item on deleted (second) page
            "INSERT INTO exam_item (id, item_key, assessment_item_bank_key, assessment_item_key, item_type, exam_page_id, position, item_file_Path)" +
                "VALUES " +
                "(:item1Id, '187-1234', 187, 1234, 'MS', 805, 1, '/path/to/item/187-1234.xml')," +
                "(:item2Id, '187-1235', 187, 1235, 'MS', 805, 2, '/path/to/item/187-1235.xml')," +
                "(:item3Id, '187-1236', 187, 1236, 'ER', 806, 3, '/path/to/item/187-1236.xml')";

        jdbcTemplate.update(insertSegmentSQL, testParams);
        jdbcTemplate.update(insertPageSQL, testParams);
        jdbcTemplate.update(insertPageEventSQL, testParams);
        jdbcTemplate.update(insertItemSQL, testParams);

        ExamItemResponse examItem1Response = new ExamItemResponse.Builder()
            .withExamItemId(item1Id)
            .withResponse("response1")
            .build();

        ExamItemResponse examItem2Response = new ExamItemResponse.Builder()
            .withExamItemId(item2Id)
            .withResponse("response2")
            .build();

        ExamItemResponse examDeletedItemResponse = new ExamItemResponse.Builder()
            .withExamItemId(item3Id)
            .withResponse("response3")
            .build();

        examItemResponseCommandRepository.insertResponses(examItem1Response, examItem2Response, examDeletedItemResponse);
        int currentPosition = examResponseQueryRepository.getCurrentExamItemPosition(exam.getId());
        assertThat(currentPosition).isEqualTo(2);
    }
}
