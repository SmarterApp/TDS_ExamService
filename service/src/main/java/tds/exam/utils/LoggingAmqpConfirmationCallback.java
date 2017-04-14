package tds.exam.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * This confirmation callback is responsible for logging all unacknowledged
 * RabbitTemplate message submissions.
 */
public class LoggingAmqpConfirmationCallback implements RabbitTemplate.ConfirmCallback {
    private final static Logger LOG = LoggerFactory.getLogger(LoggingAmqpConfirmationCallback.class);

    @Override
    public void confirm(final CorrelationData correlationData,
                        final boolean ack,
                        final String cause) {
        if (ack) {
            LOG.debug("Message acknowledged as sent: {}", correlationData);
        } else {
            LOG.error("Message not acknowledged as sent: {} cause: {}", correlationData, cause);
        }
    }
}
