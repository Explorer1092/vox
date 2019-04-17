package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.utopia.service.campaign.api.ParentNewTermPlanService;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanPointService;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanService;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping(value = "/parentMobile/new/term/plan")
public class MobileParentNewTermPlanController extends AbstractMobileParentController {

    private static final MapMessage NO_LOGIN_MSG = MapMessage.errorMessage("未登录").add("code", 201);

    private static final int MAX_TARGET_NUM = 3;

    @Inject
    private ParentLoaderClient parentLoaderClient;
    @ImportService(interfaceClass = ParentNewTermPlanService.class)
    private ParentNewTermPlanService parentNewTermPlanService;

    @ImportService(interfaceClass = TeacherNewTermPlanPointService.class)
    private TeacherNewTermPlanPointService teacherNewTermPlanPointService;

    @ImportService(interfaceClass = TeacherNewTermPlanService.class)
    private TeacherNewTermPlanService teacherNewTermPlanService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @RequestMapping(value = "formulate_target.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage formulateTarget() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return NO_LOGIN_MSG;
        }

        long studentId = getRequestLong("studentId");
        String plans = getRequestString("plans");

        try {
            studentId = checkParams(user, studentId, plans);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }

        parentNewTermPlanService.saveNewTermActivityPlans(studentId, plans);

        teacherNewTermPlanPointService.parentAssign(user.getId(), studentId);

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        String dateFormat = DateFormatUtils.format(new Date(), "yyyy/MM/dd");
        return MapMessage.successMessage().add("studentName", studentDetail.fetchRealname()).add("date", dateFormat)
                .add("participate_count", teacherNewTermPlanService.getParticipateCount());
    }

    @RequestMapping(value = "check_ref.vpage")
    @ResponseBody
    public MapMessage checkRef() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return NO_LOGIN_MSG;
        }

        long studentId = getRequestLong("studentId");
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
        for (StudentParentRef studentParentRef : studentParentRefs) {
            if (Objects.equals(studentParentRef.getStudentId(), studentId)) {
                return MapMessage.successMessage();
            }
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "sign_up.vpage")
    @ResponseBody
    public MapMessage signUp() {
        User user = currentUser();
        if (user == null || (!user.isParent())) {
            return noLoginResult;
        }
        return parentNewTermPlanService.signUp(user.getId());
    }

    private long checkParams(User user, long studentId, String plans) {
        List<StudyPlanningItemMapper> itemMappers = JsonUtils.fromJsonToList(plans, StudyPlanningItemMapper.class);

        if (CollectionUtils.isEmpty(itemMappers)) {
            throw new IllegalArgumentException("最少设置1个目标");
        }

        if (itemMappers.size() > MAX_TARGET_NUM) {
            throw new IllegalArgumentException("最多只可设置3个目标");
        }

        for (StudyPlanningItemMapper itemMapper : itemMappers) {
            if (StringUtils.isEmpty(itemMapper.getPlanningName())) {
                throw new IllegalArgumentException("目标名和目标详情都不能为空");
            }

            boolean valaditeTime = (StringUtils.isNotEmpty(itemMapper.getConfigStartTime()) &&
                    StringUtils.isNotEmpty(itemMapper.getConfigEndTime())) || StringUtils.isNotEmpty(itemMapper.getQuantum());

            if (!valaditeTime) {
                throw new IllegalArgumentException("目标必须有时间");
            }
        }

        if (studentId == 0L) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                throw new IllegalArgumentException("必须选定一个孩子指定目标");
            }
            studentId = studentParentRefs.get(0).getStudentId();
        }

        return studentId;
    }

}
