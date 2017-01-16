package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class FieldTestItemGroupRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private FieldTestItemGroupQueryRepository fieldTestItemGroupQueryRepository;
    private FieldTestItemGroupCommandRepository fieldTestItemGroupCommandRepository;
    private ExamCommandRepository examCommandRepository;

    @Before
    public void setUp() {
        fieldTestItemGroupCommandRepository = new FieldTestItemGroupCommandRepositoryImpl(commandJdbcTemplate);
        fieldTestItemGroupQueryRepository = new FieldTestItemGroupQueryRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldReturnListOfNonDeletedRecords() {
        final Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        final UUID sessionId = UUID.randomUUID();
        final String segmentKey = "segkey";

        FieldTestItemGroup group1 = new FieldTestItemGroup.Builder()
            .withExamId(exam.getId())
            .withGroupId("groupid1")
            .withGroupKey("groupkey1")
            .withBlockId("A")
            .withPositionAdministered(7)
            .withLanguageCode("ENU")
            .withPosition(2)
            .withSegmentId("segid")
            .withSegmentKey(segmentKey)
            .withItemCount(1)
            .withSessionId(sessionId)
            .build();

        FieldTestItemGroup group2 = new FieldTestItemGroup.Builder()
            .withExamId(exam.getId())
            .withGroupId("groupid2")
            .withGroupKey("groupkey2")
            .withBlockId("B")
            .withLanguageCode("ENU")
            .withPosition(3)
            .withPositionAdministered(12)
            .withSegmentId("segid")
            .withSegmentKey(segmentKey)
            .withItemCount(2)
            .withSessionId(sessionId)
            .build();

        FieldTestItemGroup deletedGroup = new FieldTestItemGroup.Builder()
            .withExamId(exam.getId())
            .withGroupId("deleted-group-id")
            .withGroupKey("deleted-group-key")
            .withBlockId("C")
            .withLanguageCode("ENU")
            .withPosition(2)
            .withSegmentId("segid")
            .withSegmentKey(segmentKey)
            .withItemCount(1)
            .withSessionId(sessionId)
            .withDeletedAt(Instant.now().minusMillis(100000))
            .build();

        fieldTestItemGroupCommandRepository.insert(Arrays.asList(group1, group2, deletedGroup));

        List<FieldTestItemGroup> retFieldTestItemGroups = fieldTestItemGroupQueryRepository.find(exam.getId(), segmentKey);
        assertThat(retFieldTestItemGroups).containsExactly(group1, group2);

        FieldTestItemGroup retGroup1 = retFieldTestItemGroups.stream()
                .filter(fieldTestItemGroup -> fieldTestItemGroup.equals(group1))
                .findFirst().get();

        assertThat(retGroup1.getBlockId()).isEqualTo(group1.getBlockId());
        assertThat(retGroup1.getGroupId()).isEqualTo(group1.getGroupId());
        assertThat(retGroup1.isDeleted()).isFalse();
        assertThat(retGroup1.getCreatedAt()).isNotNull();
        assertThat(retGroup1.getSessionId()).isEqualTo(sessionId);
        assertThat(retGroup1.getSegmentKey()).isEqualTo(segmentKey);
        assertThat(retGroup1.getSegmentId()).isEqualTo("segid");
        assertThat(retGroup1.getItemCount()).isEqualTo(group1.getItemCount());
        assertThat(retGroup1.getPosition()).isEqualTo(group1.getPosition());
        assertThat(retGroup1.getPositionAdministered()).isEqualTo(group1.getPositionAdministered());
    }
}
