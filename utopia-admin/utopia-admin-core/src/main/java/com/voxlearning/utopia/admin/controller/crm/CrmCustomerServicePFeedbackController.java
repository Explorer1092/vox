package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCallback;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 客服产品反馈
 * Created by yaguang.wang
 * on 2017/3/2.
 */
@Controller
@RequestMapping("/crm/cs_productfeedback")
public class CrmCustomerServicePFeedbackController extends ProductFeedbackController {
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;


    /**
     * 老师反馈记录页
     *
     * @param model 模板
     * @return 老师反馈记录页
     */
    @RequestMapping(value = "teacher_feedback_list.vpage", method = RequestMethod.GET)
    public String teacherProductFeedbackList(Model model) {
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("feedbackList", crmProductFeedbackService.loadTeacherFeedbackList(teacherId));
        return "crm/feedback/teacherfeedbacklist";
    }

    /**
     * 取反馈记录的详情
     *
     * @return
     */
    @RequestMapping(value = "load_feedback_page.vpage", method = RequestMethod.GET)
    @ResponseBody
    protected MapMessage loadFeedbackPage() {
        MapMessage msg = MapMessage.successMessage();
        msg.add("type", createAgentFeedbackType());
        return msg;
    }

    /**
     * 查询反馈列表页
     *
     * @param model 放入选项的参数
     * @return 反馈列表页
     */
    @RequestMapping(value = "productfeedbacklist.vpage", method = RequestMethod.GET)
    public String productFeedbackList(Model model) {
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        model.addAttribute("statDate", DateUtils.getFirstDayOfMonth(new Date()));
        model.addAttribute("endDate", new Date());
        model.addAttribute("subject", AgentProductFeedbackSubject.values());
        model.addAttribute("type", AgentProductFeedbackType.values());
        model.addAttribute("status", AgentProductFeedbackStatus.values());
        model.addAttribute("pageSize",PAGE_SIZE);
        model.addAttribute("callback", AgentProductFeedbackCallback.values());
        model.addAttribute("prePath",prePath);
        return "crm/feedback/customerservicefeedback";
    }

    /**
     * 客服查询产品反馈
     *
     * @return 反馈列表内容
     */
    @RequestMapping(value = "feedback_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadFeedbackList() {
        ProductFeedbackListCondition condition = getFeedbackOfRequest();
        condition.setFeedbackPeopleId(getCurrentAdminUser().getAdminUserName());
        Integer page = getRequestInt("page") == 0 ? 1 : getRequestInt("page");
        return crmProductFeedbackService.loadFeedbackListByCondition(condition, getCurrentAdminUser().getRealName(), page, PAGE_SIZE);
    }

    /**
     * 新建反馈
     *
     * @return 是否新建成功
     */
    @RequestMapping(value = "save_new_feedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNewFeedback(HttpServletRequest request) {

        MultiValueMap<String,MultipartFile> multiValuedMap = ((DefaultMultipartHttpServletRequest)request).getMultiFileMap();
        AgentProductFeedback feedback = new AgentProductFeedback();
        AgentProductFeedbackType fbType = AgentProductFeedbackType.of(getRequestInt("type"));
        if (fbType == null) {
            return MapMessage.errorMessage("反馈类型错误");
        }
        feedback.setFeedbackType(fbType);
        String mobile = getRequestString("mobile");      // 老师手机号
        String content = StringUtils.filterEmojiForMysql(getRequestString("content"));           // 需求建议
        String bookName = getRequestString("bookName");         // 教材名称
        String bookGrade = getRequestString("bookGrade");       // 年级
        String bookUnit = getRequestString("bookUnit");         // 单元
        String bookCoveredArea = getRequestString("bookCoveredArea");   // 覆盖地区
        Integer bookCoveredStudentCount = getRequestInt("stuCount");  // 覆盖学生数
        MapMessage msg = getTeacherInfoByMobile(mobile);
        if (!msg.isSuccess()) {
            return msg;
        }
        Long teacherId = SafeConverter.toLong(msg.get("teacherId"));
        String teacherName = SafeConverter.toString(msg.get("teacherName"));
        AgentProductFeedbackSubject teacherSubject = msg.get("subjectType") == null ? null : (AgentProductFeedbackSubject) msg.get("subjectType");
        feedback.setContent(content);
        feedback.setBookGrade(bookGrade);
        feedback.setTeacherId(teacherId);
        feedback.setTeacherName(teacherName);
        feedback.setTeacherSubject(teacherSubject);
        AuthCurrentAdminUser user = getCurrentAdminUser();
        feedback.setAccount(user.getAdminUserName());
        feedback.setAccountName(user.getRealName());
        if (!AgentProductFeedbackType.isClazz(1, fbType)) {
            feedback.setBookName(bookName);
            feedback.setBookGrade(bookGrade);
        }
        if (AgentProductFeedbackType.isClazz(2, fbType)) {
            feedback.setBookUnit(bookUnit);
        }
        if (AgentProductFeedbackType.isClazz(3, fbType)) {
            feedback.setBookCoveredArea(bookCoveredArea);
            feedback.setBookCoveredStudentCount(bookCoveredStudentCount);
        }

        AgentProductFeedbackCallback callback = AgentProductFeedbackCallback.of(getRequestBool("callback"));
        if (callback == null) {
            return MapMessage.errorMessage("是否需要回电错误");
        }
        feedback.setCallback(callback.getId());

        List<MultipartFile> multipartFiles = multiValuedMap.get("file");
        if (CollectionUtils.isNotEmpty(multipartFiles)) {

            for (MultipartFile pic : multipartFiles) {

                String originalFileName = pic.getOriginalFilename();
                String prefix = "rai-" + DateUtils.dateToString(new Date(), "yyyyMMdd");

                try (InputStream inStream = pic.getInputStream()) {
                    String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
                    if(feedback.getPic1Url() == null){
                        feedback.setPic1Url(filename);
                    } else if(feedback.getPic2Url() == null) {
                        feedback.setPic2Url(filename);
                    } else if(feedback.getPic3Url() == null) {
                        feedback.setPic3Url(filename);
                    } else if(feedback.getPic4Url() == null) {
                        feedback.setPic4Url(filename);
                    } else if(feedback.getPic5Url() == null) {
                        feedback.setPic5Url(filename);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return crmProductFeedbackService.saveNewFeedback(feedback);
    }

    @RequestMapping(value = "search_teacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searcherTeacherByMobile() {
        String mobile = getRequestString("mobile");
        return getTeacherInfoByMobile(mobile);
    }

    private MapMessage getTeacherInfoByMobile(String mobile) {
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("所输入的电话号码有误");
        }
        UserAuthentication userAuthenticationList = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
        if (userAuthenticationList == null || userAuthenticationList.getId() == null) {
            return MapMessage.errorMessage("未找到该号码所对应的老师");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userAuthenticationList.getId());
        if (teacher == null) {
            return MapMessage.errorMessage("未找到老师信息");
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(userAuthenticationList.getId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("未找到老师对应学校的信息");
        }
        MapMessage msg = MapMessage.successMessage();
        msg.add("schoolName", school.getCname());
        msg.add("teacherId", teacher.getId());
        msg.add("teacherName", teacher.getProfile() != null ? teacher.getProfile().getRealname() : "");
        AgentProductFeedbackSubject subject = getTeacherSubject(teacher);
        msg.add("subject", subject != null ? subject.getDesc() : "");
        msg.add("subjectType", subject);
        return msg;
    }

    private AgentProductFeedbackSubject getTeacherSubject(Teacher teacher) {
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
        return subject;
    }


}
