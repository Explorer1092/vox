package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.audit.AuditWorkFlowController;
import com.voxlearning.utopia.admin.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.admin.viewdata.FeedbackListView;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCategory;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmProductFeedbackRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.mappers.AuthenticatedMobile;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品反馈
 *
 * @author song.wang
 * @date 2017/2/23
 */
@Controller
@RequestMapping("/crm/productfeedback")
public class CrmProductFeedbackController extends ProductFeedbackController {

    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    private static final String FEEDBACK_EXCEL_TEMP = "/config/templates/product_feedback_result_templates.xlsx";
    private static final String COMMAND = "notice_product_feedback";


    /**
     * 取反馈记录的详情
     *
     * @return
     */
    @RequestMapping(value = "load_feedback_page.vpage", method = RequestMethod.POST)
    @ResponseBody
    protected MapMessage loadFeedbackPage() {
        Long feedbackId = getRequestLong("feedbackId");
        AgentProductFeedback feedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);

        if (feedback == null) {
            return MapMessage.errorMessage("未找到该条反馈记录");
        }
        MapMessage msg = MapMessage.successMessage();
       /* Teacher teacher = teacherLoaderClient.loadTeacher(feedback.getTeacherId());
        if (teacher == null) {
            return MapMessage.errorMessage("反馈老师为空或不存在!");
        }*/

        if(feedback.getRelationCode() != null){
            List<AgentProductFeedback> relationList = agentProductFeedbackLoadClient.findByRelationCode(feedback.getRelationCode());
            if(CollectionUtils.isNotEmpty(relationList)){
                msg.add("relationList", relationList.stream().filter(p -> !Objects.equals(p.getId(), feedback.getId())).collect(Collectors.toList()));
            }
        }
        msg.add("canSelectedPm", Objects.equals(feedback.getFeedbackStatus(), AgentProductFeedbackStatus.PM_PENDING));
        msg.add("canSelectedOnlineDate", Objects.equals(feedback.getFeedbackStatus(), AgentProductFeedbackStatus.PM_APPROVED));
        msg.add("feedback", feedback);
        msg.add("subjectStr", feedback.getTeacherSubject() == null ? "" : feedback.getTeacherSubject().getDesc());
        msg.add("typeStr", feedback.getFeedbackType().getDesc());
        String phone = sensitiveUserDataServiceClient.showUserMobile(feedback.getTeacherId(), "admin" + getCurrentAdminUser().getRealName() + "产品反馈查看老师电话", SafeConverter.toString(feedback.getTeacherId()));
        msg.add("mobile", phone);
        msg.add("subject", createAgentFeedbackSubject());
        msg.add("type", createAgentFeedbackType());
        msg.add("pmList", AuditWorkFlowController.PM_ACCOUNT_LIST);
        msg.add("category1", feedback.getFirstCategory());
        msg.add("category2", feedback.getSecondCategory());
        msg.add("category3", feedback.getThirdCategory());

        return msg;
    }

    @RequestMapping(value = "update_feedback_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFeedbackInfo() {
        ProductFeedbackListCondition condition = getFeedbackOfRequest();
        AgentProductFeedback feedback = agentProductFeedbackLoadClient.loadByFeedbackId(condition.getId());
        if (feedback == null) {
            return MapMessage.errorMessage("未找到该条反馈记录！");
        }
        //fixme song.wang 判断下是否有操作
        MapMessage msg = crmProductFeedbackService.updateFeedbackInfo(condition, feedback);
        if (!msg.isSuccess()) {
            return msg;
        }
        // 记录更新记录
        AuthCurrentAdminUser user = getCurrentAdminUser();
        CrmProductFeedbackRecord record = new CrmProductFeedbackRecord();
        record.setOperatorName(user.getRealName());
        record.setOperatorUsername(user.getAdminUserName());
        record.setOperationContent(StringUtils.formatMessage("更新了产品反馈Id:{}", condition.getId()));
        record.setOperationObject(feedback);
        crmProductFeedbackServiceClient.insertProductFeedbackRecord(record);
        return msg;
    }

    /**
     * 确认上线功能
     *
     * @return
     */
    @RequestMapping(value = "sure_online.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sureOnline() {
        Long feedbackId = getRequestLong("feedbackId");
        AgentProductFeedback feedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);
        if (feedback == null) {
            return MapMessage.errorMessage("未找到该条反馈记录！");
        }
        Boolean noticeFlag = getRequestBool("noticeFlag");
        String noticeContent = getRequestString("noticeContent");
        if (noticeFlag && StringUtils.isBlank(noticeContent)) {
            return MapMessage.errorMessage("通知老师的内容不能为空");
        }
        feedback.setNoticeFlag(noticeFlag);
        feedback.setNoticeContent(noticeContent);
        feedback.setOnlineFlag(true);
        feedback.setOnlineDate(new Date());
        //fixme song.wang 判断下是否有操作
        agentProductFeedbackServiceClient.replaceAgentProductFeedback(feedback);
        // 通知上线邮件
        informEmail(feedback);
        informAgent(feedback.getWorkflowId());
        // push 给老师上线的确认信息
        if (SafeConverter.toBoolean(feedback.getOnlineFlag())) {
            sendAppJpushMessageByIds(feedbackId, feedback.getContent(), feedback.getTeacherSubject());
        }
        AuthCurrentAdminUser user = getCurrentAdminUser();
        CrmProductFeedbackRecord record = new CrmProductFeedbackRecord();
        record.setOperatorName(user.getRealName());
        record.setOperatorUsername(user.getAdminUserName());
        record.setOperationContent(StringUtils.formatMessage("反馈Id：{}被确认上线", feedback.getId()));
        crmProductFeedbackServiceClient.insertProductFeedbackRecord(record);
        return MapMessage.successMessage();
    }

    private void informAgent(Long workflowId) {
        Map<String, Object> map = new HashMap<>();
        map.put("recordId", workflowId);
        map.put("command", COMMAND);
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(map));
        agentCommandQueueProducer.getProducer().produce(message);
    }

    private void informEmail(AgentProductFeedback feedback) {
        String phone = sensitiveUserDataServiceClient.showUserMobile(feedback.getTeacherId(), "admin" + getCurrentAdminUser().getRealName() + "产品反馈查看老师电话", SafeConverter.toString(feedback.getTeacherId()));
        if (feedback.getUserPlatform() == SystemPlatformType.ADMIN) {
            sendOnlineEmail(StringUtils.formatMessage("您好，您提交的反馈“反馈编号{},反馈文本:{}”已经采纳并且上线，烦请联系用户“{}（{}）”告知已上线，非常感谢您的宝贵提议！祝您工作顺利！",
                    feedback.getId(), feedback.getContent(), feedback.getTeacherName(), phone), StringUtils.formatMessage("{}@17zuoye.com", feedback.getAccount()));
        }
        if (feedback.getUserPlatform() == SystemPlatformType.AGENT) {
            AgentUser user = agentUserLoaderClient.load(SafeConverter.toLong(feedback.getAccount()));
            if (user != null && StringUtils.isNoneBlank(user.getTel())) {
                smsServiceClient.createSmsMessage(user.getTel()).content(
                        StringUtils.formatMessage("您好，您提交的反馈“反馈编号：{}，反馈文本：{}”已经采纳并且上线，欢迎体验，非常感谢您的宝贵提议！祝您工作顺利！",
                                feedback.getId(), feedback.getContent())
                ).send();
            }
        }
    }


    /**
     * 全部反馈页
     *
     * @param model 反馈的内容
     * @return 全部反馈页
     */
    @RequestMapping(value = "feedback_list.vpage", method = RequestMethod.GET)
    public String feedbackList(Model model) {
        model.addAttribute("subject", AgentProductFeedbackSubject.values());
        model.addAttribute("type", AgentProductFeedbackType.values());
        model.addAttribute("status", AgentProductFeedbackStatus.values());
        model.addAttribute("statDate", DateUtils.getFirstDayOfMonth(new Date()));
        model.addAttribute("endDate", new Date());
        model.addAttribute("pmList", AuditWorkFlowController.PM_ACCOUNT_LIST);
        model.addAttribute("pageSize", PAGE_SIZE);
        return "crm/feedback/feedbacklist";
    }

    /**
     * 查询反馈内容
     *
     * @return 反馈列表的内容
     */
    @RequestMapping(value = "feedback_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFeedbackList() {
        ProductFeedbackListCondition condition = getFeedbackOfRequest();
        Integer page = getRequestInt("page") == 0 ? 1 : getRequestInt("page");
        return crmProductFeedbackService.loadFeedbackListByCondition(condition, getCurrentAdminUser().getRealName(), page, PAGE_SIZE);
    }

    /**
     * 导出反馈列表
     */
    @RequestMapping(value = "export_feedback_list.vpage", method = RequestMethod.POST)
    public void exportFeedbackList() {
        ProductFeedbackListCondition condition = getFeedbackOfRequest();
        try {
            String filename = "导出反馈信息" + DateUtils.dateToString(new Date()) + ".xlsx";
            List<FeedbackListView> result = crmProductFeedbackService.loadFeedbackListByCondition(condition, getCurrentAdminUser().getRealName());
            XSSFWorkbook workbook = convertToFeedbackWorkbook(result);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            assert workbook != null;
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (Exception ex) {
            logger.error("export feedback failed", ex);
        }
    }

    private XSSFWorkbook convertToFeedbackWorkbook(List<FeedbackListView> feedbackListViews) {
        Resource resource = new ClassPathResource(FEEDBACK_EXCEL_TEMP);
        if (!resource.exists()) {
            logger.error("template is not exists");
            return null;
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            int initLine = 1;
            for (FeedbackListView view : feedbackListViews) {
                XSSFRow row = sheet.createRow(initLine++);
                createCell(row, 0, cellStyle, format(view.getId()));
                createCell(row, 1, cellStyle, format(view.getFeedbackDate()));
                String relationIds = "";
                if(CollectionUtils.isNotEmpty(view.getRelationIds())){
                    relationIds = StringUtils.join(view.getRelationIds(), ",");
                }
                createCell(row, 2, cellStyle, format(relationIds));
                createCell(row, 3, cellStyle, format(view.getStatus()));
                createCell(row, 4, cellStyle, format(view.getUserPlatform()));
                createCell(row, 5, cellStyle, format(view.getFeedbackPeople()));
                createCell(row, 6, cellStyle, format(view.getTeacherName()));
                createCell(row, 7, cellStyle, format(view.getTeacherId()));
                createCell(row, 8, cellStyle, format(view.getSubject()));
                createCell(row, 9, cellStyle, format(view.getType()));
                createCell(row, 10, cellStyle, format(view.getFirstCategory()));
                createCell(row, 11, cellStyle, format(view.getSecondCategory()));
                createCell(row, 12, cellStyle, format(view.getThirdCategory()));

                createCell(row, 13, cellStyle, format(view.getContent()));
                createCell(row, 14, cellStyle, format(view.getSoOpinion()));
                createCell(row, 15, cellStyle, format(view.getPmData()));
                createCell(row, 16, cellStyle, format(view.getPmOpinion()));
                createCell(row, 17, cellStyle, format(view.getOnlineData()));
                createCell(row, 18, cellStyle, format(SafeConverter.toBoolean(view.getOnline()) ? "已上线" : "未上线"));
                createCell(row, 19, cellStyle, format(view.getRegionName()));
                createCell(row, 20, cellStyle, format(view.getCityName()));
                createCell(row, 21, cellStyle, format(view.getBookName()));
                createCell(row, 22, cellStyle, format(view.getBookGrade()));
                createCell(row, 23, cellStyle, format(view.getBookUnit()));
                createCell(row, 24, cellStyle, format(view.getBookCoveredArea()));
                createCell(row, 25, cellStyle, format(view.getBookCoveredStudentCount()));
            }
            return workbook;
        } catch (IOException e) {
            logger.error("convert to XSSFWorkbook is failed:{}", e);
        }
        return null;
    }

    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    /**
     * 设置字符类型文字
     */
    private String format(Object value) {
        return value == null ? "" : String.valueOf(value);
    }


    @RequestMapping(value = "load_category.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadCategory() {
        Integer typeId = getRequestInt("typeId");
        MapMessage msg = MapMessage.successMessage();
        msg.add("category", JsonUtils.toJson(AgentProductFeedbackCategory.fetchCategory(typeId)));
        return msg;
    }

    @RequestMapping(value = "update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFeedback() {

        Long feedbackId = getRequestLong("feedbackId");

        Integer teacherSubject = getRequestInt("teacherSubject"); // 学科
        Integer feedbackType = getRequestInt("feedbackType"); // 反馈类型
        String firstCategory = getRequestString("firstCategory"); // 三级分类
        String secondCategory = getRequestString("secondCategory"); //
        String thirdCategory = getRequestString("thirdCategory"); //
        String pmAccount = getRequestString("pmAccount"); // 指定的PM账号
        String pmAccountName = getRequestString("pmAccountName"); // 指定的PM姓名

        Boolean sendFlag = getRequestBool("sendFlag"); //是否给老师发送感谢消息
        String noticeContent = getRequestString("noticeContent"); // 发给老师的感谢内容

        String onlineEstimateDate = getRequestString("onlineEstimateDate"); // 预期上线日期

        AgentProductFeedback agentProductFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);
        if (agentProductFeedback == null) {
            return MapMessage.errorMessage();
        }

        // 设置老师学科
        if (AgentProductFeedbackSubject.of(teacherSubject) != null) {
            agentProductFeedback.setTeacherSubject(AgentProductFeedbackSubject.of(teacherSubject));
        }

        // 设置反馈类型
        if (AgentProductFeedbackType.of(feedbackType) != null) {
            agentProductFeedback.setFeedbackType(AgentProductFeedbackType.of(feedbackType));
        }
        // 设置三级分类
        agentProductFeedback.setFirstCategory(AgentProductFeedbackCategory.nameOf(firstCategory));
        agentProductFeedback.setSecondCategory(AgentProductFeedbackCategory.nameOf(secondCategory));
        agentProductFeedback.setThirdCategory(AgentProductFeedbackCategory.nameOf(thirdCategory));

        // 设置PM 信息
        if (StringUtils.isNotBlank(pmAccount)) {
            agentProductFeedback.setPmAccount(pmAccount);
            agentProductFeedback.setPmAccountName(pmAccountName);
        }
        if (sendFlag && StringUtils.isNotBlank(noticeContent)) {
            agentProductFeedback.setNoticeContent(noticeContent);
            sendAppJpushMessageByIds(agentProductFeedback.getTeacherId(), noticeContent, agentProductFeedback.getTeacherSubject());
        }

        // 设置预计上线日期
        if (StringUtils.isNotBlank(onlineEstimateDate)) {
            agentProductFeedback.setOnlineEstimateDate(onlineEstimateDate);
        }

        agentProductFeedbackServiceClient.replaceAgentProductFeedback(agentProductFeedback);
        return MapMessage.successMessage();
    }

    private void sendAppJpushMessageByIds(Long teacherId, String noticeContent, AgentProductFeedbackSubject subject) {
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(teacherId);
        appMessage.setMessageType(TeacherMessageType.CRMREPLY.getType());
        appMessage.setTitle("系统通知");
        appMessage.setContent(noticeContent);
        appMessage.setImageUrl("");
        appMessage.setLinkUrl(""); // 这里写相对地址
        appMessage.setLinkType(1);
        appMessage.setIsTop(false);
        appMessage.setTopEndTime(0L);
        appMessage.setExtInfo(new HashMap<>());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
        AppMessageSource appMessageSource = AppMessageSource.PRIMARY_TEACHER;
        String key = "j";
        if (AgentProductFeedbackSubject.JUNIOR_ENGLISH == subject
                || AgentProductFeedbackSubject.JUNIOR_MATH == subject
                || AgentProductFeedbackSubject.JUNIOR_CHINESE == subject) {
            appMessageSource = AppMessageSource.PRIMARY_TEACHER;
            key = "j";
        } else if (AgentProductFeedbackSubject.MIDDLE_ENGLISH == subject
                || AgentProductFeedbackSubject.MIDDLE_MATH == subject
                || AgentProductFeedbackSubject.HIGH_MATH == subject) {
            appMessageSource = AppMessageSource.JUNIOR_TEACHER;
            key = "m";
        }
        Map<String, Object> extroInfo = MiscUtils.m("s", TeacherMessageType.CRMREPLY.getType(), "key", key, "t", "msg_list");
        appMessageServiceClient.sendAppJpushMessageByIds(noticeContent, appMessageSource, Collections.singletonList(teacherId), extroInfo, 0L);
    }


    @RequestMapping(value = "update_online_data.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFeedbackOnlineData() {

        Long feedbackId = getRequestLong("feedbackId");

        String onlineEstimateDate = getRequestString("onlineEstimateDate"); // 预期上线日期
        Boolean onlineFlag = getRequestBool("onlineFlag"); // 上线标记
        String onlineNotice = getRequestString("onlineNotice"); // 上线通知

        AgentProductFeedback agentProductFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);
        if (agentProductFeedback == null) {
            return MapMessage.errorMessage();
        }


        // 设置预计上线日期
        if (StringUtils.isNotBlank(onlineEstimateDate)) {
            agentProductFeedback.setOnlineEstimateDate(onlineEstimateDate);
        }

        if (onlineFlag) {
            agentProductFeedback.setOnlineFlag(true);
            if (StringUtils.isNotBlank(onlineNotice)) {
                agentProductFeedback.setOnlineNotice(onlineNotice);
                //发送上线通知
                informEmail(agentProductFeedback);
            }
        }

        agentProductFeedbackServiceClient.replaceAgentProductFeedback(agentProductFeedback);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "update_pm_data.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFeedbackPmData() {

        Long feedbackId = getRequestLong("feedbackId");

        String pmAccount = getRequestString("pmAccount"); // 指定的PM账号
        String pmAccountName = getRequestString("pmAccountName"); // 指定的PM姓名

        AgentProductFeedback agentProductFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);
        if (agentProductFeedback == null) {
            return MapMessage.errorMessage();
        }

        // 设置PM 信息
        agentProductFeedback.setPmAccount(pmAccount);
        agentProductFeedback.setPmAccountName(pmAccountName);
        agentProductFeedbackServiceClient.replaceAgentProductFeedback(agentProductFeedback);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "update_feedback_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFeedbackStatus() {

        Long feedbackId = getRequestLong("feedbackId");
        AgentProductFeedbackStatus feedbackStatus = AgentProductFeedbackStatus.of(getRequestInt("feedbackStatus"));
        if (feedbackStatus == null) {
            return MapMessage.errorMessage();
        }
        AgentProductFeedback agentProductFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(feedbackId);
        if (agentProductFeedback == null) {
            return MapMessage.errorMessage();
        }
        agentProductFeedback.setFeedbackStatus(feedbackStatus);
        agentProductFeedbackServiceClient.replaceAgentProductFeedback(agentProductFeedback);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "add_relation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addRelation(){
        Long thisId = getRequestLong("thisId");
        Long addId = getRequestLong("addId");

        AgentProductFeedback thisFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(thisId);
        if(thisFeedback == null){
            return MapMessage.errorMessage("关联失败");
        }
        AgentProductFeedback addFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(addId);
        if(addFeedback == null){
            return MapMessage.errorMessage("关联失败");
        }

        List<AgentProductFeedback> needUpdList = new ArrayList<>();
        if(thisFeedback.getRelationCode() == null && addFeedback.getRelationCode() == null){
            Long relationCode = System.currentTimeMillis();
            thisFeedback.setRelationCode(relationCode);
            addFeedback.setRelationCode(relationCode);
            needUpdList.add(thisFeedback);
            needUpdList.add(addFeedback);
        }else if(thisFeedback.getRelationCode() == null){
            thisFeedback.setRelationCode(addFeedback.getRelationCode());
            needUpdList.add(thisFeedback);
        }else if(addFeedback.getRelationCode() == null){
            addFeedback.setRelationCode(thisFeedback.getRelationCode());
            needUpdList.add(addFeedback);
        }else {
            if(Objects.equals(thisFeedback.getRelationCode(), addFeedback.getRelationCode())){
                return MapMessage.errorMessage("反馈已关联");
            }
            Long relationCode = thisFeedback.getRelationCode();
            List<AgentProductFeedback> feedbackList = agentProductFeedbackLoadClient.findByRelationCode(addFeedback.getRelationCode());
            feedbackList.forEach(p -> {
                p.setRelationCode(relationCode);
                needUpdList.add(p);
            });
        }

        if(CollectionUtils.isNotEmpty(needUpdList)){
            needUpdList.forEach(p -> agentProductFeedbackServiceClient.replaceAgentProductFeedback(p));
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete_relation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteRelation(){
        Long thisId = getRequestLong("thisId");
        Long deleteRelationId = getRequestLong("deleteId");

        AgentProductFeedback thisFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(thisId);
        if(thisFeedback == null || thisFeedback.getRelationCode() == null){
            return MapMessage.errorMessage("解除关联失败");
        }
        AgentProductFeedback deleteRelationFeedback = agentProductFeedbackLoadClient.loadByFeedbackId(deleteRelationId);
        if(deleteRelationFeedback == null || deleteRelationFeedback.getRelationCode() == null || !Objects.equals(thisFeedback.getRelationCode(), deleteRelationFeedback.getRelationCode())){
            return MapMessage.errorMessage("解除关联失败");
        }

        agentProductFeedbackServiceClient.setRelationCode(deleteRelationId, null);
        return MapMessage.successMessage();
    }


}
