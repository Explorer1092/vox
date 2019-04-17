package com.voxlearning.enanalyze.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.enanalyze.ViewBuilder;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.GroupAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.GroupRemoveRequest;
import com.voxlearning.enanalyze.view.GroupView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * 按句子维度的排行榜controller
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Controller
@RequestMapping(value = "/enanalyze/group")
public class GroupController {

    @Resource
    GroupAggregator groupAggregator;

    /**
     * 查询某个人的所有关联分组
     *
     * @return 视图
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        MapMessage message;
        try {
            List<GroupView> view = groupAggregator.list(Session.getOpenId());
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 删除某个用户的群数据
     *
     * @param request 请求
     * @return 视图
     */
    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remove(@RequestBody GroupRemoveRequest request) {
        MapMessage message;
        try {
            groupAggregator.remove(Session.getOpenId(), request.getOpenGroupId());
            message = ViewBuilder.success(null);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

}
