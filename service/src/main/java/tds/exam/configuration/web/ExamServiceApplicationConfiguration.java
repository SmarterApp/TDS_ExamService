package tds.exam.configuration.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import tds.common.configuration.CacheConfiguration;
import tds.common.configuration.RestTemplateConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.utils.VerifyAccessInterceptor;

/**
 * Configuration for Exam microservice.
 */
@Configuration
@Import({
    ExceptionAdvice.class,
    RestTemplateConfiguration.class,
    CacheConfiguration.class,
    SecurityConfiguration.class
})
public class ExamServiceApplicationConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifyAccessInterceptor()).addPathPatterns("/**");;
    }

    @Bean
    public VerifyAccessInterceptor verifyAccessInterceptor() {
        return new VerifyAccessInterceptor();
    }

}