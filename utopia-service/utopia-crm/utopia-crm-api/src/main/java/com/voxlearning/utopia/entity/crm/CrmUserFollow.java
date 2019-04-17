/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关注列表
 * Created by Yuechen.Wang on 2016-07-18
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "user_follow_target")
@DocumentIndexes({
        @DocumentIndex(def = "{'followerId':1}", background = true),
        @DocumentIndex(def = "{'target':1}", background = true)
})
public class CrmUserFollow implements Serializable {

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createTime;                // 创建时间
    @DocumentUpdateTimestamp private Date updateTime;                // 更新时间
    private Boolean disabled;               // 删除标记

    private Long followerId;                // 关注人ID
    private String followerName;            // 关注人姓名
    private String target;                  // 被关注对象ID
    private String followType;              // 被关注对象类型
    private Boolean isFollowed;             // 当前是否关注

}
