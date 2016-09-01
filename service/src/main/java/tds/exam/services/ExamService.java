package tds.exam.services;

import tds.exam.Exam;

import java.util.UUID;

/**
 * Main entry point for interacting with {@link Exam}
 */
public interface ExamService {

    /**
     * Retrieves an exam based on the UUID
     *
     * @param uuid id for the exam
     * @return {@link Exam} otherwise null
     */
    Exam getExam(UUID uuid);
}
