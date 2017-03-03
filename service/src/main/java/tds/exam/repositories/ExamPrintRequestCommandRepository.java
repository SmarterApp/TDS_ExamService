package tds.exam.repositories;

import tds.exam.ExamPrintRequest;

/**
 * A repository for writing {@link tds.exam.ExamPrintRequest} data
 */
public interface ExamPrintRequestCommandRepository {

    /**
     * Creates a request to print an exam
     *
     * @param examPrintRequest
     */
    void insert(final ExamPrintRequest examPrintRequest);
}
