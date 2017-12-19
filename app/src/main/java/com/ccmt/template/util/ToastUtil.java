package com.ccmt.template.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast统一管理类
 *
 * @author way
 */
public class ToastUtil {

    private static Toast toast;

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static void showShort(Context context, CharSequence message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static void showShort(Context context, int message) {
        show(context, message, Toast.LENGTH_SHORT);
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static void showLong(Context context, CharSequence message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    @SuppressWarnings({"unused", "JavaDoc"})
    public static void showLong(Context context, int message) {
        show(context, message, Toast.LENGTH_LONG);
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    @SuppressWarnings("JavaDoc")
    public static void show(Context context, CharSequence message, int duration) {
        if (null == toast) {
            toast = Toast.makeText(context, message, duration);
        } else {
            toast.setText(message);
            toast.setDuration(duration);
        }
        toast.show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    @SuppressWarnings("JavaDoc")
    public static void show(Context context, int message, int duration) {
        if (null == toast) {
            toast = Toast.makeText(context, message, duration);
        } else {
            toast.setText(message);
            toast.setDuration(duration);
        }
        toast.show();
    }

    /**
     * Hide the toast, if any.
     */
    @SuppressWarnings("unused")
    public static void hideToast() {
        if (null != toast) {
            toast.cancel();
        }
    }

}
