package top.sacz.xphelper.dexkit.ext

import org.luckypray.dexkit.query.enums.MatchType
import top.sacz.xphelper.dexkit.FieldFinder
import top.sacz.xphelper.dexkit.MethodFinder
import java.lang.reflect.Method

class MethodInfo {
    var declaredClass: Class<*>? = null //方法声明类
    var parameters: Array<Class<*>>? = null //方法的参数列表
    var methodName: String? = null //方法名称
    var returnType: Class<*>? = null //方法的返回值类型
    var usedString: Array<String>? = null //方法中使用的字符串列表
    var invokeMethods: Array<Method>? = null //方法中调用的方法列表
    var callMethods: Array<Method>? = null //调用了该方法的方法列表
    var usingNumbers: LongArray? = null //方法中使用的数字列表
    var paramCount = -1 //参数数量
    var isParamCount = false
    var modifiers = 0 //修饰符
    var isModifiers = false
    var matchType: MatchType? = null
    var searchPackages: Array<String>? = null
    var excludePackages: Array<String>? = null
    var usedFields: Array<FieldFinder>? = null

    fun generate(): MethodFinder {
        val finder = MethodFinder.build()
            .declaredClass(declaredClass)
            .methodName(methodName)
            .returnType(returnType)
        if (parameters != null) {
            finder.parameters(*parameters!!)
        }
        if (usedString != null) {
            finder.useString(*usedString!!)
        }
        if (invokeMethods != null) {
            finder.invokeMethods(*invokeMethods!!)
        }
        if (callMethods != null) {
            finder.callMethods(*callMethods!!)
        }
        if (usingNumbers != null) {
            finder.usingNumbers(*usingNumbers!!)
        }
        if (usedFields != null) {
            finder.usedField(*usedFields!!)
        }
        if (searchPackages != null) {
            finder.searchPackages(*searchPackages!!)
        }
        if (excludePackages != null) {
            finder.excludePackages(*excludePackages!!)
        }
        if (isModifiers) {
            finder.modifiers(modifiers, matchType!!)
        }
        if (isParamCount) {
            finder.paramCount(paramCount)
        }
        return finder
    }
}
