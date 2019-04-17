package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * @author jiangpeng
 * @since 2017-10-31 上午11:25
 **/
@Controller
@RequestMapping(value = "/parentMobile/21days/")
public class MobileParent21DaysActivityController extends AbstractMobileParentController {

    private MapMessage activityEndMsg = MapMessage.errorMessage("活动已结束咯！");


    @RequestMapping(value = "join_status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinStatus() {
        return activityEndMsg;
    }



    @RequestMapping(value = "do_join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage doJoin() {

        return activityEndMsg;
    }



    @RequestMapping(value = "/lottery_index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lotteryIndex() {
        return activityEndMsg;
    }


    @RequestMapping(value = "/draw_lottery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage drawLottery() {

        return activityEndMsg;
    }


    @RequestMapping(value = "/prize_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage prizeList() {

        return activityEndMsg;
    }




    @RequestMapping(value = "/study_result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studyResult() {

        return activityEndMsg;
    }

    @RequestMapping(value = "/range.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage range() {

        return activityEndMsg;
    }





    @RequestMapping(value = "/parent_reward/send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendParentReward() {

        return activityEndMsg;
    }



    @RequestMapping(value = "/today_integral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getIntegral() {

        return activityEndMsg;
    }


    //正式活动接口
    @RequestMapping(value = "go_afenti.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage goAfenti() {

        return activityEndMsg;
    }

    //正式活动接口
    @RequestMapping(value = "data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexData() {

        return activityEndMsg;
    }


}
