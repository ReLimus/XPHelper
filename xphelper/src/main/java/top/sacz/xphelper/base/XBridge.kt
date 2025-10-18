package top.sacz.xphelper.base

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XposedBridge
import top.sacz.xphelper.XpHelper
import top.sacz.xphelper.XpHelper.classLoader
import top.sacz.xphelper.dexkit.MethodFinder
import top.sacz.xphelper.exception.ReflectException
import top.sacz.xphelper.reflect.ConstructorUtils
import java.lang.reflect.Member
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author ReLimus
 * @date 2025/10/18
 * Hook 基类
 */

typealias HookAction = MethodHookParam.() -> Unit

/**
 * 所有hook功能的基础类,都应该要继承这个类
 */
abstract class XBridge {
    class HookBuilder {
        var beforeAction: HookAction? = null
        var afterAction: HookAction? = null

        fun before(action: HookAction) {
            beforeAction = action
        }

        fun after(action: HookAction) {
            afterAction = action
        }
    }

    var isLoad: Boolean = false
    private val unhookRefs: MutableList<Unhook> = CopyOnWriteArrayList()

    fun startLoad() {
        if (isLoad) {
            return
        }
        try {
            isLoad = true
            // 修复：只调用一次initOnce()
            if (initOnce()) {
                onHook(XpHelper.context, classLoader)
            }
        } catch (e: Throwable) {
            XposedBridge.log("$e")
        }
    }

    protected open fun initOnce(): Boolean {
        return true
    }

    abstract fun onHook(ctx: Context, classLoader: ClassLoader)

    /**
     * @receiver 要被Hook的 Method 或 Constructor。
     * @param builder Hook配置构建器，可以设置before和after动作。
     * @return 一个 Unhook 对象列表，可用于取消Hook。
     */
    fun Member.hook(builder: HookBuilder.() -> Unit): List<Unhook?> {
        return doHook(this, null, builder)
    }

    /**
     * @receiver 要被Hook的 Method 或 Constructor。
     * @param priority Hook 优先级。
     * @param builder Hook配置构建器，可以设置before和after动作。
     * @return 一个 Unhook 对象列表，可用于取消Hook。
     */
    fun Member.hook(priority: Int, builder: HookBuilder.() -> Unit): List<Unhook?> {
        return doHook(this, priority, builder)
    }

    /**
     * @receiver MethodFinder 实例，用于查找方法。
     * @param builder Hook配置构建器，可以设置before和after动作。
     * @return 一个 Unhook 对象列表，可用于取消Hook；如果未找到方法则返回空列表。
     */
    fun MethodFinder.hook(builder: HookBuilder.() -> Unit): List<Unhook?> {
        return try {
            val member = first()
            member.hook(builder)
        } catch (e: NoSuchMethodException) {
            XposedBridge.log("Hook failed: Method not found by $this, $e")
            emptyList()
        } catch (t: Throwable) {
            XposedBridge.log(t)
            emptyList()
        }
    }

    /**
     * @receiver ConstructorUtils.Builder 实例。
     * @param builder Hook配置构建器，可以设置before和after动作。
     * @return 一个 Unhook 对象列表，可用于取消Hook；如果未找到构造函数则返回空列表。
     */
    fun ConstructorUtils.hook(builder: HookBuilder.() -> Unit): List<Unhook?> {
        return try {
            val member = first()
            member.hook(builder)
        } catch (e: ReflectException) {
            XposedBridge.log("Hook failed: Constructor not found by $this, $e")
            emptyList()
        } catch (t: Throwable) {
            XposedBridge.log(t)
            emptyList()
        }
    }

    private fun tryExecute(param: MethodHookParam, hookAction: HookAction) {
        try {
            // 调用带接收者的 lambda
            param.hookAction()
        } catch (throwable: Throwable) {
            XposedBridge.log("$throwable")
        }
    }

    private fun doHook(member: Member, priority: Int?, builder: HookBuilder.() -> Unit): List<Unhook?> {
        val hookBuilder = HookBuilder().apply(builder)
        val unhooks = mutableListOf<Unhook?>()

        val xcHook = if (priority != null) {
            object : XC_MethodHook(priority) {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    hookBuilder.beforeAction?.let { tryExecute(param, it) }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    hookBuilder.afterAction?.let { tryExecute(param, it) }
                }
            }
        } else {
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    hookBuilder.beforeAction?.let { tryExecute(param, it) }
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    hookBuilder.afterAction?.let { tryExecute(param, it) }
                }
            }
        }

        val unhook = XposedBridge.hookMethod(member, xcHook)

        unhooks.add(unhook)
        unhookRefs.add(unhook)
        return unhooks
    }

    fun unload() {
        try {
            unhookRefs.forEach {
                try {
                    it.unhook()
                } catch (_: Throwable) {
                }
            }
            unhookRefs.clear()
            isLoad = false
        } catch (e: Throwable) {
            XposedBridge.log("$e")
        }
    }
}
