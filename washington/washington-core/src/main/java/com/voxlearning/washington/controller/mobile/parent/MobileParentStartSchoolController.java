package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author shiwei.liao
 * @since 2017-8-7
 */
@Controller
@Slf4j
@RequestMapping(value = "/parentMobile/parent/start_school")
public class MobileParentStartSchoolController extends AbstractMobileParentController {


    @RequestMapping(value = "mission_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMissionList() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "mission_statistics.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMissionStatistics() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "mission_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMissionDetail() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "finish_mission.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishMission() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "create_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createParentReward() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "send_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendParentReward() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "send_privilege.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendPrivilege() {
        return activityExpiryMsg;
    }

}
