package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitV2Service;
import com.voxlearning.utopia.service.parent.api.StudyTogetherService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.GroupArea;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.MonitorRecruitV2;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorRecruitStatus;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.parent.constant.MonitorLevelType;
import com.voxlearning.utopia.service.parent.constant.MonitorRecruitType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/7/25
 */
@Log4j
@Controller
@RequestMapping(value = "/parentMobile/monitor_recruit/v2")
public class MobileParentKOLV2Controller extends AbstractMobileParentStudyTogetherController {

    @ImportService(interfaceClass = MonitorRecruitV2Service.class)
    private MonitorRecruitV2Service monitorRecruitV2Service;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @ImportService(interfaceClass = StudyTogetherService.class)
    private StudyTogetherService studyTogetherService;


    /**
     * 获取招募状态
     */
    @RequestMapping(value = "/getRecruitStatus.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRecruitStatus() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        Set<String> activeLessonIds = checkParentStudentJoinOrActiveLessonIds(parent.getId());
        if (CollectionUtils.isEmpty(activeLessonIds)) {
            return MapMessage.successMessage().add("status", MonitorRecruitType.NOT_JOIN.getTypeId());
        }
        if (checkUserLessonRecruitStop(activeLessonIds)) {
            return MapMessage.successMessage().add("status", MonitorRecruitType.JOIN_OVER.getTypeId());
        }
        MonitorRecruitV2 monitorRecruit = monitorRecruitV2Service.getMonitorApplyRecord(parent.getId());
        //新申请
        if (monitorRecruit == null) {
            return MapMessage.successMessage().add("status", MonitorRecruitType.NEW_RECRUIT.getTypeId());
        }
        //未通过、离职
        if (monitorRecruit.getStatus().equals(MonitorRecruitType.LEAVE.getTypeId()) || monitorRecruit.getStatus().equals(MonitorRecruitType.FAIL.getTypeId())) {
            if (checkParentLeaveAndFailStatus(parent.getId(), monitorRecruit.getLessonId())) {
                return MapMessage.successMessage().add("status", MonitorRecruitType.NEW_RECRUIT.getTypeId());
            } else {
                return MapMessage.successMessage().add("status", monitorRecruit.getStatus());
            }
        }
        return MapMessage.successMessage().add("status", monitorRecruit.getStatus());
    }


    /**
     * 选择孩子
     */
    @RequestMapping(value = "/getChildList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getChildList() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(students)) {
            return MapMessage.errorMessage("未绑定孩子");
        }
        Date current = new Date();
        students = students.stream().filter(e -> {
            List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(e.getId());
            if (CollectionUtils.isEmpty(studyGroups)) {
                return false;
            }
            Map<Long, StudyLesson> studyLessons = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer()
                    .getStudyLessons(studyGroups.stream().map(x -> SafeConverter.toLong(x.getLessonId())).collect(Collectors.toList()));
            return CollectionUtils.isNotEmpty(studyLessons
                    .values().stream().filter(p -> current.before(p.getCloseDate())).collect(Collectors.toList()));
        }).collect(Collectors.toList());


        List<Map<String, Object>> returnList = new ArrayList<>();
        for (User student : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("sid", student.getId());
            map.put("name", student.fetchRealnameIfBlankId());
            map.put("img", getUserAvatarImgUrl(student));
            returnList.add(map);
        }
        return MapMessage.successMessage().add("student_list", returnList);
    }

    /**
     * 选择课程
     */
    @RequestMapping(value = "/getLessonList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLessonList() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        long sid = getRequestLong("sid");
        List<String> recruitStopLesson = getRecruitStopLesson();
        List<StudyGroup> selectableLessonList = getSelectableLessonList(parent.getId(), sid);
        List<Map<String, Object>> returnList = new ArrayList<>();
        selectableLessonList.forEach(v -> {
            StudyLesson studyLesson = getStudyLesson(v.getLessonId());
            if (studyLesson == null) {
                return;
            }
            // 不显示公众号激活的课程
            if (studyLesson.safeIsActiveByOfficialAccount() || studyLesson.safeIsDirectActive()) {
                return;
            }
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("sid", sid);
            returnMap.put("lesson_id", v.getLessonId());
            returnMap.put("title", studyLesson.getTitle());
            returnMap.put("clazz_level", studyLesson.getSuitableGradeText());
            returnMap.put("phase", studyLesson.getPhase());
            returnMap.put("group_id", v.getId());
            if (recruitStopLesson.contains(v.getLessonId())) {
                returnMap.put("is_over", Boolean.TRUE);
            } else {
                returnMap.put("is_over", Boolean.FALSE);
            }
            returnList.add(returnMap);

        });
        return MapMessage.successMessage().add("lesson_list", returnList);
    }

    /**
     * 保存信息
     */
    @RequestMapping(value = "/saveInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveInfo() {
        User parent = currentParent();
        if (parent == null) {
            return go2LoginPageResult;
        }
        String birthday = getRequestString("birthday");
        String inputWechatId = getRequestString("input_wechat_id");
        String province = getRequestString("province");
        String city = getRequestString("city");
        String profession = getRequestString("profession");
        String time = getRequestString("time");
        String education = getRequestString("education");
        String advantage = getRequestString("advantage");
        String character = getRequestString("character");
        String idea = getRequestString("idea");
        String lessonId = getRequestString("lesson_id");
        Long studentId = getRequestLong("student_id");
        String groupId = getRequestString("group_id");

        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(students)) {
            return MapMessage.errorMessage("您还未绑定孩子");
        }
        Set<Long> sids = students.stream().map(User::getId).collect(Collectors.toSet());
        if (!sids.contains(studentId)) {
            return MapMessage.errorMessage("绑定关系错误");
        }
        MonitorRecruitV2 monitorRecruit = new MonitorRecruitV2();
        monitorRecruit.setParentId(parent.getId());
        monitorRecruit.setBirthday(birthday);
        monitorRecruit.setInputWechatId(inputWechatId);
        monitorRecruit.setProvince(province);
        monitorRecruit.setCity(city);
        monitorRecruit.setProfession(profession);
        monitorRecruit.setTime(time);
        monitorRecruit.setEducation(education);
        monitorRecruit.setAdvantage(advantage);
        monitorRecruit.setCharacter(character);
        monitorRecruit.setIdea(idea);
        monitorRecruit.setStatus(MonitorRecruitType.IN_RECRUIT.getTypeId());
        monitorRecruit.setLessonId(lessonId);
        monitorRecruit.setStudentId(studentId);
        monitorRecruit.setGroupId(groupId);

        MonitorRecruitV2 applyRecord = AtomicLockManager.getInstance().wrapAtomic(monitorRecruitV2Service)
                .keyPrefix("saveApplyRecord")
                .keys(parent.getId())
                .proxy()
                .saveApplyRecord(monitorRecruit);
        if (applyRecord != null) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    /**
     * 班级管理接口
     */
    @RequestMapping(value = "/manageGroup.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage manageGroup() {
        User parent = currentParent();
        if (parent == null) {
            return noLoginResult;
        }
        MonitorRecruitV2 monitorApplyRecord = monitorRecruitV2Service.getMonitorApplyRecord(parent.getId());
        if (monitorApplyRecord == null) {
            return MapMessage.errorMessage("还未申请哦~");
        }
        if (MonitorRecruitType.IN_RECRUIT.getTypeId().equals(monitorApplyRecord.getStatus())) {
            return MapMessage.errorMessage("审核中哦~");
        }
        KolMonitorStatusRecord parentLatestMonitorRecord = monitorRecruitV2Service.getParentLatestMonitorRecord(parent.getId());
        MapMessage mapMessage = MapMessage.successMessage();
        KolMonitorStatusRecord passStatusByParentId = monitorRecruitV2Service.getLatestPassStatusByParentId(parent.getId());
        //拼用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", parent.getId());
        userInfo.put("student_id", SafeConverter.toLong(monitorApplyRecord.getStudentId()));
        userInfo.put("name", getParentCallName(monitorApplyRecord.getParentId(), monitorApplyRecord.getStudentId()));
        if (parentLatestMonitorRecord != null) {
            if (parentLatestMonitorRecord.getLevel() != null) {
                userInfo.put("level", MonitorLevelType.getLevelTypeById(parentLatestMonitorRecord.getLevel()).getDesc());
            }
            mapMessage.add("status", parentLatestMonitorRecord.getRecruitStatus());
        }
        if (passStatusByParentId != null) {
            userInfo.put("become_monitor_day", DateUtils.dayDiff(new Date(), passStatusByParentId.getCreateDate()));
        }
        userInfo.put("img", getUserAvatarImgUrl(parent));
        mapMessage.add("user_info", userInfo);

        //拼管理的班级信息
        List<Map<String, Object>> returnList = new ArrayList<>();
        int groupNum = 0;
        if (parentLatestMonitorRecord != null && CollectionUtils.isNotEmpty(parentLatestMonitorRecord.getGroupIds())) {
            Map<String, StudyGroup> studyGroupMap = studyTogetherService.loadStudyGroupByIds(parentLatestMonitorRecord.getGroupIds());
            Map<String, List<StudyGroup>> studyGroupByLesson = studyGroupMap.values().stream().collect(Collectors.groupingBy(StudyGroup::getLessonId));
            Set<String> notFinishLessonIds = getNotFinishLessonIds(studyGroupByLesson.keySet());
            groupNum = SafeConverter.toInt(studyGroupMap.values().stream().filter(e -> notFinishLessonIds.contains(e.getLessonId())).count());
            if (groupNum > 0) {
                studyGroupByLesson.forEach((k, v) -> {
                    if (!notFinishLessonIds.contains(k)) {
                        return;
                    }
                    Map<String, Object> returnMap = new HashMap<>();
                    returnMap.put("lesson_id", k);
                    returnMap.put("title", getStudyLesson(k).getTitle());
                    List<Map<String, Object>> groupInfo = new ArrayList<>();
                    for (StudyGroup studyGroup : v) {
                        Map<String, Object> returnGroup = new HashMap<>();
                        returnGroup.put("group_id", studyGroup.getId());
                        returnGroup.put("name", studyGroup.getWechatGroupName());
                        if (StringUtils.isNotBlank(studyGroup.getGroupAreaId())) {
                            List<GroupArea> groupAreaByIds = studyTogetherServiceClient.getStudyTogetherBuffer().getGroupAreaByIds(Collections.singleton(studyGroup.getGroupAreaId()));
                            if (CollectionUtils.isNotEmpty(groupAreaByIds)) {
                                GroupArea groupArea = groupAreaByIds.get(0);
                                returnGroup.put("group_area_id", groupArea.getGroupAreaName());
                            }
                        }
                        groupInfo.add(returnGroup);
                    }
                    returnMap.put("group_info", groupInfo);
                    returnList.add(returnMap);
                });
            }
        }
        userInfo.put("group_num", groupNum);
        mapMessage.add("lesson_group_info", returnList);
        if (parentLatestMonitorRecord != null
                && parentLatestMonitorRecord.getRecruitStatus() != null
                && MonitorRecruitType.REST.getTypeId().equals(parentLatestMonitorRecord.getRecruitStatus())) {
            String startTime = DateUtils.dateToString(parentLatestMonitorRecord.getRestStartTime(), "yyyy.MM.dd");
            String stopTime = DateUtils.dateToString(parentLatestMonitorRecord.getRestStopTime(), "yyyy.MM.dd");
            String restTime = startTime + "-" + stopTime;
            mapMessage.add("rest_time", restTime);
        }
        return mapMessage;
    }

    private Set<String> checkParentStudentJoinOrActiveLessonIds(Long parentId) {
//        Map<String, ParentJoinLessonRef> lessonRefMap = studyTogetherServiceClient.loadParentJoinLessonRefs(parentId);
//        if (MapUtils.isEmpty(lessonRefMap)) {
//            return Collections.emptySet();
//        }
        List<User> students = studentLoaderClient.loadParentStudents(parentId);
        Set<String> lessonIds = new HashSet<>();
        Date current = new Date();
        for (User student : students) {
            List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(student.getId());
            if (CollectionUtils.isNotEmpty(studyGroups)) {
                studyGroups = studyGroups.stream().filter(e -> {
                    StudyLesson studyLesson = getStudyLesson(e.getLessonId());
                    if (studyLesson != null) {
                        return current.before(studyLesson.getCloseDate());
                    } else {
                        return false;
                    }
                }).collect(Collectors.toList());
                lessonIds.addAll(studyGroups.stream().map(StudyGroup::getLessonId).collect(Collectors.toSet()));
            }
        }
        return lessonIds;
    }

    private Boolean checkParentLeaveAndFailStatus(Long parentId, String lessonId) {
        Date current = new Date();
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (current.before(studyLesson.getCloseDate())) {
            return Boolean.FALSE;
        }
        Set<String> lessonIds = checkParentStudentJoinOrActiveLessonIds(parentId);
        lessonIds = lessonIds.stream().filter(e -> !StringUtils.equals(e, lessonId)).collect(Collectors.toSet());
        return CollectionUtils.isNotEmpty(lessonIds);
    }


    private List<StudyGroup> getSelectableLessonList(Long parentId, Long studentId) {
        //先拿申请记录
        Date currentDate = new Date();
        MonitorRecruitV2 monitorApplyStatus = monitorRecruitV2Service.getMonitorApplyRecord(parentId);
        String filterLesson = "";
        if (monitorApplyStatus != null && (monitorApplyStatus.getStatus().equals(MonitorRecruitType.LEAVE.getTypeId()) || monitorApplyStatus.getStatus().equals(MonitorRecruitType.FAIL.getTypeId()))) {
            filterLesson = monitorApplyStatus.getLessonId();
        }
        Set<String> lessonIds = new HashSet<>();
        List<StudyGroup> studyGroups = studyTogetherServiceClient.loadStudentActiveLessonGroups(studentId);
        if (CollectionUtils.isNotEmpty(studyGroups)) {
            String finalFilterLesson = filterLesson;
            List<String> activeLessonIds = studyGroups.stream().map(StudyGroup::getLessonId).collect(Collectors.toList());
            List<String> unsupportLessonIds = monitorRecruitV2Service.unSupportLessonIds();
            List<String> openLessons = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer()
                    .getStudyLessons(activeLessonIds.stream().map(SafeConverter::toLong).collect(Collectors.toList()))
                    .values()
                    .stream()
                    .filter(e -> currentDate.before(e.getCloseDate()))
                    .map(x -> SafeConverter.toString(x.getLessonId()))
                    .collect(Collectors.toList());
            studyGroups = studyGroups.stream()
                    .filter(e -> lessonIds.add(e.getLessonId())
                            && !StringUtils.equals(finalFilterLesson, e.getLessonId())
                            && openLessons.contains(e.getLessonId())
                            && !unsupportLessonIds.contains(e.getLessonId()))
                    .collect(Collectors.toList());
        }
        return studyGroups;
    }

    private String getParentCallName(Long parentId, Long studentId) {
        List<StudentParent> studentParents = parentLoaderClient.getParentLoader().loadStudentParents(studentId);
        if (CollectionUtils.isNotEmpty(studentParents)) {
            StudentParent studentParent = studentParents.stream().filter(p -> parentId.equals(p.getParentUser().getId())).findFirst().orElse(null);
            if (studentParent != null) {
                Student student = studentLoaderClient.loadStudent(studentId);
                if (null != student) {
                    return student.fetchRealname() + studentParent.getCallName();
                }
            }
        }
        return "";
    }


    private List<String> getRecruitStopLesson() {
        List<KolMonitorRecruitStatus> kolStatus = studyTogetherServiceClient.getStudyTogetherBuffer().getKolStatus();
        if (CollectionUtils.isEmpty(kolStatus)) {
            return Collections.emptyList();
        }
        return kolStatus.stream().filter(KolMonitorRecruitStatus::getIsStop).map(KolMonitorRecruitStatus::getLessonId).collect(Collectors.toList());
    }


    private Boolean checkUserLessonRecruitStop(Set<String> lessonIds) {
        if (CollectionUtils.isEmpty(lessonIds)) {
            return Boolean.TRUE;
        }
        Date currentDate = new Date();
        Set<String> userStartLesson = new HashSet<>();
        List<String> recruitStopLesson = getRecruitStopLesson();
        for (String lessonId : lessonIds) {
            StudyLesson studyLesson = getStudyLesson(lessonId);
            if (studyLesson != null && currentDate.before(studyLesson.getCloseDate()) && !recruitStopLesson.contains(lessonId)) {
                userStartLesson.add(lessonId);
            }
        }
        return CollectionUtils.isEmpty(userStartLesson);
    }

    private Set<String> getNotFinishLessonIds(Collection<String> lessonIds) {
        if (CollectionUtils.isEmpty(lessonIds)) {
            return Collections.emptySet();
        }
        Date current = new Date();
        Map<Long, StudyLesson> lessonMap = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLessons(lessonIds.stream().map(SafeConverter::toLong).collect(Collectors.toList()));
        return lessonMap.values().stream().filter(e -> DateUtils.dayDiff(current, e.getCloseDate()) <= 7).map(x -> SafeConverter.toString(x.getLessonId())).collect(Collectors.toSet());
    }
}
