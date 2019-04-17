package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2018/11/20
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语自动发送优惠券（50元）",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoSendChipsCouponJob extends ScheduledJobWithJournalSupport {
    @Inject CouponServiceClient couponServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        String userIds = SafeConverter.toString(parameters.get("userIds"));
        if (StringUtils.isBlank(userIds)) {
            logger.error("no userIds , param error.");
            return;
        }
//        String userIds = "23321172,218447285,215962806,211404110,25531725,26718609,215696857,217003919,217044791,216820178,225543231,217110540,25026034,216220423,218746854,212472109,215985306,216161280,213451534,225766692,27693915,216488319,226825858,212074234,25630926,221768535,212113221,211784646,217409168,225836924,213753458,218437559,23752230,215079814,225397074,218447830,222508661,25979450,29730059,25630926,215590378,215070905,216969065,222363002,28136908,225608570,217235824,219927632,26425927,217695084,216194225,225700993,211673231,25275898,222368332,222055746,216091597,211907332,211275817,211901576,22525934,215726477,215137858,222424946,219562145,216881903,223981963,224043507";
        Set<String> userIdSet = new HashSet<>(Arrays.asList(StringUtils.split(userIds, ",")));
        for (String uid : userIdSet) {
            MapMessage message = couponServiceClient.sendCoupon("5b73d6ee6816f84db5411798", SafeConverter.toLong(uid));
            if (!message.isSuccess()) {
                logger.warn("Chips SendCoupon job error, uid {}, error {}", uid, message.getInfo());
            }
        }
    }
}
