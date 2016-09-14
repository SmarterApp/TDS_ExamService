package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.StudentService;
import tds.student.Student;

@Service
class StudentServiceImpl implements StudentService {
    private static final Logger LOG = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public StudentServiceImpl(RestTemplate restTemplate, ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    public Optional<Student> getStudentById(long studentId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s", examServiceProperties.getStudentUrl(), studentId));

        Optional<Student> studentOptional = Optional.empty();
        try {
            final Student student = restTemplate.getForObject(builder.toUriString(), Student.class);
            studentOptional = Optional.of(student);
        } catch (RestClientException rce) {
            LOG.debug("Exception thrown when retrieving session", rce);
        }

        return studentOptional;
    }
}
