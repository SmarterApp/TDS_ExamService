package tds.exam.services;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.OpenExam;

import java.util.Optional;
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
    Optional<Exam> getExam(UUID uuid);

    Response<Exam> openExam(OpenExam openExam);
}
