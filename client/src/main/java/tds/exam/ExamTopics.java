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

/**
 * This class holds AMQP topics used by the ExamService.
 * Topic names are in the general for of "{application}.{scope}.{operation}"
 */
public class ExamTopics {

    /**
     * This is the topic exchange for all exam topics.
     */
    public static final String TOPIC_EXCHANGE = "tds.exam.exchange";

    /**
     * This topic is published to when an exam is completed.
     * The message body is a String containing the exam id.
     */
    public static final String TOPIC_EXAM_COMPLETED = "exam.completed";

    /**
     * This topic is published to when the ExamResultsTransmitter receives a response from the
     * Test Integration System acknowledging that it has successfully processed a TRT request.
     */
    public static final String TOPIC_EXAM_REPORTED = "exam.reported";
}
