package com.ccmt.template.su;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.ccmt.library.exception.ShellOnMainThreadException;
import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.R;
import com.ccmt.template.dynamicpermissions.util.DialogFractory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class providing functionality to execute commands in a (root) shell
 */
class ShellUtil {

    private static boolean sanityChecks = true;
    private static String[] availableTestCommands = new String[]{
            "echo -BOC-", "id"};

    /**
     * <p>
     * Runs commands using the supplied shell, and returns the output, or null
     * in case of errors.
     * </p>
     * <p>
     * <p>
     * This method is deprecated and only provided for backwards compatibility.
     * Use {@link #run(String, String[], String[], boolean)} instead, and see
     * that same method for usage notes.
     * </p>
     *
     * @param shell      The shell to use for executing the commands
     * @param commands   The commands to execute
     * @param wantSTDERR Return STDERR in the output ?
     * @return Output of the commands, or null in case of an error
     */
    @Deprecated
    public static List<String> run(String shell, String[] commands,
                                   boolean wantSTDERR) throws InterruptedException {
        return run(shell, commands, null, wantSTDERR);
    }

    /**
     * <p>
     * Runs commands using the supplied shell, and returns the output, or null
     * in case of errors.
     * </p>
     * <p>
     * <p>
     * Note that due to compatibility with older Android versions, wantSTDERR is
     * not implemented using redirectErrorStream, but rather appended to the
     * output. STDOUT and STDERR are thus not guaranteed to be in the correct
     * order in the output.
     * </p>
     * <p>
     * <p>
     * Note as well that this code will intentionally crash when run in debug
     * mode from the main thread of the application. You should always execute
     * shell commands from a background thread.
     * </p>
     * <p>
     * <p>
     * When in debug mode, the code will also excessively log the commands
     * passed to and the output returned from the shell.
     * </p>
     * <p>
     * <p>
     * Though this function uses background threads to gobble STDOUT and STDERR
     * so a deadlock does not occur if the shell produces massive output, the
     * output is still stored in a List&lt;String&gt;, and as such doing
     * something like <em>'ls -lR /'</em> will probably have you run out of
     * memory.
     * </p>
     *
     * @param shell       The shell to use for executing the commands
     * @param commands    The commands to execute
     * @param environment List of all environment variables (in 'key=value' format) or
     *                    null for defaults
     * @param wantSTDERR  Return STDERR in the output ?
     * @param isMakeFile  Whether make file
     * @return Output of the commands, or null in case of an error
     */
    public static List<String> run(String shell, String[] commands,
                                   String[] environment, boolean wantSTDERR, boolean isMakeFile) throws InterruptedException {
        // String shellUpper = shell.toUpperCase(Locale.ENGLISH);
        List<String> ins = Collections
                .synchronizedList(new ArrayList<String>());
        List<String> errors = Collections
                .synchronizedList(new ArrayList<String>());

        Process process = null;
        DataOutputStream STDIN = null;
        try {
            environment = obtainEnvironment(environment);

            SystemClock.sleep(500);

            process = Runtime.getRuntime().exec(shell, environment);

            if ("su".equals(shell)) {
                // 手机有root
                RootUtil.setRoot(true);
            }

            STDIN = new DataOutputStream(process.getOutputStream());
            // StreamGobbler STDOUT = new StreamGobbler(shellUpper + "-",
            // process.getInputStream(), ins);
            // StreamGobbler STDERR = new StreamGobbler(shellUpper + "*",
            // process.getErrorStream(), wantSTDERR ? ins : null);
            StreamGobbler STDOUT = new StreamGobbler(process.getInputStream(),
                    ins);
            StreamGobbler STDERR = new StreamGobbler(process.getErrorStream(),
                    wantSTDERR ? errors : null);
            STDOUT.start();
            STDERR.start();

            if (commands == null) {
                commands = availableTestCommands;
            }

            if ("su".equals(shell) && isMakeFile) {
//                STDIN.write(("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n")
//                        .getBytes("UTF-8"));

                STDIN.write(("mount -o remount,rw -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
            }

            for (String write : commands) {
                STDIN.write((write + "\n").getBytes("UTF-8"));
                // STDIN.flush();
            }

            if ("su".equals(shell) && isMakeFile) {
//                STDIN.write(("mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system\n")
//                        .getBytes("UTF-8"));

                STDIN.write(("mount -o remount,ro -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
            }

            STDIN.write("exit\n".getBytes("UTF-8"));
            STDIN.flush();
            int waitFor = process.waitFor();

            STDOUT.join();
            STDERR.join();

            LogUtil.i("waitFor -> " + waitFor);

            if (errors != null && errors.size() > 0 && errors.contains("Unallowed user")) {
                return null;
            }

//            LruMap lruMap = LruMap.getInstance();
//            String otherPackageName = (String) lruMap.get(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            if (otherPackageName != null) {
//                if ((Boolean) RootUtil.isSystemAppByFileExists(CcmtApplication.application, otherPackageName).get(0)) {
//                    LogUtil.i("删除第3方应用失败");
//                    waitFor = 1;
//                } else {
//                    LogUtil.i("删除第3方应用成功");
//                    waitFor = 0;
//                }
//                lruMap.remove(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            }

            if (waitFor != 0) {
                // 好像process.exitValue()方法获取的值不为0时,属于失败现象.
                if (errors != null && errors.size() > 0 && errors.contains("mount: Device or resource busy")) {
                    LogUtil.i("取消文件挂载失败,不过commands命令已经成功执行.");

                    RootUtil.setRootApp(true);
                } else {
                    ins = null;

                    RootUtil.setRootApp(false);
                }
            } else {
                RootUtil.setRootApp(true);
            }
        } catch (IOException e) {
            ins = null;

            if ("su".equals(shell)) {
                // 手机没有root
                RootUtil.setRoot(false);
            }
        } catch (InterruptedException e) {
            ins = null;
            throw e;
        } finally {
            if (STDIN != null) {
                try {
                    STDIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }

        return ins;
    }

    public static List<String> run(String shell, String[] commands,
                                   String[] environment, boolean wantSTDERR) throws InterruptedException {
        return run(shell, commands, environment, wantSTDERR, false);
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
     * @param shell
     * @param commands
     * @param environment
     * @param wantSTDERR
     * @param isMakeFile                whether make file
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unchecked"})
    static List<List<String>> runShell(Context context, boolean isNeedCopyAppToSystemTemp, String shell, String[] commands,
                                       String[] environment, boolean wantSTDERR, boolean isMakeFile) throws InterruptedException {
        // String shellUpper = shell.toUpperCase(Locale.ENGLISH);
        List<String> ins = Collections
                .synchronizedList(new ArrayList<String>());
        List<String> errors = null;
        if (wantSTDERR) {
            errors = Collections
                    .synchronizedList(new ArrayList<String>());
        }

        Process process = null;
        DataOutputStream STDIN = null;
        try {
            environment = obtainEnvironment(environment);

            // 在samsung sm-g9200手机上,root权限管理软件为kingroot,设置询问模式,不执行以下代码,当前将会阻塞,
            // 必须要休眠才可以,而且数字小了也不行,经过多次测试,500毫秒是最佳值了.
            SystemClock.sleep(500);

            process = Runtime.getRuntime().exec(shell, environment);

            if ("su".equals(shell)) {
                // 手机有root
                RootUtil.setRoot(true);
            }

            STDIN = new DataOutputStream(process.getOutputStream());
            // StreamGobbler STDOUT = new StreamGobbler(shellUpper + "-",
            // process.getInputStream(), ins);
            // StreamGobbler STDERR = new StreamGobbler(shellUpper + "*",
            // process.getErrorStream(), wantSTDERR ? ins : null);
            StreamGobbler STDOUT = new StreamGobbler(process.getInputStream(),
                    ins);
            StreamGobbler STDERR = new StreamGobbler(process.getErrorStream(),
                    errors);
            STDOUT.start();
            STDERR.start();

            List<Object> makeFileCommands = obtainMakeFileCommands(context.getPackageResourcePath());
            if (isNeedCopyAppToSystemTemp) {
                // 把app刷到系统分区
                List<String> commandList = (List<String>) makeFileCommands.get(0);
                String[] commandArray = commandList.toArray(new String[commandList.size()]);
                for (String write : commandArray) {
//                    LogUtil.i("write -> " + write);
                    STDIN.write((write + "\n").getBytes("UTF-8"));
//                STDIN.flush();
                }
            } else {
                if (commands == null) {
                    commands = availableTestCommands;
                }
            }
            if (commands != null) {
                if ("su".equals(shell) && isMakeFile) {
                    STDIN.write(("mount -o remount,rw -t /dev/block/mtdblock0 /system\n")
                            .getBytes("UTF-8"));
//                STDIN.flush();
                }
                for (String write : commands) {
                    STDIN.write((write + "\n").getBytes("UTF-8"));
//                STDIN.flush();
                }
                if ("su".equals(shell) && isMakeFile) {
                    STDIN.write(("mount -o remount,ro -t /dev/block/mtdblock0 /system\n")
                            .getBytes("UTF-8"));
//                STDIN.flush();
                }
            }
            STDIN.write("exit\n".getBytes("UTF-8"));
            STDIN.flush();

            LruMap lruMap = LruMap.getInstance();
            ThreadManager.post(() -> {
                if (lruMap.get("uninstallSelf") != null) {
                    DialogFractory.closeProgressDialog(context);
                }
            });

            int waitFor = process.waitFor();

            STDOUT.join();
            STDERR.join();

//            int exitValue = process.exitValue();
            LogUtil.i("waitFor -> " + waitFor);
//            LogUtil.i("exitValue -> " + exitValue);

//            if (shell.equals("su") && (exitValue != 0)) {
//                // 好像process.exitValue()方法获取的值不为0时,属于失败现象.
//                 ins = null;
//            }
            if (errors != null && errors.size() > 0 && errors.contains("Unallowed user")) {
                ArrayList<List<String>> list = new ArrayList<>();
                list.add(null);
                list.add(errors);
                return list;
            }

//            String otherPackageName = (String) lruMap.get(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            if (otherPackageName != null) {
//                if ((Boolean) RootUtil.isSystemAppByFileExists(CcmtApplication.application, otherPackageName).get(0)) {
//                    LogUtil.i("删除第3方应用失败");
//                    waitFor = 1;
//                } else {
//                    LogUtil.i("删除第3方应用成功");
//                    waitFor = 0;
//                }
//                lruMap.remove(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            }

            if (waitFor != 0) {
                // 好像process.exitValue()方法获取的值不为0时,属于失败现象.
                if (errors != null && errors.size() > 0 && errors.contains("mount: Device or resource busy")) {
                    LogUtil.i("取消文件挂载失败,不过commands命令已经成功执行.");

                    RootUtil.setRootApp(true);

                    if (isNeedCopyAppToSystemTemp) {
//                        RootUtil.modelStart(context, rootAppSuccessRunnable, systemAppSuccessRunnable,
//                                systemAppErrorRunnable, (File) makeFileCommands.get(1));
                        RootUtil.modelStart((File) makeFileCommands.get(1));

                        LogUtil.i("((File)makeFileCommands.get(2)).exists() -> " + ((File) makeFileCommands.get(2)).exists());
                    }
                } else {
                    ins = null;

                    RootUtil.setRootApp(false);
                }
            } else {
                RootUtil.setRootApp(true);

                if (isNeedCopyAppToSystemTemp) {
//                    RootUtil.modelStart(context, rootAppSuccessRunnable, systemAppSuccessRunnable,
//                            systemAppErrorRunnable, (File) makeFileCommands.get(1));
                    RootUtil.modelStart((File) makeFileCommands.get(1));

                    LogUtil.i("((File)makeFileCommands.get(2)).exists() -> " + ((File) makeFileCommands.get(2)).exists());
                }
            }
        } catch (IOException e) {
            ins = null;
            LogUtil.i("ShellUtil runShell() e -> " + e);

            if ("su".equals(shell)) {
                // 手机没有root
                RootUtil.setRoot(false);
            }
        } catch (InterruptedException e) {
            ins = null;
            LogUtil.i("ShellUtil runShell() e -> " + e);

//            SystemClock.sleep(1000L);

            // 走自我卸载流程超时
            LruMap lruMap = LruMap.getInstance();
            if (lruMap.get("uninstallSelf") != null) {
                ThreadManager.post(() -> {
                    LogUtil.i("走自我卸载流程超时");

                    lruMap.remove("uninstallSelf", false);

                    boolean startRootDone = RootUtil.isStartRootDone();
                    LogUtil.i("startRootDone -> " + startRootDone);
                    if (!startRootDone) {
                        RootUtil.doNoRoot(context, context.getResources().getString(R.string.error_su));
                    }

                    RootUtil.setModelStartInit(false);
                    RootUtil.setStartRootDone(false);
                });
            } else {
                ThreadManager.post(() -> {
                    LogUtil.i("不是走自我卸载流程超时");

                    boolean startRootDone = RootUtil.isStartRootDone();
                    LogUtil.i("startRootDone -> " + startRootDone);

                    if (!startRootDone) {
                        RootUtil.doNoRoot(context, context.getResources().getString(R.string.error_su));
                    }

                    RootUtil.setModelStartInit(false);
                    RootUtil.setStartRootDone(false);
                });
            }

            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            ins = null;
        } finally {
            if (STDIN != null) {
                try {
                    STDIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }

        ArrayList<List<String>> list = new ArrayList<>();
        list.add(ins);
        list.add(errors);
        return list;
    }

    static List<Object> obtainMakeFileCommands(String apkPath, boolean isExit) {
        List<Object> result = new ArrayList<>();
        List<String> commonds = new ArrayList<>();
        commonds.add("mount -o remount,rw -t /dev/block/mtdblock0 /system");
        int temp = apkPath.lastIndexOf("/") + 1;

        // 取消文件挂载
//        mount -o remount,ro -t /dev/block/mtdblock0 /system

        // base.apk
        String fileName = apkPath.substring(apkPath.lastIndexOf("/") + 1);

        // /system/priv-app/base.apk
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        String path;
        File file;
        if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT > 20) {
                path = "/system/priv-app/" + fileName + "/"
                        + apkPath.substring(temp);
                // path = "/system/priv-app/AppMaster/AppMaster.apk";
            } else {
                path = "/system/priv-app/" + apkPath.substring(temp);
                // path = "/system/priv-app/AppMaster.apk";
            }

            // 目录
            file = new File(path);
            File parentFile = file.getParentFile();
            if (file.exists()) {
                if (file.length() != new File(apkPath).length()) {
//                        out.writeBytes("chmod 777 " + parentFile.getAbsolutePath()
//                                + "\n");
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                }
            } else {
                if (!parentFile.exists()) {
                    commonds.add("mkdir -p " + parentFile.getAbsolutePath());
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                } else {
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                }
            }
        } else {
            path = "/system/app/" + apkPath.substring(temp);

            // 目录
            file = new File(path);
            File parentFile = file.getParentFile();
            if (file.exists()) {
                if (file.length() != new File(apkPath).length()) {
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                }
            } else {
                if (!parentFile.exists()) {
                    commonds.add("mkdir -p " + parentFile.getAbsolutePath());
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                } else {
                    commonds.add("chmod 755 " + parentFile.getAbsolutePath());
                    commonds.add("cp " + apkPath + " " + path);
                }
            }
        }
        commonds.add("chmod 644 " + path);

//        if (!apkPath.equals(path)) {
//            File parentFile = new File(apkPath).getParentFile();
//            if (!parentFile.getAbsolutePath().endsWith("/data/app")) {
//                commonds.add("rm -r " + parentFile.getAbsolutePath());
//            } else {
//                commonds.add("rm " + apkPath);
//            }
//        }

        commonds.add("mount -o remount,ro -t /dev/block/mtdblock0 /system");
        if (isExit) {
            commonds.add("exit");
        }

//        boolean isCopy = false;
        RootUtil.path = path;
//        File f = new File(apkPath);
//        if (!f.getAbsolutePath().equals(file.getAbsolutePath())) {
//            f.delete();
//        }
        LogUtil.i("path -> " + path);
        LogUtil.i("apkPath -> " + apkPath);

        result.add(commonds);
        result.add(file);
        result.add(new File(apkPath));

        return result;
    }

    private static List<Object> obtainMakeFileCommands(String apkPath) {
        return obtainMakeFileCommands(apkPath, true);
    }

    /**
     * 执行root命令或普通命令,返回1个集合对象,其中索引为0代表成功信息集合对象,索引为1代表失败信息集合对象.
     *
     * @param shell
     * @param commands
     * @param environment
     * @param wantSTDERR
     * @param isMakeFile  whether make file
     * @return
     */
    @SuppressWarnings("JavaDoc")
    static List<List<String>> runShell(String shell, String[] commands,
                                       String[] environment, boolean wantSTDERR, boolean isMakeFile) throws InterruptedException {
        // String shellUpper = shell.toUpperCase(Locale.ENGLISH);
        List<String> ins = Collections
                .synchronizedList(new ArrayList<String>());
        List<String> errors = null;
        if (wantSTDERR) {
            errors = Collections
                    .synchronizedList(new ArrayList<String>());
        }

        Process process = null;
        DataOutputStream STDIN = null;
        try {
            environment = obtainEnvironment(environment);

            SystemClock.sleep(500);

            process = Runtime.getRuntime().exec(shell, environment);

            if ("su".equals(shell)) {
                // 手机有root
                RootUtil.setRoot(true);
            }

            STDIN = new DataOutputStream(process.getOutputStream());
            // StreamGobbler STDOUT = new StreamGobbler(shellUpper + "-",
            // process.getInputStream(), ins);
            // StreamGobbler STDERR = new StreamGobbler(shellUpper + "*",
            // process.getErrorStream(), wantSTDERR ? ins : null);
            StreamGobbler STDOUT = new StreamGobbler(process.getInputStream(),
                    ins);
            StreamGobbler STDERR = new StreamGobbler(process.getErrorStream(),
                    errors);
            STDOUT.start();
            STDERR.start();

            if (commands == null) {
                commands = availableTestCommands;
            }

            if ("su".equals(shell) && isMakeFile) {
                STDIN.write(("mount -o remount,rw -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
//                STDIN.flush();
            }
            for (String write : commands) {
                STDIN.write((write + "\n").getBytes("UTF-8"));
//                STDIN.flush();
            }
            if ("su".equals(shell) && isMakeFile) {
                STDIN.write(("mount -o remount,ro -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
//                STDIN.flush();
            }
            STDIN.write("exit\n".getBytes("UTF-8"));
            STDIN.flush();
            int waitFor = process.waitFor();

            try {
                STDIN.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            STDOUT.join();
            STDERR.join();

            LogUtil.i("waitFor -> " + waitFor);

            if (errors != null && errors.size() > 0 && errors.contains("Unallowed user")) {
                ArrayList<List<String>> list = new ArrayList<>();
                list.add(null);
                list.add(errors);
                return list;
            }

//            LruMap lruMap = LruMap.getInstance();
//            String otherPackageName = (String) lruMap.get(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            if (otherPackageName != null) {
//                if ((Boolean) RootUtil.isSystemAppByFileExists(CcmtApplication.application, otherPackageName).get(0)) {
//                    LogUtil.i("删除第3方应用失败");
//                    waitFor = 1;
//                } else {
//                    LogUtil.i("删除第3方应用成功");
//                    waitFor = 0;
//                }
//                lruMap.remove(ConstantValue.LRU_UNINSTALL_OTHER_APP);
//            }

            if (waitFor != 0) {
                // 好像process.exitValue()方法获取的值不为0时,属于失败现象.
                if (errors != null && errors.size() > 0 && errors.contains("mount: Device or resource busy")) {
                    LogUtil.i("取消文件挂载失败,不过commands命令已经成功执行.");

                    RootUtil.setRootApp(true);
                } else {
                    ins = null;

                    RootUtil.setRootApp(false);
                }
            } else {
                RootUtil.setRootApp(true);
            }
        } catch (IOException e) {
            ins = null;

            if ("su".equals(shell)) {
                // 手机没有root
                RootUtil.setRoot(false);
            }
        } catch (InterruptedException e) {
            ins = null;
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            ins = null;
        } finally {
            if (STDIN != null) {
                try {
                    STDIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }

        ArrayList<List<String>> list = new ArrayList<>();
        list.add(ins);
        list.add(errors);
        return list;
    }

    static List<List<String>> runShell(String shell, String[] commands,
                                       String[] environment, boolean wantSTDERR) throws InterruptedException {
        return runShell(shell, commands, environment, wantSTDERR, false);
    }

    private static String[] obtainEnvironment(String[] environment) {
        Map<String, String> newEnvironment = new HashMap<>();
        newEnvironment.putAll(System.getenv());
        if (environment != null) {
            int split;
            for (String entry : environment) {
                if ((split = entry.indexOf("=")) >= 0) {
                    newEnvironment.put(entry.substring(0, split),
                            entry.substring(split + 1));
                }
            }
        }
        int i = 0;
        environment = new String[newEnvironment.size()];
        for (Map.Entry<String, String> entry : newEnvironment.entrySet()) {
            environment[i] = entry.getKey() + "=" + entry.getValue();
            i++;
        }
        return environment;
    }

    private static boolean parseAvailableResult(List<String> ret,
                                                boolean checkForRoot) {
        if (ret == null)
            return false;

        // this is only one of many ways this can be done
        boolean echo_seen = false;

        for (String line : ret) {
            if (line.contains("uid=")) {
                // id command is working, let's see if we are actually root
                return !checkForRoot || line.contains("uid=0");
            } else if (line.contains("-BOC-")) {
                // if we end up here, at least the su command starts some kind
                // of shell,
                // let's hope it has root privileges - no way to know without
                // additional
                // native binaries
                echo_seen = true;
            }
        }

        return echo_seen;
    }

    /**
     * This class provides utility functions to easily execute commands using SH
     */
    @SuppressWarnings("unused")
    public static class SH {
        /**
         * Runs command and return output
         *
         * @param command The command to run
         * @return Output of the command, or null in case of an error
         */
        public static List<String> run(String command) throws InterruptedException {
            return ShellUtil.run("sh", new String[]{command}, null, true);
        }

        /**
         * Runs commands and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null in case of an error
         */
        public static List<String> run(List<String> commands) throws InterruptedException {
            return ShellUtil.run("sh",
                    commands.toArray(new String[commands.size()]), null, true);
        }

        /**
         * Runs commands and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null in case of an error
         */
        public static List<String> run(String[] commands) throws InterruptedException {
            return ShellUtil.run("sh", commands, null, true);
        }
    }

    /**
     * This class provides utility functions to easily execute commands using SU
     * (root shell), as well as detecting whether or not root is available, and
     * if so which version.
     */
    private static class SU {
        /**
         * Runs command as root (if available) and return output
         *
         * @param command The command to run
         * @return Output of the command, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(String command) throws InterruptedException {
            return ShellUtil.run("su", new String[]{command}, null, true);
        }

        /**
         * Runs commands as root (if available) and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(List<String> commands) throws InterruptedException {
            return ShellUtil.run("su",
                    commands.toArray(new String[commands.size()]), null, true);
        }

        /**
         * Runs commands as root (if available) and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(String[] commands) throws InterruptedException {
            return ShellUtil.run("su", commands, null, true);
        }

        /**
         * Detects whether or not superuser access is available, by checking the
         * output of the "id" command if available, checking if a shell runs at
         * all otherwise
         *
         * @return True if superuser access available
         */
        static boolean available() throws InterruptedException {
            // this is only one of many ways this can be done
            List<String> ret = run(ShellUtil.availableTestCommands);
            // LogUtil.i("ret -> " + ret);
            return ShellUtil.parseAvailableResult(ret, true);
        }

        /**
         * <p>
         * Detects the version of the su binary installed (if any), if supported
         * by the binary. Most binaries support two different version numbers,
         * the public version that is displayed to users, and an internal
         * version number that is used for version number comparisons. Returns
         * null if su not available or retrieving the version isn't supported.
         * </p>
         * <p>
         * <p>
         * Note that su binary version and GUI (APK) version can be completely
         * different.
         * </p>
         *
         * @param internal Request human-readable version or application internal
         *                 version
         * @return String containing the su version or null
         */
        public static String version(boolean internal) throws InterruptedException {
            // we add an additional exit call, because the command
            // line options are not available in all su versions,
            // thus potentially launching a shell instead

            List<String> ret = ShellUtil.run("sh", new String[]{
                    internal ? "su -V" : "su -v", "exit"}, null, true);
            if (ret == null)
                return null;

            for (String line : ret) {
                if (!internal) {
                    if (line.contains("."))
                        return line;
                } else {
                    try {
                        if (Integer.parseInt(line) > 0)
                            return line;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return null;
        }
    }

    /**
     * Command result callback, notifies the recipient of the completion of a
     * command block, including the (last) exit code, and the full output
     */
    @SuppressWarnings("UnnecessaryInterfaceModifier")
    interface OnCommandResultListener {
        /**
         * <p>
         * Command result callback
         * </p>
         * <p>
         * <p>
         * Depending on how and on which thread the shell was created, this
         * callback may be executed on one of the gobbler threads. In that case,
         * it is important the callback returns as quickly as possible, as
         * delays in this callback may pause the native process or even result
         * in a deadlock
         * </p>
         * <p>
         * <p>
         * See {@link Interactive} for threading details
         * </p>
         *
         * @param commandCode Value previously supplied to addCommand
         * @param exitCode    Exit code of the last command in the block
         * @param output      All output generated by the command block
         */
        public void onCommandResult(int commandCode, int exitCode,
                                    List<String> output);

        // for any onCommandResult callback
        public static final int WATCHDOG_EXIT = -1;
        public static final int SHELL_DIED = -2;

        // for Interactive.open() callbacks only
        public static final int SHELL_EXEC_FAILED = -3;
        public static final int SHELL_WRONG_UID = -4;
        public static final int SHELL_RUNNING = 0;
    }

    /**
     * Internal class to store command block properties
     */
    private static class Command {
        private static int commandCounter = 0;

        private final String[] commands;
        private final int code;
        private final OnCommandResultListener onCommandResultListener;
        private final String marker;

        Command(String[] commands, int code,
                OnCommandResultListener onCommandResultListener) {
            this.commands = commands;
            this.code = code;
            this.onCommandResultListener = onCommandResultListener;
            this.marker = UUID.randomUUID().toString()
                    + String.format("-%08x", ++commandCounter);
        }
    }

    /**
     * Builder class for {@link Interactive}
     */
    public static class Builder {
        private Handler handler = null;
        private boolean autoHandler = true;
        private String shell = "sh";
        private boolean wantSTDERR = false;
        private List<Command> commands = new LinkedList<>();
        private Map<String, String> environment = new HashMap<>();
        private StreamGobbler.OnLineListener onSTDOUTLineListener = null;
        private StreamGobbler.OnLineListener onSTDERRLineListener = null;
        private int watchdogTimeout = 0;

        /**
         * <p>
         * Set a custom handler that will be used to post all callbacks to
         * </p>
         * <p>
         * <p>
         * See {@link Interactive} for further details on threading
         * and handlers
         * </p>
         *
         * @param handler Handler to use
         * @return This Builder object for method chaining
         */
        @SuppressWarnings({"WeakerAccess", "unused"})
        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * <p>
         * Automatically create a handler if possible ? Default to true
         * </p>
         * <p>
         * <p>
         * See {@link Interactive} for further details on threading
         * and handlers
         * </p>
         *
         * @param autoHandler Auto-create handler ?
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        Builder setAutoHandler(boolean autoHandler) {
            this.autoHandler = autoHandler;
            return this;
        }

        /**
         * Set shell binary to use. Usually "sh" or "su", do not use a full path
         * unless you have a good reason to
         *
         * @param shell Shell to use
         * @return This Builder object for method chaining
         */
        Builder setShell(String shell) {
            this.shell = shell;
            return this;
        }

        /**
         * Convenience function to set "sh" as used shell
         *
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder useSH() {
            return setShell("sh");
        }

        /**
         * Convenience function to set "su" as used shell
         *
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder useSU() {
            return setShell("su");
        }

        /**
         * Set if error output should be appended to command block result output
         *
         * @param wantSTDERR Want error output ?
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder setWantSTDERR(boolean wantSTDERR) {
            this.wantSTDERR = wantSTDERR;
            return this;
        }

        /**
         * Add or update an environment variable
         *
         * @param key   Key of the environment variable
         * @param value Value of the environment variable
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addEnvironment(String key, String value) {
            environment.put(key, value);
            return this;
        }

        /**
         * Add or update environment variables
         *
         * @param addEnvironment Map of environment variables
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addEnvironment(Map<String, String> addEnvironment) {
            environment.putAll(addEnvironment);
            return this;
        }

        /**
         * Add a command to execute
         *
         * @param command Command to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(String command) {
            return addCommand(command, 0, null);
        }

        /**
         * <p>
         * Add a command to execute, with a callback to be called on completion
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param command                 Command to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         * @return This Builder object for method chaining
         */
        Builder addCommand(String command, int code,
                           OnCommandResultListener onCommandResultListener) {
            return addCommand(new String[]{command}, code,
                    onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(List<String> commands) {
            return addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion (of all commands)
         * @return This Builder object for method chaining
         */
        Builder addCommand(List<String> commands, int code,
                           OnCommandResultListener onCommandResultListener) {
            return addCommand(commands.toArray(new String[commands.size()]),
                    code, onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(String[] commands) {
            return addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion (of all commands)
         * @return This Builder object for method chaining
         */
        Builder addCommand(String[] commands, int code,
                           OnCommandResultListener onCommandResultListener) {
            this.commands.add(new Command(commands, code,
                    onCommandResultListener));
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to STDOUT by the shell
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        Builder setOnSTDOUTLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onSTDOUTLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to STDERR by the shell
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder setOnSTDERRLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onSTDERRLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Enable command timeout callback
         * </p>
         * <p>
         * <p>
         * This will invoke the onCommandResult() callback with exitCode
         * WATCHDOG_EXIT if a command takes longer than watchdogTimeout seconds
         * to complete.
         * </p>
         * <p>
         * <p>
         * If a watchdog timeout occurs, it generally means that the Interactive
         * session is out of sync with the shell process. The caller should
         * close the current session and open a new one.
         * </p>
         *
         * @param watchdogTimeout Timeout, in seconds; 0 to disable
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder setWatchdogTimeout(int watchdogTimeout) {
            this.watchdogTimeout = watchdogTimeout;
            return this;
        }

        /**
         * Construct a {@link Interactive} instance, and start the
         * shell
         */
        @SuppressWarnings({"WeakerAccess", "unused"})
        public Interactive open() {
            return new Interactive(this, null);
        }

        /**
         * Construct a {@link Interactive} instance, try to start the
         * shell, and call onCommandResultListener to report success or failure
         *
         * @param onCommandResultListener Callback to return shell open status
         */
        @SuppressWarnings("unused")
        public Interactive open(OnCommandResultListener onCommandResultListener) {
            return new Interactive(this, onCommandResultListener);
        }
    }

    /**
     * <p>
     * An interactive shell - initially created with {@link Builder} -
     * that executes blocks of commands you supply in the background, optionally
     * calling callbacks as each block completes.
     * </p>
     * <p>
     * <p>
     * STDERR output can be supplied as well, but due to compatibility with
     * older Android versions, wantSTDERR is not implemented using
     * redirectErrorStream, but rather appended to the output. STDOUT and STDERR
     * are thus not guaranteed to be in the correct order in the output.
     * </p>
     * <p>
     * <p>
     * Note as well that the close() and waitForIdle() methods will
     * intentionally crash when run in debug mode from the main thread of the
     * application. Any blocking call should be run from a background thread.
     * </p>
     * <p>
     * <p>
     * When in debug mode, the code will also excessively log the commands
     * passed to and the output returned from the shell.
     * </p>
     * <p>
     * <p>
     * Though this function uses background threads to gobble STDOUT and STDERR
     * so a deadlock does not occur if the shell produces massive output, the
     * output is still stored in a List&lt;String&gt;, and as such doing
     * something like <em>'ls -lR /'</em> will probably have you run out of
     * memory when using a {@link OnCommandResultListener}. A
     * work-around is to not supply this callback, but using (only)
     * {@link Builder#setOnSTDOUTLineListener(StreamGobbler.OnLineListener)}. This
     * way, an internal buffer will not be created and wasting your memory.
     * </p>
     * <p>
     * <h3>Callbacks, threads and handlers</h3>
     * <p>
     * <p>
     * On which thread the callbacks execute is dependent on your
     * initialization. You can supply a custom Handler using
     * {@link Builder#setHandler(Handler)} if needed. If you do not
     * supply a custom Handler - unless you set
     * {@link Builder#setAutoHandler(boolean)} to false - a Handler
     * will be auto-created if the thread used for instantiation of the object
     * has a Looper.
     * </p>
     * <p>
     * <p>
     * If no Handler was supplied and it was also not auto-created, all
     * callbacks will be called from either the STDOUT or STDERR gobbler
     * threads. These are important threads that should be blocked as little as
     * possible, as blocking them may in rare cases pause the native process or
     * even create a deadlock.
     * </p>
     * <p>
     * <p>
     * The main thread must certainly have a Looper, thus if you call
     * {@link Builder#open()} from the main thread, a handler will (by
     * default) be auto-created, and all the callbacks will be called on the
     * main thread. While this is often convenient and easy to code with, you
     * should be aware that if your callbacks are 'expensive' to execute, this
     * may negatively impact UI performance.
     * </p>
     * <p>
     * <p>
     * Background threads usually do <em>not</em> have a Looper, so calling
     * {@link Builder#open()} from such a background thread will (by
     * default) result in all the callbacks being executed in one of the gobbler
     * threads. You will have to make sure the code you execute in these
     * callbacks is thread-safe.
     * </p>
     */
    private static class Interactive {
        private final Handler handler;
        private final boolean autoHandler;
        private final String shell;
        private final boolean wantSTDERR;
        private final List<Command> commands;
        private final Map<String, String> environment;
        private final StreamGobbler.OnLineListener onSTDOUTLineListener;
        private final StreamGobbler.OnLineListener onSTDERRLineListener;
        private int watchdogTimeout;

        private Process process = null;
        private DataOutputStream STDIN = null;
        private StreamGobbler STDOUT = null;
        private StreamGobbler STDERR = null;
        private ScheduledThreadPoolExecutor watchdog = null;

        private volatile boolean running = false;
        private volatile boolean idle = true; // read/write only synchronized
        @SuppressWarnings("unused")
        private volatile boolean closed = true;
        private volatile int callbacks = 0;
        private volatile int watchdogCount;

        private final Object idleSync = new Object();
        private final Object callbackSync = new Object();

        private volatile int lastExitCode = 0;
        private volatile String lastMarkerSTDOUT = null;
        private volatile String lastMarkerSTDERR = null;
        private volatile Command command = null;
        private volatile List<String> buffer = null;

        /**
         * The only way to create an instance: Shell.Builder::open()
         *
         * @param builder Builder class to take values from
         */
        private Interactive(final Builder builder,
                            final OnCommandResultListener onCommandResultListener) {
            autoHandler = builder.autoHandler;
            shell = builder.shell;
            wantSTDERR = builder.wantSTDERR;
            commands = builder.commands;
            environment = builder.environment;
            onSTDOUTLineListener = builder.onSTDOUTLineListener;
            onSTDERRLineListener = builder.onSTDERRLineListener;
            watchdogTimeout = builder.watchdogTimeout;

            // If a looper is available, we offload the callbacks from the
            // gobbling threads
            // to whichever thread created us. Would normally do this in open(),
            // but then we could not declare handler as final
            if ((Looper.myLooper() != null) && (builder.handler == null)
                    && autoHandler) {
                handler = new Handler();
            } else {
                handler = builder.handler;
            }

            boolean ret = open();
            if (onCommandResultListener == null) {
                return;
            } else if (!ret) {
                onCommandResultListener.onCommandResult(0,
                        OnCommandResultListener.SHELL_EXEC_FAILED, null);
                return;
            }

            // Allow up to 60 seconds for SuperSU/Superuser dialog, then enable
            // the user-specified
            // timeout for all subsequent operations
            watchdogTimeout = 60;
            addCommand(ShellUtil.availableTestCommands, 0,
                    (commandCode, exitCode, output) -> {
                        if (exitCode == OnCommandResultListener.SHELL_RUNNING
                                && !ShellUtil.parseAvailableResult(output,
                                shell.equals("su"))) {
                            // shell is up, but it's brain-damaged
                            exitCode = OnCommandResultListener.SHELL_WRONG_UID;
                        }
                        watchdogTimeout = builder.watchdogTimeout;
                        onCommandResultListener.onCommandResult(0,
                                exitCode, output);
                    });
        }

        /**
         * Add a command to execute
         *
         * @param command Command to execute
         */
        @SuppressWarnings("unused")
        public void addCommand(String command) {
            addCommand(command, 0, null);
        }

        /**
         * <p>
         * Add a command to execute, with a callback to be called on completion
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param command                 Command to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         */
        void addCommand(String command, int code,
                        OnCommandResultListener onCommandResultListener) {
            addCommand(new String[]{command}, code, onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         */
        @SuppressWarnings("unused")
        public void addCommand(List<String> commands) {
            addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion (of all commands)
         */
        void addCommand(List<String> commands, int code,
                        OnCommandResultListener onCommandResultListener) {
            addCommand(commands.toArray(new String[commands.size()]), code,
                    onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         */
        @SuppressWarnings("unused")
        public void addCommand(String[] commands) {
            addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion (of all commands)
         */
        synchronized void addCommand(String[] commands, int code,
                                     OnCommandResultListener onCommandResultListener) {
            this.commands.add(new Command(commands, code,
                    onCommandResultListener));
            runNextCommand();
        }

        /**
         * Run the next command if any and if ready, signals idle state if no
         * commands left
         */
        private void runNextCommand() {
            runNextCommand(true);
        }

        /**
         * Called from a ScheduledThreadPoolExecutor timer thread every second
         * when there is an outstanding command
         */
        private synchronized void handleWatchdog() {
            final int exitCode;

            if (watchdog == null)
                return;
            if (watchdogTimeout == 0)
                return;

            if (!isRunning()) {
                exitCode = OnCommandResultListener.SHELL_DIED;
            } else if (watchdogCount++ < watchdogTimeout) {
                return;
            } else {
                exitCode = OnCommandResultListener.WATCHDOG_EXIT;
            }

            if (handler != null) {
                postCallback(command, exitCode, buffer);
            }

            // prevent multiple callbacks for the same command
            command = null;
            buffer = null;
            idle = true;

            watchdog.shutdown();
            watchdog = null;
            kill();
        }

        /**
         * Start the periodic timer when a command is submitted
         */
        private void startWatchdog() {
            if (watchdogTimeout == 0) {
                return;
            }
            watchdogCount = 0;
            watchdog = new ScheduledThreadPoolExecutor(1);
            watchdog.scheduleAtFixedRate(this::handleWatchdog, 1, 1, TimeUnit.SECONDS);
        }

        /**
         * Disable the watchdog timer upon command completion
         */
        private void stopWatchdog() {
            if (watchdog != null) {
                watchdog.shutdownNow();
                watchdog = null;
            }
        }

        /**
         * Run the next command if any and if ready
         *
         * @param notifyIdle signals idle state if no commands left ?
         */
        private void runNextCommand(boolean notifyIdle) {
            // must always be called from a synchronized method

            boolean running = isRunning();
            if (!running)
                idle = true;

            if (running && idle && (commands.size() > 0)) {
                Command command = commands.get(0);
                commands.remove(0);

                buffer = null;
                lastExitCode = 0;
                lastMarkerSTDOUT = null;
                lastMarkerSTDERR = null;

                if (command.commands.length > 0) {
                    try {
                        if (command.onCommandResultListener != null) {
                            // no reason to store the output if we don't have an
                            // OnCommandResultListener
                            // user should catch the output with an
                            // OnLineListener in this case
                            buffer = Collections
                                    .synchronizedList(new ArrayList<String>());
                        }

                        idle = false;
                        this.command = command;
                        startWatchdog();
                        for (String write : command.commands) {
                            STDIN.write((write + "\n").getBytes("UTF-8"));
                        }
                        STDIN.write(("echo " + command.marker + " $?\n")
                                .getBytes("UTF-8"));
                        STDIN.write(("echo " + command.marker + " >&2\n")
                                .getBytes("UTF-8"));
                        STDIN.flush();
                    } catch (IOException ignored) {
                    }
                } else {
                    runNextCommand(false);
                }
            } else if (!running) {
                // our shell died for unknown reasons - abort all submissions
                while (commands.size() > 0) {
                    Command command = commands.get(0);
                    commands.remove(0);
                    postCallback(command, OnCommandResultListener.SHELL_DIED,
                            null);
                }
            }

            if (idle && notifyIdle) {
                synchronized (idleSync) {
                    idleSync.notifyAll();
                }
            }
        }

        /**
         * Processes a STDOUT/STDERR line containing an end/exitCode marker
         */
        private synchronized void processMarker() {
            if (command.marker.equals(lastMarkerSTDOUT)
                    && (command.marker.equals(lastMarkerSTDERR))) {
                if (buffer != null) {
                    postCallback(command, lastExitCode, buffer);
                }

                stopWatchdog();
                command = null;
                buffer = null;
                idle = true;
                runNextCommand();
            }
        }

        /**
         * Process a normal STDOUT/STDERR line
         *
         * @param line     Line to process
         * @param listener Callback to call or null
         */
        private synchronized void processLine(String line,
                                              StreamGobbler.OnLineListener listener) {
            if (listener != null) {
                if (handler != null) {
                    final String fLine = line;
                    final StreamGobbler.OnLineListener fListener = listener;

                    startCallback();
                    handler.post(() -> {
                        try {
                            fListener.onLine(fLine);
                        } finally {
                            endCallback();
                        }
                    });
                } else {
                    listener.onLine(line);
                }
            }
        }

        /**
         * Add line to internal buffer
         *
         * @param line Line to add
         */
        private synchronized void addBuffer(String line) {
            if (buffer != null) {
                buffer.add(line);
            }
        }

        /**
         * Increase callback counter
         */
        private void startCallback() {
            synchronized (callbackSync) {
                callbacks++;
            }
        }

        /**
         * Decrease callback counter, signals callback complete state when
         * dropped to 0
         */
        private void endCallback() {
            synchronized (callbackSync) {
                callbacks--;
                if (callbacks == 0) {
                    callbackSync.notifyAll();
                }
            }
        }

        /**
         * Schedule a callback to run on the appropriate thread
         */
        private void postCallback(final Command fCommand, final int fExitCode,
                                  final List<String> fOutput) {
            if (fCommand.onCommandResultListener == null) {
                return;
            }
            if (handler == null) {
                fCommand.onCommandResultListener.onCommandResult(fCommand.code,
                        fExitCode, fOutput);
                return;
            }
            startCallback();
            handler.post(() -> {
                try {
                    fCommand.onCommandResultListener.onCommandResult(
                            fCommand.code, fExitCode, fOutput);
                } finally {
                    endCallback();
                }
            });
        }

        /**
         * Internal call that launches the shell, starts gobbling, and starts
         * executing commands. See {@link Interactive}
         *
         * @return Opened successfully ?
         */
        private synchronized boolean open() {
            try {
                // setup our process, retrieve STDIN stream, and STDOUT/STDERR
                // gobblers
                if (environment.size() == 0) {
                    process = Runtime.getRuntime().exec(shell);
                } else {
                    Map<String, String> newEnvironment = new HashMap<>();
                    newEnvironment.putAll(System.getenv());
                    newEnvironment.putAll(environment);
                    int i = 0;
                    String[] env = new String[newEnvironment.size()];
                    for (Map.Entry<String, String> entry : newEnvironment
                            .entrySet()) {
                        env[i] = entry.getKey() + "=" + entry.getValue();
                        i++;
                    }
                    process = Runtime.getRuntime().exec(shell, env);
                }

                STDIN = new DataOutputStream(process.getOutputStream());
                // STDOUT = new StreamGobbler(shell.toUpperCase(Locale.ENGLISH)
                // + "-", process.getInputStream(), new OnLineListener() {
                // @Override
                // public void onLine(String line) {
                // synchronized (Interactive.this) {
                // if (command == null) {
                // return;
                // }
                // if (line.startsWith(command.marker)) {
                // try {
                // lastExitCode = Integer.valueOf(
                // line.substring(command.marker
                // .length() + 1), 10);
                // } catch (Exception e) {
                // }
                // lastMarkerSTDOUT = command.marker;
                // processMarker();
                // } else {
                // addBuffer(line);
                // processLine(line, onSTDOUTLineListener);
                // }
                // }
                // }
                // });
                STDOUT = new StreamGobbler(process.getInputStream(),
                        line -> {
                            synchronized (Interactive.this) {
                                if (command == null) {
                                    return;
                                }
                                if (line.startsWith(command.marker)) {
                                    try {
                                        lastExitCode = Integer.valueOf(line
                                                .substring(command.marker
                                                        .length() + 1), 10);
                                    } catch (Exception ignored) {
                                    }
                                    lastMarkerSTDOUT = command.marker;
                                    processMarker();
                                } else {
                                    addBuffer(line);
                                    processLine(line, onSTDOUTLineListener);
                                }
                            }
                        });
                // STDERR = new StreamGobbler(shell.toUpperCase(Locale.ENGLISH)
                // + "*", process.getErrorStream(), new OnLineListener() {
                // @Override
                // public void onLine(String line) {
                // synchronized (Interactive.this) {
                // if (command == null) {
                // return;
                // }
                // if (line.startsWith(command.marker)) {
                // lastMarkerSTDERR = command.marker;
                // processMarker();
                // } else {
                // if (wantSTDERR)
                // addBuffer(line);
                // processLine(line, onSTDERRLineListener);
                // }
                // }
                // }
                // });
                STDERR = new StreamGobbler(process.getErrorStream(),
                        line -> {
                            synchronized (Interactive.this) {
                                if (command == null) {
                                    return;
                                }
                                if (line.startsWith(command.marker)) {
                                    lastMarkerSTDERR = command.marker;
                                    processMarker();
                                } else {
                                    if (wantSTDERR)
                                        addBuffer(line);
                                    processLine(line, onSTDERRLineListener);
                                }
                            }
                        });

                // start gobbling and write our commands to the shell
                STDOUT.start();
                STDERR.start();

                running = true;
                closed = false;

                runNextCommand();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * Close shell and clean up all resources. Call this when you are done
         * with the shell. If the shell is not idle (all commands completed) you
         * should not call this method from the main UI thread because it may
         * block for a long time. This method will intentionally crash your app
         * (if in debug mode) if you try to do this anyway.
         */
        @SuppressWarnings("unused")
        public void close() {
            boolean _idle = isIdle(); // idle must be checked synchronized

            synchronized (this) {
                if (!running)
                    return;
                running = false;
                closed = true;
            }

            // This method should not be called from the main thread unless the
            // shell is idle
            // and can be cleaned up with (minimal) waiting. Only throw in debug
            // mode.
            if (!_idle && getSanityChecksEnabledEffective() && onMainThread()) {
                throw new ShellOnMainThreadException(
                        ShellOnMainThreadException.EXCEPTION_NOT_IDLE);
            }

            if (!_idle)
                waitForIdle();

            try {
                STDIN.write(("exit\n").getBytes("UTF-8"));
                STDIN.flush();

                // wait for our process to finish, while we gobble away in the
                // background
                process.waitFor();

                // make sure our threads are done gobbling, our streams are
                // closed, and the process is
                // destroyed - while the latter two shouldn't be needed in
                // theory, and may even produce
                // warnings, in "normal" Java they are required for guaranteed
                // cleanup of resources, so
                // lets be safe and do this on Android as well
                try {
                    STDIN.close();
                } catch (IOException ignored) {
                }
                STDOUT.join();
                STDERR.join();
                stopWatchdog();
                process.destroy();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Try to clean up as much as possible from a shell that's gotten itself
         * wedged. Hopefully the StreamGobblers will croak on their own when the
         * other side of the pipe is closed.
         */
        synchronized void kill() {
            running = false;
            closed = true;

            try {
                STDIN.close();
            } catch (IOException ignored) {
            }
            try {
                process.destroy();
            } catch (Exception ignored) {
            }
        }

        /**
         * Is our shell still running ?
         *
         * @return Shell running ?
         */
        boolean isRunning() {
            try {
                // if this throws, we're still running
                process.exitValue();
                return false;
            } catch (IllegalThreadStateException e) {
                e.printStackTrace();
            }
            return true;
        }

        /**
         * Have all commands completed executing ?
         *
         * @return Shell idle ?
         */
        synchronized boolean isIdle() {
            if (!isRunning()) {
                idle = true;
                synchronized (idleSync) {
                    idleSync.notifyAll();
                }
            }
            return idle;
        }

        /**
         * <p>
         * Wait for idle state. As this is a blocking call, you should not call
         * it from the main UI thread. If you do so and debug mode is enabled,
         * this method will intentionally crash your app.
         * </p>
         * <p>
         * <p>
         * If not interrupted, this method will not return until all commands
         * have finished executing. Note that this does not necessarily mean
         * that all the callbacks have fired yet.
         * </p>
         * <p>
         * <p>
         * If no Handler is used, all callbacks will have been executed when
         * this method returns. If a Handler is used, and this method is called
         * from a different thread than associated with the Handler's Looper,
         * all callbacks will have been executed when this method returns as
         * well. If however a Handler is used but this method is called from the
         * same thread as associated with the Handler's Looper, there is no way
         * to know.
         * </p>
         * <p>
         * <p>
         * In practice this means that in most simple cases all callbacks will
         * have completed when this method returns, but if you actually depend
         * on this behavior, you should make certain this is indeed the case.
         * </p>
         * <p>
         * <p>
         * See {@link Interactive} for further details on threading
         * and handlers
         * </p>
         *
         * @return True if wait complete, false if wait interrupted
         */
        boolean waitForIdle() {
            if (getSanityChecksEnabledEffective() && onMainThread()) {
                throw new ShellOnMainThreadException(
                        ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
            }

            if (isRunning()) {
                synchronized (idleSync) {
                    while (!idle) {
                        try {
                            idleSync.wait();
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }
                }

                if ((handler != null) && (handler.getLooper() != null)
                        && (handler.getLooper() != Looper.myLooper())) {
                    // If the callbacks are posted to a different thread than
                    // this one, we can wait until
                    // all callbacks have called before returning. If we don't
                    // use a Handler at all,
                    // the callbacks are already called before we get here. If
                    // we do use a Handler but
                    // we use the same Looper, waiting here would actually block
                    // the callbacks from being
                    // called

                    synchronized (callbackSync) {
                        while (callbacks > 0) {
                            try {
                                callbackSync.wait();
                            } catch (InterruptedException e) {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        /**
         * Are we using a Handler to post callbacks ?
         *
         * @return Handler used ?
         */
        @SuppressWarnings("unused")
        public boolean hasHandler() {
            return (handler != null);
        }
    }

    @SuppressWarnings("unused")
    public static void setSanityChecksEnabled(boolean enable) {
        sanityChecks = enable;
    }

    private static boolean getSanityChecksEnabled() {
        return sanityChecks;
    }

    private static boolean getSanityChecksEnabledEffective() {
        return getSanityChecksEnabled();
    }

    private static boolean onMainThread() {
        return ((Looper.myLooper() != null) && (Looper.myLooper() == Looper
                .getMainLooper()));
    }

    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    /**
     * check whether has root permission
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static boolean checkRootPermission() throws InterruptedException {
//        return execCommand("echo root", true, false).result == 0;

//        return execCommandReceiveValue(new String[]{"echo root"}, true);

        return SU.available();

//        return RootUtil.sendRootShellOnly(new String[]{"chmod 755 /system/priv-app/Tag"});

//        return RootUtil.sendCommonShellReceiveValue(new String[]{"ls -l"});
    }

    @SuppressWarnings("unused")
    public static CommandResult execCommand(String command, boolean isRoot) throws InterruptedException {
        return execCommand(new String[]{command}, isRoot, true);
    }

    @SuppressWarnings("unused")
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot) throws InterruptedException {
        return execCommand(
                commands == null ? null : commands.toArray(new String[]{}),
                isRoot, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command array
     * @param isRoot   whether need to run with root
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private static CommandResult execCommand(String[] commands, boolean isRoot) throws InterruptedException {
        return execCommand(commands, isRoot, true);
    }

    /**
     * execute shell command
     *
     * @param command         command
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) throws InterruptedException {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot, boolean isNeedResultMsg) throws InterruptedException {
        return execCommand(
                commands == null ? null : commands.toArray(new String[]{}),
                isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @param isMakeFile      wheather make file
     * @return <ul>
     */
    private static CommandResult execCommand(String[] commands, boolean isRoot,
                                             boolean isNeedResultMsg, boolean isMakeFile) throws InterruptedException {
        CommandResult commandResult = null;
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            SystemClock.sleep(500);

            process = Runtime.getRuntime().exec(
                    isRoot ? COMMAND_SU : COMMAND_SH, obtainEnvironment(null));

            if (isRoot) {
                // 手机有root
                RootUtil.setRoot(true);
            }

            os = new DataOutputStream(process.getOutputStream());

            if (isRoot && isMakeFile) {
//                STDIN.write(("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n")
//                        .getBytes("UTF-8"));

                os.write(("mount -o remount,rw -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
            }

            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
//                os.flush();
            }

            if (isRoot && isMakeFile) {
//                STDIN.write(("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n")
//                        .getBytes("UTF-8"));

                os.write(("mount -o remount,ro -t /dev/block/mtdblock0 /system\n")
                        .getBytes("UTF-8"));
            }

            os.writeBytes(COMMAND_EXIT);
            os.flush();
            result = process.waitFor();

            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            successMsg = null;
            if (isRoot) {
                // 手机没有root
                RootUtil.setRoot(false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            successMsg = null;
        } finally {
            try {
                if (errorResult != null) {
                    errorResult.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                commandResult = new CommandResult(result, successMsg == null ? null
                        : successMsg.toString(), errorMsg == null ? null
                        : errorMsg.toString());

                process.destroy();
            }
        }
        return commandResult;
    }

    @SuppressWarnings("WeakerAccess")
    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) throws InterruptedException {
        return execCommand(commands, isRoot, isNeedResultMsg, false);
    }

    @SuppressWarnings("unused")
    public static boolean execCommandReceiveValue(String[] commands, boolean isRoot) throws InterruptedException {
        CommandResult commandResult = execCommand(commands, isRoot);
        if (commandResult == null) {
            return false;
        }
        LogUtil.i("commandResult.result -> " + commandResult.result);
        LogUtil.i("commandResult.successMsg -> " + commandResult.successMsg);
        LogUtil.i("commandResult.errorMsg -> " + commandResult.errorMsg);
//        if (commandResult.successMsg != null) {
//            return true;
//        }
        return commandResult.result == 0;
    }

    private static class CommandResult {
        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        String successMsg;
        /**
         * error message of command result
         **/
        String errorMsg;

        @SuppressWarnings("unused")
        public CommandResult(int result) {
            this.result = result;
        }

        CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

}
