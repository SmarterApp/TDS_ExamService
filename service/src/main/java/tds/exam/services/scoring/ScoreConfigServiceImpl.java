package tds.exam.services.scoring;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tds.common.cache.CacheType;
import tds.exam.configuration.ExamServiceProperties;
import tds.score.services.ScoreConfigService;
import tds.student.sql.data.ItemScoringConfig;

import java.util.List;

@Service
public class ScoreConfigServiceImpl implements ScoreConfigService {
    private final RestTemplate restTemplate;
    private final ExamServiceProperties examServiceProperties;

    @Autowired
    public ScoreConfigServiceImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.examServiceProperties = examServiceProperties;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public List<ItemScoringConfig> findItemScoreConfigs(final String clientName) throws ReturnStatusException {
        UriComponents uriComponents =
            UriComponentsBuilder
                .fromHttpUrl(examServiceProperties.getConfigUrl())
                .path("config/{clientName}/scoring")
                .buildAndExpand(clientName);

        try {
            ResponseEntity<List<ItemScoringConfig>> responseEntity = restTemplate.exchange(uriComponents.toUri(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ItemScoringConfig>>() {
                });

            return responseEntity.getBody();
        } catch (RestClientException rce) {
            throw new ReturnStatusException(rce);
        }
    }
}
