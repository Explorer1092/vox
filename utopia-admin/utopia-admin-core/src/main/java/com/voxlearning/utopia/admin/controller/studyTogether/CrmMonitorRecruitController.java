package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.service.parent.api.*;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.GroupArea;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroupStudentRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorRecruitStatus;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.MonitorRecruitForm;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.MonitorRecruitVO;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.PassMonitorInfo;
import com.voxlearning.utopia.service.parent.constant.MonitorLevelType;
import com.voxlearning.utopia.service.parent.constant.MonitorRecruitType;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xuerui.zhang
 * @since 2018/6/8 下午5:40
 **/
@Slf4j
@Controller
@RequestMapping("/opmanager/studyTogether")
public class CrmMonitorRecruitController extends AbstractStudyTogetherController {

    @ImportService(interfaceClass = CrmMonitorRecruitService.class)
    private CrmMonitorRecruitService crmMonitorRecruitService;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @ImportService(interfaceClass = MonitorRecruitService.class)
    private MonitorRecruitService monitorRecruitService;
    @ImportService(interfaceClass = MonitorRecruitLoader.class)
    private MonitorRecruitLoader monitorRecruitLoader;
    @ImportService(interfaceClass = MonitorRecruitV2Service.class)
    private MonitorRecruitV2Service monitorRecruitV2Service;


    private StudyLesson getStudyLesson(String lessonId) {
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }

    /**
     * KOL审核后台列表
     */
    @RequestMapping(value = "/recruit_list.vpage", method = RequestMethod.GET)
    public String videoDayList(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        long parentId = getRequestLong("searchParentId");
        String lessonId = getRequestString("selectLessonId");
        List<String> lessonIds = getAllLessonId();
        String statusString = getRequestString("selectStatus");
        int status = SafeConverter.toInt(statusString, 1);
        String wechatGroupName = getRequestString("wechatGroupName");
        String userWechatName = getRequestString("userWechatName");
        String groupId = "";
        if (StringUtils.isNotBlank(wechatGroupName)) {
            StudyGroup group = crmStudyTogetherService.getGroupByGroupNameAndLessonId(wechatGroupName, lessonId);
            if (group != null) {
                groupId = group.getId();
            }
        }
        Page<MonitorRecruitForm> recruitFormPage = crmMonitorRecruitService.getRecruitInfoByLessonAndPage(lessonId, groupId, parentId, userWechatName, status, pageRequest);
        model.addAttribute("lessonIds", lessonIds);
        model.addAttribute("selectLessonId", lessonId);
        model.addAttribute("status", status);
        model.addAttribute("wechatGroupName", wechatGroupName);
        model.addAttribute("userWechatName", userWechatName);
        model.addAttribute("content", recruitFormPage.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", recruitFormPage.getTotalPages());
        model.addAttribute("hasPrev", recruitFormPage.hasPrevious());
        model.addAttribute("hasNext", recruitFormPage.hasNext());

        return "opmanager/studyTogether/monitorRecruit";
    }

    /**
     * 进入招募详情页面
     */
    @RequestMapping(value = "/monitordetails.vpage", method = RequestMethod.GET)
    public String getMonitorDetails(Model model) {
        Long parentId = getRequestLong("parentId");
        if (0L == parentId) {
            return "opmanager/studyTogether/monitorRecruitDetail";
        }
        MonitorRecruitVO recruitInfoByParentId = crmMonitorRecruitService.getRecruitInfoByParentId(parentId);
        if (recruitInfoByParentId == null) {
            return "opmanager/studyTogether/monitorRecruitDetail";
        }
        model.addAttribute("MonitorRecruitInfo", recruitInfoByParentId);
        return "opmanager/studyTogether/monitorRecruitDetail";
    }

    /**
     * 进入招募日志页面
     */
    @RequestMapping(value = "/monitorInfoLog.vpage", method = RequestMethod.GET)
    public String monitorInfoLog(Model model) {
        Long parentId = getRequestLong("parentId");
        if (0L == parentId) {
            return "opmanager/studyTogether/monitorRecruitLog";
        }
        List<KolMonitorStatusRecord> statusListByParentId = crmMonitorRecruitService.getStatusListByParentId(parentId);
        List<KolMonitorStatusRecord> returnList = new ArrayList<>();
        int index = 0;
        int previousStatus = -1;
        for (KolMonitorStatusRecord statusRecord : statusListByParentId) {
            if (index == 0) {
                previousStatus = statusRecord.getRecruitStatus();
                returnList.add(statusRecord);
            } else {
                if (previousStatus == statusRecord.getRecruitStatus()) {
                    continue;
                }
                returnList.add(statusRecord);
                previousStatus = statusRecord.getRecruitStatus();
            }
            index++;
        }
        model.addAttribute("recordList", returnList);
        return "opmanager/studyTogether/monitorRecruitLog";
    }

    /**
     * 更改班长审核状态
     */
    @RequestMapping(value = "/changeMonitorstatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStatus() {
        long parentId = getRequestLong("parentId");
        Integer status = getRequestInt("status");
        KolMonitorStatusRecord monitorRecord = crmMonitorRecruitService.getMonitorRecord(parentId);
        if (monitorRecord == null) {
            return MapMessage.errorMessage("用户状态错误");
        }
        if (StringUtils.isBlank(MonitorRecruitType.getTypeDescById(status))) {
            return MapMessage.errorMessage("传入状态值错误");
        }
        if (MonitorRecruitType.PASS.getTypeId().equals(monitorRecord.getRecruitStatus())
                && MonitorRecruitType.FAIL.getTypeId().equals(status)) {
            return MapMessage.errorMessage("状态错误");
        }
        crmMonitorRecruitService.saveMonitorStatus(parentId, status, Boolean.TRUE);
        return MapMessage.successMessage();
    }

    /**
     * KOL审核通过的管理后台
     */
    @RequestMapping(value = "/pass_monitor_list.vpage", method = RequestMethod.GET)
    public String passMonitorManager(Model model) {
        Integer status = getRequestInt("status", -1);
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String inputWechatId = getRequestString("inputWechatId");
        long searchParentId = getRequestLong("searchParentId");
        String lessonIdSearch = getRequestString("lessonIdSearch");
        String activeLessonIdSearch = getRequestString("activeLessonIdSearch");
        String selectedLessonId = getRequestString("selectedLessonIdSearch");
        Integer level = getRequestInt("level", -1);

        Date current = new Date();
        //获取未结课的课程ID列表
        Map<Long, StudyLesson> notEndingLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream().filter(e -> current.before(e.getCloseDate())).collect(Collectors.toMap(StudyLesson::getLessonId, Function.identity()));

        if (MapUtils.isNotEmpty(notEndingLesson)) {
            List<String> lessonIds = notEndingLesson.values().stream().map(t -> SafeConverter.toString(t.getLessonId())).collect(Collectors.toList());
            model.addAttribute("lessonIds", lessonIds);
        }
        //获取所有课程ID的列表
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("allLessonIds", lessonIds);
        }

        Page<PassMonitorInfo> monitorInfos = crmMonitorRecruitService.getMonitorInfos(selectedLessonId, lessonIdSearch, activeLessonIdSearch, level, inputWechatId, searchParentId, status, pageRequest);

        model.addAttribute("content", monitorInfos.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", monitorInfos.getTotalPages());
        model.addAttribute("hasPrev", monitorInfos.hasPrevious());
        model.addAttribute("hasNext", monitorInfos.hasNext());
        model.addAttribute("level", level);
        model.addAttribute("lessonIdSearch", lessonIdSearch);
        model.addAttribute("activeLessonIdSearch", activeLessonIdSearch);
        model.addAttribute("selectedLessonIdSearch", selectedLessonId);
        model.addAttribute("inputWechatId", inputWechatId);
        model.addAttribute("level", level);
        model.addAttribute("status", status);
        model.addAttribute("searchParentId", searchParentId == 0L ? null : searchParentId);

        return "opmanager/studyTogether/passMonitorManager";
    }

    /**
     * 获取审核通过的修改详情
     */
    @RequestMapping(value = "/passmonitordetails.vpage", method = RequestMethod.GET)
    public String getPassMonitorDetails(Model model) {
        Long parentId = getRequestLong("parentId");
        if (0L == parentId) {
            model.addAttribute("error", "参数错误");
            return "opmanager/studyTogether/passmonitordetails";
        }
        KolMonitorStatusRecord monitorRecord = crmMonitorRecruitService.getMonitorRecord(parentId);
        if (monitorRecord == null) {
            model.addAttribute("error", "未找到管理数据");
            return "opmanager/studyTogether/passmonitordetails";
        }
        MonitorRecruitVO recruitInfoByParentId = crmMonitorRecruitService.getRecruitInfoByParentId(parentId);
        if (recruitInfoByParentId == null) {
            model.addAttribute("error", "未找到申请数据");
            return "opmanager/studyTogether/passmonitordetails";
        }
        List<String> groupIds = monitorRecord.getGroupIds();
        Map<String, List<StudyGroup>> groupListMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groupIds)) {
            Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(groupIds);
            groupListMap = groupMap.values().stream().collect(Collectors.groupingBy(StudyGroup::getLessonId));
        }
//        List<String> lessonIds = monitorRecord.getLessonIds();
        List<StudyGroupStudentRef> studyGroupRefByParentId = crmMonitorRecruitService.getStudyGroupRefByParentId(parentId);
        List<String> unSupportLessonIds = monitorRecruitV2Service.unSupportLessonIds();
        Map<String, String> lessonGroupMap = new HashMap<>();
        studyGroupRefByParentId.stream().filter(e -> !unSupportLessonIds.contains(e.getStudyLessonId())).forEach(e -> {
            Date currentDate = new Date();
            String studyLessonId = e.getStudyLessonId();
            StudyLesson lessonById = getStudyLesson(studyLessonId);
            if (lessonById != null && currentDate.before(lessonById.getCloseDate())) {
                lessonGroupMap.put(SafeConverter.toString(lessonById.getLessonId()), e.getStudyGroupId());
            }
        });
        List<StudyLesson> lessonList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(lessonGroupMap.keySet())) {
            lessonGroupMap.keySet().forEach(e -> {
                StudyLesson lessonById = getStudyLesson(e);
                if (lessonById != null) {
                    lessonList.add(lessonById);
                }
            });
        }
        Map<String, StudyGroup> currentGroupMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(lessonGroupMap.values())) {
            currentGroupMap = crmStudyTogetherService.$getStudyGroupByIds(lessonGroupMap.values());
        }
        Map<String, GroupArea> groupAreaMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(currentGroupMap.values())) {
            currentGroupMap.values().forEach(e -> {
                String groupAreaId = e.getGroupAreaId();
                if (StringUtils.isNotBlank(groupAreaId)) {
                    GroupArea groupAreaById = crmStudyTogetherService.getGroupAreaById(groupAreaId);
                    if (groupAreaById != null) {
                        groupAreaMap.put(e.getId(), groupAreaById);
                    }
                }
            });
        }
        model.addAttribute("parentId", parentId);
        model.addAttribute("recruitInfo", recruitInfoByParentId);
        model.addAttribute("monitorRecord", monitorRecord);
        model.addAttribute("groupListMap", groupListMap);
        model.addAttribute("lessonList", lessonList);
        model.addAttribute("currentGroupMap", currentGroupMap);
        model.addAttribute("groupAreaMap", groupAreaMap);
        model.addAttribute("lessonGroupMap", lessonGroupMap);
        return "opmanager/studyTogether/passmonitordetails";
    }

    /**
     * 获取分配微信群时的状态
     */
    @RequestMapping(value = "/changeWechatGroupStatus.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage changeWechatGroupStatus(Model model) {
        long parentId = getRequestLong("parentId");
        String lessonId = getRequestString("lessonId");
        MonitorRecruitVO recruitInfoByParentId = crmMonitorRecruitService.getRecruitInfoByParentId(parentId);
        if (recruitInfoByParentId == null) {
            return MapMessage.errorMessage("获取信息错误");
        }
        KolMonitorStatusRecord monitorRecord = crmMonitorRecruitService.getMonitorRecord(parentId);
        if (monitorRecord == null) {
            return MapMessage.errorMessage("获取信息错误");
        }
        Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(monitorRecord.getGroupIds());
        List<StudyGroup> studyGroups = groupMap.values().stream().filter(e -> StringUtils.equals(lessonId, e.getLessonId())).collect(Collectors.toList());
        StudyLesson lessonById = getStudyLesson(lessonId);
        if (lessonById == null) {
            return MapMessage.errorMessage("课程信息错误");
        }
        Map<String, Object> lessonMap = new HashMap<>();
        lessonMap.put("_id",lessonById.getLessonId());
        lessonMap.put("title",lessonById.getTitle());
        lessonMap.put("phase",lessonById.getPhase());
        return MapMessage.successMessage()
                .add("recruitInfo", recruitInfoByParentId)
                .add("monitorRecord", monitorRecord)
                .add("lesson", lessonMap)
                .add("studyGroups", studyGroups);
    }


    /**
     * 获取班长、辅导员的修改日志
     */
    @RequestMapping(value = "/getStatusRecords.vpage", method = RequestMethod.GET)
    public String getStatusRecords(Model model) {
        long parentId = getRequestLong("parentId");
        if (parentId == 0L) {
            return "opmanager/studyTogether/monitorstatusrecords";
        }
        List<KolMonitorStatusRecord> statusListByParentId = crmMonitorRecruitService.getStatusListByParentId(parentId);
        if (CollectionUtils.isEmpty(statusListByParentId)) {
            return "opmanager/studyTogether/monitorstatusrecords";
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        statusListByParentId = statusListByParentId.stream().filter(e -> MonitorRecruitType.passVerifyStatus().contains(e.getRecruitStatus())).sorted(Comparator.comparing(KolMonitorStatusRecord::getCreateDate)).collect(Collectors.toList());
        int previousStatus = -1;
        int previousLevel = -1;
        Date previousRestStart = null;
        Date previousRestEnd = null;
        int index = 0;
        for (KolMonitorStatusRecord kolMonitorStatusRecord : statusListByParentId) {
            Map<String, Object> returnMap = new HashMap<>();
            if (index == 0) {
                returnMap.put("recordTime", kolMonitorStatusRecord.getCreateDate());
                returnMap.put("status", MonitorRecruitType.getTypeDescById(kolMonitorStatusRecord.getRecruitStatus()));
                returnMap.put("level", MonitorLevelType.getLevelTypeById(kolMonitorStatusRecord.getLevel()).getDesc());
                returnMap.put("restStart", kolMonitorStatusRecord.getRestStartTime() != null ? kolMonitorStatusRecord.getRestStartTime() : "");
                returnMap.put("restEnd", kolMonitorStatusRecord.getRestStopTime() != null ? kolMonitorStatusRecord.getRestStopTime() : "");
                previousStatus = kolMonitorStatusRecord.getRecruitStatus();
                previousLevel = kolMonitorStatusRecord.getLevel();
                if (kolMonitorStatusRecord.getRestStartTime() != null) {
                    previousRestStart = kolMonitorStatusRecord.getRestStartTime();
                }
                if (kolMonitorStatusRecord.getRestStopTime() != null) {
                    previousRestEnd = kolMonitorStatusRecord.getRestStopTime();
                }
            } else {
                returnMap.put("recordTime", kolMonitorStatusRecord.getCreateDate());
                if (kolMonitorStatusRecord.getRecruitStatus() == previousStatus && kolMonitorStatusRecord.getLevel() == previousLevel) {
                    continue;
                }
                if (kolMonitorStatusRecord.getRecruitStatus() == previousStatus) {
                    returnMap.put("status", MonitorRecruitType.getTypeDescById(kolMonitorStatusRecord.getRecruitStatus()));
                } else {
                    returnMap.put("status", MonitorRecruitType.getTypeDescById(previousStatus) + "-->" + MonitorRecruitType.getTypeDescById(kolMonitorStatusRecord.getRecruitStatus()));
                    previousStatus = kolMonitorStatusRecord.getRecruitStatus();
                }
                if (kolMonitorStatusRecord.getLevel() == previousLevel) {
                    returnMap.put("level", MonitorLevelType.getLevelTypeById(kolMonitorStatusRecord.getLevel()).getDesc());
                } else {
                    returnMap.put("level", MonitorLevelType.getLevelTypeById(previousLevel).getDesc() + "-->" + MonitorLevelType.getLevelTypeById(kolMonitorStatusRecord.getLevel()).getDesc());
                    previousLevel = kolMonitorStatusRecord.getLevel();
                }
                if (kolMonitorStatusRecord.getRestStartTime() != null && !kolMonitorStatusRecord.getRestStartTime().equals(previousRestStart)) {
                    returnMap.put("restStart", kolMonitorStatusRecord.getRestStartTime());
                    previousRestStart = kolMonitorStatusRecord.getRestStartTime();
                }
                if (kolMonitorStatusRecord.getRestStopTime() != null && !kolMonitorStatusRecord.getRestStopTime().equals(previousRestEnd)) {
                    returnMap.put("restEnd", kolMonitorStatusRecord.getRestStopTime());
                    previousRestEnd = kolMonitorStatusRecord.getRestStopTime();
                }
            }
            returnList.add(returnMap);
            index++;
        }
        model.addAttribute("returnList", returnList);
        return "opmanager/studyTogether/monitorstatusrecords";
    }

    /**
     * 获取历史班级管理记录
     */
    @RequestMapping(value = "/getHistoryManageGroupRecords.vpage", method = RequestMethod.GET)
    public String getHistoryManageGroupRecords(Model model) {
        long parentId = getRequestLong("parentId");
        if (parentId == 0L) {
            return "opmanager/studyTogether/monitorHistoryManageRecord";
        }
        Date current = new Date();
        List<KolMonitorStatusRecord> statusListByParentId = crmMonitorRecruitService.getStatusListByParentId(parentId);
        Set<String> groupIds = new HashSet<>();
        Set<String> leaveGroupIds = new HashSet<>();
        for (KolMonitorStatusRecord kolMonitorStatusRecord : statusListByParentId) {
            if (CollectionUtils.isNotEmpty(kolMonitorStatusRecord.getGroupIds())) {
                groupIds.addAll(kolMonitorStatusRecord.getGroupIds());
            }
            if (MonitorRecruitType.LEAVE.getTypeId().equals(kolMonitorStatusRecord.getRecruitStatus()) && CollectionUtils.isNotEmpty(kolMonitorStatusRecord.getGroupIds())) {
                leaveGroupIds.addAll(kolMonitorStatusRecord.getGroupIds());
            }
        }
        Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(groupIds);
        List<StudyGroup> groupList = groupMap.values().stream().filter(e -> {
            if (leaveGroupIds.contains(e.getId())) {
                return true;
            }
            StudyLesson lessonById = getStudyLesson(e.getLessonId());
            if (lessonById == null) {
                return false;
            }
            return current.after(lessonById.getCloseDate());
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(groupList)) {
            return "opmanager/studyTogether/monitorHistoryManageRecord";
        }
        Map<String, List<StudyGroup>> groupListMap = groupList.stream().collect(Collectors.groupingBy(StudyGroup::getLessonId));
        List<StudyLesson> returnLessonList = new ArrayList<>();
        for (String lessonId : groupListMap.keySet()) {
            StudyLesson lessonById = getStudyLesson(lessonId);
            if (lessonById != null) {
                returnLessonList.add(lessonById);
            }
        }
        model.addAttribute("returnLessonList", returnLessonList);
        model.addAttribute("groupListMap", groupListMap);
        return "opmanager/studyTogether/monitorHistoryManageRecord";
    }

    /**
     * 保存班长修改状态
     */
    @ResponseBody
    @RequestMapping(value = "/savebigmonitor.vpage", method = RequestMethod.POST)
    public MapMessage saveBigMonitor() {
        Long parentId = getRequestLong("parentId");
        Integer level = getRequestInt("level", -1);
        String groupIds = getRequestString("groupIds");
        String restStartTime = getRequestString("restStartTime");
        String restEndTime = getRequestString("restEndTime");
        if (0L == parentId) {
            return MapMessage.errorMessage("无效的参数");
        }
        if (-1 == level) {
            return MapMessage.errorMessage("level不能为空");
        }
        List<String> groupIdList = JsonUtils.fromJsonToList(groupIds, String.class);
        if (groupIdList == null) {
            groupIdList = new ArrayList<>();
        }
        KolMonitorStatusRecord kolMonitorStatusRecord = crmMonitorRecruitService.getMonitorRecord(parentId);
        if (kolMonitorStatusRecord == null) {
            return MapMessage.errorMessage("未找到数据");
        }
        Date current = new Date();
        if ((StringUtils.isNotBlank(restStartTime) && kolMonitorStatusRecord.getRestStartTime() == null)
                || (StringUtils.isBlank(restStartTime) && kolMonitorStatusRecord.getRestStartTime() != null)
                || !level.equals(kolMonitorStatusRecord.getLevel())) {
            kolMonitorStatusRecord = new KolMonitorStatusRecord();
            if (StringUtils.isNotBlank(restStartTime) && kolMonitorStatusRecord.getRestStartTime() == null) {
                Date restStart = DateUtils.stringToDate(restStartTime);
                Date restEnd = DateUtils.stringToDate(restEndTime);
                kolMonitorStatusRecord.setRestStartTime(restStart);
                kolMonitorStatusRecord.setRestStopTime(restEnd);
                if (current.after(restStart)) {
                    kolMonitorStatusRecord.setRecruitStatus(MonitorRecruitType.REST.getTypeId());
                } else {
                    kolMonitorStatusRecord.setRecruitStatus(MonitorRecruitType.PASS.getTypeId());
                }
            }
            if ((StringUtils.isBlank(restStartTime) && kolMonitorStatusRecord.getRestStartTime() != null)
                    || (!level.equals(kolMonitorStatusRecord.getLevel()) && !MonitorRecruitType.REST.getTypeId().equals(kolMonitorStatusRecord.getRecruitStatus()))) {
                kolMonitorStatusRecord.setRecruitStatus(MonitorRecruitType.PASS.getTypeId());
            }
        } else if (StringUtils.isNotBlank(restStartTime) && kolMonitorStatusRecord.getRestStartTime() != null) {
            Date restStart = DateUtils.stringToDate(restStartTime);
            Date restEnd = DateUtils.stringToDate(restEndTime);
            kolMonitorStatusRecord.setRestStartTime(restStart);
            kolMonitorStatusRecord.setRestStopTime(restEnd);
        }
//        Boolean compareRecords = compareRecords(monitorRecord, level, groupIdList, restStartTime, restEndTime);
//        if (compareRecords) {
//            return MapMessage.successMessage();
//        }
//        KolMonitorStatusRecord kolMonitorStatusRecord = new KolMonitorStatusRecord();
//        Date restStart;
//        Date restEnd;
        kolMonitorStatusRecord.setParentId(parentId);
        kolMonitorStatusRecord.setLevel(level);
        kolMonitorStatusRecord.setGroupIds(groupIdList);
        if (CollectionUtils.isNotEmpty(groupIdList)) {
            Map<String, StudyGroup> groupMap = crmStudyTogetherService.$getStudyGroupByIds(groupIdList);
            Set<String> lessonIds = new HashSet<>();
            if (MapUtils.isNotEmpty(groupMap)) {
                lessonIds = groupMap.values().stream().filter(Objects::nonNull).map(StudyGroup::getLessonId).collect(Collectors.toSet());
            }
            if (CollectionUtils.isNotEmpty(lessonIds)) {
                kolMonitorStatusRecord.setLessonIds(lessonIds);
            }
        } else {
            kolMonitorStatusRecord.setLessonIds(new HashSet<>());
        }
        crmMonitorRecruitService.saveMonitorInfoRecord(kolMonitorStatusRecord);
        crmMonitorRecruitService.saveMonitorStatus(parentId, kolMonitorStatusRecord.getRecruitStatus(), Boolean.FALSE);
        addAdminLog(getCurrentAdminUser().getAdminUserName() + "保存班长、辅导员状态");
        return MapMessage.successMessage();
    }

    /**
     * 检测输入的微信号是否存在
     */
    @ResponseBody
    @RequestMapping(value = "/check_wechat_name.vpage", method = RequestMethod.GET)
    private MapMessage checkWechatName() {
        String wechatName = getRequestString("wechatName");
        String lessonId = getRequestString("lessonId");
        if (null == wechatName || null == lessonId) {
            return MapMessage.errorMessage("参数错误");
        }
        StudyGroup studyGroup = crmMonitorRecruitService.checkWechatName(wechatName, lessonId);
        if (studyGroup == null) {
            return MapMessage.errorMessage("群名称错误");
        }
        //获取当前班级下学生的数量。之前是给job用，这里拿来做下判断
        Long studentCount = monitorRecruitLoader.loadStudentCountForJob(studyGroup.getId());
        if (RuntimeMode.gt(Mode.TEST) && SafeConverter.toLong(studentCount) < 20) {
            return MapMessage.errorMessage("该班级人数小于20人，不能分配");
        }
        return MapMessage.successMessage().add("group", studyGroup);
    }

    /**
     * 班长招募控制
     */
    @RequestMapping(value = "/monitor_recruit_list.vpage", method = RequestMethod.GET)
    public String monitorRecruitList(Model model) {
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isEmpty(lessonIds)) {
            return "opmanager/studyTogether/monitorRecruitList";
        }
        List<KolMonitorRecruitStatus> monitorRecruitStatuses = crmMonitorRecruitService.$queryAllLessonStatus();
        Map<String, KolMonitorRecruitStatus> statusMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(monitorRecruitStatuses)) {
            statusMap = monitorRecruitStatuses.stream().collect(Collectors.toMap(KolMonitorRecruitStatus::getLessonId, Function.identity()));
        }
        List<String> unSupportLessonIds = monitorRecruitV2Service.unSupportLessonIds();
        List<Map<String, Object>> returnList = new ArrayList<>();
        Date current = new Date();
        Map<String, KolMonitorRecruitStatus> finalStatusMap = statusMap;
        lessonIds.stream().filter(e -> !unSupportLessonIds.contains(e)).forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            StudyLesson lessonById = getStudyLesson(e);
            if (current.before(lessonById.getShowDate()) || current.after(lessonById.getCloseDate())) {
                return;
            }
            map.put("lessonId", lessonById.getLessonId());
            map.put("title", lessonById.getTitle());
            map.put("phase", lessonById.getPhase());
            if (finalStatusMap.get(e) != null) {
                map.put("status", SafeConverter.toBoolean(finalStatusMap.get(e).getIsStop(), Boolean.FALSE));
            } else {
                map.put("status", Boolean.FALSE);
            }
            returnList.add(map);
        });
        model.addAttribute("content", returnList);
        return "opmanager/studyTogether/monitorRecruitList";
    }

    /**
     * 班长招募控制
     */
    @RequestMapping(value = "/save_monitor_recruit_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveMonitorRecruitStatus() {
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("参数错误");
        }
        Boolean isStop = getRequestBool("status");
        KolMonitorRecruitStatus kolMonitorRecruitStatus = crmMonitorRecruitService.saveKolStatus(lessonId, isStop);
        if (kolMonitorRecruitStatus != null) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }


    /**
     * 导出统计数据
     */
    @RequestMapping(value = "/exportMonitorData.vpage", method = RequestMethod.GET)
    public void exportData() throws Exception {
//        List<Integer> status = Arrays.asList(1, 4);
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
//        String inputWechatId = getRequestString("inputWechatId");
//        long searchParentId = getRequestLong("searchParentId");
        String lessonIdSearch = getRequestString("lessonIdSearch");
        String activeLessonIdSearch = getRequestString("activeLessonIdSearch");
        String selectLessonIdSearch = getRequestString("selectLessonIdSearch");
//        //TODO 班级区，级别条件查询
//        String level = getRequestString("level");
//        String classArea = getRequestString("classArea");
        //控制条件，不允许导出全量数据。以免出现导不出来的情况。
        if ((StringUtils.isBlank(lessonIdSearch)
                && StringUtils.isBlank(activeLessonIdSearch) && StringUtils.isBlank(selectLessonIdSearch)) || (StringUtils.isNotBlank(lessonIdSearch)
                && StringUtils.isNotBlank(activeLessonIdSearch) && StringUtils.isNotBlank(selectLessonIdSearch))) {
            return;
        }
        List<PassMonitorInfo> pageList = crmMonitorRecruitService.exportData(lessonIdSearch, activeLessonIdSearch, selectLessonIdSearch);
        if (CollectionUtils.isEmpty(pageList)) {
            return;
        }
        String fileName = "KOL班长管理数据信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> exportData = generateDataList(pageList);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "家长ID", "家长姓名", "微信号",
                    "级别", "管理的课程ID", "激活的课程ID"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
            } catch (Exception e) {
                logger.error("generate newkol data info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download newkol data info error!", e);
            }
        }
    }


    /**
     * 导出统计数据
     */
    @RequestMapping(value = "/exportApplyData.vpage", method = RequestMethod.GET)
    public void exportApplyData() throws Exception {
        String lessonIdSearch = getRequestString("lessonIdSearch");
        String statusString = getRequestString("selectStatus");
        int status = SafeConverter.toInt(statusString, 1);
//        //TODO 班级区，级别条件查询
//        String level = getRequestString("level");
//        String classArea = getRequestString("classArea");
        //控制条件，不允许导出全量数据。以免出现导不出来的情况。
        List<MonitorRecruitForm> recruitFormList = crmMonitorRecruitService.exportApplyData(status, lessonIdSearch);
        if (CollectionUtils.isEmpty(recruitFormList)) {
            return;
        }
        String fileName = "KOL班长招募数据信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> exportData = generateApplyDataList(recruitFormList);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "家长ID", "家长姓名", "课程名称",
                    "期数", "适用年级", "微信号", "学习群号", "申请时间", "审核状态"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000, 5000, 5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
            } catch (Exception e) {
                logger.error("generate newkol data info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download newkol data info error!", e);
            }
        }
    }

    private List<List<String>> generateDataList(List<PassMonitorInfo> exportList) {
        List<List<String>> returnList = new ArrayList<>();
        for (PassMonitorInfo mapper : exportList) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(mapper.getParentId()));
            list.add(SafeConverter.toString(mapper.getParentName()));
            list.add(SafeConverter.toString(mapper.getInputWechatId()));
            list.add(SafeConverter.toString(mapper.getLevel()));
            list.add(SafeConverter.toString(mapper.getLessonIds()));
            list.add(SafeConverter.toString(mapper.getActiveLessonIds()));
            returnList.add(list);
        }
        return returnList;
    }

    private List<List<String>> generateApplyDataList(List<MonitorRecruitForm> recruitFormList) {
        List<List<String>> returnList = new ArrayList<>();
        for (MonitorRecruitForm recruitForm : recruitFormList) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(recruitForm.getParentId()));
            list.add(SafeConverter.toString(recruitForm.getParentName()));
            list.add(SafeConverter.toString(recruitForm.getLessonName()));
            list.add(SafeConverter.toString(recruitForm.getPhase()));
            list.add(SafeConverter.toString(recruitForm.getClazzLevelText()));
            list.add(SafeConverter.toString(recruitForm.getCurrentWechatId()));
            list.add(SafeConverter.toString(recruitForm.getWechatGroupName()));
            list.add(SafeConverter.toString(recruitForm.getCreateDate()));
            list.add(SafeConverter.toString(recruitForm.getStatus()));
            returnList.add(list);
        }
        return returnList;
    }

    private Boolean compareRecords(KolMonitorStatusRecord oldRecord, Integer level, List<String> groupIds, String restStart, String restEnd) {
        if (oldRecord.getGroupIds() == null) {
            oldRecord.setGroupIds(new ArrayList<>());
        }
        if (oldRecord.getLevel().equals(level) && (CollectionUtils.isEqualCollection(oldRecord.getGroupIds(), groupIds))) {
            if (StringUtils.isBlank(restStart) && StringUtils.isBlank(restEnd) && oldRecord.getRestStartTime() == null && oldRecord.getRestStopTime() == null) {
                return Boolean.TRUE;
            } else if (StringUtils.isNotBlank(restStart) && StringUtils.isNotBlank(restEnd) && oldRecord.getRestStartTime() != null && oldRecord.getRestStopTime() != null) {
                Date start = DateUtils.stringToDate(restStart);
                Date end = DateUtils.stringToDate(restEnd);
                if (oldRecord.getRestStartTime().equals(start) && oldRecord.getRestStopTime().equals(end)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}
