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
import tds.itemrenderer.processing.ItemDataService;
import tds.score.repositories.ItemDataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This implementation of an ItemDataService serves exam item data content from a repository.
 */
@Service
public class ItemDataServiceImpl implements ItemDataService {

    private final ItemDataRepository itemDataRepository;

    @Autowired
    public ItemDataServiceImpl(final ItemDataRepository repository) {
        this.itemDataRepository = repository;
    }

    @Override
    public String readData(final URI itemPath) throws IOException {
        return itemDataRepository.findOne(itemPath.toASCIIString());
    }

    @Override
    public InputStream readResourceData(final URI uri) throws IOException {
        return null;
    }

    @Override
    public boolean dataExists(final URI uri) throws IOException {
        return false;
    }
}
