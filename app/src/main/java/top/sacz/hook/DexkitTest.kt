package top.sacz.hook

import android.app.Activity
import android.os.Bundle
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.sacz.hook.ext.showToast
import top.sacz.xphelper.dexkit.DexFinder
import top.sacz.xphelper.dexkit.ext.descriptor
import top.sacz.xphelper.dexkit.ext.toMethodFinder
import top.sacz.xphelper.reflect.MethodUtils

class DexkitTest {
    fun hook() {
        //演示查找方法
        val activityCreateMethod = MethodUtils.create(Activity::class.java)
            .returnType(Void.TYPE)
            .params(Bundle::class.java)
            .methodName("onCreate")
            .first()
        //展示注入成功的提示
        XposedBridge.hookMethod(activityCreateMethod, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject as Activity
                onCreate(activity)
            }
        })
    }



    @OptIn(DelicateCoroutinesApi::class)
    private fun onCreate(activity: Activity) {
        if (activity::class.java.packageName.startsWith("com.ten")) {
            GlobalScope.launch(Dispatchers.IO) {
                startFindMethod()
            }
        }
    }

    private fun startFindMethod() {
        val startTime = System.currentTimeMillis()
        val method = DexFinder.findMethod {
            usedString =
                arrayOf("com/tencent/mm/ui/PlusSubMenuHelper\$MenuItemView", "compatCallBack")
            paramCount = 3
            isParamCount = true
        }.first()
        val field = DexFinder.findField {
            declaredClass = method.declaringClass
            readMethods = arrayOf(method.toMethodFinder())
        }.first()
        val endTime = System.currentTimeMillis()
        """
            ${endTime - startTime}ms ->
            method:${method.descriptor}
            field:${field.descriptor}
        """.trimMargin().showToast()
    }
}