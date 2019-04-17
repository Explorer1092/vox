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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
public class JobJournalDao extends StaticMongoDao<JobJournal, String> {
    @Override
    protected void calculateCacheDimensions(JobJournal source, Collection<String> dimensions) {
    }

    public void finish(String jobId, long jobEndTime, Long duration, boolean success, String errorMessage) {
        Update update = updateBuilder.build()
                .set("jobEndTime", new Date(jobEndTime))
                .set("duration", duration)
                .set("success", success)
                .set("errorMessage", StringUtils.defaultString(errorMessage));
        update(jobId, update);
    }

    public void logging(String jobId, String logMessage) {
        if (jobId == null || logMessage == null) {
            return;
        }
        Update update = updateBuilder.build().push("jobLogs", logMessage);
        update(jobId, update);
    }

    public List<JobJournal> findByStartDate(String jobStartDate) {
        Filter filter = filterBuilder.where("jobStartDate").is(jobStartDate);
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "jobStartTime"));
        return __find_OTF(find);
    }
}
