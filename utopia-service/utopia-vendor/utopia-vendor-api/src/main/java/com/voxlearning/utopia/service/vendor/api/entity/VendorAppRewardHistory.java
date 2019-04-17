/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.LongIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by tanguohong on 14-1-14.
 *
 * @author Guohong Tan
 * @serial
 * @since Jan 14, 2014
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_vendor")
@DocumentTable(table = "VOX_VENDOR_APP_REWARD_HISTORY")
public class VendorAppRewardHistory extends LongIdEntityWithDisabledField {
    private static final long serialVersionUID = 4306459661360543740L;

    @DocumentField("USER_ID") private Long userId;                 // 用户ID
    @DocumentField("APP_ID") private Integer appId;                // 应用ID
    @DocumentField("REWARD_TYPE") private String rewardType;       // 奖励类型
    @DocumentField("REWARD_VALUE") private Integer rewardValue;    // 奖励数量
    @DocumentField("COMMENT") private String comment;              // 备注说明
}
