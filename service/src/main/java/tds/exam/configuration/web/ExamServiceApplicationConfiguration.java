package tds.exam.configuration.web;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import tds.common.configuration.CacheConfiguration;
import tds.common.configuration.RedisClusterConfiguration;
import tds.common.configuration.RestTemplateConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.configuration.item.selection.ItemSelectionConfiguration;

/**
 * Configuration for Exam microservice.
 */
@Configuration
@Import({
    ExceptionAdvice.class,
    RestTemplateConfiguration.class,
    RedisClusterConfiguration.class,
    CacheConfiguration.class,
    SecurityConfiguration.class,
    ItemSelectionConfiguration.class
})
public class ExamServiceApplicationConfiguration {
}