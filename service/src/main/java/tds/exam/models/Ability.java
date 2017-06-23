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
