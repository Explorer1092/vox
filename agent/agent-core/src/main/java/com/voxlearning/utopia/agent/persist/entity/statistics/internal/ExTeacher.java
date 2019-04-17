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

package com.voxlearning.utopia.agent.persist.entity.statistics.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author Maofeng Lu
 * @since 14-7-22 上午11:15
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ExTeacher implements Serializable {
    private static final long serialVersionUID = -4200277670646261059L;

    private Long teacher_id;    //老师ID
    private String name;        //老师名字
    private String subject;     //学科
    private String mobile;      //手机
    private boolean auth;       //是否认证
    private String regtime;     //注册时间
    private String authtime;    //认证时间
    private String email;       //邮箱
    private String notes;       //备注，用户添加，不对应mongo字段
}
