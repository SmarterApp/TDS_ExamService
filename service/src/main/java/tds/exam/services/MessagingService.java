package tds.exam.services;

/**
 * Implementations of this interface are responsible for publishing messages to the ecosystem messaging framework.
 */
public interface MessagingService {

    /**
     * Send an exam completion message to the transmission topic.
     *
     * @param examId The completed exam's id
     */
    void sendExamCompletion(final String examId);
}
