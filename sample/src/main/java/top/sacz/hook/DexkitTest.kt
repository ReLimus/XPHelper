package top.sacz.hook

import android.content.Context
import android.util.Log
import top.sacz.xphelper.base.XBridge
import top.sacz.xphelper.reflect.ClassUtils

object DexkitTest : XBridge() {

    override fun onHook(ctx: Context, classLoader: ClassLoader) {
        startFindMethod()
    }
    private fun startFindMethod() {
        val classTest = ClassUtils.findClass("Ljava.lang.String")
        Log.d("FindXph", "startFindMethod: ${classTest.name} ")
    }
}