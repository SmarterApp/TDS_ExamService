package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamScoringControllerTest {

    @Mock
    private ItemScoringService mockItemScoringService;

    private ExamScoringController controller;

    @Before
    public void setup() {
        controller = new ExamScoringController(mockItemScoringService);
    }

    @Test
    public void itShouldUpdateExamScores() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String clientName = "clientName";
        final Float pageDuration = 1.23f;
        final List<ItemResponseUpdate> updates = singletonList(mock(ItemResponseUpdate.class));
        final List<ItemResponseUpdateStatus> responses = singletonList(mock(ItemResponseUpdateStatus.class));

        when(mockItemScoringService.updateResponses(any(ExamInstance.class), anyListOf(ItemResponseUpdate.class), eq(pageDuration)))
            .thenReturn(responses);

        final ResponseEntity<List<ItemResponseUpdateStatus>> response = controller.updateResponses(examId, sessionId, browserId, clientName, pageDuration, updates);
        assertThat(response.getBody()).containsExactlyElementsOf(responses);

        final ArgumentCaptor<ExamInstance> examInstanceArgumentCaptor = ArgumentCaptor.forClass(ExamInstance.class);
        final ArgumentCaptor<List> updatesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockItemScoringService).updateResponses(examInstanceArgumentCaptor.capture(), updatesArgumentCaptor.capture(), eq(pageDuration));

        assertThat(examInstanceArgumentCaptor.getValue().getExamId()).isEqualTo(examId);
        assertThat(examInstanceArgumentCaptor.getValue().getBrowserId()).isEqualTo(browserId);
        assertThat(examInstanceArgumentCaptor.getValue().getSessionId()).isEqualTo(sessionId);
        assertThat(examInstanceArgumentCaptor.getValue().getClientName()).isEqualTo(clientName);

        assertThat(updatesArgumentCaptor.getValue()).containsExactlyElementsOf(updates);
    }

    private ItemResponseUpdate itemResponseUpdate() {
        final ItemResponseUpdate update = new ItemResponseUpdate();
        update.setDateCreated(new Date().toString());
        update.setIsSelected(true);
        update.setIsValid(true);
        update.setPage(1);
        update.setPageKey("pageKey");
        update.setBankKey(123L);
        update.setFilePath("file/path");
        update.setItemKey(456L);
        update.setLanguage("ENU");
        update.setPosition(1);
        update.setScoreMark(UUID.randomUUID());
        update.setSegmentID("segmentId");
        update.setSequence(1);
        update.setTestID("testId");
        update.setValue("value");
        return update;
    }
}