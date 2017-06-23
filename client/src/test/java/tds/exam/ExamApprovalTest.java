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

package tds.exam;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.exam.ExamStatusStage.CLOSED;
import static tds.exam.ExamStatusStage.IN_USE;
import static tds.exam.ExamStatusStage.OPEN;

public class ExamApprovalTest {
    @Test
    public void shouldCreateAnExamApproval() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("open", OPEN);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithApprovedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("approved", OPEN);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithDeniedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("denied", CLOSED);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.DENIED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithLogoutStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("paused", IN_USE);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.LOGOUT);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateExamApprovalWithWaitingStatusWhenExamStatusIsNotRecognized() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("foo", CLOSED);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenStatusIsNull() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode(null, null);

        new ExamApproval(mockExamId, mockExamStatusCode, "unit test");
    }
}
