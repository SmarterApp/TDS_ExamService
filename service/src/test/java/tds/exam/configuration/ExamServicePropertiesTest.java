package tds.exam.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamServicePropertiesTest {
    private ExamServiceProperties properties;

    @Before
    public void setUp() {
        properties = new ExamServiceProperties();
    }

    @After
    public void tearDown() {}

    @Test
    public void itShouldAppendSlashIfNotPresentForSessionUrl(){
        properties.setSessionUrl("http://localhost:8080/sessions");
        assertThat(properties.getSessionUrl()).isEqualTo("http://localhost:8080/sessions");

        properties.setSessionUrl("http://localhost:8080/sessions/");
        assertThat(properties.getSessionUrl()).isEqualTo("http://localhost:8080/sessions");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAllowNullSesssionUrl() {
        properties.setSessionUrl(null);
    }

    @Test
    public void itShouldAppendSlashIfNotPresentForStudentUrl() {
        properties.setStudentUrl("http://localhost:8080/students");
        assertThat(properties.getStudentUrl()).isEqualTo("http://localhost:8080/students");

        properties.setSessionUrl("http://localhost:8080/students/");
        assertThat(properties.getStudentUrl()).isEqualTo("http://localhost:8080/students");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAllowNullStudentUrl() {
        properties.setStudentUrl(null);
    }
}
