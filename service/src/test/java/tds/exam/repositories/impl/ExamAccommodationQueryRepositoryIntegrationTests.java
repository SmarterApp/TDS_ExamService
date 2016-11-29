package tds.exam.repositories.impl;

import org.joda.time.Instant;
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
public class ExamAccommodationQueryRepositoryIntegrationTests {
    private ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examAccommodationQueryRepository = new ExamAccommodationQueryRepositoryImpl(jdbcTemplate);
        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);

        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());

        // Accommodation in second segment that is denied
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID)
            .withSegmentKey("segment-2")
            .withType("highlight")
            .withCode("TDS_Highlight1")
            .withDeniedAt(Instant.now())
            .build());

        examAccommodationCommandRepository.save(mockExamAccommodations);
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).hasSize(1);
        ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isGreaterThan(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(examAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndTwoDifferentAccommodationTypes() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(result).hasSize(2);
        ExamAccommodation firstExamAccommodation = result.get(0);
        assertThat(firstExamAccommodation.getId()).isGreaterThan(0);
        assertThat(firstExamAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstExamAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(firstExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(firstExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(firstExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(firstExamAccommodation.isApproved()).isTrue();

        ExamAccommodation secondAccmmodation = result.get(1);
        assertThat(secondAccmmodation.getId()).isGreaterThan(0);
        assertThat(secondAccmmodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondAccmmodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondAccmmodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(secondAccmmodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(secondAccmmodation.getCreatedAt()).isNotNull();
        assertThat(secondAccmmodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondAccmmodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndIgnoreAccommodationTypesThatDoNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning",
                "foo",
                "bar" });

        ExamAccommodation examAccommodation = null;
        ExamAccommodation secondExamAccommodation = null;

        assertThat(result).hasSize(2);
        for(ExamAccommodation accommodation : result) {
            if(accommodation.getCode().equals(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE)) {
                examAccommodation = accommodation;
            } else {
                secondExamAccommodation = accommodation;
            }
        }

        assertThat(examAccommodation.getId()).isGreaterThan(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(examAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.getDeniedAt()).isNull();
        assertThat(examAccommodation.isApproved()).isTrue();

        assertThat(secondExamAccommodation.getId()).isGreaterThan(0);
        assertThat(secondExamAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondExamAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(secondExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(secondExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(secondExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondExamAccommodation.getDeniedAt()).isNull();
        assertThat(secondExamAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetAccommodationForExamIdAndSegmentWithADeniedAccommodation() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "segment-2",
            new String[] { "highlight" });

        assertThat(result).hasSize(1);

        ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isGreaterThan(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("segment-2");
        assertThat(examAccommodation.getType()).isEqualTo("highlight");
        assertThat(examAccommodation.getCode()).isEqualTo("TDS_Highlight1");
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.getDeniedAt()).isNotNull();
        assertThat(examAccommodation.getDeniedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.isApproved()).isFalse();
    }

    @Test
    public void shouldGetAnEmptyListForAnExamIdThatDoesNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            UUID.randomUUID(),
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForASegmentIdThatDoesNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "foo",
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForAccommodationTypesThatDoesNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "segment-2",
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetExamAccommodations() {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        examAccommodationCommandRepository.save(Collections.singletonList(examAccommodation));

        List<ExamAccommodation> accommodations = examAccommodationQueryRepository.findAccommodations(examId);

        assertThat(accommodations).hasSize(1);
        assertThat(accommodations.get(0).getExamId()).isEqualTo(examId);


        examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .withId(examAccommodation.getId())
            .withDeletedAt(Instant.now())
            .build();

        examAccommodationCommandRepository.update(examAccommodation);

        assertThat(examAccommodationQueryRepository.findAccommodations(examId)).isEmpty();

        examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .withId(examAccommodation.getId())
            .withDeletedAt(null)
            .build();

        examAccommodationCommandRepository.update(examAccommodation);
        assertThat(examAccommodationQueryRepository.findAccommodations(examId)).hasSize(1);
    }
}
