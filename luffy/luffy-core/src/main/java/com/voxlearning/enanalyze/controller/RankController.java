package com.voxlearning.enanalyze.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.enanalyze.ViewBuilder;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.RankAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.*;
import com.voxlearning.utopia.enanalyze.model.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 按句子维度的排行榜controller
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Controller
@RequestMapping(value = "/enanalyze/rank")
public class RankController {

    @Resource
    RankAggregator rankAggregator;

    /**
     * 查询某个组的个人排行
     *
     * @param openGroupId 组id
     * @return
     */
    @RequestMapping(value = "sentence/self.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage self(@RequestParam(name = "openGroupId") String openGroupId) {
        MapMessage message;
        try {
            RankSentenceView view = rankAggregator.getRank(openGroupId, Session.getOpenId());
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 分页查询某个组的所有人排行
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "sentence/all.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage page(@RequestBody RankSentencePageRequest request) {
        MapMessage message;
        try {
            PageInfo pageInfo = new PageInfo(request.getPage(), request.getSize());
            RankSentencePageView view = rankAggregator.queryGroupRank(request.getOpenGroupId(), Session.getOpenId(), pageInfo);
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "分页查询批改记录时发生错误");
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 点赞或者取消点赞
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "sentence/like.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage like(@RequestBody RankSentenceLikeRequest request) {
        MapMessage message;
        try {
            request.setFromOpenId(Session.getOpenId());
            RankSentenceLikeView view = rankAggregator.like(request);
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "分页查询批改记录时发生错误");
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }


}
