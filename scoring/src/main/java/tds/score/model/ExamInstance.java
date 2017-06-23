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

package tds.score.model;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ExamInstance {
    public abstract UUID getExamId();
    public abstract UUID getSessionId();
    public abstract UUID getBrowserId();
    public abstract String getClientName();

    public static ExamInstance create(final UUID newExamId, final UUID newSessionId, final UUID newBrowserId, final String newClientName) {
        return new AutoValue_ExamInstance(newExamId, newSessionId, newBrowserId, newClientName);
    }
}
