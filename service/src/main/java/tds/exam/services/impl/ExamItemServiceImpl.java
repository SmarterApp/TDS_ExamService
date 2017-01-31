package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamItemResponse;
import tds.exam.ExamStatusCode;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamItemResponseCommandRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.ExamResponseQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamItemService;

@Service
public class ExamItemServiceImpl implements ExamItemService {
    private final ExamResponseQueryRepository examResponseQueryRepository;
    private final ExamItemResponseCommandRepository examItemResponseCommandRepository;
    private final ExamQueryRepository examQueryRepository;
    private final ExamApprovalService examApprovalService;

    @Autowired
    public ExamItemServiceImpl(ExamResponseQueryRepository examResponseQueryRepository,
                               ExamItemResponseCommandRepository examItemResponseCommandRepository,
                               ExamQueryRepository examQueryRepository,
                               ExamApprovalService examApprovalService) {
        this.examResponseQueryRepository = examResponseQueryRepository;
        this.examItemResponseCommandRepository = examItemResponseCommandRepository;
        this.examQueryRepository = examQueryRepository;
        this.examApprovalService = examApprovalService;
    }

    @Override
    public Response<String> insertResponses(ApprovalRequest request, ExamItemResponse... responses) {
        Response<ExamApproval> approval = examApprovalService.getApproval(request);

        if (approval.getError().isPresent()) {
            return new Response<>(approval.getError().get());
        }

        Exam exam = examQueryRepository.getExamById(request.getExamId())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page for exam id %s", request.getExamId())));

        // RULE:  An exam must be in the "started" or "review" status for responses to be saved.  Legacy rule location:
        // StudentDLL.T_UpdateScoredResponse_common, line 2031
        if (!exam.getStatus().getCode().equals(ExamStatusCode.STATUS_STARTED)
            || !exam.getStatus().getCode().equals(ExamStatusCode.STATUS_REVIEW)) {
            return new Response<>(new ValidationError(ValidationErrorCode.EXAM_INTERRUPTED, "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test."));
        }

        examItemResponseCommandRepository.insert(responses);

        // TODO:  Should the next page get built and saved here?  I.e., should I call ExamPageService.insertPages for the prefetch amount?

        return new Response<>("");
    }

    @Override
    public int getExamPosition(UUID examId) {
        return examResponseQueryRepository.getCurrentExamItemPosition(examId);
    }
}
