package tds.score.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.score.repositories.ItemDataRepository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemDataServiceImplTest {

    @Mock
    private ItemDataRepository mockRepository;

    private ItemDataServiceImpl service;

    @Before
    public void setup() {
        service = new ItemDataServiceImpl(mockRepository);
    }

    @Test
    public void itShouldUseRepositoryToFindItemData() throws Exception {
        final String itemPath = "item/path.xml";
        final String itemContent = "item content";
        when(mockRepository.findOne(itemPath)).thenReturn(itemContent);

        assertThat(service.readData(URI.create(itemPath))).isEqualTo(itemContent);
    }

}