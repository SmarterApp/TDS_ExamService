package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import tds.assessment.Assessment;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.services.FieldTestItemGroupSelector;
import tds.exam.services.ItemPoolService;

@Component
public class EqualDistributionFieldTestItemGroupSelector implements FieldTestItemGroupSelector {
    private final ItemPoolService itemPoolService;
    // Keeps track of the field test item groups and their usages. The key is the segment key.
    private static Map<String, List<FieldTestItemGroupCounter>> segmentToFieldTestItemGroupCounters;

    @Autowired
    public EqualDistributionFieldTestItemGroupSelector(ItemPoolService itemPoolService) {
        this.itemPoolService = itemPoolService;
        this.segmentToFieldTestItemGroupCounters = new ConcurrentHashMap<>();
    }

    @Override
    public List<FieldTestItemGroup> selectLeastUsedItemGroups(final Exam exam, final Set<String> assignedGroupIds,
                                                              final Assessment assessment, final Segment currentSegment, final int numItems) {
        final String segmentKey = currentSegment.getKey();
        int ftItemCount = 0;
        // Fetch every eligible item based on item constraints, item properties, and user accommodations
        Set<Item> fieldTestItems = itemPoolService.getFieldTestItemPool(exam.getId(), assessment.getItemConstraints(), currentSegment.getItems(exam.getLanguageCode()));
        /* In StudentDLL.FT_Prioritize2012_SP() [3544] - The legacy code creates temporary tables and groups data by groupid, blockid, groupkey.
            We can just worry about grouping by groupkey since groupKey appears to be the same as "<group-id>_<block-id>".

            The deletion at line [3186] simply removes item groups already found to be assigned to a student
                - the filter below will take care of this.
         */
        Map<String, List<FieldTestItemGroup>> eligibleGroupKeysToItemGroups = fieldTestItems.stream()
            .filter(fieldTestItem -> !assignedGroupIds.contains(fieldTestItem.getGroupId()))    // Filter all previously assigned item groups
            .map(fieldTestItem -> new FieldTestItemGroup.Builder()
                .withExamId(exam.getId())
                .withGroupId(fieldTestItem.getGroupId())
                .withGroupKey(fieldTestItem.getGroupKey())
                .withBlockId(fieldTestItem.getBlockId())
                .build())
            .collect(Collectors.groupingBy(FieldTestItemGroup::getGroupKey));

        List<FieldTestItemGroup> itemGroupsWithItemCounts = new ArrayList<>();
        Map<String, List<FieldTestItemGroup>> itemGroupsNotCached = new HashMap<>();
        // If the cache does not contain any group key counters for this segment, add all of the groups this
        // student is eligible for
        // Get the list (sorted by number of group key occurrences) and starting at the top, add as many as we need to the list
        List<FieldTestItemGroupCounter> fieldTestItemGroupCounters = segmentToFieldTestItemGroupCounters.get(segmentKey);
        if (fieldTestItemGroupCounters == null) {
            // If there is no cached values for this segmentKey, simply add all the group keys this student is eligible for
            fieldTestItemGroupCounters = eligibleGroupKeysToItemGroups.keySet().stream()
                .map(groupKey -> new FieldTestItemGroupCounter(groupKey))
                .collect(Collectors.toList());

            segmentToFieldTestItemGroupCounters.put(segmentKey, Collections.synchronizedList(fieldTestItemGroupCounters));
        } else { // Otherwise, if there is existing counters for this segment key, lets cache any items that don't already exist by adding them to the top
            // Get the set of cached group keys for quick comparison
            Set<String> groupKeysInCache = fieldTestItemGroupCounters.stream()
                .map(counters -> counters.getGroupKey())
                .collect(Collectors.toSet());

            // Iterate over each eligible group key for this exam and check if it is already cached. If not, add it to
            // the cache at the front of the list (because occurrence = 0 and we want all unused groups to be selected first
            for (List<FieldTestItemGroup> itemGroups : eligibleGroupKeysToItemGroups.values()) {
                FieldTestItemGroup itemGroup = itemGroups.get(0);
                if (!groupKeysInCache.contains(itemGroup.getGroupKey())) {
                    fieldTestItemGroupCounters.add(0, new FieldTestItemGroupCounter(itemGroup.getGroupKey()));
                    itemGroupsNotCached.put(itemGroup.getGroupKey(), itemGroups);
                }
            }
        }

        for (FieldTestItemGroupCounter groupCounter : fieldTestItemGroupCounters) {
            String groupKey = groupCounter.getGroupKey();
            // Break out of this loop if we've selected the # of items we needed to select
            if (ftItemCount >= numItems) {
                break;
            }

            // Check that the groupKey is one of the ones this examinee is eligible for
            List<FieldTestItemGroup> itemGroups = eligibleGroupKeysToItemGroups.get(groupKey);

            FieldTestItemGroup itemGroup;
            // If the segment is not in the cache, it has not been used yet. Select this group and cache the occurrence
            if (itemGroups == null) {
                List<FieldTestItemGroup> nonCachedItemGroups = itemGroupsNotCached.get(groupKey);

                if (nonCachedItemGroups == null || nonCachedItemGroups.isEmpty()) {
                    continue;
                }

                itemGroup = new FieldTestItemGroup.Builder()
                    .fromFieldTestItemGroup(nonCachedItemGroups.get(0))
                    .withItemCount(nonCachedItemGroups.size())
                    .build();
            } else {
                // Since we are only concerned with data shared between all the items in the group, we can just pick the first
                itemGroup = new FieldTestItemGroup.Builder()
                    .fromFieldTestItemGroup(itemGroups.get(0))
                    .withItemCount(itemGroups.size())
                    .build();
            }
            itemGroupsWithItemCounts.add(itemGroup);


            // Update the occurrence counter and the field test item count
            groupCounter.incrementOccurrance();
            ftItemCount += itemGroups.size();
            // Remove group from the map so we can keep track of any field test item groups that might need to be added to the cache
            eligibleGroupKeysToItemGroups.remove(groupKey);
        }

        // Check if there are any groups that may need to be added to cache (initialized)
        if (!eligibleGroupKeysToItemGroups.isEmpty()) {
            cacheInitializedCounters(segmentKey, eligibleGroupKeysToItemGroups);
        }

        // Re-sort the collection after updating occurrence counts
        Collections.sort(fieldTestItemGroupCounters);

        return itemGroupsWithItemCounts;
    }

    private void cacheInitializedCounters(String segmentKey, Map<String, List<FieldTestItemGroup>> fieldTestItemGroupsMap) {
        // We may need to initialize these group key counters and cache if they are not already cached
        List<FieldTestItemGroupCounter> groupCounters = segmentToFieldTestItemGroupCounters.get(segmentKey);
        Set<String> cachedGroupKeys = groupCounters.stream()
            .map(counter -> counter.getGroupKey())
            .collect(Collectors.toSet());

        for (String groupKey : fieldTestItemGroupsMap.keySet()) {
            if (!cachedGroupKeys.contains(groupKey)) {
                groupCounters.add(new FieldTestItemGroupCounter(groupKey));
            }
        }
    }

    private class FieldTestItemGroupCounter implements Comparable<FieldTestItemGroupCounter> {
        private final String groupKey;
        AtomicInteger occurrences;

        public FieldTestItemGroupCounter(String groupKey) {
            this.groupKey = groupKey;
            occurrences = new AtomicInteger(0);
        }

        public void incrementOccurrance() {
            occurrences.incrementAndGet();
        }

        public String getGroupKey() {
            return this.groupKey;
        }

        @Override
        public int compareTo(FieldTestItemGroupCounter other) {
            return occurrences.get() - other.occurrences.get();
        }
    }
}
