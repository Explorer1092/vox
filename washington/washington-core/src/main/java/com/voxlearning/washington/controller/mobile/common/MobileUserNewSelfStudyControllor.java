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
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.washington.controller.mobile.AbstractMobileSelfStudyController;
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
import static com.voxlearning.washington.controller.open.ApiConstants.RES_BOOK_ID;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by jiangpeng on 16/7/11.
 * 这个类里的方法 都是不用登陆即可调用,日后可以加一层缓存.
 */
@Controller
@RequestMapping(value = "/userMobile/selfstudy")
@Slf4j
public class MobileUserNewSelfStudyControllor extends AbstractMobileSelfStudyController {


    private static String bookImgUrlPrefix = "http://cdn-cnc.17zuoye.cn/resources/app/jzt/res/{0}.png";


    /**
     * 学生端点读机
     * 学生默认教材是否需要付费
     * 磨耳朵活动需要
     * 加了一天缓存
     *
     * @return
     */
    @RequestMapping(value = "/picListen/grindear/book_need_pay.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage picListenDefaultBookNeedPay() {
        User user = currentStudent();
        if (user == null || !user.isStudent())
            return noLoginResult;
        StudentDetail studentDetail;
        if (user instanceof StudentDetail)
            studentDetail = (StudentDetail) user;
        else
            studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (studentDetail == null)
            return noLoginResult;

        Boolean bookNeedPay = false;
        TextBookManagement textBookManagement = getStudentDefaultBook(studentDetail, getRequestString(REQ_SYS), Subject.ENGLISH);
        if (textBookManagement != null && textBookManagement.picListenBookNeedPay()) {
            bookNeedPay = true;
        } else {
            textBookManagement = getStudentDefaultBook(studentDetail, getRequestString(REQ_SYS), Subject.CHINESE);
            if (textBookManagement != null && textBookManagement.picListenBookNeedPay()) {
                bookNeedPay = true;
            }
        }
        if (textBookManagement == null)
            return MapMessage.errorMessage("教材错误");

        return MapMessage.successMessage().add("need_pay", bookNeedPay).add("clazz", studentDetail.getClazzLevelAsInteger() == null ? 3 : studentDetail.getClazzLevelAsInteger())
                .add("publisher", textBookManagement.getShortPublisherName());
    }


    private TextBookManagement getStudentDefaultBook(StudentDetail studentDetail, String sys, Subject subject) {
        List<String> defaultEnglishBookList = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, sys, true, null, subject);
        if (CollectionUtils.isEmpty(defaultEnglishBookList)) {
            return null;
        }
        String fisrtBookId = defaultEnglishBookList.get(0);
        return textBookManagementLoaderClient.getTextBook(fisrtBookId);
    }


    /**
     * 学生端点读机 出版社列表
     * #48749
     *
     * @return
     */
    @RequestMapping(value = "/picListen/publisher/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage picListenPublisherList() {
        User user = currentStudent();
        if (user == null || !user.isStudent())
            return noLoginResult;
        StudentDetail studentDetail;
        if (user instanceof StudentDetail)
            studentDetail = (StudentDetail) user;
        else
            studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (studentDetail == null)
            return noLoginResult;
        List<String> publisherName = new ArrayList<>();
        publisherName.add("人教版");
        publisherName.add("外研版");
        publisherName.add("山科版");
        return MapMessage.successMessage().add("publisher_list", publisherName);
    }


    /**
     * 学生端教材列表
     * 根据传进来的出版社
     *
     * @return
     */
    @RequestMapping(value = "/picListen/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage picListenBook() {
        User user = currentStudent();
        if (user == null || !user.isStudent())
            return noLoginResult;
        StudentDetail studentDetail;
        if (user instanceof StudentDetail)
            studentDetail = (StudentDetail) user;
        else
            studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (studentDetail == null)
            return noLoginResult;
        int levelInteger = getRequestInt(REQ_CLAZZ_LEVEL, -9);
        if (levelInteger == -9)
            levelInteger = studentDetail.getClazzLevelAsInteger();

        String publisher = getRequestString("publisher");
        if (StringUtils.isBlank(publisher))
            publisher = "人教版";

        ClazzLevel level = ClazzLevel.parse(levelInteger);
        String sys = getRequestString(REQ_SYS);


        List<TextBookManagement> englishTextBookList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.ENGLISH, level.getLevel());
        List<TextBookManagement> chineseTextBookList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.CHINESE, level.getLevel());
        List<TextBookManagement> allTextBookList = new ArrayList<>(englishTextBookList);
        allTextBookList.addAll(chineseTextBookList);

        List<String> allBookId = allTextBookList.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookId);

        List<NewBookProfile> newBookProfileList = new ArrayList<>(bookProfileMap.values());
        String finalPublisher = publisher;
        List<NewBookProfile> renjiaoBookList = newBookProfileList.stream()
                .filter(t -> {
                    boolean isRenjiao = finalPublisher.equals(t.getShortPublisher());
                    if (!isRenjiao)
                        return false;
                    return textBookManagementLoaderClient.picListenBookShow(t.getId(), false, sys);
                }).collect(Collectors.toList());

        Map<String, PicListenBookPayInfo> buyLastDayMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
        List<Map<String, Object>> bookMapList = new ArrayList<>();

        for (NewBookProfile bookProfile : renjiaoBookList) {
            PicListenBookPayInfo picListenBookPayInfo = buyLastDayMap.get(bookProfile.getId());

            bookMapList.add(convert2BookMap(bookProfile, true, picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null, false));
        }
        bookMapList.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                String status1 = SafeConverter.toString(o1.get(RES_STATUS));
                String status2 = SafeConverter.toString(o2.get(RES_STATUS));
                Integer i1 = covert2Integer(status1);
                Integer i2 = covert2Integer(status2);
                return i1.compareTo(i2);
            }

            private int covert2Integer(String status) {
                if ("purchased".equals(status))
                    return -1;
                else
                    return 1;
            }
        });
        return MapMessage.successMessage().add("book_list", bookMapList).add("clazz_level", levelInteger);
    }


    /**
     * 根据年级 学科 自学类型 返回课本列表
     *
     * @return
     */
    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookList() {

        Subject subject = Subject.ofWithUnknown(getRequestString(REQ_SUBJECT));
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        SelfStudyType selfStudyType = SelfStudyType.of(getRequestString("self_study_type"));

        if (subject == Subject.UNKNOWN || level == null || selfStudyType == SelfStudyType.UNKNOWN)
            return MapMessage.errorMessage("参数错误");

        List<TextBookManagement> englishTextBookList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(selfStudyType.getSubject(), level.getLevel());
        List<TextBookManagement> allTextBookList = new ArrayList<>(englishTextBookList);

        List<String> allBookId = allTextBookList.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookId);

        List<NewBookProfile> newBookProfileList = new ArrayList<>(bookProfileMap.values());

        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
            //下线人教版教材
            newBookProfileList = newBookProfileList.stream().filter(t -> {

                Boolean iosSupport = textBookManagementLoaderClient.picListenSysSupport(t.getId(), "ios");
                Boolean androidSupport = textBookManagementLoaderClient.picListenSysSupport(t.getId(), "android");
                return (SafeConverter.toBoolean(iosSupport) || SafeConverter.toBoolean(androidSupport)) && !textBookManagementLoaderClient.picListenBookNeedPay(t);
            }).collect(Collectors.toList());
        } else if (selfStudyType == SelfStudyType.TEXTREAD_CHINESE) {
            //过滤已经校对过的语文教材
            newBookProfileList = newBookProfileList.stream().filter(t ->
                    textBookManagementLoaderClient.textReadBookShow(t.getId(), "ios") || textBookManagementLoaderClient.textReadBookShow(t.getId(), "android")
            ).collect(Collectors.toList());
        } else if (selfStudyType == SelfStudyType.WALKMAN_ENGLISH) {
            newBookProfileList = newBookProfileList.stream().filter(t ->
                    textBookManagementLoaderClient.walkManBookShow(t.getId(), "ios") || textBookManagementLoaderClient.walkManBookShow(t.getId(), "android")
            ).filter(e -> !textBookManagementLoaderClient.walkManNeedPay(e.getId())).collect(Collectors.toList());
        }

        List<Map<String, Object>> bookMapList = new ArrayList<>();
        List<String> seriesIdList = newBookProfileList.stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        for (NewBookProfile bookProfile : newBookProfileList) {
            NewBookCatalog newBookCatalog = stringNewBookCatalogMap.get(bookProfile.getSeriesId());
            bookMapList.add(convert2BookMap(bookProfile, newBookCatalog));
        }
        return MapMessage.successMessage().add(RES_BOOK_LIST, bookMapList);
    }


    /**
     * 返回指定教材的点读目录结构
     * 点读用的. 和随声听用的
     *
     * @return
     */
    @RequestMapping(value = "/book/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage picListenBookDetail() {

        String bookId = getRequestString("book_id");
        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("book_id error");

        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null)
            return MapMessage.errorMessage("没有此教材");

        if (textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile) || textBookManagementLoaderClient.walkManNeedPay(newBookProfile.getId())) {
            newBookProfile = newContentLoaderClient.loadBook(SelfStudyType.PICLISTEN_ENGLISH.getDefaultBookId());
        }


        if (newBookProfile == null) {
            return MapMessage.errorMessage(RES_RESULT_BOOK_ERROR);
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog);

        bookMap = addUnitInfo2BookMap(bookMap, newBookProfile);

        return MapMessage.successMessage().add(RES_USER_BOOK, bookMap);
    }

    /**
     * 返回指定bookid的课文朗读结构
     *
     * @return
     */
    @RequestMapping(value = "/book/textread/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage textReadBookDetail() {

        String bookId = getRequestString("book_id");
        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("book_id error");

        Map<String, NewBookProfile> stringNewBookProfileMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));

        if (MapUtils.isEmpty(stringNewBookProfileMap))
            return MapMessage.errorMessage("no such book");
        NewBookProfile newBookProfile = stringNewBookProfileMap.get(bookId);

        if (newBookProfile == null || Subject.CHINESE.getId() != newBookProfile.getSubjectId()) {
            return MapMessage.errorMessage(RES_RESULT_BOOK_ERROR);
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog);


        Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient.loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
        List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
        if (unitList == null) unitList = new ArrayList<>();
        List<String> unitIdList = unitList.stream().map(NewBookCatalog::getId).collect(Collectors.toList());
        Map<String, List<NewBookCatalog>> unitId2LessonListMap = newContentLoaderClient.loadChildren(unitIdList, BookCatalogType.LESSON);

        List<Map<String, Object>> unitInfoMapList = new ArrayList<>();

        for (NewBookCatalog unit : unitList) {
            Map<String, Object> unitInfoMap = new LinkedHashMap<>();
            addIntoMap(unitInfoMap, RES_UNIT_CNAME, unit.getName());
            addIntoMap(unitInfoMap, RES_UNIT_ID, unit.getId());
            List<Map<String, Object>> lessonMapList = new ArrayList<>();
            String unitId = unit.getId();
            List<NewBookCatalog> lessonList = unitId2LessonListMap.get(unitId);
            if (CollectionUtils.isEmpty(lessonList))
                continue;
            lessonList = lessonList.stream().sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
            for (NewBookCatalog lesson : lessonList) {
                Map<String, Object> lessonMap = new LinkedHashMap<>();
                addIntoMap(lessonMap, RES_LESSON_CNAME, lesson.getName());
                addIntoMap(lessonMap, RES_LESSON_ID, lesson.getId());
                addIntoMap(lessonMap, RES_RANK, lesson.getRank());
                lessonMapList.add(lessonMap);
            }
            addIntoMap(unitInfoMap, RES_UNIT_INFO_LIST, lessonMapList);
            unitInfoMapList.add(unitInfoMap);
        }

        addIntoMap(bookMap, RES_UNIT_LIST, unitInfoMapList);
        return MapMessage.successMessage().add(RES_USER_BOOK, bookMap);
    }


    /**
     * 返回指定bookid的课文朗读结构
     *
     * @return
     */
    @RequestMapping(value = "/textread/word/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage textReadWordDetail() {
        User user = currentParent();
        if (user == null)
            return noLoginResult;

        String wordId = getRequestString("word_id");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(wordId) || StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("error params");
        NewWordStock newWordStock = newWordStockLoaderClient.loadNewWordStockByWordIdLessonId(wordId, lessonId);
        if (newWordStock == null)
            return MapMessage.errorMessage("不存在的生词");

        Map<String, Object> wordDetailMap = new LinkedHashMap<>();
        wordDetailMap.put("gif_url", newWordStock.getImgUrl());

        List<NewWordStock.EmbedVoice> voices = newWordStock.getVoices();
        if (voices == null)
            voices = new ArrayList<>();
        List<Map<String, Object>> embedVoiceMapList = new ArrayList<>();
        NewWordStock.EmbedVoice contentPinyinMark = newWordStock.getContentPinyinMark();
        if (contentPinyinMark != null) {
            Map<String, Object> contentVoiceMap = new LinkedHashMap<>();
            contentVoiceMap.put("audio_url", contentPinyinMark.getUrl());
            contentVoiceMap.put("pinyin", contentPinyinMark.getSpelling());
            embedVoiceMapList.add(contentVoiceMap);
        }
        voices.forEach(t -> {
            if (contentPinyinMark != null && StringUtils.equals(t.getSpelling(), contentPinyinMark.getSpelling()))
                return;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("audio_url", t.getUrl());
            map.put("pinyin", t.getSpelling());
            embedVoiceMapList.add(map);
        });
        wordDetailMap.put("embed_voices", embedVoiceMapList);

        wordDetailMap.put("word", newWordStock.getContent());

        Map<String, Object> baseInfoMap = new LinkedHashMap<>();
        baseInfoMap.put("bushou", newWordStock.getRadical());
        baseInfoMap.put("bihua", newWordStock.getStrokesOrder());
        baseInfoMap.put("bihua_count", newWordStock.getStrokes());
        baseInfoMap.put("structure", newWordStock.getStructure());

        wordDetailMap.put("base_info", baseInfoMap);

        return MapMessage.successMessage().add("word_detail", wordDetailMap);
    }

    /**
     * 点读专用
     * 单元的点读内容
     *
     * @return
     */
    @RequestMapping(value = "/unit/piclisten/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage unitPicListen() {

        String unitId = getRequestString(REQ_UNIT_ID);

        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())) {
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        }
        List<NewBookCatalogAncestor> ancestors = unit.getAncestors();
        if (CollectionUtils.isEmpty(ancestors))
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);
        NewBookCatalogAncestor bookCatalogAncestor = ancestors.stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return MapMessage.errorMessage(RES_RESULT_UNIT_ERROR_MSG);

        List<PicListen> allPicListen = questionLoaderClient.loadPicListenByNewUnitId(unitId);

        Map<String, Object> unitInfoMap = new LinkedHashMap<>();

        addIntoMap(unitInfoMap, RES_UNIT_ID, unit.getId());
        addIntoMap(unitInfoMap, RES_UNIT_CNAME, unit.getName());
        addIntoMap(unitInfoMap, RES_TOTAL_INDEX, allPicListen.size());
        addIntoMap(unitInfoMap, ContentApiConstants.RES_BOOK_ID, bookCatalogAncestor.getId());

        List<Map<String, Object>> picListenMapList = new ArrayList<>();

        int index = 1;
        for (PicListen t : allPicListen) {
            Map<String, Object> picListenMap = new LinkedHashMap<>();
            addIntoMap(picListenMap, RES_PICLISTEN_ID, t.getId());
            addIntoMap(picListenMap, RES_PIC_URL, t.getImgUrl());
            addIntoMap(picListenMap, RES_INDEX, index);
            addIntoMap(picListenMap, RES_BLOCK_DATA, t.getContent());
            addIntoMap(picListenMap, "blocks_sentences", t.getBlocks());

            picListenMapList.add(picListenMap);
            index++;
        }

        addIntoMap(unitInfoMap, RES_PICLISTEN_LIST, picListenMapList);
        return MapMessage.successMessage().add(RES_UNIT_INFO, unitInfoMap);
    }


    /**
     * 朗读的课本内容吧
     *
     * @return
     */
    @RequestMapping(value = "/textread/lesson/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage textreadLessonDetail() {
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("lesson id error");

        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null || !BookCatalogType.LESSON.name().equals(lesson.getNodeType()))
            return MapMessage.errorMessage("课程错误");
        List<ChineseSentence> chineseSentenceList = getArticleSentenceList(lessonId);
        if (CollectionUtils.isEmpty(chineseSentenceList))
            return MapMessage.errorMessage("句子错误");
        Map<Integer, List<ChineseSentence>> paragraph2SentenceListMap = chineseSentenceList.stream().collect(Collectors.groupingBy(ChineseSentence::getParagraph));
        List<Integer> paragraphList = new ArrayList<>(paragraph2SentenceListMap.keySet()).stream().sorted((o1, o2) -> o1.compareTo(o2)).collect(Collectors.toList());

        List<Map<String, Object>> paragraphMapList = new ArrayList<>();
        for (Integer paragraph : paragraphList) {
            List<ChineseSentence> sentenceList = paragraph2SentenceListMap.get(paragraph);
            if (CollectionUtils.isEmpty(sentenceList))
                continue;
            Map<String, Object> paragraphMap = new LinkedHashMap<>();
            sentenceList = sentenceList.stream().sorted((o1, o2) -> o1.getRank().compareTo(o2.getRank())).collect(Collectors.toList());
            String combineParagraphText = combineText(sentenceList);
            String paragraphId = combineParagraphId(lessonId, paragraph);
            addIntoMap(paragraphMap, RES_PARAGRAPH, paragraph);
            addIntoMap(paragraphMap, RES_PARAGRAPH_ID, paragraphId);
            addIntoMap(paragraphMap, RES_PARAGRAPH_TEXT, combineParagraphText);
            paragraphMapList.add(paragraphMap);
        }
        Map<String, Object> lessonMap = new LinkedHashMap<>();
        addIntoMap(lessonMap, RES_LESSON_CNAME, lesson.getName());
        addIntoMap(lessonMap, RES_LESSON_ID, lesson.getId());
        addIntoMap(lessonMap, RES_UNIT_ID, getAncestorId(lesson, BookCatalogType.UNIT));
        addIntoMap(lessonMap, ContentApiConstants.RES_BOOK_ID, getAncestorId(lesson, BookCatalogType.BOOK));
        addIntoMap(lessonMap, RES_PARAGRAPH_LIST, paragraphMapList);
        return MapMessage.successMessage().add(RES_LESSON_INFO, lessonMap);

    }


    /**
     * 朗读的课本内容吧
     *
     * @return
     */
    @RequestMapping(value = "/textread/paragraph/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage textreadParagraphDetail() {
        String paragraphId = getRequestString("paragraph_id");
        if (StringUtils.isBlank(paragraphId))
            return MapMessage.errorMessage("段落错误");

        String[] splitParagraphId = paragraphId.split("_");
        if (splitParagraphId.length <= 1)
            return MapMessage.errorMessage("段落错误");
        Integer targetParagraph = SafeConverter.toInt(splitParagraphId[0]);
        if (targetParagraph == 0)
            return MapMessage.errorMessage("段落错误");
        String lessonId = getLessonIdFromParagraphId(paragraphId);

        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null || !BookCatalogType.LESSON.name().equals(lesson.getNodeType()))
            return MapMessage.errorMessage("课程错误");
        List<ChineseSentence> chineseSentenceList = getArticleSentenceList(lessonId);
        if (chineseSentenceList == null)
            return MapMessage.errorMessage("课程错误");
        NewBookCatalogAncestor bookCatalogAncestor = lesson.getAncestors().stream().filter(t -> t.getNodeType().equals(BookCatalogType.BOOK.name())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return MapMessage.errorMessage("课程错误");

        NewBookProfile bookProfile = newContentLoaderClient.loadBook(bookCatalogAncestor.getId());
        if (bookProfile == null)
            return MapMessage.errorMessage("课程错误");
        Map<Integer, List<ChineseSentence>> paragraph2SentenceListMap = chineseSentenceList.stream().collect(Collectors.groupingBy(ChineseSentence::getParagraph));

        List<ChineseSentence> sentenceList = paragraph2SentenceListMap.get(targetParagraph);
        if (CollectionUtils.isEmpty(sentenceList))
            return MapMessage.errorMessage("段落错误");
        Map<String, Object> paragraphMap = new LinkedHashMap<>();
        sentenceList = sentenceList.stream().sorted((o1, o2) -> o1.getRank().compareTo(o2.getRank())).collect(Collectors.toList());
        String combineParagraphText = combineText(sentenceList);
        addIntoMap(paragraphMap, RES_PARAGRAPH, targetParagraph);
        addIntoMap(paragraphMap, RES_PARAGRAPH_TEXT, combineParagraphText);
        addIntoMap(paragraphMap, RES_LESSON_CNAME, lesson.getName());
        addIntoMap(paragraphMap, RES_BOOK_NAME, bookProfile.getShortName());

        return MapMessage.successMessage().add("paragraph_info", paragraphMap);

    }

    //前提是id格式已验证是正确的啊
    private static String getLessonIdFromParagraphId(String paragraphId) {
        return paragraphId.substring(paragraphId.indexOf("_") + 1, paragraphId.length());
    }

    private String getAncestorId(NewBookCatalog lesson, BookCatalogType ancestorType) {
        if (lesson == null)
            return null;
        List<NewBookCatalogAncestor> ancestorList = lesson.getAncestors();
        if (CollectionUtils.isEmpty(ancestorList))
            return null;
        for (NewBookCatalogAncestor ancestor : ancestorList) {
            if (ancestorType.name().equals(ancestor.getNodeType()))
                return ancestor.getId();
        }
        return null;
    }

    private String combineParagraphId(String lessonId, Integer paragraph) {
        return paragraph + "_" + lessonId;
    }

    private String combineText(List<ChineseSentence> sentenceList) {
        StringBuilder sb = new StringBuilder();
        sentenceList.stream().forEach(t -> {
            sb.append(t.getContent());
        });
        return sb.toString();
    }


    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog newBookCatalog) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_NAME, bookProfile.getName());
        addIntoMap(map, RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, Term.of(bookProfile.getTermType()).name());
        String coverUrl = StringUtils.isBlank(bookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + bookProfile.getImgUrl();
        if (StringUtils.isNotBlank(coverUrl)) {
            addIntoMap(map, RES_BOOK_VIEW_CONTENT, "");
            addIntoMap(map, RES_BOOK_COLOR, "");
            addIntoMap(map, RES_BOOK_IMAGE, coverUrl);
        } else {
            if (newBookCatalog != null) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), newBookCatalog.getName());
                if (bookPress != null) {
                    addIntoMap(map, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                    addIntoMap(map, RES_BOOK_COLOR, bookPress.getColor());
                    addIntoMap(map, RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix, bookPress.getColor()));
                }
            }
        }
        return map;
    }


}
