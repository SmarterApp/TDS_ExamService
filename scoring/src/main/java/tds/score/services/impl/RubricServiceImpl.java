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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import tds.common.cache.CacheType;
import tds.score.repositories.RubricRepository;
import tds.score.services.RubricService;

import java.io.IOException;

/**
 * This implementation of a RubricService serves rubric content from a repository.
 */
@Service
public class RubricServiceImpl implements RubricService {

    private final RubricRepository rubricRepository;

    @Autowired
    public RubricServiceImpl(final RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    @Override
    @Cacheable(CacheType.LONG_TERM)
    public String findOne(final String rubricPath) throws IOException {
        return rubricRepository.findOne(rubricPath);
    }
}
