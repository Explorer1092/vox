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

package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by fanshuo.iu  on 2015/5/25.
 * FIXME: 这玩意还有用吗？
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "CrmActivityUserData")
public class ActivityData implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -8543654624184603321L;

    @DocumentId private String id;
    private Date createDatetime;
    private Date updateDatetime;
    private String userId;
    private Map description;
    private String activityId;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createDatetime == null) createDatetime = new Date(timestamp);
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateDatetime = new Date(timestamp);
    }
}