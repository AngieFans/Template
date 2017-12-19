package com.ccmt.template.keeplive;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.ccmt.template.keeplive.service.KeepLiveJobService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobScheduler;

public class KeepLiveManager {

    private static KeepLiveManager sInstance;
    private Random mRandom;
    private List<Integer> mJobIds;

    private KeepLiveManager() {
        mRandom = new Random();
        mJobIds = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public static KeepLiveManager getInstance() {
//        LruMap lruMap = LruMap.getInstance();
//        LogUtil.i("lruMap.hashCode() -> " + lruMap.hashCode());
//        String name = KeepLiveManager.class.getName();
//        KeepLiveManager jobSchedulerManager = (KeepLiveManager) lruMap.get(name);
//        if (jobSchedulerManager == null) {
//            jobSchedulerManager = new KeepLiveManager();
//            lruMap.put(name, jobSchedulerManager);
//        }
        if (sInstance == null) {
            sInstance = new KeepLiveManager();
        }
        return sInstance;
    }

    /**
     * 返回任务的id
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public int keepLive(Context context) {
        JobScheduler jobScheduler = JobScheduler.getInstance(context);
//        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
//        if (allPendingJobs != null && allPendingJobs.size() > 0) {
//            return 0;
//        }

        int jobId;
//        if (mJobIds.size() == 0) {
        jobId = mRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
        while (mJobIds.contains(jobId)) {
            jobId = mRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
        }
//        cancelCurrentJobId(context);
        mJobIds.add(jobId);
//        }

//        PersistableBundle extras = new PersistableBundle();
//        extras.putString("key", "value");
        int periodic;
        if (Build.VERSION.SDK_INT >= 21) {
            periodic = 3;
        } else {
            periodic = 3;
        }
        jobId = jobScheduler.schedule(new JobInfo.Builder(jobId, new ComponentName(context, KeepLiveJobService.class))
                .setPeriodic(periodic)
//                .setOverrideDeadline(3)
//                .setMinimumLatency(3)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
//                .setExtras(extras)
                .setPersisted(true)
                .build());
        return jobId;
    }

    @SuppressWarnings("unused")
    public void cancelAll(Context context) {
        JobScheduler jobScheduler = JobScheduler.getInstance(context);
//        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
//        if (allPendingJobs == null || allPendingJobs.size() == 0) {
//            return;
//        }
        jobScheduler.cancelAll();
        mJobIds.clear();
    }

    @SuppressWarnings("unused")
    public void cancelCurrentJobId(Context context) {
        if (mJobIds.size() > 0) {
            int index = mJobIds.size() - 1;
            JobScheduler.getInstance(context).cancel(mJobIds.get(index));
            mJobIds.remove(index);
        }
    }

}
