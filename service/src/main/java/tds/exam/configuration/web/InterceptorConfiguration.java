package tds.exam.configuration.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import tds.exam.services.ExamApprovalService;
import tds.exam.web.interceptors.EventLoggerInterceptor;
import tds.exam.web.interceptors.VerifyAccessInterceptor;

/**
 * Configure additional interceptors for Spring
 */
@Configuration
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {

    private final ExamApprovalService examApprovalService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public InterceptorConfiguration(ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new EventLoggerInterceptor(new ObjectMapper())).addPathPatterns("/exam/**");
        registry.addInterceptor(new VerifyAccessInterceptor(examApprovalService)).addPathPatterns("/exam/**");
    }
}
