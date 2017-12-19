package com.ccmt.template.accessibility.notifycation;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.ccmt.template.accessibility.Config;
import com.ccmt.template.accessibility.service.AbstractAccessibilityService;
import com.ccmt.library.lru.LruMap;

import java.util.List;

@TargetApi(18)
public abstract class AbstractNotificationService extends NotificationListenerService {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(getClass().getName() + " onCreate()");
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        onListenerConnected();
//        }
    }

    private Config getConfig() {
        return Config.getConfig(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        LogUtil.i(getClass().getName() + " onNotificationPosted()");
        boolean enableNotificationService = getConfig().isEnableNotificationService();
        LogUtil.i("enableNotificationService -> " + enableNotificationService);
//        if (!getConfig().isAgreement()) {
//            return;
//        }
        if (!enableNotificationService) {
            LogUtil.i("do not have notification mService");
            return;
        }
        LruMap lruMap = LruMap.getInstance();
        List<AbstractAccessibilityService> accessibilityServices = (List<AbstractAccessibilityService>) lruMap
                .get(AbstractAccessibilityService.class.getName());
        LogUtil.i("accessibilityServices -> " + accessibilityServices);
        if (accessibilityServices != null) {
            AbstractAccessibilityService accessibilityService;
            int size = accessibilityServices.size();
            for (int i = 0; i < size; i++) {
                accessibilityService = accessibilityServices.get(i);
                accessibilityService.handeNotificationPosted(new IStatusBarNotification() {
                    @Override
                    public String getPackageName() {
                        return sbn.getPackageName();
                    }

                    @Override
                    public Notification getNotification() {
                        return sbn.getNotification();
                    }
                });
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        LogUtil.i(getClass().getName() + " onNotificationRemoved()");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        super.onNotificationRemoved(sbn);
//        }
    }

    @Override
    public void onListenerConnected() {
        LogUtil.i(getClass().getName() + " onListenerConnected()");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        super.onListenerConnected();
//        }

        //发送广播,已经连接上了.
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        LogUtil.i(getClass().getName() + " onDestroy()");
        super.onDestroy();

        //发送广播,已经连接上了.
        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

//    /**
//     * 是否启动通知栏监听
//     */
//    public static boolean isRunning() {
//        if (Build.VERSION.SDK_INT < 18) {
//            return false;
//        }
//
//        // 部份手机没有NotificationService服务
//        return mService != null;
//    }

    public static boolean isRunning(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
