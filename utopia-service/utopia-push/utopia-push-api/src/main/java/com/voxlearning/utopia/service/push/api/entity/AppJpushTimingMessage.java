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

package com.voxlearning.utopia.service.push.api.entity;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 27/7/2016
 * JPush 定时消息
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-message")
@DocumentDatabase(database = "vox-app-message")
@DocumentCollection(collection = "vox_app_jpush_timing_message_{}", dynamic = true)
@DocumentIndexes(
        @DocumentIndex(def = "{'sendTime':-1}", background = true)
)
@DocumentRangeable(range = DateRangeType.M, age = 1)
public class AppJpushTimingMessage implements Serializable {

    private static final long serialVersionUID = 1592197887096918102L;

    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Long createTime;

    private String messageSource; //消息来源
    private String notify; //消息内容
    private Long sendTime;  //预订的发送时间
    private Date expireAt;
}
