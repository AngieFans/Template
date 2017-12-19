package com.ccmt.template.accessibility.job;

import android.view.accessibility.AccessibilityEvent;

import com.ccmt.template.accessibility.notifycation.IStatusBarNotification;
import com.ccmt.template.accessibility.service.AbstractAccessibilityService;

public interface IAccessibilityJob {

    String getTargetPackageName();

    void onCreateJob(AbstractAccessibilityService service);

    void onReceiveJob(AbstractAccessibilityService accessibilityService, AccessibilityEvent event);

    void onStopJob();

    void onNotificationPosted(IStatusBarNotification sService);

    boolean isEnable();

}
