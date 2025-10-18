package top.sacz.hook

import android.content.Context
import android.content.ContextWrapper
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

class HookSteps {
    /**
     * 获取初始的Hook方法
     *
     * @param loadPackageParam
     * @return
     */
    fun getApplicationCreateMethod(loadPackageParam: LoadPackageParam): Method? {
        try {
            val applicationName = loadPackageParam.appInfo.name
            val clz = loadPackageParam.classLoader.loadClass(applicationName)
            return try {
                clz.getDeclaredMethod("attachBaseContext", Context::class.java)
            } catch (i: Throwable) {
                try {
                    clz.getDeclaredMethod("onCreate")
                } catch (e: Throwable) {
                    try {
                        clz.getSuperclass()
                            .getDeclaredMethod("attachBaseContext", Context::class.java)
                    } catch (m: Throwable) {
                        clz.getSuperclass().getDeclaredMethod("onCreate")
                    }
                }
            }
        } catch (e: Exception) {
            return try {
                ContextWrapper::class.java.getDeclaredMethod(
                    "attachBaseContext",
                    Context::class.java
                )
            } catch (ex: NoSuchMethodException) {
                null
            }
        }
    }
    fun initHook(context: Context?) {
        DexkitTest().hook()
    }
}