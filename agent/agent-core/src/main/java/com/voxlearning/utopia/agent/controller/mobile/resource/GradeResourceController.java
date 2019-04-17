package com.voxlearning.utopia.agent.controller.mobile.resource;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.resource.GradeResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.view.grade.Grade17InfoView;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 年级班级相关接口
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Controller
@RequestMapping("/mobile/resource/grade")
public class GradeResourceController extends AbstractAgentController {

    @Inject
    private GradeResourceService gradeResourceService;
    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private AsyncGroupServiceClient asyncGroupServiceClient;

    @Inject private RaikouSDK raikouSDK;

    /**
     * 年级列表
     *
     * @return
     */
    @RequestMapping(value = "grade_class_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gradeClassInfo() {
        Long schoolId = getRequestLong("schoolId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline 3：家长
        if (mode != 1 && mode != 2 && mode != 3) {
            mode = 1;
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }

        MapMessage message = MapMessage.successMessage();
        message.put("schoolId", schoolId);
        message.put("mode", mode);
        List<Map<String, Object>> gradeList = new ArrayList<>();
        if (mode == 1 || mode == 3) {
            List<Object> dataList = gradeResourceService.generateGradeClassInfo(school, mode);
            dataList.forEach(item -> {
                Map<String, Object> gradeMap = new HashMap<>();
                Grade17InfoView grade17InfoView = (Grade17InfoView) item;
                gradeMap.put("grade", grade17InfoView.getGrade());
                gradeMap.put("gradeName", grade17InfoView.getGradeName());
                gradeList.add(gradeMap);
            });
            message.put("dataList", dataList);
        } else {
            Map<Integer, List<Object>> gradeClassInfoMap = gradeResourceService.generateGradeClassInfoOffline(school);
            gradeClassInfoMap.keySet().forEach(item -> {
                Map<String, Object> gradeMap = new HashMap<>();
                gradeMap.put("grade", item);
                gradeMap.put("gradeName", item + "年级");
                gradeList.add(gradeMap);
            });
            message.put("dataMap", gradeClassInfoMap);
        }
        message.put("gradeList", gradeList);
        return message;
    }

    @RequestMapping(value = "need_update_grade_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage needUpdateGradeList() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }
        MapMessage message = MapMessage.successMessage();
        // 获取学校的年级分布
        List<ClazzLevel> schoolGradeList = gradeResourceService.getSchoolGradeList(school);
        //需要更新的年级列表
        List<Integer> needUpdGradeList = gradeResourceService.judgeGradeClassCount(school);
        message.put("needUpdGradeList", needUpdGradeList);
        List<String> needUpdGradeNameList = schoolGradeList.stream().filter(p -> needUpdGradeList.contains(p.getLevel())).map(p -> p.getDescription()).collect(Collectors.toList());
        message.put("needUpdGradeNameList", needUpdGradeNameList);
        return message;
    }

    /**
     * 年级柱状图
     *
     * @return
     */
    @RequestMapping(value = "grade_chart_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gradeChartInfo() {
        Long schoolId = getRequestLong("schoolId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        if (mode != 1 && mode != 2) {
            mode = 1;
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }
        MapMessage message = MapMessage.successMessage();
        Map<String, Object> gradeChartData = gradeResourceService.generateGradeChartInfo(school, mode);
        message.put("grade_info", gradeChartData);
        return message;
    }


    /**
     * 班级详情
     *
     * @return
     */
    @RequestMapping(value = "class_detail_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage classDetailInfo() {
        Long classId = getRequestLong("classId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        if (mode != 1 && mode != 2) {
            mode = 1;
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(classId);
        if (clazz == null || clazz.isDisabledTrue() || clazz.getClazzLevel() == ClazzLevel.INFANT_GRADUATED || clazz.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || clazz.getClazzLevel() == ClazzLevel.MIDDLE_GRADUATED) {
            return MapMessage.errorMessage("班级ID有误！");
        }
        MapMessage message = MapMessage.successMessage();
        message.put("mode", mode);
        Map<String, Object> classInfoMap = gradeResourceService.generateClassDetailInfo(clazz, mode);
        message.put("classInfo", classInfoMap);
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();
        if (school != null) {
            message.put("schoolLevel", SchoolLevel.safeParse(school.getLevel()));
        }
        return message;
    }

    /**
     * 班级老师列表信息
     *
     * @return
     */
    @RequestMapping(value = "class_teacher_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage classTeacherList() {
        Long classId = getRequestLong("classId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        if (mode != 1 && mode != 2) {
            mode = 1;
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(classId);
        if (clazz == null || clazz.isDisabledTrue() || clazz.getClazzLevel() == ClazzLevel.INFANT_GRADUATED || clazz.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || clazz.getClazzLevel() == ClazzLevel.MIDDLE_GRADUATED) {
            return MapMessage.errorMessage("班级ID有误！");
        }
        MapMessage mapMessage = gradeResourceService.generateClassTeacherList(classId, mode);
        mapMessage.put("mode", mode);
        return mapMessage;
    }

    /**
     * 班级班组列表
     *
     * @return
     */
    @RequestMapping(value = "class_group_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage classGroupList() {
        Long classId = getRequestLong("classId");
        return MapMessage.successMessage().add("groupList", asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzId(classId).getUninterruptibly());
    }

    /**
     * 班组老师学生列表
     *
     * @return
     */
    @RequestMapping(value = "group_teacher_student_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupTeacherStudentInfo() {
        Long groupId = getRequestLong("groupId");

        return MapMessage.successMessage().add("dataMap", gradeResourceService.groupTeacherStudentInfo(groupId));
    }

    // 班级详情
    @RequestMapping(value = "class_student_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage classStudentInfo() {
        Long classId = getRequestLong("classId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        if (mode != 1 && mode != 2) {
            mode = 1;
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(classId);
        if (clazz == null || clazz.isDisabledTrue() || clazz.getClazzLevel() == ClazzLevel.INFANT_GRADUATED || clazz.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || clazz.getClazzLevel() == ClazzLevel.MIDDLE_GRADUATED) {
            return MapMessage.errorMessage("班级ID有误！");
        }

        MapMessage message = MapMessage.successMessage();
        message.put("mode", mode);
        Map<String, Object> classStudentInfo = gradeResourceService.generateClassStudentInfo(clazz, mode);
        message.put("classStudentInfo", classStudentInfo);
        return message;
    }

    /**
     * 需要更新的年级信息
     *
     * @return
     */
    @RequestMapping(value = "need_update_grade_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage needUpdateGradeInfo() {
        Long schoolId = getRequestLong("schoolId");
        String needUpdGradeJsonStr = getRequestString("needUpdGradeJsonStr");//需要更新的年级
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("更新年级信息未找到学校信息");
        }
        if (StringUtils.isBlank(needUpdGradeJsonStr)) {
            return MapMessage.errorMessage("参数异常");
        }
        List<Integer> needUpdGradeList = JsonUtils.fromJsonToList(needUpdGradeJsonStr, Integer.class);
        //获取学校的年级
        List<SchoolGradeBasicData> schoolGradeBasicDataList = schoolResourceService.generateGradeBasicDataList(schoolId);
        //过滤出需要更新的年级
        schoolGradeBasicDataList = schoolGradeBasicDataList.stream().filter(p -> needUpdGradeList.contains(p.getGrade())).collect(Collectors.toList());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("schoolId", school.getId());
        dataMap.put("schoolName", school.getCmainName());
        dataMap.put("gradeDataList", schoolGradeBasicDataList);
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    /**
     * 更新年级信息
     *
     * @return
     */
    @RequestMapping(value = "update_grade_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateGradeInfo() {
        Long schoolId = getRequestLong("schoolId");
        String gradeDataJson = getRequestString("gradeDataJson");
        List<SchoolGradeBasicData> gradeDataList = JsonUtils.fromJsonToList(gradeDataJson, SchoolGradeBasicData.class);
        return schoolResourceService.updateSchoolGradeData(schoolId, gradeDataList);
    }

    /**
     * 家长年级柱状图
     *
     * @return
     */
    @RequestMapping(value = "parent_grade_chart_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentGradeChartInfo() {
        Long schoolId = getRequestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校不存在！");
        }
        MapMessage message = MapMessage.successMessage();
        Map<String, Object> gradeChartData = gradeResourceService.generateParentGradeChartInfo(school);
        message.put("grade_info", gradeChartData);
        return message;
    }
}
