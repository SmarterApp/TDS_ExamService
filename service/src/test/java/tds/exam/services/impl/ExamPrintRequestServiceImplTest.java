package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.services.ExamPrintRequestService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPrintRequestServiceImplTest {
    private ExamPrintRequestService examPrintRequestService;

    @Mock
    private ExamPrintRequestQueryRepository mockExamPrintRequestQueryRepository;

    @Mock
    private ExamPrintRequestCommandRepository mockExamPrintRequestCommandRepository;

    @Captor
    ArgumentCaptor<ExamPrintRequest> examPrintRequestArgumentCaptor;

    @Before
    public void setup() {
        examPrintRequestService = new ExamPrintRequestServiceImpl(mockExamPrintRequestCommandRepository,
            mockExamPrintRequestQueryRepository);
    }

    @Test
    public void shouldCreateExamPrintRequest() {
        ExamPrintRequest examPrintRequest = random(ExamPrintRequest.class);
        examPrintRequestService.insert(examPrintRequest);
        verify(mockExamPrintRequestCommandRepository).insert(examPrintRequest);
    }

    @Test
    public void shouldReturnListOfUnfulfilledExamPrintRequests() {
        List<ExamPrintRequest> examPrintRequests = Arrays.asList(random(ExamPrintRequest.class));
        when(mockExamPrintRequestQueryRepository.findUnfulfilledRequests(isA(UUID.class), isA(UUID.class)))
            .thenReturn(examPrintRequests);
        List<ExamPrintRequest> retRequests = examPrintRequestService.findUnfulfilledRequests(UUID.randomUUID(), UUID.randomUUID());
        verify(mockExamPrintRequestQueryRepository).findUnfulfilledRequests(isA(UUID.class), isA(UUID.class));

        assertThat(retRequests).isEqualTo(examPrintRequests);
    }

    @Test
    public void shouldDenyPrintRequest() {
        final String denyReason = "You enjoyed the movie 'Cloud Atlas'";
        final ExamPrintRequest request = random(ExamPrintRequest.class);

        when(mockExamPrintRequestQueryRepository.findExamPrintRequest(request.getId())).thenReturn(Optional.of(request));
        examPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.DENIED, request.getId(), denyReason);
        verify(mockExamPrintRequestCommandRepository).update(examPrintRequestArgumentCaptor.capture());
        ExamPrintRequest printRequest = examPrintRequestArgumentCaptor.getValue();

        assertThat(printRequest.getId()).isEqualTo(request.getId());
        assertThat(printRequest.getReasonDenied()).isEqualTo(denyReason);
        assertThat(printRequest.getStatus()).isEqualTo(ExamPrintRequestStatus.DENIED);
        assertThat(printRequest.getChangedAt()).isNotNull();
    }

    @Test
    public void shouldFindAllApprovedPrintRequests() {
        final UUID sessionId = UUID.randomUUID();
        ExamPrintRequest request1 = random(ExamPrintRequest.class);
        ExamPrintRequest request2 = random(ExamPrintRequest.class);

        when(mockExamPrintRequestQueryRepository.findApprovedRequests(sessionId)).thenReturn(Arrays.asList(request1, request2));
        List<ExamPrintRequest> examPrintRequests = examPrintRequestService.findApprovedRequests(sessionId);
        assertThat(examPrintRequests).containsExactly(request1, request2);
        verify(mockExamPrintRequestQueryRepository).findApprovedRequests(sessionId);
    }

    @Test
    public void shouldFindAndApprovePrintRequest() {
        final UUID id = UUID.randomUUID();
        final ExamPrintRequest request = random(ExamPrintRequest.class);
        when(mockExamPrintRequestQueryRepository.findExamPrintRequest(id)).thenReturn(Optional.of(request));

        Optional<ExamPrintRequest> maybeApprovedRequest = examPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null);
        assertThat(maybeApprovedRequest).isPresent();

        verify(mockExamPrintRequestQueryRepository).findExamPrintRequest(id);
        verify(mockExamPrintRequestCommandRepository).update(examPrintRequestArgumentCaptor.capture());

        ExamPrintRequest approvedRequest = examPrintRequestArgumentCaptor.getValue();
        assertThat(approvedRequest.getId()).isEqualTo(request.getId());
        assertThat(approvedRequest.getStatus()).isEqualTo(ExamPrintRequestStatus.APPROVED);
        assertThat(approvedRequest.getChangedAt()).isNotNull();
    }

    @Test
    public void shouldReturnEmptyAndNotApproveForNoExamPrintRequestFound() {
        final UUID id = UUID.randomUUID();
        final ExamPrintRequest request = random(ExamPrintRequest.class);
        when(mockExamPrintRequestQueryRepository.findExamPrintRequest(id)).thenReturn(Optional.empty());

        Optional<ExamPrintRequest> maybeApprovedRequest = examPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null);
        assertThat(maybeApprovedRequest).isNotPresent();

        verify(mockExamPrintRequestQueryRepository).findExamPrintRequest(id);
        verify(mockExamPrintRequestCommandRepository, never()).update(isA(ExamPrintRequest.class));
    }
}
