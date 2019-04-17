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
 * @author Guohong Tan
 * @version 0.1
 * @since 13-7-8
 */
@Named
@NoArgsConstructor
public class MathFlashGameLoader {

    @Inject
    GameFlashLoaderConfigManager gameFlashLoaderConfigManager;


    public Map<String, Object> loadMathGameFlash(WashingtonRequestContext requestContext, LoadMathFlashGameContext context, Boolean fromMobile) {
        Validate.notNull(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getMathPractice().getFrameType()) ? context.getMathPractice().getFrameType() : context.getMathPractice().getFilename();
        flashVars.put("lessonId", context.getLessonId());
        flashVars.put("flashId", file);
        flashVars.put("type", context.getMathPractice().getId());
        flashVars.put("appId", context.getMathPractice().getFilename()); //游戏文件名
        flashVars.put("userName", context.getUserId());
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("questionNum", context.getQuestionNum());
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计

        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        //如果是移动端来的无需设置flashUrl
        if(!fromMobile){
            gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, context.getMathPractice().getFilename());
        }

        Map<String, Object> param = new LinkedHashMap<>();
        //gameType和practiceType取的是同一个值，以后新FLASH应用统一取gameType,但为了兼容老的FLASH应用，所以practiceType也保留
        param.put("gameType", String.valueOf(context.getMathPractice().getId()));
        param.put("practiceType", String.valueOf(context.getMathPractice().getId()));
        param.put("userId", String.valueOf(context.getUserId()));
        param.put("hid", context.getHomeworkId());
        param.put("packageId", String.valueOf(context.getPackageId()));
        param.put("bookId", String.valueOf(context.getBookId()));
        param.put("unitId", String.valueOf(context.getUnitId()));
        param.put("lessonId", String.valueOf(context.getLessonId()));
        param.put("pointId", String.valueOf(context.getPointId()));
        param.put("dataType", context.getDataType());
        param.put("studyType", context.getStudyType().name());
        param.put("cid", String.valueOf(context.getClazzId()));
        param.put("gid", String.valueOf(context.getGroupId()));
        param.put("questionNum", String.valueOf(context.getQuestionNum()));
        param.put("homeworkType", context.getHomeworkType());

        flashVars.put("json",JsonUtils.toJson(param));
        String flashVarsJson = JsonUtils.toJson(flashVars);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", context.getMathPractice().getFilename());
        result.put("gameSize", context.getMathPractice().getGameSize());
        result.put("gameName", context.getMathPractice().getFilename());
        result.put("lessonId", context.getLessonId());
        result.put("needRecord", false);
        result.put("flashVars", flashVarsJson);
        result.put("version", context.getMathPractice().getMobileVersion());
        result.put("type", context.getMathPractice().getId());
        Map queryParams = MiscUtils.map("param", JsonUtils.toJson(param),
                "practiceName", context.getMathPractice().getFilename(),
                "subject", Subject.MATH,
                "pointId", context.getPointId(),
                "amount", context.getQuestionNum());

        //有广告劫持，url后面加随机版本
//        result.put("gameDataURL", HttpUtils.buildUrlQuery("appdata/flash/"+context.getMathPractice().getFilename()+"/obtain-"+ Subject.MATH.name()+"-"+context.getPointId()+"-"+ context.getQuestionNum() + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams)); //10表示出题个数
        result.put("gameDataURL", UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams)); //10表示出题个数
        //result.put("gameExtraDataURL", UrlUtils.buildUrlQuery("interactive/flash/obtainextradata/math/" + context.getMathPractice().getFilename() + Constants.AntiHijackExt, queryParams));
        result.put("gameExtraDataURL", UrlUtils.buildUrlQuery("interactive/flash/obtainextradata/math" + Constants.AntiHijackExt + "?flashGameName=" + context.getMathPractice().getFilename(), queryParams));
        result.put("endPath", "flash/" + context.getMathPractice().getFilename() + "/process" + Constants.AntiHijackExt);

        return result;
    }

    public Map<String, Object> loadMathHomeworkCorrect(WashingtonRequestContext requestContext, LoadMathFlashGameContext context,String homeworkCorrectId,Long unitId,Long lessonId,Long practiceId) {
        Validate.notNull(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getMathPractice().getFrameType()) ? context.getMathPractice().getFrameType() : context.getMathPractice().getFilename();
        flashVars.put("lessonId", context.getLessonId());
        flashVars.put("flashId", file);
        flashVars.put("type", context.getMathPractice().getId());
        flashVars.put("appId", context.getMathPractice().getFilename()); //游戏文件名
        flashVars.put("userName", context.getUserId());
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("questionNum", context.getQuestionNum());
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计

        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, context.getMathPractice().getFilename());

        Map<String, Object> param = new LinkedHashMap<>();
        //gameType和practiceType取的是同一个值，以后新FLASH应用统一取gameType,但为了兼容老的FLASH应用，所以practiceType也保留
        param.put("gameType", context.getMathPractice().getId());
        param.put("practiceType", context.getMathPractice().getId());
        param.put("userId", context.getUserId());
        param.put("hid", context.getHomeworkId());
        param.put("packageId", context.getPackageId());
        param.put("bookId", context.getBookId());
        param.put("unitId", context.getUnitId());
        param.put("lessonId", context.getLessonId());
        param.put("pointId", context.getPointId());
        param.put("dataType", context.getDataType());
        param.put("studyType", context.getStudyType());
        param.put("cid", context.getClazzId());
        param.put("questionNum", context.getQuestionNum());

        flashVars.put("json",JsonUtils.toJson(param));
        String flashVarsJson = JsonUtils.toJson(flashVars);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", context.getMathPractice().getFilename());
        result.put("gameSize", context.getMathPractice().getGameSize());
        result.put("lessonId", context.getLessonId());
        result.put("needRecord", false);
        result.put("flashVars", flashVarsJson);
        result.put("version", context.getMathPractice().getMobileVersion());
        result.put("type", context.getMathPractice().getId());
        Map queryParams = MiscUtils.map("param", JsonUtils.toJson(param),
                "practiceName", context.getMathPractice().getFilename(),
                "subject", Subject.MATH,
                "pointId", context.getPointId(),
                "amount", context.getQuestionNum(),
                "flashGameName", context.getMathPractice().getFilename(),
                "homeworkCorrectId", homeworkCorrectId,
                "unitId", unitId,
                "lessonId", lessonId,
                "practiceId", practiceId);

        //有广告劫持，url后面加随机版本
        result.put("gameDataURL", UrlUtils.buildUrlQuery("homeworkcorrectflash/content" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams)); //10表示出题个数
        //result.put("gameExtraDataURL", UrlUtils.buildUrlQuery("interactive/flash/obtainextradata/math/" + context.getMathPractice().getFilename() + Constants.AntiHijackExt, queryParams));
        result.put("gameExtraDataURL", UrlUtils.buildUrlQuery("interactive/flash/obtainextradata/math" + Constants.AntiHijackExt + "?flashGameName=" + context.getMathPractice().getFilename(), queryParams));
        result.put("endPath", UrlUtils.buildUrlQuery("homeworkcorrectflash/processback" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(),
                MiscUtils.map("homeworkCorrectId", homeworkCorrectId, "unitId", unitId, "lessonId", lessonId, "pointId", context.getPointId(), "practiceId", practiceId)));
//        result.put("endPath", "homeworkcorrectflash/processback.vpage"  + "?v=" + System.currentTimeMillis()+"&homeworkCorrectId=" + homeworkCorrectId + "&unitId=" + unitId + "&lessonId=" + lessonId + "&pointId=" + context.getPointId() + "&practiceId=" + practiceId);

        return result;
    }

}
