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

package tds.exam;

/**
 * An {@link tds.exam.ExamPrintRequest} with additional data
 */
public class ExpandableExamPrintRequest {
    public static final String EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM = "withExam";

    private Exam exam;
    private ExamPrintRequest examPrintRequest;

    private ExpandableExamPrintRequest() {
    }

    public ExpandableExamPrintRequest(final Builder builder) {
        this.exam = builder.exam;
        this.examPrintRequest = builder.examPrintRequest;
    }

    public static class Builder {
        private Exam exam;
        private ExamPrintRequest examPrintRequest;

        public Builder(ExamPrintRequest examPrintRequest) {
            this.examPrintRequest = examPrintRequest;
        }

        public Builder withExam(Exam exam) {
            this.exam = exam;
            return this;
        }

        public ExpandableExamPrintRequest build() {
            return new ExpandableExamPrintRequest(this);
        }
    }

    /**
     * @return The {@link tds.exam.Exam} associated with the {@link tds.exam.ExamPrintRequest}.
     */
    public Exam getExam() {
        return exam;
    }

    /**
     * @return The {@link tds.exam.ExamPrintRequest}
     */
    public ExamPrintRequest getExamPrintRequest() {
        return examPrintRequest;
    }
}
