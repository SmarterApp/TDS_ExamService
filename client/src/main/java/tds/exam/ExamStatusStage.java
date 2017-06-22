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

package tds.exam;

/**
 * Exam status also has an associated stage.
 */
public enum ExamStatusStage {
    IN_PROGRESS("inprogress"),
    IN_USE("inuse"),
    CLOSED("closed"),
    INACTIVE("inactive"),
    OPEN("open");

    private final String type;

    ExamStatusStage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * @param type the string type for the stage
     * @return the equivalent {@link tds.exam.ExamStatusStage}
     */
    public static ExamStatusStage fromType(String type) {
        for (ExamStatusStage stage : ExamStatusStage.values()) {
            if (stage.getType().equals(type)) {
                return stage;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamStatusStage for %s", type));
    }
}
