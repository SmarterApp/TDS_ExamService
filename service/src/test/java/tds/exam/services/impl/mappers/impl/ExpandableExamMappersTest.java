package tds.exam.services.impl.mappers.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.ExamSegment;
import tds.exam.ExamStatusCode;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeNote;
import tds.exam.ExamineeRelationship;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamPrintRequestService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamStatusService;
import tds.exam.services.ExamineeNoteService;
import tds.exam.services.ExamineeService;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.mappers.impl.ExamAccommodationsExpandableExamMapper;
import tds.exam.mappers.impl.ExamPageItemResponseExpandableExamMapper;
import tds.exam.mappers.impl.ExamSegmentExpandableExamMapper;
import tds.exam.mappers.impl.ExamStatusExpandableExamMapper;
import tds.exam.mappers.impl.ExamineeAttributesExpandableExamMapper;
import tds.exam.mappers.impl.ExamineeNotesExpandableExamMapper;
import tds.exam.mappers.impl.ItemResponseExpandableExamMapper;
import tds.exam.mappers.impl.PrintRequestsExpandableExamMapper;

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

    // Used for TRT
    private ExpandableExamMapper examSegmentExpandableExamMapper;
    private ExpandableExamMapper examineeNotesExpandableExamMapper;
    private ExpandableExamMapper examineeAttributesExpandableExamMapper;
    private ExpandableExamMapper examStatusExpandableExamMapper;
    private ExpandableExamMapper examPageItemResponseExpandableExamMapper;

    private final Set<ExpandableExamAttributes> expandableExamAttributes = ImmutableSet.of(
        ExpandableExamAttributes.EXAM_ACCOMMODATIONS,
        ExpandableExamAttributes.ITEM_RESPONSE_COUNT,
        ExpandableExamAttributes.UNFULFILLED_REQUEST_COUNT
    );

    private final Set<ExpandableExamAttributes> expandableExamParametersTrt = ImmutableSet.of(
        ExpandableExamAttributes.EXAM_SEGMENTS,
        ExpandableExamAttributes.EXAM_NOTES,
        ExpandableExamAttributes.EXAMINEE_ATTRIBUTES_AND_RELATIONSHIPS,
        ExpandableExamAttributes.EXAM_PAGE_AND_ITEMS,
        ExpandableExamAttributes.EXAM_STATUS_DATES
    );

    @Mock
    private ExamItemService mockExamItemService;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Mock
    private ExamPrintRequestService mockExamPrintRequestService;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    @Mock
    private ExamineeNoteService mockExamineeNoteService;

    @Mock
    private ExamineeService mockExamineeService;

    @Mock
    private ExamPageService mockExamPageService;

    @Mock
    private ExamStatusService mockExamStatusService;

    @Before
    public void setup() {
        itemResponseExpandableExamMapper = new ItemResponseExpandableExamMapper(mockExamItemService);
        examAccommodationsExpandableExamMapper = new ExamAccommodationsExpandableExamMapper(mockExamAccommodationService);
        printRequestsExpandableExamMapper = new PrintRequestsExpandableExamMapper(mockExamPrintRequestService);
        examSegmentExpandableExamMapper = new ExamSegmentExpandableExamMapper(mockExamSegmentService);
        examineeNotesExpandableExamMapper = new ExamineeNotesExpandableExamMapper(mockExamineeNoteService);
        examineeAttributesExpandableExamMapper = new ExamineeAttributesExpandableExamMapper(mockExamineeService);
        examPageItemResponseExpandableExamMapper = new ExamPageItemResponseExpandableExamMapper(mockExamPageService, mockExamItemService);
        examStatusExpandableExamMapper = new ExamStatusExpandableExamMapper(mockExamStatusService);
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

    @Test
    public void shouldMapExamSegmentsToExpandableExam() {
        Exam exam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(exam);
        List<ExamSegment> examSegmentList = Arrays.asList(random(ExamSegment.class));

        when(mockExamSegmentService.findExamSegments(exam.getId())).thenReturn(examSegmentList);
        examSegmentExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(exam.getId(), expandableExamBuilder), exam.getSessionId()
        );
        verify(mockExamSegmentService).findExamSegments(exam.getId());

        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getExamSegments()).isEqualTo(examSegmentList);
    }

    @Test
    public void shouldMapExamineeNotesToExpandableExam() {
        Exam exam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(exam);
        List<ExamineeNote> examineeNotesList = Arrays.asList(random(ExamineeNote.class));

        when(mockExamineeNoteService.findAllNotes(exam.getId())).thenReturn(examineeNotesList);
        examineeNotesExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(exam.getId(), expandableExamBuilder), exam.getSessionId()
        );
        verify(mockExamineeNoteService).findAllNotes(exam.getId());

        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getExamineeNotes()).isEqualTo(examineeNotesList);
    }

    @Test
    public void shouldMapExamineeAttributesAndRelationshipsToExpandableExam() {
        Exam exam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(exam);
        List<ExamineeAttribute> examineeAttributes = Arrays.asList(random(ExamineeAttribute.class));
        List<ExamineeRelationship> examineeRelationships = Arrays.asList(random(ExamineeRelationship.class));

        when(mockExamineeService.findAllAttributes(exam.getId())).thenReturn(examineeAttributes);
        when(mockExamineeService.findAllRelationships(exam.getId())).thenReturn(examineeRelationships);

        examineeAttributesExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(exam.getId(), expandableExamBuilder), exam.getSessionId()
        );

        verify(mockExamineeService).findAllAttributes(exam.getId());
        verify(mockExamineeService).findAllRelationships(exam.getId());

        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getExamineeAttributes()).isEqualTo(examineeAttributes);
        assertThat(expandableExam.getExamineeRelationships()).isEqualTo(examineeRelationships);
    }

    @Test
    public void shouldMapExamStatusesToExpandableExam() {
        Exam exam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(exam);

        Instant dateCompleted = Instant.now();
        Instant dateStarted = Instant.now().minus(500);

        when(mockExamStatusService.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_COMPLETED))
            .thenReturn(Optional.of(dateCompleted));
        when(mockExamStatusService.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_FORCE_COMPLETED))
            .thenReturn(Optional.absent());
        when(mockExamStatusService.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_STARTED))
            .thenReturn(Optional.of(dateStarted));

        examStatusExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(exam.getId(), expandableExamBuilder), exam.getSessionId()
        );

        verify(mockExamStatusService).findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_COMPLETED);
        verify(mockExamStatusService).findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_FORCE_COMPLETED);
        verify(mockExamStatusService).findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_STARTED);

        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getStartedAt()).isEqualTo(dateStarted);
        assertThat(expandableExam.getCompletedAt()).isEqualTo(dateCompleted);
        assertThat(expandableExam.getForceCompletedAt()).isNull();
    }
}
