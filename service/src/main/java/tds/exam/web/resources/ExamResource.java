package tds.exam.web.resources;

import org.springframework.hateoas.ResourceSupport;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.Exam;
import tds.exam.web.endpoints.ExamController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ExamResource extends ResourceSupport {
    private Exam exam;
    private ValidationError[] errors;

    public ExamResource(Exam exam) {
        this.exam = exam;
        addSelfLink(exam);
    }

    public ExamResource(Response<Exam> examResponse) {
        if(examResponse.getData().isPresent()) {
            this.exam = examResponse.getData().get();
            addSelfLink(exam);
        }

        if(examResponse.getErrors().isPresent()) {
            errors = examResponse.getErrors().get();
        }
    }

    public Exam getExam() {
        return exam;
    }

    public ValidationError[] getErrors() {
        return errors;
    }

    private void addSelfLink(Exam exam) {
        this.add(linkTo(
            methodOn(ExamController.class)
                .getExamById(exam.getId()))
            .withSelfRel());
    }
}
