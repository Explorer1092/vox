package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileTeacherDayController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jiangpeng
 * @since 16/8/24
 * 活动已下线 留着入口防止404
 */
@Controller
@RequestMapping("/parentMobile/teacherDay")
@NoArgsConstructor
@Slf4j
public class MobileParentTeacherDayController extends AbstractMobileTeacherDayController {


    @RequestMapping(value = "/bless/load.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadBlessByStudent() {
       return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }


    @RequestMapping(value = "/flower/send.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendFlower() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }


    @RequestMapping(value = "/invite/history.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage inviteHistory() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }


    @RequestMapping(value = "/moreflower/selflearn/is_finish.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage isFinishAllSelfLearn() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }


    @RequestMapping(value = "/invite/accept.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage acceptInvite() {
        return MapMessage.errorMessage("活动已下线,请参加其他活动");
    }
}
