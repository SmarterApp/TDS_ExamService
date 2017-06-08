package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import tds.exam.repositories.ExamPageWrapperQueryRepository;
import tds.exam.services.ExamPageWrapperService;
import tds.exam.wrapper.ExamPageWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageWrapperServiceImplTest {
    @Mock
    private ExamPageWrapperQueryRepository mockExamPageWrapperQueryRepository;

    private ExamPageWrapperService examPageWrapperService;

    @Before
    public void setUp() {
        examPageWrapperService = new ExamPageWrapperServiceImpl(mockExamPageWrapperQueryRepository);
    }

    @Test
    public void findExamPageByExamId() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPagesWithItems(examId)).thenReturn(Collections.singletonList(examPageWrapper));

        assertThat(examPageWrapperService.findPagesWithItems(examId)).containsExactly(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPagesWithItems(examId);
    }

    @Test
    public void findExamPageByExamIdAndPagePosition() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPageWithItems(examId, 1)).thenReturn(Optional.of(examPageWrapper));

        assertThat(examPageWrapperService.findPageWithItems(examId, 1).get()).isEqualTo(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPageWithItems(examId, 1);
    }

    @Test
    public void findExamPageByExamIdAndSegmentKey() {
        ExamPageWrapper examPageWrapper = mock(ExamPageWrapper.class);
        UUID examId = UUID.randomUUID();

        when(mockExamPageWrapperQueryRepository.findPagesForExamSegment(examId, "segmentKey")).thenReturn(Collections.singletonList(examPageWrapper));

        assertThat(examPageWrapperService.findPagesForExamSegment(examId, "segmentKey")).containsExactly(examPageWrapper);

        verify(mockExamPageWrapperQueryRepository).findPagesForExamSegment(examId, "segmentKey");
    }
}