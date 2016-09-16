package tds.exam.services;

import java.util.Optional;

import tds.student.Student;

/**
 * handles student operations
 */
public interface StudentService {
    /**
     * Retrieves the student by the student id
     * @param studentId id for the student
     * @return populated optional with student otherwise empty
     */
    Optional<Student> getStudentById(long studentId);
}
