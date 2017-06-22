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

public class ExamAccommodationTest {
    @Test
    public void shouldCreateAnAccommodation() {
        UUID mockExamId = UUID.randomUUID();
        UUID mockExamAccommodationId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(mockExamAccommodationId)
            .withExamId(mockExamId)
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.getId()).isEqualTo(mockExamAccommodationId);
        assertThat(examAccommodation.getExamId()).isEqualTo(mockExamId);
        assertThat(examAccommodation.getSegmentKey()).isEqualTo("Segment 1");
        assertThat(examAccommodation.getType()).isEqualTo("unit test type");
        assertThat(examAccommodation.getCode()).isEqualTo("unit test code");
    }

    @Test
    public void shouldBeApproved() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.isApproved()).isTrue();
    }

    @Test
    public void shouldNotBeApproved() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withDeniedAt(Instant.now())
            .withCreatedAt(Instant.now())
            .build();

        assertThat(examAccommodation.isApproved()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecasueExamIdCannotBeNull() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(null)
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecausSegmentIdCannotBeNull() {
        ExamAccommodation examAccommodation = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey(null)
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseTypeCannotBeNull() {
        new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType(null)
            .withCode("unit test code")
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCodeCannotBeNull() {
        new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode(null)
            .withCreatedAt(Instant.now())
            .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionBecauseCreatedAtCannotBeNull() {
        new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withSegmentKey("Segment 1")
            .withType("unit test type")
            .withCode("unit test code")
            .withCreatedAt(null)
            .build();
    }
}
