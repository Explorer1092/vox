package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.campaign.FlowPacketConvert;
import com.voxlearning.utopia.schedule.cache.ScheduleCacheSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.FlowPacketConvertServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:XiaochaoWei
 * @Description:
 * @CreateTime: 2017/4/14
 */
@Named
@ScheduledJobDefinition(
        jobName = "流量充值结果查询任务",
        jobDescription = "流量充值结果查询任务,每1小时运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ?"
)
@ProgressTotalWork(100)
public class AutoFlowPacketConvertQueryJob extends ScheduledJobWithJournalSupport {

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FlowPacketConvertServiceClient flowPacketConvertServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private ScheduleCacheSystem scheduleCacheSystem;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<FlowPacketConvert> chargingList = flowPacketConvertServiceClient.getFlowPacketConvertService()
                .findTobeCheckResultList()
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(chargingList)) {
            return;
        }
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, chargingList.size());
        for (FlowPacketConvert chargingItem : chargingList) {
            switch (chargingItem.fetchVendor()) {
                case AXJ:
                    if (!checkAXJChargingResult(chargingItem)) return;
                    break;
                case JJLL:
                    if (!checkJJLLChargingResult(chargingItem)) return;
                    break;
                case UNKNOWN:
                default:
                    break;
            }
            monitor.worked(1);
        }
        progressMonitor.done();
    }

    private boolean checkJJLLChargingResult(FlowPacketConvert chargingItem) {
        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

        String searchUrl = commonConfiguration.getFlowPacketSearchUrl();
        String us = commonConfiguration.getFlowPacketMerchantMark();
        String phone = sensitiveUserDataServiceClient.loadFlowPacketConvertTargetMobile(chargingItem.getId());
        String thirdSeq = chargingItem.getId();

        // 拼成请求的URL
        Map<String, String> params = new HashMap<>();
        params.put("us", us);
        params.put("phone", phone);
        params.put("thirdSeq", thirdSeq);

        try {
//            logger.info("Send flow packet charging check result request to {} with params {}",searchUrl,params);
            String URL = UrlUtils.buildUrlQuery(searchUrl, params);
//            logger.info("Send flow packet charging check result request to {}", URL);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
//            logger.info(" Flow packet check result response {} ", response.getResponseString());
            if (response.getStatusCode() == 200) {
                String json = response.getResponseString(Charset.forName("UTF-8"));
                Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
                if ("0".equals(resMap.get("result"))) {
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingSuccess(chargingItem.getId(), chargingItem.getTransactionId(), FlowPacketConvert.FlowPacketVendor.JJLL)
                            .awaitUninterruptibly();
                } else if ("10009".equals(resMap.get("result"))) { // 10009没有此订单号或订单号过期
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateUnsubmit(chargingItem.getId())
                            .awaitUninterruptibly();
                } else {
                    // 系统错误，发送提示邮件，状态不变
                    Map<String, Object> content = new HashMap<>();
                    content.put("info", " RESPONSE:" + response.getResponseString() + " params:" + params);
                    emailServiceClient.createTemplateEmail(EmailTemplate.office)
                            .to("xiaochao.wei@17zuoye.com")
                            .cc("zhilong.hu@17zuoye.com")
                            .subject(RuntimeMode.getCurrentStage() + "加加流量充值系统错误")
                            .content(content)
                            .send();
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateProcessTime(chargingItem.getId())
                            .awaitUninterruptibly();
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Flow packet convert query submit request failed.", e);
        }
        return true;
    }

    private boolean checkAXJChargingResult(FlowPacketConvert chargingItem) {
        long timeOut = 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        long createTime = chargingItem.getCreateDatetime().getTime();
        if (now - createTime > timeOut) {
            Map<String, Object> content = new HashMap<>();
            content.put("info", " 订单创建时间:" + chargingItem.getCreateDatetime() + " 订单ID:" + chargingItem.getId());

            String emailKey = CacheKeyGenerator.generateCacheKey(AutoFlowPacketConvertQueryJob.class,
                    new String[]{"userId", "date", "type"},
                    new Object[]{chargingItem.getUserId(), DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), "syserr-axj"});
            if (scheduleCacheSystem.CBS.flushable.get(emailKey) == null) {
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to("yuechen.wang@17zuoye.com;xiaochao.wei@17zuoye.com")
                        .cc("zhilong.hu@17zuoye.com")
                        .subject(RuntimeMode.getCurrentStage() + "安信捷流量回调超时")
                        .content(content)
                        .send();
                scheduleCacheSystem.CBS.flushable.set(emailKey, DateUtils.getCurrentToDayEndSecond(), "1");
            }
            flowPacketConvertServiceClient.getFlowPacketConvertService()
                    .updateProcessTime(chargingItem.getId())
                    .awaitUninterruptibly();
            return false;
        }
        return true;
    }

}
