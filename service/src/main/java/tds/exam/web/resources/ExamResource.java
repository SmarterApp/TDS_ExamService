package tds.exam.web.resources;

import org.springframework.hateoas.ResourceSupport;

import tds.exam.Exam;
import tds.exam.web.endpoints.ExamController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ExamResource extends ResourceSupport {
    private final Exam exam;

    public ExamResource(Exam exam) {
        this.exam = exam;
        this.add(linkTo(
            methodOn(ExamController.class)
                .getExamById(exam.getId()))
            .withSelfRel());
    }

    public Exam getExam() {
        return exam;
    }
}
