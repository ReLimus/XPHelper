package top.sacz.xphelper.dexkit.cache;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class DexKitCache {

    public static void clearCache() {
        new DexKitCacheProxy().clearCache();
    }

    public static void putMethodList(String key, List<Method> methodList) {
        new DexKitCacheProxy().putMethodList(key, methodList);
    }

    public static List<Method> getMethodList(String key) {
        return new DexKitCacheProxy().getMethodList(key);
    }


    public static void putFieldList(String key, List<Field> fieldList) {
        new DexKitCacheProxy().putFieldList(key, fieldList);
    }

    public static List<Field> getFieldList(String key) {
        return new DexKitCacheProxy().getFieldList(key);
    }

    public static String getAllMethodString() {
        return new DexKitCacheProxy().toString();
    }
}
