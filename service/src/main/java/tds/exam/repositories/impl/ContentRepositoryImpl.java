package tds.exam.repositories.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import tds.exam.configuration.ExamServiceProperties;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.ITSDocument;
import tds.score.repositories.ContentRepository;

@Repository
@Primary
public class ContentRepositoryImpl implements ContentRepository {
    private static final String CONTENT_APP_CONTEXT = "item";
    private final RestTemplate restTemplate;
    private final String contentUrl;

    public ContentRepositoryImpl(final RestTemplate restTemplate, final ExamServiceProperties examServiceProperties) {
        this.restTemplate = restTemplate;
        this.contentUrl = examServiceProperties.getContentUrl();
    }

    @Override
    public IITSDocument getContent(String itemPath, AccLookup accommodations) throws ReturnStatusException {
        final UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromHttpUrl(String.format("%s/%s?itemPath=%s",
                    contentUrl,
                    CONTENT_APP_CONTEXT,
                    itemPath));

        return restTemplate.postForObject(builder.build().toUri(), accommodations, ITSDocument.class);
    }
}
