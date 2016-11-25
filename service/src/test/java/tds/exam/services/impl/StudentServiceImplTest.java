package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.StudentService;
import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

@RunWith(MockitoJUnitRunner.class)
public class StudentServiceImplTest {
    @Mock
    private RestTemplate restTemplate;
    private StudentService studentService;

    @Before
    public void setUp() {
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setStudentUrl("http://localhost:8080/students");
        studentService = new StudentServiceImpl(restTemplate, properties);
    }

    @Test
    public void shouldFindStudentById() {
        Student student = new Student(1, "testId", "CA", "clientName");

        when(restTemplate.getForObject("http://localhost:8080/students/1", Student.class)).thenReturn(student);
        Optional<Student> maybeStudent = studentService.getStudentById(1);
        verify(restTemplate).getForObject("http://localhost:8080/students/1", Student.class);

        assertThat(maybeStudent.get()).isEqualTo(student);
    }

    @Test
    public void shouldReturnEmptyWhenStudentNotFound() {
        when(restTemplate.getForObject("http://localhost:8080/students/1", Student.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        Optional<Student> maybeStudent = studentService.getStudentById(1);
        verify(restTemplate).getForObject("http://localhost:8080/students/1", Student.class);

        assertThat(maybeStudent).isNotPresent();
    }

    @Test (expected = RestClientException.class)
    public void shouldThrowIfStatusNotNotFoundWhenFindingStudentById() {
        when(restTemplate.getForObject("http://localhost:8080/students/1", Student.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        studentService.getStudentById(1);;
    }

    @Test
    public void shouldFindAttributes() {
        RtsStudentPackageAttribute attribute = new RtsStudentPackageAttribute("test1", "test1Val");
        RtsStudentPackageAttribute attribute2 = new RtsStudentPackageAttribute("test2", "test2Val");
        List<RtsStudentPackageAttribute> attributes = Arrays.asList(attribute, attribute2);
        ResponseEntity<List<RtsStudentPackageAttribute>> entity = new ResponseEntity<>(attributes, HttpStatus.OK);

        when(restTemplate.exchange("http://localhost:8080/students/1/rts/SBAC_PT/attributes=test1,test2", GET, null, new ParameterizedTypeReference<List<RtsStudentPackageAttribute>>() {}))
            .thenReturn(entity);

        List<RtsStudentPackageAttribute> foundAttributes = studentService.findStudentPackageAttributes(1, "SBAC_PT", "test1", "test2");

        assertThat(foundAttributes).containsExactly(attribute, attribute2);
    }
}
