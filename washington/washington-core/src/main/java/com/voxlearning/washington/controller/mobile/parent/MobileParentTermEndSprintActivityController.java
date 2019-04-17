package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=35330793
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=35335324
 * @author jiangpeng
 * @since 2017-12-04 下午12:22
 **/

@Controller
@RequestMapping(value = "/parentMobile/sprint_afenti/")
public class MobileParentTermEndSprintActivityController extends AbstractMobileParentController{


    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "send_reward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReward() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "calendar.vpage ", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage calendar() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "mock.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mockFinish() {
        return activityExpiryMsg;
    }

}
