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

package tds.exam.utils.listeners;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.common.EntityUpdate;
import tds.common.entity.utils.ChangeListener;
import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamAccommodationService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OnDeniedStatusExamChangeListenerTest {
    private ChangeListener<Exam> onDeniedStatusExamChangeListener;

    @Mock
    private ExamAccommodationService mockExamAccommodationService;

    @Before
    public void setup() {
        onDeniedStatusExamChangeListener = new OnDeniedStatusExamChangeListener(mockExamAccommodationService);
    }

    @Test
    public void shouldCallDenyAccommodationsIfExamHasBeenUpdatedToDenied() {
        Exam exam = new ExamBuilder().build();
        Exam deniedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_DENIED), Instant.now())
            .build();

        onDeniedStatusExamChangeListener.accept(new EntityUpdate<>(exam, deniedExam));
        verify(mockExamAccommodationService).denyAccommodations(exam.getId(), deniedExam.getChangedAt());
    }

    @Test
    public void shouldNotCallDenyIfExamIsNotDenied() {
        Exam exam = new ExamBuilder().build();
        Exam deniedExam = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_APPROVED), Instant.now())
            .build();

        onDeniedStatusExamChangeListener.accept(new EntityUpdate<>(exam, deniedExam));
        verify(mockExamAccommodationService, never()).denyAccommodations(exam.getId(), deniedExam.getChangedAt());
    }
}
