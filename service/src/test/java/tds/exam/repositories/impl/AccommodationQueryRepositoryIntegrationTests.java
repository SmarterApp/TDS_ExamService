package tds.exam.repositories.impl;

import javax.sql.DataSource;

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
    @Autowired
    @Qualifier("commandDataSource")
    private DataSource commandDataSource;

    private AccommodationQueryRepository accommodationQueryRepository;
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        accommodationQueryRepository = new AccommodationQueryRepositoryImpl(commandDataSource);
        jdbcTemplate = new NamedParameterJdbcTemplate(commandDataSource);

        List<Accommodation> mockAccommodations = new ArrayList<>();
        // Two accommodations for the first Exam ID
        mockAccommodations.add(new AccommodationBuilder().build());
        mockAccommodations.add(new AccommodationBuilder()
            .withType(AccommodationBuilder.SECOND_ACCOMMODATION_TYPE)
            .withCode(AccommodationBuilder.SECOND_ACCOMMODATION_CODE)
            .build());

        // Accommodation for the second Exam ID, which has multiple segments
        mockAccommodations.add(new AccommodationBuilder()
            .withExamId(AccommodationBuilder.SECOND_EXAM_ID)
            .build());

        // Accommodation for second Exam ID in second segment that is denied
        mockAccommodations.add(new AccommodationBuilder()
            .withExamId(AccommodationBuilder.SECOND_EXAM_ID)
            .withSegmentId(AccommodationBuilder.SECOND_SEGMENT_ID)
            .withType(AccommodationBuilder.SECOND_ACCOMMODATION_TYPE)
            .withCode(AccommodationBuilder.SECOND_ACCOMMODATION_CODE)
            .withDeniedAt(Instant.now())
            .build());

        mockAccommodations.forEach(this::insertMockAccommodationData);
    }

    @Test
    public void shouldGetOneAccommodationForExamAndSegmentAndSingleAccommodationType() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.FIRST_EXAM_ID,
            AccommodationBuilder.FIRST_SEGMENT_ID,
            new String[] { AccommodationBuilder.FIRST_ACCOMMODATION_TYPE });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isGreaterThan(0);
        assertThat(result.get(0).getExamId()).isEqualTo(AccommodationBuilder.FIRST_EXAM_ID);
        assertThat(result.get(0).getSegmentId()).isEqualTo(AccommodationBuilder.FIRST_SEGMENT_ID);
        assertThat(result.get(0).getType()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_TYPE);
        assertThat(result.get(0).getCode()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_CODE);
        assertThat(result.get(0).getCreatedAt()).isNotNull();
        assertThat(result.get(0).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(0).isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndTwoDifferentAccommodationTypes() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.FIRST_EXAM_ID,
            AccommodationBuilder.FIRST_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.FIRST_ACCOMMODATION_TYPE,
                AccommodationBuilder.SECOND_ACCOMMODATION_TYPE });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isGreaterThan(0);
        assertThat(result.get(0).getExamId()).isEqualTo(AccommodationBuilder.FIRST_EXAM_ID);
        assertThat(result.get(0).getSegmentId()).isEqualTo(AccommodationBuilder.FIRST_SEGMENT_ID);
        assertThat(result.get(0).getType()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_TYPE);
        assertThat(result.get(0).getCode()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_CODE);
        assertThat(result.get(0).getCreatedAt()).isNotNull();
        assertThat(result.get(0).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(0).isApproved()).isTrue();

        assertThat(result.get(1).getId()).isGreaterThan(0);
        assertThat(result.get(1).getExamId()).isEqualTo(AccommodationBuilder.FIRST_EXAM_ID);
        assertThat(result.get(1).getSegmentId()).isEqualTo(AccommodationBuilder.FIRST_SEGMENT_ID);
        assertThat(result.get(1).getType()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_TYPE);
        assertThat(result.get(1).getCode()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_CODE);
        assertThat(result.get(1).getCreatedAt()).isNotNull();
        assertThat(result.get(1).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(1).isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForExamAndSegmentAndIgnoreAccommodationTypesThatDoNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.FIRST_EXAM_ID,
            AccommodationBuilder.FIRST_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.FIRST_ACCOMMODATION_TYPE,
                AccommodationBuilder.SECOND_ACCOMMODATION_TYPE,
                "foo",
                "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isGreaterThan(0);
        assertThat(result.get(0).getExamId()).isEqualTo(AccommodationBuilder.FIRST_EXAM_ID);
        assertThat(result.get(0).getSegmentId()).isEqualTo(AccommodationBuilder.FIRST_SEGMENT_ID);
        assertThat(result.get(0).getType()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_TYPE);
        assertThat(result.get(0).getCode()).isEqualTo(AccommodationBuilder.FIRST_ACCOMMODATION_CODE);
        assertThat(result.get(0).getCreatedAt()).isNotNull();
        assertThat(result.get(0).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(0).getDeniedAt()).isNull();
        assertThat(result.get(0).isApproved()).isTrue();

        assertThat(result.get(1).getId()).isGreaterThan(0);
        assertThat(result.get(1).getExamId()).isEqualTo(AccommodationBuilder.FIRST_EXAM_ID);
        assertThat(result.get(1).getSegmentId()).isEqualTo(AccommodationBuilder.FIRST_SEGMENT_ID);
        assertThat(result.get(1).getType()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_TYPE);
        assertThat(result.get(1).getCode()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_CODE);
        assertThat(result.get(1).getCreatedAt()).isNotNull();
        assertThat(result.get(1).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(1).getDeniedAt()).isNull();
        assertThat(result.get(1).isApproved()).isTrue();
    }

    @Test
    public void shouldGetAccommodationForExamIdAndSegmentWithADeniedAccommodation() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.SECOND_EXAM_ID,
            AccommodationBuilder.SECOND_SEGMENT_ID,
            new String[] { AccommodationBuilder.SECOND_ACCOMMODATION_TYPE });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        assertThat(result.get(0).getId()).isGreaterThan(0);
        assertThat(result.get(0).getExamId()).isEqualTo(AccommodationBuilder.SECOND_EXAM_ID);
        assertThat(result.get(0).getSegmentId()).isEqualTo(AccommodationBuilder.SECOND_SEGMENT_ID);
        assertThat(result.get(0).getType()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_TYPE);
        assertThat(result.get(0).getCode()).isEqualTo(AccommodationBuilder.SECOND_ACCOMMODATION_CODE);
        assertThat(result.get(0).getCreatedAt()).isNotNull();
        assertThat(result.get(0).getCreatedAt()).isLessThan(Instant.now());
        assertThat(result.get(0).getDeniedAt()).isNotNull();
        assertThat(result.get(0).getDeniedAt()).isLessThan(Instant.now());
        assertThat(result.get(0).isApproved()).isFalse();
    }

    @Test
    public void shouldGetAnEmptyListForAnExamIdThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            UUID.randomUUID(),
            AccommodationBuilder.FIRST_SEGMENT_ID,
            new String[] { AccommodationBuilder.FIRST_ACCOMMODATION_TYPE });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForASegmentIdThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.FIRST_EXAM_ID,
            "foo",
            new String[] { AccommodationBuilder.FIRST_ACCOMMODATION_TYPE });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldGetAnEmptyListForAccommodationTypesThatDoesNotExist() {
        List<Accommodation> result = accommodationQueryRepository.findAccommodations(
            AccommodationBuilder.FIRST_EXAM_ID,
            AccommodationBuilder.SECOND_SEGMENT_ID,
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }

    private void insertMockAccommodationData(Accommodation accommodation) {
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
