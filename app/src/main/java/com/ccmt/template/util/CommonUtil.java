package com.ccmt.template.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.CcmtApplication;
import com.ccmt.template.R;
import com.ccmt.template.activity.AbstractActivity;
import com.ccmt.template.net.NetManager;
import com.ccmt.template.traffic.ITrafficManager;
import com.ccmt.template.traffic.TrafficManagerFactory;
import com.ccmt.template.traffic.domain.ProcessInfo;
import com.ccmt.template.view.CustomAlertDialog;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CommonUtil {

    @SuppressWarnings("unused")
    private static final String[] CURRENT_PKG;

    public static List<Object> sList;

    static {
        CURRENT_PKG = new String[]{
                CcmtApplication.application.getPackageName()
        };
    }

    @SuppressWarnings("FieldCanBeLocal")
    private static int EXCESS_VALUE = 20 * 1024;

    @SuppressWarnings("FieldCanBeLocal")
    private static int RUNNING_SERVICE_COUNT = 120;

    public static <T> List<T> listInit(List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        } else {
            if (list.size() > 0) {
                list.clear();
            }
        }
        return list;
    }

    public static <T> void listAdd(List<T> list, T t) {
        if (!list.contains(t)) {
            list.add(t);
        }
    }

    public static <T> void listRemove(List<T> list, T t) {
        if (list.contains(t)) {
            list.remove(t);
        }
    }

    @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
    public static Activity obtainTopActivity() {
        Activity topActivity = null;
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getATMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            Object activityThread = getATMethod.invoke(null);
            activitiesField.setAccessible(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ArrayMap activites = (ArrayMap) activitiesField.get(activityThread);
                if (activites == null || activites.size() == 0) {
                    return null;
                }
                Object activityClientRecord = activites.valueAt(0);

                Class activityClientRecordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                topActivity = (Activity) activityField.get(activityClientRecord);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
//        LogUtil.i("topActivity -> " + topActivity);
        return topActivity;
    }

//    /**
//     * 获取所有运行中的app的进程信息
//     *
//     * @param context
//     * @param packageNames
//     * @return
//     */
//    @SuppressWarnings({"JavaDoc", "WeakerAccess", "deprecation"})
//    public static List<Object> obtainCurrentProcessInfo(Context context, String[] packageNames) {
//        List<Object> result;
//        List<ProcessInfo> processInfos;
//        List<ProcessInfo> excessProcessInfos = null;
////        long totalNetworkSpeed = 0;
//        ProcessInfo processInfo;
//        PackageManager packageManager = context.getPackageManager();
//        if (Build.VERSION.SDK_INT >= 21) {
//            if (Build.VERSION.SDK_INT < 24) {
//                List<AndroidAppProcess> androidAppProcesses;
//                androidAppProcesses = ProcessManager.getRunningAppProcesses();
//                if (androidAppProcesses == null) {
//                    return null;
//                }
//                AndroidAppProcess androidAppProcess;
//                int size = androidAppProcesses.size();
//                if (size == 0) {
//                    return null;
//                }
//                String packageNameTemp;
//                processInfos = new ArrayList<>();
//                excessProcessInfos = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    androidAppProcess = androidAppProcesses.get(i);
//                    if (androidAppProcess == null) {
//                        continue;
//                    }
//
//                    packageNameTemp = androidAppProcess.getPackageName();
//                    if (packageNameTemp == null) {
//                        continue;
//                    }
////                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
////                        continue;
////                    }
//                    boolean iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (packageNameTemp.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                if (applicationInfo.uid == 1000) {
////                    continue;
////                }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
////                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{androidAppProcess.pid});
//
////                    LogUtil.i("------");
////                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("androidAppProcess.pid -> " + androidAppProcess.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo = new ProcessInfo();
//                    processInfo.setPackageName(packageNameTemp);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(androidAppProcess.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
////                    processInfo.setSystemApp(false);
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(packageNameTemp);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                        CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                    }
//                }
//            } else {
//                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
//                if (runningServices == null) {
//                    return null;
//                }
//                ActivityManager.RunningServiceInfo runningServiceInfo;
//                int size = runningServices.size();
//                LogUtil.i("运行中的服务的数量 -> " + size);
//                if (size == 0) {
//                    return null;
//                }
//                String packageNameTemp;
//                processInfos = new ArrayList<>();
//                excessProcessInfos = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    runningServiceInfo = runningServices.get(i);
//                    if (runningServiceInfo == null) {
//                        continue;
//                    }
//
//                    packageNameTemp = runningServiceInfo.service.getPackageName();
//                    if (packageNameTemp == null) {
//                        continue;
//                    }
////                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
////                        continue;
////                    }
//                    boolean iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (packageNameTemp.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                if (applicationInfo.uid == 1000) {
////                    continue;
////                }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningServiceInfo.pid});
//
////                    LogUtil.i("------");
////                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("runningServiceInfo.pid -> " + runningServiceInfo.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo = new ProcessInfo();
//                    processInfo.setPackageName(packageNameTemp);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(runningServiceInfo.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadLogo(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(packageNameTemp);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                        CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                    }
//                }
//            }
//        } else {
//            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
//            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
//            String[] arr;
//            int size = runningAppProcesses.size();
//            if (size == 0) {
//                return null;
//            }
//            processInfos = new ArrayList<>();
//            for (int i = 0; i < size; i++) {
//                runningAppProcessInfo = runningAppProcesses.get(i);
//                if (runningAppProcessInfo == null) {
//                    continue;
//                }
//                arr = runningAppProcessInfo.pkgList;
//                if (arr == null || arr.length == 0) {
//                    continue;
//                }
//
////                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});
//
//                processInfo = new ProcessInfo();
//                excessProcessInfos = new ArrayList<>();
//                boolean iscontain;
//                for (String anArr : arr) {
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
////                    if (anArr.contains("setting") || anArr.contains("launcher")) {
////                        continue;
////                    }
//                    iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (anArr.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                        LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                    if (applicationInfo.uid == 1000) {
////                        continue;
////                    }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            anArr) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    LogUtil.i("------");
////                    LogUtil.i("anArr -> " + anArr);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("runningAppProcessInfo.pid -> " + runningAppProcessInfo.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            anArr) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo.setPackageName(anArr);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(runningAppProcessInfo.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(anArr);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                        CcmtApplication.application.mNetworkSpeeds.put(anArr, uidRxBytes);
//                    }
//                }
//            }
//        }
//        if (processInfos.size() == 0) {
//            return null;
//        }
//
//        // 不显示所有耗流量的app了,不需要再排序.
////        Collections.sort(processInfos);
//
//        if (excessProcessInfos != null) {
//            Collections.sort(excessProcessInfos);
//        }
//        result = new ArrayList<>();
//        result.add(processInfos);
////        result.add(totalNetworkSpeed);
//        result.add(NetManager.createNetManager().getCurrentNetType(context));
//        result.add(excessProcessInfos);
//        return result;
//    }

//    /**
//     * 获取所有运行中的app的进程信息
//     *
//     * @param context
//     * @param packageNames
//     * @return
//     */
//    @SuppressWarnings({"JavaDoc", "WeakerAccess", "deprecation", "StatementWithEmptyBody"})
//    public static List<Object> obtainCurrentProcessInfo(Context context, String[] packageNames) {
//        List<Object> result;
//        List<ProcessInfo> processInfos;
//        List<ProcessInfo> excessProcessInfos = null;
////        long totalNetworkSpeed = 0;
//        ProcessInfo processInfo;
//        PackageManager packageManager = context.getPackageManager();
//        if (Build.VERSION.SDK_INT >= 21) {
//            if (Build.VERSION.SDK_INT < 24) {
//                List<AndroidAppProcess> androidAppProcesses;
//                androidAppProcesses = ProcessManager.getRunningAppProcesses();
//                if (androidAppProcesses == null) {
//                    return null;
//                }
//                AndroidAppProcess androidAppProcess;
//                int size = androidAppProcesses.size();
//                if (size == 0) {
//                    return null;
//                }
//                String packageNameTemp;
//                processInfos = new ArrayList<>();
//                excessProcessInfos = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    androidAppProcess = androidAppProcesses.get(i);
//                    if (androidAppProcess == null) {
//                        continue;
//                    }
//
//                    packageNameTemp = androidAppProcess.getPackageName();
//                    if (packageNameTemp == null) {
//                        continue;
//                    }
////                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
////                        continue;
////                    }
//                    boolean iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (packageNameTemp.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                if (applicationInfo.uid == 1000) {
////                    continue;
////                }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
////                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{androidAppProcess.pid});
//
////                    LogUtil.i("------");
////                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("androidAppProcess.pid -> " + androidAppProcess.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo = new ProcessInfo();
//                    processInfo.setPackageName(packageNameTemp);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(androidAppProcess.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
////                    processInfo.setSystemApp(false);
////                    boolean flag = false;
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(packageNameTemp);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        } else if (temp > 0) {
////                            flag = true;
//                            CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                        CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                    }
//                    // 暂时保留
////                    else {
////                        if (flag) {
////                            int index = processInfos.indexOf(processInfo);
////                            ProcessInfo processInfo2 = processInfos.get(index);
////                            processInfo2.setNetworkSpeed(processInfo.getNetworkSpeed());
////                            networkSpeed = processInfo.getNetworkSpeed();
////                            if (networkSpeed >= EXCESS_VALUE) {
////                                index = excessProcessInfos.indexOf(processInfo);
////                                if (index < 0) {
////                                    excessProcessInfos.add(processInfo);
////                                } else {
////                                    processInfo2 = excessProcessInfos.get(index);
////                                    processInfo2.setNetworkSpeed(processInfo.getNetworkSpeed());
////                                }
////                            }
////                        }
////                    }
//                }
//            } else {
//                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
//                if (runningServices == null) {
//                    return null;
//                }
//                ActivityManager.RunningServiceInfo runningServiceInfo;
//                int size = runningServices.size();
//                LogUtil.i("运行中的服务的数量 -> " + size);
//                if (size == 0) {
//                    return null;
//                }
//                String packageNameTemp;
//                processInfos = new ArrayList<>();
//                excessProcessInfos = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    runningServiceInfo = runningServices.get(i);
//                    if (runningServiceInfo == null) {
//                        continue;
//                    }
//
//                    packageNameTemp = runningServiceInfo.service.getPackageName();
//                    if (packageNameTemp == null) {
//                        continue;
//                    }
////                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
////                        continue;
////                    }
//                    boolean iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (packageNameTemp.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                if (applicationInfo.uid == 1000) {
////                    continue;
////                }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningServiceInfo.pid});
//
////                    LogUtil.i("------");
////                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("runningServiceInfo.pid -> " + runningServiceInfo.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo = new ProcessInfo();
//                    processInfo.setPackageName(packageNameTemp);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(runningServiceInfo.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadLogo(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
////                    processInfo.setSystemApp(false);
////                    boolean flag = false;
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(packageNameTemp);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        } else if (temp > 0) {
////                            flag = true;
//                            CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                        CcmtApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                    }
//                }
//            }
//        } else {
//            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
//            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
//            String[] arr;
//            int size = runningAppProcesses.size();
//            if (size == 0) {
//                return null;
//            }
//            processInfos = new ArrayList<>();
//            for (int i = 0; i < size; i++) {
//                runningAppProcessInfo = runningAppProcesses.get(i);
//                if (runningAppProcessInfo == null) {
//                    continue;
//                }
//                arr = runningAppProcessInfo.pkgList;
//                if (arr == null || arr.length == 0) {
//                    continue;
//                }
//
////                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});
//
//                processInfo = new ProcessInfo();
//                excessProcessInfos = new ArrayList<>();
//                boolean iscontain;
//                for (String anArr : arr) {
//                    ApplicationInfo applicationInfo;
//                    CharSequence applicationLabel;
////                    if (anArr.contains("setting") || anArr.contains("launcher")) {
////                        continue;
////                    }
//                    iscontain = false;
//                    if (packageNames != null) {
//                        for (String packageName : packageNames) {
//                            if (anArr.equals(packageName)) {
//                                iscontain = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (iscontain) {
//                        continue;
//                    }
//                    try {
//                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
//                    } catch (PackageManager.NameNotFoundException e) {
//                        e.printStackTrace();
//                        continue;
//                    }
//                    try {
////                    CharSequence applicationLabel = context.getPackageManager()
////                            .getApplicationLabel(context.getPackageManager()
////                                    .getApplicationInfo(packageNameTemp, 0));
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                        applicationLabel = applicationInfo.loadLabel(packageManager);
////                        LogUtil.i("applicationLabel -> " + applicationLabel);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        continue;
//                    }
////                    if (applicationInfo.uid == 1000) {
////                        continue;
////                    }
//                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
//                        continue;
//                    }
//                    if (!applicationInfo.enabled) {
//                        continue;
//                    }
//                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                            anArr) == PackageManager.PERMISSION_DENIED) {
//                        continue;
//                    }
//
////                    LogUtil.i("------");
////                    LogUtil.i("anArr -> " + anArr);
////                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
////                    LogUtil.i("applicationLabel -> " + applicationLabel);
////                    LogUtil.i("runningAppProcessInfo.pid -> " + runningAppProcessInfo.pid);
////                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
////                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
////                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
////                    LogUtil.i("enabledSetting -> " + enabledSetting);
////                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
////                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
////                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
////                            anArr) == PackageManager.PERMISSION_GRANTED;
////                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);
//
////                    String lowerCase = applicationLabel.toString().toLowerCase();
////                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
////                        continue;
////                    }
//
//                    processInfo.setPackageName(anArr);
////                    processInfo.setProcessName(applicationInfo.processName);
////                    processInfo.setPid(runningAppProcessInfo.pid);
////                    processInfo.setUid(applicationInfo.uid);
//                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
//                    processInfo.setAppName(applicationLabel.toString());
////                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
////                    processInfo.setSystemApp(false);
////                    boolean flag = false;
//                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
//                            .getUidRxBytes(context, applicationInfo.uid);
//                    Long networkSpeed = CcmtApplication.application.mNetworkSpeeds.get(anArr);
//                    if (networkSpeed != null) {
//                        long temp = uidRxBytes - networkSpeed;
//                        if (temp < 0) {
//                            temp = Math.abs(temp);
//                        } else if (temp > 0) {
////                            flag = true;
//                            CcmtApplication.application.mNetworkSpeeds.put(anArr, uidRxBytes);
//                        }
//                        processInfo.setNetworkSpeed(temp);
//                    } else {
//                        processInfo.setNetworkSpeed(0L);
//                        CcmtApplication.application.mNetworkSpeeds.put(anArr, uidRxBytes);
//                    }
//                    if (!processInfos.contains(processInfo)) {
//                        processInfos.add(processInfo);
//                        networkSpeed = processInfo.getNetworkSpeed();
////                        totalNetworkSpeed += networkSpeed;
//                        if (networkSpeed >= EXCESS_VALUE) {
//                            excessProcessInfos.add(processInfo);
//                        }
//                    }
//                }
//            }
//        }
//        if (processInfos.size() == 0) {
//            return null;
//        }
//
//        // 不显示所有耗流量的app了,不需要再排序.
////        Collections.sort(processInfos);
//
//        if (excessProcessInfos != null) {
//            Collections.sort(excessProcessInfos);
//        }
//        result = new ArrayList<>();
//        result.add(processInfos);
////        result.add(totalNetworkSpeed);
//        result.add(NetManager.createNetManager().getCurrentNetType(context));
//        result.add(excessProcessInfos);
//        return result;
//    }

    /**
     * 获取所有运行中的app的进程信息
     *
     * @param context
     * @param packageNames
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess", "deprecation", "StatementWithEmptyBody"})
    public static List<Object> obtainCurrentProcessInfo(Context context, String[] packageNames) {
        List<Object> result;
        List<ProcessInfo> processInfos;
        List<ProcessInfo> excessProcessInfos = null;
//        long totalNetworkSpeed = 0;
        ProcessInfo processInfo;
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT < 24) {
                List<AndroidAppProcess> androidAppProcesses;
                androidAppProcesses = ProcessManager.getRunningAppProcesses();
                if (androidAppProcesses == null) {
                    return null;
                }
                AndroidAppProcess androidAppProcess;
                int size = androidAppProcesses.size();
                if (size == 0) {
                    return null;
                }
                String packageNameTemp;
                processInfos = new ArrayList<>();
                excessProcessInfos = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    androidAppProcess = androidAppProcesses.get(i);
                    if (androidAppProcess == null) {
                        continue;
                    }

                    packageNameTemp = androidAppProcess.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (packageNameTemp.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{androidAppProcess.pid});

//                    LogUtil.i("------");
//                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("androidAppProcess.pid -> " + androidAppProcess.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo = new ProcessInfo();
                    processInfo.setPackageName(packageNameTemp);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(androidAppProcess.pid);
                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
//                        totalNetworkSpeed += networkSpeed;
                    }
                }
            } else {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
                if (runningServices == null) {
                    return null;
                }
                ActivityManager.RunningServiceInfo runningServiceInfo;
                int size = runningServices.size();
                LogUtil.i("运行中的服务的数量 -> " + size);
                if (size == 0) {
                    return null;
                }
                String packageNameTemp;
                processInfos = new ArrayList<>();
                excessProcessInfos = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    runningServiceInfo = runningServices.get(i);
                    if (runningServiceInfo == null) {
                        continue;
                    }

                    packageNameTemp = runningServiceInfo.service.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (packageNameTemp.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningServiceInfo.pid});

//                    LogUtil.i("------");
//                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("runningServiceInfo.pid -> " + runningServiceInfo.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo = new ProcessInfo();
                    processInfo.setPackageName(packageNameTemp);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(runningServiceInfo.pid);
                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadLogo(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
//                        totalNetworkSpeed += networkSpeed;
                    }
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return null;
            }
            processInfos = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }

//                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});

                processInfo = new ProcessInfo();
                excessProcessInfos = new ArrayList<>();
                boolean iscontain;
                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
//                    if (anArr.contains("setting") || anArr.contains("launcher")) {
//                        continue;
//                    }
                    iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (anArr.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                        LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                    if (applicationInfo.uid == 1000) {
//                        continue;
//                    }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            anArr) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    LogUtil.i("------");
//                    LogUtil.i("anArr -> " + anArr);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("runningAppProcessInfo.pid -> " + runningAppProcessInfo.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            anArr) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo.setPackageName(anArr);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(runningAppProcessInfo.pid);
                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
//                        totalNetworkSpeed += networkSpeed;
                    }
                }
            }
        }
        if (processInfos.size() == 0) {
            return null;
        }
        long uidRxBytes;
        ITrafficManager trafficManager = TrafficManagerFactory.createTrafficManager(context);
        ProcessInfo next;
        long start = System.currentTimeMillis();
        Iterator<ProcessInfo> ite = processInfos.iterator();
        while (ite.hasNext()) {
            next = ite.next();
            uidRxBytes = trafficManager.getUidRxBytes(context, next.getUid());
            if (uidRxBytes < 0) {
                uidRxBytes = 0;
            }
            next.setNetworkSpeed(uidRxBytes);
        }
        long dtime = System.currentTimeMillis() - start;
        float dtimeSecond = 0.0F;
        if (dtime < 1000) {
            SystemClock.sleep(1000 - dtime);
        } else {
            dtimeSecond = ((float) dtime) / 1000;
        }
        ite = processInfos.iterator();
        long temp;
        while (ite.hasNext()) {
            next = ite.next();
            uidRxBytes = trafficManager.getUidRxBytes(context, next.getUid());
            if (uidRxBytes < 0) {
                uidRxBytes = 0;
            }
            temp = uidRxBytes - next.getNetworkSpeed();
            if (dtimeSecond != 0.0F) {
                temp = (long) (temp / dtimeSecond);
            }
            next.setNetworkSpeed(temp);
            if (next.getNetworkSpeed() < CommonUtil.EXCESS_VALUE) {
                ite.remove();
            }
        }

        Collections.sort(processInfos);

//        if (excessProcessInfos != null) {
//            Collections.sort(excessProcessInfos);
//        }
        result = new ArrayList<>();
        result.add(processInfos);
//        result.add(totalNetworkSpeed);
        result.add(NetManager.createNetManager().getCurrentNetType(context));
        result.add(excessProcessInfos);
        return result;
    }

    @SuppressWarnings({"deprecation", "unused", "UnusedAssignment"})
    public static boolean isRunningApp(Context context, String packageName, String[] packageNames, boolean isCheckInternetPermissions) {
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT < 24) {
                List<AndroidAppProcess> androidAppProcesses = ProcessManager.getRunningAppProcesses();
                if (androidAppProcesses == null) {
                    return false;
                }
                AndroidAppProcess androidAppProcess;
                int size = androidAppProcesses.size();
                if (size == 0) {
                    return false;
                }
                String packageNameTemp;
                for (int i = 0; i < size; i++) {
                    androidAppProcess = androidAppProcesses.get(i);
                    if (androidAppProcess == null) {
                        continue;
                    }

                    packageNameTemp = androidAppProcess.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (packageNameTemp.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        applicationLabel = applicationInfo.loadLabel(packageManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (isCheckInternetPermissions) {
                        if (packageManager.checkPermission(Manifest.permission.INTERNET,
                                packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                            continue;
                        }
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (packageNameTemp.equals(packageName)) {
                        return true;
                    }
                }
            } else {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
                if (runningServices == null) {
                    return false;
                }
                ActivityManager.RunningServiceInfo runningServiceInfo;
                int size = runningServices.size();
                if (size == 0) {
                    return false;
                }
                String packageNameTemp;
                for (int i = 0; i < size; i++) {
                    runningServiceInfo = runningServices.get(i);
                    if (runningServiceInfo == null) {
                        continue;
                    }

                    packageNameTemp = runningServiceInfo.service.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (packageNameTemp.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (isCheckInternetPermissions) {
                        if (packageManager.checkPermission(Manifest.permission.INTERNET,
                                packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                            continue;
                        }
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (packageNameTemp.equals(packageName)) {
                        return true;
                    }
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }
                boolean iscontain;
                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
//                    if (anArr.contains("setting") || anArr.contains("launcher")) {
//                        continue;
//                    }
                    iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (anArr.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        applicationLabel = applicationInfo.loadLabel(packageManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                    if (applicationInfo.uid == 1000) {
//                        continue;
//                    }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (isCheckInternetPermissions) {
                        if (packageManager.checkPermission(Manifest.permission.INTERNET,
                                anArr) == PackageManager.PERMISSION_DENIED) {
                            continue;
                        }
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (anArr.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 获取当前应用的主进程id
     *
     * @param context
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static int obtainCurrentMainProcessId(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.pid;
            }
        }
        return -1;
    }

    /**
     * 当前应用的主进程是否运行中
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static boolean isRunningAppForMainProcess(Context context) {
//        List<ProcessInfo> processInfos = obtainCurrentProcessInfo(context, context.getPackageName());
//        if (processInfos == null || processInfos.size() == 0) {
//            return false;
//        }
//        ProcessInfo processInfo;
//        for (int i = 0; i < processInfos.size(); i++) {
//            processInfo = processInfos.get(i);
//            if (processInfo.getPackageName().equals(processInfo.getProcessName())) {
//                return true;
//            }
//        }
//        return false;
        return obtainCurrentMainProcessId(context) != -1;
    }

    public static boolean isAppExists(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        PackageInfo packageInfo;
        for (int i = 0; i < packageInfos.size(); i++) {
            packageInfo = packageInfos.get(i);
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSystemApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean status = false;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            status = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                    == ApplicationInfo.FLAG_SYSTEM;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 跳转到当前应用的设置界面
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    public static void goToAppSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void goToApplicationDetailsSettings(Context context, String packageName, boolean isShouldAddFlagActivityNewTask) {
        // TODO 暂时保留,在8.0手机上无法跳转,后续再解决.
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.applications.InstalledAppDetails"));
        settingIntent.setData(Uri.parse("package:" + packageName));
        if (isShouldAddFlagActivityNewTask) {
            settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(settingIntent);
    }

    /**
     * 在强行停止界面显示引导界面
     *
     * @param activity
     */
    @SuppressWarnings("JavaDoc")
    public static void showGuide(AbstractActivity activity) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_go_to_force_stop, null);
        FrameLayout.LayoutParams layoutParams = //
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);
        NavigationBarManager navigationBarManager = NavigationBarManager.getInstance();
        boolean navigationBarShow = navigationBarManager.isNavigationBarShow(activity, null);
        navigationBarManager.add(view, navigationBarShow, isNavigationBarShow -> {
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (isNavigationBarShow) {
                layoutParams2.topMargin = ViewUtil.obtainViewPx(activity, 27, false);
            } else {
                layoutParams2.topMargin = 0;
            }
            view.setLayoutParams(layoutParams2);
        });
        showDialog(activity, view, navigationBarShow);
    }

    private static void showDialog(AbstractActivity activity, View view, boolean navigationBarShow) {
        CustomAlertDialog customAlertDialog;
        if (navigationBarShow) {
            customAlertDialog = new CustomAlertDialog.Builder(activity,
                    R.style.custom_alter_dialog_style_fullscreen_style)
                    .setOnDismissListener(dialog -> DialogFractory.closeProgressDialog(activity))
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .setClickContentViewDismiss(true)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.7F)
                    .setTopMargin(ViewUtil.obtainViewPx(activity, 27, false))
                    .setGlobalDialog(true)
                    .create();
        } else {
            customAlertDialog = new CustomAlertDialog.Builder(activity,
                    R.style.custom_alter_dialog_style_fullscreen_style)
                    .setOnDismissListener(dialog -> DialogFractory.closeProgressDialog(activity))
                    .setCancelable(true)
                    .setCanceledOnTouchOutside(true)
                    .setClickContentViewDismiss(true)
                    .setGravity(Gravity.CENTER)
                    .setDimAmount(0.7F)
                    .setGlobalDialog(true)
                    .create();
        }
        customAlertDialog.show();
        LogUtil.i("新手引导对话框弹出");
        customAlertDialog.setContentView(view);
    }

    @SuppressWarnings("unused")
    public static Typeface getSourceTypeFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCN-Regular.otf");
    }

}
