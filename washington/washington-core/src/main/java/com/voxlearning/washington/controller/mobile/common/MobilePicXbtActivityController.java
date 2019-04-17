package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 点读机学霸团 活动
 * 绘本总动员 活动
 *
 * @author jiangpeng
 * @since 2016-12-13 下午3:48
 **/

@Controller
@RequestMapping(value = "/usermobile/xbt")
@Slf4j
public class MobilePicXbtActivityController extends AbstractMobileController {


    /**
     * 家长端 学生端 活动页
     * 绘本总动员也用这个页面了。
     * 家长端需要sid
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        return "common/mobileerrorinfo";
    }

    /**
     * 活动分享出去的页面地址
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/share.vpage", method = {RequestMethod.GET})
    public String sharePage(Model model) {
        return "common/mobileerrorinfo";
    }


    /**
     * 点击第一个立即参加
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/join_step1.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage joinStepOne() {
        return activityExpiryMsg;
    }

    @RequestMapping(value = "/send_code.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendCode() {
        return activityExpiryMsg;
    }

    /**
     * 立即参加第二步,
     * 验证验证码并加入团
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/join_step2.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage joinStepTwo() {
        return activityExpiryMsg;
    }

    /**
     * 分享时自动关注公众号（静默关注）
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/follow_official_account.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage followOfficialAccount() {
        return activityExpiryMsg;
    }

    /**
     * 期末英雄团小组页
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/hero_group.vpage", method = {RequestMethod.GET})
    public String heroGroup() {
        return "common/mobileerrorinfo";
    }

    /**
     * 前一天的完成情况
     * 点读机学霸团
     * 绘本总动员
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/yesterday/members.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage membersYesterday() {
        return activityExpiryMsg;
    }


    /**
     * 绘本总动员 组员详情页
     *
     * @return mapmessage
     */
    @RequestMapping(value = "/zdy/member/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage membersIndex() {
       return activityExpiryMsg;
    }

}
