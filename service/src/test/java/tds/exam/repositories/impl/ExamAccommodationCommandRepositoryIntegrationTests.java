package tds.exam.repositories.impl;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
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
import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.repositories.ExamCommandRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamAccommodationCommandRepositoryIntegrationTests {

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ExamCommandRepository examCommandRepository;

    private Exam exam;
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private ExamAccommodationQueryRepository accommodationQueryRepository;

    @Before
    public void setUp() {
        exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);
        accommodationQueryRepository = new ExamAccommodationQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertExamAccommodations() {
        final UUID examId = exam.getId();
        final List<ExamAccommodation> savedExamAccommodations = insertExamAccommodations(examId);

        final List<ExamAccommodation> accommodations = accommodationQueryRepository.findAccommodations(examId, "segment", "language", "closed captioning");

        assertThat(accommodations).containsOnly(savedExamAccommodations.toArray(new ExamAccommodation[savedExamAccommodations.size()]));
        accommodations.forEach(a -> assertThat(a.getCreatedAt()).isNotNull());
    }

    @Test
    public void shouldDeleteExamAccommodations() {
        final UUID examId = exam.getId();
        insertExamAccommodations(examId);

        List<ExamAccommodation> accommodations = accommodationQueryRepository.findAccommodations(examId);

        assertThat(accommodations).hasSize(2);

        final ExamAccommodation accommodation = accommodations.get(0);
        final ExamAccommodation deletedAccommodation = accommodations.get(1);

        examAccommodationCommandRepository.delete(Collections.singletonList(deletedAccommodation));

        accommodations = accommodationQueryRepository.findAccommodations(examId);

        assertThat(accommodations).containsExactly(accommodation);
    }

    private List<ExamAccommodation> insertExamAccommodations(final UUID examId) {
        final EnhancedRandom rand = EnhancedRandomBuilder.aNewEnhancedRandomBuilder().stringLengthRange(3, 10).build();
        final List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockExamAccommodations.add(ExamAccommodation.Builder
            .fromExamAccommodation(rand.nextObject(ExamAccommodation.class))
            .withExamId(examId)
            .withSegmentKey("segment")
            .withType("language")
            .withValue("ENU")
            .withDeniedAt(null)
            .withDeletedAt(null)
            .build());
        mockExamAccommodations.add(ExamAccommodation.Builder
            .fromExamAccommodation(rand.nextObject(ExamAccommodation.class))
            .withExamId(examId)
            .withSegmentKey("segment")
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .withTotalTypeCount(5)
            .withDeniedAt(null)
            .withDeletedAt(null)
            .build());

        examAccommodationCommandRepository.insert(mockExamAccommodations);
        return mockExamAccommodations;
    }
}
