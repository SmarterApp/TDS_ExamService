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

package tds.exam;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Request approval for {@link tds.exam.ExamAccommodation}s
 */
public class ApproveAccommodationsRequest {
    private UUID sessionId;
    private UUID browserId;
    // Segment position -> Accommodation codes
    private Map<Integer, Set<String>> accommodationCodes;
    private boolean isGuest;

    /*
        Private constructor for frameworks
     */
    private ApproveAccommodationsRequest() {}

    public ApproveAccommodationsRequest(UUID sessionId, UUID browserId, Map<Integer, Set<String>> accommodationCodes) {
        this.sessionId = sessionId;
        this.browserId = browserId;
        this.accommodationCodes = accommodationCodes;
        this.isGuest = false;
    }

    public ApproveAccommodationsRequest(UUID sessionId, UUID browserId, boolean isGuest, Map<Integer, Set<String>> accommodationCodes) {
      this.sessionId = sessionId;
      this.browserId = browserId;
      this.accommodationCodes = accommodationCodes;
      this.isGuest = isGuest;
    }
  
    /**
     * @return the session id of the {@link tds.exam.ApproveAccommodationsRequest}
     */
    public UUID getSessionId() {
      return sessionId;
    }
  
    /**
     * @return the browser id of the {@link tds.exam.ApproveAccommodationsRequest}
     */
    public UUID getBrowserId() {
      return browserId;
    }
  
    /**
     * @return a mapping of the accomodation codes to their respective segment position
     */
    public Map<Integer, Set<String>> getAccommodationCodes() {
      return accommodationCodes;
    }

    public boolean isGuest() {
        return isGuest;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      
      ApproveAccommodationsRequest that = (ApproveAccommodationsRequest) o;
      
      if (!sessionId.equals(that.sessionId)) return false;
      if (!browserId.equals(that.browserId)) return false;
      return accommodationCodes.equals(that.accommodationCodes);
    }
    
    @Override
    public int hashCode() {
      int result = sessionId.hashCode();
      result = 31 * result + browserId.hashCode();
      result = 31 * result + accommodationCodes.hashCode();
      return result;
    }
}
