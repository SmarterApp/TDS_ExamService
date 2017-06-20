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
