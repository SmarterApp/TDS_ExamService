package tds.score.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.itemrenderer.processing.ItemDataService;
import tds.score.repositories.ItemDataRepository;

import java.io.IOException;
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
}
