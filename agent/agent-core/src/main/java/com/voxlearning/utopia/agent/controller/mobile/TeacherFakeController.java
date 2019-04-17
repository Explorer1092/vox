package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.TeacherFakeService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jia HuanYin
 * @since 2015/12/2
 */
@Controller
@RequestMapping(value = "/mobile/teacher_fake")
public class TeacherFakeController extends AbstractAgentController {

    @Inject
    TeacherFakeService teacherFakeService;
    @Inject
    private TeacherResourceService teacherResourceService;

//    @RequestMapping(value = "fake_teacher.vpage", method = RequestMethod.GET)
//    public String fakeTeacher(Model model) {
//        Long teacherId = requestLong("teacherId");
//        boolean is17ActiveTeacher = teacherFakeService.isActiveTeacher(teacherId);//先将就这样写吧，不清楚快乐学模式是否有活跃度这个校验提醒 及 活跃度是否和 一起作业 是否一样
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//        model.addAttribute("isKLXTeacher", teacher.isKLXTeacher());
//        model.addAttribute("is17ActiveTeacher", is17ActiveTeacher);
//        model.addAttribute("teacherId", teacherId);
//        return "mobile/teacher_fake/fake_teacher";
//    }

    @RequestMapping(value = "fake_teacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("ca0e0a9053204636")
    public MapMessage fakeTeacher() {
        Long teacherId = requestLong("teacherId");
        String fakeNote = requestString("fakeNote");
        AuthCurrentUser currentUser = getCurrentUser();
        if (teacherId == null){
            return MapMessage.errorMessage("老师ID不能为空");
        }
        boolean is17ActiveTeacher = teacherFakeService.isActiveTeacher(teacherId);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null){
            return MapMessage.errorMessage("老师不存在");
        }
        //公私海场景，判断该用户是否有权限，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(currentUser.getUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        boolean isKLXTeacher = teacher.isKLXTeacher();
        //一起作业模式 活跃老师需要走审批流程
        boolean needApprove = !isKLXTeacher && is17ActiveTeacher;
        return doTeacherFake(teacherId, fakeNote, currentUser, needApprove);
    }

    @RequestMapping(value = "fake_teacher_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fakeTeacherList() {
        Set<Long> teacherIds = requestLongSet("teacherIds");
        String fakeNote = "批量判假";
        AuthCurrentUser currentUser = getCurrentUser();
        if (CollectionUtils.isEmpty(teacherIds)) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        String resultInfo = "批量判假完成，本次共判假" + teacherIds.size() + "位老师";
        List<String> approveTeacherNames = new ArrayList<>();
        teacherIds.forEach(p -> {
            boolean is17ActiveTeacher = teacherFakeService.isActiveTeacher(p);
            Teacher teacher = teacherLoaderClient.loadTeacher(p);
            boolean isKLXTeacher = teacher.isKLXTeacher();
            //一起作业模式 活跃老师需要走审批流程
            boolean needApprove = !isKLXTeacher && is17ActiveTeacher;
            MapMessage mapMessage = doTeacherFake(p, fakeNote, currentUser, needApprove);
            if (mapMessage.isSuccess() && needApprove) {
                approveTeacherNames.add(teacher.fetchRealname());
            }
        });
        if (approveTeacherNames.size() > 0) {
            resultInfo += "，" + StringUtils.join(approveTeacherNames, "、") + "为活跃老师需风控人工审核。";
        }
        return MapMessage.successMessage(resultInfo);
    }


    private MapMessage doTeacherFake(Long teacherId, String fakeNote, AuthCurrentUser currentUser, boolean needApprove) {
        try {
            MapMessage msg;
            if (needApprove) {//一起作业模式 活跃老师需要走审批流程
                msg = teacherFakeService.fakeActiveTeacher(teacherId, fakeNote, currentUser);
            } else {
                msg = teacherFakeService.fakeInactiveTeacher(teacherId, fakeNote, currentUser);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Failed fake teacher, teacher={}, user={}", teacherId, currentUser.getUserId());
            return MapMessage.errorMessage("老师判假失败, 原因:" + ex.getMessage());
        }
    }

    @RequestMapping(value = "fake_teachers.vpage")
    public String fakeTeachers(Model model) {
        Long fakerId = getCurrentUserId();
        Map<String, List<CrmTeacherFake>> fakeTeachers = teacherFakeService.fakeTeachers(fakerId);
        model.addAttribute("fakeTeachers", fakeTeachers);
//        return "mobile/teacher_fake/fake_teachers";
        return "rebuildViewDir/mobile/school/fake_teachers";
    }
}
