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

import org.joda.time.Instant;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamItemResponseTest {
    @Test
    public void shouldBuildAnItemResponse() {
        UUID examId = UUID.randomUUID();
        UUID examItemId = UUID.randomUUID();
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse("response text")
            .withExamItemId(examItemId)
            .withExamId(examId)
            .withSequence(1)
            .withCreatedAt(Instant.now().minus(20000))
            .build();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getResponse()).isEqualTo("response text");
        assertThat(response.getExamItemId()).isEqualTo(examItemId);
        assertThat(response.getExamId()).isEqualTo(examId);
        assertThat(response.getSequence()).isEqualTo(1);
        assertThat(response.getCreatedAt()).isLessThan(Instant.now());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenResponseIsNull() {
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse(null)
            .withSequence(1)
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAnIllegalArgumentExceptionWhenSequenceIsLessThanOne() {
        ExamItemResponse response = new ExamItemResponse.Builder()
            .withId(1L)
            .withResponse("response")
            .withSequence(0)
            .build();
    }
}
