package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.services.ExamService;
import tds.exam.services.ExamineeService;
import tds.exam.services.StudentService;
import tds.student.RtsStudentPackageAttribute;
import tds.student.RtsStudentPackageRelationship;
import tds.student.Student;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamineeServiceImplTest {
    @Mock
    private ExamineeCommandRepository examineeCommandRepository;

    @Mock
    private StudentService studentService;

    private ExamineeService examineeService;

    @Before
    public void setUp() {
        examineeService = new ExamineeServiceImpl(examineeCommandRepository, studentService);
    }

    @Test
    public void shouldInsertAttributesAndRelationships() {
        Exam mockExam = new ExamBuilder().build();

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

        when(studentService.getStudentById(mockExam.getStudentId()))
            .thenReturn(Optional.of(mockStudent));

        examineeService.insertAttributesAndRelationships(mockExam, ExamineeContext.INITIAL);
        verify(studentService).getStudentById(mockStudent.getId());
        verify(examineeCommandRepository).insertAttributes((ExamineeAttribute[]) anyVararg());
        verify(examineeCommandRepository).insertRelationships((ExamineeRelationship[]) anyVararg());
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamCanBeFoundButStudentCannot() {
        Exam mockExam = new ExamBuilder().build();

        when(studentService.getStudentById(any(Long.class)))
            .thenReturn(Optional.empty());

        examineeService.insertAttributesAndRelationships(mockExam, ExamineeContext.INITIAL);
        verify(studentService).getStudentById(any(Long.class));
        verifyZeroInteractions(examineeCommandRepository);
    }
}
