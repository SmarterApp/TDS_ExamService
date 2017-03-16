package tds.exam.web.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.services.ExamApprovalService;
import tds.exam.web.annotations.VerifyAccess;
import tds.exam.web.exceptions.ValidationException;

/**
 * Verifies access for a user based on the ExamId, BrowserId and SessionId.
 * This is used when a controller endpoint uses the {@link tds.exam.web.annotations.VerifyAccess} annotation
 * The controller URL must follow the convention /exam/{examId}/[path]?sessionId=xxx&browserId=yyy
 */
public class VerifyAccessInterceptor extends HandlerInterceptorAdapter {
    private ExamApprovalService examApprovalService;

    public VerifyAccessInterceptor(final ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ValidationException, IllegalArgumentException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        VerifyAccess verifyAccessAnnotation = ((HandlerMethod)handler).getMethodAnnotation(VerifyAccess.class);

        if (verifyAccessAnnotation == null) {
            return true;
        }

        // @VerifyAccess will only work on URLs that follow the convention /exam/{examId}/...., otherwise the examId won't be found
        String[] pathParts = request.getRequestURI().split("/");
        if (!pathParts[1].equals("exam")) {
            throw new IllegalArgumentException(String.format("VerifyAccess: Exam ID could not be found for url %s.", request.getRequestURI()));
        }

        UUID examId = UUID.fromString(pathParts[2]);
        UUID sessionId = null;
        UUID browserId = null;

        Map<String, String[]> parameters = request.getParameterMap();

        if (parameters.containsKey(verifyAccessAnnotation.sessionParamName())) {
            sessionId = UUID.fromString(parameters.get(verifyAccessAnnotation.sessionParamName())[0]);
        }

        if (parameters.containsKey(verifyAccessAnnotation.browserParamName())) {
            browserId = UUID.fromString(parameters.get(verifyAccessAnnotation.browserParamName())[0]);
        }

        if (sessionId == null || browserId == null) {
            throw new IllegalArgumentException("VerifyAccess: The browser and session IDs are required.");
        }

        Response<ExamApproval> approval = examApprovalService.getApproval(new ExamInfo(examId, sessionId, browserId));

        if (approval.getError().isPresent()) {
            throw new ValidationException(approval.getError().get());
        }

        return true;
    }
}
