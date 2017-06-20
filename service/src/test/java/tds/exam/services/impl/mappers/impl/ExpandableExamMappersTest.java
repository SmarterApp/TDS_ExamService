/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
import java.util.Collections;
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
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.mappers.impl.ExamAccommodationsExpandableExamMapper;
import tds.exam.mappers.impl.ExamSegmentExpandableExamMapper;
import tds.exam.mappers.impl.ExamSegmentWrappersExpandableExamMapper;
import tds.exam.mappers.impl.ExamStatusExpandableExamMapper;
import tds.exam.mappers.impl.ExamineeAttributesExpandableExamMapper;
import tds.exam.mappers.impl.ExamineeNotesExpandableExamMapper;
import tds.exam.mappers.impl.ItemResponseCountExpandableExamMapper;
import tds.exam.mappers.impl.ItemResponseUpdateExpandableExamMapper;
import tds.exam.mappers.impl.PrintRequestsExpandableExamMapper;
import tds.exam.mappers.impl.WindowAttemptsExpandableExamMapper;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ExamItemService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamPrintRequestService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.services.ExamService;
import tds.exam.services.ExamStatusService;
import tds.exam.services.ExamineeNoteService;
import tds.exam.services.ExamineeService;
import tds.exam.wrapper.ExamSegmentWrapper;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
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
    private ExpandableExamMapper windowAttemptsExpandableExamMapper;
    private ExpandableExamMapper itemResponseUpdateExpandableExamMapper;
    private ExpandableExamMapper examSegmentWrappersExpandableExamMapper;

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
        ExpandableExamAttributes.EXAM_STATUS_DATES,
        ExpandableExamAttributes.WINDOW_ATTEMPTS,
        ExpandableExamAttributes.ITEM_RESPONSE_UPDATES,
        ExpandableExamAttributes.EXAM_SEGMENT_WRAPPERS
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

    @Mock
    private ExamSegmentWrapperService mockExamSegmentWrapperService;

    @Mock
    private ExamService mockExamService;

    @Before
    public void setup() {
        itemResponseExpandableExamMapper = new ItemResponseCountExpandableExamMapper(mockExamItemService);
        examAccommodationsExpandableExamMapper = new ExamAccommodationsExpandableExamMapper(mockExamAccommodationService);
        printRequestsExpandableExamMapper = new PrintRequestsExpandableExamMapper(mockExamPrintRequestService);
        examSegmentExpandableExamMapper = new ExamSegmentExpandableExamMapper(mockExamSegmentService);
        examineeNotesExpandableExamMapper = new ExamineeNotesExpandableExamMapper(mockExamineeNoteService);
        examineeAttributesExpandableExamMapper = new ExamineeAttributesExpandableExamMapper(mockExamineeService);
        examStatusExpandableExamMapper = new ExamStatusExpandableExamMapper(mockExamStatusService);
        examSegmentWrappersExpandableExamMapper = new ExamSegmentWrappersExpandableExamMapper(mockExamSegmentWrapperService);
        windowAttemptsExpandableExamMapper = new WindowAttemptsExpandableExamMapper(mockExamService);
        itemResponseUpdateExpandableExamMapper = new ItemResponseUpdateExpandableExamMapper(mockExamItemService);
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
        assertThat(expandableExam2.getExamAccommodations()).isEmpty();
    }

    @Test
    public void shouldMapExamSegmentsToExpandableExam() {
        Exam exam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(exam);
        List<ExamSegment> examSegmentList = Collections.singletonList(random(ExamSegment.class));

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
        List<ExamineeNote> examineeNotesList = Collections.singletonList(random(ExamineeNote.class));

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
        List<ExamineeAttribute> examineeAttributes = Collections.singletonList(random(ExamineeAttribute.class));
        List<ExamineeRelationship> examineeRelationships = Collections.singletonList(random(ExamineeRelationship.class));

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
        Instant dateForceCompleted = Instant.now().minus(500);

        when(mockExamStatusService.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_FORCE_COMPLETED))
            .thenReturn(Optional.of(dateForceCompleted));

        examStatusExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(exam.getId(), expandableExamBuilder), exam.getSessionId()
        );

        verify(mockExamStatusService).findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_FORCE_COMPLETED);
        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getForceCompletedAt()).isEqualTo(dateForceCompleted);
    }

    @Test
    public void shouldMapWindowAttemptsToExpandableExam() {
        Exam currentExam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(currentExam);
        // Same assessment/window
        Exam exam1 = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withClientName(currentExam.getClientName())
            .withAssessmentKey(currentExam.getAssessmentKey())
            .withAssessmentWindowId(currentExam.getAssessmentWindowId())
            .build();

        // Different assessment
        Exam exam2 = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withClientName(currentExam.getClientName())
            .withAssessmentWindowId(currentExam.getAssessmentWindowId())
            .build();

        // Different window
        Exam exam3 = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withClientName(currentExam.getClientName())
            .withAssessmentKey(currentExam.getAssessmentKey())
            .build();

        when(mockExamService.findAllExamsForStudent(currentExam.getStudentId())).thenReturn(Arrays.asList(exam1, exam2, exam3, currentExam));
        windowAttemptsExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(currentExam.getId(), expandableExamBuilder), currentExam.getSessionId());
        verify(mockExamService).findAllExamsForStudent(currentExam.getStudentId());
        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getWindowAttempts()).isEqualTo(2);
    }

    @Test
    public void shouldMapExamSegmentWrappersToExpandableExam() {
        Exam currentExam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(currentExam);
        List<ExamSegmentWrapper> wrappers = randomListOf(2, ExamSegmentWrapper.class);

        when(mockExamSegmentWrapperService.findAllExamSegments(currentExam.getId())).thenReturn(wrappers);
        examSegmentWrappersExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(currentExam.getId(), expandableExamBuilder), currentExam.getSessionId());
        verify(mockExamSegmentWrapperService).findAllExamSegments(currentExam.getId());
        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getExamSegmentWrappers()).isEqualTo(wrappers);
    }

    @Test
    public void shouldMapItemResponseUpdatesToExpandableExam() {
        Exam currentExam = random(Exam.class);
        ExpandableExam.Builder expandableExamBuilder = new ExpandableExam.Builder(currentExam);
        UUID examItemId1 = UUID.randomUUID();
        UUID examItemId2 = UUID.randomUUID();

        when(mockExamItemService.getResponseUpdateCounts(currentExam.getId())).thenReturn(
            ImmutableMap.of(
                examItemId1, 2,
                examItemId2, 3
            )
        );
        itemResponseUpdateExpandableExamMapper.updateExpandableMapper(expandableExamParametersTrt,
            ImmutableMap.of(currentExam.getId(), expandableExamBuilder), currentExam.getSessionId());
        verify(mockExamItemService).getResponseUpdateCounts(currentExam.getId());
        ExpandableExam expandableExam = expandableExamBuilder.build();
        assertThat(expandableExam.getItemResponseUpdates().get(examItemId1)).isEqualTo(2);
        assertThat(expandableExam.getItemResponseUpdates().get(examItemId2)).isEqualTo(3);
    }
}
