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

package com.voxlearning.washington.service.support;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.washington.service.LoadFlashGameContext;
import com.voxlearning.washington.support.WashingtonRequestContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.AllowUserTokenTypes;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.USTALK_MOVE_DATE;

/**
 * For loading flash game.
 *
 * @author Xiaohai Zhang
 * @since 2013-06-08 13:12
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract public class GameFlashLoaderTemplate extends SpringContainerSupport {

    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject private GameFlashLoaderConfigManager gameFlashLoaderConfigManager;

    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    private StudyType studyType;

    private static final Map<StudyType, GameFlashLoaderTemplate> registry = new HashMap<>();

    public static GameFlashLoaderTemplate getGameFlashLoaderTemplate(StudyType studyType) {
        return registry.get(studyType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        IdentificationStudyType annotation = getClass().getAnnotation(IdentificationStudyType.class);
        studyType = annotation.value();
        registry.put(annotation.value(), this);
    }

    public final Map<String, Object> load(WashingtonRequestContext requestContext, LoadFlashGameContext context) {
        Validate.notNull(context);
        verifyContext(context);
        String json = buildJson(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getEnglishPractice().getFrameType()) ? context.getEnglishPractice().getFrameType() : context.getEnglishPractice().getFilename();
        String gameName = context.getEnglishPractice().getFilename();
        boolean needSaveVoice = (studyType != StudyType.selfstudy && context.getEnglishPractice().fetchUploadVoice());
        flashVars.put("json", json);
        flashVars.put("lessonId", context.getLessonId());
        flashVars.put("type", context.getEnglishPractice().getId());
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计
        flashVars.put("flashId", file); //   框架类游戏赋值框架类型，非框架类游戏赋值游戏文件名
        flashVars.put("appId", gameName); //游戏文件名
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("saveVoice", needSaveVoice ? "1" : "0");
        flashVars.put("voicePassScore", "30");   //TODO: 现在引擎就按30分算通过吧，将来需要进一步优化引擎
        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        /**
         * 阅读理解需要提供特殊字段
         * tts_url  tts访问路径
         * isPreview  (boo==0)?false:true    是否是预览状态  预览状态的话  加载页面传来的数据
         * isTeacher  (boo==0)?false:true    是否是老师  老师有较高的控制权
         */
        if (NewHomeworkConstants.SPECIAL_PRACTICE_TYPE_ENGLISH_READING == context.getEnglishPractice().getId()) {
            flashVars.put("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            flashVars.put("isPreview", 0);
            //如果是测试环境需要可以点击下一页，快速通过
//            if (RuntimeMode.lt(RuntimeMode.Mode.STAGING)) {
//                flashVars.put("isTeacher", 1);
//            } else {
            flashVars.put("isTeacher", 0);
//            }
        }

        //增加额外的flashvars
        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, gameName);

        String flashVarsJson = JsonUtils.toJson(flashVars);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file);
        result.put("gameSize", context.getEnglishPractice().getGameSize());
        result.put("gameName", context.getEnglishPractice().getFilename());
        result.put("lessonId", context.getLessonId());
        result.put("type", context.getEnglishPractice().getId());
        result.put("flashVars", flashVarsJson);
        result.put("needRecord", context.getEnglishPractice().fetchNeedRecord());

        //有广告劫持，url后面加随机版本
        result.put("getRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/getrank" + Constants.AntiHijackExt); //获取应用排行数据
        result.put("sendRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/process" + Constants.AntiHijackExt);//保存应用交互数据

        Map queryParams = MapUtils.map("practiceName", context.getEnglishPractice().getFilename(),
                "subject", Subject.ENGLISH,
                "lessonId", context.getLessonId());

        //有广告劫持，url后面加随机版本
        result.put("gameDataURL", UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams));
        result.put("endPath", UrlUtils.buildUrlQuery("flash/" + context.getEnglishPractice().getFilename() + "/process" + Constants.AntiHijackExt, MapUtils.map("postType", "json")));

        result.put("version", context.getEnglishPractice().getMobileVersion());

        return result;
    }

    public final Map<String, Object> loadNewSelfStudy(WashingtonRequestContext requestContext, LoadFlashGameContext context) {
        Validate.notNull(context);
//        verifyContext(context);
        String json = buildJson(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getEnglishPractice().getFrameType()) ? context.getEnglishPractice().getFrameType() : context.getEnglishPractice().getFilename();
        String gameName = context.getEnglishPractice().getFilename();
        boolean needSaveVoice = (studyType != StudyType.selfstudy && context.getEnglishPractice().fetchUploadVoice());
        flashVars.put("json", json);
        flashVars.put("lessonId", context.getNewLessonId());
        flashVars.put("type", context.getEnglishPractice().getId());
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计
        flashVars.put("flashId", file); //   框架类游戏赋值框架类型，非框架类游戏赋值游戏文件名
        flashVars.put("appId", gameName); //游戏文件名
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("saveVoice", needSaveVoice ? "1" : "0");
        flashVars.put("voicePassScore", "30");   //TODO: 现在引擎就按30分算通过吧，将来需要进一步优化引擎
        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        /**
         * 阅读理解需要提供特殊字段
         * tts_url  tts访问路径
         * isPreview  (boo==0)?false:true    是否是预览状态  预览状态的话  加载页面传来的数据
         * isTeacher  (boo==0)?false:true    是否是老师  老师有较高的控制权
         */
        if (NewHomeworkConstants.SPECIAL_PRACTICE_TYPE_ENGLISH_READING == context.getEnglishPractice().getId()) {
            flashVars.put("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            flashVars.put("isPreview", 0);
            //如果是测试环境需要可以点击下一页，快速通过
//            if (RuntimeMode.current().lt(RuntimeMode.Mode.STAGING)) {
//                flashVars.put("isTeacher", 1);
//            } else {
            flashVars.put("isTeacher", 0);
//            }
        }

        //增加额外的flashvars
        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, gameName);

        String flashVarsJson = JsonUtils.toJson(flashVars);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file);
        result.put("gameSize", context.getEnglishPractice().getGameSize());
        result.put("gameName", context.getEnglishPractice().getFilename());
        result.put("lessonId", context.getNewLessonId());
        result.put("type", context.getEnglishPractice().getId());
        result.put("flashVars", flashVarsJson);
        result.put("needRecord", context.getEnglishPractice().fetchNeedRecord());

        //有广告劫持，url后面加随机版本
        result.put("getRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/getrank" + Constants.AntiHijackExt); //获取应用排行数据
        result.put("sendRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/process" + Constants.AntiHijackExt);//保存应用交互数据

        Map queryParams = MapUtils.map("practiceName", context.getEnglishPractice().getFilename(),
                "subject", Subject.ENGLISH,
                "lessonId", context.getNewLessonId(),
                "practiceId", context.getEnglishPractice().getId(),
                "categoryId", context.getEnglishPractice().getCategoryId(),
                "pictureBookId", context.getPictureBookId(),
                "qids", context.getQids(),
                "bookId", context.getNewBookId());

        //有广告劫持，url后面加随机版本
        result.put("gameDataURL", UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams));
        result.put("endPath", UrlUtils.buildUrlQuery("flash/" + context.getEnglishPractice().getFilename() + "/process" + Constants.AntiHijackExt, MapUtils.map("postType", "json")));

        result.put("version", context.getEnglishPractice().getMobileVersion());

        return result;
    }

    public final Map<String, Object> loadEnglishHomeworkCorrect(WashingtonRequestContext requestContext, LoadFlashGameContext context, String homeworkCorrectId, Long unitId, Long lessonId, Long pointId, Long practiceId) {
        Validate.notNull(context);
        verifyContext(context);
        String json = buildJson(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getEnglishPractice().getFrameType()) ? context.getEnglishPractice().getFrameType() : context.getEnglishPractice().getFilename();
        String gameName = context.getEnglishPractice().getFilename();
        boolean needSaveVoice = (studyType != StudyType.selfstudy && context.getEnglishPractice().fetchUploadVoice());
        flashVars.put("json", json);
        flashVars.put("lessonId", context.getLessonId());
        flashVars.put("type", context.getEnglishPractice().getId());
        flashVars.put("studyType", context.getStudyType().name());  //增加学习类型，自学还是作业，用于log统计
        flashVars.put("flashId", file); //   框架类游戏赋值框架类型，非框架类游戏赋值游戏文件名
        flashVars.put("appId", gameName); //游戏文件名
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("saveVoice", needSaveVoice ? "1" : "0");
        flashVars.put("voicePassScore", "30");   //TODO: 现在引擎就按30分算通过吧，将来需要进一步优化引擎
        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        /**
         * 阅读理解需要提供特殊字段
         * tts_url  tts访问路径
         * isPreview  (boo==0)?false:true    是否是预览状态  预览状态的话  加载页面传来的数据
         * isTeacher  (boo==0)?false:true    是否是老师  老师有较高的控制权
         */
        if (NewHomeworkConstants.SPECIAL_PRACTICE_TYPE_ENGLISH_READING == context.getEnglishPractice().getId()) {
            flashVars.put("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            flashVars.put("isPreview", 0);
            //如果是测试环境需要可以点击下一页，快速通过
//            if (RuntimeMode.lt(RuntimeMode.Mode.STAGING)) {
//                flashVars.put("isTeacher", 1);
//            } else {
            flashVars.put("isTeacher", 0);
//            }
        }

        //增加额外的flashvars
        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, gameName);

        String flashVarsJson = JsonUtils.toJson(flashVars);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file);
        result.put("gameSize", context.getEnglishPractice().getGameSize());
        result.put("gameName", context.getEnglishPractice().getFilename());
        result.put("lessonId", context.getLessonId());
        result.put("type", context.getEnglishPractice().getId());
        result.put("flashVars", flashVarsJson);
        result.put("needRecord", context.getEnglishPractice().fetchNeedRecord());

        //有广告劫持，url后面加随机版本
        result.put("getRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/getrank" + Constants.AntiHijackExt); //获取应用排行数据
        result.put("sendRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/process" + Constants.AntiHijackExt);//保存应用交互数据

        result.put("gameDataURL", "homeworkcorrectflash/content.vpage" + "?v=" + System.currentTimeMillis() + "&flashGameName=" + context.getEnglishPractice().getFilename() + "&homeworkCorrectId=" + homeworkCorrectId + "&unitId=" + unitId + "&lessonId=" + lessonId + "&practiceId=" + practiceId);
        result.put("endPath", "homeworkcorrectflash/processback.vpage" + "?v=" + System.currentTimeMillis() + "&homeworkCorrectId=" + homeworkCorrectId + "&unitId=" + unitId + "&lessonId=" + lessonId + "&pointId=" + pointId + "&practiceId=" + practiceId);

        result.put("version", context.getEnglishPractice().getMobileVersion());

        return result;
    }

    public final Map<String, Object> loadNewHomework(WashingtonRequestContext requestContext, LoadFlashGameContext context) {
        Validate.notNull(context);
        verifyContext(context);
        String json = buildJson(context);
        Map<String, Object> flashVars = new LinkedHashMap<>();
        String file = StringUtils.isNotBlank(context.getEnglishPractice().getFrameType()) ? context.getEnglishPractice().getFrameType() : context.getEnglishPractice().getFilename();
        String gameName = context.getEnglishPractice().getFilename();
        boolean needSaveVoice = (studyType != StudyType.selfstudy && context.getEnglishPractice().fetchUploadVoice());
        flashVars.put("json", json);
        //
        flashVars.put("lessonId", context.getNewLessonId());
        flashVars.put("pictureBookId", context.getPictureBookId());
        flashVars.put("type", context.getEnglishPractice().getId());
        if (StringUtils.equalsIgnoreCase(context.getNewHomeworkType(), NewHomeworkType.WinterVacation.name())) {
            flashVars.put("studyType", StudyType.vacationHomework);
        } else {
            flashVars.put("studyType", context.getStudyType().name());
        }
        flashVars.put("flashId", file); //   框架类游戏赋值框架类型，非框架类游戏赋值游戏文件名
        flashVars.put("appId", gameName); //游戏文件名
        flashVars.put("studyCompleted", "parent.refreshHomeWorkState");
        flashVars.put("nextHomeWork", "parent.nextHomeWork");
        flashVars.put("saveVoice", needSaveVoice ? "1" : "0");
        flashVars.put("voicePassScore", "30");   //TODO: 现在引擎就按30分算通过吧，将来需要进一步优化引擎
        flashVars.put("newHomeworkType", context.getNewHomeworkType());
        flashVars.put("objectiveConfigType", context.getObjectiveConfigType());
        User currentUser = requestContext.getCurrentUser();
        if (currentUser != null) {
            flashVars.put("userType", currentUser.getUserType());
        }

        /**
         * 阅读理解需要提供特殊字段
         * tts_url  tts访问路径
         * isPreview  (boo==0)?false:true    是否是预览状态  预览状态的话  加载页面传来的数据
         * isTeacher  (boo==0)?false:true    是否是老师  老师有较高的控制权
         */
        if (NewHomeworkConstants.SPECIAL_PRACTICE_TYPE_ENGLISH_READING == context.getEnglishPractice().getId()) {
            flashVars.put("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
            flashVars.put("isPreview", 0);
            //如果是测试环境需要可以点击下一页，快速通过
//            if (RuntimeMode.current().lt(RuntimeMode.Mode.STAGING)) {
//                flashVars.put("isTeacher", 1);
//            } else {
            flashVars.put("isTeacher", 0);
//            }
        }

        //增加额外的flashvars
        gameFlashLoaderConfigManager.setupFlashUrl(flashVars, requestContext.getRequest(), file, gameName);

        String flashVarsJson = JsonUtils.toJson(flashVars);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file);
        result.put("gameSize", context.getEnglishPractice().getGameSize());
        result.put("gameName", context.getEnglishPractice().getFilename());
        result.put("lessonId", context.getNewLessonId());
        result.put("pictureBookId", context.getPictureBookId());
        result.put("type", context.getEnglishPractice().getId());
        result.put("flashVars", flashVarsJson);
        result.put("needRecord", context.getEnglishPractice().fetchNeedRecord());

        //有广告劫持，url后面加随机版本
        result.put("getRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/getrank" + Constants.AntiHijackExt); //获取应用排行数据
        result.put("sendRankDataUrl", "/interactive/flash/" + Subject.ENGLISH.name() + "/process" + Constants.AntiHijackExt);//保存应用交互数据

        Map queryParams = MapUtils.map("practiceName", context.getEnglishPractice().getFilename(),
                "subject", Subject.ENGLISH,
                "lessonId", context.getNewLessonId(),
                "pictureBookId", context.getPictureBookId(),
                "hid", context.getHomeworkId(),
                "categoryId", context.getEnglishPractice().getCategoryId(),
                "practiceId", context.getEnglishPractice().getId(),
                "newHomeworkType", context.getNewHomeworkType(),
                "objectiveConfigType", context.getObjectiveConfigType(),
                "token", context.getToken());

        //有广告劫持，url后面加随机版本
        NewHomeworkType newHomeworkType = NewHomeworkType.of(context.getNewHomeworkType());

        if (AllowUserTokenTypes.contains(newHomeworkType)) {
            result.put("gameDataURL", UrlUtils.buildUrlQuery("livecast/student/homework/appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams));
            result.put("endPath", UrlUtils.buildUrlQuery("livecast/student/homework/batch/processresult" + Constants.AntiHijackExt, MapUtils.m("token", context.getToken())));
        } else {
            result.put("gameDataURL", UrlUtils.buildUrlQuery("appdata/obtain" + Constants.AntiHijackExt + "?v=" + System.currentTimeMillis(), queryParams));
            result.put("endPath", UrlUtils.buildUrlQuery("/exam/flash/newhomework/batch/processresult" + Constants.AntiHijackExt, MiscUtils.m("sid", context.getUserId())));
        }
        result.put("version", context.getEnglishPractice().getMobileVersion());
        return result;
    }

    abstract protected void verifyContext(LoadFlashGameContext context);

    abstract protected String buildJson(LoadFlashGameContext context);
}
