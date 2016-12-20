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

        examAccommodationCommandRepository.insert(mockExamAccommodations);
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

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
    public void shouldGetTwoAccommodationsForExamAndSegmentAndNoTypes() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);

        assertThat(result).hasSize(2);
        ExamAccommodation firstExamAccommodation = null;
        ExamAccommodation secondAccommodation = null;

        for (ExamAccommodation examAccommodation : result) {
            if(examAccommodation.getType().equals("closed captioning")) {
                firstExamAccommodation = examAccommodation;
            } else if (examAccommodation.getType().equals(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE)) {
                secondAccommodation = examAccommodation;
            }
        }

        assertThat(firstExamAccommodation).isNotNull();
        assertThat(secondAccommodation).isNotNull();

        assertThat(firstExamAccommodation.getId()).isGreaterThan(0);
        assertThat(firstExamAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstExamAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(firstExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(firstExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(firstExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(firstExamAccommodation.isApproved()).isTrue();

        assertThat(secondAccommodation.getId()).isGreaterThan(0);
        assertThat(secondAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(secondAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(secondAccommodation.getCreatedAt()).isNotNull();
        assertThat(secondAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndIgnoreAccommodationTypesThatDoNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning",
            "foo",
            "bar");

        ExamAccommodation examAccommodation = null;
        ExamAccommodation secondExamAccommodation = null;

        assertThat(result).hasSize(2);
        for (ExamAccommodation accommodation : result) {
            if (accommodation.getCode().equals(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE)) {
                examAccommodation = accommodation;
            } else {
                secondExamAccommodation = accommodation;
            }
        }

        assertThat(examAccommodation).isNotNull();
        assertThat(secondExamAccommodation).isNotNull();

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
            "highlight");

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
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForASegmentIdThatDoesNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "foo",
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForAccommodationTypesThatDoesNotExist() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "segment-2",
            "foo", "bar");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetExamAccommodations() {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        examAccommodationCommandRepository.insert(Collections.singletonList(examAccommodation));

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

    @Test
    public void shouldRetrieveApprovedAccommodations() {
        List<ExamAccommodation> allExamAccommodations = examAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(allExamAccommodations).hasSize(3);

        List<ExamAccommodation> approvedExamAccommodations = examAccommodationQueryRepository.findApprovedAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(approvedExamAccommodations).hasSize(2);

        approvedExamAccommodations.forEach(accommodation -> {
                assertThat(accommodation.isApproved()).isTrue();
                //segment-2 is the key for the denied accommodation in the before block
                assertThat(accommodation.getSegmentKey()).doesNotMatch("segment-2");
            }
        );
    }
}
