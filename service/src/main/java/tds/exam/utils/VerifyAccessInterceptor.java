package tds.exam.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.services.ExamApprovalService;

public class VerifyAccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ExamApprovalService examApprovalService;

    @Autowired
    private ObjectMapper objectMapper;

//    @Autowired
//    public VerifyAccessInterceptor(final ExamApprovalService examApprovalService) {
//        this.examApprovalService = examApprovalService;
//    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod)handler;
        VerifyAccess verifyAccessAnnotation = handlerMethod.getMethodAnnotation(VerifyAccess.class);

        if (verifyAccessAnnotation == null) {
            return true;
        }

        // examId MUST be at /exam/{examId}/.... ALWAYS
        String[] pathParts = request.getRequestURI().split("/");
        if (!pathParts[1].equals("exam")) {
            return false; // Should throw error of some kind?
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
            return false;
        }

        /*
        * This method is a port of the legacy OpportunityRepository.getOpportunitySegments() [241] and
        *  StudentDLL.T_GetOpportunitySegments_SP [10212]
        * ValidateItemsAccess_FN() in StudentDLL [10214]
        */
        Response errorResponse = null;
        try {
            Response<ExamApproval> approval = examApprovalService.getApproval(new ExamInfo(examId, sessionId, browserId));

            if (approval.getError().isPresent()) {
                errorResponse = new Response<>(approval.getError().get());
            }
        }
        catch (IllegalArgumentException ex) {
            errorResponse = new Response<>(new ValidationError("400", ex.getMessage()));
        }

        if (errorResponse != null) {
            response.setStatus(400);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String json = objectMapper.writeValueAsString(errorResponse.getError());

            try (ServletOutputStream output = response.getOutputStream()) {
                output.write(json.getBytes());
            }

            return false;
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
