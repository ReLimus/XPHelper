package top.sacz.xphelper.dexkit


import org.luckypray.dexkit.DexKitBridge
import top.sacz.xphelper.XpHelper
import top.sacz.xphelper.dexkit.bean.ClassInfo
import top.sacz.xphelper.dexkit.bean.FieldInfo
import top.sacz.xphelper.dexkit.bean.MethodInfo
import top.sacz.xphelper.dexkit.cache.DexKitCache
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

object DexFinder {
    val isLoadLibrary = AtomicBoolean()
    private var dexKitBridge: DexKitBridge? = null
    private var timer: Timer? = null

    private var autoCloseTime = (10 * 1000).toLong()

    /**
     * 设置关闭的时间 超过此时间没有使用dexkit 则自动关闭 (其实有可能查找过程中被关闭)
     * 所以确保每次调用getDexKitBridge后十秒内完成查找
     * 默认十秒 设置为0则不会自动关闭
     *
     * @param time 单位毫秒
     */
    fun setAutoCloseTime(time: Long) {
        autoCloseTime = time
    }

    /**
     * 初始化dexkit
     *
     * @param apkPath
     */
    @Synchronized
    fun create(apkPath: String) {
        if (dexKitBridge != null) {
            return
        }
        if (!isLoadLibrary.getAndSet(true)) {
            try {
                System.loadLibrary("dexkit")
            } catch (e: Exception) {
            }
        }
        dexKitBridge = DexKitBridge.create(apkPath)
    }

    /**
     * 得到dexkit实例
     */
    @JvmStatic
    fun getDexKitBridge(): DexKitBridge {
        if (dexKitBridge == null) {
            create(XpHelper.context.applicationInfo.sourceDir)
        }
        resetTimer()
        return dexKitBridge!!
    }

    @JvmSynthetic
    fun findMethod(methodInfo: MethodInfo.() -> Unit): MethodFinder {
        val newInfo = MethodInfo().also(methodInfo)
        return newInfo.generate()
    }

    @JvmSynthetic
    fun findField(fieldInfo: FieldInfo.() -> Unit): FieldFinder {
        val newInfo = FieldInfo().also(fieldInfo)
        return newInfo.generate()
    }

    @JvmSynthetic
    fun findClass(classInfo: ClassInfo.() -> Unit): ClassFinder {
        val newInfo = ClassInfo().also(classInfo)
        return newInfo.generate()
    }

    /**
     * 清空缓存
     */
    fun clearCache() {
        DexKitCache.clearCache()
    }

    private fun resetTimer() {
        if (autoCloseTime <= 0) {
            return
        }
        //如果存在则取消 达到重置时间的效果
        if (timer != null) {
            timer!!.cancel()
        }
        //定时 10秒钟后关闭
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                close()
            }
        }, autoCloseTime) // 10 seconds
    }

    /**
     * 释放dexkit资源
     */
    fun close() {
        if (dexKitBridge != null) {
            dexKitBridge!!.close()
            dexKitBridge = null
        }
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }
}
