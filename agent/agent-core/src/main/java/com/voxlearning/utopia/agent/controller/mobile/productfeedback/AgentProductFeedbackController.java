package com.voxlearning.utopia.agent.controller.mobile.productfeedback;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.productfeedback.ProductFeedbackListInfo;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.productfeedback.AgentProductFeedbackService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * 产品反馈
 * Created by yaguang.wang on 2017/2/21.
 */
@Controller
@RequestMapping("/mobile/feedback")
public class AgentProductFeedbackController extends AbstractAgentController {

    @Inject private AgentProductFeedbackService agentProductFeedbackService;
    @Inject private BaseOrgService baseOrgService;
    @Inject
    private TeacherResourceService teacherResourceService;

    private static final String PRODUCT_KEY = "AGENT_PRODUCT_FEEDBACK_TYPE";
    private static final List<AgentRoleType> IS_MANAGE_ROLE = Arrays.asList(AgentRoleType.CityManager, AgentRoleType.Country, AgentRoleType.BUManager, AgentRoleType.Region, AgentRoleType.AreaManager);

    /**
     * 产品反馈
     *
     * @param model model
     * @return 产品反馈列表
     */
    @RequestMapping(value = "view/index.vpage", method = RequestMethod.GET)
    @OperationCode("46f15f4f2a46412c")
    public String index(Model model) {
        Long userId = getRequestLong("userId");
        if (userId == 0L) {
            userId = getCurrentUserId();
        }
        AgentUser user = baseOrgService.getUser(userId);
        if (user != null) {
            model.addAttribute("userName", user.getRealName());
        }
        AgentRoleType userRole = baseOrgService.getUserRole(getCurrentUserId());
        ProductFeedbackListInfo feedbackList = agentProductFeedbackService.createProductFeedbackInfo(userId);
        feedbackList.setIsManager(IS_MANAGE_ROLE.contains(userRole));
        model.addAttribute("feedbackList", feedbackList);
        return "rebuildViewDir/mobile/feedback/feedBackList";
    }

    /**
     * 新建产品反馈 产品反馈详情
     *
     * @param model model
     * @return 新建产品反馈页，产品反馈详情
     */
    @RequestMapping(value = "view/feedbackinfo.vpage", method = RequestMethod.GET)
    public String viewFeedbackInfo(Model model) {
        Long feedbackId = getRequestLong("feedbackId");
        Long teacherId = getRequestLong("teacherId");       // 老师详情页进入新建产品反馈时，加入teacherId
        AgentProductFeedback feedback = getSessionAgentProductFeedback();
        if (feedback.getId() == null && feedbackId == 0L) {
            feedback = getSessionAgentProductFeedback();
        } else if (feedbackId != 0L) {
            feedback = agentProductFeedbackService.loadAgentProductFeedbackById(feedbackId);
        } 
        if (feedback != null && feedbackId != 0L) {
            feedback.setMySelf(feedback.getTeacherId() == null);
        }
        if (feedback != null && (feedback.getId() == null || feedback.getId() == 0L)) {
            feedbackSetTeacher(teacherId, feedback);                   // 老师信息赋值
            setSessionAgentProductFeedback(feedback);
        }
        model.addAttribute("applyProcessResult", agentProductFeedbackService.loadApplyProcessByApplyId(feedbackId));
        model.addAttribute("feedback", feedback);
        model.addAttribute("feedbackType", AgentProductFeedbackType.values());
        return "rebuildViewDir/mobile/feedback/product_feedback";
    }

    /**
     * 提交新建产品反馈
     *
     * @return 提交是否成功
     */
    @RequestMapping(value = "operate/feedbackinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("34132abacd224e22")
    public MapMessage operateFeedbackInfo() {
        AgentProductFeedback feedback = getFeedbackFromRequest();
        feedback.setAccount(SafeConverter.toString(getCurrentUser().getUserId()));
        feedback.setAccountName(getCurrentUser().getRealName());
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        if(feedback.getTeacherId() != null){
            MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(getCurrentUserId(), feedback.getTeacherId(), SearchService.SCENE_SEA);
            if (!mapMessage.isSuccess()){
                if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                    return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
                }else {
                    return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
                }
            }
        }
        MapMessage msg = agentProductFeedbackService.saveProductFeedback(feedback);
        if (!msg.isSuccess()) {
            return msg;
        }
        cleanSessionAgentProductFeedback();
        return msg;
    }

    /**
     * 保存还未新建的产品到session
     *
     * @return 成功保存
     */
    @RequestMapping(value = "operate/savesession.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSession() {
        AgentProductFeedback feedback = getFeedbackFromRequest();
        setSessionAgentProductFeedback(feedback);
        return MapMessage.successMessage();
    }

    private AgentProductFeedback getFeedbackFromRequest() {
        Integer fbType = getRequestInt("fbType");
        String noticeFlagStr = getRequest().getParameter("noticeFlag");
        Boolean noticeFlag = StringUtils.isBlank(noticeFlagStr) ? null : SafeConverter.toInt(noticeFlagStr) == 1;
        String pic1Url = getRequestString("pic1Url"); // 附图1 URL
        String pic2Url = getRequestString("pic2Url"); // 附图2 URL
        String pic3Url = getRequestString("pic3Url"); // 附图3 URL
        String content = StringUtils.filterEmojiForMysql(getRequestString("content"));
        String bookName = getRequestString("bookName");
        String bookGrade = getRequestString("bookGrade");
        String bookUnit = getRequestString("bookUnit");
        String bookCoveredArea = getRequestString("bookCoveredArea");
        Integer bookCoveredStudentCount = getRequestInt("bookCoveredStudentCount");
        AgentProductFeedback feedback = getSessionAgentProductFeedback();
        boolean  myself = getRequestBool("mySelf");
        agentProductFeedbackService.createAgentProductFeedback(feedback, fbType, noticeFlag, pic1Url, pic2Url, pic3Url, content, bookName,
                bookGrade, bookUnit, bookCoveredArea, bookCoveredStudentCount,myself);
        return feedback;
    }

    /**
     * 进入选择反馈老师页
     *
     * @return
     */
    @RequestMapping(value = "view/searchteacher.vpage", method = RequestMethod.GET)
    public String searchTeacher(Model model) {
        String back = getRequestString("back");
        String type = getRequestString("type");
        model.addAttribute("type", type);
        model.addAttribute("back", back);
        return "rebuildViewDir/mobile/feedback/productTeacher";
    }

    /**
     * 保存老师信息
     *
     * @return 是否保存成功
     */
    @RequestMapping(value = "operate/saveteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacher() {
        Long teacherId = getRequestLong("teacherId");
        AgentProductFeedback feedback = getSessionAgentProductFeedback();
        return feedbackSetTeacher(teacherId, feedback);
    }

    private MapMessage feedbackSetTeacher(Long teacherId, AgentProductFeedback feedback) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage(StringUtils.formatMessage("老师ID{}的信息未找到!", teacherId));
        }
        feedback.setTeacherName(teacher.getProfile() == null ? "" : teacher.getProfile().getRealname());
        feedback.setTeacherId(teacher.getId());
        //fixme 对应老师的科目
        AgentProductFeedbackSubject subject = null;
        // 小学
        if(teacher.isPrimarySchool()){
            if(teacher.isEnglishTeacher()){
                subject = AgentProductFeedbackSubject.JUNIOR_ENGLISH;
            }else if(teacher.isChineseTeacher()){
                subject = AgentProductFeedbackSubject.JUNIOR_CHINESE;
            }else if(teacher.isMathTeacher()){
                subject = AgentProductFeedbackSubject.JUNIOR_MATH;
            }
        }else if(teacher.isJuniorTeacher()){
            if(teacher.isEnglishTeacher()){
                subject = AgentProductFeedbackSubject.MIDDLE_ENGLISH;
            }else if(teacher.isMathTeacher()){
                subject = AgentProductFeedbackSubject.MIDDLE_MATH;
            }else {
                subject = AgentProductFeedbackSubject.MIDDLE_HIGH_OTHERS;
            }
        }else if(teacher.isSeniorTeacher()){
            if(teacher.isMathTeacher()){
                subject = AgentProductFeedbackSubject.HIGH_MATH;
            }else {
                subject = AgentProductFeedbackSubject.MIDDLE_HIGH_OTHERS;
            }
        }
        feedback.setTeacherSubject(subject);
        setSessionAgentProductFeedback(feedback);
        return MapMessage.successMessage();
    }


    // 从缓存中获取缓存对象
    private AgentProductFeedback getSessionAgentProductFeedback() {
        Long userId = getCurrentUserId();
        Object obj = agentCacheSystem.getUserSessionAttribte(userId, PRODUCT_KEY);
        if (obj != null && obj instanceof AgentProductFeedback) {
            return (AgentProductFeedback) obj;
        }
        return new AgentProductFeedback();
    }

    // 保存缓存的方法
    private void setSessionAgentProductFeedback(AgentProductFeedback feedback) {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, PRODUCT_KEY, feedback);
    }

    // 清空缓存
    private void cleanSessionAgentProductFeedback() {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, PRODUCT_KEY, new AgentProductFeedback());
    }
}
