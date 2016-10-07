package tds.exam.web.resources;

import org.springframework.hateoas.ResourceSupport;
import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamApproval;
import tds.exam.web.endpoints.ExamController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * A HATEOAS representation of an {@link tds.exam.ExamApproval}
 */
public class ExamApprovalResource extends ResourceSupport {
    private ExamApproval examApproval;
    private ValidationError[] errors;

    public ExamApprovalResource(Response<ExamApproval> examApprovalResponse) {

        if (examApprovalResponse.getData().isPresent()) {
            this.examApproval = examApprovalResponse.getData().get();
            this.add(linkTo(
                    methodOn(ExamController.class)
                            .getExamById(examApproval.getExamId()))
                    .withRel("exam"));
        }

        if (examApprovalResponse.getErrors().isPresent()) {
            this.errors = examApprovalResponse.getErrors().get();
        }
    }

    public ExamApproval getExamApproval() {
        return examApproval;
    }

    public ValidationError[] getErrors() {
        return errors;
    }
}
