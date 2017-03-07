package tds.score.services.impl;

import java.util.Optional;

import tds.score.model.Item;

public interface ItemService {
    Optional<Item> findItem(final long bankKey, final long itemKey, final long stimulusKey);
}
