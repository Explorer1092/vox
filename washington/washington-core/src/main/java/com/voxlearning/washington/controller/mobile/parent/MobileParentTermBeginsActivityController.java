package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/22
 */
@Controller
@RequestMapping(value = "/parentMobile/term_begins/")
public class MobileParentTermBeginsActivityController extends AbstractMobileParentController {

    /**
     * 预热活动-首页
     */
    @RequestMapping(value = "join_status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinStatus() {
        return activityExpiryMsg;
    }

    /**
     * 预热活动-报名
     */
    @RequestMapping(value = "do_join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doJoin() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-日历、连续奖励
     */
    @RequestMapping(value = "get_data_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getDataList() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-打卡动态
     */
    @RequestMapping(value = "get_trends.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTrends() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-任务状态
     */
    @RequestMapping(value = "get_clock_status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClockStatus() {
        return activityExpiryMsg;
    }


    /**
     * 正式活动-分享页信息
     */
    @RequestMapping(value = "get_share_page_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSharePageInfo() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-分享打卡
     */
    @RequestMapping(value = "record_clock.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recordClock() {
        return activityExpiryMsg;
    }


    /**
     * 正式活动-当天任务完成后领奖
     */
    @RequestMapping(value = "send_today_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTodayReward() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-连续打卡发奖
     */
    @RequestMapping(value = "send_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReward() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-点赞
     */
    @RequestMapping(value = "do_like.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doLike() {
        return activityExpiryMsg;
    }

    /**
     * 正式活动-学校排行榜
     */
    @RequestMapping(value = "school_rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolRank() {
        return activityExpiryMsg;
    }

    /**
     * 测试后门接口
     */
    @RequestMapping(value = "back_door.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage backDoor() {
        return activityExpiryMsg;

    }

}
