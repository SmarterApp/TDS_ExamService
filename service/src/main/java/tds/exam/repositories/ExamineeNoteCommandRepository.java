package tds.exam.repositories;

import tds.exam.ExamineeNote;

/**
 * Handles data modification for the examinee_note table
 */
public interface ExamineeNoteCommandRepository {
    /**
     * Persist an {@link tds.exam.ExamineeNote} to the database
     *
     * @param examineeNote The {@link tds.exam.ExamineeNote} to persist
     */
    void insert(final ExamineeNote examineeNote);
}
