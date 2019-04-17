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

package com.voxlearning.utopia.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.ActivationType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Activate info mapper.
 *
 * @author Rui Bao
 * @version 0.1
 * @serial
 * @since 13-4-21
 */
@Data
public class ActivateInfoMapper implements Serializable {
    private static final long serialVersionUID = -2584999087431625046L;

    private Long userId;            // 用户Id
    private String userName;        // 用户名
    private String userAvatar;      // 用户头像
    private boolean choose;         // 是否目前是被选中状态
    private ActivationType type;    // 激活种类
    private Subject subject;
    private String historyId;
    private Boolean christmasFlag; //圣诞节唤醒活动 临时
    private Date activateSuccessDate;
    private Integer activeLevel; // 学生魔法城堡唤醒等级 一级梦境 二级梦境

    // 校园大使激活老师使用属性
    private String lastLoginDays = null; // 被唤醒后最近一次登录时间是*天前
    private String lastHomeworkDays = null;  // 被唤醒后最近一次布置作业时间是*天前
    private String lastCheckHomeworkDays = null; // 被唤醒后最后一次检查（或应检查）作业时间是*天前
    private String maxHomeworkFinishCount = null;  // 被唤醒后完成单次作业人数最多的人数
    private boolean finishLighted = false; // 完成作业人数状态是否点亮（>=8)人完成会点亮
    private long activateSuccessDays; // 已被成功唤醒的天数
    private long activateSpendDays; // 从唤醒到唤醒成功用的天数
    private String activateIntegral; // 校园大使获得的金币奖励
    private String suggestion = null; // 建议
    private String oppoIntegral; // 对方奖励

}
