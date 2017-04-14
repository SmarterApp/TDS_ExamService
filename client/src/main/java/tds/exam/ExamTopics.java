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
}
