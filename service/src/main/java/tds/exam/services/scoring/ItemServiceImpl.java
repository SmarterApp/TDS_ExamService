package tds.exam.services.scoring;

import java.util.Optional;

import tds.score.model.Item;
import tds.score.services.ItemService;

public class ItemServiceImpl implements ItemService {
    @Override
    public Optional<Item> findItemByStimulusKey(final long bankKey, final long stimulusKey) {
        return null;
    }

    @Override
    public Optional<Item> findItemByKey(final long bankKey, final long itemKey) {
        return null;
    }
}
