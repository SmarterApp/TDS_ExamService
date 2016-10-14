package tds.exam.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Model representing an exam's ability.
 */
public class Ability {
    private UUID examId;
    private String assessmentId;
    private Integer attempts;
    private Instant dateScored;
    private Double score;

    public Ability() {
         // Default constructor used for SQL row mapping
    }

    public Ability(UUID examId, String assessmentId, Integer attempts, Instant dateScored, Double score) {
        this.examId = examId;
        this.assessmentId = assessmentId;
        this.attempts = attempts;
        this.dateScored = dateScored;
        this.score = score;
    }

    /**
     * @return the id of the exam
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return the id of the assessment
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @return The attempt number of the exam
     */
    public Integer getAttempts() {
        return attempts;
    }

    /**
     * @return the date the exam was scored
     */
    public Instant getDateScored() {
        return dateScored;
    }
    /**
     * @return the ability score value
     */
    public Double getScore() {
        return score;
    }

}
