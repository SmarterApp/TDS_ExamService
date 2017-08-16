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

package tds.exam.wrapper;

import java.util.Collections;
import java.util.List;

import tds.exam.ExamItem;
import tds.exam.ExamPage;

/**
 * Contains the {@link tds.exam.ExamPage} and its associated {@link tds.exam.ExamItem}s
 */
public class ExamPageWrapper {
    private ExamPage examPage;
    private List<ExamItem> examItems;

    public ExamPageWrapper(final ExamPage examPage, final List<ExamItem> examItems) {
        this.examPage = examPage;
        this.examItems = examItems;
    }

    //For frameworks
    private ExamPageWrapper() {
    }

    /**
     * @return the {@link tds.exam.ExamPage}
     */
    public ExamPage getExamPage() {
        return examPage;
    }

    /**
     * @return the associated {@link tds.exam.ExamPage}'s {@link tds.exam.ExamItem}
     */
    public List<ExamItem> getExamItems() {
        if(examItems == null) return Collections.emptyList();

        return examItems;
    }
}
