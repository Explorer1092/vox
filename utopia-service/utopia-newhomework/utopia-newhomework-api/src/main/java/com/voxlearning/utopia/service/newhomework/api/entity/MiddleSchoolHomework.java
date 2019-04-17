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
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-middleschool")
@DocumentDatabase(database = "vox-studycraft")
@DocumentCollection(collection = "sc_homework")
public class MiddleSchoolHomework implements Serializable {

    private static final long serialVersionUID = 7910922808180559496L;

    @DocumentId
    private String id;

    @DocumentField("name")
    private String name;

    @DocumentField("clazz_id")
    private Long groupId;

    @DocumentField("status")
    private Long status;

    @DocumentField("create_time")
    private Date createTime;

    @DocumentField("start_time")
    private Date startTime;

    @DocumentField("close_time")
    private Date closeTime;
}
