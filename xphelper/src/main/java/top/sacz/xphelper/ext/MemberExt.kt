package top.sacz.xphelper.ext

import top.sacz.xphelper.dexkit.FieldFinder
import top.sacz.xphelper.dexkit.MethodFinder
import top.sacz.xphelper.reflect.ClassUtils
import top.sacz.xphelper.reflect.FieldUtils
import top.sacz.xphelper.reflect.Ignore
import top.sacz.xphelper.reflect.MethodUtils
import top.sacz.xphelper.util.DexMethodDescriptor
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author suzhelan
 * @date 2025/03/11
 * xphelper拓展类
 */

fun Method.toMethodFinder(): MethodFinder = MethodFinder.from(this)

inline val Method.descriptor: String get() = MethodUtils.getDescriptor(this)

fun Field.toFieldFinder(): FieldFinder = FieldFinder.from(this)

inline val Field.descriptor: String get() = FieldUtils.getDescriptor(this)

inline val Constructor<*>.descriptor : String get() = DexMethodDescriptor(this).descriptor

/**
 * 通过描述符获取类
 */
fun String.toClass(): Class<*> = ClassUtils.findClass(this)

/**
 * 方法拓展
 * 通过方法签名获取方法
 */
fun String.toMethod(): Method = MethodUtils.getMethodByDescriptor(this)

fun String.toMethod(clsLoader: ClassLoader): Method = MethodUtils.getMethodByDescriptor(this, clsLoader)

fun String.toConstructor(): Constructor<*> {
    val constructor = DexMethodDescriptor(this).getConstructorInstance(ClassUtils.getClassLoader())
    constructor.isAccessible = true
    return constructor
}

fun String.toConstructor(clsLoader: ClassLoader): Constructor<*> {
    val constructor = DexMethodDescriptor(this).getConstructorInstance(clsLoader)
    constructor.isAccessible = true
    return constructor
}

/**
 * 变量拓展
 * 通过变量签名获取变量
 */
fun String.toField(): Field = FieldUtils.getFieldByDescriptor(this)

fun String.toField(clsLoader: ClassLoader): Field = FieldUtils.getFieldByDescriptor(this, clsLoader)

/**
 * 对象拓展 通过对象的方法参数调用
 * 不支持静态方法的调用
 */
fun <T> Any.callMethod(
    methodName: String,
    vararg args: Any?
): T {
    val paramTypes = args.map {
        if (it == null) {
            return@map Ignore::class.java
        } else {
            return@map it.javaClass
        }
    }.toTypedArray()
    return MethodUtils.create(this)
        .methodName(methodName)
        .params(*paramTypes)
        .callFirst<T>(this, *args)
}

/**
 * 调用类的静态方法
 */
fun <T> Class<*>.callStaticMethod(
    methodName: String,
    vararg args: Any?
): T {
    val paramTypes = args.map {
        if (it == null) {
            return@map Ignore::class.java
        } else {
            return@map it.javaClass
        }
    }.toTypedArray()
    return MethodUtils.create(this)
        .methodName(methodName)
        .params(*paramTypes)
        .callFirst(null, *args)
}

/**
 * 对象拓展
 * 获取字段值
 * 传参 字段名 或者 字段类型
 */
fun <T> Any.getFieldValue(name: String? = null, type: Class<*>? = null): T {
    return FieldUtils.create(this)
        .fieldName(name)
        .fieldType(type)
        .firstValue(this)
}

/**
 * 获取字段值 传参字段名
 */
fun <T> Any.getFieldValue(name: String): T {
    return this.getFieldValue(name, null)
}

/**
 * 获取字段值 传参字段类型
 */
fun <T> Any.getFieldValue(type: Class<*>): T {
    return this.getFieldValue(null, type)
}

/**
 * 对象拓展
 * 设置字段值
 * 传参 字段名 或者 字段类型
 */
fun <T> Any.setFieldValue(name: String? = null, type: Class<*>? = null, value: T) {
    FieldUtils.create(this)
        .fieldName(name)
        .fieldType(type)
        .setFirst(this, value)
}

/**
 * 设置字段值 传参字段名
 */
fun <T> Any.setFieldValue(name: String, value: T) {
    this.setFieldValue(name, null, value)
}

/**
 * 设置字段值 传参字段类型
 */
fun <T> Any.setFieldValue(type: Class<*>, value: T) {
    this.setFieldValue(null, type, value)
}

/**
 * 类拓展
 * 获取静态字段值
 * 传参 字段名 或者 字段类型
 */
fun <T> Class<*>.getStaticFieldValue(name: String? = null, type: Class<*>? = null): T {
    return FieldUtils.create(this)
        .fieldName(name)
        .fieldType(type)
        .firstValue<T>(null)
}

/**
 * 类拓展
 * 设置静态字段值
 * 传参 字段名 或者 字段类型
 */
fun <T> Class<*>.setStaticFieldValue(name: String? = null, type: Class<*>? = null, value: T) {
    FieldUtils.create(this)
        .fieldName(name)
        .fieldType(type)
        .setFirst(null, value)
}