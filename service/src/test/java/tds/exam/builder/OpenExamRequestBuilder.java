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

import java.util.UUID;

import tds.exam.OpenExamRequest;

public class OpenExamRequestBuilder {
    private long studentId = 1;
    private String assessmentKey = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private int maxAttempts = 0;
    private UUID sessionId = UUID.randomUUID();
    private String guestAccommodations;
    private UUID browserId = UUID.randomUUID();

    public OpenExamRequestBuilder withStudentId(long studentId) {
        this.studentId = studentId;
        return this;
    }

    public OpenExamRequestBuilder withAssessmentKey(String assessmentKey) {
        this.assessmentKey = assessmentKey;
        return this;
    }

    public OpenExamRequestBuilder withMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public OpenExamRequestBuilder withSessionId(UUID sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public OpenExamRequestBuilder withGuestAccommodations(String guestAccommodations) {
        this.guestAccommodations = guestAccommodations;
        return this;
    }

    public OpenExamRequestBuilder withBrowserId(UUID browserId) {
        this.browserId = browserId;
        return this;
    }

    public OpenExamRequest build() {
        return new OpenExamRequest.Builder()
            .withStudentId(studentId)
            .withAssessmentKey(assessmentKey)
            .withSessionId(sessionId)
            .withGuestAccommodations(guestAccommodations)
            .withBrowserId(browserId)
            .build();
    }
}
