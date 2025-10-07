package top.sacz.hook;

import android.content.Context;
import android.content.ContextWrapper;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.sacz.xphelper.XpHelper;

public class InjectHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String TAG = "XpHelper";
    public static XC_LoadPackage.LoadPackageParam loadPackageParam;
    private final HookSteps hookSteps = new HookSteps();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadParam) throws Throwable {
        if (loadParam.isFirstApplication) {
            loadPackageParam = loadParam;
            Method applicationCreateMethod = hookSteps.getApplicationCreateMethod(loadParam);

            XposedBridge.hookMethod(applicationCreateMethod, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ContextWrapper context = (ContextWrapper) param.thisObject;
                    entryHook(context.getBaseContext());
                }
            });
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        //初始化xphelper
        XpHelper.initZygote(startupParam);
    }

    private void entryHook(Context context) {
        //初始化context
        XpHelper.initContext(context);
        //进入自己的Hook逻辑
        hookSteps.initHook(context);
    }
}
