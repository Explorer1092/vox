package com.voxlearning.washington.controller.mobile.student.fairyland;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.CourseStatus;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.LiveType;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author fugui.chang
 * @since 2017/7/11
 */
@Controller
@RequestMapping("/student/fairyland/babyeagle")
public class StudentFairylandBabyEagleController extends AbstractApiController {

    @Inject
    private BabyEagleLoaderClient babyEagleLoaderClient;
    @Inject
    private BabyEagleServiceClient babyEagleServiceClient;

    @RequestMapping(value = "courses.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courses() {
        User studentUser = currentStudent();
        if (studentUser == null) {
            return WonderlandResult.ErrorType.COMMON_MSG.result("数据异常,请重试");
        }
        try {
            return babyEagleLoaderClient.getBabyEagleLoader().fetchStudentPlayingCourseInfo(studentUser.getId()).getUninterruptibly();
        } catch (Exception ignore) {
            return WonderlandResult.ErrorType.COMMON_MSG.result("数据异常,请重试");
        }
    }

    @RequestMapping(value = "courseaddress.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseAddress() {
        User studentUser = currentStudent();
        if (studentUser == null || studentUser.getId() == null) {
            MapMessage.errorMessage("请登录后使用");
        }

        try {
            String courseId = getRequestString("courseId");
            if (StringUtils.isBlank(courseId)) {
                return WonderlandResult.ErrorType.COMMON_MSG.result("参数错误!");
            }
            MapMessage mapMessage = babyEagleServiceClient.getRemoteReference().getCoursePlayResource(studentUser.getId(), courseId, LiveType.TalkFun);
            String status = (String) mapMessage.get("status");
            String playUrl = (String) mapMessage.get("playUrl");
            if (StringUtils.equals(status, CourseStatus.PLAY.name()) && StringUtils.isNotBlank(playUrl)) {
                return MapMessage.successMessage().add("playUrl", playUrl);
            } else {
                return WonderlandResult.ErrorType.COMMON_MSG.result("暂无直播课程");
            }
        } catch (Exception ignore) {
            return WonderlandResult.ErrorType.COMMON_MSG.result("数据异常,请重试");
        }
    }
}
