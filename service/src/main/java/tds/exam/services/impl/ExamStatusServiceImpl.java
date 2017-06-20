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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.services.ExamStatusService;

@Service
public class ExamStatusServiceImpl implements ExamStatusService {
    private final ExamStatusQueryRepository examStatusQueryRepository;

    @Autowired
    public ExamStatusServiceImpl(final ExamStatusQueryRepository examStatusQueryRepository) {
        this.examStatusQueryRepository = examStatusQueryRepository;
    }

    @Override
    public Optional<Instant> findRecentTimeAtStatus(final UUID examId, final String examStatus) {
        return examStatusQueryRepository.findRecentTimeAtStatus(examId, examStatus);
    }
}
