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
 * Describe the possible contexts for an {@link tds.exam.ExamineeNote}:
 * <ul>
 * <li>{@link #EXAM}</li>
 * <li>{@link #ITEM}</li>
 * </ul>
 */
public enum ExamineeNoteContext {
    /**
     * The {@link tds.exam.ExamineeNote} was created using the Notepad tool for the {@link tds.exam.Exam}.
     */
    EXAM("GlobalNotes"),

    /**
     * The {@link tds.exam.ExamineeNote} was created using the Notepad tool at a specific {@link tds.exam.ExamItem}
     */
    ITEM("TESTITEM");

    private final String type;

    ExamineeNoteContext(final String type) {
        this.type = type;
    }

    String getType() {
        return type;
    }

    /**
     * Get an {@link ExamineeNoteContext} from its string representation
     *
     * @param type The string that describes the type of {@link ExamineeNoteContext} to get
     * @return The equivalent {@link ExamineeNoteContext}
     */
    public static ExamineeNoteContext fromType(final String type) {
        for (ExamineeNoteContext context : ExamineeNoteContext.values()) {
            if (context.getType().equals(type)) {
                return context;
            }
        }

        throw new IllegalArgumentException(String.format("Could not find ExamineeNoteContext for %s", type));
    }

    @Override
    public String toString() {
        return type;
    }
}
