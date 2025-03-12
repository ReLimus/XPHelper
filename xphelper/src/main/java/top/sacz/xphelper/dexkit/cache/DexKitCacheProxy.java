package top.sacz.xphelper.dexkit.cache;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import top.sacz.xphelper.reflect.ClassUtils;
import top.sacz.xphelper.reflect.FieldUtils;
import top.sacz.xphelper.reflect.MethodUtils;
import top.sacz.xphelper.util.ConfigUtils;

public class DexKitCacheProxy {

    ConfigUtils configUtils = new ConfigUtils("DexKitCache");

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
        ArrayList<String> methodInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (methodInfoList != null) {
            for (String methodInfo : methodInfoList) {
                result.add(findMethodByJSONString(methodInfo));
            }
        }
        return result;
    }

    private Method findMethodByJSONString(String methodInfoStrJSON) {
        JSONObject methodInfo = JSONObject.parseObject(methodInfoStrJSON);
        String methodName = methodInfo.getString("MethodName");
        String declareClass = methodInfo.getString("DeclareClass");
        String ReturnType = methodInfo.getString("ReturnType");
        JSONArray methodParams = methodInfo.getJSONArray("Params");
        Class<?>[] params = new Class[methodParams.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = ClassUtils.findClass(methodParams.getString(i));
        }
        return MethodUtils.create(declareClass)
                .methodName(methodName)
                .returnType(ClassUtils.findClass(ReturnType))
                .params(params)
                .first();
    }

    private String getMethodInfoJSON(Method method) {
        method.setAccessible(true);
        JSONObject result = new JSONObject();
        String methodName = method.getName();
        String declareClass = method.getDeclaringClass().getName();
        Class<?>[] methodParams = method.getParameterTypes();
        JSONArray params = new JSONArray();
        for (Class<?> type : methodParams) {
            params.add(type.getName());
        }
        result.put("DeclareClass", declareClass);
        result.put("MethodName", methodName);
        result.put("Params", params);
        result.put("ReturnType", method.getReturnType().getName());
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
        ArrayList<String> fieldInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (fieldInfoList != null) {
            for (String fieldInfo : fieldInfoList) {
                result.add(findFieldByJSONString(fieldInfo));
            }
        }
        return result;
    }

    private Field findFieldByJSONString(String fieldInfoStrJSON) {
        JSONObject fieldInfo = JSONObject.parseObject(fieldInfoStrJSON);
        String fieldName = fieldInfo.getString("FieldName");
        String declareClass = fieldInfo.getString("DeclareClass");
        String fieldType = fieldInfo.getString("FieldType");
        return FieldUtils.create(declareClass)
                .fieldName(fieldName)
                .fieldType(ClassUtils.findClass(fieldType))
                .first();
    }

    private String getFieldInfoJSON(Field field) {
        field.setAccessible(true);
        JSONObject result = new JSONObject();
        String fieldName = field.getName();
        String declareClass = field.getDeclaringClass().getName();
        result.put("DeclareClass", declareClass);
        result.put("FieldName", fieldName);
        result.put("FieldType", field.getType().getName());
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
        ArrayList<String> classInfoList = configUtils.getObject(key, new TypeReference<>() {
        });
        if (classInfoList != null) {
            for (String classInfo : classInfoList) {
                result.add(findClassByJSONString(classInfo));
            }
        }
        return result;
    }

    public Class<?> findClassByJSONString(String classInfoJSON) {
        JSONObject classInfo = JSONObject.parseObject(classInfoJSON);
        String className = classInfo.getString("ClassName");
        return ClassUtils.findClass(className);
    }

    private String getClassInfoJSON(Class<?> clazz) {
        JSONObject result = new JSONObject();
        result.put("ClassName", clazz.getName());
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
