/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import com.voxlearning.utopia.service.feedback.client.FeedbackLoaderClient;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shuai Huan on 2014/6/17.
 */
@Controller
@RequestMapping("/crm/feedback/tag")
public class CrmFeedbackTagController extends CrmAbstractController {

    @Inject private FeedbackLoaderClient feedbackLoaderClient;
    @Inject private FeedbackServiceClient feedbackServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String tagIndex(Model model) {

        String watcher = getRequestParameter("watcher", "-1");
        List<UserFeedbackTag> tagList;
        if (watcher.equals("-1")) {
            tagList = feedbackLoaderClient.getFeedbackLoader().findAllTags();
        } else {
            tagList = feedbackLoaderClient.getFeedbackLoader().findByWatcherName(watcher);
        }

//        List<UserFeedbackWatcher> allUserFeedbackWatchers = userFeedbackTagPersistence.findAllWatchers();
//        List<Map<String, Object>> watchersWithTag = userFeedbackTagPersistence.findWatchersWithTag();
//        model.addAttribute("watchers",allUserFeedbackWatchers);
//        model.addAttribute("watchersWithTag",watchersWithTag);
        model.addAttribute("tags", tagList);
        return "/crm/feedback/tagindex";
    }

    @RequestMapping(value = "addtag.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addTag(@RequestParam("name") String name, @RequestParam("watcher") String watcher) {
        MapMessage result = new MapMessage();

        UserFeedbackTag userFeedbackTag = feedbackLoaderClient.getFeedbackLoader().findByTagName(name);
        if (userFeedbackTag == null) {
            userFeedbackTag = new UserFeedbackTag();
            userFeedbackTag.setName(name);
            userFeedbackTag.setWatcherName(watcher);
            MapMessage message = feedbackServiceClient.getFeedbackService().persistUserFeedbackTag(userFeedbackTag);
            Long id = (Long) message.get("id");
            userFeedbackTag.setId(id);
        } else {
            result.setSuccess(false);
            result.setInfo("这个Tag已经被添加过了");
            return result;
        }
        result.setSuccess(true);
        result.set("value", userFeedbackTag);
        return result;
    }

    @RequestMapping(value = "edittag.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage editTag(@RequestParam("id") Long id, @RequestParam("name") String name, @RequestParam("watcher") String watcher) {
        MapMessage result = new MapMessage();

        // FIXME: 需要修改
        UserFeedbackTag userFeedbackTag = feedbackLoaderClient.getFeedbackLoader().loadUserFeedbackTag(id);
        if (userFeedbackTag != null) {
            userFeedbackTag.setWatcherName(watcher);
            userFeedbackTag.setName(name);
            feedbackServiceClient.getFeedbackService().updateUserFeedbackTag(id, userFeedbackTag);
        } else {
            result.setSuccess(false);
            result.setInfo("这个Tag不存在，无法编辑!");
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    @RequestMapping(value = "deltag.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delTag(@RequestParam("id") Long id) {
        MapMessage result = new MapMessage();

        // FIXME: 需要修改
        UserFeedbackTag userFeedbackTag = feedbackLoaderClient.getFeedbackLoader().loadUserFeedbackTag(id);
        if (userFeedbackTag != null) {
            userFeedbackTag.setDisabled(true);
            feedbackServiceClient.getFeedbackService().updateUserFeedbackTag(id, userFeedbackTag);
        } else {
            result.setSuccess(false);
            result.setInfo("这个Tag不存在，无法删除!");
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    @RequestMapping(value = "loadtagtree.vpage", method = RequestMethod.GET)
    @ResponseBody
    String loadTagTree() {

        MapMessage message = new MapMessage();
        try {
            return buildFeedbackTagTree();
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
            return "";
        }

    }

    private String buildFeedbackTagTree() {

        List<UserFeedbackTag> tagList = feedbackLoaderClient.getFeedbackLoader().findAllTags();
        List retList = new ArrayList();

        // 第一次循环，将KnowledgePoint转成要用的Json Map
        Map<String, Map> kpMap = new HashMap();
        for (UserFeedbackTag tag : tagList) {
            // 转换成要使用的HashMap对象
            Map kpItemMap = new HashMap();
            kpItemMap.put("title", tag.getName());
            kpItemMap.put("key", String.valueOf(tag.getId()));
            kpItemMap.put("children", new ArrayList());

            // 判断是否为根节点
            if (tag.getParentId() == 0L) {
                kpItemMap.put("type", "root");
                retList.add(kpItemMap);
            }
            // 临时存储该对象便于查询
            kpMap.put(String.valueOf(tag.getId()), kpItemMap);
        }
        // 第二次循环，根据Id和ParentID构建父子关系
        for (UserFeedbackTag tag : tagList) {
            Long parentId = tag.getParentId();
            if (parentId == 0L) {
                continue;
            }

            Map parentObj = kpMap.get(String.valueOf(parentId));
            Map childObj = kpMap.get(String.valueOf(tag.getId()));

            // 如果父节点不存在，自动成为根节点
            if (parentObj == null && !retList.contains(childObj)) {
                retList.add(childObj);
            } else {
                List children = (List) parentObj.get("children");
                if (!children.contains(childObj)) {
                    children.add(childObj);
                }
            }
        }
        return JsonUtils.toJson(retList);
    }

}
