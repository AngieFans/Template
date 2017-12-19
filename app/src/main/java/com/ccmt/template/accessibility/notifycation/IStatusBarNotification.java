package com.ccmt.template.accessibility.notifycation;

import android.app.Notification;

public interface IStatusBarNotification {

    String getPackageName();

    Notification getNotification();

}
