package top.sacz.xphelper.dexkit.bean

import org.luckypray.dexkit.query.enums.MatchType
import top.sacz.xphelper.dexkit.FieldFinder
import top.sacz.xphelper.dexkit.MethodFinder

class FieldInfo {
    var declaredClass: Class<*>? = null // 字段声明类
    var fieldName: String? = null // 字段名称
    var fieldType: Class<*>? = null // 字段类型
    var modifiers = -1 // 修饰符
    var matchType: MatchType = MatchType.Contains
    var searchPackages: Array<String>? = null // 搜索包
    var excludePackages: Array<String>? = null // 排除包
    var readMethods: Array<MethodFinder>? = null // 读取了该字段的方法
    var writeMethods: Array<MethodFinder>? = null // 写入了该字段的方法

    fun generate(): FieldFinder {
        val finder = FieldFinder.build()
            .declaredClass(declaredClass)
            .fieldName(fieldName)
            .fieldType(fieldType)
        if (modifiers != -1) {
            finder.modifiers(modifiers, matchType)
        }
        if (searchPackages != null) {
            finder.searchPackages(*searchPackages!!)
        }
        if (excludePackages != null) {
            finder.excludePackages(*excludePackages!!)
        }
        if (readMethods != null) {
            finder.readMethods(*readMethods!!)
        }
        if (writeMethods != null) {
            finder.writeMethods(*writeMethods!!)
        }
        return finder
    }
}