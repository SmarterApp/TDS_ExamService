package tds.exam.services;

import java.util.List;
import java.util.Optional;

import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

/**
 * handles student operations
 */
public interface StudentService {
    /**
     * Retrieves the student by the student id
     *
     * @param studentId id for the student
     * @return populated optional with student otherwise empty
     */
    Optional<Student> getStudentById(long studentId);

    /**
     * Finds the student package attributes from the student endpoint
     *
     * @param studentId      the student id
     * @param clientName     the client name
     * @param attributeNames the attribute names to use to fetch the attributes values from the package
     * @return list containing any {@link tds.student.RtsStudentPackageAttribute} corresponding to the attribute names
     */
    List<RtsStudentPackageAttribute> findStudentPackageAttributes(long studentId, String clientName, String... attributeNames);
}
