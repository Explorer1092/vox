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

package com.voxlearning.utopia.schedule.journal;


import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-statistics")
@DocumentCollection(collection = "vox_job_journal")
@DocumentIndexes({
        @DocumentIndex(def = "{'jobStartDate':-1,'jobName':1}", background = true)
})
public class JobJournal implements Serializable {
    private static final long serialVersionUID = -5826787370942088717L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;    // 任务ID
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private String jobName;                             // 任务名字
    private String jobClass;                            // 任务类名
    private String jobApplication;                      // 执行任务的应用程序
    private String jobIpAddresses;                      // 执行任务的主机IP地址
    private String jobHostname;                         // 执行任务的主机
    private String jobStartDate;                        // 任务启动的日期，格式为yyyyMMdd
    private Date jobStartTime;                          // 任务开始执行的时间
    private Date jobEndTime;                            // 任务结束执行的时间
    private Long duration;                              // 任务执行的时长，单位为毫秒
    private Boolean success;                            // 任务是否成功执行
    private String errorMessage;                        // 如果任务异常，捕获的异常信息
    private List<String> jobLogs;                       // 任务日志
}
