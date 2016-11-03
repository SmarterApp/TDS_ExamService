package tds.exam.repositories.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

import tds.common.data.mysql.UuidAdapter;
import tds.exam.Accommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.repositories.AccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class AccommodationQueryRepositoryIntegrationTests {
    private AccommodationQueryRepository accommodationQueryRepository;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    @Before
    public void setUp() {
        accommodationQueryRepository = new AccommodationQueryRepositoryImpl(jdbcTemplate);

        List<Accommodation> mockAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockAccommodations.add(new AccommodationBuilder().build());
        mockAccommodations.add(new AccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());

        // Accommodation in second segment that is denied
        mockAccommodations.add(new AccommodationBuilder()
            .withExamId(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID)
            .withSegmentId("segment-2")
            .withType("highlight")
            .withCode("TDS_Highlight1")
            .withDeniedAt(Instant.now())
            .build());

        mockAccommodations.forEach(this::insertMockAccommodation);
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).hasSize(1);
        Accommodation accommodation = result.get(0);
        assertThat(accommodation.getId()).isGreaterThan(0);
        assertThat(accommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(accommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(accommodation.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(accommodation.getCode()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(accommodation.getCreatedAt()).isNotNull();
        assertThat(accommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(accommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndTwoDifferentAccommodationTypes() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(result).hasSize(2);
        Accommodation firstAccommodation = result.get(0);
        assertThat(firstAccommodation.getId()).isGreaterThan(0);
        assertThat(firstAccommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstAccommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(firstAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(firstAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(firstAccommodation.getCreatedAt()).isNotNull();
        assertThat(firstAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(firstAccommodation.isApproved()).isTrue();

        Accommodation secondAccmmodation = result.get(1);
        assertThat(secondAccmmodation.getId()).isGreaterThan(0);
        assertThat(secondAccmmodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondAccmmodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(secondAccmmodation.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(secondAccmmodation.getCode()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(secondAccmmodation.getCreatedAt()).isNotNull();
        assertThat(secondAccmmodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondAccmmodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndIgnoreAccommodationTypesThatDoNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning",
                "foo",
                "bar" });

        assertThat(result).hasSize(2);
        Accommodation accommodation = result.get(0);
        assertThat(accommodation.getId()).isGreaterThan(0);
        assertThat(accommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(accommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(accommodation.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(accommodation.getCode()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(accommodation.getCreatedAt()).isNotNull();
        assertThat(accommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(accommodation.getDeniedAt()).isNull();
        assertThat(accommodation.isApproved()).isTrue();

        Accommodation secondAccommodation = result.get(1);
        assertThat(secondAccommodation.getId()).isGreaterThan(0);
        assertThat(secondAccommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondAccommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(secondAccommodation.getType()).isEqualTo("closed captioning");
        assertThat(secondAccommodation.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(secondAccommodation.getCreatedAt()).isNotNull();
        assertThat(secondAccommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(secondAccommodation.getDeniedAt()).isNull();
        assertThat(secondAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldGetAccommodationForExamIdAndSegmentWithADeniedAccommodation() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "segment-2",
            new String[] { "highlight" });

        assertThat(result).hasSize(1);

        Accommodation accommodation = result.get(0);
        assertThat(accommodation.getId()).isGreaterThan(0);
        assertThat(accommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(accommodation.getSegmentId()).isEqualTo("segment-2");
        assertThat(accommodation.getType()).isEqualTo("highlight");
        assertThat(accommodation.getCode()).isEqualTo("TDS_Highlight1");
        assertThat(accommodation.getCreatedAt()).isNotNull();
        assertThat(accommodation.getCreatedAt()).isLessThan(Instant.now());
        assertThat(accommodation.getDeniedAt()).isNotNull();
        assertThat(accommodation.getDeniedAt()).isLessThan(Instant.now());
        assertThat(accommodation.isApproved()).isFalse();
    }

    @Test
    public void shouldGetAnEmptyListForAnExamIdThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            UUID.randomUUID(),
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForASegmentIdThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "foo",
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForAccommodationTypesThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            "segment-2",
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    private void insertMockAccommodation(Accommodation accommodation) {
        SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(accommodation.getExamId()))
            .addValue("segmentId", accommodation.getSegmentId())
            .addValue("type", accommodation.getType())
            .addValue("code", accommodation.getCode())
            .addValue("description", accommodation.getDescription())
            .addValue("deniedAt", accommodation.getDeniedAt() == null ? null : Date.from(accommodation.getDeniedAt()));

        final String SQL =
            "INSERT INTO exam_accommodations(exam_id, segment_id, type, code, description, denied_at)" +
            "VALUES(:examId, :segmentId, :type, :code, :description, :deniedAt)";

        jdbcTemplate.update(SQL, parameters);
    }
}
