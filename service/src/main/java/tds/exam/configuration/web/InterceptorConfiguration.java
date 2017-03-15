package tds.exam.configuration.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import tds.exam.utils.VerifyAccessInterceptor;

@Configuration
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifyAccessInterceptor()).addPathPatterns("/**");;
    }

    @Bean
    public VerifyAccessInterceptor verifyAccessInterceptor() {
        return new VerifyAccessInterceptor();
    }
}
