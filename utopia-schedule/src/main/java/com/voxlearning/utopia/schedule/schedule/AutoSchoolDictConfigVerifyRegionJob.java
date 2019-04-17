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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaguang,wang
 * on 2016/7/3.
 */
@Named
@ScheduledJobDefinition(
        jobName = "字典表刷新字典表里的地区信息",
        jobDescription = "每天4:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 4 * * ?"
)
public class AutoSchoolDictConfigVerifyRegionJob extends ScheduledJobWithJournalSupport {

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Map<String, Object> command = new HashMap<>();
        command.put("command", "school_dict_config_verify");
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
    }
}

        /*List<AgentDictSchool> agentSchoolDictList = agentDictSchoolPersistence.findAllDictSchool();
        if (CollectionUtils.isEmpty(agentSchoolDictList)) {
            return;
        }
        progressMonitor.worked(20);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, agentSchoolDictList.size());
        agentSchoolDictList.forEach(p -> {
            try {
                if (p != null) {
                    School school = schoolLoaderClient.loadSchoolIncludeDisabled(p.getSchoolId());
                    if (school == null) {
                        return;
                    }
                    Integer regionCode = school.getRegionCode();
                    ExRegion exRegion = regionLoaderClient.loadRegion(regionCode);
                    if (exRegion == null) {
                        return;
                    }
                    p.setCountyCode(regionCode);
                    p.setCountyName(ConversionUtils.toString(exRegion.getCityName()) + ConversionUtils.toString(exRegion.getCountyName()));
                    p.setSchoolLevel(school.getLevel());
                    agentDictSchoolPersistence.upsert(p);
                }
            } catch (Exception ex) {
                logger.error("update school dict of regionCode and regionName is failed dictId=" + ConversionUtils.toString(p.getId()), ex);
            } finally {
                monitor.worked(1);
            }
        });
        progressMonitor.done();
    }
}*/
