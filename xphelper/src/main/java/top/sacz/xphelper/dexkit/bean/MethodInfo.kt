package top.sacz.xphelper.dexkit.bean

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
    var modifiers = -1 //修饰符
    var matchType: MatchType = MatchType.Contains
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
            finder.usedString(*usedString!!)
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
            finder.usedFields(*usedFields!!)
        }
        if (searchPackages != null) {
            finder.searchPackages(*searchPackages!!)
        }
        if (excludePackages != null) {
            finder.excludePackages(*excludePackages!!)
        }
        if (modifiers != -1) {
            finder.modifiers(modifiers, matchType)
        }
        if (paramCount != -1) {
            finder.paramCount(paramCount)
        }
        return finder
    }
}
