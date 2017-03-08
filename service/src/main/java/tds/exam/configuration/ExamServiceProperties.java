package tds.exam.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "exam-service")
public class ExamServiceProperties {
    private String sessionUrl = "";
    private String studentUrl = "";
    private String assessmentUrl = "";
    private String configUrl = "";

    /**
     * Get the URL for the config microservice.
     *
     * @return config microservice URL
     */
    public String getConfigUrl() {
        return configUrl;
    }

    /**
     * @param configUrl not null config url without trailing slash
     */
    public void setConfigUrl(String configUrl) {
        if (configUrl == null) throw new IllegalArgumentException("configUrl cannot be null");
        this.configUrl = removeTrailingSlash(configUrl);
    }

    /**
     * Get the URL for the session microservice.
     *
     * @return session microservice URL
     */
    public String getSessionUrl() {
        return sessionUrl;
    }

    /**
     * @param sessionUrl not null student url
     */
    public void setSessionUrl(String sessionUrl) {
        if (sessionUrl == null) throw new IllegalArgumentException("sessionUrl cannot be null");
        this.sessionUrl = removeTrailingSlash(sessionUrl);
    }

    /**
     * Get the URL for the student microservice.
     *
     * @return student microservice URL
     */
    public String getStudentUrl() {
        return studentUrl;
    }

    public void setStudentUrl(String studentUrl) {
        if (studentUrl == null) throw new IllegalArgumentException("studentUrl cannot be null");
        this.studentUrl = removeTrailingSlash(studentUrl);
    }

    /**
     * Get the URL for the assessment microservice.
     *
     * @return assessment microservice URL
     */
    public String getAssessmentUrl() {
        return assessmentUrl;
    }

    public void setAssessmentUrl(String assessmentUrl) {
        if (studentUrl == null) throw new IllegalArgumentException("asssessmentUrl cannot be null");
        this.assessmentUrl = removeTrailingSlash(assessmentUrl);
    }

    private String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        } else {
            return url;
        }
    }
}
