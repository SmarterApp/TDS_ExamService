package tds.exam;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.junit4.SpringRunner;

import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.configuration.web.InterceptorConfiguration;

@WebMvcTest(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {InterceptorConfiguration.class})})
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class, SecurityConfiguration.class})
public @interface WebMvcControllerIntegrationTest {
    @AliasFor(
        annotation = WebMvcTest.class,
        attribute = "value"
    )
    Class<?>[] controllers() default {};
}
