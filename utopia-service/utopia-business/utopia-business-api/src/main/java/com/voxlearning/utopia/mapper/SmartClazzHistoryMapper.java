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

import com.voxlearning.alps.calendar.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 智慧教室发放记录历史
 *
 * @author Maofeng Lu
 * @since 14-7-1 下午1:53
 */
@Getter
@Setter
public class SmartClazzHistoryMapper extends SmartClazzRank implements Serializable {

    private static final long serialVersionUID = 7489376731444554860L;


    private Date createDate;               //创建时间
    private String comment;                  //备注
    private Long addIntegralUserId;        //奖励人ID
    private String addIntegralUserName;      //奖励人名字

    public String createDateToString() {
        return DateUtils.dateToString(createDate, "yyyy年MM月dd日 HH:mm:ss");
    }
}
