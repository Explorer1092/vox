package com.voxlearning.luffy.controller.tobbit;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.luffy.controller.MiniProgramAuthController;
import com.voxlearning.utopia.service.ai.client.TobbitMathServiceClient;
import com.voxlearning.utopia.service.ai.constant.TobbitScoreType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;


/**
 * 特供版spring, RequestMapping inheritance 被阉割了,CD.
 */
@Slf4j
@RequestMapping(value = "/mp/tobbit")
@Controller
public class TobbitAuthController extends MiniProgramAuthController {


    @Inject
    private TobbitMathServiceClient tobbitMathServiceClient;


    @Override
    protected MiniProgramType type() {
        return MiniProgramType.TOBBIT;
    }

    @Override
    protected void afterLogin(User user, MapMessage mm) {
        if (user != null) {
            // 打卡
            miniProgramServiceClient.getUserMiniProgramCheckService().doCheck(user.getId(), type());

            boolean s = tobbitMathServiceClient.getTobbitMathScoreService().addScore(user.getId(), TobbitScoreType.SIGNUP);
            if (s) mm.add("score", TobbitScoreType.SIGNUP.json());
        }
    }

    @Override
    protected void firstLogin(User user, MapMessage mm) {
        tobbitMathServiceClient.getTobbitMathService().spreadRegUser(getOpenId(), user.getId(), true);
    }

    @Override
    protected void outOfSystemUser(User user,boolean outOfSystem, MapMessage mm) {

    }

    @RequestMapping(value = "/auth.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage auth() {
        return super.auth();
    }

    @RequestMapping(value = "/authlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage authLogin() {
        return super.authLogin();
    }


    @RequestMapping(value = "/verifycode/getcid.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCid() {
        return super.getCid();
    }

    @RequestMapping(value = "/verifycode/get.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getVerifyCode() {
        return super.getVerifyCode();
    }

    @RequestMapping(value = "/verifycode/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifiedLoginPost() {
        return super.verifiedLoginPost();
    }
}
