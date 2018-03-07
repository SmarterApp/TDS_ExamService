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

package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.assessment.AssessmentInfo;
import tds.assessment.AssessmentWindow;
import tds.assessment.SegmentItemInformation;
import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.exam.services.AssessmentService;
import tds.session.ExternalSessionConfiguration;

import static tds.exam.configuration.SupportApplicationConfiguration.ASSESSMENT_APP_CONTEXT;

@Service
class AssessmentServiceImpl implements AssessmentService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public AssessmentServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Optional<Assessment> findAssessment(final String clientName, final String key) {
        URI uri =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/%s",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT,
                    key)).build().toUri();

        Optional<Assessment> maybeAssessment = Optional.empty();
        try {
            final Assessment assessment = restTemplate.getForObject(uri, Assessment.class);
            maybeAssessment = Optional.of(assessment);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeAssessment;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public List<AssessmentWindow> findAssessmentWindows(final String clientName,
                                                        final String assessmentId,
                                                        final boolean guestStudent,
                                                        final ExternalSessionConfiguration configuration) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/%s/windows",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT,
                    assessmentId));

        builder.queryParam("shiftWindowStart", configuration.getShiftWindowStart());
        builder.queryParam("shiftWindowEnd", configuration.getShiftWindowEnd());
        builder.queryParam("shiftFormStart", configuration.getShiftFormStart());
        builder.queryParam("shiftFormEnd", configuration.getShiftFormEnd());
        builder.queryParam("guestStudent", guestStudent);

        ResponseEntity<List<AssessmentWindow>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<AssessmentWindow>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public List<Accommodation> findAssessmentAccommodationsByAssessmentKey(final String clientName, final String assessmentKey) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/accommodations",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT))
                .queryParam("assessmentKey", assessmentKey);

        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public List<Accommodation> findAssessmentAccommodationsByAssessmentId(final String clientName, final String assessmentId) {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s/accommodations",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT))
                .queryParam("assessmentId", assessmentId);


        ResponseEntity<List<Accommodation>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<Accommodation>>() {
            });

        return responseEntity.getBody();
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public Optional<SegmentItemInformation> findSegmentItemInformation(final String segmentKey) {
        URI uri =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/segment-items/%s",
                    examServiceProperties.getAssessmentUrl(),
                    segmentKey)).build().toUri();

        Optional<SegmentItemInformation> maybeSegmentItemInfo = Optional.empty();
        try {
            final SegmentItemInformation segmentItemInformation = restTemplate.getForObject(uri, SegmentItemInformation.class);
            maybeSegmentItemInfo = Optional.of(segmentItemInformation);
        } catch (HttpClientErrorException hce) {
            if (hce.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw hce;
            }
        }

        return maybeSegmentItemInfo;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public List<AssessmentInfo> findAssessmentInfosForAssessments(final String clientName, final String... assessmentKeys) {
        final UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT));

        for (String assessmentKey : assessmentKeys) {
            builder.queryParam("assessmentKeys", assessmentKey);
        }

        final ResponseEntity<List<AssessmentInfo>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<AssessmentInfo>>() {
            });

        return responseEntity.getBody();
    }

    @Override
//    @Cacheable(CacheType.LONG_TERM)
    public List<AssessmentInfo> findAssessmentInfosForGrade(final String clientName, final String grade) {
        final UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s/%s",
                    examServiceProperties.getAssessmentUrl(),
                    clientName,
                    ASSESSMENT_APP_CONTEXT))
                .queryParam("grade", grade);

        final ResponseEntity<List<AssessmentInfo>> responseEntity = restTemplate.exchange(builder.build().toUri(),
            HttpMethod.GET, null, new ParameterizedTypeReference<List<AssessmentInfo>>() {
            });

        return responseEntity.getBody();
    }
}
