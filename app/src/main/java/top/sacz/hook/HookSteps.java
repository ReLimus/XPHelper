package top.sacz.hook;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import top.sacz.hook.activity.ModuleActivity;
import top.sacz.xphelper.XpHelper;
import top.sacz.xphelper.reflect.MethodUtils;

public class HookSteps {

    /**
     * 获取初始的Hook方法
     *
     * @param loadPackageParam
     * @return
     */
    public Method getApplicationCreateMethod(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            String applicationName = loadPackageParam.appInfo.name;
            Class<?> clz = loadPackageParam.classLoader.loadClass(applicationName);
            try {
                return clz.getDeclaredMethod("attachBaseContext", Context.class);
            } catch (Throwable i) {
                try {
                    return clz.getDeclaredMethod("onCreate");
                } catch (Throwable e) {
                    try {
                        return clz.getSuperclass().getDeclaredMethod("attachBaseContext", Context.class);
                    } catch (Throwable m) {
                        return clz.getSuperclass().getDeclaredMethod("onCreate");
                    }
                }
            }
        } catch (Exception e) {
            try {
                return ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }

    public void initHook(Context context) {
        initDialogX(context);
        //演示查找方法
        Method activityCreateMethod = MethodUtils.create(Activity.class)
                .returnType(void.class)
                .params(Bundle.class)
                .methodName("onCreate")
                .first();
        //展示注入成功的提示
        XposedBridge.hookMethod(activityCreateMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                PopTip.show("注入成功 ->" + InjectHook.loadPackageParam.appInfo.name);
                Activity activity = (Activity) param.thisObject;
                XpHelper.injectResourcesToContext(activity);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, ModuleActivity.class);
                        activity.startActivity(intent);
                    }
                },5000);
            }
        });
    }

    private void initDialogX(Context context) {
        DialogX.init(context);
        DialogX.globalTheme = DialogX.THEME.AUTO;
        DialogX.globalStyle = new MaterialYouStyle();
    }
}
