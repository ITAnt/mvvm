package com.miekir.mvvm.tools;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * 网络工具类
 *
 * @author Miekir
 */
public final class JsonTools {
    private JsonTools() {}

    /**
     * Map转JSON
     *
     * @param params 请求参数
     * @return 请求体
     */
    public static JsonObject getJsonFromKeyValueList(Map<String, Object> params) {
        JsonObject contentObject = new JsonObject();
        if (params == null || params.size() == 0) {
            return contentObject;
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof String) {
                contentObject.addProperty(entry.getKey(), String.valueOf(entry.getValue()));
            } else if (entry.getValue() instanceof Number) {
                contentObject.addProperty(entry.getKey(), (Number) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                contentObject.addProperty(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof List<?>) {
                List<?> list = (List<?>) entry.getValue();
                JsonArray jsonArray = new Gson().toJsonTree(list, new TypeToken<List<?>>() {
                }.getType()).getAsJsonArray();
                contentObject.add(entry.getKey(), jsonArray);
            }
        }

        return contentObject;
    }

    /**
     * 不会会自动将int转换为double
     * 对象转JsonObject
     * @param obj 模板对象
     * @return JsonObject
     */
    public static JsonObject getJsonObject(Object obj) {
        Gson gson = new Gson();
        return JsonParser.parseString(gson.toJson(obj)).getAsJsonObject();
    }
}
