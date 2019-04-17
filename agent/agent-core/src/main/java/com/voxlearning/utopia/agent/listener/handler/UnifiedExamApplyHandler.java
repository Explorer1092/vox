package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.TestPaperEntryStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.UnifiedExamApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.UnifiedExamTestPaperSourceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.UnifiedExamApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.UnifiedExamApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.UnifiedExamApplyServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.entity.XxBaseRegion;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.bouncycastle.util.Strings;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dell on 2017/4/20.
 */
@Named
public class UnifiedExamApplyHandler extends SpringContainerSupport {
    @Inject
    private UnifiedExamApplyServiceClient unifiedExamApplyServiceClient;
    @Inject
    private UnifiedExamApplyLoaderClient unifiedExamApplyClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private WorkFlowServiceClient workFlowServiceClient;
    @Inject
    private AgentNotifyService agentNotifyService;

    @Inject
    private AgentWorkflowService agentWorkflowService;
    @Inject
    private AgentTagService agentTagService;

    private String[] PAPER_VERSION = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    public void handle(Long workflowId, WorkFlowProcessResult processResult, String workflowStatus, Boolean hasFollowStatus){

        if(workflowId == null || processResult == null || hasFollowStatus == null || StringUtils.isBlank(workflowStatus)){
            return;
        }
        UnifiedExamApply apply = unifiedExamApplyClient.findByWorkflowId(workflowId);
        if(apply == null){
            return;
        }

        UnifiedExamApplyStatus unifiedExamStatus = apply.getUnifiedExamStatus();
        boolean isSoAgree = false;
        if(WorkFlowProcessResult.agree == processResult){
            if(Objects.equals(workflowStatus, "lv2")){ // 销运同意
                unifiedExamStatus = UnifiedExamApplyStatus.CR_PENDING;
                isSoAgree = true;
            }else if(Objects.equals(workflowStatus, "processed")){// 内容库同意
                unifiedExamStatus = UnifiedExamApplyStatus.CR_APPROVED;
                if(StringUtils.isNotBlank(apply.getTestPaperId())){
                    // 如果是采用旧的试卷时， 销运通过后系统会自动进行内容库审核并发布上线， 这种情形下上线状态为已上线
                    apply.setEntryStatus(TestPaperEntryStatus.ONLINE);
                    //上线提醒 同步发送申请人员 和相关的销运审核人员
                    String sendEmail = "xuyi.tian@17zuoye.com";
                    sendEmailUtils("统考申请发布通知", StringUtils.formatMessage("{} 申请的 {} 的 {} 试卷已经录入完成并已上线，学生可正常考试。", apply.getAccountName(), DateUtils.dateToString(apply.getUnifiedExamBeginTime()), apply.getUnifiedExamName()), sendEmail);
                    // 发送站内通知
                    sendOnlineMessage(apply.getUnifiedExamName(), Long.valueOf(apply.getAccount()));
                }else {
                    apply.setEntryStatus(TestPaperEntryStatus.DRAFT);// 人工审核通过后，录入状态调整为"录入中"
                }
            }
        }else if(WorkFlowProcessResult.reject == processResult){
            if(Objects.equals(workflowStatus, "reject_lv1")){ // 销运驳回
                unifiedExamStatus = UnifiedExamApplyStatus.SO_REJECTED;
            }else if(Objects.equals(workflowStatus, "reject_lv2")){ // 内容库驳回
                unifiedExamStatus = UnifiedExamApplyStatus.CR_REJECTED;
            }
        } else if (WorkFlowProcessResult.revoke == processResult) {
            unifiedExamStatus = UnifiedExamApplyStatus.REVOKE;
        }

        ApplyStatus applyStatus = null;
        if(!hasFollowStatus){ // 工作流已审批结束，没有后续状态
            if(WorkFlowProcessResult.agree == processResult){
                applyStatus = ApplyStatus.APPROVED;
            }else if(WorkFlowProcessResult.reject == processResult){
                applyStatus = ApplyStatus.REJECTED;
            }else if(WorkFlowProcessResult.revoke == processResult){
                applyStatus = ApplyStatus.REVOKED;
            }
            apply.setStatus(applyStatus);
        }
        apply.setUnifiedExamStatus(unifiedExamStatus);
        unifiedExamApplyServiceClient.update(apply);

        // 销运同意的情况下，调用内容库接口，创建统考试卷
        if(isSoAgree){
            MapMessage message = sendHttpRequestToTiku(apply);
            if(message.isSuccess()){
                // 判断内容库的状态是否是online状态
                boolean onlineFlag = SafeConverter.toBoolean(message.get("onlineFlag"));
                // 如果使用已有的试卷时，自动进行内容库审批
                if(onlineFlag && StringUtils.isNotBlank(apply.getTestPaperId())){
                    workFlowServiceClient.processWorkflow("agent", "cr", "内容库", workflowId, processResult, "内容库自动通过", null);
                }
            }
        }

        // 被驳回时， 发送邮件
        if(ApplyStatus.REJECTED == applyStatus){
            if(StringUtils.isNotBlank(workflowStatus)){
                //内容库驳回特殊处理直接写死在这地方
                if(Objects.equals(workflowStatus, "reject_lv2")){ //内容库驳回
                    String sendEmail = "xuyi.tian@17zuoye.com";
                    sendEmailUtils("统考申请驳回通知", StringUtils.formatMessage("抱歉，您申请的 {} 的 {} 试卷由于内容问题，没有通过审核，请及时处理。详情在天权中查看", DateUtils.dateToString(apply.getUnifiedExamBeginTime()), apply.getUnifiedExamName()), sendEmail);
                }
            }
            // 发送站内通知
            // 获取审批历史
            WorkFlowProcessHistory latestProcessHistory = agentWorkflowService.getLatestProcessHistory(workflowId);
            String rejectNote = "";
            String rejectName = "";
            if(latestProcessHistory != null){
                rejectNote = latestProcessHistory.getProcessNotes();
                rejectName = latestProcessHistory.getProcessorName();
            }
            String content =StringUtils.formatMessage("您提交的“{}”申请被驳回。\r\n" +
                    "驳回原因：{}【驳回人：{}】。",apply.getUnifiedExamName(),rejectNote,rejectName);
            List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
            agentNotifyService.sendNotifyWithTags(AgentNotifyType.UNIFIED_EXAM_APPLY.getType(), "统考测评", content ,
                    Collections.singleton(Long.valueOf(apply.getAccount())), null, null, null, tagIds);
        }
    }

    // 申请信息发送到内容库后，采用已有试卷的情况下，如果申请校验通过，则会直接设成已发布状态， 如果审核不通过，则会设成待审核状态
    private MapMessage sendHttpRequestToTiku(UnifiedExamApply apply){
        Map<Object, Object> marketingData = new HashMap<>();
        // 模考id
        marketingData.put("id", apply.getId());
        // 考试名称
        marketingData.put("name", apply.getUnifiedExamName());
        // 考试类型默认统考
        marketingData.put("examType", NewExamType.unify);
        // 考试区域级别
        marketingData.put("regionLevel", apply.getRegionLeve());
        // 地区信息
        XxBaseRegion region = new XxBaseRegion();
        region.setProvinceId(Integer.valueOf(apply.getProvinceCode()));
        region.setCityId(Integer.valueOf(apply.getCityCode()));
        if(ObjectUtils.equals(apply.getRegionLeve(),"country")){
            region.setRegionId(Integer.valueOf(apply.getRegionCode()));
        }
        marketingData.put("regions", Collections.singletonList(region));

        // 校级统考对应的学校ID
        if(ObjectUtils.equals(apply.getRegionLeve(),"school")){
            List<Long> schoolIds = new ArrayList<>();
            String[] str = apply.getUnifiedExamSchool().split(",");
            for(String id : str){
                schoolIds.add(Long.valueOf(id));
            }
            marketingData.put("schoolIds",schoolIds);
        }

        //学校级别
        int subject = Integer.valueOf(apply.getSubject());
        SchoolLevel schoolLevel = SchoolLevel.JUNIOR;
        if (subject < 201) { //参照题库数据结构 101 为 小学语文 102 小学数学 103 小学英语 201 为 初中语文 202 为初中数学 203 初中英语
            marketingData.put("schoolLevel", SchoolLevel.JUNIOR);
        } else {
            marketingData.put("schoolLevel", SchoolLevel.MIDDLE);
            schoolLevel = SchoolLevel.MIDDLE;
        }
        // 考试科目
        marketingData.put("subjectId", apply.getSubject());
        // 年级信息
        marketingData.put("clazzLevels", apply.fetchGradeLevel());
        // 开考后多长时间允许提交
        marketingData.put("submitAfterMinutes", apply.getMinSubmittedTestPaper());
        // 结果发布时间
        marketingData.put("resultIssueAt", apply.getAchievementReleaseTime());
        // 市场上报人员姓名
        marketingData.put("agentName", apply.getAccountName());
        // 市场上报人员id
        marketingData.put("agentId", Long.valueOf(apply.getAccount()));
        // 口语题可答题次数
        if (apply.getOralLanguageFrequency() != null) {//口语可答题次数
            marketingData.put("oralRepeatCount", apply.getOralLanguageFrequency());
        }
        // 考试结束时间
        marketingData.put("examStopAt", apply.getUnifiedExamEndTime());
        // 考试时长
        marketingData.put("durationMinutes", apply.getMaxSubmittedTestPaper());
        // 老师批改截止时间
        marketingData.put("correctStopAt", apply.getCorrectingTestPaper());
        // 考试开始时间
        marketingData.put("examStartAt", apply.getUnifiedExamBeginTime());

        // 考试总分
        marketingData.put("score", apply.getScore());//总分

        // 关联教材
        if (apply.getTestPaperSourceType() == UnifiedExamTestPaperSourceType.NEWLYADDED) {
            marketingData.put("bookCatalogId", apply.getBookCatalogId());//新试卷需要关联考试教材
        }
        // 多试卷参数
        marketingData.put("papers", createPapersJsonString(apply.fetchPaperId(), apply.fetchPaperAddress()));
        // 等第制分制信息
        marketingData.put("ranks", apply.getRanks() == null? "": apply.getRanks());
        // 内容类型
        marketingData.put("contentTypes", apply.fetchTestPaperType().stream().map(p -> Strings.toLowerCase(p.name())).collect(Collectors.toList()));
        // 试卷分配方式
        marketingData.put("distribution", apply.getDistribution());
        // 考试方式
        marketingData.put("testScene", apply.getTestScene());
        // 分数制
        marketingData.put("gradeType", apply.getGradeType());
        String status = "NEW";
        if (StringUtils.isNotBlank(apply.getTestPaperId())) { //当使用旧的试卷时由天权流程结束
            status = "ONLINE";
        }
        marketingData.put("status", status);
        String marketUrl = getMarketingUrl(schoolLevel) + "/service/applyExam";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(marketUrl).addParameter(marketingData).execute();
        Boolean sendToTikuSuccess = true;
        Boolean onlineFlag = false;
        if (response.getStatusCode() == 200){
            Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
            if (null != resultMap){
                Object result = resultMap.get("success");
                Boolean bResult = (Boolean) result;
                if (!bResult ){
                    sendToTikuSuccess = false;
                }else {
                    // 获取内容库的状态  "NEW":待审核   "REJECT":驳回   "DRAFT":录入中  "ONLINE":已发布   "OFFLINE":下线
                    String tikuApplyStatus = (String)resultMap.get("status");
                    if(StringUtils.equals(tikuApplyStatus, "ONLINE")){
                        onlineFlag = true;
                    }
                }
            }else {
                sendToTikuSuccess = false;
            }
        }else {
            sendToTikuSuccess = false;
        }
        if (!sendToTikuSuccess){
            List<String> messages = new ArrayList<>();
            messages.add(StringUtils.formatMessage("Agent send UnifiedExamApply HttpRequestToTiku，marketUrl:{},id={},agentId={},返回结果如下：",marketUrl,apply.getId(),apply.getAccount()));
            messages.add(StringUtils.formatMessage("statusCode..................{}", response.getStatusCode()));
            messages.add(StringUtils.formatMessage("hasException............{}", response.hasHttpClientException()));
            messages.add(StringUtils.formatMessage("exceptionMessage........{}", response.getHttpClientExceptionMessage()));
            messages.add(StringUtils.formatMessage("responseContentType.....{}", response.getContentType()));
            messages.add(StringUtils.formatMessage("responseCharset.........{}", response.getResponseCharset()));
            messages.add(StringUtils.formatMessage("responseString.........{}", response.getResponseString()));
            String emailContent = StringUtils.join(messages, "\n");
            sendEmailUtils(RuntimeMode.current().getStageMode() +
                    "统考申请发布到题库出现错误",emailContent, AgentConstants.TEAM_EMAIL);

        }
        MapMessage message = new MapMessage();
        message.setSuccess(sendToTikuSuccess);
        message.add("onlineFlag", onlineFlag);
        return message;
    }

    private String createPapersJsonString(List<String> paperIds, List<String> paperAddress) {
        List<Map<String, String>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paperIds)) {
            for (String paperId : paperIds) {
                Map<String, String> info = new HashMap<>();
                info.put("paperId", paperId);
                result.add(info);
            }
            //Integer paperVirsionIndex = 0;
            for (String address : paperAddress) {
                Map<String, String> info = new HashMap<>();
                info.put("fileUrl", address);
                //info.put("fileName",address);
                result.add(info);
            }
        }
        return JsonUtils.toJson(result);
    }

    /**
     * 获取调用接口的 地址
     * @return
     */
    private String getMarketingUrl(SchoolLevel schoolLevel) {
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                if(schoolLevel == SchoolLevel.JUNIOR){
                    return "http://tiku.17zuoye.net";
                }else {
                    return "http://zytiku.17zuoye.com";
                }
            case STAGING:
                if(schoolLevel == SchoolLevel.JUNIOR){
                    return "http://tiku.17zuoye.net";
                }else {
                    return "http://zytiku.17zuoye.com";
                }
            case TEST:
                if(schoolLevel == SchoolLevel.JUNIOR){
                    return "http://10.0.1.50:8095";
                }else {
                    return "http://zytiku.test.17zuoye.net";
                }
            case DEVELOPMENT:
                return "http://10.0.1.50:8095";
            default:
                return "http://10.0.1.50:8095";
        }
    }

    /**
     * 发送邮件提醒
     * 采用这种方式替换一下
     */
    private void sendEmailUtils(String title, String content, String emailAddress){
        emailServiceClient.createPlainEmail()
                .body(content)
                .subject(title)
                .to(emailAddress)
                .send();
    }


    public void sendOnlineMessage(String unifiedExamName, Long receiverId){
        agentNotifyService.sendNotify(AgentNotifyType.UNIFIED_EXAM_APPLY.getType(), "统考测评", "您的统考申请已上线\r\n名称：" + unifiedExamName,
                Collections.singleton(receiverId), null);
    }

    public static void main(String[] args) {
        Map<Object, Object> marketingData = new HashMap<>();
        String marketUrl = "http://10.0.1.108:5234/service/applyExam";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(marketUrl).addParameter(marketingData).execute();
        if (response.getStatusCode() == 200){
            Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
            System.out.println(resultMap);
        }
    }
}

