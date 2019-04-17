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

package com.voxlearning.washington.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.service.support.GameFlashLoaderConfigManager;
import com.voxlearning.washington.support.WashingtonRequestContext;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tanguohong on 14-7-2.
 */
@Named
@NoArgsConstructor
public class ChineseFlashGameLoader {
    @Inject GameFlashLoaderConfigManager gameFlashLoaderConfigManager;

    public Map<String, Object> loadChineseGameFlash(WashingtonRequestContext requestContext, LoadChineseFlashGameContext context) {
        Validate.notNull(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getChinesePractice().getFrameType()) ? context.getChinesePractice().getFrameType() : context.getChinesePractice().getFilename();
        flashVars.put("lessonId", context.getLessonId());
        flashVars.put("flashId", context.getChinesePractice().getFilename());
        flashVars.put("type", context.getChinesePractice().getId());
        flashVars.put("appId", context.getChinesePractice().getFilename()); //游戏文件名
        flashVars.put("userName", context.getUserId());
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计
        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file ,context.getChinesePractice().getFilename());


        Map<String, Object> param = new LinkedHashMap<>();
        //gameType和practiceType取的是同一个值，以后新FLASH应用统一取gameType,但为了兼容老的FLASH应用，所以practiceType也保留
        param.put("gameType", String.valueOf(context.getChinesePractice().getId()));
        param.put("practiceType", String.valueOf(context.getChinesePractice().getId()));
        param.put("userId", String.valueOf(context.getUserId()));
        param.put("homeworkId", context.getHomeworkId());
        param.put("bookId", String.valueOf(context.getBookId()));
        param.put("unitId", String.valueOf(context.getUnitId()));
        param.put("lessonId", String.valueOf(context.getLessonId()));
        param.put("studyType", context.getStudyType().name());
        param.put("clazzId", String.valueOf(context.getClazzId()));
        flashVars.put("json",JsonUtils.toJson(param));
        String flashVarsJson = JsonUtils.toJson(flashVars);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", context.getChinesePractice().getFilename());
        result.put("gameSize", context.getChinesePractice().getGameSize());
        result.put("gameName", context.getChinesePractice().getFilename());
        result.put("lessonId", context.getLessonId());
        result.put("needRecord", false);
        result.put("flashVars", flashVarsJson);
        result.put("version", context.getChinesePractice().getMobileVersion());
        result.put("type", context.getChinesePractice().getId());

        //有广告劫持，url后面加随机版本
        Map queryParams = MiscUtils.map("param", JsonUtils.toJson(param),
                "practiceName", context.getChinesePractice().getFilename(),
                "subject", Subject.CHINESE,
                "lessonId", context.getLessonId());

        //有广告劫持，url后面加随机版本
        result.put("gameDataURL", UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams)); //10表示出题个数
        result.put("endPath", "flash/" + context.getChinesePractice().getFilename() + "/process" + Constants.AntiHijackExt);



        return result;
    }
}
