package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.CANVASS_VOTE_END_DATE;

@Controller
@RequestMapping("/courseware/canvass")
public class TeacherCourseCanvassController extends AbstractController {

    @Inject private TeacherCoursewareContestServiceClient coursewareContestServiceClient;

    @RequestMapping(value = "list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage canvassList(){
        String subject = getRequestString("subject");
        if (StringUtils.isBlank(subject)) {
            subject = "CHINESE";
        }

        String keyword = getRequestString("keyword");
        String sort = getRequestString("sort");
        if (StringUtils.isBlank(sort)) {
            sort = "default";
        }

        TeacherDetail teacherDetail = null;
        String openId = getRequestContext().getAuthenticatedOpenId();
        User curUser = currentUser();
        if (curUser != null && curUser.isTeacher()) {
            teacherDetail = currentTeacherDetail();
        }

        MapMessage canvassResult = coursewareContestServiceClient.loadUserCanvassInfo(subject, teacherDetail, openId);
        if (!canvassResult.isSuccess()) {
            return canvassResult;
        }

        // do filter and sort
        List<Map<String, Object>> canvassData = (List<Map<String, Object>>) canvassResult.get("canvassData");
        if (StringUtils.isNoneBlank(keyword)) {
            canvassData = canvassData.stream()
                    .filter(p -> (SafeConverter.toString(p.get("title")).toLowerCase().contains(keyword.toLowerCase()))
                            || (SafeConverter.toString(p.get("teacherName")).toLowerCase().equals(keyword.toLowerCase()))
                            || (StringUtils.equals(SafeConverter.toString(p.get("teacherName")), keyword)))
                    .collect(Collectors.toList());
        }

        if ("default".equals(sort)) {
            Collections.sort(canvassData, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))));
        } else {
            Collections.sort(canvassData, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("totalCanvassNum")), SafeConverter.toInt(o1.get("totalCanvassNum"))));
        }

        return MapMessage.successMessage()
                .add("wechatLogined", StringUtils.isNotEmpty(openId))
                .add("userLogined", teacherDetail != null)
                .add("auth", teacherDetail != null && Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState()))
                .add("totalSurplus", canvassResult.get("totalSurplus"))
                .add("canvassData", canvassData)
                .add("leftTime", (CANVASS_VOTE_END_DATE.getTime() - System.currentTimeMillis()));
    }

    // 投票操作
    @RequestMapping(value = "vote.vpage",method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage canvassVote() {
        String courseId = getRequestString("courseId");
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("错误的课件ID");
        }

        TeacherCourseware courseware = coursewareContestServiceClient.loadTeacherCoursewareById(courseId);
        if (courseware == null) {
            return MapMessage.errorMessage("错误的课件ID");
        }

        if (new Date().getTime() > CANVASS_VOTE_END_DATE.getTime()) {
            return MapMessage.errorMessage("投票已结束");
        }

        User curUser = currentUser();
        String openId = getRequestContext().getAuthenticatedOpenId();

        if (curUser != null && curUser.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            return coursewareContestServiceClient.canvassVote(teacherDetail, courseId, courseware.getTeacherId());
        } else if (StringUtils.isNotEmpty(openId)) {
            return coursewareContestServiceClient.canvassVote(openId, courseId, courseware.getTeacherId());
        }

        return MapMessage.errorMessage("未登录");
    }

    @RequestMapping(value = "canvass.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage canvass() {
        String courseId = getRequestString("courseId");
        if (StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("错误的课件ID");
        }

        if (new Date().getTime() > CANVASS_VOTE_END_DATE.getTime()) {
            return MapMessage.errorMessage("投票已结束");
        }

        User curUser = currentUser();
        String openId = getRequestContext().getAuthenticatedOpenId();

        if (curUser != null && curUser.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            return coursewareContestServiceClient.canvassHelper(courseId, teacherDetail.getId());
        } else if (StringUtils.isNotEmpty(openId)) {
            return coursewareContestServiceClient.canvassHelper(courseId, openId);
        }
        return MapMessage.errorMessage("未登录");
    }

}
