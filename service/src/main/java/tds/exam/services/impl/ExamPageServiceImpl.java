package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamPageService;

@Service
public class ExamPageServiceImpl implements ExamPageService {
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;
    private final ExamApprovalService examApprovalService;

    @Autowired
    public ExamPageServiceImpl(ExamPageQueryRepository examPageQueryRepository,
                               ExamPageCommandRepository examPageCommandRepository,
                               ExamApprovalService examApprovalService) {
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
        this.examApprovalService = examApprovalService;
    }

    @Transactional
    @Override
    public void insertPages(final ExamPage... examPages) {
        examPageCommandRepository.insert(examPages);
    }

    @Transactional
    @Override
    public void deletePages(final UUID examId) {
        examPageCommandRepository.deleteAll(examId);
    }

    @Override
    public List<ExamPage> findAllPages(final UUID examId) {
        return examPageQueryRepository.findAll(examId);
    }

    @Override
    public Response<ExamPage> getPage(final ExamInfo request, final int pageNumber) {
        Response<ExamApproval> approval = examApprovalService.getApproval(request);

        if (approval.getError().isPresent()) {
            return new Response<>(approval.getError().get());
        }

        ExamPage examPage = examPageQueryRepository.findPageWithItems(request.getExamId(), pageNumber)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find an exam page for exam id %s and page number/position %s", request.getExamId(), pageNumber)));

        // Update the exam page to record the started_at time, which can be used for measuring the amount of time a
        // student spends responding to items on a page.  Consider the time this page of items was fetched from the
        // database as the start time.
        examPageCommandRepository.update(examPage);

        return new Response<>(examPage);
    }
}
