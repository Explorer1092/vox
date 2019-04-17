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

package com.voxlearning.washington.controller.open.wechat.o2o;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineListenPaper;
import com.voxlearning.utopia.service.question.api.entity.XxWorkbook;
import com.voxlearning.utopia.service.question.api.entity.XxWorkbookContent;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Objects;

@Controller
@RequestMapping(value = "/open/wechat/o2o")
@Slf4j
public class WechatO2OController extends AbstractOpenController {
    @RequestMapping(value = "getlistenpaper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getListenPaper(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String paperId = ConversionUtils.toString(context.getParams().get("paperId")); // 纸质试卷ID
        if (StringUtils.isBlank(paperId)) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }
        OfflineListenPaper paper = newHomeworkPartLoaderClient.findOfflineListenPaperByIds(Collections.singletonList(paperId)).get(paperId);
        if (paper == null) {
            context.setCode("400");
            context.setError("listen file not found");
            return context;
        }
        String prefixAudioUrl = commonConfiguration.getExamUploadResourceUrl();
        context.setCode("200");
        context.add("prefixAudioUrl", prefixAudioUrl);
        context.add("audioList", paper.getFileIds());
        context.add("totalTime", paper.getTotalTime());
        context.add("title", paper.getTitle());
        return context;
    }


    @RequestMapping(value = "oralquiz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getOralQuiz(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("这个功能已经关闭了哦，去做其他作业吧");
        return context;
    }

    @RequestMapping(value = "listenpaperquiz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getListenpaperQuiz(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("这个功能已经关闭了哦，去做其他作业吧");
        return context;
    }

    /**
     * o2o扫码提交答案获取答案
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "getexamanswer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getExamAnswer(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("这个功能已经关闭了哦，去做其他作业吧");
        return context;
    }

    @RequestMapping(value = "anserrecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updateExamAnswerRecord(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("这个功能已经关闭了哦，去做其他作业吧");
        return context;
    }

    /**
     * o2o听力结果提交
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "savelistenresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext saveListenResult(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("200");
        context.setError("这个功能已经关闭了哦，去做其他作业吧");
        return context;
    }

    /**
     * o2o教辅B模式，入口在StudentWorkbookHomeworkController---qrBmode
     *
     * @param request request
     * @return 什么
     */
    @RequestMapping(value = "getworkbookcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getWorkbookContent(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String workbookId = ConversionUtils.toString(context.getParams().get("wb"));
        String type0Ids = ConversionUtils.toString(context.getParams().get("t0d"));
        String type1Ids = ConversionUtils.toString(context.getParams().get("t1d"));
        String workbookContentId = ConversionUtils.toString(context.getParams().get("wcod"));
        String part = ConversionUtils.toString(context.getParams().get("p"));
        // 没有用到的参数预留先

        if (StringUtils.isBlank(workbookContentId)) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }

        XxWorkbookContent content = xxWorkbookContentLoaderClient.getRemoteReference().loadXxWorkbookContentByDocIdIncludeDisabled(workbookContentId);
        if (Objects.isNull(content)) {
            context.setCode("400");
            context.setError("XxWorkbookContent is null");
            return context;
        }

        XxWorkbook workbook = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooksIncludeDisabled(Collections.singleton(content.getWorkbook_id())).get(content.getWorkbook_id());
        if (Objects.isNull(workbook)) {
            context.setCode("400");
            context.setError("XxWorkbook is null");
            return context;
        }
        context.setCode("200");
        context.add("fullPrefixAudioUrl", content.getListen().getUrl());
        context.add("totalTime", content.getListen().getDuration());
        context.add("title", workbook.getTitle());
        return context;
    }


}