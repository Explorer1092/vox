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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.fromUtopia.entity.PsrPrimaryAppEnContent;
import com.voxlearning.athena.api.fromUtopia.entity.PsrPrimaryAppEnItem;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.athena.PsrServiceClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.REQ_UNIT_ID;

/**
 * Created by sadi.wan
 */
@Controller
@RequestMapping(value = "/v1/appquestion")
@Slf4j
public class AppQuestionApiController extends AbstractApiController {

    @Inject private PsrServiceClient psrServiceClient;
    /* ———————————— 提供给公司自行研发的应用的推题接口 ———————————————— */

    @RequestMapping(value = "/english.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getEnglishQuestion() {
        MapMessage mesg = new MapMessage();
        mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        mesg.add(RES_MESSAGE, "接口已经下线");
        return mesg;
    }

    @RequestMapping(value = "/math.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getMathQuestion() {
        MapMessage mesg = new MapMessage();
        mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        mesg.add(RES_MESSAGE, "接口已经下线");
        return mesg;
    }

    /* ———————————— 提供给第三方研发的应用的推题接口 ———————————————— */

    @RequestMapping(value = "/vendors/english.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchEnglishSentences() {
        // 参数校验
        try {
            validateRequired(REQ_POINT_COUNT, "知识点数");
            validateRequired(REQ_BOOK_ID, "教材ID");
            validateRequired(REQ_GAME_TYPE, "游戏类型");
            validateDigitNumber(REQ_POINT_COUNT, "知识点数");
        } catch (IllegalArgumentException e) {
            return new MapMessage().add(RES_MESSAGE, e.getMessage())
                    .add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        }

        // 请求校验
        try {
            if (StringUtils.isNotBlank(getRequestString(REQ_UNIT_ID))) {
                validateRequest(REQ_POINT_COUNT, REQ_BOOK_ID, REQ_GAME_TYPE, REQ_UNIT_ID);
            } else {
                validateRequest(REQ_POINT_COUNT, REQ_BOOK_ID, REQ_GAME_TYPE);
            }
        } catch (IllegalArgumentException e) {
            return new MapMessage().add(RES_MESSAGE, e.getMessage())
                    .add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        }

        Long studentId = getApiRequestUser().getId();
        String appKey = getApiRequestApp().getAppKey();
        int count = getRequestInt(REQ_POINT_COUNT);
        String gameType = getRequestString(REQ_GAME_TYPE);

        String bookId = getRequestString(REQ_BOOK_ID);
        NewBookProfile book = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singletonList(bookId)).get(bookId);
        if (book == null) return new MapMessage().add(RES_MESSAGE, "用户没有课本")
                .add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        String unitId = getRequestString(REQ_UNIT_ID);

        if (count < 0 || count > 7) return new MapMessage().add(RES_MESSAGE, "单次获取知识点个数不能超过7个")
                .add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);

        // 获取用户并校验
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (null == studentDetail) return new MapMessage().add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE)
                .add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);

        String eType = gameTypeMap.getOrDefault(gameType, "");
        List<String> words = new ArrayList<>();
        String req = "";
        String resp = "";
        try {
            PsrPrimaryAppEnContent psr;
            if (StringUtils.isNotBlank(unitId)) {
                // psr = utopiaPsrServiceClient.getRemoteReference().getPsrPrimaryAppEn(appKey, studentId, studentDetail.getCityCode(), bookId, unitId, count, eType);
                psr = psrServiceClient.getUtopiaPsrLoader().getPsrPrimaryAppEn(appKey, studentId, studentDetail.getCityCode(), bookId, unitId, count, eType);
                Map<String, Object> reqMap = MapUtils.m(
                        "appKey", appKey,
                        "studentId", studentId,
                        "cityCode", studentDetail.getCityCode(),
                        "bookId", bookId,
                        "unitId", unitId,
                        "count", count,
                        "eType", eType);
                req = JsonUtils.toJson(reqMap);
            } else {
                // psr = utopiaPsrServiceClient.getRemoteReference().getPsrPrimaryAppEn(appKey, studentId, studentDetail.getCityCode(), bookId, "-1", count, eType);
                psr = psrServiceClient.getUtopiaPsrLoader().getPsrPrimaryAppEn(appKey, studentId, studentDetail.getCityCode(), bookId, "-1", count, eType);
                Map<String, Object> reqMap = MapUtils.m(
                        "appKey", appKey,
                        "studentId", studentId,
                        "cityCode", studentDetail.getCityCode(),
                        "bookId", bookId,
                        "unitId", -1,
                        "count", count,
                        "eType", eType);
                req = JsonUtils.toJson(reqMap);
            }

            if (psr != null && StringUtils.equalsIgnoreCase(psr.getErrorContent(), "success")) {
                words.addAll(psr.getAppEnList().stream().map(PsrPrimaryAppEnItem::getEid).collect(Collectors.toList()));
                resp = JsonUtils.toJson(psr);
            }
        } catch (Exception e) {
            logger.error("PSR getPsrPrimaryAppEn FAILED with parameter:(cityCode:{},bookId:{},unitId:{})", studentDetail.getCityCode(), bookId, unitId, e);
        }

        // 获取sentence
        List<Sentence> sentences = englishContentLoaderClient.getExtension().loadSentenceFromWordListAndNewBook(req, resp, words, bookId, count)
                .stream()
                .collect(Collectors.groupingBy(Sentence::getEnText))
                .values()
                .stream()
                .map(e -> e.iterator().next())
                .collect(Collectors.toList());

        return new MapMessage().add(RES_RESULT, RES_RESULT_SUCCESS)
                .add(RES_QUESTION_LIST, flashGameServiceClient.loadEnglishSentenceMappers(sentences, Ktwelve.PRIMARY_SCHOOL));
    }

    private final static Map<String, String> gameTypeMap;

    static {
        gameTypeMap = new LinkedHashMap<>();
        gameTypeMap.put("word-indentification", "单词辨识");
        gameTypeMap.put("word-spelling", "单词拼写");
        gameTypeMap.put("word-listening-and-choosing", "听音选词");
        gameTypeMap.put("picture-word-recognation", "看图识词");
    }
}
