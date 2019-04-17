/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.washington.support.AbstractController;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-1-3
 */
@Controller
@RequestMapping("/interactive/flash")
public class InteractiveFlashHomeworkGameController extends AbstractController {

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //        获取作业游戏除了习题之外的数据                        //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    @RequestMapping(value = "obtainextradata/math.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map obtainMathExtraData(@RequestParam("flashGameName") String flashGameName) {
        return new LinkedHashMap();
    }

    @RequestMapping(value = "colorcandygetreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public List<Map> colorCandyGetReport() {
        return Collections.emptyList();
    }


    /**
     * 英语flash应用交互数据上传
     */
    @RequestMapping(value = "ENGLISH/process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSkyscraper() {
        Map<String, Object> command = new HashMap<>();
        try {
            @Cleanup InputStream inStream = getRequest().getInputStream();
            String content = IOUtils.toString(inStream, "UTF-8");
            command = JsonUtils.fromJson(content, Map.class);
            Validate.notNull(command, "Convert json to pojo failed, the json is: %s", content);
        } catch (Exception ex) {
            logger.error("Error occursing when parsing input stream", ex);
            return MapMessage.errorMessage("Error occursing when parsing input stream");
        }
        Long bookId = conversionService.convert(command.get("bookId"), Long.class);
        Long unitId = conversionService.convert(command.get("unitId"), Long.class);
        Long lessonId = conversionService.convert(command.get("lessonId"), Long.class);
        Long practiceId = conversionService.convert(command.get("gameType"), Long.class);
        Integer score = conversionService.convert(command.get("score"), Integer.class);
        Map<String, Object> detailData = JsonUtils.fromJson(JsonUtils.toJson(command.get("detailData")));

        return businessStudentServiceClient.saveJuniorAppDetail(currentUserId(), bookId, unitId, lessonId, practiceId, score, detailData);
    }

    /**
     * 获取英语flash应用交互数
     */
    @RequestMapping(value = "ENGLISH/getrank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSkyscraperRank(@RequestBody Map<String, Object> command) {
        Long bookId = conversionService.convert(command.get("bookId"), Long.class);
        Long unitId = conversionService.convert(command.get("unitId"), Long.class);
        Long lessonId = conversionService.convert(command.get("lessonId"), Long.class);
        Long practiceId = conversionService.convert(command.get("gameType"), Long.class);
        Integer tag = conversionService.convert(command.get("tag"), Integer.class);
        Lesson lesson = englishContentLoaderClient.loadEnglishLesson(lessonId);
        String lessonName = "";
        if (tag != 0) {
            List<Lesson> lessons = englishContentLoaderClient.loadEnglishUnitLessons(unitId);
            if (lessons != null && lessons.size() > 0) {
                int index = lessons.indexOf(lesson);
                if (tag < 0) {
                    if (index > 0) {
                        lesson = lessons.get(index - 1);
                        if (lesson != null) {
                            lessonId = lesson.getId();
                        }
                    } else {
                        lesson = lessons.get(lessons.size() - 1);
                        lessonId = lesson.getId();
                    }
                } else {
                    if (index < (lessons.size() - 1)) {
                        lesson = lessons.get(index + 1);
                        if (lesson != null) {
                            lessonId = lesson.getId();
                        }
                    } else {
                        lesson = lessons.get(0);
                        lessonId = lesson.getId();
                    }
                }
            }

        }
        lessonName = lesson.getCname();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
        if (clazz != null) {
            MapMessage mapMessage = businessStudentServiceClient.findClazzRank(clazz.getId(), bookId, unitId, lessonId, practiceId);
            mapMessage.put("cdnUrl", getCdnBaseUrlStaticSharedWithSep());
            mapMessage.put("lessonName", lessonName);
            mapMessage.put("lessonId", lessonId);
            return mapMessage;
        } else {
            return MapMessage.errorMessage("用户未加入班级");
        }
    }
}
