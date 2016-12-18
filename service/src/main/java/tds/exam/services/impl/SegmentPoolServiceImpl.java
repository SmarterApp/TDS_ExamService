package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Algorithm;
import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.Segment;
import tds.assessment.Strand;
import tds.exam.models.SegmentPoolInfo;
import tds.exam.services.ItemPoolService;
import tds.exam.services.SegmentPoolService;

@Service
public class SegmentPoolServiceImpl implements SegmentPoolService {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentPoolServiceImpl.class);
    private final ItemPoolService itemPoolService;

    @Autowired
    public SegmentPoolServiceImpl(ItemPoolService itemPoolService) {
        this.itemPoolService = itemPoolService;
    }

    @Override
    public SegmentPoolInfo computeSegmentPool(final UUID examId, final Segment segment,
                                              final List<ItemConstraint> itemConstraints, String languageCode) {
        // Get the list of eligible items based on constraints and exam accommodations
        Set<Item> itemPool = itemPoolService.getItemPool(examId, itemConstraints, segment.getItems(languageCode));
        /* getItemPool selects the items that are eligible for the segment pool we are constructing.
           In legacy code, we can skip a lot of the temp-table initialization logic because of this */
        Set<Strand> strands = segment.getStrands();
        int shortfall = 0;
        int strandCount = 0;
        // This map wil be used to cache values for strandcounts
        Map<String, Integer> strandCountMap = new HashMap<>();

        for (Item item : itemPool) {
            // Find items with matching strands and non-null adaptiveCut value
            Optional<Strand> maybeItemStrand = strands.stream()
                    .filter(strand ->
                            strand.getName().equals(item.getStrand()) &&
                            strand.getAdaptiveCut() != null)
                    .findFirst();

            if (maybeItemStrand.isPresent()) {
                Strand itemStrand = maybeItemStrand.get();
                // Get the count of (non-field test) items that have the same strand value and cache it
                int poolCount;

                if (strandCountMap.containsKey(itemStrand.getName())) {
                    poolCount = strandCountMap.get(itemStrand.getName());
                } else {
                    poolCount = (int) itemPool.stream()
                        .filter(innerItem ->
                            !innerItem.isFieldTest() &&
                            innerItem.getStrand().equals(itemStrand.getName()))
                        .count();
                    strandCountMap.put(itemStrand.getName(), poolCount);
                }

                strandCount += poolCount;

                if (poolCount < itemStrand.getMinItems()) {
                    shortfall += (itemStrand.getMinItems() - poolCount);
                }
            } else {
                LOG.warn("No strand match for item with id '{}' and strand '{}'. Unable to add to segment pool computation",
                        item.getId(), item.getStrand());
            }
        }

        /* [2887,2914]: sessionKey is null only when we are not in simulation mode. See line 4622.
            TODO: Skip the conditional branch of code [2914-2938] until simulation mode is implemented  */

        int lengthDelta = segment.getMaxItems() - shortfall;
        int newLength;
        if (lengthDelta < strandCount) {
            if (lengthDelta > 0) {
                newLength = lengthDelta;
            } else {
                newLength = segment.getMaxItems();
            }
        } else {
            newLength = strandCount;
        }

        return new SegmentPoolInfo(newLength, strandCount, itemPool);
    }
}
