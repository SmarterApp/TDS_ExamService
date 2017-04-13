package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamItem;
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
import tds.exam.services.ExamItemResponseScoringService;
import tds.exam.services.ExamItemService;

import static tds.exam.error.ValidationErrorCode.EXAM_ITEM_DOES_NOT_EXIST;
import static tds.exam.error.ValidationErrorCode.EXAM_ITEM_RESPONSE_DOES_NOT_EXIST;

@Service
public class ExamItemServiceImpl implements ExamItemService {
    private final ExamItemQueryRepository examItemQueryRepository;
    private final ExamItemCommandRepository examItemCommandRepository;
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamQueryRepository examQueryRepository;
    private final ExamItemResponseScoringService examItemResponseScoringService;

    @Autowired
    public ExamItemServiceImpl(final ExamItemQueryRepository examItemQueryRepository,
                               final ExamItemCommandRepository examItemCommandRepository,
                               final ExamPageCommandRepository examPageCommandRepository,
                               final ExamPageQueryRepository examPageQueryRepository,
                               final ExamQueryRepository examQueryRepository,
                               final ExamItemResponseScoringService examItemResponseScoringService) {
        this.examItemQueryRepository = examItemQueryRepository;
        this.examItemCommandRepository = examItemCommandRepository;
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examQueryRepository = examQueryRepository;
        this.examItemResponseScoringService = examItemResponseScoringService;
    }

    @Transactional
    @Override
    public Response<ExamPage> insertResponses(final UUID examId, final int mostRecentPagePosition, final ExamItemResponse... responses) {
        Exam exam = examQueryRepository.getExamById(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam for exam id %s", examId)));


        // RULE:  An exam must be in the "started" or "review" status for responses to be saved.  Legacy rule location:
        // StudentDLL.T_UpdateScoredResponse_common, line 2031
        if (!(exam.getStatus().getCode().equals(ExamStatusCode.STATUS_STARTED)
            || exam.getStatus().getCode().equals(ExamStatusCode.STATUS_REVIEW))) {
            return new Response<>(new ValidationError(ValidationErrorCode.EXAM_INTERRUPTED, "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test."));
        }

        // Get the current page, which will be used as the basis for creating the next page
        ExamPage currentPage = examPageQueryRepository.find(examId, mostRecentPagePosition)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find exam page for id %s and position %d", examId, mostRecentPagePosition)));

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

        ExamPage nextPage = new ExamPage.Builder()
            .fromExamPage(currentPage)
            .withPagePosition(mostRecentPagePosition + 1)
            .build();

        examPageCommandRepository.insert(nextPage);

        return new Response<>(nextPage);
    }

    @Override
    public int getExamPosition(final UUID examId) {
        return examItemQueryRepository.getCurrentExamItemPosition(examId);
    }

    @Override
    public Map<UUID, Integer> getResponseCounts(final UUID... examIds) {
        return examItemQueryRepository.getResponseCounts(examIds);
    }

    @Override
    public Optional<ValidationError> markForReview(final UUID id, final int position, final Boolean mark) {
        Optional<ExamItem> maybeExamItem = examItemQueryRepository.findExamItemAndResponse(id, position);

        if (!maybeExamItem.isPresent()) {
            return Optional.of(new ValidationError(EXAM_ITEM_DOES_NOT_EXIST,
                String.format("No exam item found for exam id '%s' at position '%s'.", id, position)));
        }

        ExamItem examItem = maybeExamItem.get();

        if (!examItem.getResponse().isPresent()) {
            return Optional.of(new ValidationError(EXAM_ITEM_RESPONSE_DOES_NOT_EXIST,
                String.format("No exam response found for exam id '%s' at item position '%s'.", id, position)));
        }

        ExamItemResponse updatedResponse = ExamItemResponse.Builder
            .fromExamItemResponse(examItem.getResponse().get())
            .withMarkedForReview(mark)
            .build();

        examItemCommandRepository.insertResponses(updatedResponse);

        return Optional.empty();
    }
}
