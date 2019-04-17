package com.voxlearning.enanalyze.controller;

import com.google.common.base.Stopwatch;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.enanalyze.ViewBuilder;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.UserAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.UserLoginRequest;
import com.voxlearning.enanalyze.view.UserLoginView;
import com.voxlearning.enanalyze.view.UserRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 授权和鉴权
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Controller
@RequestMapping(value = "/enanalyze/user")
public class UserController {

    @Resource
    private UserAggregator userAggregator;

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @RequestMapping(value = "login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage login(@RequestBody UserLoginRequest request) {
        MapMessage result;
        try {
            UserLoginView response = userAggregator.login(request);
            if (null != response.getToken() && !"".equals(response.getToken())) {
                result = ViewBuilder.success(response);
            } else {
                result = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "登录异常");
            }
        } catch (BusinessException e) {
            result = ViewBuilder.error(e);
        } catch (Exception e) {
            result = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return result;
    }


    /**
     * 更新用户信息
     *
     * @param request 用户信息
     * @return 响应
     */
    @RequestMapping(value = "update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage update(@RequestBody UserRequest request) {
        MapMessage result;
        try {
            request.setOpenId(Session.getOpenId());
            userAggregator.update(request);
            result = ViewBuilder.success(true);
        } catch (BusinessException e) {
            result = ViewBuilder.error(e);
        } catch (Exception e) {
            result = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return result;
    }

    /**
     * test
     *
     * @return
     */
    @RequestMapping(value = "test.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sessionTest() {
        return ViewBuilder.success("ok");
    }

}



