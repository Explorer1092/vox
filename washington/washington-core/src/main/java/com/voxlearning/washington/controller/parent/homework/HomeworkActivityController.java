package com.voxlearning.washington.controller.parent.homework;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserActivity;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 作业活动api
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Controller
@RequestMapping("/parent/homework/activity")
public class HomeworkActivityController extends AbstractController {

    @ImportService(interfaceClass = HomeworkActivityService.class)
    private HomeworkActivityService activityService;
    @ImportService(interfaceClass = HomeworkUserActivityService.class)
    private HomeworkUserActivityService userActivityService;
    private static final String ACTIVITY_ID = "20190303_OCR_MENTAL_ARITHMETIC";

    //Logic
    /**
     * 报名参加活动
     *
     * @return
     */
    @RequestMapping(value = "/join.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage join(Long studentId, @RequestParam(required = false, defaultValue = ACTIVITY_ID) String activityId) {
        UserActivity userActivity = new UserActivity();
        userActivity.setActivityId(activityId);
        userActivity.setStudentId(studentId);
        userActivity.setParentId(currentUserId());
        return userActivityService.join(userActivity);
    }

    /**
     * 获取用户活动信息
     *
     * @param activityId 活动id
     * @param studentId 学生id
     * @return
     */
    @RequestMapping(value = "/get.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage get(Long studentId, @RequestParam(required = false, defaultValue = ACTIVITY_ID) String activityId) {
        return userActivityService.load(studentId, activityId);
    }

}
