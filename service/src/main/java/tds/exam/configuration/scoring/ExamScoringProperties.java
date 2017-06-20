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

package tds.exam.configuration.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import tds.score.configuration.ItemScoreSettings;

/**
 * Contains the properties associated with the scoring engine
 */
@Component
@ConfigurationProperties(prefix = "exam.scoring.engine")
public class ExamScoringProperties extends ItemScoreSettings {
    private int queueThreadCount;
    private int queueHiWaterMark;
    private int queueLowWaterMark;
    private String pythonScoringUrl;
    private int maxAttempts;
    private int sympyTimeoutMillis;

    public int getQueueThreadCount() {
        return queueThreadCount;
    }

    public void setQueueThreadCount(final int queueThreadCount) {
        this.queueThreadCount = queueThreadCount;
    }

    public int getQueueHiWaterMark() {
        return queueHiWaterMark;
    }

    public void setQueueHiWaterMark(final int queueHiWaterMark) {
        this.queueHiWaterMark = queueHiWaterMark;
    }

    public int getQueueLowWaterMark() {
        return queueLowWaterMark;
    }

    public void setQueueLowWaterMark(final int queueLowWaterMark) {
        this.queueLowWaterMark = queueLowWaterMark;
    }

    public String getPythonScoringUrl() {
        return pythonScoringUrl;
    }

    public void setPythonScoringUrl(final String pythonScoringUrl) {
        this.pythonScoringUrl = pythonScoringUrl;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getSympyTimeoutMillis() {
        return sympyTimeoutMillis;
    }

    public void setSympyTimeoutMillis(final int sympyTimeoutMillis) {
        this.sympyTimeoutMillis = sympyTimeoutMillis;
    }
}
