package com.voxlearning.luffy.controller.tobbit;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.luffy.controller.MiniProgramController;
import com.voxlearning.utopia.service.ai.api.TobbitMathBoostService;
import com.voxlearning.utopia.service.ai.client.TobbitMathServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=45251113">wiki</a>
 */
@Slf4j
@Controller
@RequestMapping(value = "/mp/tobbit/boost")
public class TobbitMathBoostController extends MiniProgramController {


    @Inject
    private TobbitMathServiceClient tobbitMathServiceClient;

    @Override
    protected MiniProgramType type() {
        return MiniProgramType.TOBBIT;
    }


    @Override
    public boolean onBeforeControllerMethod() {
        return true;
    }


    @RequestMapping(value = "/status.vpage")
    @ResponseBody
    public MapMessage status() {

        String bid = getRequestString("bid");
        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没权限访问");
        }
        return wrapper((mm) -> {
            mm.putAll(boostServ().status(openId, uid(),bid));
        });
    }


    @RequestMapping(value = "/append.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage append() {

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没权限访问");
        }

        String bid = getRequestString("bid");
        String name = getRequestString("name");
        String avatar = getRequestString("avatar");
        if (StringUtils.isAnyBlank(bid, avatar)) {
            return MapMessage.errorMessage("参数错误");
        }

        return wrapper((mm) -> {
            mm.putAll(boostServ().appendBoost(bid, openId, name, avatar));
        });
    }


    @RequestMapping(value = "/new.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage newBoost() {

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没权限访问");
        }
        String bookId = getRequestString("bookId");
        String name = getRequestString("name");
        String tel = getRequestString("tel");
        String city = getRequestString("city");
        String addr = getRequestString("addr");

        if (StringUtils.isAnyBlank(bookId, name, tel, city, addr)) {
            return MapMessage.errorMessage("参数错误");
        }
        return wrapper((mm) -> {
            mm.putAll(boostServ().newBoost(openId, uid(), bookId, name, tel, city, addr));
        });
    }


    @RequestMapping(value = "/books.vpage")
    @ResponseBody
    public MapMessage books() {

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没权限访问");
        }
        return wrapper((mm) -> {
            mm.putAll(boostServ().oralBookList());
        });
    }


    @RequestMapping(value = "/scrolling.vpage")
    @ResponseBody
    public MapMessage scrolling() {

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("没权限访问");
        }
        return wrapper((mm) -> {
            mm.putAll(boostServ().scrollingList());
        });
    }


    private TobbitMathBoostService boostServ() {
        return tobbitMathServiceClient.getTobbitMathBoostService();
    }


}
