/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.campaign.FlowPacketConvert;
import com.voxlearning.utopia.schedule.cache.ScheduleCacheSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.JiajialiuliangUtil;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.finance.client.FlowPacketConvertServiceClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
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
 * @CreateTime: 2017/4/13
 */

@Named
@ScheduledJobDefinition(
        jobName = "流量充值任务",
        jobDescription = "流量充值任务,每5分钟运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.TEST, Mode.STAGING},
        cronExpression = "0 */5 * * * ?"
)
@ProgressTotalWork(100)
public class AutoFlowPacketConvertJob extends ScheduledJobWithJournalSupport {

//    加加流量异常吗
//    private static final Map<String, String> SYS_ERROR_CODE_MAP = new HashMap<>();
//    static {
//        SYS_ERROR_CODE_MAP.put("10001", "参数错误");
//        SYS_ERROR_CODE_MAP.put("10002", "手机未查到运营商");
//        SYS_ERROR_CODE_MAP.put("10003", "用户错误");
//        SYS_ERROR_CODE_MAP.put("10004", "操作失败+失败码");
//        SYS_ERROR_CODE_MAP.put("10005", "未找到归属地");
//        SYS_ERROR_CODE_MAP.put("10006", "MD5参数校验失败");
//        SYS_ERROR_CODE_MAP.put("10007", "没有匹配到产品 ");
//        SYS_ERROR_CODE_MAP.put("10008", "商家流量费用不够支付!");
//        SYS_ERROR_CODE_MAP.put("10009", "没有此订单号或订单号过期");
//        SYS_ERROR_CODE_MAP.put("10010", "商户订单号为空");
//        SYS_ERROR_CODE_MAP.put("10011", "订单不能重复提交");
//        SYS_ERROR_CODE_MAP.put("10012", "参数异常");
//        SYS_ERROR_CODE_MAP.put("10013", "查询订单接口（订单不存在）");
//        SYS_ERROR_CODE_MAP.put("19999", "加加流量系统异常");
//    }

    // 安信捷流量异常码
    private static final Map<String, String> REQUEST_ERROR_CODE_MAP = new HashMap<>();

    static {
        REQUEST_ERROR_CODE_MAP.put("Error10", "IP认证错");
        REQUEST_ERROR_CODE_MAP.put("Error20", "参数有空值，或者手机号非法");
        REQUEST_ERROR_CODE_MAP.put("Error30", "鉴权错误");
        REQUEST_ERROR_CODE_MAP.put("Error40", "产品代码错误");
        REQUEST_ERROR_CODE_MAP.put("Error50", "用户欠费");
    }

    // 电信手机号段
    private static final String[] TELECOM = {"133", "153", "189", "180", "181", "177", "173"};
    // 联通手机号段
    private static final String[] UNICOM = {"130", "131", "132", "156", "155", "186", "185", "145", "176"};
    // 移动手机号段
    private static final String[] MOBILE = {"139", "138", "137", "136", "135", "134", "159", "158", "157", "150", "151",
            "152", "147", "188", "187", "182", "183", "184", "178"};
    // 安信捷产品代码映射
    private static final Map<Integer, String> TELECOM_MAP = new HashMap<>();

    static {
        TELECOM_MAP.put(5, "1100004");
        TELECOM_MAP.put(10, "1100006");
        TELECOM_MAP.put(30, "1100008");
        TELECOM_MAP.put(50, "1100014");
        TELECOM_MAP.put(100, "1100016");
        TELECOM_MAP.put(200, "1100018");
        TELECOM_MAP.put(500, "1100024");
        TELECOM_MAP.put(1024, "1100026");
        TELECOM_MAP.put(2048, "1100028");
        TELECOM_MAP.put(3072, "1100034");
    }

    private static final Map<Integer, String> UNICOM_MAP = new HashMap<>();

    static {
        UNICOM_MAP.put(20, "1100001");
        UNICOM_MAP.put(30, "1100017");
        UNICOM_MAP.put(50, "1100003");
        UNICOM_MAP.put(100, "1100007");
        UNICOM_MAP.put(200, "1100011");
        UNICOM_MAP.put(300, "1100021");
        UNICOM_MAP.put(500, "1100013");
        UNICOM_MAP.put(1024, "1100023");
    }

    private static final Map<Integer, String> MOBILE_MAP = new HashMap<>();

    static {
        MOBILE_MAP.put(10, "1100000");
        MOBILE_MAP.put(30, "1100002");
        MOBILE_MAP.put(70, "1100005");
        MOBILE_MAP.put(100, "1100029");
        MOBILE_MAP.put(150, "1100009");
        MOBILE_MAP.put(500, "1100010");
        MOBILE_MAP.put(1024, "1100012");
        MOBILE_MAP.put(2048, "1100024");
        MOBILE_MAP.put(3072, "1100019");
        MOBILE_MAP.put(4096, "1100020");
        MOBILE_MAP.put(6144, "1100022");
        MOBILE_MAP.put(11264, "1100025");
    }

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FlowPacketConvertServiceClient flowPacketConvertServiceClient;
    @Inject private ScheduleCacheSystem scheduleCacheSystem;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private RewardServiceClient rewardServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<FlowPacketConvert> chargingList = flowPacketConvertServiceClient.getFlowPacketConvertService()
                .findTobeChargingList()
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(chargingList)) {
            return;
        }

        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, chargingList.size());
        // 获取流量充值通道
        int threshold = SafeConverter.toInt(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "FLOW_PACKET_CHANNEL")
        );

        for (FlowPacketConvert chargingItem : chargingList) {
            // 默认调用加加流量充值
            if (threshold == 0 || RandomUtils.nextInt(100) > threshold) {
                if (!submitJJLLChargingRequest(chargingItem)) {
                    return;
                }
            } else {
                // 调用安信捷流量充值
                if (!submitAXJChargingRequest(chargingItem)) {
                    return;
                }
            }
            monitor.worked(1);
        }
        progressMonitor.done();
    }

    /**
     * 加加流量充值请求
     */
    private boolean submitJJLLChargingRequest(FlowPacketConvert chargingItem) {
        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
        String requestUrl = commonConfiguration.getFlowPacketRequestUrl();
        String us = commonConfiguration.getFlowPacketMerchantMark();
        String ts = String.valueOf(System.currentTimeMillis());
        String thirdSeq = chargingItem.getId();
        String phone = sensitiveUserDataServiceClient.loadFlowPacketConvertTargetMobile(chargingItem.getId());

        // 先查询一下，避免重复提交订单
        String searchUrl = commonConfiguration.getFlowPacketSearchUrl();

        // 拼装查询请求的URL
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("us", us);
        searchParams.put("phone", phone);
        searchParams.put("thirdSeq", thirdSeq);
        String searchURL = UrlUtils.buildUrlQuery(searchUrl, searchParams);
        AlpsHttpResponse searchResponse = HttpRequestExecutor.defaultInstance().get(searchURL).execute();
        try {
            if (searchResponse.getStatusCode() == 200) {
                String json = searchResponse.getResponseString(Charset.forName("UTF-8"));
                Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
                if ("0".equals(resMap.get("result"))) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Flow packet convert search submit request failed. response={}", searchResponse.getResponseString(), e);
        }

        String flow = String.valueOf(chargingItem.getFlowSize());
        String type = "1"; // 漫游属性1.全国 2.本地
        String notify = ProductConfig.getMainSiteBaseUrl() + commonConfiguration.getFlowPacketCallbackUrl();
        String pass = chargingItem.getId();
        // 参数拼接
        String paramStr = "phone=" + phone + "&flow=" + flow + "&type=" + type + "&notify=" + notify + "&pass=" + pass;
        String signStr = us + "||" + phone + "||" + flow + "||" + type + "||" + ts + "||" + JiajialiuliangUtil.saltKey;
        try {
            // 参数加密
            String param = JiajialiuliangUtil.encrypt(paramStr, JiajialiuliangUtil.saltKey, JiajialiuliangUtil.saltKey);
            String sign = JiajialiuliangUtil.MD5(signStr);
            Map<String, String> params = new HashMap<>();
            params.put("us", us);
            params.put("parm", param);
            params.put("ts", ts);
            params.put("sig", sign);
            params.put("thirdSeq", thirdSeq);
//            logger.info("Send flow packet charging request to {} with params {}", requestUrl, params);
            String URL = UrlUtils.buildUrlQuery(requestUrl, params);
//            logger.info("Send flow packet charging request url {}", URL);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(URL).execute();
//            logger.info("Send flow packet charging response {} ", response.getResponseString());
            if (response.getStatusCode() == 200) {
                String json = response.getResponseString(Charset.forName("UTF-8"));
                Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
                if ("0".equals(resMap.get("result")) && "0".equals(resMap.get("callback"))) {
                    //  添加流量充值成功逻辑，改数据库状态为2  订单默认是充值成功，失败才回调
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingSuccess(chargingItem.getId(), String.valueOf(resMap.get("transactionId")), FlowPacketConvert.FlowPacketVendor.JJLL)
                            .awaitUninterruptibly();
                } else if ("0".equals(resMap.get("result")) && !"0".equals(resMap.get("callback"))) {
                    //  添加流量充值请求已提交逻辑，改数据库状态为1，待异步回调成功后奖状态改为2
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingSubmitSuccess(chargingItem.getId(), String.valueOf(resMap.get("transactionId")), FlowPacketConvert.FlowPacketVendor.JJLL)
                            .awaitUninterruptibly();
                } else if ("10007".equals(resMap.get("result"))) { // 10007没有匹配到产品
                    // result返回一定是不为0的异常码，改数据库状态为9 并保存错误码、错误信息
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingFailed(chargingItem.getId(), String.valueOf(resMap.get("result")), String.valueOf(resMap.get("msg")), FlowPacketConvert.FlowPacketVendor.JJLL)
                            .awaitUninterruptibly();
                    // 运营商不匹配透传给奖品中心-订单
                    rewardServiceClient.cancelFlowPacketOrder(Long.valueOf(chargingItem.getOrderNo()), String.valueOf(resMap.get("msg")));
                } else if ("10011".equals(resMap.get("result"))) { // 10011订单重复提交
                    // 什么都不做
                } else {
                    String emailKey = CacheKeyGenerator.generateCacheKey(AutoFlowPacketConvertJob.class,
                            new String[]{"userId", "date", "type"},
                            new Object[]{chargingItem.getUserId(), DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), "syserr-jjll"});

                    if (scheduleCacheSystem.CBS.flushable.get(emailKey) == null) {
                        Map<String, Object> content = new HashMap<>();
                        content.put("info", "RESPONSE:" + response.getResponseString() + " paramStr:" + paramStr + " signStr:" + signStr + " params:" + params);
                        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                                .to("xiaochao.wei@17zuoye.com")
                                .cc("zhilong.hu@17zuoye.com")
                                .subject(RuntimeMode.getCurrentStage() + "加加流量充值系统错误")
                                .content(content)
                                .send();
                        scheduleCacheSystem.CBS.flushable.set(emailKey, DateUtils.getCurrentToDayEndSecond(), "1");
                    }
                    // 坑比较多，将本条数据后移确保其他充值可以执行
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateProcessTime(chargingItem.getId())
                            .awaitUninterruptibly();
                    // 告诉奖品中心-订单 流量充值失败
                    rewardServiceClient.cancelFlowPacketOrder(Long.valueOf(chargingItem.getOrderNo()), "充值失败");
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Flow packet convert submit request failed.", e);
        }
        return true;
    }

    /**
     * 安信捷流量充值请求
     */
    private boolean submitAXJChargingRequest(FlowPacketConvert chargingItem) {
        try {
            CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
            String reqUrl = commonConfiguration.getFlowPacketReqUrl();
            String name = commonConfiguration.getFlowPacketName();
            String pass = commonConfiguration.getFlowPacketPass();
            String seqnum = chargingItem.getId();
            String mobiles = sensitiveUserDataServiceClient.loadFlowPacketConvertTargetMobile(chargingItem.getId());
            String procode = this.getProcode(mobiles, chargingItem.getFlowSize());
            String itype = "5"; // 流量单品（默认值为5）
            String signTmp = pass + name + seqnum + mobiles + procode + itype + pass;
            String authpara = DigestUtils.sha256Hex(signTmp);
            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            params.put("seqnum", seqnum);
            params.put("mobiles", mobiles);
            params.put("procode", procode);
            params.put("itype", itype);
            params.put("authpara", authpara);
//            logger.info("Send flow packet charging request to {} with params {}", reqUrl, params);
            String URL = UrlUtils.buildUrlQuery(reqUrl, params);
//            logger.info("Send flow packet charging request url {}", URL);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(URL).execute();
//            logger.info("Send flow packet charging response {} ", response.getResponseString());
            if (response.getStatusCode() == 200) {
                String json = response.getResponseString(Charset.forName("UTF-8"));
                Map<String, Object> resMap = JsonUtils.convertJsonObjectToMap(json);
                String resultCode = SafeConverter.toString(resMap.get("result_code"));
                if ("0000".equals(resultCode)) {
                    //  流量充值提交成功逻辑，改数据库状态为1  订单默认是充值成功，失败才回调
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingSubmitSuccess(chargingItem.getId(), String.valueOf(resMap.get("request_no")), FlowPacketConvert.FlowPacketVendor.AXJ)
                            .awaitUninterruptibly();
                } else if (REQUEST_ERROR_CODE_MAP.containsKey(resultCode)) {
                    // 改数据库状态为9 并保存错误码、错误信息
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateChargingFailed(chargingItem.getId(), resultCode, REQUEST_ERROR_CODE_MAP.get(resultCode), FlowPacketConvert.FlowPacketVendor.AXJ)
                            .awaitUninterruptibly();
                    // 运营商不匹配透传给奖品中心-订单
                    rewardServiceClient.cancelFlowPacketOrder(Long.valueOf(chargingItem.getOrderNo()), "充值失败");
                } else {
                    String emailKey = CacheKeyGenerator.generateCacheKey(AutoFlowPacketConvertJob.class,
                            new String[]{"userId", "date", "type"},
                            new Object[]{chargingItem.getUserId(), DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE), "syserr-axj"});

                    if (scheduleCacheSystem.CBS.flushable.get(emailKey) == null) {
                        Map<String, Object> content = new HashMap<>();
                        content.put("info", "RESPONSE:" + response.getResponseString() + " signTmp:" + signTmp + " params:" + params);
                        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                                .to("xiaochao.wei@17zuoye.com;yuechen.wang@17zuoye.com")
                                .cc("zhilong.hu@17zuoye.com")
                                .subject(RuntimeMode.getCurrentStage() + "安信捷流量充值系统错误")
                                .content(content)
                                .send();
                        scheduleCacheSystem.CBS.flushable.set(emailKey, DateUtils.getCurrentToDayEndSecond(), "1");
                    }
                    // 坑比较多，将本条数据后移确保其他充值可以执行
                    flowPacketConvertServiceClient.getFlowPacketConvertService()
                            .updateProcessTime(chargingItem.getId())
                            .awaitUninterruptibly();
                    // 告诉奖品中心-订单 流量充值失败
                    rewardServiceClient.cancelFlowPacketOrder(Long.valueOf(chargingItem.getOrderNo()), "充值失败");
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Flow packet convert submit request failed.", e);
        }
        return true;
    }

    /**
     * 移动 测试产品代码7654320
     * 联通 测试产品代码7654321
     * 电信 测试产品代码7654322
     */
    private String getProcode(String phone, int size) {
        String tmp = phone.substring(0, 3);
        if (ArrayUtils.contains(TELECOM, tmp)) {
            return RuntimeMode.le(Mode.DEVELOPMENT) ? "7654322" : TELECOM_MAP.get(size);
        }
        if (ArrayUtils.contains(UNICOM, tmp)) {
            return RuntimeMode.le(Mode.DEVELOPMENT) ? "7654321" : UNICOM_MAP.get(size);
        }
        if (ArrayUtils.contains(MOBILE, tmp)) {
            return RuntimeMode.le(Mode.DEVELOPMENT) ? "7654320" : MOBILE_MAP.get(size);
        }
        return "";
    }

    public static void main(String[] args) {
        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
        String reqUrl = commonConfiguration.getFlowPacketReqUrl();
        String name = commonConfiguration.getFlowPacketName();
        String pass = commonConfiguration.getFlowPacketPass();
        String seqnum = RandomUtils.randomString(12);
        String mobiles = "18210251506";

//        移动 测试产品代码7654320
//        联通 测试产品代码7654321
//        电信 测试产品代码7654322
        String procode = "7654320";
        String itype = "5"; // 流量单品（默认值为5）
        String signTmp = pass + name + seqnum + mobiles + procode + itype + pass;
        String authpara = DigestUtils.sha256Hex(signTmp);
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("seqnum", seqnum);
        params.put("mobiles", mobiles);
        params.put("procode", procode);
        params.put("itype", itype);
        params.put("authpara", authpara);
//            logger.info("Send flow packet charging request to {} with params {}", reqUrl, params);
        String URL = UrlUtils.buildUrlQuery(reqUrl, params);
//            logger.info("Send flow packet charging request url {}", URL);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(URL).execute();
        System.out.println("URL : " + URL);

        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(URL)
                    .execute();
            System.out.println(response.getResponseString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

