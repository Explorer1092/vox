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

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author Jia HuanYin
 * @since 2015/11/26
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_school_task")
public class AgentSchoolTask extends IAgentTask {
    private static final long serialVersionUID = 6069909298252655576L;

    @DocumentId
    private String id;
    private Long schoolId;
    private String schoolName;
    private Long contactId;
    private String contactName;
    private Long executorId;
    private String executorName;
    private String executeNote;
    private Boolean pushed;
    private Boolean finished;
    private Date finishTime;

    public void push() {
        this.pushed = true;
    }

    public boolean pushed() {
        return Boolean.TRUE == this.pushed;
    }

    public void finish(String executeNote) {
        this.finishTime = new Date();
        this.finished = true;
        this.executeNote = executeNote;
    }

    public boolean finished() {
        return Boolean.TRUE == this.finished;
    }
}