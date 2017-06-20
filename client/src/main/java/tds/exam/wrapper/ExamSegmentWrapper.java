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

import java.util.ArrayList;
import java.util.List;

import tds.exam.ExamSegment;

/**
 * Contains the {@link tds.exam.ExamSegment} and its associated {@link tds.exam.ExamPage}s
 */
public class ExamSegmentWrapper {
    private ExamSegment examSegment;
    private List<ExamPageWrapper> examPages = new ArrayList<>();

    public ExamSegmentWrapper(final ExamSegment examSegment, final List<ExamPageWrapper> examPages) {
        this.examSegment = examSegment;
        this.examPages = examPages;
    }

    //For frameworks
    private ExamSegmentWrapper() {
    }

    /**
     * @return the {@link tds.exam.ExamSegment}
     */
    public ExamSegment getExamSegment() {
        return examSegment;
    }

    /**
     * @return the {@link tds.exam.ExamPage}s associated with the {@link tds.exam.ExamSegment}
     */
    public List<ExamPageWrapper> getExamPages() {
        return examPages;
    }
}

