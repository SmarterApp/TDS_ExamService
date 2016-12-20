package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.services.ExamAccommodationService;

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

    @RequestMapping(value = "/{examId}/accommodations")
    ResponseEntity<List<ExamAccommodation>> findAccommodations(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examAccommodationService.findAllAccommodations(examId));
    }

    @RequestMapping(value = "/{examId}/accommodations/approved")
    ResponseEntity<List<ExamAccommodation>> findApprovedAccommodations(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examAccommodationService.findApprovedAccommodations(examId));
    }
}
