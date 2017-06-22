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

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.services.ExpandableExamService;

@RestController
@RequestMapping("/exam")
public class ExpandableExamController {
    private final ExpandableExamService expandableExamService;

    public ExpandableExamController(final ExpandableExamService expandableExamService) {
        this.expandableExamService = expandableExamService;
    }

    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExpandableExam>> findExamsForSessionId(@PathVariable final UUID sessionId,
                                                               @RequestParam(required = false) final Set<String> statusNot,
                                                               @RequestParam(required = false) final ExpandableExamAttributes... expandableAttribute) {
        final List<ExpandableExam> exams = expandableExamService.findExamsBySessionId(sessionId, statusNot, expandableAttribute);

        return ResponseEntity.ok(exams);
    }

    @RequestMapping(value = "/{examId}/expandable", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExpandableExam> findExpandableExam(@PathVariable final UUID examId,
                                                      @RequestParam(required = false) final ExpandableExamAttributes... expandableAttribute) {
        ExpandableExam expandableExam = expandableExamService.findExam(examId, expandableAttribute)
            .orElseThrow(() -> new NotFoundException("Could not find exam for %s", examId));

        return ResponseEntity.ok(expandableExam);
    }
}
