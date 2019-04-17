/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.admin.constant;

/**
 * @author Longlong Yu
 * @since 上午1:03,13-11-23.
 */
public enum AdminPageRole {

    ALLOW_ALL(0, "allowAll"),           // 允许所有人访问
    GET_ACCESSOR(10, "getAccessor"),    // 允许get方法访问
    POST_ACCESSOR(20, "postAccessor");  // 允许post方法访问

    private int roleType;
    // roleName 与权限管理系统的名字要一致
    private String roleName;

    private AdminPageRole(int roleType, String roleName) {
        this.roleType = roleType;
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
