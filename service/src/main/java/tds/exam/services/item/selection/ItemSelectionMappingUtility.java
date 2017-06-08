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
