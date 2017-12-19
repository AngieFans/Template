package com.ccmt.template.su;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Looper;
import android.view.View;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.CcmtApplication;
import com.ccmt.template.R;
import com.ccmt.template.util.CommonUtil;
import com.ccmt.template.util.DialogFractory;
import com.ccmt.template.util.ToastUtil;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootUtil {

    private static final Object obj = new Object();
    private static final Object obj2 = new Object();
    private static final Object obj3 = new Object();
    private static final Object obj4 = new Object();
    private static final Object obj5 = new Object();
    private static final Object obj6 = new Object();
    public static final Object obj7 = new Object();

    /**
     * 当前手机的文件系统是否为只读系统,也就是说是否可以进行文件挂载,为true代表不能进行文件挂载.
     */
    private static final String LRU_IS_ONLY_READ_SYSTEM = "lru_is_only_read_system";

    //    public static final int REQUEST_CODE_RESTORE = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int REQUEST_CODE_UNINSTALL = 2;

    public static String path;

    /**
     * 是否正在走模块开启流程,如果是,要把该变量设置为true,走完流程或超时后,要设置为false.
     * 弹出root授权框时,计算从弹框到用户点击允许或拒绝所花时间,需要用到该变量.
     */
    public static volatile boolean isModelStartInit;

    private static volatile boolean isAppSystemUninstall;
    @SuppressWarnings("WeakerAccess")
    public static boolean isAppCommonUninstall;
    private static volatile boolean isRoot;
    private static volatile boolean isRootApp;
    private static volatile boolean isSystemApp;
    public static volatile boolean isStartRootDone;
    private static boolean isAtSystem;
    public static volatile Boolean isDialogShow;
//    public static Handler handler = new Handler() {
//        @SuppressWarnings("unchecked")
//        public void handleMessage(android.os.Message msg) {
//            ArrayList<Object> runnables;
//            Runnable rootAppSuccessRunnable;
//            Runnable systemAppSuccessRunnable;
//            ArrayList<Object> list;
//            Runnable rootAppErrorRunnable;
//            Runnable systemAppErrorRunnable;
//            Context context;
//            switch (msg.what) {
//                case 1:
//                    runnables = (ArrayList<Object>) msg.obj;
//                    rootAppSuccessRunnable = (Runnable) runnables.get(0);
//                    systemAppSuccessRunnable = (Runnable) runnables.get(1);
//                    if (systemAppSuccessRunnable != null) {
//                        systemAppSuccessRunnable.run();
//                    }
//                    if (rootAppSuccessRunnable != null) {
//                        rootAppSuccessRunnable.run();
//                    }
//                    break;
//                case 2:
//                    // app获取root成功,提升为系统app成功,权限功能可以用.
//                    // LogUtil.i("成功");
//                    runnables = (ArrayList<Object>) msg.obj;
//                    rootAppSuccessRunnable = (Runnable) runnables.get(0);
//                    systemAppSuccessRunnable = (Runnable) runnables.get(1);
//                    if (systemAppSuccessRunnable != null) {
//                        systemAppSuccessRunnable.run();
//                    }
//                    if (rootAppSuccessRunnable != null) {
//                        rootAppSuccessRunnable.run();
//                    }
//                    break;
//                case 3:
//                    if (msg.arg1 == 1) {
//                        // app获取root成功,提升为系统app成功,权限功能用不了.
//                        runnables = (ArrayList<Object>) msg.obj;
//                        rootAppSuccessRunnable = (Runnable) runnables.get(0);
//                        systemAppSuccessRunnable = (Runnable) runnables.get(1);
//                        if (systemAppSuccessRunnable != null) {
//                            systemAppSuccessRunnable.run();
//                        }
//                        if (rootAppSuccessRunnable != null) {
//                            rootAppSuccessRunnable.run();
//                        }
//                    } else {
//                        // app获取root成功,但是提升为系统app失败,属于走模块开启流程.
//                        // LogUtil.i("失败");
//                        runnables = (ArrayList<Object>) msg.obj;
//                        rootAppSuccessRunnable = (Runnable) runnables.get(0);
//                        systemAppErrorRunnable = (Runnable) runnables.get(1);
//                        if (systemAppErrorRunnable != null) {
//                            systemAppErrorRunnable.run();
//                        }
//                        if (rootAppSuccessRunnable != null) {
//                            rootAppSuccessRunnable.run();
//                        }
//                    }
//                    break;
//                case 4:
//                    list = (ArrayList<Object>) msg.obj;
//
//                    rootAppErrorRunnable = (Runnable) list.get(6);
//                    if (rootAppErrorRunnable != null) {
//                        rootAppErrorRunnable.run();
//                    }
//
//                    context = (Context) list.get(0);
//
//                    DialogFractory.closeProgressDialog(context);
//
//                    // 手机没有root,点击确定按钮跳转到官网.
//                    DialogFractory.showAppNoRootDialog(context, (String) list.get(2),
//                            (String) list.get(3),
//                            (String) list.get(4), (String) list.get(5), (dialog, which) -> {
//                                View view = (View) list.get(1);
//                                if (view != null) {
//                                    view.setEnabled(true);
//
//                                    LruMap.getInstance().remove("view", false);
//                                }
//
//                                Intent intent = new Intent(
//                                        Intent.ACTION_VIEW,
//                                        Uri.parse("http://www.supersu.com/appmaster/howtoroot"));
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                context.startActivity(intent);
//                            }, (dialog, which) -> {
//                                View view = (View) list.get(1);
//                                if (view != null) {
//                                    view.setEnabled(true);
//
//                                    LruMap.getInstance().remove("view", false);
//                                }
//                            });
//                    break;
//                case 5:
//                    // app获取root失败,没走模块开启流程.
//                    // ToastUtil.showLong(application, R.string.error_su);
//                    list = (ArrayList<Object>) msg.obj;
//
//                    rootAppErrorRunnable = (Runnable) list.get(6);
//                    systemAppErrorRunnable = (Runnable) list.get(7);
//                    if (systemAppErrorRunnable != null) {
//                        systemAppErrorRunnable.run();
//                    }
//                    if (rootAppErrorRunnable != null) {
//                        rootAppErrorRunnable.run();
//                    }
//
//                    context = (Context) list.get(0);
//
//                    // 手机没有root,点击确定按钮跳转到官网.
//                    boolean isNeedPopupDoNoRoot = (boolean) list.get(8);
//                    if (isNeedPopupDoNoRoot) {
//                        View view = (View) list.get(1);
//                        Dialog progressDialog = (Dialog) list.get(9);
////                        if (progressDialog != null) {
////                            progressDialog.dismiss();
////                        } else {
////                            DialogFractory.closeProgressDialog(context);
////                        }
//
//                        DialogFractory.showAppNoRootDialog(context, view, (String) list.get(2),
//                                (String) list.get(3),
//                                (String) list.get(4), (String) list.get(5), (dialog, which) -> {
//                                    RootUtil.endRootNew(view, progressDialog);
//
////                                    if (view != null) {
////                                        view.setEnabled(true);
////                                    }
////
////                                    synchronized (RootUtil.obj) {
////                                        RootUtil.isModelStartInit = false;
////                                        RootUtil.isStartRootDone = false;
////                                        RootUtil.isDialogShow = null;
////                                    }
//
//                                    Intent intent = new Intent(
//                                            Intent.ACTION_VIEW,
//                                            Uri.parse("http://www.supersu.com/appmaster/howtoroot"));
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    context.startActivity(intent);
//                                }, (dialog, which) -> {
//                                    RootUtil.endRootNew(view, progressDialog);
//
////                                    if (view != null) {
////                                        view.setEnabled(true);
////                                    }
////
////                                    synchronized (RootUtil.obj) {
////                                        RootUtil.isModelStartInit = false;
////                                        RootUtil.isStartRootDone = false;
////                                        RootUtil.isDialogShow = null;
////                                    }
//                                });
//                    } else {
//                        View view = (View) list.get(1);
//                        Dialog progressDialog = (Dialog) list.get(9);
//                        RootUtil.endRootNew(view, progressDialog);
//                    }
//                    break;
//                case 6:
//                    // 应用每次启动时都执行定时任务,所有需要在应用启动时都需要执行的定时任务和服务都可以在这里做.
////                    doTimingTask();
//                    break;
//            }
//        }
//    };

    /**
     * 手机是否root,而不是app是否root,调用者需要在子线程里调用.
     *
     * @return
     * @throws InterruptedException
     */
    @SuppressWarnings("JavaDoc")
    public static boolean isRoot() throws InterruptedException {
        if (!isRoot) {
            synchronized (obj) {
                if (!isRoot) {
                    ShellUtil.checkRootPermission();
                    return isRoot;
                }
                return true;
            }
        }
        return true;
    }

    /**
     * 设置手机是否root
     *
     * @param isRoot
     */
    @SuppressWarnings("JavaDoc")
    public static void setRoot(boolean isRoot) {
        if (RootUtil.isRoot != isRoot) {
            synchronized (obj) {
                if (RootUtil.isRoot != isRoot) {
                    // 双重锁判断最好不要直接对变量赋值,有安全问题,需要把要做的事情封装为1个方法.
                    setRoot2(isRoot);
                }
            }
        }
    }

    private static void setRoot2(boolean isRoot) {
        RootUtil.isRoot = isRoot;
    }

    /**
     * 是否正在走模块开启流程,其他地方不用调用该方法.
     */
    @SuppressWarnings("unused")
    public static boolean isModelStartInit() {
        synchronized (obj2) {
            return isModelStartInit;
        }
    }

    /**
     * 走模块开启流程时做超时判断用的,其他地方不用调用该方法.
     *
     * @param isModelStartInit
     */
    @SuppressWarnings("JavaDoc")
    static void setModelStartInit(boolean isModelStartInit) {
        synchronized (obj2) {
            RootUtil.isModelStartInit = isModelStartInit;
        }
    }

    /**
     * 自我卸载时用到,其他地方不用调用该方法.
     */
    @SuppressWarnings("JavaDoc")
    private static boolean isAppSystemUninstall() {
        synchronized (obj3) {
            return isAppSystemUninstall;
        }
    }

    /**
     * 自我卸载时用到,其他地方不用调用该方法.
     *
     * @param isAppSystemUninstall
     */
    @SuppressWarnings("JavaDoc")
    private static void setAppSystemUninstall(boolean isAppSystemUninstall) {
        synchronized (obj3) {
            RootUtil.isAppSystemUninstall = isAppSystemUninstall;
        }
    }

    /**
     * 是否root授权框弹出,走模块开启流程时做超时判断用的,其他地方不用调用该方法.
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    static boolean isStartRootDone() {
        synchronized (obj4) {
            return isStartRootDone;
        }
    }

    /**
     * 走模块开启流程时做超时判断用的,其他地方不用调用该方法.
     *
     * @param isStartRootDone
     */
    @SuppressWarnings("JavaDoc")
    static void setStartRootDone(boolean isStartRootDone) {
        synchronized (obj4) {
            RootUtil.isStartRootDone = isStartRootDone;
        }
    }

    /**
     * 只是获取app是否root,不会真的去执行app获取root权限的命令.
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean isRootAppNotObtain() {
        synchronized (obj5) {
            return isRootApp;
        }
    }

    /**
     * app是否root,而不是手机是否root,调用者需要在子线程里调用.
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static boolean isRootApp() throws InterruptedException {
        return RootUtil.obtainRoot();
    }

    /**
     * 设置app是否root.
     *
     * @param isRootApp
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static void setRootApp(boolean isRootApp) {
        synchronized (obj5) {
            RootUtil.isRootApp = isRootApp;
        }
    }

    /**
     * 获取root权限,在内存中保存CcmtApplication.isRootApp值,如果内存中的值为true,不再重新获取,
     * 直接返回CcmtApplication.isRootApp值.如果如果内存中的值为false,重新获取root权限,
     * 再在内存中保存并返回值.不走模块开启流程.
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static boolean obtainRoot() throws InterruptedException {
        if (!isRootApp) {
            synchronized (obj5) {
                return isRootApp || startRoot();
            }
        }
        return true;
    }

    /**
     * 判断是否为系统app且当前运行的app与所在系统分区的app完全相同,也就是说文件大小也要相同.
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean isSystemApp(Context context) {
        if (!isSystemApp) {
            synchronized (obj6) {
                if (!isSystemApp) {
                    isSystemApp = isAtSystem(context)
                            && new File(obtainAppPathAtSystem(context, context.getPackageName(), false))
                            .length() == new File(obtainAppPathAtSystem(context)).length();
                    return isSystemApp;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    @SuppressWarnings("unused")
    public static boolean isSystemAppNotObtain() {
        synchronized (obj6) {
            return isSystemApp;
        }
    }

    @SuppressWarnings("unused")
    private static void setSystemApp(boolean isSystemApp) {
        synchronized (obj6) {
            RootUtil.isSystemApp = isSystemApp;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isAtSystem(Context context) {
        if (!isAtSystem) {
            isAtSystem = CommonUtil.isSystemApp(context, context.getPackageName());
        }
        return isAtSystem;
    }

    @SuppressWarnings({"JavaDoc", "unchecked", "JavadocReference"})
    static void modelStart(File file) {
        boolean isCopy = file.exists();
        LogUtil.i("isCopy -> " + isCopy);
        LogUtil.i("path -> " + path);
//        setSystemApp(isCopy);
        if (isCopy) {
            LruMap lruMap = LruMap.getInstance();
            ArrayList<String> paths = (ArrayList<String>) lruMap
                    .get("paths");
            if (paths == null) {
                paths = new ArrayList<>();
                paths.add(path);
                lruMap.put("paths", paths, true);
            } else {
                if (!paths.contains(path)) {
                    paths.add(path);
                    lruMap.put("paths", paths, true);
                }
            }
        }
    }

//    /**
//     * 将当前app挂载到系统目录后判断是否应该重启系统
//     *
//     * @param context
//     * @return
//     */
//    @SuppressWarnings("JavaDoc")
//    public static boolean isRestartSystem(Context context) {
//        AbstractPermissionsManager abstractPermissionsManager;
//        boolean b = false;
//        try {
//            abstractPermissionsManager = ObjectUtil.obtainPermissionsManager(context);
//            if (abstractPermissionsManager == null) {
//                PermissionsManager.isEnablePermissions = false;
//                return false;
//            }
//            String packageName = CommonUtil.obtainSystemAppPackageName(context);
//            packageName = packageName == null ? context.getPackageName()
//                    : packageName;
//            int op = PermissionsManager.sOpCode[0];
//            int mode = abstractPermissionsManager.checkOpNoThrow(op, packageName);
//            LogUtil.i("mode -> " + mode);
//            // if (mode == -1) {
//            // PermissionsManager.isEnablePermissions = false;
//            // return false;
//            // }
//            if (mode != PermissionsManager.MODE_ALLOWED) {
//                abstractPermissionsManager.setMode(op, packageName,
//                        PermissionsManager.MODE_ALLOWED);
//            } else {
//                abstractPermissionsManager.setMode(op, packageName,
//                        PermissionsManager.MODE_IGNORED);
//            }
//            int mode2 = abstractPermissionsManager.checkOpNoThrow(op, packageName);
//            LogUtil.i("mode2 -> " + mode2);
//            if (mode == mode2) {
//                if (mode2 == -1) {
//                    LruMap lruMap = LruMap.getInstance();
//                    String systemPath = (String) lruMap.get("systemPath");
//                    if (systemPath != null) {
//                        PermissionsManager.isEnablePermissions = false;
//                        return false;
//                    }
//                    if (RootUtil.path != null) {
//                        lruMap.put("systemPath", RootUtil.path, true);
//                    }
//                } else {
//                    PermissionsManager.isEnablePermissions = true;
//                    return false;
//                }
//                PermissionsManager.isEnablePermissions = false;
//                b = true;
//            } else {
//                abstractPermissionsManager.setMode(op, packageName, mode);
//                PermissionsManager.isEnablePermissions = true;
//                b = false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return b;
//    }

    /**
     * 每次调用都会重新获取root权限,并将获取到的值在内存中保存并返回.不走模块开启流程.
     *
     * @return
     * @throws InterruptedException
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean obtainRootNotSave() throws InterruptedException {
        return startRoot();
    }

    /**
     * 调用该方法申请root权限
     *
     * @return
     * @throws InterruptedException
     */
    @SuppressWarnings("JavaDoc")
    private static boolean startRoot() throws InterruptedException {
        long start = System.currentTimeMillis();
        // boolean available = ShellUtil.SU.available();
        boolean available = ShellUtil.checkRootPermission();
        isRootApp = available;
        LogUtil.i("System.currentTimeMillis()-start -> " + (System.currentTimeMillis() - start));
        return available;
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean sendShell(String[] commands) {
        List<String> list = null;
        try {
            list = ShellUtil.run("su", commands, null, true, true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogUtil.i("list -> " + list);
        return list != null;
    }

    private static List<List<String>> sendRootShell(Context context, boolean isNeedCopyAppToSystemTemp,
                                                    String[] commands,
                                                    boolean isMakeFile) throws InterruptedException {
        return ShellUtil.runShell(context, isNeedCopyAppToSystemTemp, "su", commands, null, true, isMakeFile);
    }

    private static List<List<String>> sendRootShell(String[] commands,
                                                    boolean isMakeFile) throws InterruptedException {
        return ShellUtil.runShell("su", commands, null, true, isMakeFile);
    }

    @SuppressWarnings("unused")
    private static List<List<String>> sendRootShell(String[] commands) throws InterruptedException {
        return sendRootShell(commands, false);
    }

    /**
     * 执行root命令,不执行java代码,返回1个集合对象,其中索引为0代表成功信息集合对象,索引为1代表失败信息集合对象.
     *
     * @param commands
     * @param isMakeFile whether make file
     * @return
     */
    @SuppressWarnings({"WeakerAccess", "JavaDoc"})
    public static boolean sendRootShellOnly(String[] commands,
                                            boolean isMakeFile) throws InterruptedException {
        List<List<String>> list = sendRootShell(commands, isMakeFile);
        LogUtil.i("list -> " + list);
        return list.get(0) != null;
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean sendRootShellOnly(String[] commands) throws InterruptedException {
        return sendRootShellOnly(commands, false);
    }

    /**
     * 执行root命令,并执行java代码,返回1个集合对象,其中索引为0代表成功信息集合对象,索引为1代表失败信息集合对象.
     * 如果root命令执行成功,会执行rootAppSuccessRunnable对象的run()方法,否则会执行rootAppErrorRunnable对象的run()方法.
     * 如果isNeedCopyAppToSystemTemp为true,并且root命令执行成功,又成功刷到系统分区下,这3个条件同时满足了,
     * 也会执行systemAppSuccessRunnable对象的run()方法,否则会执行systemAppErrorRunnable对象的run()方法.
     * 如果rootAppSuccessRunnable对象的run()方法和systemAppSuccessRunnable对象的run()方法都会执行,
     * 那么会先执行systemAppSuccessRunnable对象的run()方法,再执行rootAppSuccessRunnable对象的run()方法.
     * 如果rootAppErrorRunnable对象的run()方法和systemAppErrorRunnable对象的run()方法都会执行,
     * 那么会先执行systemAppErrorRunnable对象的run()方法,再执行rootAppErrorRunnable对象的run()方法.
     *
     * @param context
     * @param isNeedCopyAppToSystemTemp
     * @param commands
     * @param isMakeFile                whether make file
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess", "unused"})
    public static boolean sendRootShellAndExecuteJavaCode(Context context, boolean isNeedCopyAppToSystemTemp,
                                                          String[] commands,
                                                          boolean isMakeFile) throws InterruptedException {
        List<List<String>> list = sendRootShell(context, isNeedCopyAppToSystemTemp, commands, isMakeFile);
        LogUtil.i("list -> " + list);
        return list.get(0) != null;
    }

    /**
     * 执行普通命令,返回1个集合对象,其中索引为0代表成功信息集合对象,索引为1代表失败信息集合对象.
     *
     * @param commands
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private static List<List<String>> sendCommonShell(String[] commands) throws InterruptedException {
        return ShellUtil.runShell("sh", commands, null, true);
    }

    /**
     * 执行普通命令,返回1个集合对象,其中索引为0代表成功信息集合对象,索引为1代表失败信息集合对象.
     *
     * @param commands
     * @return
     */
    @SuppressWarnings({"WeakerAccess", "JavaDoc", "unused"})
    public static boolean sendCommonShellOnly(String[] commands) throws InterruptedException {
        List<List<String>> list = sendCommonShell(commands);
        LogUtil.i("list -> " + list);
        return list.get(0) != null;
    }

    /**
     * 安装apk
     *
     * @param apkPath     欲安装apk的路径
     * @param isReinstall 是否覆盖安装
     * @return
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static boolean installApk(String apkPath, boolean isReinstall) {
        if (isReinstall) {
            return RootUtil
                    .sendShell(new String[]{
                            "pm install -r " + apkPath
                    });
        } else {
            return RootUtil.sendShell(new String[]{
                    "pm install " + apkPath
            });
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRoot(Context context, View view, final String modelMessage,
                              final Runnable rootAppSuccessRunnable,
                              final Runnable systemAppSuccessRunnable,
                              final Runnable rootAppErrorRunnable,
                              final Runnable systemAppErrorRunnable,
                              boolean isNeedCopyAppToSystem, String[] commonds,
                              boolean isMakeFile, boolean isNeedPopupDoNoRoot) {
        doRootWithDialog(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable, rootAppErrorRunnable,
                systemAppErrorRunnable, isNeedCopyAppToSystem, commonds, isMakeFile, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRoot(Context context, View view, final String modelMessage,
                              final Runnable rootAppSuccessRunnable,
                              final Runnable systemAppSuccessRunnable,
                              final Runnable rootAppErrorRunnable,
                              final Runnable systemAppErrorRunnable,
                              boolean isNeedCopyAppToSystem, String[] commonds,
                              boolean isMakeFile) {
        doRoot(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable, rootAppErrorRunnable,
                systemAppErrorRunnable, isNeedCopyAppToSystem, commonds, isMakeFile, true);
    }

    /**
     * 如果只执行需要root权限才能生效的功能,不需要执行相关root命令,请调用该方法.
     *
     * @param context
     * @param view
     * @param modelMessage
     * @param rootAppSuccessRunnable
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void doRoot(Context context, View view, String modelMessage, Runnable rootAppSuccessRunnable) {
        doRoot(context, view, modelMessage, rootAppSuccessRunnable, null, null, null, false, null, false, true);
    }

    /**
     * 进入各个模块前先判断是否已经是系统app,如果是,就执行systemAppSuccessRunnable对象的run()方法.
     * 否则,如果isNeedCopyAppToSystem为true,走提升为系统app的流程.
     * 如果成功提升为系统app,会执行systemAppSuccessRunnable对象的run()方法.
     * 如果提升为系统app失败,会执行systemAppErrorRunnable对象的run()方法.
     * 如果isNeedCopyAppToSystem为false,只获取root权限.
     * 如果获取root权限成功,会执行rootAppSuccessRunnable对象的run()方法.
     * 如果rootAppSuccessRunnable和commonds都不为空,会先执行commonds命令,
     * 再执行rootAppSuccessRunnable对象的run()方法.
     * 如果是只需要app获取root就能做的操作,只传rootAppSuccessRunnable对象就可以了.
     * 如果是必须要app为系统应用才能做的操作,就传systemAppSuccessRunnable对象.
     * 如果app既有root权限又为系统应用,rootAppSuccessRunnable和systemAppSuccessRunnable对象都传了,
     * 那么会先执行systemAppSuccessRunnable对象的run()方法,再执行rootAppSuccessRunnable对象的run()方法,
     * 所以建议不要两个对象都传.
     * 注意,所有Runnable对象的run()方法都是在主线程执行.
     *
     * @param context                  上下文
     * @param view                     触发事件的控件,可以为空.
     * @param modelMessage             如果手机没有root,对话框要显示的消息,可以为空.
     * @param rootAppSuccessRunnable   app获取root成功后要执行的任务,可以为空.
     * @param systemAppSuccessRunnable 提升为系统app成功后要执行的任务,可以为空.
     * @param rootAppErrorRunnable     app获取root失败后要执行的任务,可以为空.
     * @param systemAppErrorRunnable   提升为系统app失败后要执行的任务,可以为空.
     * @param isNeedCopyAppToSystem    如果是需要系统应用才能生效的功能就传true,如果只需要root权限就能生效传false.
     * @param commonds                 每次走模块开启流程时的root命令,当该参数为空时,如果isNeedCopyAppToSystem
     *                                 参数为true,会先执行systemAppErrorRunnable对象的run()方法,
     *                                 再执行的rootAppSuccessRunnable对象的run()方法.如果isNeedCopyAppToSystem
     *                                 参数为false,会执行rootAppSuccessRunnable对象的run()方法.
     * @param isMakeFile               是否操作文件且需要root权限的命令,例如chmod 777 目录或文件名.
     * @param isNeedPopupDoNoRoot      如果手机没有root,是否弹出怎么root对话框,
     *                                 有的模块有root成功和root失败执行不同功能的需求,这就不能弹怎么root对话框.
     * @param isDialogShow             转圈图标是否以对话框形式显示,true为以Dialog显示,false为以Activity显示.
     */
    private static void doRootNew(Context context, View view, final String modelMessage,
                                  final Runnable rootAppSuccessRunnable, final Runnable systemAppSuccessRunnable,
                                  final Runnable rootAppErrorRunnable, final Runnable systemAppErrorRunnable,
                                  boolean isNeedCopyAppToSystem, String[] commonds, boolean isMakeFile,
                                  boolean isNeedPopupDoNoRoot, boolean isDialogShow) {
        if (view != null) {
            view.setEnabled(false);
        }
        if (isNeedCopyAppToSystem) {
            // 需要系统应用才能生效
            if (isAtSystem(context)) {
                // 当前app是系统应用
                if (systemAppSuccessRunnable != null) {
                    systemAppSuccessRunnable.run();
                }
                if (view != null) {
                    view.setEnabled(true);
                }
                return;
            } else {
                // 当前app不是系统应用
                if ((boolean) isSystemAppByFileExists(context).get(0)) {
                    // 已经成功刷到系统分区,就不用再刷1次了,但是需要重启.
                    DialogFractory.showRestartPhoneDialog(context, true, context.getPackageName(), () -> {
                        if (view != null) {
                            view.setEnabled(true);
                        }
                    });
                    return;
                }
            }
            startRootNew(context, view, modelMessage, rootAppSuccessRunnable, rootAppErrorRunnable,
                    systemAppErrorRunnable, true, commonds, isMakeFile, true, isNeedPopupDoNoRoot, isDialogShow);
        } else {
            // 只要root权限就能生效
            startRootNew(context, view, modelMessage, rootAppSuccessRunnable, rootAppErrorRunnable,
                    systemAppErrorRunnable, false, commonds, isMakeFile, false, isNeedPopupDoNoRoot, isDialogShow);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRootWithDialog(Context context, View view, final String modelMessage,
                                        final Runnable rootAppSuccessRunnable,
                                        final Runnable systemAppSuccessRunnable,
                                        final Runnable rootAppErrorRunnable,
                                        final Runnable systemAppErrorRunnable,
                                        boolean isNeedCopyAppToSystem, String[] commonds,
                                        boolean isMakeFile, boolean isNeedPopupDoNoRoot) {
        doRootNew(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                rootAppErrorRunnable, systemAppErrorRunnable, isNeedCopyAppToSystem,
                commonds, isMakeFile, isNeedPopupDoNoRoot, true);
    }

    @SuppressWarnings("unused")
    public static void doRootWithDialog(Context context, View view, final Runnable rootAppSuccessRunnable,
                                        final Runnable rootAppErrorRunnable, String[] commonds,
                                        boolean isMakeFile, boolean isNeedPopupDoNoRoot) {
        doRootWithDialog(context, view, null, rootAppSuccessRunnable, null, rootAppErrorRunnable,
                null, false, commonds, isMakeFile, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("unused")
    public static void doRootWithDialog(Context context, View view, final Runnable rootAppSuccessRunnable,
                                        final Runnable rootAppErrorRunnable, String[] commonds,
                                        boolean isMakeFile) {
        doRootWithDialog(context, view, null, rootAppSuccessRunnable, null, rootAppErrorRunnable,
                null, false, commonds, isMakeFile, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRootWithActivity(Context context, View view, final String modelMessage,
                                          final Runnable rootAppSuccessRunnable,
                                          final Runnable systemAppSuccessRunnable,
                                          final Runnable rootAppErrorRunnable,
                                          final Runnable systemAppErrorRunnable,
                                          boolean isNeedCopyAppToSystem, String[] commonds,
                                          boolean isMakeFile, boolean isNeedPopupDoNoRoot) {
        doRootNew(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                rootAppErrorRunnable, systemAppErrorRunnable, isNeedCopyAppToSystem,
                commonds, isMakeFile, isNeedPopupDoNoRoot, false);
    }

    @SuppressWarnings("unused")
    public static void doRootWithActivity(Context context, View view,
                                          final Runnable rootAppSuccessRunnable,
                                          final Runnable rootAppErrorRunnable,
                                          String[] commonds, boolean isMakeFile,
                                          boolean isNeedPopupDoNoRoot) {
        doRootWithActivity(context, view, null, rootAppSuccessRunnable, null, rootAppErrorRunnable,
                null, false, commonds, isMakeFile, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("unused")
    public static void doRootWithActivity(Context context, View view,
                                          final Runnable rootAppSuccessRunnable,
                                          final Runnable rootAppErrorRunnable,
                                          String[] commonds, boolean isMakeFile) {
        doRootWithActivity(context, view, null, rootAppSuccessRunnable, null, rootAppErrorRunnable,
                null, false, commonds, isMakeFile, true);
    }

    @SuppressWarnings("unchecked")
    private static void startRootNew(final Context context, final View view, final String modelMessage,
                                     final Runnable rootAppSuccessRunnable, final Runnable rootAppErrorRunnable,
                                     final Runnable systemAppErrorRunnable, final boolean isNeedCopyAppToSystem,
                                     final String[] commonds, final boolean isMakeFile, final boolean isNeedRestart,
                                     final boolean isNeedPopupDoNoRoot, boolean isDialogShow) {
        synchronized (RootUtil.obj7) {
            RootUtil.isModelStartInit = true;

            RootUtil.isDialogShow = isDialogShow;
        }

        Dialog progressDialog = null;
//        if (isDialogShow) {
//            progressDialog = DialogFractory.createFullScreenProgressDialogNew(context);
//            progressDialog.show();
//        } else {
//            DialogFractory.showProgressDialog(context, true);
//        }
        if (isDialogShow) {
            progressDialog = DialogFractory.createFullScreenProgressDialogNew(context);
        }

        List<String> list = null;
        List<Object> makeFileCommands = null;
        if (isNeedCopyAppToSystem) {
            // 把app刷到系统分区
            makeFileCommands = ShellUtil.obtainMakeFileCommands(context.getPackageResourcePath(), false);
            list = (List<String>) makeFileCommands.get(0);
        } else {
            if (commonds != null && commonds.length > 0) {
                list = new ArrayList<>();
                if (isMakeFile) {
                    list.add("mount -o remount,rw -t /dev/block/mtdblock0 /system\n");
                }
                Collections.addAll(list, commonds);
                if (isMakeFile) {
                    list.add("mount -o remount,ro -t /dev/block/mtdblock0 /system\n");
                }
            }
        }
        List<Object> makeFileCommandsTemp = makeFileCommands;
        Dialog progressDialogTemp = progressDialog;
        new RootShell.RootCommand().setCallback(new RootShell.RootCommand.Callback() {
            @Override
            public void cbFunc(RootShell.RootCommand state) {
                LogUtil.i("cbFunc()");
                LogUtil.i("Thread.currentThread().getName() -> " + Thread.currentThread().getName());
                LogUtil.i("state.exitCode -> " + state.exitCode);
                LogUtil.i("RootShell.rootState -> " + RootShell.rootState);

                boolean onMainThread = RootUtil.isOnMainThread();
                if (state.exitCode == 0) {
                    // root执行成功
                    LogUtil.i("root执行成功");

                    if (onMainThread) {
                        // 主线程
                        if (state.commandIndex < state.script.size()) {
                            // 不是RootCommand对象的最后1条命令
                            LogUtil.i("不是RootCommand对象的最后1条命令");

                            return;
                        }

                        if (!isNeedCopyAppToSystem) {
                            // 不需要刷系统分区
                            processRootSuccessNotNeedSystem(view, rootAppSuccessRunnable,
                                    progressDialogTemp);
                        } else {
                            // 需要刷系统分区
                            processRootSuccessNeedSystem(context, view, makeFileCommandsTemp,
                                    systemAppErrorRunnable, rootAppSuccessRunnable, isNeedRestart,
                                    progressDialogTemp);
                        }
                    } else {
                        // 子线程
                        ThreadManager.post(() -> {
                            if (state.commandIndex < state.script.size()) {
                                // 不是RootCommand对象的最后1条命令
                                LogUtil.i("不是RootCommand对象的最后1条命令");

                                return;
                            }

                            if (!isNeedCopyAppToSystem) {
                                // 不需要刷系统分区
                                processRootSuccessNotNeedSystem(view, rootAppSuccessRunnable,
                                        progressDialogTemp);
                            } else {
                                // 需要刷系统分区
                                processRootSuccessNeedSystem(context, view, makeFileCommandsTemp,
                                        systemAppErrorRunnable, rootAppSuccessRunnable, isNeedRestart,
                                        progressDialogTemp);
                            }
                        });
                    }
                } else {
                    // root执行失败
                    LogUtil.i("root执行失败");

                    if (onMainThread) {
                        // 主线程
                        if (state.exitCode == RootShell.EXIT_NO_ROOT_PHONE) {
                            // 手机没有root
                            LogUtil.i("手机没有root");

                            processRootErrorNoRootPhone(context, view, modelMessage, rootAppErrorRunnable,
                                    systemAppErrorRunnable, isNeedPopupDoNoRoot, progressDialogTemp);

                            return;
                        }

                        // 手机有root
                        if (state.exitCode == RootShell.EXIT_NO_ROOT_ACCESS) {
                            // 用户拒绝授权给应用
                            processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                    systemAppErrorRunnable, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode == RootShell.EXIT_TIMEOUT) {
                            // 超时
                            processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                    systemAppErrorRunnable, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode > 0) {
                            // 命令执行失败
                            LogUtil.i("命令执行失败");

                            processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                    systemAppErrorRunnable, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode == RootShell.EXIT_NO_COMMAND) {
                            // 没有要执行的root命令
                            LogUtil.i("没有要执行的root命令");
                        }

                        if (!isNeedCopyAppToSystem) {
                            // 不需要刷系统分区
                            processRootSuccessNotNeedSystem(view, rootAppSuccessRunnable,
                                    progressDialogTemp);
                        } else {
                            // 需要刷系统分区
                            processRootSuccessNeedSystem(context, view, makeFileCommandsTemp,
                                    systemAppErrorRunnable, rootAppSuccessRunnable, isNeedRestart,
                                    progressDialogTemp);
                        }
                    } else {
                        // 子线程
                        ThreadManager.post(() -> {
                            if (state.exitCode == RootShell.EXIT_NO_ROOT_PHONE) {
                                // 手机没有root
                                LogUtil.i("手机没有root");

                                processRootErrorNoRootPhone(context, view, modelMessage, rootAppErrorRunnable,
                                        systemAppErrorRunnable, isNeedPopupDoNoRoot, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_NO_ROOT_ACCESS) {
                                // 用户拒绝授权给应用
                                processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                        systemAppErrorRunnable, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_TIMEOUT) {
                                // 超时
                                processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                        systemAppErrorRunnable, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode > 0) {
                                // 命令执行失败
                                LogUtil.i("命令执行失败");

                                processRootErrorNoRootAccess(view, rootAppErrorRunnable,
                                        systemAppErrorRunnable, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_NO_COMMAND) {
                                // 没有要执行的root命令
                                LogUtil.i("没有要执行的root命令");
                            }

                            if (!isNeedCopyAppToSystem) {
                                // 不需要刷系统分区
                                processRootSuccessNotNeedSystem(view, rootAppSuccessRunnable,
                                        progressDialogTemp);
                            } else {
                                // 需要刷系统分区
                                processRootSuccessNeedSystem(context, view, makeFileCommandsTemp,
                                        systemAppErrorRunnable, rootAppSuccessRunnable, isNeedRestart,
                                        progressDialogTemp);
                            }
                        });
                    }
                }
            }
        }).setReopenShell(true).setRes(new StringBuilder()).run(context, true, list, progressDialogTemp);
    }

    private static void processRootErrorNoRootAccess(View view, Runnable rootAppErrorRunnable,
                                                     Runnable systemAppErrorRunnable, Dialog progressDialog) {
        if (systemAppErrorRunnable != null) {
            systemAppErrorRunnable.run();
        }

        if (rootAppErrorRunnable != null) {
            rootAppErrorRunnable.run();
        }

        if (view != null) {
            ToastUtil.showShort(view.getContext(), R.string.error_su);
        } else {
            ToastUtil.showShort(CcmtApplication.application, R.string.error_su);
        }

        endRootNew(view, progressDialog);
    }

    private static void processRootErrorNoRootPhone(Context context, View view, String modelMessage,
                                                    Runnable rootAppErrorRunnable,
                                                    Runnable systemAppErrorRunnable,
                                                    boolean isNeedPopupDoNoRoot,
                                                    Dialog progressDialog) {
        RootUtil.doNoRootNew(context, view, modelMessage, rootAppErrorRunnable,
                systemAppErrorRunnable, isNeedPopupDoNoRoot, progressDialog);
    }

    private static void processRootSuccessNeedSystem(Context context, View view,
                                                     List<Object> makeFileCommandsTemp,
                                                     Runnable systemAppErrorRunnable,
                                                     Runnable rootAppSuccessRunnable,
                                                     boolean isNeedRestart, Dialog progressDialog) {
        RootUtil.modelStart((File) makeFileCommandsTemp.get(1));

        LogUtil.i("((File)makeFileCommands.get(2)).exists() -> " + ((File) makeFileCommandsTemp.get(2)).exists());

        String systemAppPath = obtainSystemAppPath();
        if (systemAppPath == null || !new File(systemAppPath).exists()) {
            // 当前手机无法进行文件挂载,所以刷系统分区失败.
            LogUtil.i("当前手机无法进行文件挂载,所以刷系统分区失败.");
            LruMap.getInstance().put(LRU_IS_ONLY_READ_SYSTEM, true);

            if (systemAppErrorRunnable != null) {
                systemAppErrorRunnable.run();
            }
            if (rootAppSuccessRunnable != null) {
                rootAppSuccessRunnable.run();
            }

            endRootNew(view, progressDialog);

            return;
        }

        if (systemAppErrorRunnable != null) {
            systemAppErrorRunnable.run();
        }
        if (rootAppSuccessRunnable != null) {
            rootAppSuccessRunnable.run();
        }

        if (isNeedRestart) {
            DialogFractory.showRestartPhoneDialog(context, true,
                    context.getPackageName(), () -> endRootNew(view, progressDialog));
        } else {
            endRootNew(view, progressDialog);
        }
    }

    private static void processRootSuccessNotNeedSystem(View view, Runnable rootAppSuccessRunnable,
                                                        Dialog progressDialog) {
        if (rootAppSuccessRunnable != null) {
            rootAppSuccessRunnable.run();
        }

        endRootNew(view, progressDialog);
    }

    private static void endRootNew(View view, Dialog progressDialog) {
        if (view != null) {
            view.setEnabled(true);
        }

        synchronized (RootUtil.obj7) {
            RootUtil.isModelStartInit = false;
            RootUtil.isStartRootDone = false;
            RootUtil.isDialogShow = null;
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        } else {
            if (view != null) {
                DialogFractory.closeProgressDialog(view.getContext());
            } else {
                DialogFractory.closeProgressDialog(CcmtApplication.application);
            }
        }
    }

    /**
     * 先弹对话框,用户点开启按钮后,才把app刷到系统分区.要知道每个参数的具体含义,请看参数最多的doRoot()方法.
     * 只要是系统应用才能执行的功能,且要走模块开启流程的话,请调用该方法.
     *
     * @param context
     * @param modelMessage
     * @param systemAppSuccessRunnable
     * @param rootAppErrorRunnable
     * @param systemAppErrorRunnable
     * @param commonds
     * @param isMakeFile
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static void doRootSystemApp(Context context, View view, String modelMessage,
                                       Runnable systemAppSuccessRunnable, Runnable rootAppErrorRunnable,
                                       Runnable systemAppErrorRunnable, String[] commonds,
                                       boolean isMakeFile, boolean isNeedPopupDoNoRoot) {
        doRootSystemWithDialog(context, view, modelMessage, null, systemAppSuccessRunnable, rootAppErrorRunnable,
                systemAppErrorRunnable, commonds, isMakeFile, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemApp(Context context, View view, String modelMessage,
                                       Runnable systemAppSuccessRunnable, Runnable rootAppErrorRunnable,
                                       Runnable systemAppErrorRunnable, String[] commonds,
                                       boolean isMakeFile) {
        doRootSystemApp(context, view, modelMessage, systemAppSuccessRunnable, rootAppErrorRunnable,
                systemAppErrorRunnable, commonds, isMakeFile, true);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemApp(Context context, String modelMessage,
                                       Runnable systemAppSuccessRunnable, Runnable rootAppErrorRunnable,
                                       Runnable systemAppErrorRunnable, String[] commonds,
                                       boolean isMakeFile) {
        doRootSystemApp(context, null, modelMessage, systemAppSuccessRunnable, rootAppErrorRunnable,
                systemAppErrorRunnable, commonds, isMakeFile, true);
    }

    /**
     * 先弹对话框,用户点开启按钮后,才把app刷到系统分区.要知道每个参数的具体含义,请看参数最多的doRoot()方法.
     * 只要是系统应用才能执行的功能,且要走模块开启流程的话,请调用该方法.
     *
     * @param context                  上下文,不能为空.
     * @param view                     触发事件的控件,可以为空.
     * @param modelMessage             如果手机没有root,对话框要显示的消息.可以为空.
     * @param rootAppSuccessRunnable   app获取root成功后要执行的任务,可以为空.
     * @param systemAppSuccessRunnable 提升为系统app成功后要执行的任务,可以为空.
     * @param rootAppErrorRunnable     app获取root失败后要执行的任务,可以为空.
     * @param systemAppErrorRunnable   提升为系统app失败后要执行的任务,可以为空.
     * @param commonds                 每次走模块开启流程时的root命令,当该参数为空时,如果isNeedCopyAppToSystem
     *                                 参数为true,会先执行systemAppErrorRunnable对象的run()方法,
     *                                 再执行的rootAppSuccessRunnable对象的run()方法.如果isNeedCopyAppToSystem
     *                                 参数为false,会执行rootAppSuccessRunnable对象的run()方法.
     * @param isMakeFile               是否操作文件且需要root权限的命令,例如chmod 777 目录或文件名.
     * @param isNeedPopupDoNoRoot      如果手机没有root,是否弹出怎么root对话框,
     *                                 有的模块有root成功和root失败执行不同功能的需求,这就不能弹怎么root对话框.
     * @param isDialogShow             转圈图标是否以对话框形式显示,true为以Dialog显示,false为以Activity显示.
     */
    @SuppressWarnings({"JavaDoc"})
    private static void doRootSystemAppNew(Context context, View view, final String modelMessage,
                                           final Runnable rootAppSuccessRunnable,
                                           final Runnable systemAppSuccessRunnable,
                                           final Runnable rootAppErrorRunnable,
                                           final Runnable systemAppErrorRunnable,
                                           String[] commonds,
                                           boolean isMakeFile, boolean isNeedPopupDoNoRoot,
                                           boolean isDialogShow) {
        if (view != null) {
            view.setEnabled(false);
        }

        if (LruMap.getInstance().get(LRU_IS_ONLY_READ_SYSTEM) != null) {
            // 无法进行文件挂载
            if (systemAppErrorRunnable != null) {
                systemAppErrorRunnable.run();
            }
            ToastUtil.showLong(context, R.string.error_system_app);
            if (view != null) {
                view.setEnabled(true);
            }
            return;
        }

        boolean systemApp = isAtSystem(context);
        LogUtil.i("systemApp -> " + systemApp);

        DialogFractory.showUninstallCCMTNavigationDialogNew(context, view,
                () -> {
                    if (isDialogShow) {
                        doRootWithDialog(context, view,
                                modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                                rootAppErrorRunnable, systemAppErrorRunnable, true,
                                commonds, isMakeFile, isNeedPopupDoNoRoot);
                    } else {
                        doRootWithActivity(context, view,
                                modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                                rootAppErrorRunnable, systemAppErrorRunnable, true,
                                commonds, isMakeFile, isNeedPopupDoNoRoot);
                    }
                },
                systemAppSuccessRunnable, !systemApp);
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRootSystemWithDialog(Context context, View view, final String modelMessage,
                                              final Runnable rootAppSuccessRunnable,
                                              final Runnable systemAppSuccessRunnable,
                                              final Runnable rootAppErrorRunnable,
                                              final Runnable systemAppErrorRunnable,
                                              String[] commonds, boolean isMakeFile,
                                              boolean isNeedPopupDoNoRoot) {
        doRootSystemAppNew(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                rootAppErrorRunnable, systemAppErrorRunnable, commonds, isMakeFile, isNeedPopupDoNoRoot, true);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemWithDialog(Context context, View view,
                                              final Runnable systemAppSuccessRunnable,
                                              final Runnable systemAppErrorRunnable,
                                              String[] commonds, boolean isNeedPopupDoNoRoot) {
        doRootSystemWithDialog(context, view, null, null, systemAppSuccessRunnable, null,
                systemAppErrorRunnable, commonds, true, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemWithDialog(Context context, View view,
                                              final Runnable systemAppSuccessRunnable,
                                              final Runnable systemAppErrorRunnable,
                                              String[] commonds) {
        doRootSystemWithDialog(context, view, null, null, systemAppSuccessRunnable, null,
                systemAppErrorRunnable, commonds, true, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static void doRootSystemWithActivity(Context context, View view, final String modelMessage,
                                                final Runnable rootAppSuccessRunnable,
                                                final Runnable systemAppSuccessRunnable,
                                                final Runnable rootAppErrorRunnable,
                                                final Runnable systemAppErrorRunnable,
                                                String[] commonds, boolean isMakeFile,
                                                boolean isNeedPopupDoNoRoot) {
        doRootSystemAppNew(context, view, modelMessage, rootAppSuccessRunnable, systemAppSuccessRunnable,
                rootAppErrorRunnable, systemAppErrorRunnable, commonds, isMakeFile, isNeedPopupDoNoRoot, false);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemWithActivity(Context context, View view,
                                                final Runnable systemAppSuccessRunnable,
                                                final Runnable systemAppErrorRunnable,
                                                String[] commonds, boolean isNeedPopupDoNoRoot) {
        doRootSystemWithActivity(context, view, null, null, systemAppSuccessRunnable,
                null, systemAppErrorRunnable, commonds, true, isNeedPopupDoNoRoot);
    }

    @SuppressWarnings("unused")
    public static void doRootSystemWithActivity(Context context, View view,
                                                final Runnable systemAppSuccessRunnable,
                                                final Runnable systemAppErrorRunnable,
                                                String[] commonds) {
        doRootSystemWithActivity(context, view, null, null, systemAppSuccessRunnable,
                null, systemAppErrorRunnable, commonds, true, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static void doNoRoot(Context context, View view, String modelTitle,
                                String modelMessage, String modelAllowText,
                                String modelDenyText, Runnable rootAppErrorRunnable) {
//        ArrayList<Object> list = new ArrayList<>();
//        list.add(context);
//        list.add(view);
//        list.add(modelTitle);
//        list.add(modelMessage);
//        list.add(modelAllowText);
//        list.add(modelDenyText);
//        list.add(rootAppErrorRunnable);
//        Message message = Message.obtain();
//        message.what = 4;
//        message.obj = list;
//        handler.sendMessage(message);
        ThreadManager.post(() -> {
            if (rootAppErrorRunnable != null) {
                rootAppErrorRunnable.run();
            }

            DialogFractory.closeProgressDialog(context);

//            手机没有root, 点击确定按钮跳转到官网.
            DialogFractory.showAppNoRootDialog(context, modelTitle, modelMessage, modelAllowText,
                    modelDenyText, (dialog, which) -> {
                        if (view != null) {
                            view.setEnabled(true);

                            LruMap.getInstance().remove("view", false);
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.supersu.com/appmaster/howtoroot"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }, (dialog, which) -> {
                        if (view != null) {
                            view.setEnabled(true);

                            LruMap.getInstance().remove("view", false);
                        }
                    });
        });
    }

    @SuppressWarnings("WeakerAccess")
    public static void doNoRoot(Context context, String modelMessage) {
        Resources resources = context.getResources();
        doNoRoot(context, (View) LruMap.getInstance().get("view"), null, modelMessage, resources.getString(R.string.root_how_to),
                resources.getString(R.string.default_negative_btn_label), null);
    }

    @SuppressWarnings("unused")
    public static void doNoRoot(Context context) {
        doNoRoot(context, context.getResources().getString(R.string.error_su));
    }

    private static void doNoRootNew(Context context, View view, String modelTitle, String modelMessage,
                                    String modelAllowText, String modelDenyText,
                                    Runnable rootAppErrorRunnable, Runnable systemAppErrorRunnable,
                                    boolean isNeedPopupDoNoRoot, Dialog progressDialog) {
//        ArrayList<Object> list = new ArrayList<>();
//        list.add(context);
//        list.add(view);
//        list.add(modelTitle);
//        list.add(modelMessage);
//        list.add(modelAllowText);
//        list.add(modelDenyText);
//        list.add(rootAppErrorRunnable);
//        list.add(systemAppErrorRunnable);
//        list.add(isNeedPopupDoNoRoot);
//        list.add(progressDialog);
//        Message message = Message.obtain();
//        message.what = 5;
//        message.obj = list;
//        handler.sendMessage(message);
        ThreadManager.post(() -> {
//            app获取root失败, 没走模块开启流程.
//            ToastUtil.showLong(context, R.string.error_su);
            if (systemAppErrorRunnable != null) {
                systemAppErrorRunnable.run();
            }
            if (rootAppErrorRunnable != null) {
                rootAppErrorRunnable.run();
            }

//            手机没有root, 点击确定按钮跳转到官网.
            if (isNeedPopupDoNoRoot) {
//                if (progressDialog != null) {
//                    progressDialog.dismiss();
//                } else {
//                    DialogFractory.closeProgressDialog(context);
//                }

                DialogFractory.showAppNoRootDialog(context, view, modelTitle,
                        modelMessage,
                        modelAllowText, modelDenyText, (dialog, which) -> {
                            RootUtil.endRootNew(view, progressDialog);

//                            if (view != null) {
//                                view.setEnabled(true);
//                            }
//
//                            synchronized (RootUtil.obj) {
//                                RootUtil.isModelStartInit = false;
//                                RootUtil.isStartRootDone = false;
//                                RootUtil.isDialogShow = null;
//                            }

                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://www.supersu.com/appmaster/howtoroot"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }, (dialog, which) -> {
                            RootUtil.endRootNew(view, progressDialog);

//                            if (view != null) {
//                                view.setEnabled(true);
//                            }
//
//                            synchronized (RootUtil.obj) {
//                                RootUtil.isModelStartInit = false;
//                                RootUtil.isStartRootDone = false;
//                                RootUtil.isDialogShow = null;
//                            }
                        });
            } else {
                RootUtil.endRootNew(view, progressDialog);
            }
        });
    }

    private static void doNoRootNew(Context context, View view, String modelTitle, String modelMessage,
                                    Runnable rootAppErrorRunnable, Runnable systemAppErrorRunnable,
                                    boolean isNeedPopupDoNoRoot, Dialog progressDialog) {
        Resources resources = context.getResources();
        doNoRootNew(context, view, modelTitle, modelMessage, resources.getString(R.string.root_how_to),
                resources.getString(R.string.default_negative_btn_label), rootAppErrorRunnable,
                systemAppErrorRunnable, isNeedPopupDoNoRoot, progressDialog);
    }

    private static void doNoRootNew(Context context, View view, String modelMessage,
                                    Runnable rootAppErrorRunnable, Runnable systemAppErrorRunnable,
                                    boolean isNeedPopupDoNoRoot, Dialog progressDialog) {
        doNoRootNew(context, view, null, modelMessage, rootAppErrorRunnable, systemAppErrorRunnable,
                isNeedPopupDoNoRoot, progressDialog);
    }

    /**
     * 如果指定包名是当前app的包名,判断app是否为系统应用,
     * 如果app在系统分区且运行时当前目录的apk大小和系统分区中apk的大小相等,就是系统应用,否则为个人应用.
     * 如果指定包名是其他app的包名,判断app是否为系统应用,如果app在系统分区且,就是系统应用,否则为个人应用.
     *
     * @return 索引0为Boolean类型, 代表指定包名的路径是否在系统分区存在.索引1为String类型, 代表指定包名的路径.
     */
    @SuppressWarnings({"ConstantConditions", "WeakerAccess"})
    public static List<Object> isSystemAppByFileExists(Context context, String packageName) {
        ArrayList<Object> result;
        boolean isSame = context.getPackageName().equals(packageName);
        if (isSame) {
            synchronized (RootUtil.class) {
                if (isSame) {
                    String path = RootUtil.obtainAppPathAtSystem(context);
//                    File file = null;
                    if (isAtSystem(context) || path != null) {
                        result = new ArrayList<>();
                        result.add(true);
                        result.add(path);
                        return result;
                    }

//                    boolean exists = file.exists();
//                    if (exists && file.length() == new File(context.getPackageResourcePath())
//                            .length()) {
//                        isSystemApp = true;
//                    }
                    result = new ArrayList<>();
                    result.add(false);
                    result.add(null);
                    return result;
                } else {
                    return isFileExists(context, packageName);
                }
            }
        } else {
            return isFileExists(context, packageName);
        }
    }

    /**
     * 判断当前app是否为系统应用,如果app在系统分区且运行时当前目录的apk大小和系统分区中apk的大小相等,
     * 就是系统应用,否则为个人应用.
     *
     * @return 索引0为Boolean类型, 代表当前app的路径是否在系统分区存在.索引1为String类型, 代表当前app的路径.
     */
    @SuppressWarnings("WeakerAccess")
    public static List<Object> isSystemAppByFileExists(Context context) {
        return isSystemAppByFileExists(context, context.getPackageName());
    }

    private static List<Object> isFileExists(Context context, String packageName) {
        ArrayList<Object> result;
        String path = RootUtil.obtainAppPathAtSystem(context, packageName, false);
        File file = null;
        if (path != null) {
            file = new File(path);
        }
        if (file != null && file.exists()) {
            result = new ArrayList<>();
            result.add(true);
            result.add(path);
            return result;
        }
        result = new ArrayList<>();
        result.add(false);
        result.add(path);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String obtainSystemAppPath() {
        LruMap lruMap = LruMap.getInstance();
        List<String> paths = (List<String>) lruMap.get("paths");
        if (paths != null) {
            String path;
            for (int i = 0; i < paths.size(); i++) {
                path = paths.get(i);
                if (new File(path).exists()) {
                    return path;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    public static boolean uninstallOtherApk(Context context, List<String> packageNames, boolean isKeep) {
        boolean result;
        ArrayList<String> list1 = null;
        ArrayList<String> list2 = null;
        try {
            if (!isRootApp()) {
                // 如果当前应用是系统应用,静默卸载指定包名对应的应用.
                String packageName = null;
                for (int i = 0; i < packageNames.size(); i++) {
                    packageName = packageNames.get(i);
                    if (isKeep) {
                        if (list1 == null) {
                            list1 = new ArrayList<>();
                        }
                        list1.add("pm uninstall -k " + packageName);
                    } else {
                        if (list2 == null) {
                            list2 = new ArrayList<>();
                        }
                        list2.add("pm uninstall " + packageName);
                    }
                }
                if (isKeep) {
                    if (list1 != null) {
                        result = RootUtil.sendShell(list1.toArray(new String[list1.size()]));
                    }
                } else {
                    if (list2 != null) {
                        result = RootUtil.sendShell(list2.toArray(new String[list2.size()]));
                    }
                }
                result = true;
            } else {
                // 如果当前应用是普通应用,用什么方式卸载指定包名对应的应用.
                result = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = false;
            doNoRoot(context, context.getResources().getString(R.string.error_su));
        }
        return result;
    }

    /**
     * 卸载apk
     *
     * @param packageName 欲卸载apk的包名
     * @param isKeep      是否保持apk的数据
     * @return
     */
    @SuppressWarnings({"unchecked", "JavaDoc", "WeakerAccess"})
    public static String[] uninstallOtherApk(Context context, String packageName, boolean isKeep) {
        String temp = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            temp = deleteApkPathReturnString(context.getPackageManager()
                    .getApplicationInfo(packageName, 0).sourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (temp != null) {
            list.add(temp);
        }

        if (isKeep) {
            list.add("pm uninstall -k " + packageName);
        } else {
            list.add("pm uninstall " + packageName);
        }

        if (list.size() == 0) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 卸载当前app.
     * 注意,在子线程调用.
     *
     * @param context
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void uninstallSelfApk(Context context, boolean isSystemApp) {
        if (isAppSystemUninstall()) {
            ThreadManager.post(() -> DialogFractory.showRestartPhoneDialog(context, false));
            return;
        }
        if (isSystemApp) {
            uninstallSelfApk(context, context.getPackageName(), false);
        } else {
            if (!isAppCommonUninstall) {
                uninstallCommonApk(context, context.getPackageName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void uninstallSelfApk(Context context, String packageName,
                                         boolean isKeep) {
        ArrayList<String> list = new ArrayList<>();
        LruMap lruMap = LruMap.getInstance();
        ArrayList<String> paths = (ArrayList<String>) lruMap.get("paths");
        LogUtil.i("paths -> " + paths);
        String deleteSelfApkPath;
        if (paths != null) {
            String path;
            for (int i = 0; i < paths.size(); i++) {
                path = paths.get(i);
                if (path != null) {
                    deleteSelfApkPath = deleteApkPathReturnString(path);
                    if (deleteSelfApkPath != null) {
                        list.add(deleteSelfApkPath);
                    }
                }
            }
//                lruMap.remove("paths");

            // 删除app所在data分区的路径
            List<String> deleteDataPaths = RootUtil.deleteDataPath(context);
            if (deleteDataPaths != null) {
                list.addAll(deleteDataPaths);
            }

            if (isKeep) {
                list.add("pm uninstall -k "
                        + packageName);
            } else {
                list.add("pm uninstall "
                        + packageName);
            }
            String[] commands = list.toArray(new String[list.size()]);
            LogUtil.i("Arrays.toString(commands) -> " + Arrays.toString(commands));

            lruMap.put("uninstallSelf", true);
            ThreadManager.post(() -> doRoot(context, null,
                    context.getResources().getString(R.string.error_su), () -> {
                        // 删除app所在system分区的路径
                        lruMap.remove("paths");

                        LogUtil.i("卸载成功");
                        setAppSystemUninstall(true);
                        if (CommonUtil.isRunningAppForMainProcess(context)) {
                            // 暂时保留,以后还会用到.
//                                boolean appExists = CommonUtil.isAppExists(context, packageName);
//                                LogUtil.i("CommonUtil.isAppExists(context,packageName) -> " + appExists);
//                                if (appExists) {
//                                    LogUtil.i("准备重启");
//                                    DialogFractory.showRestartPhoneDialog(context, false);
//                                } else {
//                                    LogUtil.i("退出");
////                            android.os.Process.killProcess(android.os.Process.myPid());
//                                    System.exit(0);
//                                }
                            DialogFractory.showRestartPhoneDialog(context, false);
                        }
                    }, null, () -> LogUtil.i("卸载失败"), null, false, commands, true));
        }
    }

    /**
     * 卸载指定包名的app.
     *
     * @param context
     * @param packageName
     * @param rootAppSuccessRunnable
     */
    @SuppressWarnings({"WeakerAccess", "JavaDoc"})
    public static void uninstallOtherSystemApk(Context context, View view, String packageName,
                                               Runnable rootAppSuccessRunnable, boolean isShowRestartDialog) {
        if ((boolean) isSystemAppByFileExists(context, packageName).get(0)) {
            RootUtil.doRoot(context, view, context.getResources().getString(R.string.error_su), () -> {
                        LogUtil.i("卸载成功");
                        if (rootAppSuccessRunnable != null) {
                            rootAppSuccessRunnable.run();
                        }
                        if ((boolean) isSystemAppByFileExists(context, packageName).get(0)
                                || CommonUtil.isAppExists(context, packageName)) {
                            LogUtil.i("这里就需要重启了");
                            if (isShowRestartDialog) {
                                DialogFractory.showRestartPhoneDialog(context, false);
                            }
                        }
                    }, null, null, null,
                    false, uninstallOtherApk(context, packageName, false), true);
        } else {
            if (!isAppCommonUninstall) {
                uninstallCommonApk(context, packageName);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void uninstallOtherSystemApk(Context context, View view,
                                               String packageName, Runnable rootAppSuccessRunnable) {
        uninstallOtherSystemApk(context, view, packageName, rootAppSuccessRunnable, false);
    }

    @SuppressWarnings("WeakerAccess")
    public static void uninstallCommonApk(Context context, String packageName) {
        uninstallCommonApk(context, packageName, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static void uninstallCommonApk(Context context, String packageName,
                                          boolean isReturnResult) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + packageName));
        if (isReturnResult) {
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        }
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE_UNINSTALL);
    }

    private static String deleteApkPathReturnString(String path) {
        if (!new File(path).exists()) {
            return null;
        }
        return returnString(path);
    }

    private static String returnString(String path) {
        String result = null;
        if (path.contains("/system/priv-app")) {
            Matcher m = Pattern.compile("/system/priv-app/.+/.+").matcher(path);
            if (m.matches()) {
                result = "rm -r "
                        + new File(path).getParentFile().getAbsolutePath();
            } else {
                m = Pattern.compile("/system/priv-app/.+").matcher(path);
                if (m.matches()) {
                    result = "rm " + path;
                }
            }
        } else if (path.contains("/system/app")) {
            Matcher m = Pattern.compile("/system/app/.+/.+").matcher(path);
            if (m.matches()) {
                result = "rm -r "
                        + new File(path).getParentFile().getAbsolutePath();
            } else {
                m = Pattern.compile("/system/app/.+").matcher(path);
                if (m.matches()) {
                    result = "rm " + path;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static boolean deleteApkPathReturnBoolean(String path) {
        return !new File(path).exists() || returnBoolean(path);
    }

    private static boolean returnBoolean(String path) {
        boolean result = false;
        if (path.contains("/system/priv-app")) {
            Matcher m = Pattern.compile("/system/priv-app/.+/.+").matcher(path);
            if (m.matches()) {
                result = RootUtil.sendShell(new String[]{
                        "rm -r "
                                + new File(path).getParentFile().getAbsolutePath()
                });
            } else {
                m = Pattern.compile("/system/priv-app/.+").matcher(path);
                if (m.matches()) {
                    result = RootUtil.sendShell(new String[]{
                            "rm " + path
                    });
                }
            }
        } else if (path.contains("/system/app")) {
            Matcher m = Pattern.compile("/system/app/.+/.+").matcher(path);
            if (m.matches()) {
                result = RootUtil.sendShell(new String[]{
                        "rm -r "
                                + new File(path).getParentFile().getAbsolutePath()
                });
            } else {
                m = Pattern.compile("/system/app/.+").matcher(path);
                if (m.matches()) {
                    result = RootUtil.sendShell(new String[]{
                            "rm " + path
                    });
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"StatementWithEmptyBody", "UnusedParameters"})
    public static void restartAndroid(Context context, boolean isInstall,
                                      String packageName) throws InterruptedException {
        if (context.getPackageName().equals(packageName)) {
//            if (!isInstall) {
//                uninstallSelfApk(context, context.getPackageName(), false);
//            }
        }

        // 在魅族5手机上发现用RootTools.restartAndroid()方法有1次无法重启手机,先执行以下代码尝试下.
//        boolean sendCommonShellOnly = RootUtil.sendRootShellOnly(new String[]{"reboot"});
//        LogUtil.i("sendCommonShellOnly -> " + sendCommonShellOnly);
//        LogUtil.showPhoneInfo();
        Runnable runnable = RootTools::restartAndroid;
        RootUtil.doRootWithDialog(context, null, null, runnable, null, runnable, null, false,
                new String[]{"reboot"}, false, false);
    }

    @SuppressWarnings("unused")
    public static void restartAndroid(Context context, boolean isInstall) throws InterruptedException {
        restartAndroid(context, isInstall, context.getPackageName());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static boolean kill(String... packageName) throws InterruptedException {
        return sendRootShellOnly(packageName);
    }

    /**
     * 获取指定包名的app在系统分区的路径
     *
     * @param context
     * @param packageName
     * @param isSame      当前app的包名是否和指定包名相同
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static String obtainAppPathAtSystem(Context context, String packageName, boolean isSame) {
        String path = null;
//        String apkPath;
        if (isSame) {
//            apkPath = context.getPackageResourcePath();
//            int temp = apkPath.lastIndexOf("/") + 1;
//            String fileName = apkPath.substring(temp);
//            fileName = fileName.substring(0, fileName.lastIndexOf("."));
//            if (Build.VERSION.SDK_INT >= 19) {
//                if (Build.VERSION.SDK_INT > 20) {
//                    path = "/system/priv-app/" + fileName + "/"
//                            + apkPath.substring(temp);
//                } else {
//                    path = "/system/priv-app/" + apkPath.substring(temp);
//                }
//            } else {
//                path = "/system/app/" + apkPath.substring(temp);
//            }

            path = obtainSystemAppPath();
        } else {
            if (context.getPackageName().equals(packageName)) {
                path = context.getPackageResourcePath();
            } else {
                try {
                    path = context.getPackageManager()
                            .getApplicationInfo(packageName, 0).sourceDir;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    /**
     * 获取指定包名的app在系统分区的路径
     *
     * @param context
     * @param packageName
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static String obtainAppPathAtSystem(Context context, String packageName) {
        return obtainAppPathAtSystem(context, packageName, true);
    }

    /**
     * 获取当前app在系统分区的路径
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static String obtainAppPathAtSystem(Context context) {
        return obtainAppPathAtSystem(context, context.getPackageName());
    }

    /**
     * 保存app在data分区下的路径,每次升级后在不同手机下可能会产生apk文件.
     */
    @SuppressWarnings({"unchecked", "unused"})
    public static void saveDataPath(String currentDataPath) {
        if (currentDataPath.contains("/data/app")) {
            LruMap lruMap = LruMap.getInstance();
            List<String> dataPaths = (List<String>) lruMap.get("dataPaths");
            if (dataPaths == null) {
                dataPaths = new ArrayList<>();
            }
            if (!dataPaths.contains(currentDataPath)) {
                dataPaths.add(currentDataPath);
                lruMap.put("dataPaths", dataPaths, true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> deleteDataPath(Context context) {
        ArrayList<String> result = null;
        ArrayList<String> removes;
        LruMap lruMap = LruMap.getInstance();
        List<String> dataPaths = (List<String>) lruMap.get("dataPaths");
        if (dataPaths != null) {
            result = new ArrayList<>();
            removes = new ArrayList<>();
            String currentDataPath = obtainAppPathAtSystem(context, context.getPackageName(), false);
            LogUtil.i("currentDataPath -> " + currentDataPath);
            LogUtil.i("dataPaths -> " + dataPaths);
            String dataPath;
            File f;
            File dataParentFile;
            for (int i = 0; i < dataPaths.size(); i++) {
                dataPath = dataPaths.get(i);
                LogUtil.i("dataPath -> " + dataPath);
                if (currentDataPath != null && dataPath.equals(currentDataPath)) {
                    continue;
                }
                f = new File(dataPath);
                if (f.exists()) {
                    LogUtil.i("f.getAbsolutePath() -> " + f.getAbsolutePath());
                    LogUtil.i("f.exists() -> " + f.exists());
                    dataParentFile = f.getParentFile();
                    String parentPath = dataParentFile.getAbsolutePath();
                    if (!parentPath.endsWith("/data/app")) {
                        result.add("rm -r " + parentPath);
                    } else {
                        result.add("rm " + dataPath);
                    }
                }
                removes.add(dataPath);
            }

            dataPaths.removeAll(removes);

            if (dataPaths.size() == 1) {
                currentDataPath = dataPaths.get(0);
                f = new File(currentDataPath);
                dataParentFile = f.getParentFile();
                String parentPath = dataParentFile.getAbsolutePath();
                if (!parentPath.endsWith("/data/app")) {
                    result.add("rm -r " + parentPath);
                } else {
                    result.add("rm " + currentDataPath);
                }
                LogUtil.i("f.exists() -> " + f.exists());
            }

            lruMap.remove("dataPaths");
        }

        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isOnMainThread() {
        return ((Looper.myLooper() != null) && (Looper.myLooper() == Looper.getMainLooper()));
    }

}
