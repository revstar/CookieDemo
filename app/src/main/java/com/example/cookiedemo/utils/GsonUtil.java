package com.example.cookiedemo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

public class GsonUtil {
    private static Gson gson = null;

    static {
        GsonBuilder builder = new GsonBuilder();
        //builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Date.class, new DateSerializer());
        gson = builder.create();
    }

    public static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsLong());
        }

    }

    public static class DateSerializer implements JsonSerializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    public static String toJson(Object o){
        return gson.toJson(o);
    }

    public static <T> T asT(Class<T> clazz, String json){
        return gson.fromJson(json, clazz);
    }

    public static <T> String toJsonString(T object) {
        return gson.toJson(object);
    }

    public static <T> T getBean(String jsonStr, Class<T> className) {
        try {
            T t = gson.fromJson(jsonStr, className);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 对json字符串进行格式化
     * @param content 要格式化的json字符串
     * @param indent 是否进行缩进(false时压缩为一行)
     * @param colonWithSpace key冒号后是否加入空格
     * @return
     */
    public static String toFormat(String content, boolean indent, boolean colonWithSpace) {
        if (content == null) return null;
        StringBuilder sb = new StringBuilder();
        int count = 0;
        boolean inString = false;
        String tab = "\t";
        for (int i = 0; i < content.length(); i ++) {
            char ch = content.charAt(i);
            switch (ch) {
                case '{':
                case '[':
                    sb.append(ch);
                    if (!inString) {
                        if (indent) {
                            sb.append("\n");
                            count ++;
                            for (int j = 0; j < count; j ++) {
                                sb.append(tab);
                            }
                        }

                    }
                    break;
                case '\uFEFF': //非法字符
                    if (inString) sb.append(ch);
                    break;
                case '}':
                case ']':
                    if (!inString) {
                        if (indent) {
                            count --;
                            sb.append("\n");
                            for (int j = 0; j < count; j ++) {
                                sb.append(tab);
                            }
                        }

                        sb.append(ch);
                    } else {
                        sb.append(ch);
                    }
                    break;
                case ',':
                    sb.append(ch);
                    if (!inString) {
                        if (indent) {
                            sb.append("\n");
                            for (int j = 0; j < count; j ++) {
                                sb.append(tab);
                            }
                        } else {
                            if (colonWithSpace) {
                                sb.append(' ');
                            }
                        }
                    }
                    break;
                case ':':
                    sb.append(ch);

                    if (!inString) {
                        if (colonWithSpace) {  //key名称冒号后加空格
                            sb.append(' ');
                        }
                    }
                    break;
                case ' ':
                case '\n':
                case '\t':
                    if (inString) {
                        sb.append(ch);
                    }
                    break;
                case '"':
                    if (i > 0 && content.charAt(i - 1) != '\\') {
                        inString = !inString;
                    }
                    sb.append(ch);
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }


}
