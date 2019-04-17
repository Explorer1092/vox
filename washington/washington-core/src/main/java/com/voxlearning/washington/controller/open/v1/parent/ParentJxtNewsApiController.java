package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsCacheType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsComment;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016-7-1
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/jxt/news/")
public class ParentJxtNewsApiController extends AbstractParentApiController {


    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    /**
     * 发评论
     */
    @RequestMapping(value = "publish_comment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishComment() {
        String newsId = getRequestString(REQ_JXT_NEWS_ID);
        String comment = getRequestString(REQ_JXT_NEWS_COMMENT);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_JXT_NEWS_ID, "资讯ID");
            validateRequired(REQ_JXT_NEWS_COMMENT, "评论内容");
            validateRequest(REQ_JXT_NEWS_ID, REQ_JXT_NEWS_COMMENT);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        if (CollectionUtils.isEmpty(studentParentRefs) && VersionUtil.compareVersion(ver, "1.5.3") < 0) {
            //1.5.3以前没有孩子不能评论
            return failMessage(RES_RESULT_USER_UNBIND_STUDENT);
        }

        String userName = generateUserNameForJxtNewsComment(parent);

        JxtNewsComment jxtNewsComment = new JxtNewsComment();
        jxtNewsComment.setUserId(parent.getId());
        jxtNewsComment.setUserType(parent.getUserType());
        jxtNewsComment.setUserName(userName);
        jxtNewsComment.setComment(comment);
        jxtNewsComment.setTypeId(newsId);
        jxtNewsComment.setNewsId(newsId);
        //评论设置为不显示，是否显示需要后台控制
        jxtNewsComment.setIsShow(false);
        jxtNewsComment.setIsDisabled(false);

        String id = jxtNewsServiceClient.saveJxtNewsComment(jxtNewsComment);
        if (id != null) {
            return successMessage();
        }
        return failMessage("评论失败,请重试");
    }

    /**
     * 获取通知数量
     */
    @RequestMapping(value = "getJxtNewsNoticeCount.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getJxtNewsNoticeCount() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        User user = getCurrentParent();
        if (user == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long userId = user.getId();
//        Long count = vendorCacheClient.getParentJxtCacheManager().loadTabNoticeCount(userId.toString());
        Long count = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_TAB_NOTICE_COUNT, Collections.singleton(userId.toString()))
                .take()
                .get(userId.toString());
        Map<String, Object> map = new HashMap<>();
        map.put("noticeCount", count != null ? count : 0);
        map.put("show_red_point", false);
        return successMessage().add("info", map);
    }

}
