package tds.exam.builder;

import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.Minutes;

import java.util.UUID;

import tds.session.Session;

public class SessionBuilder {
    private UUID id = UUID.randomUUID();
    private String sessionKey = "ADM-23";
    private int type = 0;
    private String status = "open";
    private Instant dateBegin = Instant.now().minus(Minutes.minutes(20).toStandardDuration());
    private Instant dateEnd = Instant.now().plus(Hours.EIGHT.toStandardDuration());
    private Instant dateChanged;
    private Instant dateVisited;
    private String clientName = "SBAC_PT";
    private Long proctorId = 1L;
    private UUID browserKey = UUID.randomUUID();

    public Session build() {
        return new Session.Builder()
            .withId(id)
            .withSessionKey(sessionKey)
            .withType(type)
            .withStatus(status)
            .withDateBegin(dateBegin)
            .withDateEnd(dateEnd)
            .withDateVisited(dateVisited)
            .withDateChanged(dateChanged)
            .withClientName(clientName)
            .withProctorId(proctorId)
            .withBrowserKey(browserKey)
            .build();
    }

    public SessionBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public SessionBuilder withSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
        return this;
    }

    public SessionBuilder withType(int type) {
        this.type = type;
        return this;
    }

    public SessionBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public SessionBuilder withDateBegin(Instant instant) {
        this.dateBegin = instant;
        return this;
    }

    public SessionBuilder withDateEnd(Instant instant) {
        this.dateEnd = instant;
        return this;
    }

    public SessionBuilder withDateChanged(Instant instant) {
        this.dateChanged = instant;
        return this;
    }

    public SessionBuilder withDateVisited(Instant instant) {
        this.dateVisited = instant;
        return this;
    }

    public SessionBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public SessionBuilder withProctorId(Long proctorId) {
        this.proctorId = proctorId;
        return this;
    }

    public SessionBuilder wtihBrowserKey(UUID browserKey) {
        this.browserKey = browserKey;
        return this;
    }
}
