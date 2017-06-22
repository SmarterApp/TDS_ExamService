/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
     * Retrieves the student by the student id and client
     *
     * @param clientName client name for the student
     * @param studentId id for the student
     * @return populated optional with student otherwise empty
     */
    Optional<Student> getStudentById(final String clientName, final long studentId);

    /**
     * Finds the student package attributes from the student endpoint
     *
     * @param studentId      the student id
     * @param clientName     the client name
     * @param attributeNames the attribute names to use to fetch the attributes values from the package
     * @return list containing any {@link tds.student.RtsStudentPackageAttribute} corresponding to the attribute names
     */
    List<RtsStudentPackageAttribute> findStudentPackageAttributes(final long studentId, final String clientName, final String... attributeNames);
}
