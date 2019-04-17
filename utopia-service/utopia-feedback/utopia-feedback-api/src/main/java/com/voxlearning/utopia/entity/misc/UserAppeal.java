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

package com.voxlearning.utopia.entity.misc;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Summer Yang on 2016/7/20.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_USER_APPEAL")
public class UserAppeal extends AbstractDatabaseEntity implements CacheDimensionDocument {
    private static final long serialVersionUID = -6106325518237944336L;

    @DocumentField("USER_ID")
    private Long userId;

    @DocumentField("SCHOOL_ID")
    private Long schoolId;

    @DocumentField("USER_NAME")
    private String userName;

    @DocumentField("SCHOOL_NAME")
    private String schoolName;

    @DocumentField("PNAME")
    private String pname;

    @DocumentField("CNAME")
    private String cname;

    @DocumentField("ANAME")
    private String aname;

    @DocumentField("REASON")
    private String reason;

    @DocumentField("FILE_NAME")
    private String fileName;

    @DocumentField("STATUS")
    private Status status;

    @DocumentField("COMMENT")
    private String comment;

    @DocumentField("AUDIT_ID")
    private String auditId;

    @DocumentField("AUDIT_TIME")
    private Date auditTime;

    @DocumentField("TYPE")
    private Type type;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        WAIT("等待审核"),
        PASS("审核通过"),
        UNPASS("驳回");

        @Getter
        private final String description;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {
        FAKE("账号异常"),
        CHEATING("作业异常");

        @Getter
        private final String description;
    }
}
