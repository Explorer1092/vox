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

package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.v1.content.ContentApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;


/**
 * Created by jiangpeng on 16/7/4.
 * 壳分享自学课本和单元,提供课本内容和单元内容给h5
 * 随声听的课本相亲和单元详情接口
 * 诶 不用验证登陆 直接爬数据
 */
@Controller
@RequestMapping(value = "/userMobile/selfstudy")
@Slf4j
public class MobileUserSelfStudyShareController extends AbstractMobileController {


    private static List<String> filterLesson = Arrays.asList("语感练习", "听力练习", "语法练习");
    private static String bookImgUrlPrefix = "http://cdn-cnc.17zuoye.cn/resources/app/jzt/res/{0}.png";



    @RequestMapping(value = "/book/share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage bookShare() {

        String bookId = getRequestString("book_id");
        if( StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("book_id error");

        Map<String, NewBookProfile> stringNewBookProfileMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));

        if(MapUtils.isEmpty(stringNewBookProfileMap))
            return MapMessage.errorMessage("no such book");
        NewBookProfile newBookProfile = stringNewBookProfileMap.get(bookId);

        if(newBookProfile == null){
            return MapMessage.errorMessage(RES_RESULT_BOOK_ERROR);
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
            Map<String, NewBookCatalog> moduleMap = moduleList.stream().collect(Collectors.toMap(NewBookCatalog::getId, t -> (t)));

            for (Map.Entry<String, List<NewBookCatalog>> entry : moduleId2UnitListMap.entrySet()) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = entry.getKey();
                List<NewBookCatalog> unitList = entry.getValue();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getName());
                    addIntoMap(unitGroupInfo, RES_UNIT_ENAME, unit.getAlias());
                    addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                    groupUnitList.add(unitGroupInfo);
                }

                NewBookCatalog module = moduleMap.get(moduleId);
                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_GROUP_ENAME, module.getAlias());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupList.add(groupInfo);

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }
        return MapMessage.successMessage().add("book_info",bookMap);
    }


    @RequestMapping(value = "/unit/share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitShare() {

        String unitId = getRequestString("unit_id");
        if(StringUtils.isBlank(unitId))
            return MapMessage.errorMessage("unit 错误");


        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if(unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())){
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        }
        String bookId = getRequestString("book_Id");
        if(StringUtils.isBlank(bookId)) {
            List<NewBookCatalogAncestor> unitAncestors = unit.getAncestors();
            if (CollectionUtils.isEmpty(unitAncestors))
                return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
            for (NewBookCatalogAncestor ancestor : unitAncestors) {
                if (BookCatalogType.BOOK.name().equals(ancestor.getNodeType())) {
                    bookId = ancestor.getId();
                }
            }
        }
        if(bookId == null)
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        Map<String, List<NewBookCatalog>> book2UnitListMap = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT);
        if(MapUtils.isEmpty(book2UnitListMap))
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);

        List<NewBookCatalog> unitList = book2UnitListMap.get(bookId);
        if(CollectionUtils.isEmpty(unitList))
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);

        String nextUnitId = "";
        for(int i=0;i<unitList.size();i++){
            NewBookCatalog unitItem = unitList.get(i);
            if(i == unitList.size()-1) {
                nextUnitId = "";
                break;
            }
            if(unitItem.getId().equals(unit.getId())){
                nextUnitId = unitList.get(i+1).getId();
                break;
            }

        }

        Map<String, List<NewBookCatalog>> unti2LessonListMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
        if(MapUtils.isEmpty(unti2LessonListMap)){
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        }

        List<NewBookCatalog> lessonList = unti2LessonListMap.get(unitId);
        if(CollectionUtils.isEmpty(lessonList)){
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        }
        Set<String> lessonIds = lessonList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());

        Map<String, List<Sentence>> lesson2SentenceListMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIds);

        if(MapUtils.isEmpty(lesson2SentenceListMap)){
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
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
        unitInfo.put("next_unit_id",nextUnitId);


        return MapMessage.successMessage().add("unit_info",unitInfo);
    }

    private Map<String,Object> convert2BookMapForOld(NewBookProfile bookProfile, NewBookCatalog newBookCatalog){
        Map<String,Object> map = new LinkedHashMap<>();
        addIntoMap(map, ContentApiConstants.RES_BOOK_ID,bookProfile.getId());
        addIntoMap(map,RES_BOOK_CNAME,bookProfile.getName());
        addIntoMap(map,RES_BOOK_ENAME,bookProfile.getAlias());
        addIntoMap(map,RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map,RES_CLAZZ_LEVEL,bookProfile.getClazzLevel());
        addIntoMap(map,RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map,RES_BOOK_TERM, bookProfile.getTermType());

        if(newBookCatalog != null){
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), newBookCatalog.getName());
            if (bookPress != null) {
                addIntoMap(map,RES_BOOK_VIEW_CONTENT,bookPress.getViewContent());
                addIntoMap(map,RES_BOOK_COLOR,bookPress.getColor());
                addIntoMap(map,RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix,bookPress.getColor()));
            }
        }
        return map;
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
