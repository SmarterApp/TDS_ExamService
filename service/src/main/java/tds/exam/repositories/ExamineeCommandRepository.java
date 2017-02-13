package tds.exam.repositories;

import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeRelationship;

/**
 * Handles data modification for the examinee_attribute and examinee_relationshiptables
 */
public interface ExamineeCommandRepository {
    /**
     * Insert a collection of {@link tds.exam.ExamineeAttribute}s into the database.
     *
     * @param attributes One or more {@link tds.exam.ExamineeAttribute}s to insert
     */
    void insertAttributes(ExamineeAttribute... attributes);

    /**
     * Insert a collection of {@link tds.exam.ExamineeRelationship}s into the database.
     *
     * @param relationships One or more {@link tds.exam.ExamineeRelationship}s to insert
     */
    void insertRelationships(ExamineeRelationship... relationships);
}
