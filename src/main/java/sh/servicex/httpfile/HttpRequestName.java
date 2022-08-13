package sh.servicex.httpfile;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpRequestName {
    /**
     * http request name in http file, e.g. "myIp"
     */
    String value();
}
