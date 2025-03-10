package top.sacz.xphelper.dexkit;

import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.enums.MatchType;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.FieldDataList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.sacz.xphelper.dexkit.cache.DexKitCache;
import top.sacz.xphelper.reflect.ClassUtils;

public class FieldFinder {

    private Class<?> declaredClass;        // 字段声明类
    private String fieldName;              // 字段名称
    private Class<?> fieldType;            // 字段类型
    private int modifiers;                 // 修饰符
    private boolean isModifiers = false;
    private MatchType matchType;

    private String[] searchPackages;       // 搜索包
    private String[] excludePackages;      // 排除包

    private MethodFinder[] readMethods;
    private MethodFinder[] writeMethods;

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
        finder.isModifiers = true;
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
        this.readMethods = readMethods;
        return this;
    }
    /**
     * 读取了该字段的方法
     *
     * @param readMethods
     * @return
     */
    public FieldFinder readMethods(Method... readMethods) {
        this.readMethods = new MethodFinder[readMethods.length];
        for (int i = 0; i < readMethods.length; i++) {
            this.readMethods[i] = MethodFinder.from(readMethods[i]);
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
        this.writeMethods = writeMethods;
        return this;
    }

    /**
     * 写入了该字段的方法
     *
     * @param writeMethods
     * @return
     */
    public FieldFinder writeMethods(Method... writeMethods) {
        this.writeMethods = new MethodFinder[writeMethods.length];
        for (int i = 0; i < writeMethods.length; i++) {
            this.writeMethods[i] = MethodFinder.from(writeMethods[i]);
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
        this.isModifiers = true;
        this.matchType = matchType;
        return this;
    }

    /**
     * 设置搜索包过滤
     *
     * @param packages 搜索包数组
     * @return 当前FieldFinder实例
     */
    public FieldFinder searchPackages(String... packages) {
        this.searchPackages = packages;
        return this;
    }

    /**
     * 设置排除包过滤
     *
     * @param packages 排除包数组
     * @return 当前FieldFinder实例
     */
    public FieldFinder excludePackages(String... packages) {
        this.excludePackages = packages;
        return this;
    }

    /**
     * 构建FindField查询对象
     *
     * @return FindField实例
     */
    private FindField buildFindField() {
        FindField findField = FindField.create();
        if (searchPackages != null) findField.searchPackages(searchPackages);
        if (excludePackages != null) findField.excludePackages(excludePackages);
        return findField.matcher(buildFieldMatcher());
    }

    /**
     * 构建FieldMatcher匹配器
     *
     * @return FieldMatcher实例
     */
    public FieldMatcher buildFieldMatcher() {
        FieldMatcher matcher = FieldMatcher.create();
        if (declaredClass != null) matcher.declaredClass(declaredClass);
        if (fieldName != null) matcher.name(fieldName);
        if (fieldType != null) matcher.type(fieldType);
        if (isModifiers) matcher.modifiers(modifiers, matchType);
        if (readMethods != null) {
            for (MethodFinder readMethod : readMethods) {
                matcher.addReadMethod(readMethod.buildMethodMatcher());
            }
        }
        if (writeMethods != null) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (declaredClass != null) sb.append(declaredClass.getName());
        if (fieldName != null) sb.append(fieldName);
        if (fieldType != null) sb.append(fieldType.getName());
        if (isModifiers) sb.append(modifiers);
        if (searchPackages != null) sb.append(Arrays.toString(searchPackages));
        if (excludePackages != null) sb.append(Arrays.toString(excludePackages));
        return sb.toString();
    }
}