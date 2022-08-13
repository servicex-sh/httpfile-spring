package sh.servicex.httpfile;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpFile {
    /**
     * http file from classpath, e.g. "/httpbin.http"
     */
    String value();
}
