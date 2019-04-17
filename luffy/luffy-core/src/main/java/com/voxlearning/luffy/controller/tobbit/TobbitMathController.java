package com.voxlearning.luffy.controller.tobbit;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.luffy.controller.MiniProgramController;
import com.voxlearning.utopia.service.ai.api.TobbitMathScoreService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import com.voxlearning.utopia.service.ai.client.TobbitMathServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=45039152">wiki</a>
 */
@Controller
@Slf4j
@RequestMapping(value = "/mp/tobbit")
public class TobbitMathController extends MiniProgramController {


    @Inject
    private TobbitMathServiceClient tobbitMathServiceClient;


    @Override
    protected MiniProgramType type() {
        return MiniProgramType.TOBBIT;
    }


    @RequestMapping(value = "/my.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage my() {
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().loadUserInfo(getOpenId(), uid()));
        });
    }


    @RequestMapping(value = "/clean.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cleanHistory() {
        return wrapper((mm) -> {
            mm.putAll(tobbitServ().clean(getOpenId(), uid()));
        });
    }


    @RequestMapping(value = "/redeem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage redeemCourse() {
        String cid = getRequestString("cid");
        if (StringUtils.isBlank(cid)) {
            return MapMessage.errorMessage("参数错误");
        }
        return wrapper((mm) -> {
            mm.putAll(scoreServ().redeemCourse(uid(), cid));
        });
    }

    @RequestMapping(value = "/score.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getScore() {
        return wrapper((mm) -> {
            mm.putAll(scoreServ().history(uid()));
        });
    }


    private TobbitMathService tobbitServ() {
        return tobbitMathServiceClient.getTobbitMathService();
    }


    private TobbitMathScoreService scoreServ() {
        return tobbitMathServiceClient.getTobbitMathScoreService();
    }


}
