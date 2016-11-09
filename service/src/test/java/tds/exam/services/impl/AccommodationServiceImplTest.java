package tds.exam.services.impl;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import tds.exam.ExamAccommodation;
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
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new AccommodationBuilder().build());
        when(accommodationQueryRepository.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE})).thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(results).hasSize(1);
        ExamAccommodation examAccommodation = results.get(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(examAccommodation.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
    }

    @Test
    public void shouldReturnTwoAccommodations() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new AccommodationBuilder().build());
        mockExamAccommodations.add(new AccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());
        when(accommodationQueryRepository.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" }))
            .thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(results).hasSize(2);

        ExamAccommodation firstResult = results.get(0);
        assertThat(firstResult.getExamId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentId()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(firstResult.getType()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(firstResult.getCode()).isEqualTo(AccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);

        ExamAccommodation secondResult = results.get(1);
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

        List<ExamAccommodation> result = accommodationService.findAccommodations(AccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            AccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }
}
