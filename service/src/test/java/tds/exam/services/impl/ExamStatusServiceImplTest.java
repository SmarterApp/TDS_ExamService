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

package tds.exam.services.impl;

import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.services.ExamStatusService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamStatusServiceImplTest {
    @Mock
    private ExamStatusQueryRepository mockExamStatusQueryRepository;

    private ExamStatusService examStatusService;

    @Before
    public void setup() {
        examStatusService = new ExamStatusServiceImpl(mockExamStatusQueryRepository);
    }

    @Test
    public void shouldFindExamStatusStartedAt() {
        final Instant now = Instant.now();
        final UUID examId = UUID.randomUUID();
        when(mockExamStatusQueryRepository.findRecentTimeAtStatus(examId, ExamStatusCode.STATUS_STARTED)).thenReturn(Optional.of(now));
        Optional<Instant> maybeStartDate = examStatusService.findRecentTimeAtStatus(examId, ExamStatusCode.STATUS_STARTED);

        verify(mockExamStatusQueryRepository).findRecentTimeAtStatus(examId, ExamStatusCode.STATUS_STARTED);
        assertThat(maybeStartDate.isPresent()).isTrue();
        assertThat(maybeStartDate.get()).isEqualTo(now);
    }
}
