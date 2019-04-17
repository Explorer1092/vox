package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.api.constans.*;
import com.voxlearning.utopia.service.rstaff.consumer.SchoolMasterServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.constants.ResearchStaffUserType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016-9-24 17:40
 */
@Controller
@RequestMapping("/schoolmaster")
public class SchoolMasterController extends SchoolMasterBaseController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolMasterServiceClient schoolMasterServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        if (researchStaff.getManagedRegion().getResarchStaffUserType() != ResearchStaffUserType.PRESIDENT.getType()) { //不是校长账号
            model.addAttribute("infofailed", true);
            return "redirect:/rstaff/generaloverview.vpage";
        }
        return "redirect:/schoolmaster/generaloverview.vpage"; //大数据报告--学校概况
    }

    /**
     * 校长总体概览页面
     *
     * @return
     */
    @RequestMapping(value = "generaloverview.vpage", method = RequestMethod.GET)
    public String generalOverview(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        model.addAttribute("idType", "schoolmaster");
        model.addAttribute("pageType", "generaloverview");
        return "adminteacher/schoolmaster/generaloverview";
    }

    /**
     * 校长学情分析页面
     *
     * @return
     */
    @RequestMapping(value = "learninganalysis.vpage", method = RequestMethod.GET)
    public String learningAnalysis(Model model) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        model.addAttribute("idType", "schoolmaster");
        model.addAttribute("pageType", "learninganalysis");
        return "adminteacher/schoolmaster/learninganalysis";
    }

    /**
     * 校长模考统测页面
     *
     * @return
     */
    @RequestMapping(value = "systemtest.vpage", method = RequestMethod.GET)
    public String systemTest(Model model) {
        //在这里确定登录的人员是校长，还是市教研员，区教研员，还是街道教研员
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        if (researchStaff == null) {
            return "redirect:" + getBaseDomain();
        }
        String result = validatePasswdAndMobile(researchStaff.getId());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        model.addAttribute("idType", "schoolmaster");
        model.addAttribute("pageType", "systemtest");
        model.addAttribute("regionLevel", "school");
        return "adminteacher/schoolmaster/systemtest";
    }

    /**
     * 校长模考报告页面
     *
     * @return
     */
    @RequestMapping(value = "testreport.vpage", method = RequestMethod.GET)
    public String testReport(Model model) {
        model.addAttribute("idType", "schoolmaster");
        model.addAttribute("pageType", "testreport");
        return "adminteacher/testreport/index";
    }

    /**
     * 校长个人中心页面
     *
     * @return
     */
    @RequestMapping(value = "admincenter.vpage", method = RequestMethod.GET)
    public String adminCenter(Model model) {
        model.addAttribute("idType", "schoolmaster");
        model.addAttribute("pageType", "admincenter");
        return "adminteacher/personalcenter/index";
    }

    //显示个人中心数据
    @RequestMapping(value = "loadPersonInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadPersonInfo() {
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (researchStaff == null) {
                return MapMessage.errorMessage("请重新登录");
            }
            String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(researchStaff.getId());

            if (researchStaff.getManagedRegion().getResarchStaffUserType() == ResearchStaffUserType.PRESIDENT.getType()) {
                Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
                School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                String schoolRegion = region.formalizeCityCountyName();

                return MapMessage.successMessage().add("schoolName", school.getCname())
                        .add("schoolRegion", schoolRegion)
                        .add("mobile", mobile);
            } else {
                Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
                List<Integer> regionCodes = getRegionCodesParam(researchStaff);
                List<Integer> cityCodes = getCityCodesParam(researchStaff);
                List<String> regionNames = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(schoolIds)) {
                    Map<Long, School> schools = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly();
                    for (Map.Entry<Long, School> entry : schools.entrySet()) {
                        School temp = entry.getValue();
                        regionNames.add(temp.getCname());
                    }
                }

                if (CollectionUtils.isNotEmpty(regionCodes)) {
                    Map<Integer, ExRegion> regions = raikouSystem.getRegionBuffer().loadRegions(regionCodes);
                    for (Map.Entry<Integer, ExRegion> entry : regions.entrySet()) {
                        ExRegion temp = entry.getValue();
                        regionNames.add(temp.getName());
                    }
                }

                if (CollectionUtils.isNotEmpty(cityCodes)) {
                    Map<Integer, ExRegion> citys = raikouSystem.getRegionBuffer().loadRegions(cityCodes);
                    for (Map.Entry<Integer, ExRegion> entry : citys.entrySet()) {
                        ExRegion temp = entry.getValue();
                        regionNames.add(temp.getName());
                    }
                }
                return MapMessage.successMessage().add("regionNames", regionNames)
                        .add("mobile", mobile);
            }
        } catch (Exception e) {
            logger.error("获取数据异常", e);
            return MapMessage.errorMessage("获取数据失败");
        }
    }

    /**
     * 个人中心 更新我的资料
     */
    @RequestMapping(value = "modifyprofile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyProfile() {
        ResearchStaff schoolmaster = currentResearchStaff();
        if (schoolmaster == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String newName = getRequestString("name");
        if (!SpecialTeacherConstants.checkChineseName(newName)) {
            return MapMessage.errorMessage("姓名仅支持十个字以内中文、间隔符·");
        }

        if (badWordCheckerClient.containsUserNameBadWord(newName)) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇!");
        }

        // 更新名字
        if (!StringUtils.equals(newName, schoolmaster.getProfile().getEname())) {
            if (!userServiceClient.changeName(schoolmaster.getId(), newName).isSuccess()) {
                return MapMessage.errorMessage("个人信息更新失败！");
            }
        }
        return MapMessage.successMessage("更新个人信息成功！");
    }

    /**
     * 发送手机验证码
     */
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendMobileCode() {
        ResearchStaff schoolmaster = currentResearchStaff();
        if (schoolmaster == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String mobile = getRequestString("mobile");
        try {
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_VERIFY_MOBILE_CENTER.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    /**
     * 重新绑定手机号 验证手机号
     */
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRebindMobile() {
        ResearchStaff schoolmaster = currentResearchStaff();
        if (schoolmaster == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String code = getRequestString("code");
        try {
            return verificationService.verifyMobile(schoolmaster.getId(), code, SmsType.TEACHER_VERIFY_MOBILE_CENTER.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    /**
     * 修改密码，发送短信验证码
     */
    @RequestMapping(value = "smsvalidatecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendChangePasswordCode() {
        ResearchStaff schoolmaster = currentResearchStaff();
        if (schoolmaster == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String am = sensitiveUserDataServiceClient.loadUserMobile(currentUserId());
        if (StringUtils.isBlank(am)) {
            return MapMessage.errorMessage("请先绑定手机");
        }
        if (!MobileRule.isMobile(am)) {
            return MapMessage.errorMessage("错误的手机号");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), am, SmsType.TEACHER_CHANGE_PASSWORD.name());
    }

    /**
     * 用户通过手机验证码的方式重置密码
     */
    @RequestMapping(value = "resetpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassword() {
        ResearchStaff schoolmaster = currentResearchStaff();
        if (schoolmaster == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Long userId = schoolmaster.getId();
        String verifyCode = getRequestString("verifyCode");
        String newPassword = getRequestString("newPassword");
        try {
            MapMessage verifyResult = smsServiceClient.getSmsService().verifyValidateCode(userId, verifyCode, SmsType.TEACHER_CHANGE_PASSWORD.name());
            if (!verifyResult.isSuccess()) {
                return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
            }
            // 修改密码
            userServiceClient.setPassword(schoolmaster, newPassword);
            // 用户自己修改密码记录一下
            saveForgotPwdRecord(userId, FindPasswordMethod.MODIFY_PASSWORD);
            // 由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            // 由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            resetAuthCookie(getWebRequestContext(), -1);
            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            logger.error("Failed change password for special teacher. id={}", userId, ex);
            return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
        }
    }

    /**
     * 重置cookie
     */
    private void resetAuthCookie(WashingtonRequestContext context, int expire) {
        List<UserSecurity> securities = userLoaderClient.loadUserSecurities(context.getCurrentUser().getId().toString(), context.getCurrentUser().fetchUserType());
        UserSecurity userSecurity = MiscUtils.firstElement(securities);
        if (null != userSecurity) {
            context.saveAuthenticationStates(expire, userSecurity);
        }
    }

    private void saveForgotPwdRecord(Long userId, FindPasswordMethod method) {
        if (userId == null) {
            return;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(method.getDescription());

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }


    @RequestMapping(value = "loadSchoolUsageData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadSchoolUsageData(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            String teacViewType = getRequestString("teacViewType"); //取值1，2   1，代表本月使用人数，2，代表本月新增人数
            String studViewType = getRequestString("studViewType"); //取值1，2   1，代表本月使用人数，2，代表本月新增人数

            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            Date curDate = new Date();
            int day = curDate.getDate();
            Calendar cal = Calendar.getInstance();
            if (day <= 3) {
                cal.add(Calendar.MONTH, -1);
            }
            String dateStr = DateUtils.dateToString(cal.getTime(), "yyyyMM");

            Map<String, Object> data = schoolMasterServiceClient.loadSchoolUsageData(schoolId, dateStr);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            //老师总使用人数
            long usageTeachers = (long) data.get("usageTeachers");
            //学生总使用人数
            long usageStudents = (long) data.get("usageStudents");
            //新增老师总使用人数
            long increaseTeachers = (long) data.get("increaseTeachers");
            //新增学生总使用人数
            long increaseStudents = (long) data.get("increaseStudents");
            //各年级的学科以及学科对应老师使用人数
            Map<String, List<Map<String, Integer>>> usageSubjectTeachers = (Map<String, List<Map<String, Integer>>>) data.get("usageSubjectTeachers");
            Map<String, Integer> usageStudentsMap = (Map<String, Integer>) data.get("usageStudentsMap");
            Map<String, List<Map<String, Integer>>> increaseSubjectTeachers = (Map<String, List<Map<String, Integer>>>) data.get("increaseSubjectTeachers");
            Map<String, Integer> increaseStudentsMap = (Map<String, Integer>) data.get("increaseStudentsMap");

            //组织固定返回数据
            result.add("usageTeachers", usageTeachers);
            result.add("usageStudents", usageStudents);
            result.add("increaseTeachers", increaseTeachers);
            result.add("increaseStudents", increaseStudents);

            //老师使用情况柱图的数据
            //X轴为年级数
            List<String> gradeList = getGradeList();
            result.add("teacGradeData", gradeList);
            //legend
            List<String> subjectList = getSubjectList();
            result.add("teacLegendData", subjectList);
            //series Data  语文对应的data Map<语文字符串，次数的拼接字符串>
            List<Map<String, Object>> seriesData = new LinkedList<>();
            Map<String, List<Integer>> teacSubjectData = null;

            if (teacViewType.equals("2")) {
                teacSubjectData = getTeacSeriesData(increaseSubjectTeachers);
            } else {
                teacSubjectData = getTeacSeriesData(usageSubjectTeachers);
            }

            Map<String, Object> chineseSubData = new LinkedHashMap<>();
            chineseSubData.put("name", Subject.CHINESE.getValue());
            chineseSubData.put("data", teacSubjectData.get(Subject.CHINESE.toString()));
            Map<String, Object> mathSubData = new LinkedHashMap<>();
            mathSubData.put("name", Subject.MATH.getValue());
            mathSubData.put("data", teacSubjectData.get(Subject.MATH.toString()));
            Map<String, Object> englishSubData = new LinkedHashMap<>();
            englishSubData.put("name", Subject.ENGLISH.getValue());
            englishSubData.put("data", teacSubjectData.get(Subject.ENGLISH.toString()));

            seriesData.add(chineseSubData);
            seriesData.add(mathSubData);
            seriesData.add(englishSubData);
            result.set("teacSeries", seriesData);

            //组织学生的图标信息
            List<Integer> studSubjectData = null;
            if (studViewType.equals("2")) {
                studSubjectData = getStuSeriesData(usageStudentsMap);
            } else {
                studSubjectData = getStuSeriesData(increaseStudentsMap);
            }
            result.add("studGradeData", gradeList);
            result.set("studSeries", studSubjectData);
            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长获取使用情况数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadHomeworkCondition.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadHomeworkCondition(Model model) {
        MapMessage result = new MapMessage();
        //月份  ，如果当前时间是六月四号，就是包含六月份，如果当前时间是六月三号之前不包含当前月份
        Date curDate = new Date();
        int day = curDate.getDate();
        Calendar cal = Calendar.getInstance();
        if (day <= 3) {
            cal.add(Calendar.MONTH, -6);
        } else {
            cal.add(Calendar.MONTH, -5);
        }

        List<Map<String, String>> dateList = new LinkedList<>();
        for (int i = 0; i < 6; i++) {
            String value = DateUtils.dateToString(cal.getTime(), "yyyyMM");
            String text = DateUtils.dateToString(cal.getTime(), "yyyy年MM月");
            Map<String, String> dateMap = new LinkedHashMap<>();
            dateMap.put("name", text);
            dateMap.put("value", value);
            if (value.compareTo("201806") >= 0) {
                dateList.add(dateMap);
            }
            cal.add(Calendar.MONTH, 1);
        }
        result.add("dateList", dateList);

        //年级
        List<Map<String, String>> gradeList = new LinkedList<>();
        Map<String, String> temp0 = new LinkedHashMap<>();
        temp0.put("name", "全部年级");
        temp0.put("value", "0");
        gradeList.add(temp0);
        List<String> tempGrade = getGradeList();
        for (int i = 1; i <= tempGrade.size(); i++) {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put("name", tempGrade.get(i - 1));
            temp.put("value", "" + i);
            gradeList.add(temp);
        }
        result.add("gradeList", gradeList);

        //学科
        Map<String, String> subjectMap1 = new LinkedHashMap<>();
        subjectMap1.put("name", Subject.ENGLISH.getValue());
        subjectMap1.put("value", Subject.ENGLISH.toString());
        Map<String, String> subjectMap2 = new LinkedHashMap<>();
        subjectMap2.put("name", Subject.MATH.getValue());
        subjectMap2.put("value", Subject.MATH.toString());
        Map<String, String> subjectMap3 = new LinkedHashMap<>();
        subjectMap3.put("name", Subject.CHINESE.getValue());
        subjectMap3.put("value", Subject.CHINESE.toString());
        List<Map<String, String>> subjectList = new LinkedList<>();
        subjectList.add(subjectMap1);
        subjectList.add(subjectMap2);
        subjectList.add(subjectMap3);
        result.add("subjectList", subjectList);
        result.add("result", true);
        return result;
    }

    @RequestMapping(value = "loadHomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadHomework(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            String dateStr = getRequestString("dateStr");
            String grade = getRequestString("grade");
            String subject = getRequestString("subject");

            Map<String, Object> data = schoolMasterServiceClient.loadHomework(schoolId, grade, subject, dateStr);
            if (data == null) {
                result.add("result", false);
                result.add("info", "没有数据");
                return result;
            }
            //饼图的数据
            Map<String, Integer> homeworkScenes = (Map<String, Integer>) data.get("homeworkScenes");
            List<String> pieLegendData = new LinkedList<>();
            pieLegendData.addAll(homeworkScenes.keySet());
            Collections.sort(pieLegendData);
            List<Map<String, Object>> pieSeriesData = new LinkedList<>();
            for (String scenes : pieLegendData) {
                Integer counts = homeworkScenes.get(scenes);
                Map<String, Object> pieData = new LinkedHashMap<>();
                pieData.put("name", scenes);
                pieData.put("value", counts);
                pieSeriesData.add(pieData);
            }
            result.add("pieLegendData", pieLegendData);
            result.add("pieSeriesData", pieSeriesData);

            //柱图数据
            Map<String, Map<String, Integer>> weekHomeworkScenes = (Map<String, Map<String, Integer>>) data.get("weekHomeworkScenes");
            Iterator<String> tempWeeks = weekHomeworkScenes.keySet().iterator();
            Set<String> barScenesSet = new HashSet();
            while (tempWeeks.hasNext()) {
                String week = tempWeeks.next();
                Map<String, Integer> scenesMap = weekHomeworkScenes.get(week);
                barScenesSet.addAll(scenesMap.keySet());
            }
            List<String> barLegendData = new LinkedList<>();
            barLegendData.addAll(barScenesSet);
            Collections.sort(barLegendData);

            Set<String> barWeekData = new TreeSet<>();
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            for (String scenes : barLegendData) {
                List<Integer> countsList = new LinkedList<>();
                Iterator<String> weeks = weekHomeworkScenes.keySet().iterator();
                while (weeks.hasNext()) {
                    String week = weeks.next();
                    barWeekData.add(week);
                    Map<String, Integer> countMap = weekHomeworkScenes.get(week);
                    Integer counts = countMap.get(scenes);
                    if (counts == null) {
                        counts = 0;
                    }
                    countsList.add(counts);
                }
                Map<String, Object> tempBarMap = new LinkedHashMap<>();
                tempBarMap.put("name", scenes);
                tempBarMap.put("data", countsList);
                barSeriesData.add(tempBarMap);
            }

            result.add("barWeekData", barWeekData);
            result.add("barLegendData", barLegendData);
            result.add("barSeriesData", barSeriesData);
            //班级作业排名，  sortMapByValue
            Map<String, Integer> clazzDoHomeworkCounts = (Map<String, Integer>) data.get("clazzDoHomeworkCounts");
            Map<String, Integer> clazzDoHomeworkData = sortMapByValue(clazzDoHomeworkCounts);
            List<Map<String, Object>> clazzDoHomeworkDataL = new LinkedList();
            if (MapUtils.isNotEmpty(clazzDoHomeworkData)) {
                for (Map.Entry<String, Integer> entry : clazzDoHomeworkData.entrySet()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put("name", entry.getKey());
                    temp.put("value", entry.getValue());
                    if (entry.getValue().intValue() > 0) {
                        clazzDoHomeworkDataL.add(temp);
                    }
                }
            }
            reckonRanking(clazzDoHomeworkDataL);
            if (clazzDoHomeworkDataL.size() > 5) {
                result.add("clazzDoHomeworkData", clazzDoHomeworkDataL.subList(0, 5));
            } else {
                result.add("clazzDoHomeworkData", clazzDoHomeworkDataL);
            }
            //老师排名
            Map<String, Integer> assignmentCounts = (Map<String, Integer>) data.get("assignmentCounts");
            Map<String, Integer> assignmentData = sortMapByValue(assignmentCounts);
            List<Map<String, Object>> assignmentDataL = new LinkedList();
            if (MapUtils.isNotEmpty(assignmentData)) {
                for (Map.Entry<String, Integer> entry : assignmentData.entrySet()) {
                    Map<String, Object> temp = new LinkedHashMap<>();
                    temp.put("name", entry.getKey());
                    temp.put("value", entry.getValue());
                    if (entry.getValue().intValue() > 0) {
                        assignmentDataL.add(temp);
                    }
                }
            }
            reckonRanking(assignmentDataL);
            if (assignmentDataL.size() > 5) {
                result.add("assignmentData", assignmentDataL.subList(0, 5));
            } else {
                result.add("assignmentData", assignmentDataL);
            }
            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长获取作业情况数据异常", e);
            result.add("result", false);
            result.add("info", "没有数据");
        }
        return result;
    }


    @RequestMapping(value = "loadUnitAvgQuestionsCondition.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadUnitAvgQuestionsCondition(Model model) {
        MapMessage result = new MapMessage();
        //年级
        List<Map<String, String>> gradeList = new LinkedList<>();
        List<String> tempGrade = getGradeList();
        for (int i = 1; i <= tempGrade.size(); i++) {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put("name", tempGrade.get(i - 1));
            temp.put("value", "" + i);
            gradeList.add(temp);
        }
        result.add("gradeList", gradeList);

        //学科
        Map<String, String> subjectMap1 = new LinkedHashMap<>();
        subjectMap1.put("name", Subject.ENGLISH.getValue());
        subjectMap1.put("value", Subject.ENGLISH.toString());
        Map<String, String> subjectMap2 = new LinkedHashMap<>();
        subjectMap2.put("name", Subject.MATH.getValue());
        subjectMap2.put("value", Subject.MATH.toString());
        Map<String, String> subjectMap3 = new LinkedHashMap<>();
        subjectMap3.put("name", Subject.CHINESE.getValue());
        subjectMap3.put("value", Subject.CHINESE.toString());
        List<Map<String, String>> subjectList = new LinkedList<>();
        subjectList.add(subjectMap1);
        subjectList.add(subjectMap2);
        subjectList.add(subjectMap3);
        result.add("subjectList", subjectList);


        ResearchStaff researchStaff = currentResearchStaff();
        Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
        List<Clazz> clazzList = getClazzesByShooolId(schoolId);

        //学年，学期
        List<Map<String, Object>> termList = getTerms();

        result.add("termList", termList);
        Map<String, Object> term0 = termList.get(0);
        Map<String, Object> term1 = termList.get(1);
        //夸学年的情况考虑
        int jie0 = (int) term0.get("jie");
        int jie1 = (int) term1.get("jie");
        List<Map<String, Object>> gradeClazzList = new LinkedList<>();
        if (jie0 == jie1) {//同一学年
            getGradeClazzRel(gradeList, clazzList, jie0, gradeClazzList);
        } else {
            //jie0的数据
            List<Map<String, Object>> gradeClazzListTemp = new LinkedList<>();
            getGradeClazzRel(gradeList, clazzList, jie1, gradeClazzListTemp);
            gradeClazzList.addAll(gradeClazzListTemp);
            //上一节学年的对应关系，根据届去计算。比如2017届的在2017~2018学年是一年级，2016届在2017~2018学年是二年级......
            int grade = 1;
            int limit = jie0 - 6;
            if (CollectionUtils.isNotEmpty(clazzList)) {
                Clazz tempClazz = clazzList.get(0);
                if (tempClazz.getEduSystem() == EduSystemType.P5) {
                    limit = jie0 - 5;
                    //五年制的时候6年级的处理
                    Map<String, Object> clazzMap0 = new LinkedHashMap<>();
                    clazzMap0.put("name", "全部");
                    clazzMap0.put("id", 0L);
                    List<Map<String, Object>> clazzMapList = new LinkedList<>();
                    clazzMapList.add(clazzMap0);

                    Map<String, Object> clazzListMap = new LinkedHashMap<>();
                    clazzListMap.put("grade", "6_" + jie0);
                    clazzListMap.put("clazzList", clazzMapList);
                    gradeClazzList.add(clazzListMap);
                }
            }
            for (int i = jie0; i > limit; i--) {
                final int jie = i;
                List<Clazz> jieClazzList = clazzList.stream().filter(p -> SafeConverter.toInt(p.getJie()) == jie).collect(Collectors.toList());
                List<Map<String, Object>> clazzMapList = new LinkedList<>();

                Map<String, Object> clazzListMap = new LinkedHashMap<>();
                Map<String, Object> clazzMap0 = new LinkedHashMap<>();
                clazzMap0.put("name", "全部");
                clazzMap0.put("id", 0L);
                clazzMapList.add(clazzMap0);

                Collections.sort(jieClazzList, new Comparator<Clazz>() {
                    @Override
                    public int compare(Clazz x, Clazz y) {
                        String fileA = x.getClassName();
                        String fileB = y.getClassName();
                        char[] arr1 = fileA.toCharArray();
                        char[] arr2 = fileB.toCharArray();
                        int i = 0, j = 0;
                        while (i < arr1.length && j < arr2.length) {
                            if (Character.isDigit(arr1[i]) && Character.isDigit(arr2[j])) {
                                String s1 = "", s2 = "";
                                while (i < arr1.length && Character.isDigit(arr1[i])) {
                                    s1 += arr1[i];
                                    i++;
                                }
                                while (j < arr2.length && Character.isDigit(arr2[j])) {
                                    s2 += arr2[j];
                                    j++;
                                }
                                if (Integer.parseInt(s1) > Integer.parseInt(s2)) {
                                    return 1;
                                }
                                if (Integer.parseInt(s1) < Integer.parseInt(s2)) {
                                    return -1;
                                }
                            } else {
                                if (arr1[i] > arr2[j]) {
                                    return 1;
                                }
                                if (arr1[i] < arr2[j]) {
                                    return -1;
                                }
                                i++;
                                j++;
                            }
                        }
                        if (arr1.length == arr2.length) {
                            return 0;
                        } else {
                            return arr1.length > arr2.length ? 1 : -1;
                        }
                    }
                });
                for (Clazz clazz : jieClazzList) {
                    Map<String, Object> clazzMap = new LinkedHashMap<>();
                    clazzMap.put("name", clazz.getClassName());
                    clazzMap.put("id", clazz.getId());
                    clazzMapList.add(clazzMap);
                }
                clazzListMap.put("grade", grade + "_" + jie0);
                clazzListMap.put("clazzList", clazzMapList);
                gradeClazzList.add(clazzListMap);
                grade++;
            }
        }

        result.add("gradeClazzList", gradeClazzList);
        result.add("result", true);
        return result;
    }

    private void getGradeClazzRel(List<Map<String, String>> gradeList, List<Clazz> clazzList, int jie, List<Map<String, Object>> gradeClazzList) {
        //班级和年级的关系
        for (Map<String, String> temp : gradeList) {
            String name = temp.get("name");
            String value = temp.get("value");
            Map<String, Object> clazzListMap = new LinkedHashMap<>();
            List<Clazz> newSchoolClazzList = clazzList.stream().filter(p -> p.getClazzLevel().getLevel() == SafeConverter.toInt(value)).collect(Collectors.toList());
            List<Map<String, Object>> clazzMapList = new LinkedList<>();
            Map<String, Object> clazzMap0 = new LinkedHashMap<>();
            clazzMap0.put("name", "全部");
            clazzMap0.put("id", 0L);
            clazzMapList.add(clazzMap0);
            Collections.sort(newSchoolClazzList, new Comparator<Clazz>() {
                @Override
                public int compare(Clazz x, Clazz y) {
                    String fileA = x.getClassName();
                    String fileB = y.getClassName();
                    char[] arr1 = fileA.toCharArray();
                    char[] arr2 = fileB.toCharArray();
                    int i = 0, j = 0;
                    while (i < arr1.length && j < arr2.length) {
                        if (Character.isDigit(arr1[i]) && Character.isDigit(arr2[j])) {
                            String s1 = "", s2 = "";
                            while (i < arr1.length && Character.isDigit(arr1[i])) {
                                s1 += arr1[i];
                                i++;
                            }
                            while (j < arr2.length && Character.isDigit(arr2[j])) {
                                s2 += arr2[j];
                                j++;
                            }
                            if (SafeConverter.toInt(s1) > SafeConverter.toInt(s2)) {
                                return 1;
                            }
                            if (SafeConverter.toInt(s1) < SafeConverter.toInt(s2)) {
                                return -1;
                            }
                        } else {
                            if (arr1[i] > arr2[j]) {
                                return 1;
                            }
                            if (arr1[i] < arr2[j]) {
                                return -1;
                            }
                            i++;
                            j++;
                        }
                    }
                    if (arr1.length == arr2.length) {
                        return 0;
                    } else {
                        return arr1.length > arr2.length ? 1 : -1;
                    }
                }
            });
            for (Clazz clazz : newSchoolClazzList) {
                Map<String, Object> clazzMap = new LinkedHashMap<>();
                clazzMap.put("name", clazz.getClassName());
                clazzMap.put("id", clazz.getId());
                clazzMapList.add(clazzMap);
            }
            clazzListMap.put("grade", value + "_" + jie);
            clazzListMap.put("clazzList", clazzMapList);
            gradeClazzList.add(clazzListMap);
        }
    }


    @RequestMapping(value = "loadUnitAvgQuestions.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadUnitAvgQuestions(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            Integer areaId = school.getRegionCode();
            String grade = getRequestString("grade");
            String subject = getRequestString("subject");
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }

            Map<String, Object> data = schoolMasterServiceClient.loadUnitAvgQuestions(areaId, schoolId, subject, grade, clazz, schoolYear, term);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            //legendData
            ExRegion exRegion = raikouSystem.loadRegion(areaId);
            String countyName = exRegion.getCountyName();
            List<String> legendData = new LinkedList<>();
            legendData.add(school.getShortName() + " 人均题数");
            legendData.add(countyName + " 人均题数");
            legendData.add(school.getShortName() + " 正确率");
            legendData.add(countyName + " 正确率");
            result.add("legendData", legendData);
            Set<String> unitNameList = new TreeSet<>();

            Map<String, Double> unitAvgQuestions = (Map<String, Double>) data.get("unitAvgQuestions");
            Map<String, Double> areaUnitAvgQuestions = (Map<String, Double>) data.get("areaUnitAvgQuestions");
            Map<String, Double> unitQuestionRightRatios = (Map<String, Double>) data.get("unitQuestionRightRatio");
            Map<String, Double> areaUnitQuestionRightRatios = (Map<String, Double>) data.get("areaUnitQuestionRightRatio");
            unitNameList.addAll(unitAvgQuestions.keySet());

            String bookId = (String) data.get("bookId");
            List<NewBookCatalog> unitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT)
                    .getOrDefault(bookId, Collections.emptyList()).stream().sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());

            //按照unitList的顺序去排序unitNameList
            List<String> newUnitNameList = new LinkedList<>();
            List<String> unitNames = new LinkedList<>();
            for (NewBookCatalog newBookCatalog : unitList) {
                String unitId = newBookCatalog.getId();
                for (String unitIdandName : unitNameList) {
                    String tempUnitId = unitIdandName.split("-*-")[0];
                    String tempUnitName = unitIdandName.split("-*-")[2];
                    if (unitId.equals(tempUnitId)) {
                        newUnitNameList.add(tempUnitId + "-*-" + tempUnitName);
                        unitNames.add(tempUnitName);
                    }
                }
            }
            result.add("unitNames", unitNames);

            List<Double> unitAvgQuestionsList = new LinkedList<>();
            List<Double> areaUnitAvgQuestionsList = new LinkedList<>();
            List<Double> unitQuestionRightRatioList = new LinkedList<>();
            List<Double> areaUnitQuestionRightRatioList = new LinkedList<>();

            DecimalFormat dfFormat = new DecimalFormat("#.00");
            for (String unitIdAndUnitName : newUnitNameList) {
                Double unitQuestions = unitAvgQuestions.get(unitIdAndUnitName);
                if (unitQuestions == null) {
                    unitQuestions = 0D;
                }
                unitAvgQuestionsList.add(Double.valueOf(dfFormat.format(unitQuestions)));

                Double areaUnitQuestions = areaUnitAvgQuestions.get(unitIdAndUnitName);
                if (areaUnitQuestions == null) {
                    areaUnitQuestions = 0D;
                }
                areaUnitAvgQuestionsList.add(Double.valueOf(dfFormat.format(areaUnitQuestions)));

                Double unitQuestionRightRatio = SafeConverter.toDouble(unitQuestionRightRatios.get(unitIdAndUnitName));
                if (unitQuestionRightRatio == null) {
                    unitQuestionRightRatio = 0D;
                }
                unitQuestionRightRatioList.add(Double.valueOf(dfFormat.format(unitQuestionRightRatio)));

                Double areaUnitQuestionRightRatio = SafeConverter.toDouble(areaUnitQuestionRightRatios.get(unitIdAndUnitName));
                if (areaUnitQuestionRightRatio == null) {
                    areaUnitQuestionRightRatio = 0D;
                }
                areaUnitQuestionRightRatioList.add(Double.valueOf(dfFormat.format(areaUnitQuestionRightRatio)));
            }

            List<Map<String, Object>> seriesData = new LinkedList<>();

            Map<String, Object> unitAvgQuestionsMap = new LinkedHashMap<>();
            unitAvgQuestionsMap.put("name", school.getShortName() + " 人均题数");
            unitAvgQuestionsMap.put("type", "bar");
            unitAvgQuestionsMap.put("data", unitAvgQuestionsList);

            Map<String, Object> areaUnitAvgQuestionsMap = new LinkedHashMap<>();
            areaUnitAvgQuestionsMap.put("name", countyName + " 人均题数");
            areaUnitAvgQuestionsMap.put("type", "bar");
            areaUnitAvgQuestionsMap.put("data", areaUnitAvgQuestionsList);

            Map<String, Object> unitQuestionRightRatioMap = new LinkedHashMap<>();
            unitQuestionRightRatioMap.put("name", school.getShortName() + " 正确率");
            unitQuestionRightRatioMap.put("type", "line");
            unitQuestionRightRatioMap.put("data", unitQuestionRightRatioList);

            Map<String, Object> areaUnitQuestionRightRatioMap = new LinkedHashMap<>();
            areaUnitQuestionRightRatioMap.put("name", countyName + " 正确率");
            areaUnitQuestionRightRatioMap.put("type", "line");
            areaUnitQuestionRightRatioMap.put("data", areaUnitQuestionRightRatioList);

            seriesData.add(unitAvgQuestionsMap);
            seriesData.add(areaUnitAvgQuestionsMap);
            seriesData.add(unitQuestionRightRatioMap);
            seriesData.add(areaUnitQuestionRightRatioMap);
            result.add("seriesData", seriesData);
            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长账号", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadLearningSkills.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadLearningSkills(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            Integer areaId = school.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(areaId);
            String grade = getRequestString("grade");
            String subject = getRequestString("subject");
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }

            Map<String, Object> data = schoolMasterServiceClient.loadLearningSkills(areaId, schoolId, subject, grade, clazz, schoolYear, term);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }

            Map<String, Double> schoolSkillMap = (Map<String, Double>) data.get("schoolSkillRightRates");
            Map<String, Double> areaSkillMap = (Map<String, Double>) data.get("areaSkillRightRates");
            Map<String, Double> nationSkillMap = (Map<String, Double>) data.get("nationSkillRightRates");

            //组织柱状图数据,数学和英语
            List<String> skillNames = null;
            List<Double> schoolSeriesData = new LinkedList<>();
            List<Double> areaSeriesData = new LinkedList<>();
            List<Double> nationSeriesData = new LinkedList<>();
            if (Subject.MATH.name().equals(subject)) {
                skillNames = MathSkill.getAllMathSkillNames();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else if (Subject.ENGLISH.name().equals(subject)) {
                skillNames = EnglishSkill.getAllEnglishSkill();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else {
                skillNames = ChineseSkill.getAllChineseSkillNames();
                getSkillSeriesData(schoolSkillMap, areaSkillMap, nationSkillMap, skillNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            }

            List<String> barLegendData = new LinkedList<>();
            barLegendData.add(school.getShortName());
            barLegendData.add(exRegion.getCountyName());
            barLegendData.add("全国");

            result.add("barSkillData", skillNames);
            result.add("barLegendData", barLegendData);
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            Map<String, Object> schoolSeriesDataMap = new LinkedHashMap<>();
            schoolSeriesDataMap.put("name", school.getShortName());
            schoolSeriesDataMap.put("data", schoolSeriesData);
            barSeriesData.add(schoolSeriesDataMap);

            Map<String, Object> areaSeriesDataMap = new LinkedHashMap<>();
            areaSeriesDataMap.put("name", exRegion.getCountyName());
            areaSeriesDataMap.put("data", areaSeriesData);
            barSeriesData.add(areaSeriesDataMap);

            Map<String, Object> nationSeriesDataMap = new LinkedHashMap<>();
            nationSeriesDataMap.put("name", "全国");
            nationSeriesDataMap.put("data", nationSeriesData);
            barSeriesData.add(nationSeriesDataMap);

            result.add("barSeriesData", barSeriesData);
            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长获取学科能力养成数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    /**
     * 学情分析--学科及知识板块
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "loadKnowledgeModule.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadKnowledgeModule(Model model) {
        MapMessage result = new MapMessage();
        try {
            ResearchStaff researchStaff = currentResearchStaff();
            if (Objects.isNull(researchStaff)) {
                return MapMessage.errorMessage("请重新登录");
            }
            String res = validatePasswdAndMobile(researchStaff.getId());
            if (StringUtils.isNotBlank(res)) {
                return MapMessage.errorMessage("请绑定手机或者重置秘密");
            }
            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            Integer areaId = school.getRegionCode();
            ExRegion exRegion = raikouSystem.loadRegion(areaId);
            String grade = getRequestString("grade");
            String subject = getRequestString("subject");
            String schoolYearTerm = getRequestString("schoolYearTerm"); //17~18下学期
            String schoolYear = schoolYearTerm.split("-")[0];
            String term = schoolYearTerm.split("-")[1];
            String clazz = getRequestString("clazz");
            if (StringUtils.isEmpty(clazz)) {
                clazz = "0";
            }
            String knowledgeModuleLevel = getRequestString("knowledgeModuleLevel");
            Map<String, Object> data = schoolMasterServiceClient.loadKnowledgeModule(areaId, schoolId, subject, grade, clazz, schoolYear, term, knowledgeModuleLevel);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }

            Map<String, Double> schoolKnowledgeRightRates = (Map<String, Double>) data.get("schoolKnowledgeRightRates");
            Map<String, Double> areaKnowledgeRightRates = (Map<String, Double>) data.get("areaKnowledgeRightRates");
            Map<String, Double> nationKnowledgeRightRates = (Map<String, Double>) data.get("nationKnowledgeRightRates");

            //组织柱状图数据,数学和英语
            List<String> knowledgeModuleNames = null;
            List<Double> schoolSeriesData = new LinkedList<>();
            List<Double> areaSeriesData = new LinkedList<>();
            List<Double> nationSeriesData = new LinkedList<>();
            if (Subject.MATH.name().equals(subject)) {
                if (Objects.equals("1", knowledgeModuleLevel)) {
                    knowledgeModuleNames = MathKnowledgeModule.getAllMathKnowledgeModule();
                } else {
                    knowledgeModuleNames = MathSecondKnowledgeModule.getAllMathSecondKnowledgeModule();
                }
                getSkillSeriesData(schoolKnowledgeRightRates, areaKnowledgeRightRates, nationKnowledgeRightRates, knowledgeModuleNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else if (Subject.ENGLISH.name().equals(subject)) {
                if (Objects.equals("1", knowledgeModuleLevel)) {
                    knowledgeModuleNames = EnglishKnowledgeModule.getAllEnglishKnowledgeModule();
                } else {
                    result.add("result", false);
                    result.add("info", "暂无数据");
                    return result;
                }
                getSkillSeriesData(schoolKnowledgeRightRates, areaKnowledgeRightRates, nationKnowledgeRightRates, knowledgeModuleNames, schoolSeriesData, areaSeriesData, nationSeriesData);
            } else {
                //语文暂无
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            List<String> barLegendData = new LinkedList<>();
            barLegendData.add(school.getShortName());
            barLegendData.add(exRegion.getCountyName());
            barLegendData.add("全国");

            result.add("barSkillData", knowledgeModuleNames);
            result.add("barLegendData", barLegendData);
            List<Map<String, Object>> barSeriesData = new LinkedList<>();
            Map<String, Object> schoolSeriesDataMap = new LinkedHashMap<>();
            schoolSeriesDataMap.put("name", school.getShortName());
            schoolSeriesDataMap.put("data", schoolSeriesData);
            barSeriesData.add(schoolSeriesDataMap);

            Map<String, Object> areaSeriesDataMap = new LinkedHashMap<>();
            areaSeriesDataMap.put("name", exRegion.getCountyName());
            areaSeriesDataMap.put("data", areaSeriesData);
            barSeriesData.add(areaSeriesDataMap);

            Map<String, Object> nationSeriesDataMap = new LinkedHashMap<>();
            nationSeriesDataMap.put("name", "全国");
            nationSeriesDataMap.put("data", nationSeriesData);
            barSeriesData.add(nationSeriesDataMap);

            result.add("barSeriesData", barSeriesData);

            result.set("result", true);
        } catch (Exception e) {
            logger.error("校长获取知识板块数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    private List<Integer> getStuSeriesData(Map<String, Integer> usageStudents) {
        List<String> gradeList = getGradeList();
        List<Integer> dataList = new LinkedList<>();
        for (String gradeStr : gradeList) {
            Integer intData = usageStudents.get(gradeStr);
            if (intData != null) {
                dataList.add(intData);
            } else {
                dataList.add(0);
            }
        }
        return dataList;
    }

    private Map<String, List<Integer>> getTeacSeriesData(Map<String, List<Map<String, Integer>>> usageSubjectTeachers) {
        List<Map<String, Integer>> chineseData = usageSubjectTeachers.get(Subject.CHINESE.toString());
        List<Map<String, Integer>> mathData = usageSubjectTeachers.get(Subject.MATH.toString());
        List<Map<String, Integer>> englishData = usageSubjectTeachers.get(Subject.ENGLISH.toString());

        Map<String, List<Integer>> resultMap = new LinkedHashMap<>();
        resultMap.put(Subject.CHINESE.toString(), getDataStr(chineseData));
        resultMap.put(Subject.MATH.toString(), getDataStr(mathData));
        resultMap.put(Subject.ENGLISH.toString(), getDataStr(englishData));
        return resultMap;
    }

    private List<Integer> getDataStr(List<Map<String, Integer>> subjectData) {
        Map<String, Integer> dataMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(subjectData)) {
            for (Map<String, Integer> temp : subjectData) {
                dataMap.putAll(temp);
            }
        }
        List<Integer> dataList = new ArrayList<>();
        List<String> gradeList = getGradeList();
        for (String gradeStr : gradeList) {
            Integer intData = dataMap.get(gradeStr);
            if (intData == null) {
                dataList.add(0);
            } else {
                dataList.add(intData);
            }
        }
        return dataList;
    }


}
