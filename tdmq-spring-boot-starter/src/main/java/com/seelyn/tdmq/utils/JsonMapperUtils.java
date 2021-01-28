package com.seelyn.tdmq.utils;

import org.apache.pulsar.shade.com.fasterxml.jackson.core.json.JsonReadFeature;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.JavaType;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class JsonMapperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapperUtils.class);
    private static final JsonMapperUtils jsonMapperUtils = new JsonMapperUtils();
    private final JsonMapper mapper;

    public JsonMapperUtils() {
        this.mapper = JsonMapper.builder()
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public static JsonMapperUtils getInstance() {
        return jsonMapperUtils;
    }

    /**
     * 反序列化POJO或简单Collection如List.
     *
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * 如需反序列化复杂Collection如List, 请使用fromJson(String,JavaType)
     *
     *
     * @param jsonString json 字符串
     * @param clazz      待转化的类型
     * @param <T>        泛型
     * @return 转换后的对象
     */
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (!StringUtils.hasLength(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOGGER.warn("parse json string error: {} :{}", jsonString, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * 字符串转换集合
     *
     * @param jsonStr         json字符串
     * @param collectionClass 集合类型
     * @param elementClasses  元素类型
     * @param <T>             泛型
     * @return 转换后的实例
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T fromJson(String jsonStr, Class collectionClass, Class elementClasses) {
        if (!StringUtils.hasLength(jsonStr)) {
            return null;
        }
        try {
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(collectionClass, elementClasses);
            return mapper.readValue(jsonStr, javaType);
        } catch (IOException e) {
            LOGGER.warn("parse json string error: {} : {}", jsonStr, e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Object可以是POJO，也可以是Collection或数组。
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     *
     * @param object 待转化对象
     * @return json字符串
     */
    public String toJson(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            LOGGER.error("write to json string error: {} : {}", object, e.getLocalizedMessage(), e);
            return "{}";
        }
    }

}
