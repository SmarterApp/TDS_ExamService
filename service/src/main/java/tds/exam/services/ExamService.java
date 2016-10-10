package tds.exam.services;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.config.ClientTestProperty;
import tds.exam.Exam;
import tds.exam.OpenExamRequest;

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

    Response<Exam> openExam(OpenExamRequest openExamRequest);

    /**
     * Retrieves the initial ability value for an {@link Exam}.
     *
     * @param exam      the exam to retrieve an ability for.
     * @param clientTestProperty  properties object for the exam.
     * @return  the initial ability for an {@link Exam}.
     */
    Optional<Double> getInitialAbility(Exam exam, ClientTestProperty clientTestProperty);
}
