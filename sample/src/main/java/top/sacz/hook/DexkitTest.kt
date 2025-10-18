package top.sacz.hook

import android.util.Log
import top.sacz.xphelper.reflect.ClassUtils

class DexkitTest {
    fun hook() {
        startFindMethod()
    }

    private fun startFindMethod() {
        val classTest = ClassUtils.findClass("Ljava.lang.String")
        Log.d("FindXph", "startFindMethod: ${classTest.name} ")
    }
}