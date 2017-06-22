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
