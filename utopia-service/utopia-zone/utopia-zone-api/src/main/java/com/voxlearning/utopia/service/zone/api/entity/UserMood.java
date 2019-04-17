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

package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.zone.mdb.MDBUserMood;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 心情
 *
 * @author RuiBao
 * @version 0.1
 * @since 14-4-30
 */
@Getter
@Setter
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_USER_MOOD")
public class UserMood implements CacheDimensionDocument {
    private static final long serialVersionUID = 2554285900020896964L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;
    @DocumentCreateTimestamp
    private Date createDatetime;
    private String title;
    private String description;
    private String imgUrl;

    @Override
    public String[] generateCacheDimensions() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public MDBUserMood transform() {
        MDBUserMood t = new MDBUserMood();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setTitle(title);
        t.setDescription(description);
        t.setImgUrl(imgUrl);
        return t;
    }
}
