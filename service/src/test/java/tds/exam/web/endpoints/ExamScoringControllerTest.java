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
}