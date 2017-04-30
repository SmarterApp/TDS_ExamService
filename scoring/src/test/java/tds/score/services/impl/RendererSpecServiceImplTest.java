package tds.score.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.score.repositories.RendererSpecRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RendererSpecServiceImplTest {

    @Mock
    private RendererSpecRepository mockRepository;

    private RendererSpecServiceImpl service;

    @Before
    public void setup() {
        service = new RendererSpecServiceImpl(mockRepository);
    }

    @Test
    public void itShouldUseRepositoryToFindRendererSpec() throws Exception {
        final String rendererSpecPath = "renderer/spec/path.xml";
        final String rendererSpecContent = "renderer spec content";
        when(mockRepository.findOne(rendererSpecPath)).thenReturn(rendererSpecContent);

        assertThat(service.findOne(rendererSpecPath)).isEqualTo(rendererSpecContent);
    }
}