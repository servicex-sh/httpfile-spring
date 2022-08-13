package sh.servicex.httpfile.spring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


public class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static String writeValueAsString(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    public static <T> T readValue(String jsonText, Class<T> valueType) throws IOException {
        return OBJECT_MAPPER.readValue(jsonText, valueType);
    }


}

