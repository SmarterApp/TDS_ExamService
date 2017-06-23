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

package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.student.sql.data.OpportunityItem;

public interface ExamItemSelectionService {
    /**
     * Creates the next page group
     *
     * @param examId   exam id
     * @param lastPage the last page that had items
     * @return a {@link tds.student.services.data.PageGroup}
     */
    List<OpportunityItem> createNextPageGroup(UUID examId, int lastPage, int lastItemPosition);
}
