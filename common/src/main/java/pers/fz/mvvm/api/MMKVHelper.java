package pers.fz.mvvm.api;

import android.os.Parcelable;

import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by fz on 2023/4/26 14:43
 * describe :MMKV工具类
 */
public class MMKVHelper {
    private static volatile MMKVHelper mmkvHelper;
    private static MMKV mmkv;

    private MMKVHelper() {
    }

    public static MMKVHelper getInstance() {
        if (mmkvHelper == null) {
            synchronized (MMKVHelper.class) {
                if (mmkvHelper == null) {
                    mmkvHelper = new MMKVHelper();
                }
            }
        }
        if (mmkv == null) {
            mmkv = MMKV.defaultMMKV();
        }
        return mmkvHelper;
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            mmkv.encode(key, (String) object);
        } else if (object instanceof Integer) {
            mmkv.encode(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mmkv.encode(key, (Boolean) object);
        } else if (object instanceof Float) {
            mmkv.encode(key, (Float) object);
        } else if (object instanceof Long) {
            mmkv.encode(key, (Long) object);
        } else if (object instanceof Double) {
            mmkv.encode(key, (Double) object);
        } else if (object instanceof byte[]) {
            mmkv.encode(key, (byte[]) object);
        } else if (object instanceof Parcelable) {
            mmkv.encode(key, (Parcelable) object);
        } else if (object instanceof List) {
            mmkv.encode(key, new Gson().toJson(object));
        } else {
            mmkv.encode(key, object.toString());
        }
    }

    public <T> void setArray( String name,List<T> list) {
        if (list == null || list.isEmpty()) {
            mmkv.putInt(name + "size", 0);
            int size = mmkv.getInt(name + "size", 0);
            for (int i = 0; i < size; i++) {
                if (mmkv.getString(name + i, null) != null) {
                    mmkv.remove(name + i);
                }
            }
        } else {
            mmkv.putInt(name + "size", list.size());
            if (list.size() > 20) {
                list.remove(0);   //只保留后20条记录
            }
            for (int i = 0; i < list.size(); i++) {
                mmkv.remove(name + i);
                mmkv.remove(new Gson().toJson(list.get(i)));//删除重复数据 先删后加
                mmkv.putString(name + i, new Gson().toJson(list.get(i)));
            }
        }
        mmkv.sync();
    }

    public <T> List<T> getArray(String name, Class<T> clx) {
        List<T> list = new ArrayList<T>();
        int size = mmkv.getInt(name + "size", 0);
        for (int i = 0; i < size; i++) {
            if (mmkv.getString(name + i, null) != null) {
                try {
                    list.add((T) new Gson().fromJson(mmkv.getString(name + i, null), clx));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return list;
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    public Integer getInt(String key) {
        return mmkv.decodeInt(key, 0);
    }

    public Double getDouble(String key) {
        return mmkv.decodeDouble(key, 0.00);
    }

    public Long getLong(String key) {
        return mmkv.decodeLong(key, 0L);
    }

    public Boolean getBoolean(String key) {
        return mmkv.decodeBool(key, false);
    }

    public Float getFloat(String key) {
        return mmkv.decodeFloat(key, 0F);
    }

    public byte[] getBytes(String key) {
        return mmkv.decodeBytes(key);
    }

    public String getString(String key) {
        return mmkv.decodeString(key, "");
    }

    public Set<String> getStringSet(String key) {
        return mmkv.decodeStringSet(key, Collections.<String>emptySet());
    }

    public <T extends Parcelable> Parcelable getParcelable(String key, Class<T> clx) {
        return mmkv.decodeParcelable(key, clx);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    public Integer getInt(String key, int defaultValue) {
        return mmkv.decodeInt(key, defaultValue);
    }

    public Double getDouble(String key, double defaultValue) {
        return mmkv.decodeDouble(key, defaultValue);
    }

    public Long getLong(String key, long defaultValue) {
        return mmkv.decodeLong(key, defaultValue);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return mmkv.decodeBool(key, defaultValue);
    }

    public Float getFloat(String key, float defaultValue) {
        return mmkv.decodeFloat(key, defaultValue);
    }

    public String getString(String key, String defaultValue) {
        return mmkv.decodeString(key, defaultValue);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mmkv.decodeStringSet(key, defaultValue);
    }

    //移除某个key对
    public void removeValueForKey(String key) {
        mmkv.removeValueForKey(key);
    }

    // 同时移除多个key对
    public void removeValuesForKeys(String[] strings) {
        mmkv.removeValuesForKeys(strings);
    }

    //清除所有key
    public void clearAll() {
        mmkv.clearAll();
    }
}
