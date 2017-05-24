package tds.exam.web.endpoints;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tds.exam.web.annotations.VerifyAccess;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * This controller is responsible for providing scoring information for exams.
 */
@RestController
@RequestMapping("/exam/{examId}/scores")
public class ExamScoringController {

    private final ItemScoringService itemScoringService;


    @Autowired
    ExamScoringController(final ItemScoringService itemScoringService) {
        this.itemScoringService = itemScoringService;
    }

    @Autowired
    ObjectMapper objectMapper;

    @RequestMapping(value = "/responses", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    public ResponseEntity<List<ItemResponseUpdateStatus>> updateResponses(@PathVariable final UUID examId,
                                                                          @RequestParam final UUID sessionId,
                                                                          @RequestParam final UUID browserId,
                                                                          @RequestParam final String clientName,
                                                                          @RequestParam final Float pageDuration,
                                                                          @RequestBody final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException {
        final ExamInstance examInstance = ExamInstance.create(examId, sessionId, browserId, clientName);
        StopWatch timer = new StopWatch();
        timer.start();
        System.out.println(String.format("examID: %s sessionId: %s browserId: %s clientName: %s pageDuration: %f", examId.toString(), sessionId.toString(), browserId.toString(), clientName, pageDuration));

        ObjectWriter ow = objectMapper.writer();

        try {
            System.out.println(ow.writeValueAsString(responseUpdates));
        } catch (Exception e) {
            e.printStackTrace();
        }

        final List<ItemResponseUpdateStatus> responses = itemScoringService.updateResponses(examInstance, responseUpdates, pageDuration);

        timer.stop();
        System.out.println("ExamScoringController:updateResponses: " + timer.toString());

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @RequestMapping(value = "/responses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemResponseUpdateStatus>> updateResponsesOpt() throws Exception {
        final UUID examId = UUID.fromString("f1c8bc38-b6d8-4064-afd2-b7e6f026f5ef");
        final UUID sessionId = UUID.fromString("ad1350c7-0747-45d2-8879-7289dee2566f");
        final UUID browserId = UUID.fromString("7b5b8fe5-7ec3-46a9-a6e5-b226767151fd");
        final String clientName = "SBAC_PT";
        final Float pageDuration = 6460.000000f;
        ObjectReader or = objectMapper.reader();
        ItemResponseUpdate[] itemResponseUpdateStatus = objectMapper.readValue("[{\"IsSelected\":true,\"IsValid\":true,\"Page\":1,\"PageKey\":\"4914de90-554a-499b-9343-586001157897\",\"Value\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?><AnswerSet><Question id=\\\"\\\"><QuestionPart id=\\\"1\\\"><ObjectSet><RegionGroupObject name=\\\"Key\\\" numselected=\\\"0\\\"><RegionObject name=\\\"4cups\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"10cups\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"20cups\\\" isselected=\\\"false\\\"/></RegionGroupObject><RegionGroupObject name=\\\"WeekOne\\\" numselected=\\\"1\\\"><RegionObject name=\\\"WeekOneHalf\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne1\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne1Half\\\" isselected=\\\"true\\\"/><RegionObject name=\\\"WeekOne2\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne2Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne3\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne3Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne4\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne4Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne5\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne5Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne6\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne6Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne7\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne7Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekOne8\\\" isselected=\\\"false\\\"/></RegionGroupObject><RegionGroupObject name=\\\"WeekTwo\\\" numselected=\\\"1\\\"><RegionObject name=\\\"WeekTwoHalf\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo1\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo1Half\\\" isselected=\\\"true\\\"/><RegionObject name=\\\"WeekTwo2\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo2Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo3\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo3Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo4\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo4Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo5\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo5Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo6\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo6Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo7\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo7Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekTwo8\\\" isselected=\\\"false\\\"/></RegionGroupObject><RegionGroupObject name=\\\"WeekThree\\\" numselected=\\\"1\\\"><RegionObject name=\\\"WeekThreeHalf\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree1\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree1Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree2\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree2Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree3\\\" isselected=\\\"true\\\"/><RegionObject name=\\\"WeekThree3Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree4\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree4Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree5\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree5Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree6\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree6Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree7\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree7Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekThree8\\\" isselected=\\\"false\\\"/></RegionGroupObject><RegionGroupObject name=\\\"WeekFour\\\" numselected=\\\"0\\\"><RegionObject name=\\\"WeekFourHalf\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour1\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour1Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour2\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour2Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour3\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour3Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour4\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour4Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour5\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour5Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour6\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour6Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour7\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour7Half\\\" isselected=\\\"false\\\"/><RegionObject name=\\\"WeekFour8\\\" isselected=\\\"false\\\"/></RegionGroupObject></ObjectSet><SnapPoint></SnapPoint></QuestionPart></Question></AnswerSet>\",\"Position\":4,\"Sequence\":5,\"ItemID\":\"187-1578\",\"FilePath\":\"/usr/local/tomcat/resources/tds/bank/items/Item-187-1578/item-187-1578.xml\",\"ScoreMark\":\"9891c1ae-6ff1-416e-8aeb-d4a2e44a4092\",\"Language\":\"ENU\",\"ClientName\":\"SBAC_PT\",\"ItemKey\":1578,\"BankKey\":187,\"SegmentID\":\"SBAC-Perf-MATH-3\",\"TestID\":\"SBAC-Perf-MATH-3\",\"TestKey\":\"(SBAC_PT)SBAC-Perf-MATH-3-Spring-2013-2015\"},{\"IsSelected\":true,\"IsValid\":true,\"Page\":1,\"PageKey\":\"4914de90-554a-499b-9343-586001157897\",\"Value\":\"<responseSpec><responseTable><tr><th id=\\\"col0\\\"/><th id=\\\"col1\\\"/></tr><tr><td/><td>1</td></tr><tr><td/><td>1</td></tr><tr><td/><td>1</td></tr><tr><td/><td/></tr></responseTable></responseSpec>\",\"Position\":3,\"Sequence\":5,\"ItemID\":\"187-2789\",\"FilePath\":\"/usr/local/tomcat/resources/tds/bank/items/Item-187-2789/item-187-2789.xml\",\"Language\":\"ENU\",\"ClientName\":\"SBAC_PT\",\"ItemKey\":2789,\"BankKey\":187,\"SegmentID\":\"SBAC-Perf-MATH-3\",\"TestID\":\"SBAC-Perf-MATH-3\",\"TestKey\":\"(SBAC_PT)SBAC-Perf-MATH-3-Spring-2013-2015\"},{\"IsSelected\":true,\"IsValid\":true,\"Page\":1,\"PageKey\":\"4914de90-554a-499b-9343-586001157897\",\"Value\":\"<response><math xmlns=\\\"http://www.w3.org/1998/Math/MathML\\\" title=\\\"1\\\"><mstyle><mn>1</mn></mstyle></math></response>\",\"Position\":2,\"Sequence\":5,\"ItemID\":\"187-1576\",\"FilePath\":\"/usr/local/tomcat/resources/tds/bank/items/Item-187-1576/item-187-1576.xml\",\"ScoreMark\":\"6aa63160-91bc-4316-bd24-b150bc6303bf\",\"Language\":\"ENU\",\"ClientName\":\"SBAC_PT\",\"ItemKey\":1576,\"BankKey\":187,\"SegmentID\":\"SBAC-Perf-MATH-3\",\"TestID\":\"SBAC-Perf-MATH-3\",\"TestKey\":\"(SBAC_PT)SBAC-Perf-MATH-3-Spring-2013-2015\"},{\"IsSelected\":true,\"IsValid\":true,\"Page\":1,\"PageKey\":\"4914de90-554a-499b-9343-586001157897\",\"Value\":\"<responseSpec><responseTable><tr><th id=\\\"col0\\\"/><th id=\\\"col1\\\"/></tr><tr><td/><td>1</td></tr><tr><td/><td>1</td></tr><tr><td/><td>1</td></tr><tr><td/><td>1</td></tr></responseTable></responseSpec>\",\"Position\":1,\"Sequence\":5,\"ItemID\":\"187-2788\",\"FilePath\":\"/usr/local/tomcat/resources/tds/bank/items/Item-187-2788/item-187-2788.xml\",\"Language\":\"ENU\",\"ClientName\":\"SBAC_PT\",\"ItemKey\":2788,\"BankKey\":187,\"SegmentID\":\"SBAC-Perf-MATH-3\",\"TestID\":\"SBAC-Perf-MATH-3\",\"TestKey\":\"(SBAC_PT)SBAC-Perf-MATH-3-Spring-2013-2015\"}]\n", ItemResponseUpdate[].class);
        final List<ItemResponseUpdate> responseUpdates = Arrays.asList(itemResponseUpdateStatus);

        return updateResponses(examId, sessionId, browserId, clientName, pageDuration, responseUpdates);
    }
}
