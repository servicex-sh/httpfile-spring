package sh.servicex.httpfile.spring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.invoker.HttpClientAdapter;
import org.springframework.web.service.invoker.HttpRequestValues;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Optional;


public class HttpClientAdapterMock implements HttpClientAdapter {
    private static final String MOCK_RESULT_ATTR_NAME = "mockResult";
    ObjectMapper objectMapper = new ObjectMapper();

    @Nonnull
    @Override
    public Mono<Void> requestToVoid(@Nonnull HttpRequestValues requestValues) {
        return Mono.empty();
    }

    @Nonnull
    @Override
    public Mono<HttpHeaders> requestToHeaders(@Nonnull HttpRequestValues requestValues) {
        return Mono.empty();
    }

    @Nonnull
    @Override
    public <T> Mono<T> requestToBody(HttpRequestValues requestValues, @Nonnull ParameterizedTypeReference<T> bodyType) {
        String body = (String) requestValues.getAttributes().get(MOCK_RESULT_ATTR_NAME);
        TypeReference<T> tr = new TypeReference<>() {
            public Type getType() {
                return bodyType.getType();
            }
        };
        return Mono.create(sink -> {
            try {
                sink.success(objectMapper.readValue(new StringReader(body), tr));
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Nonnull
    @Override
    public <T> Flux<T> requestToBodyFlux(HttpRequestValues requestValues, @Nonnull ParameterizedTypeReference<T> bodyType) {
        String body = (String) requestValues.getAttributes().get(MOCK_RESULT_ATTR_NAME);
        TypeReference<T> tr = new TypeReference<>() {
            public Type getType() {
                return bodyType.getType();
            }
        };
        return Flux.create(sink -> {
            try {
                sink.next(objectMapper.readValue(new StringReader(body), tr));
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Nonnull
    @Override
    public Mono<ResponseEntity<Void>> requestToBodilessEntity(@Nonnull HttpRequestValues requestValues) {
        return Mono.empty();
    }

    @Nonnull
    @Override
    public <T> Mono<ResponseEntity<T>> requestToEntity(HttpRequestValues requestValues, @Nonnull ParameterizedTypeReference<T> bodyType) {
        String body = (String) requestValues.getAttributes().get(MOCK_RESULT_ATTR_NAME);
        TypeReference<T> tr = new TypeReference<>() {
            public Type getType() {
                return bodyType.getType();
            }
        };
        return Mono.create(sink -> {
            try {
                final T resultValue = objectMapper.readValue(new StringReader(body), tr);
                sink.success(ResponseEntity.of(Optional.of(resultValue)));
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Nonnull
    @Override
    public <T> Mono<ResponseEntity<Flux<T>>> requestToEntityFlux(HttpRequestValues requestValues, @Nonnull ParameterizedTypeReference<T> bodyType) {
        String body = (String) requestValues.getAttributes().get(MOCK_RESULT_ATTR_NAME);
        TypeReference<T> tr = new TypeReference<>() {
            public Type getType() {
                return bodyType.getType();
            }
        };
        return Mono.create(sink -> {
            try {
                final T resultValue = objectMapper.readValue(new StringReader(body), tr);
                sink.success(ResponseEntity.of(Optional.of(Flux.just(resultValue))));
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
