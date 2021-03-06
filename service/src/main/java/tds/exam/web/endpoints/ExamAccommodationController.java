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
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.ExamAccommodation;
import tds.exam.services.ExamAccommodationService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/exam")
public class ExamAccommodationController {
    private final ExamAccommodationService examAccommodationService;
    
    @Autowired
    public ExamAccommodationController(ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }
    
    @RequestMapping(value = "/{examId}/{segmentId}/accommodations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamAccommodation>> findAccommodations(@PathVariable final UUID examId,
                                                               @PathVariable final String segmentId,
                                                               @RequestParam(name = "type", required = false) final String[] accommodationTypes) {
        return ResponseEntity.ok(examAccommodationService.findAccommodations(examId, segmentId, accommodationTypes));
    }
    
    @RequestMapping(value = "/{examId}/accommodations", method = RequestMethod.GET)
    ResponseEntity<List<ExamAccommodation>> findAccommodations(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examAccommodationService.findAllAccommodations(examId));
    }
    
    @RequestMapping(value = "/{examId}/accommodations/approved")
    ResponseEntity<List<ExamAccommodation>> findApprovedAccommodations(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examAccommodationService.findApprovedAccommodations(examId));
    }
}
