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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.exam.web.annotations.VerifyAccess;
import tds.student.sql.data.OpportunityItem;

@RestController
@RequestMapping("/exam")
public class ExamItemController {
    private final ExamItemService examItemService;
    private final ExamItemSelectionService examItemSelectionService;

    @Autowired
    public ExamItemController(final ExamItemService examItemService,
                              final ExamItemSelectionService examItemSelectionService) {
        this.examItemService = examItemService;
        this.examItemSelectionService = examItemSelectionService;
    }

    @PutMapping("/{examId}/item/{position}/review")
    @VerifyAccess
    ResponseEntity<NoContentResponseResource> markItemForReview(@PathVariable final UUID examId,
                                                                @PathVariable final int position,
                                                                @RequestBody final Boolean mark) {
        final Optional<ValidationError> maybeMarkFailure = examItemService.markForReview(examId, position, mark);

        if (maybeMarkFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeMarkFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{examId}/item")
    ResponseEntity<List<OpportunityItem>> getNextItemGroup(@PathVariable final UUID examId,
                                                           @RequestParam int lastPagePosition,
                                                           @RequestParam int lastItemPosition) {
        List<OpportunityItem> page = examItemSelectionService.createNextPageGroup(examId, lastPagePosition, lastItemPosition);
        return ResponseEntity.ok(page);
    }
}
