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
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 14-7-2.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "readingDraft")
@DocumentIndexes({
        @DocumentIndex(def = "{'ugcAuthor':1}", background = true)
})
public class ReadingDraft implements Serializable {
    private static final long serialVersionUID = 1515393316817935734L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createDatetime;
    @DocumentUpdateTimestamp private Date updateDatetime;

    private String cname;//阅读中文名称
    private String ename;//阅读英文名称
    private List<Long> points;//知识点id
    private Integer difficultyLevel;//难度级别
    private String status; // 状态，draft:草稿,published:发布,verifyed:审核通过,verifyFailure:审核失败
    private String style;//体裁
    private Long ugcAuthor;
    private Integer wordsCount;
    private Integer recommendTime;
    private Map<String, Object> content;
}
