package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by jiang wei on 2017/3/2.
 * 点读机预习者联盟活动
 */
@Controller
@RequestMapping(value = "/usermobile/yx")
@Slf4j
public class MobilePicYxActivityController extends AbstractMobileController {

    /**
     * 获取当前学生参加活动的状态
     */
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        return activityExpiryMsg;
    }


    /**
     * 唤醒页，查看当前组的同学
     */
    @RequestMapping(value = "/groupIndex.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupIndex() {
        return activityExpiryMsg;
    }

    /**
     * 唤醒页，查看当前组的同学的活动参与情况
     */
    @RequestMapping(value = "/groupStudentDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupStudentActivityDetail() {
        return activityExpiryMsg;
    }

    /**
     * 获取奖励提醒
     */
    @RequestMapping(value = "/getGroupRewardNoticeInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupRewardNoticeInfo() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/addRecordByStudentId.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addRecordByStudentId() {
        return activityExpiryMsg;
    }

}
