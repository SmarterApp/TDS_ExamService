package tds.exam.services;

import java.util.UUID;

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
}
