package top.sacz.xphelper.dexkit;


import org.jetbrains.annotations.NotNull;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.enums.MatchType;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.ClassDataList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.base.BaseDexQuery;
import top.sacz.xphelper.dexkit.cache.DexKitCache;
import top.sacz.xphelper.reflect.ClassUtils;

public class ClassFinder extends BaseDexQuery {

    private final List<String> interfaces = new ArrayList<>();         // 实现的接口
    private final List<String> searchPackages = new ArrayList<>();       // 搜索包过滤
    private final List<String> excludePackages = new ArrayList<>();      // 排除包过滤
    private final List<FieldFinder> fields = new ArrayList<>();          // 包含的字段
    private final List<MethodFinder> methods = new ArrayList<>();        // 包含的方法
    private String className;              // 类名匹配
    private String superClass;           // 父类匹配
    private int modifiers;                 // 修饰符
    private boolean isModifiers = false;
    private MatchType matchType;

    public static ClassFinder build() {
        return new ClassFinder();
    }

    public static ClassFinder from(Class<?> clazz) {
        ClassFinder finder = new ClassFinder();
        finder.className = clazz.getName();
        finder.superClass = clazz.getSuperclass().getName();
        for (Class<?> interfaceClass : clazz.getInterfaces()) {
            finder.interfaces.add(interfaceClass.getName());
        }
        finder.modifiers = clazz.getModifiers();
        finder.isModifiers = true;
        finder.matchType = MatchType.Equals;
        return finder;
    }

    public ClassFinder className(String name) {
        this.className = name;
        return this;
    }

    public ClassFinder superClass(String superClass) {
        this.superClass = superClass;
        return this;
    }

    public ClassFinder addInterface(String... interfaces) {
        this.interfaces.addAll(Arrays.asList(interfaces));
        return this;
    }

    public ClassFinder modifiers(int modifiers, MatchType matchType) {
        this.modifiers = modifiers;
        this.isModifiers = true;
        this.matchType = matchType;
        return this;
    }


    public ClassFinder searchPackages(String... packages) {
        this.searchPackages.addAll(Arrays.asList(packages));
        return this;
    }

    public ClassFinder excludePackages(String... packages) {
        this.excludePackages.addAll(Arrays.asList(packages));
        return this;
    }

    public ClassFinder fields(FieldFinder... fields) {
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }

    public ClassFinder methods(MethodFinder... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }

    private FindClass buildFindClass() {
        FindClass findClass = FindClass.create();
        if (!searchPackages.isEmpty())
            findClass.searchPackages(searchPackages.toArray(new String[0]));
        if (!excludePackages.isEmpty())
            findClass.excludePackages(excludePackages.toArray(new String[0]));
        return findClass.matcher(buildClassMatcher());
    }

    public ClassMatcher buildClassMatcher() {
        ClassMatcher matcher = ClassMatcher.create();
        if (className != null) matcher.className(className);
        if (superClass != null) matcher.superClass(superClass);
        if (!interfaces.isEmpty()) {
            for (String interfaceClassName : interfaces) {
                matcher.addInterface(interfaceClassName);
            }
        }
        if (isModifiers) matcher.modifiers(modifiers, matchType);
        if (!fields.isEmpty()) {
            for (FieldFinder field : fields) {
                matcher.addField(field.buildFieldMatcher());
            }
        }
        if (!methods.isEmpty()) {
            for (MethodFinder method : methods) {
                matcher.addMethod(method.buildMethodMatcher());
            }
        }
        return matcher;
    }

    public List<Class<?>> find() {
        try {
            List<Class<?>> cache = DexKitCache.getClassList(toString());
            if (cache != null) return cache;

            ArrayList<Class<?>> result = new ArrayList<>();
            ClassDataList dataList = DexFinder.getDexKitBridge().findClass(buildFindClass());

            if (dataList.isEmpty()) {
                DexKitCache.putClassList(toString(), result);
                return result;
            }

            for (ClassData data : dataList) {
                Class<?> clazz = data.getInstance(ClassUtils.getClassLoader());
                result.add(clazz);
            }

            DexKitCache.putClassList(toString(), result);
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public Class<?> firstOrNull() {
        List<Class<?>> list = find();
        return list.isEmpty() ? null : list.get(0);
    }

    public Class<?> first() throws ClassNotFoundException {
        List<Class<?>> list = find();
        if (list.isEmpty()) throw new ClassNotFoundException("Class not found: " + this);
        return list.get(0);
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (className != null) sb.append(className);
        if (superClass != null) sb.append(superClass);
        if (!interfaces.isEmpty()) sb.append(interfaces);
        if (isModifiers) sb.append(modifiers);
        if (!searchPackages.isEmpty()) sb.append(searchPackages);
        if (!excludePackages.isEmpty()) sb.append(excludePackages);
        if (!fields.isEmpty()) sb.append(fields);
        if (!methods.isEmpty()) sb.append(methods);
        return sb.toString();
    }
}

