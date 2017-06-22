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

import java.util.UUID;

/**
 * A request to wait for a segment approval
 */
public class SegmentApprovalRequest {
    private UUID sessionId;
    private UUID browserId;
    private int segmentPosition;
    private boolean entryApproval;

    private SegmentApprovalRequest() {
    }

    public SegmentApprovalRequest(final UUID sessionId, final UUID browserId, final int segmentPosition, final boolean entryApproval) {
      this.sessionId = sessionId;
      this.browserId = browserId;
      this.segmentPosition = segmentPosition;
      this.entryApproval = entryApproval;
    }

    /**
     * @return The session id of the exam requesting segment approval
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The browser id of the exam requesting segment approval
     */
    public UUID getBrowserId() {
        return browserId;
    }

    /**
     * @return The position of the exam segment to seek entry/exit approval for
     */
    public int getSegmentPosition() {
        return segmentPosition;
    }

    /**
     * @return If {@code true}, this is an entry approval request. If {@code false} this is an exit segment approval request
     */
    public boolean isEntryApproval() {
        return entryApproval;
    }
}
