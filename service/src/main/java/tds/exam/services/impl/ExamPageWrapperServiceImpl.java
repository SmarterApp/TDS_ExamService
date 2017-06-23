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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.repositories.ExamPageWrapperQueryRepository;
import tds.exam.services.ExamPageWrapperService;
import tds.exam.wrapper.ExamPageWrapper;

@Service
public class ExamPageWrapperServiceImpl implements ExamPageWrapperService {
    private final ExamPageWrapperQueryRepository examPageWrapperQueryRepository;

    @Autowired
    public ExamPageWrapperServiceImpl(final ExamPageWrapperQueryRepository examPageWrapperQueryRepository) {
        this.examPageWrapperQueryRepository = examPageWrapperQueryRepository;
    }

    @Override
    public Optional<ExamPageWrapper> findPageWithItems(final UUID examId, final int pagePosition) {
        return examPageWrapperQueryRepository.findPageWithItems(examId, pagePosition);
    }

    @Override
    public List<ExamPageWrapper> findPagesWithItems(final UUID examId) {
        return examPageWrapperQueryRepository.findPagesWithItems(examId);
    }

    @Override
    public List<ExamPageWrapper> findPagesForExamSegment(final UUID examId, final String segmentKey) {
        return examPageWrapperQueryRepository.findPagesForExamSegment(examId, segmentKey);
    }
}
