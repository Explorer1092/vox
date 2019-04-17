package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.partner.api.entity.ThirdPartyUserInfo;
import com.voxlearning.galaxy.service.partner.api.entity.ThirdPartyUserInfoRef;
import com.voxlearning.galaxy.service.partner.api.service.MizarThirdPartyService;
import com.voxlearning.galaxy.service.partner.api.service.ThirdPartyService;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/6/25
 */
@Named
@ScheduledJobDefinition(
        jobName = "第三方数据从mongo迁移到mysql",
        jobDescription = "手动执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 1 ? * MON",
        ENABLED = false)
@ProgressTotalWork(100)
public class AutoTransferDataFromMongoToMysql extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = MizarThirdPartyService.class)
    private MizarThirdPartyService mizarThirdPartyService;

    @ImportService(interfaceClass = ThirdPartyService.class)
    private ThirdPartyService thirdPartyService;

    @Override

    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<ThirdPartyUserInfo> thirdPartyUserInfos = mizarThirdPartyService.queryAllForJob();
        if (CollectionUtils.isEmpty(thirdPartyUserInfos)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(90, thirdPartyUserInfos.size());
        for (ThirdPartyUserInfo thirdPartyUserInfo : thirdPartyUserInfos) {
            if (SafeConverter.toLong(thirdPartyUserInfo.getRecordId()) != 0L) {
                continue;
            }
            ThirdPartyUserInfoRef thirdPartyUserInfoRef = new ThirdPartyUserInfoRef();
            thirdPartyUserInfoRef.setInfoId(thirdPartyUserInfo.getId());
            thirdPartyUserInfoRef.setChildAge(thirdPartyUserInfo.getChildAge());
            thirdPartyUserInfoRef.setChildName(thirdPartyUserInfo.getChildName());
            thirdPartyUserInfoRef.setClazzLevel(thirdPartyUserInfo.getClazzLevel());
            thirdPartyUserInfoRef.setMobile(thirdPartyUserInfo.getMobile());
            thirdPartyUserInfoRef.setOrderId(thirdPartyUserInfo.getOrderId());
            thirdPartyUserInfoRef.setParentId(thirdPartyUserInfo.getParentId());
            thirdPartyUserInfoRef.setRegionCode(thirdPartyUserInfo.getRegionCode());
            thirdPartyUserInfoRef.setThirdPartyTypeId(thirdPartyUserInfo.getThirdPartyTypeId());
            thirdPartyUserInfoRef.setCreateDatetime(thirdPartyUserInfo.getCreateDate());
            thirdPartyUserInfoRef.setUpdateDatetime(thirdPartyUserInfo.getUpdateDate());

            ThirdPartyUserInfoRef userInfoRef = thirdPartyService.upsertUserInfoRef(thirdPartyUserInfoRef);
            thirdPartyUserInfo.setRecordId(userInfoRef.getId());
            thirdPartyService.upsertUserInfo(thirdPartyUserInfo);
            iSimpleProgressMonitor.worked(1);
        }
    }
}
