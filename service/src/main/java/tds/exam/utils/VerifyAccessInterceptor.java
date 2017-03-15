package tds.exam.utils;

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
import tds.exam.web.exceptions.ValidationException;

public class VerifyAccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ExamApprovalService examApprovalService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        VerifyAccess verifyAccessAnnotation = ((HandlerMethod)handler).getMethodAnnotation(VerifyAccess.class);

        if (verifyAccessAnnotation == null) {
            return true;
        }

        // @VerifyAccess will only work on URLs that follow the convention /exam/{examId}/...., otherwise the examId won't be found
        //  providing flexibility to work with /exam/{examId} and /exams/{examId since the plural is more RESTful
        String[] pathParts = request.getRequestURI().split("/");
        if (!pathParts[1].equals("exam") && !pathParts[1].equals("exams")) {
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

        Response<ExamApproval> approval = examApprovalService.getApproval(new ExamInfo(examId, sessionId, browserId));

        if (approval.getError().isPresent()) {
            throw new ValidationException(approval.getError().get());
        }

        return true;
    }
}
