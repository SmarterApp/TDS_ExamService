package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageServiceImplTest {
    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    private ExamPageService examPageService;

    @Before
    public void setUp() {
        examPageService = new ExamPageServiceImpl(mockExamPageQueryRepository,
            mockExamPageCommandRepository);
    }

    @Test
    public void shouldReturnAllPagesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamPage examPage1 = new ExamPageBuilder()
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);
        examPages.add(examPage2);

        when(mockExamPageQueryRepository.findAll(examId)).thenReturn(examPages);
        List<ExamPage> retExamPages = examPageService.findAllPages(examId);
        assertThat(retExamPages).hasSize(2);
    }

    @Test
    public void shouldDeletePagesForExamId() {
        final UUID examId = UUID.randomUUID();
        examPageService.deletePages(examId);
        verify(mockExamPageCommandRepository).deleteAll(examId);
    }

    @Test
    public void shouldInsertPagesForExamId() {
        ExamPage examPage1 = new ExamPageBuilder()
            .build();

        examPageService.insertPages(examPage1);
        verify(mockExamPageCommandRepository).insert(examPage1);
    }

    @Test
    public void shouldFindExamPageById() {
        ExamPage examPage = new ExamPageBuilder().build();

        when(mockExamPageQueryRepository.find(examPage.getId())).thenReturn(Optional.of(examPage));

        assertThat(examPageService.find(examPage.getId()).get()).isEqualTo(examPage);
        verify(mockExamPageQueryRepository).find(examPage.getId());
    }

    @Test
    public void shouldUpdateExamPage() {
        ExamPage examPage = new ExamPageBuilder().build();
        ArgumentCaptor<ExamPage> captor = ArgumentCaptor.forClass(ExamPage.class);

        examPageService.update(examPage);
        verify(mockExamPageCommandRepository).update(captor.capture());

        assertThat(captor.getValue()).isEqualTo(examPage);
    }
}
