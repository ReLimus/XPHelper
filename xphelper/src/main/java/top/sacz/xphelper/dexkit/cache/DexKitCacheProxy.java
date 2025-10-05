package top.sacz.xphelper.dexkit.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import top.sacz.xphelper.reflect.ClassUtils;
import top.sacz.xphelper.reflect.FieldUtils;
import top.sacz.xphelper.reflect.MethodUtils;
import top.sacz.xphelper.util.ConfigUtils;

public class DexKitCacheProxy {
    private static final String TAG = "DexKitCacheProxy";

    private final ConfigUtils configUtils = new ConfigUtils("DexKitCache");
    private final Gson gson = new Gson();

    public void checkCacheExpired(Context context) {
        //获取应用的版本号
        try {
            String key = "version";
            String packageName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            String versionName = packageInfo.versionName;
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
            String versionFlag = versionName + "_" + versionCode;
            String configFlag = configUtils.getString(key, "");
            if (configFlag.equals(versionFlag)) {
                return;
            }
            clearCache();
            configUtils.put(key, versionFlag);
            Log.d(TAG, "checkCacheExpired: Host version updated Cache cleaned old:" + configFlag + " new:" + versionFlag);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "checkCacheExpired: " + Log.getStackTraceString(e));
        }
    }

    public Set<String> keys() {
        return configUtils.getAllKeys();
    }

    public void clearCache() {
        configUtils.clearAll();
    }

    public void putMethodList(String key, List<Method> methodList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Method method : methodList) {
            infoList.add(getMethodInfoJSON(method));
        }
        configUtils.put(key, infoList);
    }

    public List<Method> getMethodList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Method> result = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> methodInfoList = configUtils.getObject(key, listType);
        if (methodInfoList != null) {
            for (String methodInfo : methodInfoList) {
                result.add(findMethodByJSONString(methodInfo));
            }
        }
        return result;
    }

    private Method findMethodByJSONString(String methodInfoStrJSON) {
        JsonObject methodInfo = JsonParser.parseString(methodInfoStrJSON).getAsJsonObject();
        String methodName = methodInfo.get("MethodName").getAsString();
        String declareClass = methodInfo.get("DeclareClass").getAsString();
        String returnType = methodInfo.get("ReturnType").getAsString();
        JsonArray methodParams = methodInfo.getAsJsonArray("Params");
        Class<?>[] params = new Class[methodParams.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = ClassUtils.findClass(methodParams.get(i).getAsString());
        }
        return MethodUtils.create(declareClass)
                .methodName(methodName)
                .returnType(ClassUtils.findClass(returnType))
                .params(params)
                .first();
    }

    private String getMethodInfoJSON(Method method) {
        method.setAccessible(true);
        JsonObject result = new JsonObject();
        String methodName = method.getName();
        String declareClass = method.getDeclaringClass().getName();
        Class<?>[] methodParams = method.getParameterTypes();
        JsonArray params = new JsonArray();
        for (Class<?> type : methodParams) {
            params.add(type.getName());
        }
        result.addProperty("DeclareClass", declareClass);
        result.addProperty("MethodName", methodName);
        result.add("Params", params);
        result.addProperty("ReturnType", method.getReturnType().getName());
        return result.toString();
    }

    public void putFieldList(String key, List<Field> fieldList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Field field : fieldList) {
            infoList.add(getFieldInfoJSON(field));
        }
        configUtils.put(key, infoList);
    }

    public List<Field> getFieldList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Field> result = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> fieldInfoList = configUtils.getObject(key, listType);
        if (fieldInfoList != null) {
            for (String fieldInfo : fieldInfoList) {
                result.add(findFieldByJSONString(fieldInfo));
            }
        }
        return result;
    }

    private Field findFieldByJSONString(String fieldInfoStrJSON) {
        JsonObject fieldInfo = JsonParser.parseString(fieldInfoStrJSON).getAsJsonObject();
        String fieldName = fieldInfo.get("FieldName").getAsString();
        String declareClass = fieldInfo.get("DeclareClass").getAsString();
        String fieldType = fieldInfo.get("FieldType").getAsString();
        return FieldUtils.create(declareClass)
                .fieldName(fieldName)
                .fieldType(ClassUtils.findClass(fieldType))
                .first();
    }

    private String getFieldInfoJSON(Field field) {
        field.setAccessible(true);
        JsonObject result = new JsonObject();
        String fieldName = field.getName();
        String declareClass = field.getDeclaringClass().getName();
        result.addProperty("DeclareClass", declareClass);
        result.addProperty("FieldName", fieldName);
        result.addProperty("FieldType", field.getType().getName());
        return result.toString();
    }

    public void putClassList(String key, List<Class<?>> classList) {
        ArrayList<String> infoList = new ArrayList<>();
        for (Class<?> clazz : classList) {
            infoList.add(getClassInfoJSON(clazz));
        }
        configUtils.put(key, infoList);
    }

    public List<Class<?>> getClassList(String key) {
        if (!configUtils.containsKey(key)) {
            return null;
        }
        ArrayList<Class<?>> result = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> classInfoList = configUtils.getObject(key, listType);
        if (classInfoList != null) {
            for (String classInfo : classInfoList) {
                result.add(findClassByJSONString(classInfo));
            }
        }
        return result;
    }

    public Class<?> findClassByJSONString(String classInfoJSON) {
        JsonObject classInfo = JsonParser.parseString(classInfoJSON).getAsJsonObject();
        String className = classInfo.get("ClassName").getAsString();
        return ClassUtils.findClass(className);
    }

    private String getClassInfoJSON(Class<?> clazz) {
        JsonObject result = new JsonObject();
        result.addProperty("ClassName", clazz.getName());
        return result.toString();
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys()) {
            stringBuilder.append(key).append(":").append(getMethodList(key)).append("\n");
        }
        return stringBuilder.toString();
    }
}
