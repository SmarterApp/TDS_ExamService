package tds.exam.services;

import tds.exam.Exam;
import tds.exam.ExamineeContext;
import tds.student.Student;

/**
 * A service for interacting with {@link tds.exam.ExamineeAttribute}s and {@link tds.exam.ExamineeRelationship}s
 */
public interface ExamineeService {
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
