package com.seelyn.tdmq.utils;

import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;

public class ResolvableTypeUtils {

    public static List<Class<?>> getResolvableType(ResolvableType resolvableType){
        List<Class<?>> classes = new ArrayList<>();
        classes.add(resolvableType.resolve());
        if (resolvableType.hasGenerics()) {
            classes.addAll(getResolvableType(resolvableType.getGeneric(0)));
        }
        return classes;
    }

}
