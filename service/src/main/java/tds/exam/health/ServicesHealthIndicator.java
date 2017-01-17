package tds.exam.health;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import tds.common.health.HealthIndicatorClient;
import tds.exam.configuration.ExamServiceProperties;

import static com.google.common.collect.Maps.transformValues;

/**
 * Returns the health status of this service's dependencies.
 */
@Component
public class ServicesHealthIndicator implements HealthIndicator {
    private final HealthIndicatorClient healthIndicatorClient;
    private final Map<String, String> services;

    private final HealthAggregator healthAggregator = new OrderedHealthAggregator();

    @Autowired
    ServicesHealthIndicator(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        healthIndicatorClient = new HealthIndicatorClient(restTemplate);
        services = ImmutableMap.of(
                "assessment", examServiceProperties.getAssessmentUrl(),
                "student", examServiceProperties.getStudentUrl(),
                "config",  examServiceProperties.getConfigUrl(),
                "session", examServiceProperties.getSessionUrl());
    }

    @Override
    public Health health() {
        return healthAggregator.aggregate(transformValues(services, healthIndicatorClient::health));
    }
}
