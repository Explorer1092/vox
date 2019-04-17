package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jiangpeng
 * @since 2018-01-15 下午2:27
 **/
@Controller
@RequestMapping(value = "/parentMobile/winter_practice/")
public class MobileParentWinterPracticeActivityController extends AbstractMobileParentController {

    @RequestMapping(value = "join_status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinStatus() {
        return activityExpiryMsg;
    }



    @RequestMapping(value = "do_join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doJoin() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/lottery_index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lotteryIndex() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/draw_lottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage drawLottery() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/prize_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage prizeList() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/study_result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyResult() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "/rank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage range() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/today_integral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getIntegral() {
        return activityExpiryMsg;

    }


    //正式活动接口
    @RequestMapping(value = "go_afenti.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage goAfenti() {
        return activityExpiryMsg;

    }

    //正式活动接口
    @RequestMapping(value = "data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexData() {
        return activityExpiryMsg;
    }

    //正式活动接口
    @RequestMapping(value = "certificate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage certificate() {
        return activityExpiryMsg;
    }



    @RequestMapping(value = "/mockData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mock() {
        return activityExpiryMsg;
    }
}
