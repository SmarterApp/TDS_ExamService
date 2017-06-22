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

import org.springframework.stereotype.Service;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamScoringStatus;
import tds.exam.services.ExamItemResponseScoringService;

@Service
public class TemporaryExamItemResponseScoringServiceImpl implements ExamItemResponseScoringService {
    @Override
    public ExamItemResponseScore getScore(final ExamItemResponse response) {
        // This implementation is temporary until scoring logic is ported over from legacy.  in particular, this
        // return value emulates returning an "empty"/dummy score when isScoringAsynchronous() == true
        // (tds.student.services.ItemScoringService, line 286).
        return new ExamItemResponseScore.Builder()
            .withScore(-1)
            .withScoringStatus(ExamScoringStatus.WAITING_FOR_MACHINE_SCORE)
            .withScoringRationale("Waiting for machine score")
            .build();
    }
}
