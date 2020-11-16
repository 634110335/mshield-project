package com.cuisec.mshield.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * JSON 转换工具类
 */
public class JsonUtil {

    /**
     * 将对象转为json
     */
    public static String toJson(Object o) {
        Gson gson = new Gson();
        String str = "";
        if (o != null) {
            str = gson.toJson(o);
        }
        return str;
    }

    /**
     * 将对象转为json
     */
    public static Object toObject(String jsonStr, Type type) {
        try{
            Gson gson = new Gson();
            Object o = null;
            if (jsonStr != null && !"".equals(jsonStr)) {
                o = gson.fromJson(jsonStr, type);
            }
            return o;
        }catch (Exception e){
            return null;
        }
    }
}
