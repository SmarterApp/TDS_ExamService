package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamineeService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class ExamineeAttributesExpandableExamMapper implements ExpandableExamMapper {
    private final ExamineeService examineeService;

    @Autowired
    public ExamineeAttributesExpandableExamMapper(final ExamineeService examineeService) {
        this.examineeService=  examineeService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_EXAMINEE_ATTRIBUTES_AND_RELATIONSHIPS)) {
            return;
        }

        examBuilders.forEach((examId, examBuilder) -> {
            List<ExamineeAttribute> examineeAttributes = examineeService.findAllAttributes(examId, ExamineeContext.FINAL);
            List<ExamineeRelationship> examineeRelationships = examineeService.findAllRelationships(examId, ExamineeContext.FINAL);

            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamineeAttributes(examineeAttributes);
            builder.withExamineeRelationship(examineeRelationships);
        });
    }
}
