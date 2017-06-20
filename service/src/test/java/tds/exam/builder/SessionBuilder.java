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

package tds.exam.builder;

import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.Minutes;

import java.util.UUID;

import tds.session.Session;

public class SessionBuilder {
    private UUID id = UUID.randomUUID();
    private String sessionKey = "ADM-23";
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
