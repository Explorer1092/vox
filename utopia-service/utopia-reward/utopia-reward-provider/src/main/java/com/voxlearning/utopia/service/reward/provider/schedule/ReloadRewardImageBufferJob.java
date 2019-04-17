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

package com.voxlearning.utopia.service.reward.provider.schedule;

import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardImageList;
import com.voxlearning.utopia.service.reward.base.buffer.RewardImageBuffer;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardImageService;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.version.RewardImageVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "ReloadRewardImageBufferJob",
        cronExpression = "0 */5 * * * ?")
@ProgressTotalWork(100)
public class ReloadRewardImageBufferJob extends ProgressedScheduleJob {

    @Inject private InternalRewardImageService internalRewardImageService;
    @Inject private RewardImageVersion rewardImageVersion;
    @Inject private RewardLoaderImpl rewardLoader;

    @Override
    protected void executeJob(long startTimestamp,
                              Map<String, Object> parameters,
                              ISimpleProgressMonitor progressMonitor) {
        RewardImageBuffer buffer = rewardLoader.getRewardImageBuffer();
        long current = rewardImageVersion.currentVersion();
        if (current != 1 && buffer.getVersion() < current) {
            VersionedRewardImageList data = internalRewardImageService.loadVersionedRewardImageList();
            buffer.attach(data);
            //logger.info("Buffer [RewardImage] reloaded, update version to {}.", data.getVersion());
        }

        progressMonitor.done();
    }
}
