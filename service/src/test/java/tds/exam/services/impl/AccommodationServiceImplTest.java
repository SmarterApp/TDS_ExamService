package tds.exam.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import tds.exam.Accommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.repositories.AccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccommodationServiceImplTest {
    private AccommodationQueryRepository accommodationQueryRepository;
    private AccommodationServiceImpl accommodationService;

    @Before
    public void setUp() {
        accommodationQueryRepository = mock(AccommodationQueryRepository.class);
        accommodationService = new AccommodationServiceImpl(accommodationQueryRepository);
    }

    @Test
    public void shouldReturnAnAccommodation() {
        List<Accommodation> mockAccommodations = new ArrayList<>();
        mockAccommodations.add(new AccommodationBuilder().build());
        when(accommodationQueryRepository.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE})).thenReturn(mockAccommodations);

        List<Accommodation> results = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(results).hasSize(1);
        Accommodation accommodation = results.get(0);
        assertThat(accommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(accommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(accommodation.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
    }

    @Test
    public void shouldReturnTwoAccommodations() {
        List<Accommodation> mockAccommodations = new ArrayList<>();
        mockAccommodations.add(new AccommodationBuilder().build());
        mockAccommodations.add(new AccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());
        when(accommodationQueryRepository.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" }))
            .thenReturn(mockAccommodations);

        List<Accommodation> results = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(results).hasSize(2);

        Accommodation firstResult = results.get(0);
        assertThat(firstResult.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(firstResult.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(firstResult.getCode()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);

        Accommodation secondResult = results.get(1);
        assertThat(secondResult.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondResult.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(secondResult.getType()).isEqualTo("closed captioning");
        assertThat(secondResult.getCode()).isEqualTo("TDS_ClosedCap0");
    }

    @Test
    public void shouldReturnAnEmptyListWhenSearchingForAccommodationsThatDoNotExist() {
        when(accommodationQueryRepository.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { "foo", "bar" })).thenReturn(Lists.emptyList());

        List<Accommodation> result = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }
}
