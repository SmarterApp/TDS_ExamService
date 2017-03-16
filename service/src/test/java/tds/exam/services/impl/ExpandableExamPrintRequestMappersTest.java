package tds.exam.services.impl;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Set;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExpandableExamPrintRequestMapper;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpandableExamPrintRequestMappersTest {
    private ExpandableExamPrintRequestMapper examExpandableExamPrintRequestMapper;

    private final Set<String> expandableAttributes = ImmutableSet.of(
        ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM
    );

    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Before
    public void setup() {
        examExpandableExamPrintRequestMapper = new ExamExpandableExamPrintRequestMapper(mockExamQueryRepository);
    }

    @Test
    public void shouldMapExamToExamPrintRequest() {
        ExamPrintRequest request = random(ExamPrintRequest.class);
        ExpandableExamPrintRequest.Builder builder = new ExpandableExamPrintRequest.Builder(request);
        Exam exam = new ExamBuilder().build();

        when(mockExamQueryRepository.getExamById(request.getExamId())).thenReturn(Optional.of(exam));
        examExpandableExamPrintRequestMapper.updateExpandableMapper(expandableAttributes, builder, request.getExamId());

        verify(mockExamQueryRepository).getExamById(request.getExamId());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionForRequestWithNoExam() {
        ExamPrintRequest request = random(ExamPrintRequest.class);
        ExpandableExamPrintRequest.Builder builder = new ExpandableExamPrintRequest.Builder(request);

        when(mockExamQueryRepository.getExamById(request.getExamId())).thenReturn(Optional.empty());
        examExpandableExamPrintRequestMapper.updateExpandableMapper(expandableAttributes, builder, request.getExamId());
    }

}
