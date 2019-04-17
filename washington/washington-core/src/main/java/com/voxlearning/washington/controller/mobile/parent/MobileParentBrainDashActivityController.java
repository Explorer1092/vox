package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jiangpeng
 * @since 2018-01-25 上午11:14
 **/
@Controller
@RequestMapping(value = "/parentMobile/brain_dash")
public class MobileParentBrainDashActivityController extends AbstractMobileParentController {

    @RequestMapping(value = "all_bingo_sids.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage allBingoSidList() {
        return MapMessage.errorMessage();
    }


    @RequestMapping(value = "data_you_want.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage exportData() {
        return MapMessage.errorMessage();
    }

    /**
     * 还是哪个接口  type -1你告诉我总数   type为其他int数 你就保存即可  怎么样 unique
     * @return
     */
    @RequestMapping(value = "online_counter.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage onlineCounter() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "activity_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activities() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "timeline.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage timeline() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "submit.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage submit() {
        return MapMessage.errorMessage();
    }


    @RequestMapping(value = "answer_result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage answerResult() {
        return MapMessage.errorMessage();
    }


    @RequestMapping(value = "final_result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage finalResult() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "time_out.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage timeOut() {
        return MapMessage.errorMessage();
    }


    @RequestMapping(value = "student_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentInfo() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage join() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "join_status.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinStatus() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "join_next.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinNext() {
        return MapMessage.errorMessage();
    }


    @RequestMapping(value = "student_index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage studentIndex() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "range.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllRange() {
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "preheat.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage preHeat() {
        return MapMessage.errorMessage();
    }

}
