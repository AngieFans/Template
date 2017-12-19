package com.ccmt.template.appopspermissions;

import com.ccmt.library.lru.LruMap;

/**
 * @author myx
 *         by 2017-08-18
 */
class OnRequestPermissionsListenerFactory {

    private OnRequestPermissionsListenerFactory() {

    }

    static AbstractOnRequestPermissionsListener createOnRequestPermissionsListener() {
        AbstractOnRequestPermissionsListener onRequestPermissionsListener = null;
        if (RomUtil.isMiuiRom()) {
            LruMap lruMap = LruMap.getInstance();
            String name = MiUiOnRequestPermissionsListener.class.getName();
            onRequestPermissionsListener = (AbstractOnRequestPermissionsListener) lruMap.get(name);
            if (onRequestPermissionsListener == null) {
                onRequestPermissionsListener = new MiUiOnRequestPermissionsListener();
                lruMap.put(name, onRequestPermissionsListener);
            }
        } else if (RomUtil.isMeizuRom()) {
            LruMap lruMap = LruMap.getInstance();
            String name = MeizuOnRequestPermissionsListener.class.getName();
            onRequestPermissionsListener = (AbstractOnRequestPermissionsListener) lruMap.get(name);
            if (onRequestPermissionsListener == null) {
                onRequestPermissionsListener = new MeizuOnRequestPermissionsListener();
                lruMap.put(name, onRequestPermissionsListener);
            }
        }
        return onRequestPermissionsListener;
    }

}
