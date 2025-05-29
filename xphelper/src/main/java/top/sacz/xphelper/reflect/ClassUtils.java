package top.sacz.xphelper.reflect;


import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import top.sacz.xphelper.exception.ReflectException;

public class ClassUtils {
    private static final Object[][] baseTypes = {{"int", int.class}, {"boolean", boolean.class}, {"byte", byte.class}, {"long", long.class}, {"char", char.class}, {"double", double.class}, {"float", float.class}, {"short", short.class}, {"void", void.class}};
    private static ClassLoader classLoader;//宿主应用类加载器

    public static ClassLoader getModuleClassLoader() {
        return ClassUtils.class.getClassLoader();
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 获取基本类型
     */
    private static Class<?> getBaseTypeClass(String baseTypeName) {
        if (baseTypeName.length() == 1) return findSimpleType(baseTypeName.charAt(0));
        for (Object[] baseType : baseTypes) {
            if (baseTypeName.equals(baseType[0])) {
                return (Class<?>) baseType[1];
            }
        }
        throw new ReflectException(baseTypeName + " <-不是基本的数据类型");
    }

    /**
     * conversion base type
     *
     * @param simpleType Smali Base Type V,Z,B,I...
     */
    private static Class<?> findSimpleType(char simpleType) {
        switch (simpleType) {
            case 'V':
                return void.class;
            case 'Z':
                return boolean.class;
            case 'B':
                return byte.class;
            case 'S':
                return short.class;
            case 'C':
                return char.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'F':
                return float.class;
            case 'D':
                return double.class;
        }
        throw new RuntimeException("Not an underlying type");
    }

    /**
     * 排除常用类
     */
    public static boolean isCommonlyUsedClass(String name) {
        return name.startsWith("androidx.") || name.startsWith("android.") || name.startsWith("kotlin.") || name.startsWith("kotlinx.") || name.startsWith("com.tencent.mmkv.") || name.startsWith("com.android.tools.r8.") || name.startsWith("com.google.android.") || name.startsWith("com.google.gson.") || name.startsWith("com.google.common.") || name.startsWith("com.microsoft.appcenter.") || name.startsWith("org.intellij.lang.annotations.") || name.startsWith("org.jetbrains.annotations.");
    }

    public static Class<?> findClassOrNull(String className) {
        try {
            return findClass(className);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取类
     */
    public static Class<?> findClass(String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void intiClassLoader(ClassLoader loader) {
        if (loader == null) throw new ReflectException("类加载器为Null 无法设置");
        //如果我们自己重写了 就不再次继承
        if (loader instanceof CacheClassLoader) {
            classLoader = loader;
            return;
        }
        classLoader = new CacheClassLoader(loader);
    }


    private static class CacheClassLoader extends ClassLoader {
        private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

        public CacheClassLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException {
            Class<?> clazz = CLASS_CACHE.get(className);
            if (clazz != null) {
                return clazz;
            }
            if (className.endsWith(";") || className.contains("/") || className.contains("L")) {
                className = className.replace('/', '.');
                // 处理所有 L 开头的情况（无论是否有分号）
                if (className.startsWith("L")) {
                    int endIndex = className.endsWith(";")
                            ? className.length() - 1
                            : className.length();
                    className = className.substring(1, endIndex);
                } else if (className.endsWith(";")) {
                    className = className.substring(0, className.length() - 1);
                }
            }
            if (className.startsWith("[")) {
                int dimension = 0;
                while (className.charAt(dimension) == '[') {
                    dimension++;
                }
                String componentType = className.substring(dimension);
                // 递归处理组件类型（确保组件类型被规范化）
                Class<?> componentClass = loadClass(componentType); // 改为递归调用
                for (int i = 0; i < dimension; i++) {
                    componentClass = Array.newInstance(componentClass, 0).getClass();
                }
                CLASS_CACHE.put(className, componentClass);
                return componentClass;
            }

            //可能是基础类型
            try {
                clazz = getBaseTypeClass(className);
            } catch (Exception e) {
                //因为默认的ClassLoader.load() 不能加载"int"这种类型
                clazz = super.loadClass(className);
            }
            CLASS_CACHE.put(className, clazz);
            return clazz;

        }

    }
}
