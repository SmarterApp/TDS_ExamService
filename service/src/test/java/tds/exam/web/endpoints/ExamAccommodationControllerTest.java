package tds.exam.web.endpoints;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.services.ExamAccommodationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamAccommodationControllerTest {
    private ExamAccommodationController controller;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Before
    public void setUp() {
        controller = new ExamAccommodationController(mockExamAccommodationService);
    }

    @Test
    public void shouldGetASingleAccommodation() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());

        when(mockExamAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE))
            .thenReturn(mockExamAccommodations);

        ResponseEntity<List<ExamAccommodation>> response = controller.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[]{ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE});
        verify(mockExamAccommodationService).findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(response.getBody().get(0).getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(response.getBody().get(0).getType()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE);
        assertThat(response.getBody().get(0).getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(response.getBody().get(0).isApproved()).isTrue();
    }

    @Test
    public void shouldGetTwoAccommodationsForTheSpecifiedAccommodationTypes() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .build());

        when(mockExamAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning"))
            .thenReturn(mockExamAccommodations);

        ResponseEntity<List<ExamAccommodation>> response = controller.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[]{
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        ExamAccommodation firstResult = response.getBody().get(0);
        assertThat(firstResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstResult.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(firstResult.isApproved()).isTrue();

        ExamAccommodation secondResult = response.getBody().get(1);
        assertThat(secondResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondResult.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(secondResult.isApproved()).isTrue();
    }

    @Test
    public void shouldIncludeDeniedAccommodations() {
        List<ExamAccommodation> mockExamAccommodations = new ArrayList<>();
        mockExamAccommodations.add(new ExamAccommodationBuilder().build());
        mockExamAccommodations.add(new ExamAccommodationBuilder()
            .withType("closed captioning")
            .withCode("TDS_ClosedCap0")
            .withDeniedAt(Instant.now())
            .build());

        when(mockExamAccommodationService.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
            "closed captioning"))
            .thenReturn(mockExamAccommodations);

        ResponseEntity<List<ExamAccommodation>> response = controller.findAccommodations(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID,
            ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY,
            new String[]{
                ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_TYPE,
                "closed captioning"});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        ExamAccommodation firstResult = response.getBody().get(0);
        assertThat(firstResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(firstResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(firstResult.getCode()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_ACCOMMODATION_CODE);
        assertThat(firstResult.isApproved()).isTrue();

        ExamAccommodation secondResult = response.getBody().get(1);
        assertThat(secondResult.getExamId()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_EXAM_ID);
        assertThat(secondResult.getSegmentKey()).isEqualTo(ExamAccommodationBuilder.SampleData.DEFAULT_SEGMENT_KEY);
        assertThat(secondResult.getCode()).isEqualTo("TDS_ClosedCap0");
        assertThat(secondResult.isApproved()).isFalse();
    }

    @Test
    public void shouldReturnAllAccommodations() {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(Collections.singletonList(examAccommodation));

        ResponseEntity<List<ExamAccommodation>> response = controller.findAccommodations(examId);

        verify(mockExamAccommodationService).findAllAccommodations(examId);
        verifyNoMoreInteractions(mockExamAccommodationService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(examAccommodation);
    }

    @Test
    public void shouldReturnApprovedAccommodations() {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        when(mockExamAccommodationService.findApprovedAccommodations(examId)).thenReturn(Collections.singletonList(examAccommodation));

        ResponseEntity<List<ExamAccommodation>> response = controller.findApprovedAccommodations(examId);

        verify(mockExamAccommodationService).findApprovedAccommodations(examId);
        verifyNoMoreInteractions(mockExamAccommodationService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(examAccommodation);
    }
}
