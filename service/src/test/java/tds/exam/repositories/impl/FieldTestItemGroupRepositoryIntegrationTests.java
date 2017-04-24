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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;
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
    private ExamSegmentCommandRepository examSegmentCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamItemCommandRepository examItemCommandRepository;

    @Before
    public void setUp() {
        fieldTestItemGroupCommandRepository = new FieldTestItemGroupCommandRepositoryImpl(commandJdbcTemplate);
        fieldTestItemGroupQueryRepository = new FieldTestItemGroupQueryRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
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

    @Test
    public void shouldFindFieldTestItemsThatWereAdministeredInAnExam() {
        UUID mockSessionId = UUID.randomUUID();
        Exam mockExam = new ExamBuilder().build();
        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withExamId(mockExam.getId())
            .build();
        ExamPage mockFirstPage = new ExamPageBuilder()
            .withExamId(mockExam.getId())
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withPagePosition(1)
            .withSegmentPosition(1)
            .build();
        ExamPage mockSecondPage = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(mockExam.getId())
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withItemGroupKey("item-group-key-2")
            .withPagePosition(2)
            .withSegmentPosition(2)
            .build();
        ExamItem mockFirstPageFirstItem = new ExamItemBuilder()
            .withExamPageId(mockFirstPage.getId())
            .withPosition(1)
            .withFieldTest(true)
            .build();
        ExamItem mockFirstPageSecondItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(mockFirstPage.getId())
            .withFieldTest(true)
            .withPosition(2)
            .build();
        ExamItem mockSecondPageFirstItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(mockSecondPage.getId())
            .withFieldTest(true)
            .withPosition(3)
            .build();
        FieldTestItemGroup mockFirstFtItemGroup = new FieldTestItemGroup.Builder()
            .withExamId(mockExam.getId())
            .withGroupId("item-group-id")
            .withGroupKey(mockFirstPage.getItemGroupKey())
            .withBlockId("A")
            .withPositionAdministered(1)
            .withAdministeredAt(Instant.now().minus(60L, ChronoUnit.SECONDS))
            .withLanguageCode("ENU")
            .withPosition(2)
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withItemCount(1)
            .withSessionId(mockSessionId)
            .build();
        FieldTestItemGroup mockSecondFtItemGroup = new FieldTestItemGroup.Builder()
            .withExamId(mockExam.getId())
            .withGroupId("item-group-key-2")
            .withGroupKey(mockSecondPage.getItemGroupKey())
            .withBlockId("B")
            .withLanguageCode("ENU")
            .withPosition(3)
            .withPositionAdministered(12)
            .withAdministeredAt(Instant.now().minus(60L, ChronoUnit.SECONDS))
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withItemCount(2)
            .withSessionId(mockSessionId)
            .build();

        examCommandRepository.insert(mockExam);
        examSegmentCommandRepository.insert(Collections.singletonList(mockExamSegment));
        examPageCommandRepository.insert(mockFirstPage, mockSecondPage);
        examItemCommandRepository.insert(mockFirstPageFirstItem, mockFirstPageSecondItem, mockSecondPageFirstItem);
        fieldTestItemGroupCommandRepository.insert(Arrays.asList(mockFirstFtItemGroup, mockSecondFtItemGroup));

        List<FieldTestItemGroup> fieldTestItemGroups = fieldTestItemGroupQueryRepository.findUsageInExam(mockExam.getId());

        assertThat(fieldTestItemGroups).hasSize(2);
        FieldTestItemGroup firstFtItemGroupResult = fieldTestItemGroups.get(0);
        assertThat(firstFtItemGroupResult.getPositionAdministered()).isEqualTo(mockFirstPageFirstItem.getPosition());

        FieldTestItemGroup secondFtItemGroupResult = fieldTestItemGroups.get(1);
        assertThat(secondFtItemGroupResult.getPositionAdministered()).isEqualTo(mockSecondPageFirstItem.getPosition());
    }

    @Test
    public void shouldUpdateFieldTestItemGroupRecords() {
        Exam mockExam = new ExamBuilder().build();
        FieldTestItemGroup mockFirstFtItemGroup = new FieldTestItemGroup.Builder()
            .withExamId(mockExam.getId())
            .withGroupId("item-group-id")
            .withGroupKey("item-group-key")
            .withBlockId("A")
            .withPositionAdministered(1)
            .withAdministeredAt(Instant.now().minus(60L, ChronoUnit.SECONDS))
            .withLanguageCode("ENU")
            .withPosition(2)
            .withSegmentId("segment-id")
            .withSegmentKey("segment-key")
            .withItemCount(1)
            .withSessionId(UUID.randomUUID())
            .build();

        examCommandRepository.insert(mockExam);
        fieldTestItemGroupCommandRepository.insert(Collections.singletonList(mockFirstFtItemGroup));

        Instant newAdministeredAt = Instant.now().minus(60L, ChronoUnit.SECONDS);
        FieldTestItemGroup updatedFirstFtItemGroup = new FieldTestItemGroup.Builder()
            .fromFieldTestItemGroup(mockFirstFtItemGroup)
            .withPositionAdministered(100)
            .withAdministeredAt(newAdministeredAt)
            .build();

        fieldTestItemGroupCommandRepository.update(updatedFirstFtItemGroup);

        List<FieldTestItemGroup> fieldTestItemGroups = fieldTestItemGroupQueryRepository.find(mockExam.getId(),
            mockFirstFtItemGroup.getSegmentKey());

        assertThat(fieldTestItemGroups).hasSize(1);
        FieldTestItemGroup result = fieldTestItemGroups.get(0);
        assertThat(result.getPositionAdministered()).isEqualTo(updatedFirstFtItemGroup.getPositionAdministered());
        assertThat(result.getAdministeredAt()).isEqualTo(updatedFirstFtItemGroup.getAdministeredAt());
    }
}
