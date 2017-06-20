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
import java.util.UUID;

/**
 * Contains all the item groups used for the exam
 */
public class ItemGroupHistory {
    private final UUID examId;
    private final Set<String> itemGroupIds;

    public ItemGroupHistory(final UUID examId, final Set<String> itemGroupIds) {
        this.examId = examId;
        this.itemGroupIds = itemGroupIds;
    }

    /**
     * @return the associated exam id
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return the item group ids for the exam
     */
    public Set<String> getItemGroupIds() {
        return itemGroupIds;
    }
}
