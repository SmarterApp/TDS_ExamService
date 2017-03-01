package tds.exam.services.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExpandableExam;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPrintRequestService;
import tds.exam.services.ExpandableExamMapper;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpandableExamMappersTest {
    private ExpandableExamMapper itemResponseExpandableExamMapper;
    private ExpandableExamMapper examAccommodationsExpandableExamMapper;
    private ExpandableExamMapper printRequestsExpandableExamMapper;

    private final Set<String> expandableExamAttributes = ImmutableSet.of(
        ExpandableExam.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS,
        ExpandableExam.EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT,
        ExpandableExam.EXPANDABLE_PARAMS_UNFULFILLED_REQUEST_COUNT
    );

    @Mock
    private ExamItemService mockExamItemService;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Mock
    private ExamPrintRequestService mockExamPrintRequestService;

    @Before
    public void setup() {
        itemResponseExpandableExamMapper = new ItemResponseExpandableExamMapper(mockExamItemService);
        examAccommodationsExpandableExamMapper = new ExamAccommodationsExpandableExamMapper(mockExamAccommodationService);
        printRequestsExpandableExamMapper = new PrintRequestsExpandableExamMapper(mockExamPrintRequestService);
    }

    @Test
    public void shouldMapPrintRequestsToExpandableExams() {
        UUID sessionId = UUID.randomUUID();
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();

        ExpandableExam.Builder expandableExamBuilder1 = new ExpandableExam.Builder(exam1);
        ExpandableExam.Builder expandableExamBuilder2 = new ExpandableExam.Builder(exam2);

        when(mockExamPrintRequestService.findRequestCountsForExamIds(eq(sessionId), any(), any())).thenReturn(ImmutableMap.of(
            exam1.getId(), 2
        ));


        printRequestsExpandableExamMapper.updateExpandableMapper(expandableExamAttributes,
            ImmutableMap.of(
                exam1.getId(), expandableExamBuilder1,
                exam2.getId(), expandableExamBuilder2
            ), sessionId);

        verify(mockExamPrintRequestService).findRequestCountsForExamIds(eq(sessionId), any(), any());

        ExpandableExam expandableExam1 = expandableExamBuilder1.build();
        ExpandableExam expandableExam2 = expandableExamBuilder2.build();

        assertThat(expandableExam1.getRequestCount()).isEqualTo(2);
        assertThat(expandableExam2.getRequestCount()).isEqualTo(0);
    }

    @Test
    public void shouldMapItemResponsesToExpandableExams() {
        UUID sessionId = UUID.randomUUID();
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();

        ExpandableExam.Builder expandableExamBuilder1 = new ExpandableExam.Builder(exam1);
        ExpandableExam.Builder expandableExamBuilder2 = new ExpandableExam.Builder(exam2);

        when(mockExamItemService.getResponseCounts(any(), any())).thenReturn(ImmutableMap.of(
            exam1.getId(), 3
        ));


        itemResponseExpandableExamMapper.updateExpandableMapper(expandableExamAttributes,
            ImmutableMap.of(
                exam1.getId(), expandableExamBuilder1,
                exam2.getId(), expandableExamBuilder2
            ), sessionId);

        verify(mockExamItemService).getResponseCounts(any(), any());

        ExpandableExam expandableExam1 = expandableExamBuilder1.build();
        ExpandableExam expandableExam2 = expandableExamBuilder2.build();

        assertThat(expandableExam1.getItemsResponseCount()).isEqualTo(3);
        assertThat(expandableExam2.getItemsResponseCount()).isEqualTo(0);
    }

    @Test
    public void shouldMapExamAccommodationsToExpandableExams() {
        UUID sessionId = UUID.randomUUID();
        Exam exam1 = new ExamBuilder().withSessionId(sessionId).build();
        Exam exam2 = new ExamBuilder().withSessionId(sessionId).build();

        ExpandableExam.Builder expandableExamBuilder1 = new ExpandableExam.Builder(exam1);
        ExpandableExam.Builder expandableExamBuilder2 = new ExpandableExam.Builder(exam2);
        ExamAccommodation examAccommodation1 = new ExamAccommodation.Builder(UUID.randomUUID())
            .fromExamAccommodation(random(ExamAccommodation.class))
            .withExamId(exam1.getId())
            .build();
        ExamAccommodation examAccommodation2 = new ExamAccommodation.Builder(UUID.randomUUID())
            .fromExamAccommodation(random(ExamAccommodation.class))
            .withExamId(exam1.getId())
            .build();

        when(mockExamAccommodationService.findApprovedAccommodations(any(), any()))
            .thenReturn(Arrays.asList(examAccommodation1, examAccommodation2));


        examAccommodationsExpandableExamMapper.updateExpandableMapper(expandableExamAttributes,
            ImmutableMap.of(
                exam1.getId(), expandableExamBuilder1,
                exam2.getId(), expandableExamBuilder2
            ), sessionId);

        verify(mockExamAccommodationService).findApprovedAccommodations(any(), any());

        ExpandableExam expandableExam1 = expandableExamBuilder1.build();
        ExpandableExam expandableExam2 = expandableExamBuilder2.build();

        assertThat(expandableExam1.getExam()).isEqualTo(exam1);
        assertThat(expandableExam1.getExamAccommodations()).containsExactlyInAnyOrder(examAccommodation1, examAccommodation2);
        assertThat(expandableExam2.getExam()).isEqualTo(exam2);
        assertThat(expandableExam2.getExamAccommodations()).isNull();
    }
}
