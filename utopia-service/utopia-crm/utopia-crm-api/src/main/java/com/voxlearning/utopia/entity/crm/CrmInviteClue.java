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
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/12
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_invite_clue")
public class CrmInviteClue implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 6482628799100385282L;

    @DocumentId private String id;
    private Date createTime;
    private Date updateTime;
    private Long inviterId;
    private String inviterName;
    private String inviterMobile;
    private String inviterSubject;
    private Long inviterSchoolId;
    private String inviterSchoolName;
    private Integer inviterCountyCode;
    private String inviterCountyName;
    private Integer inviterCityCode;
    private String inviterCityName;
    private Integer inviterProvinceCode;
    private String inviterProvinceName;
    private Long inviteeId;
    private String inviteeName;
    private String inviteeMobile;
    private String inviteeSubject;
    private String inviteeSchoolName;
    private String status;
    private String inviteeReturnVisitId;//被邀请人回访ID
    private String inviterReturnVisitId;//邀请人回访ID

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
