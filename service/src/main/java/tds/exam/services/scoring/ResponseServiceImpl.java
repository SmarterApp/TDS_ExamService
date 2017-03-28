package tds.exam.services.scoring;

import AIR.Common.DB.DbComparator;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.collect.Sets;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;
import tds.score.model.ExamInstance;
import tds.score.services.ResponseService;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.IItemResponseUpdate;

import static tds.exam.ExamStatusCode.STATUS_REVIEW;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_ENTRY;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_EXIT;
import static tds.exam.ExamStatusCode.STATUS_STARTED;

@Service
public class ResponseServiceImpl implements ResponseService {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseServiceImpl.class);
    private final ExamService examService;
    private final ConfigService configService;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamItemQueryRepository examItemQueryRepository;
    private final ExamPageService examPageService;

    private static final Set<String> VALID_EXAM_STATUS_CODES = Sets.newHashSet(STATUS_STARTED, STATUS_REVIEW, STATUS_SEGMENT_ENTRY, STATUS_SEGMENT_EXIT);

    @Autowired
    public ResponseServiceImpl(final ExamService examService,
                               final ConfigService configService,
                               final ExamItemCommandRepository examItemCommandRepository,
                               final ExamItemQueryRepository examItemQueryRepository,
                               final ExamPageService examPageService) {
        this.examService = examService;
        this.configService = configService;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examItemQueryRepository = examItemQueryRepository;
        this.examPageService = examPageService;
    }

    @Transactional
    @Override
    public ReturnStatus updateScoredResponse(final ExamInstance examInstance,
                                             final IItemResponseUpdate responseUpdate,
                                             final int score,
                                             final String scoreStatus,
                                             final String scoreRationale,
                                             final long scoreLatency,
                                             final long pageDuration) throws ReturnStatusException {
        //Replaces the implementation in StudentDLL.T_UpdateScoredResponse_common
        //Decision was made to not worry about verify access.  This should be done prior to this call.

        Optional<Exam> maybeExam = examService.findExam(examInstance.getExamId());
        if (!maybeExam.isPresent()) {
            throw new ReturnStatusException(String.format("Could not find exam %s associated with response", examInstance.getExamId()));
        }

        Exam exam = maybeExam.get();

        if (!VALID_EXAM_STATUS_CODES.contains(exam.getStatus().getCode())) {
            String message = configService.getFormattedMessage(examInstance.getClientName(), "T_UpdateScoredResponse", "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.");
            throw new ReturnStatusException(message);
        }

        Optional<ExamItem> maybeItem = examItemQueryRepository.findExamItemAndResponse(examInstance.getExamId(), responseUpdate.getPosition());

        Date dt = null;
        String dateCreated = responseUpdate.getDateCreated();
        try {
            if (responseUpdate.getDateCreated() != null) {
                String[] tokens = dateCreated.split("\\.");
                if (tokens.length == 2 && tokens[1].length() > 3) {
                    dateCreated = String.format("%s.%s", tokens[0], tokens[1].substring(0, 3));
                }
                dt = DateUtils.parseDate(dateCreated,
                    new String[]{"yyyy-MM-dd HH:mm:ss.SSS",
                        "yyyy-MM-dd HH:mm:ss"});
            }
        } catch (Exception e) {
            LOG.error(String.format("Unexpected dateCreated format: %s, please update StudentDLL.T_UpdateScoredResponse to parse this format!",
                dateCreated));
        }

        String errorMessage = "";
        //TODO - There is a check in StudentDLL to check if the item still exists in itembank.  Doesn't seem necessary
        if (!maybeItem.isPresent() || !maybeItem.get().getItemKey().equals(responseUpdate.getItemID())) {
            errorMessage = String.format("The item does not exist at this position in this test opportunity: Position = %d; Item = %s; testeeresponse._efk_ItemKey found is %s",
                responseUpdate.getPosition(), responseUpdate.getItemID(), (maybeItem.map(ExamItem::getItemKey).orElse("null")));
        } else if (maybeItem.get().getResponse().isPresent() && dt != null && maybeItem.get().getResponse().get().getCreatedAt().getMillis() != dt.getTime()) {
            errorMessage = String.format("Item security codes do not match:  Position = %d; Item = %s; Date = %s ", responseUpdate.getPosition(), responseUpdate.getItemID(), dateCreated);
        } else if (maybeItem.get().getResponse().isPresent() && DbComparator.greaterThan(maybeItem.get().getResponse().get().getSequence(), responseUpdate.getSequence())) {
            errorMessage = String.format("Responses out of sequence: Position = %d;  Stored sequence = %d;  New sequence = %d", responseUpdate.getPosition(), maybeItem.get().getResponse().get().getSequence(), responseUpdate.getSequence());
        }

        if (StringUtils.isNotEmpty(errorMessage)) {
            String message = configService.getFormattedMessage(examInstance.getClientName(), null, "T_UpdateScoredResponse", null);
            throw new ReturnStatusException(message);
        }

        ExamItem existingExamItem = maybeItem.get();
        ExamItemResponse.Builder updatedResponseBuilder = new ExamItemResponse.Builder().fromExamItemResponse(existingExamItem.getResponse().get());
        ExamItemResponseScore.Builder updatedScoreBuilder = new ExamItemResponseScore.Builder();
        final Instant now = Instant.now();
        if (responseUpdate.getValue() != null || DbComparator.lessThan(score, 0)) {
            // new response, score to be determined
            updatedScoreBuilder.withScore(-1); // not scored
            updatedScoreBuilder.withScoreMark(UUID.randomUUID()); // app will need this token to update the
            // record with the score for THIS response
            // once it is determined asynchronously
            updatedScoreBuilder.withScoreSentAt(now); // start the scoring clock running
        } else if (responseUpdate.getValue() != null && DbComparator.greaterOrEqual(scoreLatency, 0)) {
            {
//                updatedScoreBuilder.withScoreMark(null); // any previous scoremark is now obsolete
                updatedScoreBuilder.withScore(score); // use the provided score

                updatedScoreBuilder.withScoreSentAt(now); // 'instantaneous' time lag - TODO - check if we should do this
                updatedScoreBuilder.withScoredAt(now);
            }
        }

        if (responseUpdate.getValue() != null) {
            updatedResponseBuilder.withResponse(
                StringUtils.replace(responseUpdate.getValue(), "\\", "\\\\"));
            updatedScoreBuilder.withScoreLatency(scoreLatency);
            updatedScoreBuilder.withScoringStatus(scoreStatus == null ? null : ExamScoringStatus.fromType(scoreStatus));
            updatedScoreBuilder.withScoringRationale(scoreRationale);
            updatedScoreBuilder.withScoringDimensions(buildScoreInfoNode(score, "overall", scoreStatus));
        }

        updatedResponseBuilder.withSelected(responseUpdate.getIsSelected());
        updatedResponseBuilder.withValid(responseUpdate.getIsValid());
        updatedResponseBuilder.withSequence(responseUpdate.getSequence());

        ExamItemResponse response = updatedResponseBuilder.withScore(updatedScoreBuilder.build()).build();
        examItemCommandRepository.insertResponses(response);

        if (responseUpdate.getValue() != null) {
            Optional<ExamPage> maybeExamPage = examPageService.find(existingExamItem.getExamPageId());

            //Something is misconfigured if this is ever true
            if(!maybeExamPage.isPresent()) {
                throw new ReturnStatusException("Exam page is no longer present for the updated item");
            }

            ExamPage examPage = ExamPage.Builder.fromExamPage(maybeExamPage.get())
                .withDuration(pageDuration)
                .build();

            examPageService.update(examPage);
        }

        return new ReturnStatus("updated");
    }

    @Override
    public ReturnStatus updateItemScore(final UUID oppKey,
                                        final IItemResponseScorable responseScorable,
                                        final int score, final String scoreStatus,
                                        final String scoreRationale,
                                        final String scoreDimensions) throws ReturnStatusException {
        return null;
    }

    //Pulled directly from StudentDLL
    private String buildScoreInfoNode (Integer score, String scoreDimConstant, String scoreStatus) {
        // (SELECT Score AS "@scorePoint" ,'overall' AS
        // "@scoreDimension",scorestatus AS "@scoreStatus",
        // Example
        // <ScoreInfo scorePoint="1" scoreDimension="overall" scoreStatus="Scored">
        return String.format ("<ScoreInfo scorePoint=\"%d\" scoreDimension=\"%s\" scoreStatus=\"%s\"><SubScoreList /></ScoreInfo>",
            score, scoreDimConstant, scoreStatus);
    }
}
