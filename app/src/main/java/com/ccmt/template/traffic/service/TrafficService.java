package com.ccmt.template.traffic.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.widget.TextView;

import com.ccmt.library.service.AbstractTheadService;
import com.ccmt.library.util.ThreadManager;
import com.ccmt.template.CcmtApplication;
import com.ccmt.template.util.CommonUtil;

import java.util.List;

public class TrafficService extends AbstractTheadService {

    @SuppressLint("StaticFieldLeak")
    public static TextView sTvSpeed;
    @SuppressLint("StaticFieldLeak")
    public static TextView sTvSpeedUnit;
//    @SuppressLint("StaticFieldLeak")
//    public static ProcessColumAdapter sAdapter;

    @Override
    public void onDestroy() {
        super.onDestroy();
        sTvSpeed = null;
        sTvSpeedUnit = null;
//        sAdapter = null;
    }

    @SuppressLint("SetTextI18n")
    @SuppressWarnings({"unchecked", "StatementWithEmptyBody"})
    @Override
    protected void doTask(Intent intent) {
        super.doTask(intent);

        // 启动服务后,只有线程结束,mIsDoTaskable才被赋值为true,外部再启动服务时才能往下运行.
        // 否则外部无论启动多少次服务,都只有第1次启动时才会往下运行.
        mIsDoTaskable = null;

        ThreadManager.executeAsyncTask(() -> {
            while (true) {
                SystemClock.sleep(5000);
                if (mIsExit) {
                    CcmtApplication.application.mHandler.post(() -> mIsDoTaskable = true);
                    return;
                }
//                List<ProcessInfo> processInfos = null;
                long currentNetworkType = 0;
                List<Object> list = CommonUtil.obtainCurrentProcessInfo(TrafficService.this, null);
                if (list != null) {
                    CommonUtil.sList = list;
//                    processInfos = (List<ProcessInfo>) list.get(0);
                    currentNetworkType = (int) list.get(1);
                }
//                List<ProcessInfo> processInfosTemp = processInfos;
                long currentNetworkTypeTemp = currentNetworkType;
                CcmtApplication.application.mHandler.post(() -> {
//                    if (sTvSpeed == null || sAdapter == null) {
//                        return;
//                    }
                    if (sTvSpeed == null || sTvSpeedUnit == null) {
                        return;
                    }

                    if (currentNetworkTypeTemp == 0) {
                        sTvSpeed.setText("0");
//                        sTvSpeedUnit.setText("B/s");
                    } else {
                        String speed = Formatter.formatShortFileSize(TrafficService.this, currentNetworkTypeTemp);
                        LogUtil.i("speed -> " + speed);
                        String[] strs = speed.split(" ");
                        sTvSpeed.setText(strs[0]);
//                        sTvSpeedUnit.setText(strs[1] + "/s");
                    }

//                    if (totalNetworkSpeedTemp != 0) {
////                        sTvSpeed.setText(String.format("%s/s", Formatter.formatFileSize(TrafficService.this, totalNetworkSpeedTemp)));
//                        sTvSpeed.setText(totalNetworkSpeedTemp + "");
//                    } else {
//                        sTvSpeed.setText("0.00 B/s");
//                    }

//                    sAdapter.setList(processInfosTemp);
//                    sAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    @Override
    protected Runnable getRunnable() {
        return null;
    }

}
