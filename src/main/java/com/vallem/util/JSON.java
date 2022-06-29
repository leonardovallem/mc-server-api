package com.vallem.util;

import com.google.gson.Gson;
import io.activej.common.function.FunctionEx;

public class JSON {
    private final static Gson gson = new Gson();

    public static String stringify(Object obj) {
        if (obj == null) return gson.toJson("");
        return gson.toJson(obj);
    }

    public static <T> T parse(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}
