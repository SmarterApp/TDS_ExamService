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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.itemrenderer.processing.RendererSpecService;
import tds.score.repositories.RendererSpecRepository;

import java.io.IOException;

/**
 * This implementation of an RendererSpecService serves renderer spec content from a repository.
 */
@Service
public class RendererSpecServiceImpl implements RendererSpecService {

    private final RendererSpecRepository repository;

    @Autowired
    public RendererSpecServiceImpl(final RendererSpecRepository repository) {
        this.repository = repository;
    }

    @Override
    public String findOne(final String rendererSpecPath) throws IOException {
        return repository.findOne(rendererSpecPath);
    }
}
