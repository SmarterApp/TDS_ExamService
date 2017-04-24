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

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
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
public class ExamItemRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamPageQueryRepository examPageQueryRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamItemQueryRepository examItemQueryRepository;
    private ExamSegmentCommandRepository examSegmentCommandRepository;

    @Before
    public void setUp() {
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examPageQueryRepository = new ExamPageQueryRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        examItemQueryRepository = new ExamItemQueryRepositoryImpl(commandJdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    public void shouldExcludeItemsInDeletedPages() {
        Exam exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);
        ExamSegment mockExamSegment = new ExamSegmentBuilder()
            .withExamId(exam.getId())
            .build();
        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment));

        // This page will be deleted
        ExamPage page1 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withPagePosition(1)
            .withItemGroupKey("key1")
            .build();

        examPageCommandRepository.insert(page1);

        ExamItem examItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(page1.getId())
            .withPosition(1)
            .build();

        ExamItemResponse examItemResponse = new ExamItemResponseBuilder()
            .withExamItemId(examItem.getId())
            .withSequence(2)
            .build();

        examItemCommandRepository.insert(examItem);
        examItemCommandRepository.insertResponses(examItemResponse);
        // Now the above is marked as deleted.
        examPageCommandRepository.deleteAll(exam.getId());

        ExamPage page2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam.getId())
            .withSegmentId(mockExamSegment.getSegmentId())
            .withSegmentKey(mockExamSegment.getSegmentKey())
            .withPagePosition(1)
            .withItemGroupKey("key1")
            .build();

        examPageCommandRepository.insert(page2);

        ExamItem examItem2 = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(page1.getId())
            .withPosition(1)
            .build();

        ExamItemResponse examItemResponse2 = new ExamItemResponseBuilder()
            .withExamItemId(examItem.getId())
            .withSequence(2)
            .build();

        examItemCommandRepository.insert(examItem2);
        examItemCommandRepository.insertResponses(examItemResponse2);
        Map<UUID, Integer> examIdsToResponseCounts = examItemQueryRepository.getResponseCounts(exam.getId());
        assertThat(examIdsToResponseCounts).hasSize(1);
        assertThat(examIdsToResponseCounts.get(exam.getId())).isEqualTo(1);
    }

    @Test
    public void shouldFindTwoExamsThatHaveItemsRespondedTo() {
        Exam exam1 = new ExamBuilder().build();
        Exam exam2 = new ExamBuilder().build();
        examCommandRepository.insert(exam1);
        examCommandRepository.insert(exam2);

        ExamSegment mockExamSegment1 = new ExamSegmentBuilder()
            .withExamId(exam1.getId())
            .build();
        ExamSegment mockExamSegment2 = new ExamSegmentBuilder()
            .withExamId(exam2.getId())
            .build();

        examSegmentCommandRepository.insert(Arrays.asList(mockExamSegment1, mockExamSegment2));

        // Page for 1st exam
        ExamPage examPage1 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam1.getId())
            .withSegmentId(mockExamSegment1.getSegmentId())
            .withSegmentKey(mockExamSegment1.getSegmentKey())
            .withPagePosition(1)
            .withItemGroupKey("key1")
            .build();

        // Page for 2nd exam
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .withExamId(exam2.getId())
            .withSegmentId(mockExamSegment2.getSegmentId())
            .withSegmentKey(mockExamSegment2.getSegmentKey())
            .withPagePosition(1)
            .withItemGroupKey("key1")
            .build();

        assertThat(examPageQueryRepository.findAll(exam1.getId())).isEmpty();
        assertThat(examPageQueryRepository.findAll(exam2.getId())).isEmpty();
        examPageCommandRepository.insert(examPage1, examPage2);

        // will have single response
        ExamItem exam1Item1 = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(examPage1.getId())
            .withPosition(1)
            .build();

        // will have two responses
        ExamItem exam1Item2 = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(examPage1.getId())
            .withPosition(2)
            .build();

        // will have two responses
        ExamItem exam2Item1 = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(examPage2.getId())
            .withPosition(1)
            .build();

        // will have no responses
        ExamItem exam2Item2 = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(examPage2.getId())
            .withPosition(2)
            .build();

        examItemCommandRepository.insert(exam1Item1, exam1Item2, exam2Item1, exam2Item2);

        // Exam 1
        ExamItemResponse exam1Item1Response = new ExamItemResponseBuilder()
            .withExamItemId(exam1Item1.getId())
            .withSequence(2)
            .build();
        ExamItemResponse exam1Item2Response1 = new ExamItemResponseBuilder()
            .withExamItemId(exam1Item2.getId())
            .withSequence(1)
            .build();
        ExamItemResponse exam1Item2Response2 = new ExamItemResponseBuilder()
            .withExamItemId(exam1Item2.getId())
            .withSequence(3)
            .build();
        // Exam 2
        ExamItemResponse exam2Item1Response1 = new ExamItemResponseBuilder()
            .withExamItemId(exam2Item1.getId())
            .withSequence(1)
            .build();
        ExamItemResponse exam2Item1Response2 = new ExamItemResponseBuilder()
            .withExamItemId(exam2Item1.getId())
            .withSequence(2)
            .build();

        examItemCommandRepository.insertResponses(exam1Item1Response, exam1Item2Response1, exam1Item2Response2,
            exam2Item1Response1, exam2Item1Response2);

        Map<UUID, Integer> examIdsToResponseCounts = examItemQueryRepository.getResponseCounts(exam1.getId(), exam2.getId());
        assertThat(examIdsToResponseCounts).hasSize(2);
        assertThat(examIdsToResponseCounts.get(exam1.getId())).isEqualTo(2);
        assertThat(examIdsToResponseCounts.get(exam2.getId())).isEqualTo(1);
    }
}
