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

import com.alibaba.fastjson.JSONObject;
import com.couchbase.client.deps.io.netty.util.internal.StringUtil;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cipher.DesUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.constants.AlterationCcProcessState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherAlterationServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import lombok.Cleanup;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.entity.StringEntity;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.calculateDateDay;

/**
 * 生成换班自动外呼任务列表
 * 外包商换成中通，暑假重新启用
 *
 * @author Yuechen Wang
 * @author haitian.gan
 * @version 0.1
 * @since 2017-07-17
 */
@Named
@ScheduledJobDefinition(
        jobName = "生成换班自动外呼任务列表",
        jobDescription = "每天早上10点半生成需要自动换班外呼的任务列表",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 50 9 * * ?"
)
@ProgressTotalWork(100)
public class AutoSendClazzAlterationTaskJob extends ScheduledJobWithJournalSupport {

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private VendorAppsServiceClient vendorAppsServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    private static final String DIAL_OUT_PROVIDER_HOST = "dms.icsoc.net";// 外呼提供商地址
    private static final String OUTBOUND_CALL_SUFFIX = "OUTCALL_";
    private static final int PARAM_CALL_NUM = 2;// 重呼次数
    private static final int PARAM_CALL_INTERVAL = 20;// 呼叫时长

    private static final String USER_SERVICE_URL = "http://cs.17zuoye.net:8090/PDWebService/ServiceMain.asmx";
    private UtopiaSql utopiaSql;

    private static final List<String> TeacherFilterType = Arrays.asList(
            CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName(),
            CrmTeacherFakeValidationType.AUTO_VALIDATION_CAC.getName()
    );

    // 测试专用ID
//    private static final List<Long> RecordIdListForTest = Arrays.asList();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    /**
     * 筛选记录的要求
     * 1. 请求创建天数：3<=天数<=10
     * 2. ALTERATION_STATE=PENDING 且 CC_PROCESS_STATE=UNPROCESSED状态的 REPLACE 和 TRANSFER 请求
     * 3. 排除applicant为 假（人工判假）老师 的请求 以及 respondent
     * 4. 排除applicant为 “疑” 且来源为CRM换班自动外呼的老师
     * 5. 排除applicant为 异常挂断数 >=3 的老师 的请求
     */
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // 根据SecretKey去获得加密用的密钥
        String appKey = "UserService";
        VendorApps vendorApp = vendorAppsServiceClient.getVendorAppsBuffer().loadByAk(appKey);
        if (vendorApp == null || StringUtils.isBlank(vendorApp.getSecretKey())) {
            throw new UtopiaRuntimeException("What?? I can't believe this! Please check [UserService]'s secretKey RIGHT NOW!");
        }

        // 准备查询数据
        final String querySql = "SELECT "
                + " ID, ALTERATION_TYPE, APPLICANT_ID, RESPONDENT_ID, CLAZZ_ID, UPDATE_DATETIME "
                + " FROM VOX_CLAZZ_TEACHER_ALTERATION "
                + " WHERE DISABLED=0 AND UPDATE_DATETIME BETWEEN :startDate AND :endDate "
                + " AND ALTERATION_STATE='PENDING' AND CC_PROCESS_STATE='UNPROCESSED' "
                + " AND ALTERATION_TYPE IN ('TRANSFER','REPLACE','LINK') "
                + " ORDER BY UPDATE_DATETIME DESC LIMIT 600 ";

        logger.info("自动生成换班自动外呼任务列表开始...");

        Date startDate = calculateDateDay(new Date(), -10);
        Date endDate = calculateDateDay(new Date(), -2);

        // 已过滤条件1 & 条件2 & 条件5
        List<Map<String, Object>> alterations = utopiaSql.withSql(querySql)
                .useParams(MiscUtils.m("startDate", startDate, "endDate", endDate))
                .queryAll();
//        alterations = alterations.stream().filter(this::onlyForTest).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(alterations)) {
            logger.info("近3~10天没有需要处理的换班记录...");
            progressMonitor.done();
            return;
        }

        // 过滤掉条件3 和 条件4
        Set<Long> teacherId = new HashSet<>();
        Set<Long> applicantIds = alterations.stream()
                .map(r -> SafeConverter.toLong(r.get("APPLICANT_ID")))
                .collect(Collectors.toSet());

        Set<Long> respondentIds = alterations.stream()
                .map(r -> SafeConverter.toLong(r.get("RESPONDENT_ID")))
                .collect(Collectors.toSet());

        teacherId.addAll(applicantIds);
        teacherId.addAll(respondentIds);

        // 过滤假老师
        Map<Long, TeacherExtAttribute> teacherExtAttrs = teacherLoaderClient.loadTeacherExtAttributes(teacherId);
        Set<Long> fakeTeacherIds = teacherExtAttrs.values().stream()
                .filter(t -> t.isFakeTeacher() && StringUtils.isNoneBlank(t.getValidationType()) && TeacherFilterType.contains(t.getValidationType()))
                .map(TeacherExtAttribute::getId)
                .collect(Collectors.toSet());

        alterations = alterations.stream()
                .filter(r -> !fakeTeacherIds.contains(SafeConverter.toLong(r.get("APPLICANT_ID")))
                        && !fakeTeacherIds.contains(SafeConverter.toLong(r.get("RESPONDENT_ID"))))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(alterations)) {
            logger.info("近3~10天没有满足条件的换班记录...");
            progressMonitor.done();
            return;
        }

        progressMonitor.worked(10);
        logger.info("一共有{}条换班记录需要发送", alterations.size());

        // 处理拼出来的SOAPMessage信息
        // 替换成中通外呼
        HttpClient client = new HttpClient(new SimpleHttpConnectionManager(true));
        client.getHostConfiguration().setHost(DIAL_OUT_PROVIDER_HOST, 80, "http");

        // 改用Webservice的方式去调用了....
        Map<String, String> processInfo = new HashMap<>();
        int failedCnt = 0;
        ISimpleProgressMonitor monitor = progressMonitor.subTask(85, alterations.size());

        // 根据被申请的老师ID，对请求进行分组。
        Map<Object, List<Map<String, Object>>> groupedAlteration = alterations.stream()
                .collect(Collectors.groupingBy(ar -> ar.get("RESPONDENT_ID")));

        Map<Long, List<Map<String, Object>>> altersGroupByApplicant = alterations.stream()
                .collect(Collectors.groupingBy(ar -> SafeConverter.toLong(ar.get("APPLICANT_ID"))));

        for (Map.Entry<Object, List<Map<String, Object>>> entry : groupedAlteration.entrySet()) {

            List<Map<String, Object>> alters = entry.getValue();
            Map<String, Object> alteration = alters.get(0);

            // 查询完毕，处理数据格式
            Long pickRecordId = SafeConverter.toLong(alteration.get("ID"));
            // 可能是一个老师对应多个请求
            String multiRecordId = alters.stream()
                    .map(al -> SafeConverter.toString(al.get("ID")))
                    .reduce((acc, item) -> acc + "," + item)
                    .orElse(null);

            String alterationType = SafeConverter.toString(alteration.get("ALTERATION_TYPE"));
            // 如果是多个请求的话，合并成第三种模板
            if (alters.size() > 1) {
                alterationType = "MANUAL";
            }

            // 获得外呼项目的配置
            String projectConfigStr;
            try {
                projectConfigStr = crmConfigService.$loadCommonConfigValue(
                        ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(),
                        OUTBOUND_CALL_SUFFIX + alterationType);
            } catch (Exception e) {
                projectConfigStr = "";
            }

            if (StringUtil.isNullOrEmpty(projectConfigStr)) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("项目配置({})不存在", alterationType));
                continue;
            }

            Map<String, Object> projectConfig = JsonUtils.fromJson(projectConfigStr);
            if (projectConfig == null) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("项目配置({})错误", alterationType));
                continue;
            }

            // 再判断一次该条记录有没有处理过
            ClazzTeacherAlteration teacherAlteration = teacherLoaderClient.loadClazzTeacherAlteration(pickRecordId);
            if (teacherAlteration == null) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("{}换班请求不存在!", multiRecordId));
                continue;
            } else if (teacherAlteration.getCcProcessState() != AlterationCcProcessState.UNPROCESSED) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("{}请求已处理，跳过!", multiRecordId));
                continue;
            }

            // 项目的基础配置信息
            int vccId = SafeConverter.toInt(projectConfig.get("vccId"));
            int proId = SafeConverter.toInt(projectConfig.get("proId"));
            String token = SafeConverter.toString(projectConfig.get("token"));

            Long applicantId = SafeConverter.toLong(alteration.get("APPLICANT_ID"));
            TeacherDetail applicant = teacherLoaderClient.loadTeacherDetail(applicantId);
            if (applicant == null) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("申请老师({})不存在", applicantId));
                failedCnt++;
                continue;
            }

            if (StringUtils.isBlank(applicant.getProfile().getRealname())) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("无法获取申请老师({})姓名", applicantId));
                failedCnt++;
                continue;
            }

            Long respondentId = SafeConverter.toLong(alteration.get("RESPONDENT_ID"));
            TeacherDetail respondent = teacherLoaderClient.loadTeacherDetail(respondentId);
            if (respondent == null) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("处理老师({})不存在", respondentId));
                failedCnt++;
                continue;
            }
            if (StringUtils.isBlank(respondent.getProfile().getRealname())) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("无法获取处理老师({})姓名", respondentId));
                failedCnt++;
                continue;
            }

            Long clazzId = SafeConverter.toLong(alteration.get("CLAZZ_ID"));
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzIncludeDisabled(clazzId);
            if (clazz == null) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("班级({})不存在", clazzId));
                failedCnt++;
                continue;
            }
            if (StringUtils.isBlank(clazz.formalizeClazzName())) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("无法获取班级({})名称", clazzId));
                failedCnt++;
                continue;
            }

            String mobile = sensitiveUserDataServiceClient.showUserMobile(respondentId, "换班自动外呼", SafeConverter.toString(respondentId));
            if (StringUtils.isBlank(mobile)) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("老师({})没有绑定手机号", respondentId));
                failedCnt++;
                continue;
            }

            String applyMobile = sensitiveUserDataServiceClient.showUserMobile(applicantId, "换班自动外呼", SafeConverter.toString(applicantId));
            if (StringUtils.isEmpty(applyMobile)) {
                processInfo.put(multiRecordId, StringUtils.formatMessage("老师({})没有绑定手机号", applicantId));
                failedCnt++;
                continue;
            }

            // 判断LINK的条件
            if (alterationType.equals(ClazzTeacherAlterationType.LINK.name())) {

                // 获得这个申请人的其它请求，如果有和LINK的被申请人同班的，则忽略处理
                // 修改成，看application除LINK之外，有没有其它请求，有的话就跳过
                boolean existOtherRequest = altersGroupByApplicant.get(applicantId)
                        .stream()
                        .filter(a -> !Objects.equals(pickRecordId, SafeConverter.toLong(a.get("ID"))))
                        .filter(a -> Objects.equals(clazzId, SafeConverter.toLong(a.get("CLAZZ_ID"))))
                        .anyMatch(a -> !Objects.equals(MapUtils.getString(a, "ALTERATION_TYPE"), ClazzTeacherAlterationType.LINK.name()));

                if (existOtherRequest)
                    continue;
            }

            PostMethod post = new PostMethod("/api/v2/data");

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("callInterval", PARAM_CALL_INTERVAL);
            map.put("callNum", PARAM_CALL_NUM);
            map.put("jobId", multiRecordId);
            map.put("phoneNum", mobile);

            Map<String, String> tplInfo = new HashMap<>();
            // 和原本的含义对调了
            tplInfo.put("takeTeacherName", applicant.getProfile().getRealname());

            // 获得科目的名字
            String subjectName;
            Subject subject = applicant.getSubject();
            if (subject != null)
                subjectName = subject.getValue();
            else
                subjectName = "";

            tplInfo.put("takeTeacherSubject", subjectName);
            tplInfo.put("className", clazz.formalizeClazzName());
            tplInfo.put("classNameNew", clazz.formalizeClazzName());

            map.put("templateInfo", tplInfo);

            Map<String, Object> data = new HashMap<>();
            data.put("vccId", vccId);
            data.put("proId", proId);

            String mapStr = JSONObject.toJSONString(map);
            data.put("data", map);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();

            String str = mapStr + token;
            String sign = DigestUtils.sha1Hex(str);

            data.put("sign", sign);
            String body = JSONObject.toJSONString(data);

            post.setRequestHeader("Content-Type", "application/json");
            StringRequestEntity requestEntity = new StringRequestEntity(
                    body, "application/json", "UTF-8");

            post.setRequestEntity(requestEntity);
            client.executeMethod(post);

            String response = new String(post.getResponseBodyAsString().getBytes("utf-8"));
            MapMessage returnMsg = parse(response);

            if (returnMsg.isSuccess()) {
                // 可能是多请求，全部置上状态
                for (Map<String, Object> alter : alters) {
                    Long subAlterId = SafeConverter.toLong(alter.get("ID"));
                    // 发送完毕，更新请求状态
                    MapMessage processMsg = teacherAlterationServiceClient.updateAlterationProcessState(subAlterId, AlterationCcProcessState.PROCESSING, OperationSourceType.cc);
                    if (!processMsg.isSuccess()) failedCnt++;
                    processInfo.put(multiRecordId, processMsg.isSuccess() ? "请求成功，等待外呼系统处理" : "请求成功，但状态更新失败");
                }
            } else {
                failedCnt++;
                processInfo.put(multiRecordId, "请求失败:" + returnMsg.getInfo());
            }

            post.releaseConnection();
            monitor.worked(1);
        }
        Map<String, Object> content = new HashMap<>();
        content.put("allCount", alterations.size());
        content.put("failedCnt", failedCnt);
        content.put("processInfo", processInfo);
        content.put("env", RuntimeMode.current().name());
        if (RuntimeMode.isProduction()) {
            //发邮件
            emailServiceClient.createTemplateEmail(EmailTemplate.autoteacherclazzalteration)
                    .to("yaxiang.zhao@17zuoye.com;xiaoning.liu@17zuoye.com")
                    .cc("zhilong.hu@17zuoye.com")
                    .subject("自动生成换班自动外呼任务执行结果")
                    .content(content).send();
        } else {
            emailServiceClient.createTemplateEmail(EmailTemplate.autoteacherclazzalteration)
                    .to("zhilong.hu@17zuoye.com")
                    .subject("自动生成换班自动外呼任务执行结果")
                    .content(content).send();
        }
        logger.info("自动生成换班自动外呼任务列表结束，成功:{}条，失败:{}条...", alterations.size() - failedCnt, failedCnt);

        progressMonitor.done();
    }

    private void addElement(StringBuilder builder, String node, String value) {
        builder.append("<").append(node).append(">")
                .append(value)
                .append("</").append(node).append(">");
    }

    private MapMessage parse(String response) throws Exception {
        MapMessage returnMsg = new MapMessage();
        if (StringUtils.isBlank(response)) {
            return MapMessage.errorMessage("没有返回信息");
        }

        Map<String, Object> result = JsonUtils.fromJson(response);
        int resultCode = SafeConverter.toInt(result.get("code"));

        if (resultCode == 200)
            returnMsg.setSuccess(true);
        else {
            returnMsg.setSuccess(false);
            returnMsg.setInfo(SafeConverter.toString(result.get("msg")));
        }

        return returnMsg;
    }

//    private boolean onlyForTest(Map<String, Object> record) {
//        return record.containsKey("ID") && RecordIdListForTest.contains(SafeConverter.toLong(record.get("ID")));
//    }

    public static void main(String[] args) throws Exception {
        // 处理拼出来的SOAPMessage信息
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();
        SOAPMessage request = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage();
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();
        QName methodName = new QName("http://tempuri.org/", "CreateTaskInfo");  // 用于创建Element
        SOAPBodyElement element = body.addBodyElement(methodName);
        // 拼参数列表
        String params = "<requestinfos><requestinfo>"
                + "<requestid>13</requestid>" // 外呼请求ID
                + "<requesttype>REPLACE</requesttype>" // 换班类型 REPLACE/TRANSFER
                + "<teachername>甲老师</teachername>" // 请求老师姓名
                + "<teachername2>乙老师</teachername2>" // 外呼老师姓名
                + "<phone>18210251506</phone>" // 外呼老师号码
                + "<requestclass>3年级2班</requestclass>"  // 对象班级
                + "</requestinfo></requestinfos>";
        element.addChildElement("paraXml").setValue(DesUtils.encryptHexString("AqkN9D9R", params));

        // 创建SOAPMessage
        Source source = request.getSOAPPart().getContent();
        @Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bos);
        tf.transform(source, result);
        String requestString = new String(bos.toByteArray());
        // 数据格式处理完毕，准备发送
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(USER_SERVICE_URL)
                .contentType("text/xml; charset=utf-8")
                .entity(new StringEntity(requestString, Charset.defaultCharset()))
                .execute();

        System.out.println("ALPS http response debug information:");
        System.out.println(StringUtils.formatMessage("hasException............{}", response.hasHttpClientException()));
        System.out.println(StringUtils.formatMessage("exceptionMessage........{}", response.getHttpClientExceptionMessage()));
        System.out.println(StringUtils.formatMessage("statusCode..............{}", response.getStatusCode()));
        System.out.println(StringUtils.formatMessage("responseString...{}", response.getResponseString()));
        System.out.println(StringUtils.formatMessage("responseContentType.....{}", response.getContentType()));
    }

}
