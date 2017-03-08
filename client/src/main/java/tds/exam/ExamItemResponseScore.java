package tds.exam;

import com.google.common.base.Optional;
import org.joda.time.Instant;

/**
 * Score and scoring metadata for the {@link tds.exam.ExamItemResponse}
 */
public class ExamItemResponseScore {
    private int score;
    private ExamScoringStatus scoringStatus;
    private String scoringRationale;
    private String scoringDimensions;
    private Instant scoredAt;

    private ExamItemResponseScore() {
    }

    public ExamItemResponseScore(Builder builder) {
        score = builder.score;
        scoringStatus = builder.scoringStatus;
        scoringRationale = builder.scoringRationale;
        scoringDimensions = builder.scoringDimensions;
        scoredAt = builder.scoredAt;
    }

    public static final class Builder {
        private int score;
        private ExamScoringStatus scoringStatus;
        private String scoringRationale;
        private String scoringDimensions;
        private Instant scoredAt;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamItemResponseScore)) return false;

        ExamItemResponseScore that = (ExamItemResponseScore) o;

        if (getScore() != that.getScore()) return false;
        if (getScoringStatus() != that.getScoringStatus()) return false;
        if (!getScoringRationale().equals(that.getScoringRationale())) return false;
        if (!getScoringDimensions().equals(that.getScoringDimensions())) return false;
        return getScoredAt().equals(that.getScoredAt());
    }

    @Override
    public int hashCode() {
        int result = getScore();
        result = 31 * result + getScoringStatus().hashCode();
        result = 31 * result + getScoringRationale().hashCode();
        result = 31 * result + getScoringDimensions().hashCode();
        result = 31 * result + getScoredAt().hashCode();
        return result;
    }
}
