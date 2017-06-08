package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.student.Student;

/**
 * A service for interacting with {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s
 */
public interface ExamineeService {
    /**
     * Fetches a list of {@link tds.exam.ExamineeAttribute}s for the given examId
     *
     * @param examId          The id of the {@link tds.exam.Exam} for which to fetch attributes for
     * @return The list of attributes for the given exam and context
     */
    List<ExamineeAttribute> findAllAttributes(final UUID examId);

    /**
     * Fetches a list of {@link tds.exam.ExamineeRelationship}s for the given examId
     *
     * @param examId          The id of the {@link tds.exam.Exam} for which to fetch relationships for
     * @return The list of attributes for the given exam and context
     */
    List<ExamineeRelationship> findAllRelationships(final UUID examId);

    /**
     * Insert the {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s for this
     * {@link tds.exam.Exam} for the specified {@link tds.exam.ExamineeContext}.
     *
     * @param exam    The {@link tds.exam.Exam} for which attributes and relationships are being stored
     * @param context The {@link tds.exam.ExamineeContext} that describes the state of the examniee's information when
     *                these attributes and relationships were collected
     */
    void insertAttributesAndRelationships(final Exam exam, final ExamineeContext context);

    /**
     * Insert the {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s for this
     * {@link tds.exam.Exam} for the specified {@link tds.exam.ExamineeContext}.
     *
     * @param exam    The {@link tds.exam.Exam} for which attributes and relationships are being stored
     * @param student the {@link tds.student.Student} with the attributes and relationships
     * @param context The {@link tds.exam.ExamineeContext} that describes the state of the examniee's information when
     *                these attributes and relationships were collected
     */
    void insertAttributesAndRelationships(final Exam exam, final Student student, final ExamineeContext context);
}
