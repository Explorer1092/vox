package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.AnnualReportService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;
import com.voxlearning.utopia.service.region.api.constant.RegionConstants;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.consumer.ActivityReportServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * 2017 年度报告 Controller
 * 2018 年终盘点
 */
@Controller
@RequestMapping("/usermobile/annual_report")
public class MobileAnnualReportController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = AnnualReportService.class)
    private AnnualReportService annualReportService;

    @Inject private UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private ActivityReportServiceClient activityReportServiceClient;

    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService tchActivityService;

    /**
     * 学生报告
     * @return
     */
    @RequestMapping(value = "/student.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentReport(){
        try{
            Long userId = getRequestLong("stuId");

            Student stu = studentLoaderClient.loadStudent(userId);
            Validate.notNull(stu,"学生ID不存在!");

            MapMessage resultMsg = annualReportService.getStudentReport(stu.getId());

            Map<Long,Boolean> userBlackMap = isBlackSchoolOrBlackUser(Collections.singletonList(stu));
            Boolean inBlack = SafeConverter.toBoolean(userBlackMap.get(userId));

            // 上面是看个人和学校的，下面是看地区的
            School school = asyncStudentServiceClient.getAsyncStudentService()
                    .loadStudentSchool(userId)
                    .getUninterruptibly();
            if (school != null && school.getRegionCode() != null) {
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                if (region != null && region.containsTag(RegionConstants.TAG_PAYMENT_BLACKLIST_REGIONS)) {
                    inBlack = true;
                }
            }

            resultMsg.put("inBlack",inBlack);

            return resultMsg;
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 老师报告
     * @return
     */
    @RequestMapping(value = "/teacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherReport(){
        try{
            User user = currentUser();
            Validate.notNull(user,"非法的请求!");

            Validate.isTrue(user.getUserType() == UserType.TEACHER.getType(),"非法的请求!");

            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            Validate.notNull(teacher,"老师ID不存在!");

            MapMessage resultMsg = annualReportService.getTeacherReport(teacher.getId());

            // 查看是否布置过寒假作业，通过抽奖记录来看，我也是醉了
            TeacherVocationLottery lottery = tchActivityService.loadTeacherVocationLottery(teacher.getId());
            if(lottery == null || lottery.getAssignTime() == null || lottery.getAssignTime() <= 0){
                resultMsg.put("vocationAssigned",false);
            }else
                resultMsg.put("vocationAssigned",true);

            return resultMsg;
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 家长报告
     * @return
     */
    @RequestMapping(value = "/parent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getParentReport(){
        try{
            User user = currentUser();
            Validate.notNull(user,"非法的请求!");

            Validate.isTrue(user.getUserType() == UserType.PARENT.getType(),"非法的请求!");

            Long studentId = getRequestLong("stuId");

            return annualReportService.getParentReport(user.getId(),studentId);
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/username.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserName(){
        try{
            Long userId = getRequestLong("userId");
            User user = raikouSystem.loadUser(userId);

            Validate.notNull(user,"用户不存在!");
            return MapMessage.successMessage().add("name",user.fetchRealname());
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/teacher2018.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeacherData(){
        MapMessage result = new MapMessage();
        try {
            Teacher teacher = currentTeacher();
            if (Objects.isNull(teacher)) {
                return MapMessage.errorMessage().setInfo("需要登陆才能操作!");
            }
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            Map<String, Object> teacherDatas = activityReportServiceClient.loadTeacherYearData(teacher.getId());
            result.set("city", teacherDetail.getCityName());
            result.set("subject", teacherDetail.getSubject().getValue());
            result.set("teacherRegion", teacherDetail.getCountyName());
            result.set("id", teacher.getId());
            if (Objects.isNull(teacherDatas)) {
                result.setSuccess(true);
                result.set("data", null);
                return result;
            }
            result.set("data", teacherDatas);
            result.setSuccess(true);
            return result;
        }catch(Exception e){
            logger.error("请求数据异常",e);
            return MapMessage.errorMessage().setInfo("请求数据异常!");
        }
    }

    @RequestMapping(value = "/student2018.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadStudentData(){
        MapMessage result = new MapMessage();
        try {
            User user = currentUser();
            if (Objects.isNull(user)) {
                return MapMessage.errorMessage().setInfo("需要登陆才能操作!");
            }
            Long studentId = null;
            if(UserType.STUDENT.getType() == user.getUserType().intValue()){
                studentId = user.getId();
            }else if(UserType.PARENT.getType() == user.getUserType().intValue()){
                User parent =  currentParent();
                String sid = getRequestString("sid");
                if(StringUtils.isNotBlank(sid)){
                    studentId = SafeConverter.toLong(sid);
                }else{
                    List<StudentParentRef> students = parentLoaderClient.loadParentStudentRefs(parent.getId());
                    if(CollectionUtils.isNotEmpty(students)){
                        StudentParentRef studentParentRef = students.get(0);
                        studentId = studentParentRef.getStudentId();
                    }else{
                        return MapMessage.errorMessage().setInfo("请求数据异常!");
                    }
                }
            }else{
                return MapMessage.errorMessage().setInfo("请求数据异常!");
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            Map<String, Object> studentDatas = activityReportServiceClient.loadStudentYearData(studentId);
            ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
            result.set("studentName", studentDetail.fetchRealname());
            result.set("studentRegion", Objects.nonNull(exRegion)?exRegion.getCountyName():"");
            result.set("city", Objects.nonNull(exRegion)?exRegion.getCityName():"");
            result.set("id", studentId);
            if (Objects.isNull(studentDatas)) {
                result.setSuccess(true);
                result.set("data", null);
                return result;
            }
            result.setSuccess(true);
            result.add("data", studentDatas);
            return result;
        }catch(Exception e){
            logger.error("请求数据异常",e);
            return MapMessage.errorMessage().setInfo("请求数据异常!");
        }
    }

    //老师分享页使用
    @RequestMapping(value = "/loadTeacher2018.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeacherDataById(){
        try {
            String teacherId = getRequestString("id");
            if (StringUtils.isBlank(teacherId) || SafeConverter.toLong(teacherId) == 0) {
                return MapMessage.errorMessage().setInfo("参数错误!");
            }
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(SafeConverter.toLong(teacherId));
            Map<String, Object> teacherDatas = activityReportServiceClient.loadTeacherYearData(SafeConverter.toLong(teacherId));
            if (Objects.isNull(teacherDatas)) {
                return MapMessage.successMessage().set("teacherName", teacherDetail.fetchRealname()).add("data", null);
            }
            //分享页所展示的信息
            Map<String, Object> sharePageData = new LinkedHashMap<>();
            sharePageData.put("assignHomeworkSum", teacherDatas.get("assignHomeworkSum"));
            sharePageData.put("deepNights", teacherDatas.get("deepNights"));
            sharePageData.put("teacherRanking", teacherDatas.get("teacherRanking"));
            sharePageData.put("homeworkTypes", teacherDatas.get("homeworkTypes"));
            sharePageData.put("assistantReadArticleNums", teacherDatas.get("assistantReadArticleNums"));
            sharePageData.put("assistantReadWords", teacherDatas.get("assistantReadWords"));
            sharePageData.put("assistantReadTime", teacherDatas.get("assistantReadTime"));
            sharePageData.put("assignHomeworkDays", teacherDatas.get("assignHomeworkDays"));
            return MapMessage.successMessage().set("teacherName", teacherDetail.fetchRealname()).add("data", sharePageData);
        }catch(Exception e){
            logger.error("请求数据异常",e);
            return MapMessage.errorMessage().setInfo("请求数据异常!");
        }
    }

    //老师分享页使用
    @RequestMapping(value = "/loadStudent2018.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadStudentDataById(){
        try {
            String studentId = getRequestString("id");
            if (StringUtils.isBlank(studentId) || SafeConverter.toLong(studentId) == 0) {
                return MapMessage.errorMessage().setInfo("参数错误!");
            }
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(SafeConverter.toLong(studentId));
            Map<String, Object> studentDatas = activityReportServiceClient.loadStudentYearData(SafeConverter.toLong(studentId));
            if (Objects.isNull(studentDatas)) {
                return MapMessage.successMessage().set("studentName", studentDetail.fetchRealname()).add("data", null);
            }
            Map<String, Object> sharePageData = new LinkedHashMap<>();
            //loginedDays,homeworkDoneSum,studentTitle,homeworkSurvey,xuedouNums,giveFlowers
            sharePageData.put("loginedDays", studentDatas.get("loginedDays"));
            sharePageData.put("homeworkDoneSum", studentDatas.get("homeworkDoneSum"));
            sharePageData.put("studentTitle", studentDatas.get("studentTitle"));
            sharePageData.put("homeworkSurvey", studentDatas.get("homeworkSurveyBackup"));
            sharePageData.put("xuedouNums", studentDatas.get("xuedouNums"));
            sharePageData.put("giveFlowers", studentDatas.get("giveFlowers"));
            return MapMessage.successMessage().set("studentName", studentDetail.fetchRealname()).add("data", sharePageData);
        }catch(Exception e){
            logger.error("请求数据异常",e);
            return MapMessage.errorMessage().setInfo("请求数据异常!");
        }
    }

}
