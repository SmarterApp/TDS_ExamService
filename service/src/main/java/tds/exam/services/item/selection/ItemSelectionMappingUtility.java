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

package tds.exam.services.item.selection;

import tds.assessment.Item;
import tds.itemselection.base.TestItem;

/**
 * Utility class with common mapping utility methods
 */
class ItemSelectionMappingUtility {
    private ItemSelectionMappingUtility() {
    }

    /**
     * Converts a {@link tds.assessment.Item} to an item selection {@link tds.itemselection.base.TestItem}
     *
     * @param item the item to convert
     * @return the {@link tds.itemselection.base.TestItem}
     */
    static TestItem convertItem(Item item) {
        TestItem testItem = new TestItem();
        testItem.setItemID(item.getId());
        testItem.setGroupID(item.getGroupId());
        testItem.setFieldTest(item.isFieldTest());
        testItem.setRequired(item.isRequired());
        testItem.isActive = item.isActive();
        testItem.strandName = item.getStrand();
        testItem.position = item.getPosition();
        testItem.setItemType(item.getItemType());

        //We call this claims but the legacy application calls it content levels
        testItem.setContentLevels(item.getClaims());
        return testItem;
    }
}
