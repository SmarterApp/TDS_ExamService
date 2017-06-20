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

package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.repositories.ExamineeQueryRepository;
import tds.exam.services.ExamineeService;
import tds.exam.services.StudentService;
import tds.student.RtsStudentPackageAttribute;
import tds.student.RtsStudentPackageRelationship;
import tds.student.Student;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamineeServiceImplTest {
    @Mock
    private ExamineeCommandRepository examineeCommandRepository;

    @Mock
    private ExamineeQueryRepository examineeQueryRepository;

    @Mock
    private StudentService mockStudentService;

    private ExamineeService examineeService;

    @Before
    public void setUp() {
        examineeService = new ExamineeServiceImpl(examineeCommandRepository, examineeQueryRepository, mockStudentService);
    }

    @Test
    public void shouldInsertAttributesAndRelationships() {
        Exam mockExam = new ExamBuilder()
            .withClientName("SBAC_PT")
            .build();

        RtsStudentPackageAttribute[] mockRtsStudentPackageAttributes = new RtsStudentPackageAttribute[]{
            new RtsStudentPackageAttribute("UnitTestAttribute", "UnitTestAttributeValue"),
            new RtsStudentPackageAttribute("AnotherUnitTestAttribute", "AnotherUnitTestAttributeValue"),
        };
        RtsStudentPackageRelationship[] mockRtsStudentPackageRelationships = new RtsStudentPackageRelationship[]{
            new RtsStudentPackageRelationship("RelationshipId",
                "RelationshipType",
                "RelationshipValue",
                "entityKey"),
            new RtsStudentPackageRelationship("AnotherRelationshipId",
                "AnotherRelationshipType",
                "AnotherRelationshipValue",
                "anotherEntityKey"),
        };

        Student mockStudent = new Student.Builder(1L, "UNIT_TEST_CLIENT")
            .withLoginSSID("UNIT_TEST_SSID")
            .withAttributes(Arrays.asList(mockRtsStudentPackageAttributes))
            .withRelationships(Arrays.asList(mockRtsStudentPackageRelationships))
            .build();

        when(mockStudentService.getStudentById("SBAC_PT", mockExam.getStudentId()))
            .thenReturn(Optional.of(mockStudent));

        examineeService.insertAttributesAndRelationships(mockExam, ExamineeContext.INITIAL);
        verify(mockStudentService).getStudentById("SBAC_PT", mockStudent.getId());
        verify(examineeCommandRepository).insertAttributes((ExamineeAttribute[]) anyVararg());
        verify(examineeCommandRepository).insertRelationships((ExamineeRelationship[]) anyVararg());
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamCanBeFoundButStudentCannot() {
        Exam mockExam = new ExamBuilder()
            .withStudentId(123)
            .withClientName("SBAC_PT")
            .build();

        when(mockStudentService.getStudentById("SBAC_PT", mockExam.getStudentId()))
            .thenReturn(Optional.empty());

        examineeService.insertAttributesAndRelationships(mockExam, ExamineeContext.INITIAL);
        verify(mockStudentService).getStudentById("SBAC_PT", mockExam.getStudentId());
        verifyZeroInteractions(examineeCommandRepository);
    }

    @Test
    public void shouldReturnIfGuestStudent() {
        Exam mockExam = new ExamBuilder()
            .withStudentId(-10)
            .withClientName("SBAC_PT")
            .build();

        examineeService.insertAttributesAndRelationships(mockExam, ExamineeContext.INITIAL);
        verifyZeroInteractions(mockStudentService, examineeCommandRepository);
    }

    @Test
    public void shouldFindExamineeAttributes() {
        final UUID examId = UUID.randomUUID();

        ExamineeAttribute attribute = random(ExamineeAttribute.class);
        when(examineeQueryRepository.findAllAttributes(examId)).thenReturn(Arrays.asList(attribute));
        List<ExamineeAttribute> retAttributes = examineeService.findAllAttributes(examId);
        verify(examineeQueryRepository).findAllAttributes(examId);
        assertThat(retAttributes).containsExactly(attribute);
    }

    @Test
    public void shouldFindExamineeRelationships() {
        final UUID examId = UUID.randomUUID();
        ExamineeRelationship relationship = random(ExamineeRelationship.class);

        when(examineeQueryRepository.findAllRelationships(examId)).thenReturn(Arrays.asList(relationship));
        List<ExamineeRelationship> retAttributes = examineeService.findAllRelationships(examId);
        verify(examineeQueryRepository).findAllRelationships(examId);
        assertThat(retAttributes).containsExactly(relationship);
    }
}
