package tds.exam.repositories.impl;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamSegmentCommandRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Ignore
public class ExamDataGenerator {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate commandJdbcTemplate;
    private ExamItemCommandRepository examItemCommandRepository;
    private ExamPageCommandRepository examPageCommandRepository;
    private ExamSegmentCommandRepository examSegmentCommandRepository;
    private ExamCommandRepository examCommandRepository;
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;

    @Before
    public void setUp() {
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
        examPageCommandRepository = new ExamPageCommandRepositoryImpl(commandJdbcTemplate);
        examSegmentCommandRepository = new ExamSegmentCommandRepositoryImpl(commandJdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(commandJdbcTemplate);
        examItemCommandRepository = new ExamItemCommandRepositoryImpl(commandJdbcTemplate);
        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(commandJdbcTemplate);
    }

    @Test
    @Ignore
    public void generateSeedData() {
        final int minThingsToGenerate = 1;
        final int maxNumberOfPages = 3;
        final int maxNumberOfItems = 5;
        EnhancedRandom rand = EnhancedRandomBuilder.aNewEnhancedRandomBuilder().stringLengthRange(3, 10).build();

        for (int e = 0; e < 500; e++) {
            final Exam exam = new ExamBuilder()
                .build();

            // Each exam has two segments
            final ExamSegment firstSegment = new ExamSegmentBuilder()
                .withExamId(exam.getId())
                .withSegmentPosition(1)
                .build();
            final ExamSegment secondSegment = new ExamSegmentBuilder()
                .withExamId(exam.getId())
                .withSegmentPosition(2)
                .build();

            Map<Integer, ExamSegment> examSegmentMaps = new HashMap<>(2);
            examSegmentMaps.put(0, firstSegment);
            examSegmentMaps.put(1, secondSegment);

            List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
            mockExamAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
                .fromExamAccommodation(rand.nextObject(ExamAccommodation.class))
                .withExamId(exam.getId())
                .withSegmentKey(firstSegment.getSegmentKey())
                .withType("language")
                .withValue("ENU")
                .withDeniedAt(null)
                .withDeletedAt(null)
                .build());
            mockExamAccommodations.add(new ExamAccommodation.Builder(UUID.randomUUID())
                .fromExamAccommodation(rand.nextObject(ExamAccommodation.class))
                .withExamId(exam.getId())
                .withSegmentKey(firstSegment.getSegmentKey())
                .withType("closed captioning")
                .withCode("TDS_ClosedCap0")
                .withTotalTypeCount(5)
                .withDeniedAt(null)
                .withDeletedAt(null)
                .build());

            final int pagesToCreate = ThreadLocalRandom.current().nextInt(minThingsToGenerate, maxNumberOfPages + 1);
            final int itemsToCreate = ThreadLocalRandom.current().nextInt(minThingsToGenerate, maxNumberOfItems + 1);
            final ExamPage[] pages = new ExamPage[pagesToCreate];
            final ExamItem[] items = new ExamItem[itemsToCreate];
            List<ExamItemResponse> examItemResponses = new ArrayList<>();

            for (int p = 0; p < pagesToCreate; p++) {
                pages[p] = new ExamPageBuilder()
                    .withId(UUID.randomUUID())
                    .withExamId(exam.getId())
                    .withSegmentKey(examSegmentMaps.get(p % 2).getSegmentKey())
                    .build();

                for (int i = 0; i < itemsToCreate; i++) {
                    examItemResponses.clear();

                    items[i] = new ExamItemBuilder()
                        .withId(UUID.randomUUID())
                        .withExamPageId(pages[p].getId())
                        .withItemKey(String.format("187-123%s", i))
                        .withAssessmentItemBankKey(187)
                        .withAssessmentItemKey(Integer.parseInt(String.format("123%s", i)))
                        .withItemType("MI")
                        .withPosition(i + 1)
                        .withFieldTest(false)
                        .withRequired(true)
                        .build();

                    final ExamItemResponse response = new ExamItemResponse.Builder()
                        .withExamItemId(items[i].getId())
                        .withResponse(String.format("response for item %s", items[i].getId()))
                        .withSequence(i + 1)
                        .withValid(true)
                        .withSelected(true)
                        .withMarkedForReview(false)
                        .withCreatedAt(Instant.now())
                        .build();

                    examItemResponses.add(response);
                }
            }

            examCommandRepository.insert(exam);
            examSegmentCommandRepository.insert(Arrays.asList(firstSegment, secondSegment));
            examAccommodationCommandRepository.insert(mockExamAccommodations);
            examPageCommandRepository.insert(pages);
            examItemCommandRepository.insert(items);
            examItemCommandRepository.insertResponses(examItemResponses.toArray(new ExamItemResponse[examItemResponses.size()]));
        }
    }
}