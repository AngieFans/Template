package com.ccmt.template;

import android.app.Application;
import android.os.Handler;
import android.os.Process;

import com.ccmt.library.util.LogUtil;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.dynamicpermissions.util.CommonUtil;
import com.ccmt.template.util.SystemUtil;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class CcmtApplication extends Application {

    public static CcmtApplication application;
    //    private List<Activity> allActivities;
    private RefWatcher mRefWatcher;
    @SuppressWarnings("unused")
    public Handler mHandler = new Handler();
//    public Map<String, Long> mNetworkSpeeds = new HashMap<>();

    /**
     * 每个Activity和Fragment的onDestroy()方法被调用时调用该方法
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static RefWatcher getRefWatcher() {
        return ((CcmtApplication) application.getApplicationContext()).mRefWatcher;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

//        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();

        if (LeakCanary.isInAnalyzerProcess(application)) {
            return;
        }
        mRefWatcher = LeakCanary.install(application);

        // 解决InputMethodManager类的内存泄露问题
        SystemUtil.fixMemoryLeak(this);

        //Tencent Bugly 初始化 第三个参数，测试阶段建议设置成true，发布时设置为false
//        CrashReport.initCrashReport(getApplicationContext(), "a35ee3e22", false);

        // 由于有运行在其他进程的组件,所以如果当前运行的进程如果不是主进程,就不用再做初始化操作.
        int pid = CommonUtil.obtainCurrentMainProcessId(this);
        if (Process.myPid() != pid) {
            return;
        }

        //EventBus索引生成，只在主进程初始化一次
//        if (TextUtils.equals(CommonUtil.getCurrentProcessName(application), getPackageName())) {
//            EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
//        }

        LogUtil.i("WifiAccelerateApplication onCreate()");

//        Global.serializableFileDir = getFileStreamPath("Ser").getAbsolutePath();
//        Global.serializableFileDirNotDelete = getFileStreamPath("SerNotDelete")
//                .getAbsolutePath();
        // Global.serializableFileDir = getFilesDir().getAbsolutePath()
        // + File.separator + "Ser";
        // Global.serializableFileDirNotDelete = getFilesDir().getAbsolutePath()
        // + File.separator + "SerNotDelete";

        ThreadManager.startup();

        //        if (Build.VERSION.SDK_INT < 21) {
//        KeepLiveManager jobSchedulerManager = KeepLiveManager.getInstance();
//        jobSchedulerManager.keepLive(application);
//        }

        if (!BuildConfig.IS_DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                // LogUtil.i("ex.getMessage() -> " + ex.getMessage());
                // LogUtil.i("ex.getLocalizedMessage() -> "
                // + ex.getLocalizedMessage());
                // LogUtil.i("ex.getStackTrace() -> "
                // + Arrays.toString(ex.getStackTrace()));

//                if (allActivities != null) {
//                    Iterator<Activity> ite = allActivities.iterator();
//                    while (ite.hasNext()) {
//                        ite.next().onBackPressed();
//                        ite.remove();
//                    }
//                    allActivities = null;
//                }

//                if (Global.allRunningServices != null) {
//                    Iterator<Class<? extends Service>> ite = Global.allRunningServices.iterator();
//                    while (ite.hasNext()) {
//                        stopService(new Intent(application, ite.next()));
//                        ite.remove();
//                    }
//                    Global.allRunningServices = null;
//                }

                Process.killProcess(Process.myPid());
            });
        }

//        FileUtil.deleteDir(StorageUtil.getIncrementalUpdatingDir(application));

//        allActivities = new ArrayList<>();
    }

//    /**
//     * 每次进程启动时,都会调用该方法,而且在onCreate()方法之前被调用,主要用来dex突破65535的限制.
//     *
//     * @param base
//     */
//    @SuppressWarnings("JavaDoc")
//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//
////        if (Process.myPid() == CommonUtil.obtainCurrentMainProcessId(base)) {
//////            MultiDex.install(this);
////        }
//    }

    @SuppressWarnings("unused")
    public static CcmtApplication getAccelerateApplication() {
        return application;
    }

//    public boolean addActivity(Activity activity) {
//        if (allActivities == null) {
//            allActivities = new ArrayList<>();
//        }
//        return allActivities.add(activity);
//    }

//    public boolean removeActivity(Activity activity) {
//        return allActivities.remove(activity);
//    }

}
