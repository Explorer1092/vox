/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.data.CrmMainSubApplyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 包班申请
 *
 * @author Yuechen.wang
 * @since 2016-07-22
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "main_sub_account_apply")
public class CrmMainSubAccountApply implements Serializable {


    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createTime;                // 创建时间
    @DocumentUpdateTimestamp private Date updateTime;                // 更新时间
    private Boolean disabled;               // 删除标记


    private Long applicantId;               // 发起人ID
    private String applicantName;           // 发起人姓名
    private Long teacherId;                 // 老师ID
    private String teacherName;             // 老师姓名
    private Subject currentSubject;         // 当前所教学科
    private Subject applySubject;           // 申请包班学科
    private Long schoolId;                  // 申请包班学校ID
    private String schoolName;              // 申请包班学校名称
    private Long clazzId;                   // 申请包班班级ID
    private String clazzName;               // 申请包班班级名称

    private Date auditTime;                 // 审核时间
    private Long auditor;                   // 市场审核人ID
    private String auditorName;             // 市场审核人姓名
    private String auditorRole;             // 市场审核人角色
    private CrmMainSubApplyStatus auditStatus;  // 审核状态 （申请中、已开通、审核未通过）
    private String auditNote;               // 审核备注

    public static String ck_uid(Long applicantId){
        return CacheKeyGenerator.generateCacheKey(CrmMainSubAccountApply.class, "UID", applicantId);
    }

    public static CrmMainSubAccountApply newInstance() {
        CrmMainSubAccountApply apply = new CrmMainSubAccountApply();
        apply.setAuditStatus(CrmMainSubApplyStatus.PENDING);
        apply.setDisabled(false);
        return apply;
    }

    public boolean canBeApplied() {
        return auditStatus != null && (CrmMainSubApplyStatus.PENDING == auditStatus || CrmMainSubApplyStatus.APPROVED == auditStatus);
    }
}
