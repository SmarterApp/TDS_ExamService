package tds.exam;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.exam.configuration.web.InterceptorConfiguration;
import tds.exam.web.advice.ExamExceptionAdvice;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WebMvcTest(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {InterceptorConfiguration.class})})
@Import({ExamExceptionAdvice.class, JacksonObjectMapperConfiguration.class, SecurityConfiguration.class})
public @interface WebMvcControllerIntegrationTest {
    @AliasFor(
        annotation = WebMvcTest.class,
        attribute = "value"
    )
    Class<?>[] controllers() default {};
}
