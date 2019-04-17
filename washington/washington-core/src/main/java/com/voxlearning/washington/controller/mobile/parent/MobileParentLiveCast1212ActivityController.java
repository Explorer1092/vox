package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jiangpeng
 * @since 2017-11-29 上午11:58
 **/
@Controller
@RequestMapping(value = "/parentMobile/livecast1212/")
public class MobileParentLiveCast1212ActivityController extends AbstractMobileParentController {


    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "/draw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage drawLottery() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "/notify_share.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage notifyShare() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/award_history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage prizeList() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "/add_chance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addChance() {
        return activityExpiryMsg;
    }
}
