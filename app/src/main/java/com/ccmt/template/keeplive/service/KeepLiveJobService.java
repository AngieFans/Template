package com.ccmt.template.keeplive.service;

import android.os.Handler;
import android.os.SystemClock;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

public class KeepLiveJobService extends JobService {

    @SuppressWarnings("unused")
    private Handler mHandler = new Handler();

    @Override
    public boolean onStartJob(JobParameters params) {
        ThreadManager.executeAsyncTask(() -> {
            LogUtil.i("KeepLiveJobService onStartJob()");
            SystemClock.sleep(1);
            jobFinished(params, false);
        });
//        mHandler.post(() -> {
//            LogUtil.i("KeepLiveJobService onStartJob()");
//            jobFinished(params, false);
//        });
//        LogUtil.i("KeepLiveJobService onStartJob()");
//        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtil.i("KeepLiveJobService onStopJob()");
        return false;
    }

}
