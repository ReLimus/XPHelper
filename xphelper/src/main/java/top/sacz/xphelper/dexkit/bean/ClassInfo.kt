package top.sacz.xphelper.dexkit.bean

import org.luckypray.dexkit.query.enums.MatchType
import top.sacz.xphelper.dexkit.ClassFinder
import top.sacz.xphelper.dexkit.FieldFinder
import top.sacz.xphelper.dexkit.MethodFinder

class ClassInfo {
    var className: String? = null // 类名
    var superClass: String? = null // 父类
    var interfaces: Array<String>? = null // 实现的接口
    var modifiers: Int = 0 // 修饰符
    var isModifiers: Boolean = false
    var matchType: MatchType? = null
    var searchPackages: Array<String>? = null // 搜索包
    var excludePackages: Array<String>? = null // 排除包
    var fields: Array<FieldFinder>? = null // 包含的字段
    var methods: Array<MethodFinder>? = null // 包含的方法

    fun generate(): ClassFinder {
        val finder = ClassFinder.build()
            .className(className)
            .superClass(superClass)
        if (interfaces != null) {
            finder.addInterface(*interfaces!!)
        }
        if (isModifiers) {
            finder.modifiers(modifiers, matchType!!)
        }
        if (searchPackages != null) {
            finder.searchPackages(*searchPackages!!)
        }
        if (excludePackages != null) {
            finder.excludePackages(*excludePackages!!)
        }
        if (fields != null) {
            finder.fields(*fields!!)
        }
        if (methods != null) {
            finder.methods(*methods!!)
        }
        return finder
    }
}