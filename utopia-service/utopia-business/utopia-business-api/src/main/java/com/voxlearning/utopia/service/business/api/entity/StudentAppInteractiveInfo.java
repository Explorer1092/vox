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
import java.util.Map;

/**
 * Created by tanguohong on 14-4-10.
 * {
 * "userId":30006,
 * "unitId":5670,
 * "lessonId":56700001,
 * "practiceId":91,
 * "dataJson":{"integral":1000,"floors":5},
 * "createDatetime":ISODate("2014-04-04T17:12:17.563Z"),
 * "updateDatetime":ISODate("2014-04-04T17:12:17.563Z")
 * }
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "WashingtonDatabase")
@DocumentCollection(collection = "appInteractiveInfo")
@DocumentIndexes({
        @DocumentIndex(def = "{'userId':1,'bookId':1,'unitId':1,'lessonId':1,'practiceId':1}", background = true)
})
public class StudentAppInteractiveInfo implements Serializable {

    @DocumentId private String id;
    private Long userId;
    private Long bookId;
    private Long unitId;
    private Long lessonId;
    private Long practiceId;
    private Map<String, Object> dataJson;
    private Integer score;
    @DocumentCreateTimestamp private Date createDatetime;
    @DocumentUpdateTimestamp private Date updateDatetime;
}
