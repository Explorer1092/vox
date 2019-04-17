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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.controller.open.v1.content.ContentLoaderWrapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * 自学，随身听相关
 * Created by Fanshuo Liu on 2015/8/5.
 */
@Controller
@RequestMapping(value = "/v1/student")
@Slf4j
public class StudentSelfStudyApiController extends AbstractStudentApiController {
    @Inject
    private ContentLoaderWrapperFactory contentLoaderWrapperFactory;

    private static List<String> filterLesson = Arrays.asList("语感练习", "听力练习", "语法练习");



    // 这个是对应英语学科的功能
    /**
     * 返回这个学生的课本信息
     * 如果是家长,则返回传入学生id的课本信息.
     * @return
     */
    @RequestMapping(value = "/selfloadbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage selfloadbook() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequest(REQ_SUBJECT);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String sys = getRequestString(REQ_SYS);

        StudentDetail studentDetail = getCurrentStudentDetail();
        if(studentDetail == null){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
            return resultMap;
        }
        if(ClazzLevel.MIDDLE_GRADUATED.equals(studentDetail.getClazzLevel()) || studentDetail.isJuniorStudent()){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_LEVEL_ERROR);
            return resultMap;
        }
        NewBookProfile newBookProfile = parentSelfStudyPublicHelper.loadDefaultSelfStudyBook(studentDetail, SelfStudyType.WALKMAN_ENGLISH, false, sys);
        if(newBookProfile == null){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
            return resultMap;
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String,Object> bookMap = convert2BookMapForOld(newBookProfile,newBookCatalog);

        Map<String, List<NewBookCatalog>> bookId2ModuleMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.MODULE);
        List<NewBookCatalog> moduleList = bookId2ModuleMap==null ? new ArrayList<>(): bookId2ModuleMap.get(newBookProfile.getId());
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);


        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();
            Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient
                    .loadChildren(Collections.singleton(newBookProfile.getId()),BookCatalogType.UNIT);
            List<NewBookCatalog> unitList = bookId2UnitListMap==null ? new ArrayList<>(): bookId2UnitListMap.get(newBookProfile.getId());
            if(!CollectionUtils.isEmpty(unitList)){
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitMap = new LinkedHashMap<>();
                    unitMap.put(RES_UNIT_ID, unit.getId());
                    unitMap.put(RES_RANK, unit.getRank());
                    unitMap.put(RES_UNIT_CNAME, unit.getName());
                    unitMap.put(RES_UNIT_ENAME, unit.getAlias());
                    unitMapList.add(unitMap);
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }

        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, BookCatalogType.UNIT);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }

            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(module.getId());
                if (unitList == null)
                    unitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getName());
                    addIntoMap(unitGroupInfo, RES_UNIT_ENAME, unit.getAlias());
                    addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                    groupUnitList.add(unitGroupInfo);
                }

                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_GROUP_ENAME, module.getAlias());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupList.add(groupInfo);

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }

        return resultMap.add(RES_RESULT,RES_RESULT_SUCCESS).add(RES_USER_BOOK,bookMap);

    }

    // 这个是对应英语学科的功能
    /**
     * 返回课本的单元信息
     * @return
     */
    @RequestMapping(value = "/selfloadurl.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getUnitInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequired(REQ_UNIT_ID, "单元ID");
            validateRequest(REQ_SUBJECT, REQ_UNIT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        
        String unitId = getRequestString(RES_UNIT_ID);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        if (subject == Subject.ENGLISH) {

            NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
            if(unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }

            Map<String, List<NewBookCatalog>> unti2LessonListMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
            if(MapUtils.isEmpty(unti2LessonListMap)){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }

            List<NewBookCatalog> lessonList = unti2LessonListMap.get(unitId);
            if(CollectionUtils.isEmpty(lessonList)){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }
            Set<String> lessonIds = lessonList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());

            Map<String, List<Sentence>> lesson2SentenceListMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIds);

            if(MapUtils.isEmpty(lesson2SentenceListMap)){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }
            Map<String, Object> unitInfo = new LinkedHashMap<>();
            List<Map<String,Object>> lessonInfo = new LinkedList<>();

            for (NewBookCatalog lesson : lessonList) {
                List<Map> sentenceInfo = new LinkedList<>();

                if (!lesson2SentenceListMap.containsKey(lesson.getId())) {
                    continue;
                }
                List<Sentence> sentencesList = lesson2SentenceListMap.get(lesson.getId());
                for (Sentence sentence : sentencesList) {
                    if (StringUtils.isNotBlank(sentence.getWaveUri())) {
                        Map<String, Object> sentenceInfoMap = new LinkedHashMap<>();
                        sentenceInfoMap.put(RES_SENTENCE_ID, sentence.getId());
                        sentenceInfoMap.put(RES_SENTENCE_CNTEXT, sentence.getCnText());
                        sentenceInfoMap.put(RES_SENTENCE_ENTEXT, sentence.getEnText());
                        String url = getCdnBaseUrlStaticSharedWithSep() + sentence.getWaveUri();
                        sentenceInfoMap.put(RES_SENTENCE_WAVE_URI, url);
                        sentenceInfo.add(sentenceInfoMap);
                    }
                }
                if (sentenceInfo.size() > 0) {
                    if (containsNeedFilterLessons(lesson.getName())) {
                        continue;
                    }
                    Map<String, Object> lessonInfoMap = new LinkedHashMap<>();
                    lessonInfoMap.put(RES_LESSON_CNAME, lesson.getName());
                    lessonInfoMap.put(RES_LESSON_ENAME, lesson.getAlias());
                    lessonInfoMap.put(RES_LESSON_ID, lesson.getId());
                    lessonInfoMap.put(RES_SENTENCE_LIST, sentenceInfo);

                    lessonInfo.add(lessonInfoMap);
                }

            }

            unitInfo.put(RES_UNIT_CNAME, unit.getName());
            unitInfo.put(RES_UNIT_ENAME, unit.getAlias());
            unitInfo.put(RES_GROUP_CNAME, "xxx1232xxx");
            unitInfo.put(RES_GROUP_ENAME, "xxx13213xxxxE");
            unitInfo.put(RES_RANK, unit.getRank());
            unitInfo.put(RES_LESSON_LIST, lessonInfo);

            resultMap.add(RES_UNIT, unitInfo);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } else {
            resultMap.add(RES_RESULT, "Subject is wrong.");
            return resultMap;
        }
    }

    /**
     * 过滤几种（根据filterLesson定义）lesson
     *
     * @param lessonName
     * @return
     */
    private boolean containsNeedFilterLessons(String lessonName) {
        if (StringUtils.isBlank(lessonName)) {
            return false;
        }
        for (String name : filterLesson) {
            if (lessonName.contains(name)) {
                return true;
            }
        }
        return false;
    }

}
