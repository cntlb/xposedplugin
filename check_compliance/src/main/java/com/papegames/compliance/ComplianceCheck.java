package com.papegames.compliance;

import android.content.ContentResolver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.net.NetworkInterface;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ComplianceCheck implements IXposedHookLoadPackage {
    private static final String TAG = ComplianceCheck.class.getSimpleName();
    private static final XC_MethodHook DUMP_STACK = new Hook();


    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Exception {

        if (lpparam == null) {
            return;
        }

        Log.e(TAG, "Load app packageName:" + lpparam.packageName);
        ClassLoader cl = lpparam.classLoader;
        hook(cl, "android.telephony.TelephonyManager", "getDeviceId", DUMP_STACK);
        hook(cl, "android.telephony.TelephonyManager", "getSubscriberId", DUMP_STACK);
        hook(cl, "android.net.wifi.WifiInfo", "getMacAddress", DUMP_STACK);
        hook(cl, "java.net.NetworkInterface", "getHardwareAddress", new Hook() {
            @Override
            protected Object onResult(XC_MethodHook.MethodHookParam param) {
                NetworkInterface ni = ((NetworkInterface) param.thisObject);
                return ni.getName() + ": " + format((byte[]) param.getResult(), "%02X", ":");
            }
        });
        hook(cl, "android.provider.Settings.Secure", "getString", ContentResolver.class, String.class, DUMP_STACK);
        hook(cl, "android.provider.Settings.System", "getString", ContentResolver.class, String.class, DUMP_STACK);
        hook(cl, "android.location.LocationManager", "getLastKnownLocation", String.class, DUMP_STACK);
        hook(cl, "java.net.Inet4Address", "getAddress", new Hook() {
            @Override
            protected Object onResult(MethodHookParam param) {
                byte[] data = (byte[]) param.getResult();
                int[] result = new int[data.length];
                for (int i = 0; i < data.length; i++) {
                    result[i] = ((int) data[i]) & 0xff;//to unsigned int
                }
                return format(result, "%d", ".");
            }
        });
        hook(cl, "android.os.Environment", "getExternalStorageDirectory", new Hook() {
            @Override
            protected Object onResult(MethodHookParam param) {
                return "Get_SDCARD_Directory:" + param.getResult();
            }
        });
        hookInit(cl, "java.io.FileOutputStream", File.class, boolean.class, new Hook() {
            @Override
            protected Object onResult(MethodHookParam param) {
                return param.args[0];
            }
        });
        hookInit(cl, "java.io.FileInputStream", File.class, new Hook() {
            @Override
            protected Object onResult(MethodHookParam param) {
                return param.args[0];
            }
        });
        hook(cl, "android.telephony.TelephonyManager","listen", PhoneStateListener.class, int.class,
                new Hook() {
                    @Override
                    protected Object onResult(MethodHookParam param) {
                        return param.args[1];
                    }
                });
    }

    private void hook(ClassLoader classLoader, String className, String method, Object... args) {
        XposedHelpers.findAndHookMethod(className, classLoader, method, args);
    }

    private void hook(ClassLoader classLoader, Method method, XC_MethodHook hook) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Class[parameterTypes.length + 1];
        System.arraycopy(parameterTypes, 0, args, 0, parameterTypes.length);
        args[args.length - 1] = hook;
        XposedHelpers.findAndHookMethod(className, classLoader, methodName, args);
    }

    private void hookInit(ClassLoader classLoader, String className, Object... args) {
        XposedHelpers.findAndHookConstructor(className, classLoader, args);
    }
}
