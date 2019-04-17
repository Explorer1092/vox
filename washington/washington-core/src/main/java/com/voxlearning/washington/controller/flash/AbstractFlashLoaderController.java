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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.service.LoadFlashGameContext;
import com.voxlearning.washington.service.LoadFlashGameContextFactory;
import com.voxlearning.washington.support.AbstractController;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 14-10-21.
 */
abstract public class AbstractFlashLoaderController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    //todo 把词汇类数据改成json传输协议时，需要同时做单词收集处理，json的sentence数据需要添加完成时长finishTime和是否掌握correct
    public static final List<String> getXmlPractice() {
        List<String> xmlPractice = Arrays.asList("CrazyPub",
                "ColorCandy", "VoxPronounceStudy", "fuxiaoRecite",
                "fish", "SpaceTravel",
                "ZombiesGameForLowLevel",
                "Rescue", "PolarRun", "HotRhythm",
                "DiffPick");

        if (RuntimeMode.isStaging()) {
            xmlPractice = Arrays.asList("CrazyPub",
                    "ColorCandy", "VoxPronounceStudy", "fuxiaoRecite",
                    "fish", "SpaceTravel",
                    "ZombiesGameForLowLevel",
                    "Rescue", "PolarRun", "HotRhythm",
                    "DiffPick");
        }

        if (RuntimeMode.isProduction()) {
            xmlPractice = Arrays.asList("CrazyPub",
                    "ColorCandy", "VoxPronounceStudy", "fuxiaoRecite",
                    "fish", "SpaceTravel",
                    "ZombiesGameForLowLevel",
                    "Rescue", "PolarRun", "HotRhythm",
                    "DiffPick");
        }
        return xmlPractice;
    }

    protected Map englishSelfStudy(Long type, Long userId, Long bookId, Long unitId, Long lessonId) {
        User user = raikouSystem.loadUser(userId);
        Long clazzId = 0L;
        if (user != null && user.getUserType() == UserType.STUDENT.getType()) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
            if (clazz != null) {
                clazzId = clazz.getId();
            }
        } else {
            userId = 0L;
        }
        PracticeType englishPractice = practiceLoaderClient.loadPractice(type);
        Book book = englishContentLoaderClient.loadEnglishBook(bookId);
        String ktwelve = Ktwelve.PRIMARY_SCHOOL.name();
        Map<String, Object> data = new HashMap<>();
        if (book != null) {
            ktwelve = Ktwelve.of(book.getBookType()).name();
            LoadFlashGameContext context = LoadFlashGameContextFactory.selfstudy(englishPractice, userId, clazzId, bookId, unitId, lessonId);
            data = loadEnglishGameFlash(context);
        }
        return data;
    }

    protected Map newSelfStudy(Long type, Long userId, String lessonId, String qids, String bookId) {
        PracticeType practiceType = practiceLoaderClient.loadPractice(type);
        LoadFlashGameContext context = LoadFlashGameContextFactory.newSelfstudy(practiceType, userId, lessonId, qids, bookId);
        return loadNewSelfstudyGameFlash(context);
    }

    protected Map newPictureBookSelfStudy(Long userId, String pictureBookId) {
        PracticeType practice = practiceLoaderClient.loadPractice(67);
        LoadFlashGameContext context = LoadFlashGameContextFactory.newPictureBookSelfstudy(practice, userId, pictureBookId);
        return loadNewSelfstudyGameFlash(context);
    }

    protected Map englishHomework(StudentDetail studentDetail, Long type,
                                  Long bookId,
                                  Long unitId,
                                  Long lessonId,
                                  String hid) {
        Clazz clazz = studentDetail.getClazz();
        PracticeType englishPractice = practiceLoaderClient.loadPractice(type);
        LoadFlashGameContext context = LoadFlashGameContextFactory.homework(englishPractice, studentDetail.getId(), clazz.getId(), hid, bookId, unitId, lessonId);

        //作业上传录音的游戏需要提供数据分析参数年级和地理信息
        if (context.getEnglishPractice().fetchNeedRecord()) {
            context.setClazzLevel(clazz.fetchClazzLevel().getLevel());
            if (studentDetail.getStudentSchoolRegionCode() != null && studentDetail.getStudentSchoolRegionCode() != 0) {
                ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
                if (exRegion != null) {
                    context.setPrvRgnCode(exRegion.getProvinceCode());
                }
            }
        }
        Map<String, Object> data = loadEnglishGameFlash(context);
        return data;
    }

    // lessonId/pictureBookId是复用属性
    protected Map newHomework(StudentDetail studentDetail, Long practiceId, String lessonId, String pictureBookId, String hid, String homeworkType, String objectiveConfigType) {
        Clazz clazz = studentDetail.getClazz();
        Long clazzId = clazz != null ? clazz.getId() : 0;
        PracticeType practice = practiceLoaderClient.loadPractice(practiceId);
        LoadFlashGameContext context = LoadFlashGameContextFactory.newHomework(practice, studentDetail.getId(), clazzId, hid, lessonId, pictureBookId, homeworkType, objectiveConfigType);
        context.setNewHomeworkType(homeworkType);

        // 作业上传录音的游戏需要提供数据分析参数年级和地理信息,这段代码还有用么？没用就删了吧，xuesong.zhang 2016-10-19
        if (clazz != null) {
            if (context.getEnglishPractice().fetchNeedRecord()) {
                context.setClazzLevel(clazz.getClazzLevel().getLevel());
                if (studentDetail.getStudentSchoolRegionCode() != null && studentDetail.getStudentSchoolRegionCode() != 0) {
                    ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
                    if (exRegion != null) {
                        context.setPrvRgnCode(exRegion.getProvinceCode());
                    }
                }
            }
        }

        Map<String, Object> data = loadNewHomeworkGameFlash(context);
        return data;
    }


}
