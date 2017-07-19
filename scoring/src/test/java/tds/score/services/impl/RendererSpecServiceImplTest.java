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

//    private RendererSpecServiceImpl service;

//    @Before
//    public void setup() {
//        service = new RendererSpecServiceImpl(mockRepository);
//    }
//
//    @Test
//    public void itShouldUseRepositoryToFindRendererSpec() throws Exception {
//        final String rendererSpecPath = "renderer/spec/path.xml";
//        final String rendererSpecContent = "renderer spec content";
//        when(mockRepository.findOne(rendererSpecPath)).thenReturn(rendererSpecContent);
//
//        assertThat(service.findOne(rendererSpecPath)).isEqualTo(rendererSpecContent);
//    }
}