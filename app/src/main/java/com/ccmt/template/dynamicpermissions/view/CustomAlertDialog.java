package com.ccmt.template.dynamicpermissions.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccmt.library.util.LogUtil;
import com.ccmt.library.util.ViewUtil;
import com.ccmt.template.R;
import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.template.dynamicpermissions.util.DialogFractory;
import com.ccmt.template.util.DimenUtils;

import static android.view.View.GONE;

/**
 * 公用的基本Dialog，便于统一样式
 * Created by yangxl on 2016/8/19.
 */
public class CustomAlertDialog extends AlertDialog implements DialogInterface {

    @SuppressWarnings("unused")
    protected static final float DIALOG_RATIO_TO_SCREEN_WIDTH = 0.8f;

    //对话框title布局参数
    private static final int DIALOG_TITLE_MARGIN_LEFT = 24;
    private static final int DIALOG_TITLE_TOTAL_HEIGTH = 50;
    private static final int DIALOG_TITLE_SIZE = 16;
    private static final int DIALOG_TITLE_COLOR = 0xFF333333;

    private static final int DIALOG_TITLE_SEPRATOR_COLOR = 0x33000000;
    private static final int DIALOG_TITLE_SEPRATOR_SIZE = 1;

    private static final float DEFAULT_VALUE_DIM_AMOUNT = 0.6F;

    @SuppressLint("StaticFieldLeak")
    private static Activity sActivity;

//    private View mContentView;

    /**
     * title布局
     */
    private LinearLayout mTitleLayout;
    private TextView mTitleText;

    /**
     * 两个按钮布局
     */
    private LinearLayout mButtonLayout;
    private LinearLayout mPositiveButtonLayout;
    private LinearLayout mNegativeButtonLayout;
    private View mSplitLineView;
    private Button mPositiveButton;
    private Button mNegativeButton;

    /**
     * 对话框内容布局
     */
    private FrameLayout mMessageLayout;

    /**
     * 通用的对话框内容容器
     */
    private LinearLayout mCommonMessageContainer;

    private TextView mMessageText;
    private CheckBox mNoPrompCheckBox;

    /**
     * 对话框关闭时的回调
     */
    private DialogCloseListener mDialogCloseListener;

    private View mHoriSplitLineView;
    private String mExtraData;
    private CustomParams mCustomParams;

    @SuppressWarnings("ConstantConditions")
    private CustomAlertDialog(CustomParams p) {
        super(p.isGlobalDialog ? p.mContext.getApplicationContext() : p.mContext, p.theme);

        // 设置对话框背景透明度以及属性,如果设置了FLAG_NOT_TOUCH_MODAL标记,setCanceledOnTouchOutside(true)不再生效.
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (p.mDimAmount != -1.0F) {
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = p.mDimAmount;
        }
        if (p.mIsNotTouchModal) {
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        }
        if (p.mIsNotFoucusable) {
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        if (p.mIsNotTouchable) {
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        if (p.theme == R.style.custom_alter_dialog_style) {
            if (p.mDimAmount == -1.0F) {
                lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                lp.dimAmount = DEFAULT_VALUE_DIM_AMOUNT;
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
            window.setAttributes(lp);
            initView(p);
        } else if (p.theme == R.style.custom_alter_dialog_style_fullscreen_style) {
            mCustomParams = p;

//            FrameLayout root = (FrameLayout) findViewById(R.id.previewSV).getParent();
//            FrameLayout root = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.activity_main, null);

//            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            Window window = getWindow();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

//            Log.i("MyLog", "lp.dimAmount -> " + lp.dimAmount);
//            Log.i("MyLog", "lp.alpha -> " + lp.alpha);
//            Log.i("MyLog", "lp.screenBrightness -> " + lp.screenBrightness);
//            Log.i("MyLog", "lp.buttonBrightness -> " + lp.buttonBrightness);

            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

//            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
//            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

//        lp.dimAmount = 0f;
//            lp.format = PixelFormat.TRANSPARENT;
//            lp.alpha = 0.6F;
//        lp.screenBrightness = 0.6F;
//        lp.buttonBrightness = 1F;
//            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
            window.setAttributes(lp);
//            windowManager.addView(root, lp);
        }
    }

    private void initView(CustomParams p) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(p.mContext).inflate(
                R.layout.custom_alert_dialog_layout, null);
        FrameLayout.LayoutParams layoutParams = //
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        LogUtil.i("layoutParams.topMargin -> " + layoutParams.topMargin);
//        LogUtil.i("layoutParams.bottomMargin -> " + layoutParams.bottomMargin);
//        LogUtil.i("layoutParams.gravity -> " + layoutParams.gravity);
        view.setLayoutParams(layoutParams);
        mTitleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mButtonLayout = (LinearLayout) view.findViewById(R.id.btn_layout);

        mPositiveButton = (Button) view.findViewById(R.id.btn_positive);
        mNegativeButton = (Button) view.findViewById(R.id.btn_negative);
        mSplitLineView = view.findViewById(R.id.seprator);
        mHoriSplitLineView = view.findViewById(R.id.hori_seprator);
        mPositiveButtonLayout = (LinearLayout) view.findViewById(R.id.btn_positive_layout);
        mNegativeButtonLayout = (LinearLayout) view.findViewById(R.id.btn_negative_layout);

        mMessageLayout = (FrameLayout) view.findViewById(R.id.content_layout);
        mMessageText = (TextView) view.findViewById(R.id.content);
        mNoPrompCheckBox = (CheckBox) view.findViewById(R.id.no_promp_checkbox);

        mCommonMessageContainer = (LinearLayout) view.findViewById(R.id.common_message_container);
        setView(view);

        if (p.mTopMargin != 0 || p.mBottomMargin != 0) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("RtlHardcoded")
                @Override
                public void onGlobalLayout() {
                    ViewUtil.removeOnGlobalLayoutListener(view, this);
                    if (p.mTopMargin != 0) {
                        layoutParams.topMargin += ViewUtil.obtainViewPx((Activity) p.mContext, p.mTopMargin, false);
                    }
                    if (p.mBottomMargin != 0) {
                        layoutParams.bottomMargin += ViewUtil.obtainViewPx((Activity) p.mContext, p.mBottomMargin, false);
                    }
                    view.setLayoutParams(layoutParams);
                    LogUtil.i("layoutParams.topMargin -> " + layoutParams.topMargin);
                    LogUtil.i("layoutParams.bottomMargin -> " + layoutParams.bottomMargin);
                    LogUtil.i("layoutParams.gravity -> " + layoutParams.gravity);
                }
            });
        }
    }

    @SuppressWarnings("unused")
    public void setContentView(int layoutResID, FrameLayout.LayoutParams layoutParams) {
        View view = LayoutInflater.from(getContext()).inflate(layoutResID, null);
        view.setLayoutParams(layoutParams);
        setContentView(view);
    }

    @SuppressWarnings("unused")
    public void setContentView(View view, FrameLayout.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        setContentView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(LayoutInflater.from(getContext()).inflate(layoutResID, null));
    }

    @Override
    public void setContentView(@NonNull View view) {
//        mContentView = view;

        FrameLayout rootView = new FrameLayout(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(layoutParams);

        boolean isChange = false;
        layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            isChange = true;
        }
        if (mCustomParams != null) {
            if (mCustomParams.mGravity != Gravity.NO_GRAVITY) {
                // 如果WindowManager的宽高为MATCH_PARENT,则第1个子控件的宽高也为MATCH_PARENT.
                // 如果WindowManager的宽高为WRAP_CONTENT,则第1个子控件的宽高也为WRAP_CONTENT.
                // 所以如果WindowManager的第1个子控件为Dialog的ContentView,
                // 则Dialog的ContentView的宽高依赖于WindowManager的宽高.
                // 要解决这个问题,只要再设置1层ViewGroup作为Dialog的ContentView的父控件就可以了,
                // 此时Dialog的ContentView可以随意控制.
                layoutParams.gravity = mCustomParams.mGravity;
                isChange = true;
            }
            if (mCustomParams.mTopMargin != 0) {
                layoutParams.topMargin = mCustomParams.mTopMargin;
                isChange = true;
            }
            if (mCustomParams.mBottomMargin != 0) {
                layoutParams.bottomMargin = mCustomParams.mBottomMargin;
                isChange = true;
            }
        }
        if (isChange) {
            view.setLayoutParams(layoutParams);
        }

//        rootView.addView(mContentView, new FrameLayout.LayoutParams(
//                (int) (DimenUtils.getScreenWidth() * DIALOG_RATIO_TO_SCREEN_WIDTH),
//                FrameLayout.LayoutParams.MATCH_PARENT));
//        rootView.addView(mContentView);
        rootView.addView(view);

//        mContentView.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        super.setContentView(rootView);

        if (mCustomParams != null) {
            ((ViewGroup) rootView.getParent()).setOnTouchListener((v, event) -> {
//                    ViewGroup.LayoutParams layoutParams2 = ((ViewGroup) rootView.getParent()).getLayoutParams();
//                    LogUtil.i("layoutParams2.width -> " + layoutParams2.width);
//                    LogUtil.i("layoutParams2.height -> " + layoutParams2.height);
                int x = (int) event.getX();
                int y = (int) event.getY();
//                float rawX = event.getRawX();
//                float rawY = event.getRawY();
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
//                LogUtil.i("x -> " + x);
//                LogUtil.i("y -> " + y);
//                LogUtil.i("rawX -> " + rawX);
//                LogUtil.i("rawY -> " + rawY);
//                LogUtil.i("rect -> " + rect);
                if (!rect.contains(x, y)) {
                    if (mCustomParams.mCanceledOnTouchOutside) {
                        dismiss();
                    }
                } else {
                    if (mCustomParams.mIsClickContentViewDismiss) {
                        dismiss();
                    }
                }
                return false;
            });
        }
    }

//    @SuppressWarnings("unused")
//    public View getContentView() {
//        return mContentView;
//    }

    public void show() {
        DialogFractory.sType = DynamicPermissionManager.TYPE_NOT_ACTIVITY;
        super.show();
    }

    @Override
    public void dismiss() {
        dismiss(null, 0);
    }

    private void dismiss(OnClickListener buttonListener, int button_identifier) {
        mCustomParams = null;
        if (buttonListener != null) {
            buttonListener.onClick(this, button_identifier);
        }
        if (DialogFractory.sType != null && DialogFractory.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
            DialogFractory.sType = null;
        }
        super.dismiss();
    }

    @Override
    public void setOnDismissListener(final OnDismissListener listener) {
        super.setOnDismissListener(arg0 -> {
            if (listener != null) {
                listener.onDismiss(arg0);
            }
        });
    }

    public void setCustomTitle(View titleView) {
        mTitleLayout.removeAllViews();
        mTitleLayout.addView(titleView);
        mTitleLayout.setVisibility(View.VISIBLE);
    }

    public void setMessage(CharSequence message) {
        mMessageLayout.setVisibility(View.VISIBLE);
        mMessageText.setText(message);
    }

    private void setNoPrompCheckBox(boolean isShown,
                                    CompoundButton.OnCheckedChangeListener changedLister) {
        if (isShown) {
            mNoPrompCheckBox.setVisibility(View.VISIBLE);
            mNoPrompCheckBox.setChecked(false);
            mNoPrompCheckBox.setOnCheckedChangeListener(changedLister);
        }
    }

    public void setIcon(int iconId) {

    }

    public void setIcon(Drawable icon) {

    }

    private void setGoneButton(final int button_identifier) {
        switch (button_identifier) {
            case DialogInterface.BUTTON_POSITIVE:
                mPositiveButtonLayout.setVisibility(GONE);
                mSplitLineView.setVisibility(GONE);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mSplitLineView.setVisibility(GONE);
                mNegativeButtonLayout.setVisibility(GONE);
                break;
        }
        if (mPositiveButtonLayout.getVisibility() == GONE &&
                mNegativeButtonLayout.getVisibility() == GONE) {
            //不要GONE，会影响布局
            mHoriSplitLineView.setVisibility(View.INVISIBLE);
        }
    }

    public void setButton(final int button_identifier, CharSequence buttonText,
                          final OnClickListener buttonListener) {
        switch (button_identifier) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonLayout.setVisibility(View.VISIBLE);
                mPositiveButton.setText(buttonText);
                mPositiveButton.setOnClickListener(v -> CustomAlertDialog.this.dismiss(buttonListener, button_identifier));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonLayout.setVisibility(View.VISIBLE);
                mNegativeButton.setText(buttonText);
                mNegativeButton.setOnClickListener(v -> {
                    if (buttonListener != null) {
                        buttonListener.onClick(CustomAlertDialog.this,
                                button_identifier);
                    }
                    CustomAlertDialog.this.dismiss();
                });
                break;
        }
    }

    private void setMessageView(View view) {
        mMessageLayout.removeAllViews();
        mMessageLayout.setVisibility(View.VISIBLE);
        mMessageLayout.addView(view);
    }

    private void fixCommonMessagePadding() {
        //@note title不可见时，则要设置messagelayout与dialog上边距
//        mCommonMessageContainer.setPadding(0,
//                getContext().getResources().getDimensionPixelSize(R.dimen.dialog_message_padding_top)
//                , 0, 0);

        // 适配,15dip在720*1280下的像素为30px,再用ViewUtil.obtainViewPx()方法算出在当前手机下应该为多少像素.
        if (sActivity != null) {
            mCommonMessageContainer.setPadding(0,
                    ViewUtil.obtainViewPx(sActivity, 30, false)
                    , 0, 0);
            sActivity = null;
        } else {
            mCommonMessageContainer.setPadding(0,
                    ViewUtil.obtainViewPx(getContext(), 30, false)
                    , 0, 0);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            mTitleLayout.setVisibility(GONE);
            //@note title不可见时，则要设置messagelayout与dialog上边距
            fixCommonMessagePadding();
            return;
        }
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleText.setText(title);
        sActivity = null;
    }

    /**
     * 创建一个默认的TitleView
     */
    @SuppressLint("RtlHardcoded")
    private static View createDefaultTitleView(Context context, String title) {
        FrameLayout titleLayout = new FrameLayout(context);
        titleLayout.setMinimumHeight(DimenUtils.dp2px(context, DIALOG_TITLE_TOTAL_HEIGTH));

        TextView textView = new TextView(context);
        FrameLayout.LayoutParams textParms =
                new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParms.leftMargin = DimenUtils.dp2px(context, DIALOG_TITLE_MARGIN_LEFT);
        textParms.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        textView.setLayoutParams(textParms);
        textView.setTextColor(DIALOG_TITLE_COLOR);
        textView.setTextSize(DIALOG_TITLE_SIZE);
        textView.setText(title);

        View line = new View(context);
        FrameLayout.LayoutParams lineParms =
                new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DIALOG_TITLE_SEPRATOR_SIZE);
        lineParms.gravity = Gravity.BOTTOM;
        line.setLayoutParams(lineParms);
        line.setBackgroundColor(DIALOG_TITLE_SEPRATOR_COLOR);
//        textView.setTypeface(TypefaceUtils.load(context.getAssets(),
//                AppConfig.OPENSANSLIGHT_FONT_PATH));

        titleLayout.addView(textView);
        titleLayout.addView(line);
        titleLayout.setVisibility(View.VISIBLE);
        return titleLayout;
    }

    private static class CustomParams {
        Context mContext;
        int theme;
        CharSequence title;
        View customTitleView;
        CharSequence message;
        int iconId;
        Drawable icon;
        CharSequence positiveButtonText;
        OnClickListener positiveButtonListener;

        CharSequence negativeButtonText;
        OnClickListener negativeButtonListener;
        boolean mCancelable = true;
        boolean mCanceledOnTouchOutside = true;
        OnCancelListener mOnCancelListener;
        OnDismissListener mOnDismissListener;
        OnKeyListener mOnKeyListener;
        View mMessageView;

        boolean isNoPrompCheckBoxShown;
        CompoundButton.OnCheckedChangeListener noPrompChangedLister;
        float mDimAmount = -1.0F;
        int mTopMargin;
        int mBottomMargin;
        int mGravity;
        boolean mIsNotTouchModal;
        boolean mIsNotFoucusable;
        boolean mIsNotTouchable;
        boolean mIsClickContentViewDismiss;

        /**
         * 是否为全局对话框，全局对话框主要给后台服务使用
         */
        boolean isGlobalDialog;

        CustomParams(Context context, int theme) {
            this.mContext = context;
            this.theme = theme;
        }

        @SuppressWarnings("ConstantConditions")
        void apply(CustomAlertDialog dialog) {
            if (theme == R.style.custom_alter_dialog_style) {
                if (customTitleView != null) {
                    dialog.setCustomTitle(customTitleView);
                } else {
                    if (mContext instanceof Activity) {
                        sActivity = (Activity) mContext;
                    }
                    dialog.setTitle(title);
                    if (iconId >= 0) {
                        dialog.setIcon(iconId);
                    } else if (icon != null) {
                        dialog.setIcon(icon);
                    }
                }
                if (mMessageView != null) {
                    dialog.setMessageView(mMessageView);
                } else if (message != null) {
                    dialog.setMessage(message);
                }

                if (positiveButtonText != null) {
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                            positiveButtonText, positiveButtonListener);
                } else {
                    dialog.setGoneButton(DialogInterface.BUTTON_POSITIVE);
                }

                if (negativeButtonText != null) {
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                            negativeButtonText, negativeButtonListener);
                } else {
                    dialog.setGoneButton(DialogInterface.BUTTON_NEGATIVE);
                }

                dialog.setNoPrompCheckBox(isNoPrompCheckBoxShown, noPrompChangedLister);
            }

            if (isGlobalDialog) {
                int type;
                if (Build.VERSION.SDK_INT >= 19) {
                    type = WindowManager.LayoutParams.TYPE_TOAST;
                } else {
                    type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                dialog.getWindow().setType(type);
            }
        }
    }

    public static class Builder {
        private CustomParams P;

        public Builder(Context context) {
            this(context, 0);
        }

        public Builder(Context context, int theme) {
            P = new CustomParams(context, theme);
        }

        public Context getContext() {
            return P.mContext;
        }

        @SuppressWarnings("unused")
        public Builder setTitle(int titleId) {
            try {
                P.title = P.mContext.getString(titleId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder setTitle(CharSequence title) {
            P.title = title;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setCustomTitle(View customTitleView) {
            P.customTitleView = customTitleView;
            return this;
        }

        //定义一个默认的custom title样式
        @SuppressWarnings("unused")
        public Builder setCustomTitle(Context context, String title) {
            P.customTitleView = createDefaultTitleView(context, title);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setMessage(int messageId) {
            try {
                P.message = P.mContext.getString(messageId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder setMessage(CharSequence message) {
            P.message = message;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setIcon(int iconId) {
            P.iconId = iconId;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setIcon(Drawable icon) {
            P.icon = icon;
            return this;
        }

        public Builder setPositiveButton(int textId,
                                         final OnClickListener listener) {
            P.positiveButtonText = P.mContext.getString(textId);
            P.positiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text,
                                         final OnClickListener listener) {
            P.positiveButtonText = text;
            P.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId,
                                         final OnClickListener listener) {
            P.negativeButtonText = P.mContext.getText(textId);
            P.negativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text,
                                         final OnClickListener listener) {
            P.negativeButtonText = text;
            P.negativeButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            P.mCanceledOnTouchOutside = canceledOnTouchOutside;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        /**
         * 调用该方法后,默认的message对应的TextView和CheckBox都会被删除,只有指定的view占着message区域.
         *
         * @param view
         * @return
         */
        @SuppressWarnings({"JavaDoc", "unused"})
        public Builder setMessageView(View view) {
            P.mMessageView = view;
            return this;
        }

        /**
         * 调用该方法后,默认的message对应的TextView和CheckBox都会被删除,只有指定的view占着message区域.
         *
         * @param layoutResID
         * @return
         */
        @SuppressWarnings({"JavaDoc", "unused"})
        public Builder setMessageView(int layoutResID) {
            P.mMessageView = LayoutInflater.from(getContext()).inflate(layoutResID, null);
            return this;
        }

        public Builder setGlobalDialog(boolean isGlobalDialog) {
            P.isGlobalDialog = isGlobalDialog;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setNoPrompCheckBoxShown(
                boolean isShown, CompoundButton.OnCheckedChangeListener changedLister) {
            P.isNoPrompCheckBoxShown = isShown;
            P.noPrompChangedLister = changedLister;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setDimAmount(float dimAmount) {
            P.mDimAmount = dimAmount;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setTopMargin(int topMargin) {
            P.mTopMargin = topMargin;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setBottomMargin(int bottomMargin) {
            P.mBottomMargin = bottomMargin;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setGravity(int gravity) {
            P.mGravity = gravity;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setNotTouchModal(boolean notTouchModal) {
            P.mIsNotTouchModal = notTouchModal;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setNotFoucusable(boolean notFoucusable) {
            P.mIsNotFoucusable = notFoucusable;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setNotTouchable(boolean notTouchable) {
            P.mIsNotTouchable = notTouchable;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder setClickContentViewDismiss(boolean clickContentViewDismiss) {
            P.mIsClickContentViewDismiss = clickContentViewDismiss;
            return this;
        }

        public CustomAlertDialog create() {
            if (P.theme == 0) {
                P.theme = R.style.custom_alter_dialog_style;
            }
            final CustomAlertDialog dialog = new CustomAlertDialog(P);
            P.apply(dialog);
            dialog.setCancelable(P.mCancelable);
            dialog.setCanceledOnTouchOutside(P.mCanceledOnTouchOutside);
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (!P.mCancelable) {
                dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return true;
                    }
                    if (P.mOnKeyListener != null) {
                        P.mOnKeyListener.onKey(dialog1, keyCode, event);
                    }
                    dialog1.dismiss();
                    return false;
                });
            } else {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        @SuppressWarnings("unused")
        public CustomAlertDialog show() {
            CustomAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDialogCloseListener != null) {
            mDialogCloseListener.onBackPress();
        }
    }

    @SuppressWarnings("unused")
    public void setDialogCloseListener(DialogCloseListener listener) {
        mDialogCloseListener = listener;
    }

    @SuppressWarnings("WeakerAccess")
    public interface DialogCloseListener {
        void onBackPress();
    }

    @SuppressWarnings("unused")
    public void setExtraData(String extraData) {
        this.mExtraData = extraData;
    }

    @SuppressWarnings("unused")
    public String getExtraData() {
        return mExtraData;
    }

}
