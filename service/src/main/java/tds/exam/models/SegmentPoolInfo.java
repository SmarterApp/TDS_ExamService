package tds.exam.models;

import java.util.Set;

import tds.assessment.Item;

/**
 * A class that contains information about a computed segment pool.
 */
public class SegmentPoolInfo {
    private int length;
    private int poolCount;
    private Set<Item> itemPool;

    public SegmentPoolInfo(int length, int poolCount, Set<Item> itemPool) {
        this.length = length;
        this.poolCount = poolCount;
        this.itemPool = itemPool;
    }

    /**
     * @return the length (number of items to be selected) for the exam segment
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the sum of all strands available in the segment pool
     */
    public int getPoolCount() {
        return poolCount;
    }

    /**
     * @return the list of eligible {@link tds.assessment.Item}'s ids for the segment pool
     */
    public Set<Item> getItemPool() {
        return itemPool;
    }

}
