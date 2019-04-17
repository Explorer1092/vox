/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.entity.crm;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.api.constant.CrmContactType;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2015/10/24
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_task_record")
public class CrmTaskRecord implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -1871240268641795780L;

    @DocumentId
    private String id;
    private String taskId;
    private String recorder;
    private String recorderName;
    private Long userId;
    private String userName;
    private UserType userType;
    private CrmTaskRecordCategory firstCategory;
    private CrmTaskRecordCategory secondCategory;
    private CrmTaskRecordCategory thirdCategory;
    private CrmContactType contactType;
    private String title;
    private String content;
    private Integer callTime;
    private String audioUrl;
    private Date createTime;
    private Date updateTime;

    private String agentTaskId; //市场人员的任务ID

    private String callerId;     //客服人员的ID
    private transient String niceCreateTime;

    public void touchCategory(CrmTaskRecordCategory recordCategory) {
        thirdCategory = recordCategory;
        if (thirdCategory != null) {
            secondCategory = thirdCategory.parent;
        }
        if (secondCategory != null) {
            firstCategory = secondCategory.parent;
        }
    }

    @Override
    public void touchCreateTime(long timestamp) {
        if (createTime == null) {
            createTime = new Date(timestamp);
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateTime = new Date(timestamp);
    }
}
