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

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    static final Set<String> VALID_EXAM_STATUS_CODES = Sets.newHashSet(STATUS_STARTED, STATUS_REVIEW, STATUS_SEGMENT_ENTRY, STATUS_SEGMENT_EXIT);

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

        final Exam exam = examService.findExam(examInstance.getExamId())
            .orElseThrow(() -> new ReturnStatusException(String.format("Could not find exam %s associated with response", examInstance.getExamId())));

        if (!VALID_EXAM_STATUS_CODES.contains(exam.getStatus().getCode())) {
            final String message = configService.getFormattedMessage(examInstance.getClientName(), "T_UpdateScoredResponse", "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.");
            throw new ReturnStatusException(message);
        }

        final Optional<ExamItem> maybeItem = examItemQueryRepository.findExamItemAndResponse(examInstance.getExamId(), responseUpdate.getPosition());

        Date dt = null;
        String dateCreated = responseUpdate.getDateCreated();
        try {
            if (responseUpdate.getDateCreated() != null) {
                final String[] tokens = dateCreated.split("\\.");
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
        if (!maybeItem.isPresent() || !maybeItem.get().getItemKey().equals(responseUpdate.getItemID())) {
            errorMessage = String.format("The item does not exist at this position in this test opportunity: Position = %d; Item = %s; testeeresponse._efk_ItemKey found is %s",
                responseUpdate.getPosition(), responseUpdate.getItemID(), (maybeItem.map(ExamItem::getItemKey).orElse("null")));
        } else if (maybeItem.get().getResponse().isPresent() && dt != null && maybeItem.get().getResponse().get().getCreatedAt().getMillis() != dt.getTime()) {
            errorMessage = String.format("Item security codes do not match:  Position = %d; Item = %s; Date = %s ", responseUpdate.getPosition(), responseUpdate.getItemID(), dateCreated);
        } else if (maybeItem.get().getResponse().isPresent() && DbComparator.greaterThan(maybeItem.get().getResponse().get().getSequence(), responseUpdate.getSequence())) {
            errorMessage = String.format("Responses out of sequence: Position = %d;  Stored sequence = %d;  New sequence = %d", responseUpdate.getPosition(), maybeItem.get().getResponse().get().getSequence(), responseUpdate.getSequence());
        }

        if (StringUtils.isNotEmpty(errorMessage)) {
            LOG.warn("Problem accessing exam item: {}", errorMessage);
            final String message = configService.getFormattedMessage(examInstance.getClientName(), null, "T_UpdateScoredResponse");
            throw new ReturnStatusException(message);
        }

        final ExamItem existingExamItem = maybeItem.get();
        final ExamItemResponse.Builder updatedResponseBuilder;

        if (existingExamItem.getResponse().isPresent()) {
            updatedResponseBuilder = ExamItemResponse.Builder
                .fromExamItemResponse(existingExamItem.getResponse().get());
        } else {
            updatedResponseBuilder = new ExamItemResponse.Builder()
                .withExamItemId(existingExamItem.getId())
                .withExamId(exam.getId())
                .withResponse(responseUpdate.getValue());
        }
        updatedResponseBuilder
            .withSelected(responseUpdate.getIsSelected())
            .withValid(responseUpdate.getIsValid())
            .withSequence(responseUpdate.getSequence());

        final ExamItemResponseScore.Builder updatedScoreBuilder = new ExamItemResponseScore.Builder();

        if (responseUpdate.getValue() == null) {
            updatedResponseBuilder.withScore(updatedScoreBuilder.build());
            examItemCommandRepository.insertResponses(updatedResponseBuilder.build());
            return new ReturnStatus("updated");
        }

        final Instant now = Instant.now();
        if (DbComparator.lessThan(score, 0)) {
            // new response, score to be determined
            updatedScoreBuilder.withScore(-1); // not scored
            updatedScoreBuilder.withScoreMark(UUID.randomUUID()); // app will need this token to update the
            // record with the score for THIS response
            // once it is determined asynchronously
            updatedScoreBuilder.withScoreSentAt(now); // start the scoring clock running
        } else if (DbComparator.greaterOrEqual(scoreLatency, 0)) {
            updatedScoreBuilder.withScoreMark(null); // any previous scoremark is now obsolete
            updatedScoreBuilder.withScore(score); // use the provided score
            updatedScoreBuilder.withScoreSentAt(now); // 'instantaneous' time lag - TODO - check if we should do this
            updatedScoreBuilder.withScoredAt(now);
        }

        updatedScoreBuilder.withScoreLatency(scoreLatency)
            .withScoringStatus(scoreStatus == null ? null : ExamScoringStatus.fromType(scoreStatus))
            .withScoringRationale(scoreRationale)
            .withScoringDimensions(buildScoreInfoNode(score, "overall", scoreStatus));
        updatedResponseBuilder
            .withResponse(StringUtils.replace(responseUpdate.getValue(), "\\", "\\\\"))
            .withScore(updatedScoreBuilder.build());

        examItemCommandRepository.insertResponses(updatedResponseBuilder.build());

        final ExamPage examPage = examPageService.find(existingExamItem.getExamPageId())
            .orElseThrow(() -> new ReturnStatusException("Exam page is no longer present for the updated item"));

        final ExamPage updatedExamPage = ExamPage.Builder.fromExamPage(examPage)
            .withDuration(pageDuration + examPage.getDuration()) // Accumulate the duration
            .build();

        examPageService.update(updatedExamPage);
        return new ReturnStatus("updated");
    }

    @Override
    public ReturnStatus updateItemScore(final UUID examId,
                                        final IItemResponseScorable responseScorable,
                                        final int score,
                                        final String scoreStatus,
                                        final String scoreRationale,
                                        final String scoreDimensions) throws ReturnStatusException {
        ReturnStatus returnStatus = new ReturnStatus("updated");
        Optional<ExamItem> maybeItem = examItemQueryRepository.findExamItemAndResponse(examId, responseScorable.getPosition());
        if (!maybeItem.isPresent()) {
            throw new ReturnStatusException(String.format("The item does not exist at this position in this test opportunity: Position = %d; examId = %s; testeeresponse._efk_ItemKey found is %s",
                responseScorable.getPosition(), examId, maybeItem.map(ExamItem::getItemKey).orElse("null")));
        }

        ExamItem existingItem = maybeItem.get();

        //This simulates the conditional in StudentDLL.S_UpdateItemScore_SP (line 10009) comparing scoreMark and Sequence which will not match in our system
        //if the item does not have a response or score
        if (existingItem.getResponse().isPresent()
            && existingItem.getResponse().get().getScore().isPresent()
            && DbComparator.isEqual(responseScorable.getScoreMark(), existingItem.getResponse().get().getScore().get().getScoreMark())
            && DbComparator.isEqual(responseScorable.getSequence(), existingItem.getResponse().get().getSequence())) {

            Instant now = Instant.now();
            ExamItemResponse existingResponse = existingItem.getResponse().get();
            ExamItemResponseScore existingScore = existingItem.getResponse().get().getScore().get();

            ExamItemResponseScore updatedScore = ExamItemResponseScore.Builder
                .fromExamItemResponseScore(existingScore)
                .withScoredAt(now)
                .withScoreLatency(now.getMillis() - existingScore.getScoreSentAt().getMillis())
                .withScore(score)
                .withScoringStatus(ExamScoringStatus.fromType(scoreStatus))
                .withScoringRationale(scoreRationale)
                .withScoringDimensions(scoreDimensions)
                .build();

            ExamItemResponse updatedResponse = ExamItemResponse.Builder
                .fromExamItemResponse(existingResponse)
                .withScore(updatedScore)
                .build();

            examItemCommandRepository.insertResponses(updatedResponse);

        } else {
            returnStatus.setStatus("failed");
            returnStatus.setAppKey("No such item: " + responseScorable.getPosition());
            returnStatus.setContext("UpdateItemScore");
        }

        return returnStatus;
    }

    //Pulled directly from StudentDLL
    private String buildScoreInfoNode(Integer score, String scoreDimConstant, String scoreStatus) {
        // (SELECT Score AS "@scorePoint" ,'overall' AS
        // "@scoreDimension",scorestatus AS "@scoreStatus",
        // Example
        // <ScoreInfo scorePoint="1" scoreDimension="overall" scoreStatus="Scored">
        return String.format("<ScoreInfo scorePoint=\"%d\" scoreDimension=\"%s\" scoreStatus=\"%s\"><SubScoreList /></ScoreInfo>",
            score, scoreDimConstant, scoreStatus);
    }
}
