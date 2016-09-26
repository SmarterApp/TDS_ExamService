package tds.exam.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "exam-service")
public class ExamServiceProperties {
    private String sessionUrl = "";
    private String studentUrl = "";
    private String assessmentUrl = "";

    /**
     * Get the URL for the session microservice.  Will always end with "/" unless blank
     *
     * @return session microservice URL
     */
    public String getSessionUrl() {
        return sessionUrl.endsWith("/")
            ? sessionUrl
            : sessionUrl + "/";
    }

    /**
     * @param sessionUrl not null student url
     */
    public void setSessionUrl(String sessionUrl) {
        if (sessionUrl == null) throw new IllegalArgumentException("sessionUrl cannot be null");

        this.sessionUrl = sessionUrl;
    }

    /**
     * Get the URL for the student microservice.  Will always end with "/" unless blank
     *
     * @return student microservice URL
     */
    public String getStudentUrl() {
        return studentUrl.endsWith("/")
            ? studentUrl
            : studentUrl + "/";
    }

    public void setStudentUrl(String studentUrl) {
        if (studentUrl == null) throw new IllegalArgumentException("studentUrl cannot be null");

        this.studentUrl = studentUrl;
    }

    /**
     * Get the URL for the assessment microservice.  Will always end with "/" unless blank
     *
     * @return assessment microservice URL
     */
    public String getAssessmentUrl() {
        return assessmentUrl.endsWith("/")
            ? assessmentUrl
            : assessmentUrl + "/";
    }

    public void setAssessmentUrl(String assessmentUrl) {
        if (studentUrl == null) throw new IllegalArgumentException("asssessmentUrl cannot be null");
        this.assessmentUrl = assessmentUrl;
    }
}
