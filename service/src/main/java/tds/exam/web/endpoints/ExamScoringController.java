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

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.services.MessagingService;
import tds.exam.web.annotations.VerifyAccess;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.support.job.TestResultsWrapper;
import tds.trt.model.TDSReport;

/**
 * This controller is responsible for providing scoring information for exams.
 */
@RestController
@RequestMapping("/exam/{examId}/scores")
public class ExamScoringController {

    private final ItemScoringService itemScoringService;
    private final MessagingService messagingService;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    @Autowired
    ExamScoringController(final ItemScoringService itemScoringService, final MessagingService messagingService) throws JAXBException {
        this.itemScoringService = itemScoringService;
        this.messagingService = messagingService;

        final JAXBContext wrapperContext = JAXBContext.newInstance(TestResultsWrapper.class);
        this.marshaller = wrapperContext.createMarshaller();
        final JAXBContext context = JAXBContext.newInstance(TDSReport.class);
        this.unmarshaller = context.createUnmarshaller();
    }

    @RequestMapping(value = "/responses", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    public ResponseEntity<List<ItemResponseUpdateStatus>> updateResponses(@PathVariable final UUID examId,
                                                                          @RequestParam final UUID sessionId,
                                                                          @RequestParam final UUID browserId,
                                                                          @RequestParam final String clientName,
                                                                          @RequestParam final Float pageDuration,
                                                                          @RequestBody final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException {
        final ExamInstance examInstance = ExamInstance.create(examId, sessionId, browserId, clientName);
        final List<ItemResponseUpdateStatus> responses = itemScoringService.updateResponses(examInstance, responseUpdates, pageDuration);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @RequestMapping(value = "/rescore/{jobId}", method = RequestMethod.PUT)
    public ResponseEntity<NoContentResponseResource> rescoreTestResultsTransmission(@PathVariable final UUID examId,
                                                                                    @PathVariable final String jobId,
                                                                                    @RequestBody final String trtXml) throws ReturnStatusException, JAXBException {
        final TDSReport testResults = (TDSReport) unmarshaller.unmarshal(new StringReader(trtXml));
        final Optional<ValidationError> maybeError = itemScoringService.rescoreTestResults(examId, testResults);
        final TestResultsWrapper wrapper = new TestResultsWrapper(jobId, testResults);

        final boolean isSuccessful = !maybeError.isPresent();

        if (isSuccessful) {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            marshaller.marshal(wrapper, stream);
            messagingService.sendExamRescore(examId, stream.toByteArray());
        }

        return isSuccessful
            ? new ResponseEntity<>(new NoContentResponseResource(), HttpStatus.OK)
            : new ResponseEntity<>(new NoContentResponseResource(maybeError.get()), HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
