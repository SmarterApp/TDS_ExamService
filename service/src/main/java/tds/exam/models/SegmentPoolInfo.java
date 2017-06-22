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
