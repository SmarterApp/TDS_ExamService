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
    
    /*
        Private constructor for frameworks
     */
    private ApproveAccommodationsRequest() {}
    
    public ApproveAccommodationsRequest(UUID sessionId, UUID browserId, Map<Integer, Set<String>> accommodationCodes) {
      this.sessionId = sessionId;
      this.browserId = browserId;
      this.accommodationCodes = accommodationCodes;
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
