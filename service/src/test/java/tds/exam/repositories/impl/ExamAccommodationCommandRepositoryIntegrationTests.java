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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamAccommodationCommandRepositoryIntegrationTests {
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private ExamAccommodationQueryRepository accommodationQueryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);
        accommodationQueryRepository = new ExamAccommodationQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertExamAccommodations() {
        UUID examId = UUID.randomUUID();
        List<ExamAccommodation> savedExamAccommodations = insertExamAccommodations(examId);

        List<ExamAccommodation> accommodations = accommodationQueryRepository.findAccommodations(examId, "segment", new String[]{"language", "closed captioning"});

        assertThat(accommodations).containsExactly(savedExamAccommodations.toArray(new ExamAccommodation[savedExamAccommodations.size()]));
    }

    @Test
    public void shouldDeleteExamAccommodations() {
        UUID examId = UUID.randomUUID();
        insertExamAccommodations(examId);

        List<ExamAccommodation> accommodations = accommodationQueryRepository.findAccommodations(examId);

        assertThat(accommodations).hasSize(2);

        ExamAccommodation accommodation = accommodations.get(0);
        ExamAccommodation deletedAccommodation = accommodations.get(1);

        examAccommodationCommandRepository.delete(Collections.singletonList(deletedAccommodation));

        accommodations = accommodationQueryRepository.findAccommodations(examId);

        assertThat(accommodations).containsExactly(accommodation);
    }

    private List<ExamAccommodation> insertExamAccommodations(UUID examId) {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(examId)
            .withSegmentKey("segment")
            .build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(examId)
            .withSegmentKey("segment")
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .withTotalTypeCount(5)
            .build());

        examAccommodationCommandRepository.insert(mockExamAccommodations);
        return mockExamAccommodations;
    }
}
