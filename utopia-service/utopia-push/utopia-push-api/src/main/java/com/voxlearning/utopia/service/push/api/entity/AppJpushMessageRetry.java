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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.service.push.api.constant.PushType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author shiwe.liao
 * @since 2016/1/18
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-message")
@DocumentDatabase(database = "vox-app-message")
@DocumentCollection(collection = "vox_app_jpush_message_retry")
public class AppJpushMessageRetry implements Serializable {
    private static final long serialVersionUID = 1988953247379673288L;

    @DocumentId
    private ObjectId id;
    private String messageSource;                   // APP Key
    private String targetUrl;                       // 投递URL
    private String notify;                          // 消息内容
    private Integer retryCount;                     // 重试次数
    private Integer status;                         // 状态 0:未投递,1:已投递
    private PushType pushType;                      // push 对接的第三方
    private Integer httpStatusCode;                 //jpush错误码
    private String cause;                           //jpush错误原因
    private String notifyId;                        // 消息Id
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
}
