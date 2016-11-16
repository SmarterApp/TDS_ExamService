package tds.exam.configuration.web;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import tds.common.configuration.RestTemplateConfiguration;
import tds.common.web.advice.ExceptionAdvice;

/**
 * Configuration for Exam microservice.
 */
@Configuration
@Import({ExceptionAdvice.class, RestTemplateConfiguration.class})
public class ExamServiceApplicationConfiguration {

}