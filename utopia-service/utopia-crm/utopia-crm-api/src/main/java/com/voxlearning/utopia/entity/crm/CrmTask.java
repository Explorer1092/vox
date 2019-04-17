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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.api.constant.CrmTaskStatus;
import com.voxlearning.utopia.api.constant.CrmTaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/10/19
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_task")
public class CrmTask implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -1634855775523321312L;

    @DocumentId
    private String id;
    private String creator;
    private String creatorName;
    private String executor;
    private String executorName;
    private Long userId;
    private String userName;
    private UserType userType;
    private CrmTaskType type;
    private Date endTime;
    private String title;
    private String content;
    private CrmTaskStatus status;
    private Date createTime;
    private Date updateTime;
    private String agentTaskId; //市场流转过来的任务ID

    private transient String niceEndTime;
    private transient String niceCreateTime;
    private transient String niceStatus;

    @DocumentField("userMobile")
    private transient String userSensitiveMobile;
    private transient AuthenticationState userAuthStatus;
    private transient List<CrmTaskStub> taskStubs;

    private Boolean disabled;  //是否禁用
    private String applicantName; // 申请者名称
    private String applicantMobile; // 申请人电话

    // Agent 的审核信息
    private String otherLinkMan;            // 其它联系人
    private Boolean affirmTransferSchool;   // 转校是否正确
    private Boolean affirmTransferClass;    // 转班是否正确
    private String transferSchoolReason;    // 转校原因
    private String remark;                  // 备注
    private String auditorId;               // 审核人
    private String auditorName;             // 审核人名称
    private Boolean isProof;               // 是否被审核

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
