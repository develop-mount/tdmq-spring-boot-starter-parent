package com.seelyn.tdmq.utils;

import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;

public class ResolvableTypeUtils {

    /**
     * 获取泛型
     *
     * @param resolvableType 可分解的类型
     * @return 类型集合
     */
    public static List<Class<?>> getResolvableType(ResolvableType resolvableType) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(resolvableType.resolve());
        if (resolvableType.hasGenerics()) {
            classes.addAll(getResolvableType(resolvableType.getGeneric(0)));
        }
        return classes;
    }

}
