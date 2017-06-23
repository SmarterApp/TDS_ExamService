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

package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.services.ExamApprovalService;

@RestController
@RequestMapping("/exam")
public class ExamApprovalController {
    private final ExamApprovalService examApprovalService;

    @Autowired
    public ExamApprovalController(ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @RequestMapping(value = "/{id}/approval", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<ExamApproval>> getApproval(@PathVariable final UUID id,
                                                       @RequestParam final UUID sessionId,
                                                       @RequestParam final UUID browserId) {
        ExamInfo examInfo = new ExamInfo(id, sessionId, browserId);
        Response<ExamApproval> examApproval = examApprovalService.getApproval(examInfo);

        if (examApproval.hasError()) {
            return new ResponseEntity<>(examApproval, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examApproval);
    }

    @RequestMapping(value = "/pending-approval/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Exam>> getExamsPendingApproval(@PathVariable final UUID sessionId) {
        List<Exam> examsPendingApproval = examApprovalService.getExamsPendingApproval(sessionId);

        return ResponseEntity.ok(examsPendingApproval);
    }
}
