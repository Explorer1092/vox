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

package com.voxlearning.washington.controller.open.v1;

import com.fasterxml.jackson.databind.JavaType;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.question.api.entity.ZmQuestion;
import com.voxlearning.utopia.service.question.api.entity.ZmWord;
import com.voxlearning.utopia.service.question.api.mapper.ZmWordRequest;
import com.voxlearning.utopia.service.question.api.mapper.ZmWordResponse;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.ZmWordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author peng.zhang.a
 * @since 2016/6/6
 */
@Controller
@RequestMapping(value = "/v1/travelAmerica")
@Slf4j
public class TravalAmericaApiController extends AbstractApiController {
    @Inject
    protected ZmWordLoaderClient zmWordLoaderClient;
    @Inject
    protected QuestionLoaderClient questionLoaderClient;

    @Inject private ActionServiceClient actionServiceClient;
    @Inject private VendorServiceClient vendorServiceClient;

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @RequestMapping(value = "/getQuestionsByIds.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getQuestionsByIds() {
        List<String> ids;
        try {
            validateRequest(REQ_TRAVAL_AMERICA_CONTENT);
            String content = getRequestString(REQ_TRAVAL_AMERICA_CONTENT);
            ids = JsonUtils.fromJsonToList(content, String.class);
            if (CollectionUtils.isEmpty(ids)) {
                return failedResult("参数为空");
            }
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(StringUtils.formatMessage("参数解析错误Exception={}", e.getMessage()));
        }
        Map<String, ZmQuestion> zmQuestionMap = zmWordLoaderClient.getRemoteReference().loadZmQuestionsByIds(ids);
        if (MapUtils.isNotEmpty(zmQuestionMap)) {
            return successResult(RES_TRAVAL_AMERICA_QTSRESULT, zmQuestionMap);
        } else {
            return failedResult("question is null");
        }
    }

    @RequestMapping(value = "/getQuestions.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getQuestions() {
        Map<Integer, Map<String, Integer>> contentTypeIdWordIdStart;
        try {
            validateRequest(REQ_TRAVAL_AMERICA_CONTENT);
            String content = getRequestString(REQ_TRAVAL_AMERICA_CONTENT);

            JavaType mapValueType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Integer.class);
            JavaType mapKeyType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructType(Integer.class);
            JavaType mapType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, mapKeyType, mapValueType);
            contentTypeIdWordIdStart = JsonUtils.fromJson(content, mapType);
            if (MapUtils.isEmpty(contentTypeIdWordIdStart)) {
                return failedResult("参数为空");
            }
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(StringUtils.formatMessage("参数解析错误Exception={}", e));
        }

        Map<Integer, Map<String, Map<String, Object>>> integerMapMap = zmWordLoaderClient.getRemoteReference().loadZmQuestionByContentType2IdAndZmWordId(contentTypeIdWordIdStart);
        return successResult(RES_TRAVAL_AMERICA_QTSRESULT, integerMapMap);
    }

    @RequestMapping(value = "/getWords.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getWords() {
        Map<Integer, Collection<ZmWordRequest>> reqMap;
        try {
            validateRequest(REQ_TRAVAL_AMERICA_CONTENT);
            String content = getRequestString(REQ_TRAVAL_AMERICA_CONTENT);

            JavaType mapValueType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructParametrizedType(List.class, List.class, ZmWordRequest.class);
            JavaType mapKeyType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructType(Integer.class);
            JavaType mapType = JsonObjectMapper.OBJECT_MAPPER.getTypeFactory().constructParametrizedType(Map.class, Map.class, mapKeyType, mapValueType);
            reqMap = JsonUtils.fromJson(content, mapType);
            if (MapUtils.isEmpty(reqMap)) {
                return failedResult("参数为空");
            }
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(StringUtils.formatMessage("参数解析错误Exception={}", e));
        }

        Map<Integer, ZmWordResponse> integerMapMap = zmWordLoaderClient.getRemoteReference().loadZmWordsByZmWordRequest(reqMap);
        return successResult(RES_TRAVAL_AMERICA_WORDSRESULT, integerMapMap);
    }

    @RequestMapping(value = "/getWordByIds.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getWordByIds() {
        List<String> wordIds;
        try {
            validateRequest(REQ_TRAVAL_AMERICA_CONTENT);
            String content = getRequestString(REQ_TRAVAL_AMERICA_CONTENT);
            wordIds = JsonUtils.fromJsonToList(content, String.class);
            if (CollectionUtils.isEmpty(wordIds)) {
                return failedResult("参数为空");
            }
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(StringUtils.formatMessage("参数解析错误Exception={}", e));
        }

        Map<String, ZmWord> stringZmWordMap = zmWordLoaderClient.getRemoteReference().loadZmWordsByIds(wordIds);
        return successResult(RES_TRAVAL_AMERICA_WORDSRESULT, stringZmWordMap);
    }

    /**
     * 走遍美国学英语竞技场胜利次数 成就相关
     *
     * @return
     */
    @RequestMapping(value = "/achievement/winPK.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage winPk2Achievement() {
        try {
            validateRequest();
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }
        User user = getApiRequestUser();
        if (user == null || !user.isStudent())
            return failMessage("error user!");

        actionServiceClient.winPk(user.getId());
        return successMessage();
    }

    /**
     * 走遍美国学英语竞技场胜利次数 成就相关
     * staging调用 只用用户id
     *
     * @return
     */
    @RequestMapping(value = "/achievement/winPK_repair.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage winPk2AchievementRepire() {
        if (RuntimeMode.current() != Mode.STAGING)
            return failMessage("deny");
        try {
            validateRequestNoSessionKey(REQ_USER_ID);
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }

        actionServiceClient.winPk(getRequestLong(REQ_USER_ID));
        return successMessage();
    }


    /**
     * 走遍美国 走美学单词-提交答案
     * 成长值
     *
     * @return
     */
    @RequestMapping(value = "/growth/submitAnswer.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage submitAnswer() {
        try {
            validateRequest();
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }

        User user = getApiRequestUser();
        if (user == null || !user.isStudent())
            return failMessage("error user!");
        actionServiceClient.submitZoumeiAnswer(user.getId());

        //记录完成自学天数
        Long count = actionServiceClient.getRemoteReference()
                .increaseFinishSelfLearningCount(user.getId())
                .getUninterruptibly();
        if (count != null && count <= 1) {
            actionServiceClient.finishSelfLearning(user.getId());
        }
        return successMessage();
    }

    /**
     * 走遍美国 走美学单词-提交答案
     * 成长值
     *
     * @return
     */
    @RequestMapping(value = "/growth/submitAnswer_repair.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage submitAnswerRepair() {
        if (RuntimeMode.current() != Mode.STAGING)
            return failMessage("deny");
        try {
            validateRequestNoSessionKey(REQ_USER_ID);
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }

        Long userId = getRequestLong(REQ_USER_ID);
        actionServiceClient.submitZoumeiAnswer(userId);

//        //记录完成自学天数
//        Long count = actionCacheSystem.CBS.flushable.incr("ACTION_EVENT_FINISH_SELF_LEARNING_COUNT_"+userId, 1, 1, DateUtils.getCurrentToDayEndSecond());
//        if (count <= 1) {
//            actionServiceClient.finishSelfLearning(userId);
//        }
        return successMessage();
    }


    /**
     * 走遍美国
     * 更新我的自学 进度
     *
     * @return
     */
    @RequestMapping(value = "/studyprogress/notify.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage updateProgress() {
        try {
            validateRequired(REQ_PROGRESS, "进度");
            validateRequest(REQ_PROGRESS);
        } catch (IllegalAccessError e) {
            return failedResult(e.getMessage());
        } catch (Exception e) {
            return failedResult(e.getMessage());
        }
        User user = getApiRequestUser();
        if (user == null || !user.isStudent())
            return failMessage("error user!");
        mySelfStudyService.updateSelfStudyProgress(user.getId(), SelfStudyType.ZOUMEI_ENGLISH, getRequestString(REQ_PROGRESS));
        return successMessage();
    }


    private MapMessage successResult(String key, Object resultContent) {
        return new MapMessage().add(RES_RESULT, RES_RESULT_SUCCESS).add(key, resultContent);
    }

    private MapMessage failedResult(String errMessage) {
        MapMessage resultMap = new MapMessage();
        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, errMessage);
        return resultMap;
    }

}
