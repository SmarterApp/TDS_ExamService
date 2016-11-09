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
import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.repositories.AccommodationCommandRepository;
import tds.exam.repositories.AccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class AccommodationCommandRepositoryIntegrationTests {
    private AccommodationCommandRepository accommodationCommandRepository;
    private AccommodationQueryRepository accommodationQueryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        accommodationCommandRepository = new AccommodationCommandRepositoryImpl(jdbcTemplate);
        accommodationQueryRepository = new AccommodationQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertExamAccommodations() {
        UUID examId = UUID.randomUUID();
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(examId)
            .withSegmentId("segment")
            .build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(examId)
            .withSegmentId("segment")
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());

        accommodationCommandRepository.insertAccommodations(mockExamAccommodations);

        List<ExamAccommodation> accommodations = accommodationQueryRepository.findAccommodations(examId, "segment", new String[]{"language", "closed captioning"});

        assertThat(accommodations).hasSize(2);
    }
}
