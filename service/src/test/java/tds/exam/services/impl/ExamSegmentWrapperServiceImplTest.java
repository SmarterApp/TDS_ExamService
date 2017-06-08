package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamSegment;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.services.ExamPageWrapperService;
import tds.exam.services.ExamSegmentService;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamSegmentWrapperServiceImplTest {
    @Mock
    private ExamPageWrapperService mockExamPageWrapperService;

    @Mock
    private ExamSegmentService mockExamSegmentService;

    private ExamSegmentWrapperService examSegmentWrapperService;

    @Before
    public void setUp() {
        examSegmentWrapperService = new ExamSegmentWrapperServiceImpl(mockExamSegmentService, mockExamPageWrapperService);
    }

    @Test
    public void shouldReturnExamSegmentWrappersForExamId() {
        UUID examId = UUID.randomUUID();
        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamSegment examSegment2 = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey2")
            .withSegmentPosition(2)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .withVisible(true)
            .build(),
            Collections.emptyList());

        ExamPageWrapper examPageWrapper2 = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(2)
            .withVisible(true)
            .build(),
            Collections.emptyList());

        ExamPageWrapper examPageWrapper3 = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey2")
            .withPagePosition(3)
            .withVisible(true)
            .build(),
            Collections.emptyList());


        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Arrays.asList(examSegment, examSegment2));
        when(mockExamPageWrapperService.findPagesWithItems(examId)).thenReturn(Arrays.asList(examPageWrapper, examPageWrapper2, examPageWrapper3));

        List<ExamSegmentWrapper> examSegmentWrappers = examSegmentWrapperService.findAllExamSegments(examId);

        assertThat(examSegmentWrappers).hasSize(2);

        ExamSegmentWrapper wrapper = examSegmentWrappers.get(0);

        assertThat(wrapper.getExamSegment()).isEqualTo(examSegment);
        assertThat(wrapper.getExamPages()).containsExactly(examPageWrapper, examPageWrapper2);

        wrapper = examSegmentWrappers.get(1);

        assertThat(wrapper.getExamSegment()).isEqualTo(examSegment2);
        assertThat(wrapper.getExamPages()).containsExactly(examPageWrapper3);
    }


    @Test
    public void shouldReturnEmptyExamSegmentWrapperWhenNotFoundForExamIdAndPosition() {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.empty());
        assertThat(examSegmentWrapperService.findExamSegment(examId, 1)).isNotPresent();
    }

    @Test
    public void shouldReturnSegmentWrapperForExamAndPosition() {
        UUID examId = UUID.randomUUID();
        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            Collections.emptyList());

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.of(examSegment));
        when(mockExamPageWrapperService.findPagesForExamSegment(examId, examSegment.getSegmentKey())).thenReturn(Collections.singletonList(examPageWrapper));

        Optional<ExamSegmentWrapper> maybeWrapper = examSegmentWrapperService.findExamSegment(examId, 1);

        assertThat(maybeWrapper).isPresent();

        ExamSegmentWrapper wrapper = maybeWrapper.get();

        assertThat(wrapper.getExamSegment()).isEqualTo(examSegment);
        assertThat(wrapper.getExamPages()).containsExactly(examPageWrapper);
    }

    @Test
    public void shouldReturnEmptyExamSegmentWrapperWhenExamSegmentCannotBeFound() {
        UUID examId = UUID.randomUUID();
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.empty());
        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.of(examPageWrapper));

        assertThat(examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).isNotPresent();
    }

    @Test
    public void shouldReturnEmptyExamSegmentWrapperWhenExamPageCannotBeFound() {
        UUID examId = UUID.randomUUID();
        ExamSegment examSegment = mock(ExamSegment.class);

        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.of(examSegment));
        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.empty());

        assertThat(examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).isNotPresent();
    }

    @Test
    public void shouldReturnExamSegmentWrapperWithSinglePage() {
        UUID examId = UUID.randomUUID();
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        ExamSegment examSegment = mock(ExamSegment.class);

        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.of(examPageWrapper));
        when(mockExamSegmentService.findByExamIdAndSegmentPosition(examId, 1)).thenReturn(Optional.of(examSegment));

        ExamSegmentWrapper wrapper = examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2).get();

        assertThat(wrapper.getExamSegment()).isEqualTo(examSegment);
        assertThat(wrapper.getExamPages()).containsExactly(examPageWrapper);
    }

    @Test
    public void shouldReturnExamSegmentWrapperByExamIdAndPageNumber() {
        UUID examId = UUID.randomUUID();
        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder().withSegmentKey("segmentKey").build(), Collections.emptyList());
        ExamSegment examSegment = new ExamSegmentBuilder().withSegmentKey("segmentKey").build();

        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.of(examPageWrapper));
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Collections.singletonList(examSegment));

        ExamSegmentWrapper wrapper = examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 2).get();

        assertThat(wrapper.getExamSegment()).isEqualTo(examSegment);
        assertThat(wrapper.getExamPages()).containsExactly(examPageWrapper);
    }

    @Test
    public void shouldReturnEmptyExamSegmentWrapperByExamIdAndPageNumber() {
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.empty());

        assertThat(examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 2)).isNotPresent();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenExamSegmentCannotBeFoundForExamPage() {
        UUID examId = UUID.randomUUID();
        ExamPageWrapper examPageWrapper = new ExamPageWrapper(
            new ExamPageBuilder()
                .withSegmentKey("segmentKey")
                .build(), Collections.emptyList());
        ExamSegment examSegment = new ExamSegmentBuilder().withSegmentKey("bogusKey").build();

        when(mockExamPageWrapperService.findPageWithItems(examId, 2)).thenReturn(Optional.of(examPageWrapper));
        when(mockExamSegmentService.findExamSegments(examId)).thenReturn(Collections.singletonList(examSegment));

        examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 2);
    }
}