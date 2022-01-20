package com.seelyn.tdmq.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TdmqTopic {

    /**
     * topic
     *
     * @return 主题
     */
    String topic();

    /**
     * eg. tag1,tag2
     *
     * @return tags
     */
    String tags() default "";

}
