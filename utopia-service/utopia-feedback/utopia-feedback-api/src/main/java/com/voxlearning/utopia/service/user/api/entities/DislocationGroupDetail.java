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

package com.voxlearning.utopia.service.user.api.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author fugui.chang
 * @since 2016-10-14 19:20
 */
@Getter
@Setter
@NoArgsConstructor
public class DislocationGroupDetail extends DislocationGroup {
    private static final long serialVersionUID = -5229009387283953643L;

    private String realSchoolName;
    private String currentSchoolId;
    private String currentSchoolName;

    public DislocationGroupDetail(DislocationGroup dislocationGroup) {
        this.setId(dislocationGroup.getId());
        this.setGroupId(dislocationGroup.getGroupId());
        this.setRealSchoolId(dislocationGroup.getRealSchoolId());
        this.setNotes(dislocationGroup.getNotes());
        this.setLatestOperator(dislocationGroup.getLatestOperator());
        this.setDisabled(dislocationGroup.getDisabled());
        this.setCreateDatetime(dislocationGroup.getCreateDatetime());
        this.setUpdateDatetime(dislocationGroup.getUpdateDatetime());
    }
}
