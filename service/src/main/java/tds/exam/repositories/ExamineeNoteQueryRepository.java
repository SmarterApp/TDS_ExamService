package tds.exam.repositories;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;

/**
 * Handles data reads from the examinee_note table
 */
public interface ExamineeNoteQueryRepository {
    /**
     * Get the most recent {@link tds.exam.ExamineeNote} for the specified {@link tds.exam.Exam}
     *
     * @param examId The unique identifier of the {@link tds.exam.Exam}
     * @return The {@link tds.exam.ExamineeNote} with an {@link tds.exam.ExamineeNoteContext} of "exam"
     */
    Optional<ExamineeNote> findNoteInExamContext(final UUID examId);
}
