package tds.score.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.score.repositories.RubricRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RubricServiceImplTest {

    @Mock
    private RubricRepository mockRepository;

    private RubricServiceImpl service;

    @Before
    public void setup() {
        service = new RubricServiceImpl(mockRepository);
    }

    @Test
    public void itShouldUseRepositoryToFindRubric() throws Exception {
        final String rubricPath = "rubric/path.xml";
        final String rubricContent = "rubric content";
        when(mockRepository.findOne(rubricPath)).thenReturn(rubricContent);

        assertThat(service.findOne(rubricPath)).isEqualTo(rubricContent);
    }

}