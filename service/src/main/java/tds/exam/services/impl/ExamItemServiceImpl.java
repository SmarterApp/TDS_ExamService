package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Stream;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamItemResponseScoringService;
import tds.exam.services.ExamItemService;

@Service
public class ExamItemServiceImpl implements ExamItemService {
    private final ExamItemQueryRepository examItemQueryRepository;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamQueryRepository examQueryRepository;
    private final ExamApprovalService examApprovalService;
    private final ExamItemResponseScoringService examItemResponseScoringService;

    @Autowired
    public ExamItemServiceImpl(ExamItemQueryRepository examItemQueryRepository,
                               ExamItemCommandRepository examItemCommandRepository,
                               ExamPageCommandRepository examPageCommandRepository,
                               ExamPageQueryRepository examPageQueryRepository,
                               ExamQueryRepository examQueryRepository,
                               ExamApprovalService examApprovalService,
                               ExamItemResponseScoringService examItemResponseScoringService) {
        this.examItemQueryRepository = examItemQueryRepository;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examQueryRepository = examQueryRepository;
        this.examApprovalService = examApprovalService;
        this.examItemResponseScoringService = examItemResponseScoringService;
    }

    @Override
    public Response<ExamPage> insertResponses(final ApprovalRequest request, final int mostRecentPagePosition, final ExamItemResponse... responses) {
        Response<ExamApproval> approval = examApprovalService.getApproval(request);
        if (approval.getError().isPresent()) {
            return new Response<>(approval.getError().get());
        }

        Exam exam = examQueryRepository.getExamById(request.getExamId())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam for exam id %s", request.getExamId())));

        // RULE:  An exam must be in the "started" or "review" status for responses to be saved.  Legacy rule location:
        // StudentDLL.T_UpdateScoredResponse_common, line 2031
        if (!(exam.getStatus().getCode().equals(ExamStatusCode.STATUS_STARTED)
            || exam.getStatus().getCode().equals(ExamStatusCode.STATUS_REVIEW))) {
            return new Response<>(new ValidationError(ValidationErrorCode.EXAM_INTERRUPTED, "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test."));
        }

        // Score each response
        // TODO:  Revisit this once scoring logic has been ported over; getting a score may be more complex and/or require more data
        ExamItemResponse[] scoredResponses = Stream.of(responses).map(response -> {
            ExamItemResponseScore score = examItemResponseScoringService.getScore(response);

            return new ExamItemResponse.Builder()
                .fromExamItemResponse(response)
                .withScore(score)
                .build();
        }).toArray(ExamItemResponse[]::new);

        examItemCommandRepository.insertResponses(scoredResponses);

        // Create a record for the next page
        ExamPage currentPage = examPageQueryRepository.find(request.getExamId(), mostRecentPagePosition)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find exam page for id %s and position %d", request.getExamId(), mostRecentPagePosition)));

        ExamPage nextPage = new ExamPage.Builder()
            .fromExamPage(currentPage)
            .withPagePosition(mostRecentPagePosition + 1)
            .build();

        examPageCommandRepository.insert(nextPage);

        return new Response<>(nextPage);
    }

    @Override
    public int getExamPosition(UUID examId) {
        return examItemQueryRepository.getCurrentExamItemPosition(examId);
    }
}
