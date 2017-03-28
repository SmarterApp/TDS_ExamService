package tds.exam;

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

/**
 * Score and scoring metadata for the {@link tds.exam.ExamItemResponse}
 */
public class ExamItemResponseScore {
    private int score;
    private ExamScoringStatus scoringStatus;
    private String scoringRationale;
    private String scoringDimensions;
    private Instant scoredAt;
    private Instant scoreSentAt;
    private UUID scoreMark;
    private long scoreLatency;

    private ExamItemResponseScore() {
    }

    public ExamItemResponseScore(Builder builder) {
        score = builder.score;
        scoringStatus = builder.scoringStatus;
        scoringRationale = builder.scoringRationale;
        scoringDimensions = builder.scoringDimensions;
        scoredAt = builder.scoredAt;
        scoreMark = builder.scoreMark;
        scoreSentAt = builder.scoreSentAt;
        scoreLatency = builder.scoreLatency;
    }

    public static final class Builder {
        private int score;
        private ExamScoringStatus scoringStatus;
        private String scoringRationale;
        private String scoringDimensions;
        private Instant scoredAt;
        private UUID scoreMark;
        private Instant scoreSentAt;
        private long scoreLatency;

        public Builder withScore(int score) {
            this.score = score;
            return this;
        }

        public Builder withScoringStatus(ExamScoringStatus scoringStatus) {
            this.scoringStatus = scoringStatus;
            return this;
        }

        public Builder withScoringRationale(String scoringRationale) {
            this.scoringRationale = scoringRationale;
            return this;
        }

        public Builder withScoringDimensions(String scoringDimensions) {
            this.scoringDimensions = scoringDimensions;
            return this;
        }

        public Builder withScoredAt(Instant scoredAt) {
            this.scoredAt = scoredAt;
            return this;
        }

        public Builder withScoreMark(final UUID scoreMark) {
            this.scoreMark = scoreMark;
            return this;
        }

        public Builder withScoreSentAt(final Instant scoreSentAt) {
            this.scoreSentAt = scoreSentAt;
            return this;
        }

        public Builder withScoreLatency(final long scoreLatency) {
            this.scoreLatency = scoreLatency;
            return this;
        }

        public ExamItemResponseScore build() {
            return new ExamItemResponseScore(this);
        }
    }

    /**
     * @return The score applied to the {@link tds.exam.ExamItemResponse}
     */
    public int getScore() {
        return score;
    }

    /**
     * @return The scoring status for this {@link tds.exam.ExamItemResponseScore}
     */
    public ExamScoringStatus getScoringStatus() {
        return scoringStatus;
    }

    /**
     * @return The reason why this {@link tds.exam.ExamItemResponseScore}'s status was set to a particular value
     */
    public String getScoringRationale() {
        return scoringRationale;
    }

    /**
     * @return The scoring dimensions associated with this {@link tds.exam.ExamItemResponse}'s
     * {@link tds.exam.ExamItemResponseScore}
     */
    public Optional<String> getScoringDimensions() {
        return Optional.fromNullable(scoringDimensions);
    }

    /**
     * @return The date/time when the {@link tds.exam.ExamItemResponse} was scored
     */
    public Optional<Instant> getScoredAt() {
        return Optional.fromNullable(scoredAt);
    }

    /**
     * @return A string representing an XML fragment for this {@link tds.exam.ExamItemResponse}'s scoring dimensions.
     */
    public String getScoringDimensionsXml() {
        // Output of StudentDLL.buildSCoreInfoNode(), called on line 1907.  When StudentDLL.T_UpdateScoredResponse_SP is
        // called (line 1907), the scoreDimension attribute is always set to "overall"
        return String.format("<ScoreInfo scorePoint=\"%d\" scoreDimension=\"overall\" scoreStatus=\"%s\"><SubScoreList /></ScoreInfo>", score, scoringStatus);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExamItemResponseScore that = (ExamItemResponseScore) o;

        if (score != that.score) return false;
        if (scoringStatus != that.scoringStatus) return false;
        if (scoringRationale != null ? !scoringRationale.equals(that.scoringRationale) : that.scoringRationale != null)
            return false;
        if (scoringDimensions != null ? !scoringDimensions.equals(that.scoringDimensions) : that.scoringDimensions != null)
            return false;
        if (scoredAt != null ? !scoredAt.equals(that.scoredAt) : that.scoredAt != null) return false;
        return scoreMark != null ? scoreMark.equals(that.scoreMark) : that.scoreMark == null;
    }

    @Override
    public int hashCode() {
        int result = score;
        result = 31 * result + (scoringStatus != null ? scoringStatus.hashCode() : 0);
        result = 31 * result + (scoringRationale != null ? scoringRationale.hashCode() : 0);
        result = 31 * result + (scoringDimensions != null ? scoringDimensions.hashCode() : 0);
        result = 31 * result + (scoredAt != null ? scoredAt.hashCode() : 0);
        result = 31 * result + (scoreMark != null ? scoreMark.hashCode() : 0);
        return result;
    }
}
