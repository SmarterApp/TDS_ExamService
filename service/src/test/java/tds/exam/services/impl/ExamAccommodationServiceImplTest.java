package tds.exam.services.impl;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.repositories.ExamAccommodationQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamAccommodationServiceImplTest {
    private ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private ExamAccommodationServiceImpl accommodationService;

    @Before
    public void setUp() {
        examAccommodationQueryRepository = mock(ExamAccommodationQueryRepository.class);
        accommodationService = new ExamAccommodationServiceImpl(examAccommodationQueryRepository);
    }

    @Test
    public void shouldReturnAnAccommodation() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        when(examAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE})).thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = accommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});

        assertThat(results).hasSize(1);
        ExamAccommodation examAccommodation = results.get(0);
        assertThat(examAccommodation.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(examAccommodation.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(examAccommodation.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
    }

    @Test
    public void shouldReturnTwoAccommodations() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());
        when(examAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" }))
            .thenReturn(mockExamAccommodations);

        List<ExamAccommodation> results = accommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] {
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning" });

        assertThat(results).hasSize(2);

        ExamAccommodation firstResult = results.get(0);
        assertThat(firstResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(firstResult.getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(firstResult.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);

        ExamAccommodation secondResult = results.get(1);
        assertThat(secondResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondResult.getSegmentId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID);
        assertThat(secondResult.getType()).isEqualTo("closed captioning");
        assertThat(secondResult.getCode()).isEqualTo("TDS_ClosedCap0");
    }

    @Test
    public void shouldReturnAnEmptyListWhenSearchingForAccommodationsThatDoNotExist() {
        when(examAccommodationQueryRepository.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { "foo", "bar" })).thenReturn(Lists.emptyList());

        List<ExamAccommodation> result = accommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_ID,
            new String[] { "foo", "bar" });

        assertThat(result).isNotNull();
        assertThat(result).hasSize(0);
    }
}
