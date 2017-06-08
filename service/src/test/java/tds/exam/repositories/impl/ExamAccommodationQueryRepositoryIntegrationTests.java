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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.models.ExamAccommodationFilter;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.repositories.ExamCommandRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.exam.builder.ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE;
import static tds.exam.builder.ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE;
import static tds.exam.builder.ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamAccommodationQueryRepositoryIntegrationTests {

    @Autowired
    private ExamCommandRepository examCommandRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Exam exam;
    private ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private ExamAccommodationCommandRepository examAccommodationCommandRepository;

    @Before
    public void setUp() {
        exam = new ExamBuilder()
            .withId(UUID.randomUUID())
            .build();
        examCommandRepository.insert(exam);

        examAccommodationQueryRepository = new ExamAccommodationQueryRepositoryImpl(jdbcTemplate);
        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);

        final List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(exam.getId())
            .build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(exam.getId())
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .withAllowChange(true)
            .withSelectable(true)
            .withSegmentPosition(5)
            .withTotalTypeCount(5)
            .withCustom(true)
            .withVisible(true)
            .withDefaultAccommodation(true)
            .withDisabledOnGuestSession(true)
            .withStudentControlled(true)
            .withFunctional(true)
            .withDependsOn("Language")
            .withAllowCombine(true)
            .build());

        // Accommodation in second segment that is denied
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withExamId(exam.getId())
            .withSegmentKey("segment-2")
            .withType("highlight")
            .withCode("TDS_Highlight1")
            .withDeniedAt(Instant.now())
            .build());

        examAccommodationCommandRepository.insert(mockExamAccommodations);
    }

    @Test
    public void shouldFindAccommodationsByExamIdAndAccommodationIdentifiers() {
        List<ExamAccommodationFilter> identifiers = Arrays.asList(
          new ExamAccommodationFilter("TDS_ClosedCap0", "closed captioning"),
          new ExamAccommodationFilter("TDS_Highlight1", "highlight")
        );

        List<ExamAccommodation> accommodations = examAccommodationQueryRepository.findAccommodations(exam.getId(), identifiers);

        ExamAccommodation closedCapAccommodation = null;
        ExamAccommodation highlightAccommodation = null;

        assertThat(accommodations).hasSize(2);

        for(ExamAccommodation accommodation : accommodations) {
            if(accommodation.getCode().equals("TDS_ClosedCap0")) {
                closedCapAccommodation = accommodation;
            } else if (accommodation.getCode().equals("TDS_Highlight1")) {
                highlightAccommodation = accommodation;
            }
        }

        assertThat(closedCapAccommodation).isNotNull();
        assertThat(highlightAccommodation).isNotNull();
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(result).hasSize(1);
        final ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isNotNull();
        assertThat(examAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(DEFAULT_SEGMENT_KEY);
        assertThat(examAccommodation.getType()).isEqualTo(DEFAULT_ACCOMMODATION_TYPE);
        assertThat(examAccommodation.getCode()).isEqualTo(DEFAULT_ACCOMMODATION_CODE);
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndNoTypes() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);

        assertThat(result).hasSize(2);
        ExamAccommodation firstExamAccommodation = null;
        ExamAccommodation secondAccommodation = null;

        for (final ExamAccommodation examAccommodation : result) {
            if (examAccommodation.getType().equals("closed captioning")) {
                firstExamAccommodation = examAccommodation;
            } else if (examAccommodation.getType().equals(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE)) {
                secondAccommodation = examAccommodation;
            }
        }

        assertThat(firstExamAccommodation).isNotNull();
        assertThat(secondAccommodation).isNotNull();

        assertThat(firstExamAccommodation.getId()).isNotNull();
        assertThat(firstExamAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(firstExamAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(firstExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(firstExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(firstExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(firstExamAccommodation.isApproved()).isTrue();
        assertThat(firstExamAccommodation.isCustom()).isTrue();
        assertThat(firstExamAccommodation.isAllowCombine()).isTrue();
        assertThat(firstExamAccommodation.isDefaultAccommodation()).isTrue();
        assertThat(firstExamAccommodation.isDisabledOnGuestSession()).isTrue();
        assertThat(firstExamAccommodation.getDependsOn()).isEqualTo("Language");
        assertThat(firstExamAccommodation.isVisible()).isTrue();
        assertThat(firstExamAccommodation.isStudentControlled()).isTrue();
        assertThat(firstExamAccommodation.isFunctional()).isTrue();

        assertThat(secondAccommodation.getId()).isNotNull();
        assertThat(secondAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(secondAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(secondAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(secondAccommodation.getCreatedAt()).isNotNull();
        assertThat(secondAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndIgnoreAccommodationTypesThatDoNotExist() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning",
            "foo",
            "bar");

        ExamAccommodation examAccommodation = null;
        ExamAccommodation secondExamAccommodation = null;

        assertThat(result).hasSize(2);
        for (final ExamAccommodation accommodation : result) {
            if (accommodation.getCode().equals(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE)) {
                examAccommodation = accommodation;
            } else {
                secondExamAccommodation = accommodation;
            }
        }

        assertThat(examAccommodation).isNotNull();
        assertThat(secondExamAccommodation).isNotNull();

        assertThat(examAccommodation.getId()).isNotNull();
        assertThat(examAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(examAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(examAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.getDeniedAt()).isNull();
        assertThat(examAccommodation.isApproved()).isTrue();

        assertThat(secondExamAccommodation.getId()).isNotNull();
        assertThat(secondExamAccommodation.getExamId()).isEqualTo(exam.getId());
        assertThat(secondExamAccommodation.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(secondExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(secondExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(secondExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondExamAccommodation.getDeniedAt()).isNull();
        assertThat(secondExamAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldReturnExamAccommodationsForTwoExams() {
        final Exam tempExamA = new ExamBuilder()
            .withId(UUID.randomUUID())
            .build();
        examCommandRepository.insert(tempExamA);
        final Exam tempExamB = new ExamBuilder()
            .withId(UUID.randomUUID())
            .build();
        examCommandRepository.insert(tempExamB);

        final UUID examId1 = tempExamA.getId();
        final UUID examId2 = tempExamB.getId();

        final ExamAccommodation accommodation1 = new ExamAccommodationBuilder().withExamId(examId1).build();
        final ExamAccommodation accommodation2 = new ExamAccommodationBuilder().withExamId(examId2).build();
        final ExamAccommodation accommodation3 = new ExamAccommodationBuilder().withExamId(examId2).build();

        examAccommodationCommandRepository.insert(Arrays.asList(accommodation1, accommodation2, accommodation3));

        final List<ExamAccommodation> approvedAccommodationsExam1 = examAccommodationQueryRepository.findApprovedAccommodations(examId1);
        assertThat(approvedAccommodationsExam1).containsExactly(accommodation1);

        final List<ExamAccommodation> approvedAccommodationsExam1And2 = examAccommodationQueryRepository.findApprovedAccommodations(examId1, examId2);
        assertThat(approvedAccommodationsExam1And2).containsExactlyInAnyOrder(accommodation1, accommodation2, accommodation3);
    }

    @Test
    public void shouldGetAccommodationForExamIdAndSegmentWithADeniedAccommodation() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            "segment-2",
            "highlight");

        assertThat(result).hasSize(1);

        final ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isNotNull();
        assertThat(examAccommodation.getExamId()).isEqualTo(exam.getId());
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
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            UUID.randomUUID(),
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForASegmentIdThatDoesNotExist() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            "foo",
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForAccommodationTypesThatDoesNotExist() {
        final List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            exam.getId(),
            "segment-2",
            "foo", "bar");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetExamAccommodations() {
        final Exam tempExam = new ExamBuilder()
            .withId(UUID.randomUUID())
            .build();
        examCommandRepository.insert(tempExam);

        final UUID examId = tempExam.getId();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        examAccommodationCommandRepository.insert(Collections.singletonList(examAccommodation));

        final List<ExamAccommodation> accommodations = examAccommodationQueryRepository.findAccommodations(examId);

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
        final List<ExamAccommodation> allExamAccommodations = examAccommodationQueryRepository.findAccommodations(exam.getId());
        assertThat(allExamAccommodations).hasSize(3);

        final List<ExamAccommodation> approvedExamAccommodations = examAccommodationQueryRepository.findApprovedAccommodations(exam.getId());
        assertThat(approvedExamAccommodations).hasSize(2);

        approvedExamAccommodations.forEach(accommodation -> {
                assertThat(accommodation.isApproved()).isTrue();
                //segment-2 is the key for the denied accommodation in the before block
                assertThat(accommodation.getSegmentKey()).doesNotMatch("segment-2");
            }
        );
    }
}
