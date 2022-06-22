package com.papegames.compliance;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.XC_MethodHook;

/**
 * @author dayan
 * @since 2021-11-26 下午5:28
 */
public class Hook extends XC_MethodHook {
    @Override
    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        PrintStream s = new PrintStream(System.err);
        synchronized (System.err) {
            s.print("发生时间: ");
            s.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date()));
            s.println(onResult(param));
            s.println("调用堆栈");
            Member method = param.method;
            Class<?> clazz = method.getDeclaringClass();
            String mName = method.getName();
            if (mName.equals(clazz.getName())) {
                mName = "<init>";
            }
            StackTraceElement e = new StackTraceElement(clazz.getName(),
                    mName, clazz.getSimpleName(), -1);
            s.println("\t" + traceInfo(e));
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : elements) {
                String className = element.getClassName();
                String methodName = element.getMethodName();
                if (className.startsWith(Hook.class.getName())) continue;
                if (className.startsWith("de.robv.android.xposed")) continue;
                if (className.startsWith("me.weishu.epic")) continue;
                if (className.equals("dalvik.system.VMStack") && methodName.equals("getThreadStackTrace"))
                    continue;
                if (className.equals("java.lang.Thread") && methodName.equals("getStackTrace"))
                    continue;
                s.println("\t" + traceInfo(element));
            }
        }
    }

    protected Object onResult(XC_MethodHook.MethodHookParam param) {
        return param.getResult();
    }

    protected static String format(Object array, String fmt, String join) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; array != null && i < Array.getLength(array); i++) {
            sb.append(String.format(fmt, Array.get(array, i)));
            if (i < Array.getLength(array) - 1) {
                sb.append(join);
            }
        }
        return sb.toString();
    }

    private static String traceInfo(StackTraceElement e) {
        if (e.getFileName() == null) {
            return e.getClassName() + "." + e.getMethodName() + ": " + e.getLineNumber();
        }
        return e.toString();
    }
}
