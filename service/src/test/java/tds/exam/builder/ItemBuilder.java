package tds.exam.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tds.assessment.Item;
import tds.assessment.ItemProperty;

public class ItemBuilder {
    private String id = "item-id";
    private String groupKey = "group-key";
    private String groupId = "group-id";
    private String strand = "strand";
    private String blockId = "A";
    private String itemType = "MI";
    private boolean fieldTest = false;
    private boolean required = true;
    private List<ItemProperty> itemProperties = new ArrayList<>();
    private Set<String> formKeys = new HashSet<>();
    private String itemFilePath = "/item/item-123.xml";

    public ItemBuilder(String id) {
        this.id = id;
    }

    public Item build() {
        Item item = new Item(id);
        item.setGroupKey(groupKey);
        item.setGroupId(groupId);
        item.setStrand(strand);
        item.setBlockId(blockId);
        item.setFieldTest(fieldTest);
        item.setItemType(itemType);
        item.setFormKeys(formKeys);
        item.setRequired(required);
        item.setItemProperties(itemProperties);
        item.setItemFilePath(itemFilePath);
        return item;
    }

    public ItemBuilder withGroupKey(String groupKey) {
        this.groupKey = groupKey;
        return this;
    }

    public ItemBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public ItemBuilder withBlockId(String blockId) {
        this.blockId = blockId;
        return this;
    }

    public ItemBuilder withStrand(String strand) {
        this.strand = strand;
        return this;
    }

    public ItemBuilder withFieldTest(boolean fieldTest) {
        this.fieldTest = fieldTest;
        return this;
    }

    public ItemBuilder withItemProperties(List<ItemProperty> itemProperties) {
        this.itemProperties = itemProperties;
        return this;
    }

    public ItemBuilder withItemFilePath(final String itemFilePath) {
        this.itemFilePath = itemFilePath;
        return this;
    }

    public ItemBuilder withItemType(final String itemType) {
        this.itemType = itemType;
        return this;
    }
}
