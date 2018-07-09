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

package tds.exam.services;

import java.util.UUID;

import tds.trt.model.TDSReport;

/**
 * Implementations of this interface are responsible for publishing messages to the ecosystem messaging framework.
 */
public interface MessagingService {

    /**
     * Send an exam completion message to the transmission topic.
     *
     * @param examId The completed exam's id
     */
    void sendExamCompletion(final UUID examId);


    /**
     * Sends an exam rescore message to the transmission topic
     *
     * @param examId      The id of the exam to rescore
     * @param testResults The TRT of the exam to rescore
     */
    void sendExamRescore(final UUID examId, final UUID jobId, final TDSReport testResults);
}
