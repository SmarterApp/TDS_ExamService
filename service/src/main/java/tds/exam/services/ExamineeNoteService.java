package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;

/**
 * A service for interacting with {@link tds.exam.ExamineeNote}s.
 */
public interface ExamineeNoteService {
    /**
     * Find the most recent {@link tds.exam.ExamineeNote} for the specified {@link tds.exam.Exam}.
     *
     * @param examId The unique identifier of the {@link tds.exam.Exam}
     * @return An Optional of the {@link tds.exam.ExamineeNote}.  If no {@link tds.exam.ExamineeNote} is found,
     * an empty {@link java.util.Optional} is returned instead.
     */
    Optional<ExamineeNote> findNoteInExamContext(final UUID examId);

    /**
     * Persist a {@link tds.exam.ExamineeNote}.
     *
     * @param examineeNote The {@link tds.exam.ExamineeNote} to persist
     */
    void insert(final ExamineeNote examineeNote);
}
