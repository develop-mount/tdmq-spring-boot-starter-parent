package com.seelyn.tdmq.utils;

import org.apache.pulsar.client.api.Schema;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author linfeng
 */
public enum SchemaUtils {

    /**
     * Schema
     */
    BYTES(byte[].class.getName(), Schema.BYTES),
    BYTE_BUFFER(ByteBuffer.class.getName(), Schema.BYTEBUFFER),
    STRING(String.class.getName(), Schema.STRING),
    BYTE(Byte.class.getName(), Schema.INT8),
    SHORT(Short.class.getName(), Schema.INT16),
    INTEGER(Integer.class.getName(), Schema.INT32),
    LONG(Long.class.getName(), Schema.INT64),
    BOOL(Boolean.class.getName(), Schema.BOOL),
    FLOAT(Float.class.getName(), Schema.FLOAT),
    DOUBLE(Double.class.getName(), Schema.DOUBLE),
    DATE(Date.class.getName(), Schema.DATE),
    TIME(java.sql.Time.class.getName(), Schema.TIME),
    TIMESTAMP(java.sql.Timestamp.class.getName(), Schema.TIMESTAMP);

    private String className;
    private Schema schema;

    /**
     * 构造函数
     * @param className 类名称
     * @param schema schema
     */
    SchemaUtils(String className, Schema schema) {
        this.className = className;
        this.schema = schema;
    }

    /**
     * 得到Schema
     * @param clazz 类
     * @param <T> 泛型
     * @return Schema
     */
    public static <T> Schema<T> getSchema(Class<T> clazz) {
        for (SchemaUtils schemaUtils : SchemaUtils.values()) {
            if (schemaUtils.className.equals(clazz.getName())) {
                //noinspection unchecked
                return (Schema<T>) schemaUtils.schema;
            }
        }
        return Schema.JSON(clazz);
    }
}
