/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.support;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
@Getter
@Setter
public class LoginUser {
    private Long userId;
    private UserType type;
    private String realName;

    public LoginUser() {
    }

    public LoginUser(User user) {
        this.userId = user.getId();
        this.realName = user.getProfile().getRealname();
        this.type = UserType.of(user.getUserType());
    }
}
