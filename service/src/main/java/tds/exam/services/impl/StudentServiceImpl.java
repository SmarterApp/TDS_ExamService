package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.StudentService;
import tds.student.RtsStudentPackageAttribute;
import tds.student.Student;

import static tds.exam.configuration.SupportApplicationConfiguration.STUDENT_APP_CONTEXT;

@Service
class StudentServiceImpl implements StudentService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public StudentServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    @Cacheable(CacheType.MEDIUM_TERM)
    public Optional<Student> getStudentById(final String clientName, final long studentId) {
        URI uri =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/%s", examServiceProperties.getStudentUrl(), clientName, STUDENT_APP_CONTEXT, studentId))
                .build()
                .toUri();

        Optional<Student> maybeStudent = Optional.empty();
        try {
            final Student student = restTemplate.getForObject(uri, Student.class);
            maybeStudent = Optional.of(student);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeStudent;
    }

    @Override
    @Cacheable(CacheType.MEDIUM_TERM)
    public List<RtsStudentPackageAttribute> findStudentPackageAttributes(final long studentId,
                                                                         final String clientName,
                                                                         final String... attributeNames) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/rts/%s/attributes=%s",
                    examServiceProperties.getStudentUrl(),
                    STUDENT_APP_CONTEXT,
                    studentId,
                    clientName,
                    String.join(",", (CharSequence[]) attributeNames))
                );

        ResponseEntity<List<RtsStudentPackageAttribute>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<RtsStudentPackageAttribute>>() {
            });

        return responseEntity.getBody();
    }
}
