package com.ccmt.template.fragment;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ccmt.library.lru.LruMap;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author myx
 *         by 2017-08-23
 */
@SuppressWarnings("WeakerAccess")
public class FragmentManager {

    private Map<Class<? extends Fragment>, WeakReference<View>> mViews;

    private FragmentManager() {
        mViews = new HashMap<>();
    }

    public static FragmentManager getInstance() {
        LruMap lruMap = LruMap.getInstance();
        String name = FragmentManager.class.getName();
        FragmentManager fragmentManager = (FragmentManager) lruMap.get(name);
        if (fragmentManager == null) {
            fragmentManager = new FragmentManager();
            lruMap.put(name, fragmentManager);
        }
        return fragmentManager;
    }

    @SuppressWarnings("WeakerAccess")
    public View getView(Class<? extends Fragment> cla, LayoutInflater inflater,
                        ViewGroup container, int layoutResourceId, boolean isUseCache) {
        View view;
        if (isUseCache) {
            WeakReference<View> viewWeakReference = mViews.get(cla);
            if (viewWeakReference == null) {
                view = inflater.inflate(layoutResourceId, container, false);
                view.setTag(true);
                mViews.put(cla, new WeakReference<>(view));
            } else {
                view = viewWeakReference.get();
                if (view == null) {
                    view = inflater.inflate(layoutResourceId, container, false);
                    view.setTag(true);
                    mViews.put(cla, new WeakReference<>(view));
                } else {
                    view.setTag(null);
                }
            }
        } else {
            view = inflater.inflate(layoutResourceId, container, false);
            view.setTag(true);
        }
        return view;
    }

    @SuppressWarnings("unused")
    public View getView(Class<? extends Fragment> cla, LayoutInflater inflater,
                        ViewGroup container, int layoutResourceId) {
        return getView(cla, inflater, container, layoutResourceId, true);
    }

    @SuppressWarnings("unused")
    public void addView(Class<? extends Fragment> cla, View view) {
        view.setTag(true);
        mViews.put(cla, new WeakReference<>(view));
    }

    @SuppressWarnings("unused")
    public void removeView(Class<? extends Fragment> cla) {
        mViews.remove(cla);
    }

}
