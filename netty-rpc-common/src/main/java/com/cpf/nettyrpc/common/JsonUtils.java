package com.cpf.nettyrpc.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;

/**
 * @author jiyingdabj
 */
public class JsonUtils {

    public static IgnorableObjectMapper objectMapper = new IgnorableObjectMapper();

    public static String writeValue(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "";
        }
    }

    public static <T> T readValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T readValue(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    static {
        objectMapper.registerModule(new Jdk8Module());
    }

    static class IgnorableObjectMapper extends ObjectMapper {

        private static final long serialVersionUID = -6685212490183553479L;

        public IgnorableObjectMapper() {
            this.setConfig(this.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
            this.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }
    }
}
