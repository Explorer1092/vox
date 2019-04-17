package com.voxlearning.utopia.schedule.schedule.reward;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.newversion.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 修复托比装扮过期时间
 * @author: kaibo.he
 * @create: 2018-10-26 15:40
 **/
@Named
@ScheduledJobDefinition(
        jobName = "修复托比装扮过期时间",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING, Mode.PRODUCTION},
        ENABLED = false,
        cronExpression = "0 0 2 * * ? "
)
@ProgressTotalWork(100)
public class AutoRepairTobyTimeOutJob extends ScheduledJobWithJournalSupport {
    @Inject
    private RewardCenterClient rewardCenterClient;
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        progressMonitor.begin(10);
        progressMonitor.worked(1);
        Long userid = SafeConverter.toLong(parameters.get("userId"), 0L);
        logger.info(String.format("修复托比装扮过期时间开始，修复userId:%s", Objects.equals(userid, 0L) ? "全部":userid));
        progressMonitor.worked(1);
        if (!Objects.equals(userid, 0L)) {
            RewardCenterToby toby = rewardCenterClient.loadTobyByUserId(userid);
            if (!Objects.isNull(toby)) {
                fixAccessory(Collections.singletonList(toby));
                fixCountenance(Collections.singletonList(toby));
                fixImage(Collections.singletonList(toby));
                fixProps(Collections.singletonList(toby));
            }
            progressMonitor.worked(8);
            progressMonitor.done();
            return;
        }

        //修复装饰过期时间为-1的数据
        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadAccessoryErrExpiryTime(-1L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixAccessory(tobyList);
        }
        progressMonitor.worked(1);

        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadAccessoryErrExpiryTime(1604149125000L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixAccessory(tobyList);
        }
        progressMonitor.worked(1);

        //修复表情过期时间的数据
        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadCountenanceErrExpiryTime(-1L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixCountenance(tobyList);
        }
        progressMonitor.worked(1);

        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadCountenanceErrExpiryTime(1604149125000L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixCountenance(tobyList);
        }
        progressMonitor.worked(1);

        //修复形象过期时间的数据
        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadImageErrExpiryTime(-1L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixImage(tobyList);
        }
        progressMonitor.worked(1);

        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadImageErrExpiryTime(1604149125000L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixImage(tobyList);
        }
        progressMonitor.worked(1);

        //修复道具过期时间的数据
        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadPropsErrExpiryTime(-1L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixProps(tobyList);
        }
        progressMonitor.worked(1);
        while (true) {
            List<RewardCenterToby> tobyList = rewardCenterClient.loadPropsErrExpiryTime(1604149125000L);
            if (CollectionUtils.isEmpty(tobyList)) {
                break;
            }
            fixProps(tobyList);
        }
        progressMonitor.worked(1);
        progressMonitor.done();
    }

    private void fixAccessory(List<RewardCenterToby> tobyList) {
        tobyList.stream().forEach(toby -> {
            boolean isFix = false;
            if (Objects.equals(toby.getAccessoryExpiryTimeStamp(), Long.MAX_VALUE) || Objects.equals(toby.getAccessoryExpiryTimeStamp(), -1L)) {
                isFix = true;
                if (Objects.isNull(toby.getAccessoryId()) || Objects.equals(toby.getAccessoryId(), 0)) {
                    RewardCenterToby.defaultAccessory(toby);
                } else {
                    TobyAccessoryCVRecord accessoryCVRecord = rewardCenterClient.loadUserNewestTobyAccessory(toby.getAccessoryId(), toby.getUserId());
                    if (Objects.isNull(accessoryCVRecord)) {
                        RewardCenterToby.defaultAccessory(toby);
                    } else {
                        toby.setAccessoryExpiryTimeStamp(accessoryCVRecord.getExpiryTime());
                    }
                }
            }
            if (isFix) {
                rewardCenterClient.upsertToby(toby);
            }
        });
    }

    private void fixCountenance(List<RewardCenterToby> tobyList) {
        tobyList.stream().forEach(toby -> {
            boolean isFix = false;
            if (Objects.equals(toby.getCountenanceExpiryTimeStamp(), Long.MAX_VALUE) || Objects.equals(toby.getCountenanceExpiryTimeStamp(), -1L)) {
                isFix = true;
                if (Objects.isNull(toby.getCountenanceId()) || Objects.equals(toby.getCountenanceId(), 0)) {
                    RewardCenterToby.defaultCountenance(toby);
                } else {
                    TobyCountenanceCVRecord countenanceCVRecord = rewardCenterClient.loadUserNewestTobyCountenance(toby.getCountenanceId(), toby.getUserId());
                    if (Objects.isNull(countenanceCVRecord)) {
                        RewardCenterToby.defaultCountenance(toby);
                    } else {
                        toby.setCountenanceExpiryTimeStamp(countenanceCVRecord.getExpiryTime());
                    }
                }
            }
            if (isFix) {
                rewardCenterClient.upsertToby(toby);
            }
        });
    }

    private void fixImage(List<RewardCenterToby> tobyList) {
        tobyList.stream().forEach(toby -> {
            boolean isFix = false;
            if (SafeConverter.toLong(toby.getImageExpiryTimeStamp())==Long.MAX_VALUE || SafeConverter.toLong(toby.getImageExpiryTimeStamp())==-1L) {
                isFix = true;
                if (Objects.isNull(toby.getImageId()) || Objects.equals(toby.getImageId(), 0)) {
                    RewardCenterToby.defaultImage(toby);
                } else {
                    TobyImageCVRecord imageCVRecord = rewardCenterClient.loadUserNewestTobyImage(toby.getImageId(), toby.getUserId());
                    if (Objects.isNull(imageCVRecord)) {
                        RewardCenterToby.defaultImage(toby);
                    } else {
                        toby.setImageExpiryTimeStamp(imageCVRecord.getExpiryTime());
                    }
                }
            }

            if (isFix) {
                rewardCenterClient.upsertToby(toby);
            }
        });
    }

    private void fixProps(List<RewardCenterToby> tobyList) {
        tobyList.stream().forEach(toby -> {
            boolean isFix = false;
            if (Objects.equals(toby.getPropsExpiryTimeStamp(), Long.MAX_VALUE) || Objects.equals(SafeConverter.toLong(toby.getPropsExpiryTimeStamp()), -1L)) {
                isFix = true;
                if (Objects.isNull(toby.getPropsId()) || Objects.equals(toby.getPropsId(), 0)) {
                    RewardCenterToby.defaultProps(toby);
                } else {
                    TobyPropsCVRecord propsCVRecord = rewardCenterClient.loadUserNewestTobyProps(toby.getPropsId(), toby.getUserId());
                    if (Objects.isNull(propsCVRecord)) {
                        RewardCenterToby.defaultProps(toby);
                    } else {
                        toby.setPropsExpiryTimeStamp(propsCVRecord.getExpiryTime());
                    }
                }
            }

            if (isFix) {
                rewardCenterClient.upsertToby(toby);
            }
        });
    }
}
