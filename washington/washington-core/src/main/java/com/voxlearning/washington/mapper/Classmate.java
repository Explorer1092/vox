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

package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(of = "userId")
public class Classmate implements Serializable {
    private static final long serialVersionUID = 5349183436712054198L;

    private Long userId;         // 同学ID
    private String userName;     // 同学姓名
    private String avatar;       // 同学头像
    private String headWear;     // 同学头饰

    private String fairyImage;      // 精灵图片
    private String petName;         // 宠物名称
    private String petImage;        // 宠物图片

    //------------------- 这些字段暂时没用了 -----------------
    private int usableCredit;    // 可用自学积分
    private boolean hasFairy;       // 是否有精灵形象
    private int fairyLevel;         // 精灵等级
    private String fairyType;       // 精灵类型
    private String fairySkinType;   // 精灵皮肤

    public Classmate(User user) {
        Objects.requireNonNull(user, "classmate user must not be null");
        this.userId = user.getId();
        this.userName = user.fetchRealnameIfBlankId();
        this.avatar = user.fetchImageUrl();
    }

    public Classmate withHeadWear(StudentInfo studentInfo, Map<String, Privilege> allHeadWear) {
        if (studentInfo != null && allHeadWear != null) {
            Privilege privilege = allHeadWear.get(studentInfo.getHeadWearId());
            this.headWear = (privilege == null ? null : privilege.getImg());
        }
        return this;
    }
}
