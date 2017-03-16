package tds.exam.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface VerifyAccess {
    /**
     * The paramter name used to send the sessionId UUID in the HTTP Request.  Defaults to "sessionId"
     * @return Paramter name
     */
    String sessionParamName() default "sessionId";

    /**
     * The paramter name used to send the browserId UUID in the HTTP Request.  Defaults to "browserId"
     * @return Paramter name
     */
    String browserParamName() default "browserId";
}
