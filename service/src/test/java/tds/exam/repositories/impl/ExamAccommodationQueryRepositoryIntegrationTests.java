package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.repositories.ExamAccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamAccommodationQueryRepositoryIntegrationTests {
    private ExamAccommodationQueryRepository examAccommodationQueryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    @Before
    public void setUp() {
        examAccommodationQueryRepository = new ExamAccommodationQueryRepositoryImpl(jdbcTemplate);

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
            .withSegmentId("segment-2")
            .withType("highlight")
            .withCode("TDS_Highlight1")
            .withDeniedAt(Instant.now())
            .build());

        mockExamAccommodations.forEach(this::insertMockAccommodation);
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        List<ExamAccommodation> result = examAccommodationQueryRepository.findAccommodations(
            ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).hasSize(1);
        ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isGreaterThan(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
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
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(result).hasSize(2);
        ExamAccommodation firstExamAccommodation = result.get(0);
        assertThat(firstExamAccommodation.getId()).isGreaterThan(0);
        assertThat(firstExamAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstExamAccommodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(firstExamAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(firstExamAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(firstExamAccommodation.getCreatedAt()).isNotNull();
        assertThat(firstExamAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(firstExamAccommodation.isApproved()).isTrue();

        ExamAccommodation secondAccmmodation = result.get(1);
        assertThat(secondAccmmodation.getId()).isGreaterThan(0);
        assertThat(secondAccmmodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondAccmmodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
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
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning",
                "foo",
                "bar" });

        assertThat(result).hasSize(2);
        ExamAccommodation examAccommodation = result.get(0);
        assertThat(examAccommodation.getId()).isGreaterThan(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(examAccommodation.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(examAccommodation.getCreatedAt()).isNotNull();
        assertThat(examAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(examAccommodation.getDeniedAt()).isNull();
        assertThat(examAccommodation.isApproved()).isTrue();

        ExamAccommodation secondExamAccommodation = result.get(1);
        assertThat(secondExamAccommodation.getId()).isGreaterThan(0);
        assertThat(secondExamAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondExamAccommodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
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
        assertThat(examAccommodation.getSegmentId()).isEqualTo("segment-2");
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
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
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

    private void insertMockAccommodation(ExamAccommodation examAccommodation) {
        SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examAccommodation.getExamId()))
            .addValue("segmentId", examAccommodation.getSegmentId())
            .addValue("type", examAccommodation.getType())
            .addValue("code", examAccommodation.getCode())
            .addValue("description", examAccommodation.getDescription())
            .addValue("deniedAt", examAccommodation.getDeniedAt() == null ? null : Date.from(examAccommodation.getDeniedAt()));

        final String SQL =
            "INSERT INTO exam_accommodations(exam_id, segment_id, type, code, description, denied_at)" +
            "VALUES(:examId, :segmentId, :type, :code, :description, :deniedAt)";

        jdbcTemplate.update(SQL, parameters);
    }
}
