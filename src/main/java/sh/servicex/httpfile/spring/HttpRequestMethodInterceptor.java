package sh.servicex.httpfile.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HttpRequestMethodInterceptor implements MethodInterceptor {
    private final Map<Method, HttpRequestStub> httpServiceMethods;

    public HttpRequestMethodInterceptor(List<HttpRequestStub> methods) {
        this.httpServiceMethods = methods.stream()
                .collect(Collectors.toMap(HttpRequestStub::getMethod, Function.identity()));
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        HttpRequestStub httpRequestStub = this.httpServiceMethods.get(method);
        if (httpRequestStub != null) {
            return httpRequestStub.invoke(invocation.getArguments());
        }
        if (method.isDefault()) {
            if (invocation instanceof ReflectiveMethodInvocation reflectiveMethodInvocation) {
                Object proxy = reflectiveMethodInvocation.getProxy();
                return InvocationHandler.invokeDefault(proxy, method, invocation.getArguments());
            }
        }
        throw new IllegalStateException("Unexpected method invocation: " + method);
    }
}
