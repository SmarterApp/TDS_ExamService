package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import tds.exam.models.ItemGroupHistory;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.ExamHistoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamHistoryServiceImplTest {
    @Mock
    private HistoryQueryRepository mockHistoryQueryRepository;

    private ExamHistoryService examHistoryService;

    @Before
    public void setUp() {
        examHistoryService = new ExamHistoryServiceImpl(mockHistoryQueryRepository);
    }

    @Test
    public void shouldFindItemGroupHistories() {
        ItemGroupHistory history = new ItemGroupHistory(UUID.randomUUID(), new HashSet<>());

        UUID excludedExamId = UUID.randomUUID();
        long studentId = 1;
        String assessmentId = "ELA 3";

        when(mockHistoryQueryRepository.findPreviousItemGroups(studentId, excludedExamId, assessmentId)).thenReturn(Collections.singletonList(history));

        assertThat(examHistoryService.findPreviousItemGroups(studentId, excludedExamId, assessmentId)).containsExactly(history);

        verify(mockHistoryQueryRepository).findPreviousItemGroups(studentId, excludedExamId, assessmentId);
    }
}