package top.sacz.xphelper.dexkit;

import androidx.annotation.NonNull;

import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.enums.MatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.FieldDataList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.base.BaseDexQuery;
import top.sacz.xphelper.dexkit.cache.DexKitCache;
import top.sacz.xphelper.reflect.ClassUtils;

public class FieldFinder extends BaseDexQuery {

    private Class<?> declaredClass;        // 字段声明类
    private String fieldName;              // 字段名称
    private Class<?> fieldType;            // 字段类型
    private int modifiers = -1;                 // 修饰符
    private MatchType matchType = MatchType.Contains;

    private final List<String> searchPackages = new ArrayList<>();       // 搜索包
    private final List<String> excludePackages = new ArrayList<>();      // 排除包

    private final List<MethodFinder> readMethods = new ArrayList<>();
    private final List<MethodFinder> writeMethods = new ArrayList<>();

    /**
     * 构建器模式
     *
     * @return FieldFinder实例
     */
    public static FieldFinder build() {
        return new FieldFinder();
    }

    /**
     * 将字段转换为dexkit-FieldMatcher
     *
     * @param field 字段
     * @return FieldMatcher实例
     */
    public static FieldMatcher toFieldMatcher(Field field) {
        return FieldMatcher.create(field);
    }

    /**
     * 将字段转换为FieldFinder
     * @param field
     * @return
     */
    public static FieldFinder from(Field field) {
        FieldFinder finder = new FieldFinder();
        finder.declaredClass = field.getDeclaringClass();
        finder.fieldName = field.getName();
        finder.fieldType = field.getType();
        finder.modifiers = field.getModifiers();
        finder.matchType = MatchType.Equals;
        return finder;
    }

    /**
     * 读取了该字段的方法
     *
     * @param readMethods
     * @return
     */
    public FieldFinder readMethods(MethodFinder... readMethods) {
        this.readMethods.addAll(Arrays.asList(readMethods));
        return this;
    }
    /**
     * 读取了该字段的方法
     *
     * @param readMethods
     * @return
     */
    public FieldFinder readMethods(Method... readMethods) {
        for (Method readMethod : readMethods) {
            this.readMethods.add(MethodFinder.from(readMethod));
        }
        return this;
    }

    /**
     * 写入了该字段的方法
     *
     * @param writeMethods
     * @return
     */
    public FieldFinder writeMethods(MethodFinder... writeMethods) {
        this.writeMethods.addAll(Arrays.asList(writeMethods));
        return this;
    }

    /**
     * 写入了该字段的方法
     *
     * @param writeMethods
     * @return
     */
    public FieldFinder writeMethods(Method... writeMethods) {
        for (Method writeMethod : writeMethods) {
            this.writeMethods.add(MethodFinder.from(writeMethod));
        }
        return this;
    }

    /**
     * 设置字段所属类
     *
     * @param declaredClass 字段声明类
     * @return 当前FieldFinder实例
     */
    public FieldFinder declaredClass(Class<?> declaredClass) {
        this.declaredClass = declaredClass;
        return this;
    }

    /**
     * 设置字段名称
     *
     * @param name 字段名称
     * @return 当前FieldFinder实例
     */
    public FieldFinder fieldName(String name) {
        this.fieldName = name;
        return this;
    }

    /**
     * 设置字段类型
     *
     * @param fieldType 字段类型
     * @return 当前FieldFinder实例
     */
    public FieldFinder fieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    /**
     * 设置修饰符（如public/private等）
     *
     * @param modifiers 修饰符
     * @param matchType 匹配类型
     * @return 当前FieldFinder实例
     */
    public FieldFinder modifiers(int modifiers, MatchType matchType) {
        this.modifiers = modifiers;
        this.matchType = matchType;
        return this;
    }

    public FieldFinder searchPackages(String... packages) {
        this.searchPackages.addAll(Arrays.asList(packages));
        return this;
    }

    public FieldFinder excludePackages(String... packages) {
        this.excludePackages.addAll(Arrays.asList(packages));
        return this;
    }

    private FindField buildFindField() {
        FindField findField = FindField.create();
        if (!searchPackages.isEmpty()) findField.searchPackages(searchPackages.toArray(new String[0]));
        if (!excludePackages.isEmpty()) findField.excludePackages(excludePackages.toArray(new String[0]));
        return findField.matcher(buildFieldMatcher());
    }

    public FieldMatcher buildFieldMatcher() {
        FieldMatcher matcher = FieldMatcher.create();
        if (declaredClass != null) matcher.declaredClass(declaredClass);
        if (fieldName != null) matcher.name(fieldName);
        if (fieldType != null) matcher.type(fieldType);
        if (modifiers != -1) matcher.modifiers(modifiers, matchType);
        if (!readMethods.isEmpty()) {
            for (MethodFinder readMethod : readMethods) {
                matcher.addReadMethod(readMethod.buildMethodMatcher());
            }
        }
        if (!writeMethods.isEmpty()) {
            for (MethodFinder writeMethod : writeMethods) {
                matcher.addWriteMethod(writeMethod.buildMethodMatcher());
            }
        }
        return matcher;
    }

    /**
     * 执行查找
     *
     * @return 查找到的字段列表
     */
    public List<Field> find() {
        try {
            // 检查缓存
            List<Field> cache = DexKitCache.getFieldList(toString());
            if (cache != null) {
                return cache;
            }
            // 执行查询
            ArrayList<Field> fieldResult = new ArrayList<>();
            FieldDataList dataList = DexFinder.getDexKitBridge().findField(buildFindField());
            if (dataList.isEmpty()) {
                DexKitCache.putFieldList(toString(), fieldResult);
                return fieldResult;
            }
            for (FieldData data : dataList) {
                Field field = data.getFieldInstance(ClassUtils.getClassLoader());
                field.setAccessible(true);
                fieldResult.add(field);
            }
            // 缓存结果
            DexKitCache.putFieldList(toString(), fieldResult);
            return fieldResult;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取第一个结果或null
     *
     * @return 第一个找到的字段或null
     */
    public Field firstOrNull() {
        List<Field> list = find();
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取第一个结果或抛异常
     *
     * @return 第一个找到的字段
     * @throws NoSuchFieldException 如果没有找到字段
     */
    public Field first() throws NoSuchFieldException {
        List<Field> list = find();
        if (list.isEmpty()) throw new NoSuchFieldException("Field not found: " + this);
        return list.get(0);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (declaredClass != null) sb.append(declaredClass.getName());
        if (fieldName != null) sb.append(fieldName);
        if (fieldType != null) sb.append(fieldType.getName());
        if (modifiers != -1) sb.append(Modifier.toString(modifiers));
        if (!searchPackages.isEmpty()) sb.append(searchPackages);
        if (!excludePackages.isEmpty()) sb.append(excludePackages);
        if (!readMethods.isEmpty()) sb.append(readMethods);
        if (!writeMethods.isEmpty()) sb.append(writeMethods);
        return sb.toString();
    }
}