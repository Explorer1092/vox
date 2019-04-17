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

package com.voxlearning.utopia.admin.entity;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.*;

import java.io.Serializable;

/**
 * 非正常老师的信息
 *
 * @author Alex
 * @version 0.1
 * @since 2015-07-01
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_crm_unusual_teacher")
@DocumentIndexes({
        @DocumentIndex(def = "{'cityCode':1}", background = true)
})
public class CrmUnusualTeacher implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = -8614835715523321333L;

    @DocumentId private String id;
    private Long teacherId;                           // 老师ID
    private Integer provinceCode;                     // 老师所在省CODE
    private String provinceName;                      // 老师所在省名
    private Integer cityCode;                         // 老师所在市CODE
    private String cityName;                          // 老师所在市名
    private Integer countyCode;                       // 老师所在区CODE
    private String countyName;                        // 老师所在区名
    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

}
