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

package com.voxlearning.utopia.service.newhomework.api.entity;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-3-21
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-statistics")
@DocumentCollection(collection = "vox_possible_cheating_homework")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1}", background = true),
        @DocumentIndex(def = "{'createDatetime':-1}", background = true)
})
public class PossibleCheatingHomework implements Serializable {
    private static final long serialVersionUID = -1157370255414371789L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createDatetime;
    private String homeworkId;
    private Long clazzId;
    private Long teacherId;
    private HomeworkType homeworkType;
    private String reason;
    private Map<Long, Object> teacherIntegral;
    private List<Map<Long, Object>> studentIntegral;
    private Boolean isAddIntegral;
    private Boolean recordOnly;                             // 只记录，仍加金币
}
