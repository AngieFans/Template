package com.ccmt.template.util;

import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.library.lru.LruMap;

public class ObjectUtil {

    public static DynamicPermissionManager obtainDynamicPermissionManager() {
        Class<DynamicPermissionManager> cla = DynamicPermissionManager.class;
        return LruMap.getInstance().createOrGetElement(cla.getName(), cla,
                DynamicPermissionManager::new);
    }

}