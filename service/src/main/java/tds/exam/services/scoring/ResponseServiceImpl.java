package tds.exam.services.scoring;

import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamService;
import tds.score.model.ExamInstance;
import tds.score.services.ResponseService;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.IItemResponseUpdate;

import static tds.exam.ExamStatusCode.STATUS_REVIEW;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_ENTRY;
import static tds.exam.ExamStatusCode.STATUS_SEGMENT_EXIT;
import static tds.exam.ExamStatusCode.STATUS_STARTED;

public class ResponseServiceImpl implements ResponseService {
    private final ExamService examService;
    private final ConfigService configService;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamItemQueryRepository examItemQueryRepository;

    private static final Set<String> VALID_EXAM_STATUS_CODES = Sets.newHashSet(STATUS_STARTED, STATUS_REVIEW, STATUS_SEGMENT_ENTRY, STATUS_SEGMENT_EXIT);

    @Autowired
    public ResponseServiceImpl(final ExamService examService, final ConfigService configService, final ExamItemCommandRepository examItemCommandRepository, final ExamItemQueryRepository examItemQueryRepository) {
        this.examService = examService;
        this.configService = configService;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examItemQueryRepository = examItemQueryRepository;
    }

    @Transactional
    @Override
    public ReturnStatus updateScoredResponse(final ExamInstance examInstance,
                                             final IItemResponseUpdate responseUpdate,
                                             final int score, final String scoreStatus,
                                             final String scoreRationale,
                                             final long scoreLatency,
                                             final Float pageDuration) throws ReturnStatusException {
        //Replaces the implementation in StudentDLL.T_UpdateScoredResponse_common
        //Decision was made to not worry about verify access.  This should be done prior to this call.

        Optional<Exam> maybeExam = examService.findExam(examInstance.getExamId());
        if(!maybeExam.isPresent()) {
            throw new ReturnStatusException(String.format("Could not find exam %s associated with response", examInstance.getExamId()));
        }

        Exam exam = maybeExam.get();

        if(!VALID_EXAM_STATUS_CODES.contains(exam.getStatus().getCode())) {
            String message = configService.getFormattedMessage(examInstance.getClientName(), "T_UpdateScoredResponse", "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.");
            throw new ReturnStatusException(message);
        }

        Optional<ExamItem > maybeItem = examItemQueryRepository.findExamItemAndResponse(examInstance.getExamId(), responseUpdate.getPosition());



        return null;
    }

    @Override
    public ReturnStatus updateItemScore(final UUID oppKey,
                                        final IItemResponseScorable responseScorable,
                                        final int score, final String scoreStatus,
                                        final String scoreRationale,
                                        final String scoreDimensions) throws ReturnStatusException {
        return null;
    }
}
