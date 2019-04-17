package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.studyplanning.api.data.StudyPlanningItemMapper;
import com.voxlearning.utopia.api.constant.WarmHeartPlanConstant;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanPointService;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.washington.controller.mobile.teacher.AbstractMobileTeacherController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping(value = "/parentMobile/warm/heart/plan")
@Slf4j
public class MobileParentWarmHeartPlanController extends AbstractMobileTeacherController {

    @ImportService(interfaceClass = WarmHeartPlanService.class)
    private WarmHeartPlanService warmHeartPlanService;

    @ImportService(interfaceClass = WarmHeartPlanPointService.class)
    private WarmHeartPlanPointService warmHeartPlanPointService;

    /**
     * 活动首页
     *
     * @return
     */
    @RequestMapping(value = "index.vpage")
    @ResponseBody
    public MapMessage index() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return noLoginResult;
        }

        Long studentId = getRequestLong("sid");

        return warmHeartPlanService.loadStudentStatus(user.getId(), studentId);
    }

    /**
     * 目标制定页
     *
     * @return
     */
    @RequestMapping(value = "warm_heart_targets.vpage")
    @ResponseBody
    public MapMessage warmHeartTargets() {
        User user = currentUser();
        Long studentId = getRequestLong("sid");
        if (user == null || !user.isParent()) {
            return noLoginResult;
        }

        return warmHeartPlanService.warmHeartTargets(user.getId(), user.getProfile().getImgUrl(), studentId);
    }

    /**
     * 制定目标
     *
     * @return
     */
    @RequestMapping(value = "formulate_target.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage formulateTarget() {
        User user = currentUser();
        if (user == null || !user.isParent()) {
            return noLoginResult;
        }

        long studentId = getRequestLong("sid");
        String plans = getRequestString("plans");

        try {
            studentId = checkParams(user, studentId, plans);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        }

        MapMessage mapMessage = warmHeartPlanService.saveWarmHeartPlans(studentId, plans);
        if (mapMessage.getSuccess())
            warmHeartPlanPointService.parentAssign(user.getId(), studentId);
        return mapMessage;
    }


    private long checkParams(User user, long studentId, String plans) {
        List<StudyPlanningItemMapper> itemMappers = JsonUtils.fromJsonToList(plans, StudyPlanningItemMapper.class);

        if (CollectionUtils.isEmpty(itemMappers)) {
            throw new IllegalArgumentException("最少设置1个目标");
        }

        if (itemMappers.size() > WarmHeartPlanConstant.MAX_TARGET_NUM) {
            throw new IllegalArgumentException("最多只可设置3个目标");
        }

        for (StudyPlanningItemMapper itemMapper : itemMappers) {

            if (StringUtils.isEmpty(itemMapper.getType())) {
                throw new IllegalArgumentException("目标类型不能为空");
            }

            if (StringUtils.isEmpty(itemMapper.getPlanningName())) {
                throw new IllegalArgumentException("目标名不能为空");
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

    private static final Set<String> typeSet = new HashSet<>();

    static {
        typeSet.add("PICLISTEN");           //点读机
        typeSet.add("DUBBING");             //趣味配音
        typeSet.add("READING_ENGLISH");     //绘本warm_heart_targets.vpage
    }
}


