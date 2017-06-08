package tds.exam.services.scoring;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import tds.assessment.ItemFileMetadata;
import tds.assessment.ItemFileType;
import tds.exam.configuration.ExamServiceProperties;
import tds.score.model.Item;
import tds.score.services.ItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceImplTest {
    @Mock
    private RestTemplate mockRestTemplate;

    private ItemService itemService;
    private static final String URL = "http://localhost:8080/";

    @Before
    public void setUp() {
        ExamServiceProperties properties = new ExamServiceProperties();
        properties.setAssessmentUrl(URL);
        itemService = new ItemServiceImpl(mockRestTemplate, properties);
    }

    @Test
    public void shouldFindStimulusItem() {
        UriComponents uriComponents =
            UriComponentsBuilder
                .fromHttpUrl(URL)
                .path("assessments/item/metadata")
                .queryParam("clientName", "SBAC_PT")
                .queryParam("bankKey", 234)
                .queryParam("stimulusKey", 123)
                .build();

        ItemFileMetadata itemFileMetadata = ItemFileMetadata.create(
            ItemFileType.STIMULUS,
            "stimulus-123",
            "stimulus-123.xml",
            "items/");

        when(mockRestTemplate.getForObject(uriComponents.toUri(), ItemFileMetadata.class)).thenReturn(itemFileMetadata);

        Optional<Item> maybeItem = itemService.findItemByStimulusKey("SBAC_PT", 234, 123);

        assertThat(maybeItem).isPresent();
        assertThat(maybeItem.get().getStimulusPath()).isEqualTo("stimulus-123.xml");
    }

    @Test
    public void shouldFindItem() {
        UriComponents uriComponents =
            UriComponentsBuilder
                .fromHttpUrl(URL)
                .path("assessments/item/metadata")
                .queryParam("clientName", "SBAC_PT")
                .queryParam("bankKey", 234)
                .queryParam("itemKey", 555)
                .build();

        ItemFileMetadata itemFileMetadata = ItemFileMetadata.create(
            ItemFileType.ITEM,
            "item-555",
            "item-155.xml",
            "items/");

        when(mockRestTemplate.getForObject(uriComponents.toUri(), ItemFileMetadata.class)).thenReturn(itemFileMetadata);

        Optional<Item> maybeItem = itemService.findItemByKey("SBAC_PT", 234, 555);

        assertThat(maybeItem).isPresent();
        assertThat(maybeItem.get().getItemPath()).isEqualTo("item-155.xml");
    }
}