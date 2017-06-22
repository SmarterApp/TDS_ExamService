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

package tds.exam.builder;

import java.util.ArrayList;
import java.util.List;

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
    private String itemFilePath = "/item/item-123.xml";
    private String formKey;

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
        item.setFormKey(formKey);
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

    public ItemBuilder withFormKey(final String formKey) {
        this.formKey = formKey;
        return this;
    }

    public ItemBuilder withRequired(final boolean required) {
        this.required = required;
        return this;
    }
}
