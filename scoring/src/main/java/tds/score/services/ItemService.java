package tds.score.services;

import java.util.Optional;

import tds.score.model.Item;

/**
 * Service to handle item interaction
 */
public interface ItemService {
    /**
     * Find the item by bank and stimulus key
     *
     * @param bankKey     the bank key
     * @param stimulusKey the stimulus key
     * @return {@link tds.score.model.Item} if found otherwise empty
     */
    Optional<Item> findItemByStimulusKey(final String clientName, final long bankKey, final long stimulusKey);

    /**
     * Find the item by bank and item key
     *
     * @param bankKey the bank key
     * @param itemKey the item key
     * @return {@link tds.score.model.Item} if found otherwise empty
     */
    Optional<Item> findItemByKey(final String clientName, final long bankKey, final long itemKey);
}
