package tds.exam.configuration.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import tds.exam.services.ExamApprovalService;
import tds.exam.web.interceptors.VerifyAccessInterceptor;

@Configuration
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {
    private final ExamApprovalService examApprovalService;

    @Autowired
    public InterceptorConfiguration(ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifyAccessInterceptor(examApprovalService)).addPathPatterns("/exam/**");;
    }

    public VerifyAccessInterceptor verifyAccessInterceptor(ExamApprovalService examApprovalService) {
        return new VerifyAccessInterceptor(examApprovalService);
    }
}
