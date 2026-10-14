package io.coderf.arklab.common.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * room数据库JSON类型转换器（基于Gson）
 *
 * <p>说明：Room 泛型转换器（@ProvidedTypeConverter）在 Java 实体的生成代码场景下存在
 * 类型擦除限制（反序列化无法还原具体泛型类型），故本类采用具体类型签名；
 * 如需持久化自定义 Bean，请参照本类为其添加一对具体类型的转换方法。</p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/14 10:00
 */
public class RoomJsonConverters {

    /** 反序列化 Map&lt;String,Object&gt; 时整数解析为 Long、小数解析为 Double，避免 1 变 1.0 */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<Map<String, Object>>() {}.getType(),
                    new MapObjectDeserializer())
            .create();

    /** Map&lt;String,Object&gt; -> JSON字符串 */
    @TypeConverter
    public static String mapObjectToString(Map<String, Object> value) {
        return value == null ? null : GSON.toJson(value);
    }

    /** JSON字符串 -> Map&lt;String,Object&gt; */
    @TypeConverter
    public static Map<String, Object> stringToMapObject(String value) {
        if (value == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return GSON.fromJson(value, type);
    }

    /** Map&lt;String,String&gt; -> JSON字符串 */
    @TypeConverter
    public static String mapStringToString(Map<String, String> value) {
        return value == null ? null : GSON.toJson(value);
    }

    /** JSON字符串 -> Map&lt;String,String&gt; */
    @TypeConverter
    public static Map<String, String> stringToMapString(String value) {
        if (value == null) {
            return null;
        }
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return GSON.fromJson(value, type);
    }

    /**
     * Map&lt;String,Object&gt; 反序列化器，数字按是否含小数分别解析为 Long/Double
     */
    private static class MapObjectDeserializer implements JsonDeserializer<Map<String, Object>> {
        @Override
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
                                               JsonDeserializationContext context)
                throws JsonParseException {
            return parseMap(json.getAsJsonObject());
        }

        private Map<String, Object> parseMap(JsonObject json) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                map.put(entry.getKey(), parseElement(entry.getValue()));
            }
            return map;
        }

        private Object parseElement(JsonElement element) {
            if (element.isJsonNull()) {
                return "";
            }
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    if (element.getAsBigDecimal().stripTrailingZeros().scale() <= 0) {
                        return element.getAsLong();
                    }
                    return element.getAsDouble();
                }
                if (element.getAsJsonPrimitive().isBoolean()) {
                    return element.getAsBoolean();
                }
                return element.getAsString();
            }
            if (element.isJsonObject()) {
                return parseMap(element.getAsJsonObject());
            }
            JsonArray array = element.getAsJsonArray();
            List<Object> list = new ArrayList<>(array.size());
            for (JsonElement item : array) {
                list.add(parseElement(item));
            }
            return list;
        }
    }
}
