package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tds.exam.models.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamResponseCommandRepository;
import tds.exam.repositories.ExamResponseQueryRepository;
import tds.exam.services.ExamItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamItemServiceImplTest {
    @Mock
    private ExamResponseQueryRepository mockExamResponseQueryRepository;

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    private ExamItemService examItemService;

    @Before
    public void setUp() {
        examItemService = new ExamItemServiceImpl(mockExamPageQueryRepository, mockExamPageCommandRepository,
            mockExamResponseQueryRepository);
    }

    @Test
    public void shouldReturnLatestExamPositionForExamId() {
        final UUID examId = UUID.randomUUID();
        final int currentExamPosition = 9;
        when(mockExamResponseQueryRepository.getCurrentExamItemPosition(examId)).thenReturn(currentExamPosition);
        int examPosition = examItemService.getExamPosition(examId);
        assertThat(examPosition).isEqualTo(currentExamPosition);
        verify(mockExamResponseQueryRepository).getCurrentExamItemPosition(examId);
    }

    @Test
    public void shouldReturnAllPagesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamPage examPage1 = new ExamPage.Builder()
            .withId(UUID.randomUUID())
            .build();
        ExamPage examPage2 = new ExamPage.Builder()
            .withId(UUID.randomUUID())
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);
        examPages.add(examPage2);

        when(mockExamPageQueryRepository.findAll(examId)).thenReturn(examPages);
        List<ExamPage> retExamPages = examItemService.findAllPages(examId);
        assertThat(retExamPages).hasSize(2);
    }

    @Test
    public void shouldDeletePagesForExamId() {
        final UUID examId = UUID.randomUUID();
        examItemService.deletePages(examId);
        verify(mockExamPageCommandRepository).deleteAll(examId);
    }

    @Test
    public void shouldInsertPagesForExamId() {
        ExamPage examPage1 = new ExamPage.Builder()
            .withId(UUID.randomUUID())
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);

        examItemService.insertPages(examPages);
        verify(mockExamPageCommandRepository).insert(examPages);
    }
}
