package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.StudentService;
import tds.student.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StudentServiceImplTest {
    private RestTemplate restTemplate;
    private StudentService studentService;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
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
}
