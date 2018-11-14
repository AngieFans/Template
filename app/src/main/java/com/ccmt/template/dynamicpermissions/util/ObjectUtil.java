package com.ccmt.template.dynamicpermissions.util;

import com.ccmt.library.lru.LruMap;
import com.ccmt.template.dynamicpermissions.DynamicPermissionManager;

public class ObjectUtil {

    public static DynamicPermissionManager obtainDynamicPermissionManager() {
        Class<DynamicPermissionManager> cla = DynamicPermissionManager.class;
        return LruMap.getInstance().createOrGetElement(cla.getName(), cla,
                DynamicPermissionManager::new);
    }

}