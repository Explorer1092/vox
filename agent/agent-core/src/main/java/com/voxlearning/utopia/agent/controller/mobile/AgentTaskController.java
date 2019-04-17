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

package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskDetailService;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceMapperService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.api.constant.AgentTaskStatus;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCSchoolTask;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/26
 */
@Controller
@RequestMapping(value = "/mobile/task")
public class AgentTaskController extends AbstractAgentController {

    @Inject private AgentResourceMapperService agentResourceMapperService;
    @Inject private AgentTaskDetailService agentTaskDetailService;
    @Inject private AgentTaskService agentTaskService;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private TeacherResourceService teacherResourceService;

    @RequestMapping(value = "dispatch_ugc_school_task.vpage")
    @ResponseBody
    public CrmUGCSchoolTask dispatchUGCSchoolTask() {
        Long schoolId = requestLong("schoolId");
        String creater = requestString("creater");
        String createrName = requestString("createrName");
        Boolean branchSchool = getRequestBool("branchSchool");
        if (!checkTaskPushSign(schoolId)) {
            logger.warn("Fail to checkTaskPushSign with schoolId = {}", schoolId);
            return null;
        }
        return agentTaskService.dispatchUGCSchoolTask(schoolId, creater, createrName, branchSchool);
    }

    public boolean checkTaskPushSign(Long schoolId) {
        Long timestamp = requestLong("timestamp");
        String sign = requestString("sign");
        if (schoolId == null || timestamp == null || StringUtils.isBlank(sign)) {
            return false;
        }
        String iSign = taskPushSign(String.valueOf(schoolId), timestamp);
        return sign.equals(iSign);
    }


    public static String taskPushSign(String key, long timestamp) {
        return DigestUtils.md5Hex(key + Constants.FLOW_TASK_PUSH_SECRET + timestamp);
    }

    /** 任务中心 **/

    /**
     * 转校
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "change_school_page.vpage")
    public String createChangeSchoolTaskPage(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        Boolean choiceTeacherAble = getRequestBool("choiceTeacherAble");
        if (schoolId > 0) {
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(schoolId)
                    .getUninterruptibly();
            model.addAttribute("school", school);
        }
        List<Map<String, Object>> clazzList = agentResourceMapperService.generateTeacherClazzDataFromGroup(teacherId);
        Teacher teacherInfo = teacherLoaderClient.loadTeacher(teacherId);
        if (teacherInfo != null && choiceTeacherAble) {
            model.addAttribute("teacherName", teacherInfo.getProfile() == null ? null : teacherInfo.getProfile().getRealname());
        }
        model.addAttribute("choiceTeacherAble", choiceTeacherAble);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("clazzList", clazzList);
        return "rebuildViewDir/mobile/resource/change_school";
    }

//    @RequestMapping(value = "check_schoolId.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage checkSchoolId() {
//        Long schoolId = getRequestLong("schoolId");
//        return baseOrgService.filterUserManageSchoolId(getCurrentUserId(), schoolId, "space");
//    }

    /**
     * 新建班级
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "create_class_page.vpage")
    public String createCreateClassTaskPage(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Boolean choiceTeacherAble = getRequestBool("choiceTeacherAble");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher != null && choiceTeacherAble) {
            model.addAttribute("teacherName", teacher.getProfile() == null ? null : teacher.getProfile().getRealname());
        }
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("choiceTeacherAble", choiceTeacherAble);
        return "rebuildViewDir/mobile/resource/new_clazz";
    }

    /**
     * 绑定解绑手机
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "bind_mobile_page.vpage")
    public String createBindMobileTaskPage(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Boolean choiceTeacherAble = getRequestBool("choiceTeacherAble");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher != null && choiceTeacherAble) {
            model.addAttribute("teacherName", teacher.getProfile() == null ? null : teacher.getProfile().getRealname());
        }

        String phone = sensitiveUserDataServiceClient.showUserMobile(teacherId, "agent:createBindMobileTaskPage", getCurrentUser().getUserName());
        if (StringUtils.isNoneBlank(phone)) {
            model.addAttribute("mobile", phone);
        }
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("choiceTeacherAble", choiceTeacherAble);
        return "rebuildViewDir/mobile/resource/bind_mobile";

    }

    /**
     * 转校提交
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "teacher_change_school_task.vpage", method = RequestMethod.POST)
    @OperationCode("6f79b39c06b34e96")
    public MapMessage createChangeSchoolTask() {
        AuthCurrentUser currentUser = getCurrentUser();
        Long teacherId = getRequestLong("teacherId");
        Long targetSchoolId = getRequestLong("targetSchoolId");
        boolean includeClazz = getRequestBool("includeClazz");
        Set<Long> classIds = requestLongSet("clazzIds");
        String comment = getRequestString("comment");
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(currentUser.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return agentTaskService.createTeacherChangeSchoolTask(currentUser, teacherId, targetSchoolId, includeClazz, classIds, comment, true);
    }

    /**
     * 新建班级提交
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "teacher_create_clazz_task.vpage", method = RequestMethod.POST)
    @OperationCode("6f79b39c06b34e96")
    public MapMessage createCreateClassTask() {
        AuthCurrentUser currentUser = getCurrentUser();
        Long teacherId = getRequestLong("teacherId");
        String clazzNames = getRequestString("clazzNames");
        String comment = getRequestString("comment");
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(currentUser.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        List<String> classNameList = Arrays.asList(StringUtils.split(clazzNames, ",")).stream().filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(classNameList)) {
            clazzNames = StringUtils.join(classNameList, ",");
            agentTaskService.createTeacherCreateClazzTask(currentUser, teacherId, clazzNames, comment, true);
        }
        return MapMessage.successMessage();
    }

    /**
     * 绑定解绑手机提交
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "teacher_bind_mobile_task.vpage", method = RequestMethod.POST)
    @OperationCode("6f79b39c06b34e96")
    public MapMessage createBindMobileTask() {
        AuthCurrentUser currentUser = getCurrentUser();
        Long teacherId = getRequestLong("teacherId");
        String unbindMobile = getRequestString("unbindMobile");
        String bindMobile = getRequestString("bindMobile");
        String comment = getRequestString("comment");
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(currentUser.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        if (StringUtils.isNotBlank(unbindMobile) || StringUtils.isNotBlank(bindMobile)) {
            agentTaskService.createTeacherBindMobileTask(currentUser, teacherId, unbindMobile, bindMobile, comment, true);
        }

        return MapMessage.successMessage();
    }

    // 客服协助记录
    @RequestMapping(value = "task_list.vpage")
    public String taskList(Model model) {

        AuthCurrentUser currentUser = getCurrentUser();
        List<AgentTaskDetail> taskDetailList = agentTaskDetailService.findByUserId(currentUser.getUserId());

        List<AgentTaskDetail> followingList = taskDetailList.stream().filter(p -> !p.isExpired() && AgentTaskStatus.FOLLOWING == p.getStatus()).collect(Collectors.toList());
        List<AgentTaskDetail> finishedList = taskDetailList.stream().filter(p -> !p.isExpired() && AgentTaskStatus.FINISHED == p.getStatus()).collect(Collectors.toList());
        List<AgentTaskDetail> expiredList = taskDetailList.stream().filter(p -> p.isExpired()).collect(Collectors.toList());

        model.addAttribute("followingList", agentTaskService.setComposedDataWithTaskRecord(followingList));
        model.addAttribute("finishedList", agentTaskService.setComposedDataWithTaskRecord(finishedList));
        model.addAttribute("expiredList", agentTaskService.setComposedDataWithTaskRecord(expiredList));
        return "rebuildViewDir/mobile/resource/cusList";
    }


    @ResponseBody
    @RequestMapping(value = "finish_task_detail.vpage", method = RequestMethod.POST)
    public MapMessage finishTask() {
        String taskDetailId = getRequestString("taskDetailId");
        // 更新任务状态
        boolean result = agentTaskDetailService.updateStatus(taskDetailId, AgentTaskStatus.FINISHED);

        if (!result) {
            return MapMessage.errorMessage("操作失败");
        }
        return MapMessage.successMessage();
    }

    // 客服协助记录
    @RequestMapping(value = "task_list_new.vpage")
    @ResponseBody
    public MapMessage taskListNew() {
        MapMessage mapMessage = MapMessage.successMessage();
        Integer taskStatus = getRequestInt("taskStatus");
        AuthCurrentUser currentUser = getCurrentUser();
        List<AgentTaskDetail> taskDetailList = agentTaskDetailService.findByUserId(currentUser.getUserId()).stream().filter(p-> p.getCreateTime() != null && p.getCreateTime().after(DateUtils.addDays(new Date(),-180))).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        List<AgentTaskDetail> list = new ArrayList<>();
        //跟东伟确认不要过期判断了
        switch (taskStatus){
            case 1://客服处理中
                list= taskDetailList.stream().filter(p ->  AgentTaskStatus.FOLLOWING == p.getStatus()).collect(Collectors.toList());
                break;
            case 2://已反馈
                list= taskDetailList.stream().filter(p -> AgentTaskStatus.WAITER_FEEDBACK == p.getStatus()).collect(Collectors.toList());
                break;
            case 3://已完成
                list= taskDetailList.stream().filter(p -> AgentTaskStatus.FINISHED == p.getStatus()).collect(Collectors.toList());
                break;
//            case 4://已过期
//                list= taskDetailList.stream().filter(p -> p.isExpired()).collect(Collectors.toList());
//                break;
            default:
                list = taskDetailList;
                break;
        }
        mapMessage.put("dataList",agentTaskService.setComposedDataWithTaskRecordNew(list));
        return mapMessage;
    }

    //客服任务详情
    @ResponseBody
    @RequestMapping(value = "task_info.vpage", method = RequestMethod.POST)
    public MapMessage taskInfo() {
        String taskDetailId = getRequestString("taskDetailId");
        if (StringUtils.isBlank(taskDetailId)) {
            return MapMessage.errorMessage("taskDetailId不能为空");
        }
        return agentTaskDetailService.findTaskDetailInfo(taskDetailId);
    }
    //查询老师对应客服工单
    @ResponseBody
    @RequestMapping(value = "teacher_task_list.vpage", method = RequestMethod.POST)
    public MapMessage teacherTaskList() {
        Long teacherId = getRequestLong("teacherId");
        return MapMessage.successMessage().add("dataList",agentTaskDetailService.findTeacherTaskDetailList(teacherId));

    }
}
