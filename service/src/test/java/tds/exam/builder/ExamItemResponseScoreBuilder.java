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

package tds.exam.builder;

import org.joda.time.Instant;

import tds.exam.ExamItemResponseScore;
import tds.exam.ExamScoringStatus;

/**
 * Build an {@link tds.exam.ExamItemResponseScore} with sample data
 */
public class ExamItemResponseScoreBuilder {
    private int score = -1;
    private ExamScoringStatus scoringStatus = ExamScoringStatus.WAITING_FOR_MACHINE_SCORE;
    private String scoringRationale = "Waiting for machine score";
    private String scoringDimensions;
    private Instant scoredAt;

    public ExamItemResponseScore build() {
        return new ExamItemResponseScore.Builder()
            .withScore(score)
            .withScoringStatus(scoringStatus)
            .withScoringRationale(scoringRationale)
            .withScoringDimensions(scoringDimensions)
            .withScoredAt(scoredAt)
            .build();
    }

    public ExamItemResponseScoreBuilder withScore(int score) {
        this.score = score;
        return this;
    }

    public ExamItemResponseScoreBuilder withScoringStatus(ExamScoringStatus scoringStatus) {
        this.scoringStatus = scoringStatus;
        return this;
    }

    public ExamItemResponseScoreBuilder withScoringRationale(String scoringRationale) {
        this.scoringRationale = scoringRationale;
        return this;
    }

    public ExamItemResponseScoreBuilder withScoringDimensions(String scoringDimensions) {
        this.scoringDimensions = scoringDimensions;
        return this;
    }

    public ExamItemResponseScoreBuilder withScoredAt(Instant scoredAt) {
        this.scoredAt = scoredAt;
        return this;
    }
}
