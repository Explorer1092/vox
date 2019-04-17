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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.ChineseSentenceType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.NewWordStockLoaderClient;
import com.voxlearning.utopia.service.content.consumer.TextListenLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.ParentShareTextReadService;
import com.voxlearning.utopia.service.piclisten.client.AsyncPiclistenCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.ParentShareTextRead;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.mapper.SelfStudyAdInfo;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.CHINESE;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.RES_SUBJECTIVE_UPLOAD_FAIL_MSG;

/**
 * @author jiangpeng
 * @since 16/7/15.
 */
@Controller
@RequestMapping(value = "/v1/parent/selfstudy/textread/")
@Slf4j
public class ParentSelfStudyTextReadApiController extends AbstractSelfStudyApiController {

    private static int maxUploadCount = 10;
    private static String shareReadUrl = "{0}/view/mobile/parent/learning_tool/read_cn_wechat?voice_url={1}&student_name={2}&lesson_title={3}&duration={4}&lesson_id={5}&paragraph={6}&self_study_type=TEXTREAD_CHINESE";
    private static String lessonContentUrl = "{0}/view/mobile/parent/learning_tool/read_cn_text?clazz_level={1}&self_study_type={2}&book_id={3}&lesson_id={4}&title={5}";
    private static String paragraphContentUrl = "{0}/view/mobile/parent/learning_tool/read_cn_audio?paragraph_id={1}&title={2}";

    private static String shareReadUrlV2 = "{0}/view/mobile/parent/learning_tool/read_cn_audio_v2?voice_url={1}&student_name={2}&lesson_title={3}&duration={4}&lesson_id={5}&paragraph_id={6}&self_study_type=TEXTREAD_CHINESE";
    private static String lessonContentUrlV2 = "{0}/view/mobile/parent/learning_tool/read_cn_text_v2?clazz_level={1}&self_study_type={2}&book_id={3}&lesson_id={4}&title={5}";

    //新的朗读时候段落内容h5地址
    private static String readParagraphContentUrlV2 = "{0}/view/mobile/parent/learning_tool/read_cn_text_v2?clazz_level={1}&self_study_type={2}&book_id={3}&paragraph_id={4}&title={5}";

    //新版本, 我的录音列表里点击我的录音的页面地址
    private static String readAudioContentUrl = "{0}/view/mobile/parent/learning_tool/read_cn_audio_v2?paragraph_id={1}&self_study_type=TEXTREAD_CHINESE";


    @Inject
    private NewWordStockLoaderClient newWordStockLoader;

    @Inject
    private TextListenLoaderClient textListenLoaderClient;

    @ImportService(interfaceClass = ParentShareTextReadService.class) private ParentShareTextReadService parentShareTextReadService;

    /**
     * 课文朗读
     * 根据年级学科返回课本列表
     */
    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", CHINESE.name());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL);
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
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(subject, 0, level);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (CollectionUtils.isEmpty(newBookProfileList))
            return resultMap.add(RES_BOOK_LIST, new ArrayList<>());

        //过滤已经校对过的语文教材
        newBookProfileList = newBookProfileList.stream().filter(t -> textBookManagementLoaderClient.textReadBookShow(t.getId(), getRequestString(REQ_SYS))).collect(Collectors.toList());

        List<Map<String, Object>> bookMapList = new ArrayList<>();
        List<String> seriesIdList = newBookProfileList.stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        for (NewBookProfile bookProfile : newBookProfileList) {
            NewBookCatalog newBookCatalog = stringNewBookCatalogMap.get(bookProfile.getSeriesId());
            bookMapList.add(convert2BookMap(bookProfile, newBookCatalog, false, null));
        }
        resultMap.add(RES_BOOK_LIST, bookMapList);
        return resultMap;
    }


    /**
     * 课文朗读
     * 返回学生的默认教材详情
     */
    @RequestMapping(value = "/book/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", CHINESE.name());
            if (StringUtils.isNotBlank(getRequestString(REQ_BOOK_ID)))
                validateRequest(REQ_SUBJECT, REQ_STUDENT_ID, REQ_BOOK_ID);
            else
                validateRequest(REQ_SUBJECT, REQ_STUDENT_ID);
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

        Long parentId = getCurrentParentId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String sys = getRequestString(REQ_SYS);
        NewBookProfile newBookProfile = null;
        String requestBookId = getRequestString(REQ_BOOK_ID);
        if (StringUtils.isBlank(requestBookId)) {
            if (studentId != 0) {
                if (!checkStudentParentRef(studentId, parentId)) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }

                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if (studentDetail == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }
                if (studentDetail.isJuniorStudent()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_LEVEL_ERROR);
                    return resultMap;
                }
                newBookProfile = parentSelfStudyPublicHelper.loadDefaultSelfStudyBook(studentDetail, SelfStudyType.TEXTREAD_CHINESE, false, sys);
                if (newBookProfile == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
                    return resultMap;
                }
            } else { // 如果传的学生id为0 则取记在家长的默认教材,如果家长上没记教材
                String bookId = asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                        .CParentSelfStudyBookCacheManager_getParentSelfStudyBook(parentId, SelfStudyType.TEXTREAD_CHINESE)
                        .take();
                if (bookId != null) {
                    newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
                }
                if (newBookProfile == null)
                    newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(SelfStudyType.TEXTREAD_CHINESE.getDefaultBookId())).get(SelfStudyType.TEXTREAD_CHINESE.getDefaultBookId());
            }
        }else
            newBookProfile = newContentLoaderClient.loadBook(requestBookId);
        if (newBookProfile == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog, false, null);


        Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient.loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
        List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
        if (unitList == null) unitList = new ArrayList<>();
        List<String> unitIdList = unitList.stream().map(NewBookCatalog::getId).collect(Collectors.toList());
        Map<String, List<NewBookCatalog>> unitId2LessonListMap = newContentLoaderClient.loadChildren(unitIdList, BookCatalogType.LESSON);

        List<String> lessonIdList = unitId2LessonListMap.values().stream().flatMap(Collection::stream).map(NewBookCatalog::getId).collect(Collectors.toList());
        Map<String, Boolean> lessonHasWordMap = newWordStockLoader.checkIfContainNewWordStocks(lessonIdList);
        Map<String, Boolean> lessonHasTextMap = newChineseContentLoaderClient.loadLessonsHasSentences(lessonIdList, ChineseSentenceType.SENTENCE);
        Map<String, TextListen> stringTextListenMap = textListenLoaderClient.loadTextListensByBookCatalogIds(lessonIdList);
        List<Map<String, Object>> unitInfoMapList = new ArrayList<>();
        String lessonContentUrlTemplate;
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        if (VersionUtil.compareVersion(version, "1.8.8.0") >=0 ){ //188版本以上用新页面
            lessonContentUrlTemplate = lessonContentUrlV2;
        }else {
            lessonContentUrlTemplate = lessonContentUrl;
        }

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
                boolean hasWord = SafeConverter.toBoolean(lessonHasWordMap.get(lesson.getId()));
                boolean hasText = SafeConverter.toBoolean(lessonHasTextMap.get(lesson.getId()));
                if (!hasText && !hasWord)
                    continue;
                Map<String, Object> lessonMap = new LinkedHashMap<>();
                addIntoMap(lessonMap, RES_LESSON_CNAME, lesson.getName());
                addIntoMap(lessonMap, RES_LESSON_ID, lesson.getId());
                addIntoMap(lessonMap, RES_LESSON_H5,
                        MessageFormat.format(lessonContentUrlTemplate, fetchMainsiteUrlByCurrentSchema(), newBookProfile.getClazzLevel(),
                                SelfStudyType.TEXTREAD_CHINESE.name(), newBookProfile.getId(), lesson.getId(), lesson.getName()));
                addIntoMap(lessonMap, RES_RANK, lesson.getRank());
                addIntoMap(lessonMap, RES_HAS_NEW_WORD, hasWord);
                addIntoMap(lessonMap, RES_HAS_TEXT, hasText);
                TextListen textListen = stringTextListenMap.get(lesson.getId());//课文朗读师范音
                if (textListen != null)
                    addIntoMap(lessonMap, RES_AUDIO_URL, textListen.getListenUrl());
                lessonMapList.add(lessonMap);
            }
            addIntoMap(unitInfoMap, RES_UNIT_INFO_LIST, lessonMapList);
            unitInfoMapList.add(unitInfoMap);
        }

        List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(SelfStudyAdPosition.TEXT_READ_UNIT_LIST_BANNER, newBookProfile);
        List<Map<String, Object>> bannerMapList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(selfStudyAdInfoList)) {
            selfStudyAdInfoList.forEach(ad -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_USER_IMG_URL, ad.getImgUrl());
                if (StringUtils.isNotBlank(ad.getJumpUrl()))
                    map.put(RES_JUMP_URL, ad.getJumpUrl());
                bannerMapList.add(map);
            });
        }

        Boolean needPay = textBookManagementLoaderClient.textReadNeedPay(newBookProfile.getId());

        addIntoMap(bookMap, RES_UNIT_LIST, unitInfoMapList);
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_USER_BOOK, bookMap).add(RES_BANNERS, bannerMapList).add(RES_NEED_UPGRADE, needUpgrade(needPay, getRequestString(REQ_APP_NATIVE_VERSION)));
    }

    protected List<ChineseSentence> getArticleSentenceList(String lessonId){
        List<ChineseArticle> chineseArticles = newChineseContentLoaderClient.loadChineseArticlesByLessonId(lessonId);
        if (CollectionUtils.isEmpty(chineseArticles))
            return Collections.emptyList();
        List<ChineseSentence> chineseSentenceList = chineseArticles.get(0).getChineseSentences();
        if (CollectionUtils.isEmpty(chineseSentenceList))
            return Collections.emptyList();
        return chineseSentenceList;
    }


    /**
     * 课文朗读
     * 获取指定课文的分段信息
     * section
     */
    @RequestMapping(value = "/paragraph/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage paragraphList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_LESSON_ID, "lesson ID");
            validateRequiredNumber(REQ_SUB_TEXT_LENGTH, "length");
            validateRequest(REQ_LESSON_ID, REQ_SUB_TEXT_LENGTH);
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

        String lessonId = getRequestString(REQ_LESSON_ID);
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null || !BookCatalogType.LESSON.name().equals(lesson.getNodeType())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SECTION_ERROR);
            return resultMap;
        }
        NewBookCatalogAncestor bookCatalogAncestor = lesson.getAncestors().stream().filter(t -> t.getNodeType().equals(BookCatalogType.BOOK.name())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return failMessage(RES_RESULT_SECTION_ERROR);
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookCatalogAncestor.getId());

        List<ChineseSentence> chineseSentenceList = getArticleSentenceList(lessonId);
        Map<Integer, List<ChineseSentence>> paragraph2SentenceListMap = chineseSentenceList.stream().collect(Collectors.groupingBy(ChineseSentence::getParagraph));
        List<Integer> paragraphList = new ArrayList<>(paragraph2SentenceListMap.keySet()).stream().sorted(Integer::compareTo).collect(Collectors.toList());

        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        boolean newVersion = VersionUtil.compareVersion(version, "1.8.8.0") >= 0;

        List<Map<String, Object>> paragraphMapList = new ArrayList<>();
        Integer subLength = getRequestInt(REQ_SUB_TEXT_LENGTH);
        for (Integer paragraph : paragraphList) {
            List<ChineseSentence> sentenceList = paragraph2SentenceListMap.get(paragraph);
            if (CollectionUtils.isEmpty(sentenceList))
                continue;
            Map<String, Object> paragraphMap = new LinkedHashMap<>();
            sentenceList = sentenceList.stream().sorted((o1, o2) -> o1.getRank().compareTo(o2.getRank())).collect(Collectors.toList());
            String combineParagraphText = combineText(sentenceList);
            String subText = combineParagraphText.length() > subLength ? combineParagraphText.substring(0, subLength) + "..." : combineParagraphText;
            String paragraphId = combineParagraphId(lesson.getId(), paragraph);
            addIntoMap(paragraphMap, RES_PARAGRAPH, "第" + paragraph + "段");
            addIntoMap(paragraphMap, RES_PARAGRAPH_ID, paragraphId);
            addIntoMap(paragraphMap, RES_PARAGRAPH_TEXT, subText);
            if (!newVersion) {
                //老版本, 朗读的段落内容页 和 录音列表里的段落内容页是同一个页面
                String h5Url = MessageFormat.format(paragraphContentUrl, fetchMainsiteUrlByCurrentSchema(), paragraphId, urlEncode(lesson.getName()));
                addIntoMap(paragraphMap, RES_PARAGRAPH_H5, h5Url);
            }else {
                //新版本,朗读的段落内容页用一个新的字段,
                String h5Url = MessageFormat.format(readParagraphContentUrlV2, fetchMainsiteUrlByCurrentSchema(), newBookProfile.getClazzLevel(),
                        SelfStudyType.TEXTREAD_CHINESE.name(), newBookProfile.getId(), paragraphId, lesson.getName());
                addIntoMap(paragraphMap, RES_PARAGRAPH_H5_V2, h5Url);
                //同时,新版本的录音列表页用新的
                addIntoMap(paragraphMap, RES_PARAGRAPH_H5, MessageFormat.format(readAudioContentUrl, fetchMainsiteUrlByCurrentSchema(), paragraphId));
            }
            paragraphMapList.add(paragraphMap);
        }
        Map<String, Object> sectionMap = new LinkedHashMap<>();
        addIntoMap(sectionMap, RES_LESSON_CNAME, lesson.getName());
        addIntoMap(sectionMap, RES_LESSON_ID, lesson.getId());
        addIntoMap(sectionMap, RES_PARAGRAPH_LIST, paragraphMapList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_LESSON_INFO, sectionMap);
        return resultMap;
    }


    /**
     * 课文朗读
     * 课文的生词表
     * section
     */
    @RequestMapping(value = "/word/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage lessonWordList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_LESSON_ID, "lesson ID");
            validateRequest(REQ_LESSON_ID);
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
        String lessonId = getRequestString(REQ_LESSON_ID);
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null || !BookCatalogType.LESSON.name().equals(lesson.getNodeType())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_SECTION_ERROR);
            return resultMap;
        }
        List<NewWordStock> newWordStocks = newWordStockLoader.loadNewWordStockByLessonId(lessonId);
        if (CollectionUtils.isEmpty(newWordStocks)) {
            successMessage().add(RES_WORD_LIST, new ArrayList<>());
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        List<Map<String, Object>> wordMapList = new ArrayList<>();
        newWordStocks.forEach(newWordStock -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_ID, newWordStock.getId());
            map.put(RES_WORD, newWordStock.getContent());
            NewWordStock.EmbedVoice contentPinyinMark = newWordStock.getContentPinyinMark();
            if (contentPinyinMark == null) {
                List<NewWordStock.EmbedVoice> voices = newWordStock.getVoices();
                if (!CollectionUtils.isEmpty(voices)) {
                    contentPinyinMark = voices.get(0);
                }
            }
            if (contentPinyinMark != null) {
                map.put(RES_SPELL, contentPinyinMark.getSpelling());
                map.put(RES_AUDIO_URL, contentPinyinMark.getUrl());
            }
            map.put(RES_DETAIL_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/learning_tool/cn_dict?word_id=" + newWordStock.getId()+"&lesson_id="+lessonId);
            wordMapList.add(map);
        });

        return resultMap.add(RES_WORD_LIST, wordMapList);
    }

    /**
     * 更新学生的点读默认教材
     */
    @RequestMapping(value = "/book/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", CHINESE.name());
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_SUBJECT, REQ_STUDENT_ID, REQ_BOOK_ID);
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

        Long parentId = getCurrentParentId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String bookId = getRequestString(REQ_BOOK_ID);
        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        SelfStudyType selfStudyType = SelfStudyType.TEXTREAD_CHINESE;

        if (studentId != 0) {
            if (!checkStudentParentRef(studentId, parentId)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return resultMap;
            }
        } else {
            asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                    .CParentSelfStudyBookCacheManager_setParentSeflStudyBook(parentId, selfStudyType, bookId)
                    .awaitUninterruptibly();
            return successMessage();
        }

        Student student = studentLoaderClient.loadStudent(getRequestLong(REQ_STUDENT_ID));
        contentServiceClient.setUserSelfStudyDefaultBook(student, subject, selfStudyType.name(), bookId);
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

    }


    private String combineParagraphId(String lessonId, Integer paragraph) {
        return paragraph + "_" + lessonId;
    }

    private String combineText(List<ChineseSentence> sentenceList) {
        StringBuilder sb = new StringBuilder();
        sentenceList.stream().forEach(t -> sb.append(t.getContent()));
        return sb.toString();
    }


    //以下接口为分享音频相关

    /**
     * 上传前检查该音频是否上传过,是否超过次数限制
     * 首先检查这个文件md5是否这个家长上传过,如果上传过,直接返回对应url.
     * 如果没有上传过,则检查是否超过次数限制,即一个家长,当天,针对一段(段落id)上传次数是否超过10次.超过10次不让上传.没超过允许上传
     */
    @RequestMapping(value = "/share/check.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shareCheck() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PARAGRAPH_ID, "段落id");
            validateRequired(REQ_FILE_MD5, "md5");
            validateRequest(REQ_PARAGRAPH_ID, REQ_FILE_MD5);
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


        User parent = getCurrentParent();
        if (parent == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_ERROR_MSG);
            return resultMap;
        }

        String md5 = getRequestString(REQ_FILE_MD5);
        ParentShareTextRead parentShareTextRead = parentShareTextReadService.loadByParentIdFileMd5(parent.getId(), md5);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (parentShareTextRead != null) {
            addIntoMap(resultMap, RES_NEED_UPLOAD, false);
            addIntoMap(resultMap, RES_SHARE_INFO, generateShareMap(parentShareTextRead));
        } else {
            String paragraphId = getRequestString(REQ_PARAGRAPH_ID);
            Boolean isOverLimit = checkIsOverLimit(parent.getId(), paragraphId);
            addIntoMap(resultMap, RES_NEED_UPLOAD, !isOverLimit);
            addIntoMap(resultMap, RES_IS_OVER_LIMIT, isOverLimit);
            if (isOverLimit)
                addIntoMap(resultMap, RES_MESSAGE, RES_RESULT_UPLOAD_OVER_LIMIT);
        }
        return resultMap;

    }

    /**
     * 上传前检查该音频是否上传过,是否超过次数限制
     * 首先检查这个文件md5是否这个家长上传过,如果上传过,直接返回对应url.
     * 如果没有上传过,则检查是否超过次数限制,即一个家长,当天,针对一段(段落id)上传次数是否超过10次.超过10次不让上传.没超过允许上传
     * 然后就处理上传的文件.....,并记录分享记录,当天上传次数加一
     */
    @RequestMapping(value = "/share/upload.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage shareUpload() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PARAGRAPH_ID, "段落id");
            validateRequired(REQ_FILE_MD5, "md5");
            validateRequiredNumber(REQ_STUDENT_ID, "学生id");
            validateRequiredNumber(REQ_VOICE_DURATION, "时长");
            validateRequest(REQ_PARAGRAPH_ID, REQ_FILE_MD5, REQ_STUDENT_ID, REQ_VOICE_DURATION);
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


        User parent = getCurrentParent();
        if (parent == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_ERROR_MSG);
            return resultMap;
        }

        String md5 = getRequestString(REQ_FILE_MD5);
        ParentShareTextRead parentShareTextRead = parentShareTextReadService.loadByParentIdFileMd5(parent.getId(), md5);

        if (parentShareTextRead != null) {
            addIntoMap(resultMap, RES_SHARE_INFO, generateShareMap(parentShareTextRead));
            return resultMap;
        } else {
            String paragraphId = getRequestString(REQ_PARAGRAPH_ID);
            //检查是否超过限制
            Boolean isOverLimit = checkIsOverLimit(parent.getId(), paragraphId);
            if (isOverLimit) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_IS_OVER_LIMIT, true);
                return resultMap;
            }
            //检查家长孩子关系
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            Boolean isStudentParent = checkStudentParentRef(studentId, parent.getId());
            if (!isStudentParent) {
                addIntoMap(resultMap, RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                addIntoMap(resultMap, RES_MESSAGE, RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
                return resultMap;
            }
            //开始上传
            Integer duration = getRequestInt(REQ_VOICE_DURATION);
            parentShareTextRead = ParentShareTextRead.newInstance(parent.getId(), studentId, paragraphId, md5, duration);
            try {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile file = multipartRequest.getFile(REQ_FILE);
                MapMessage result = atomicLockManager.wrapAtomic(this).keyPrefix("textRead").keys(parent.getId())
                        .proxy().uploadAndSaveShareInfo(parentShareTextRead, file, parentShareTextReadService, asyncPiclistenCacheServiceClient);
                if (result.isSuccess()) {
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                    addIntoMap(resultMap, RES_SHARE_INFO, generateShareMap(parentShareTextRead));
                    return resultMap;
                } else {
                    addIntoMap(resultMap, RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    addIntoMap(resultMap, RES_MESSAGE, result.getInfo());
                    return resultMap;
                }
            } catch (DuplicatedOperationException ex) {
                logger.warn("Upload voice writing (DUPLICATED OPERATION): (parentId={})", parent.getId(), ex.getMessage());
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
                return resultMap;
            } catch (Exception ex) {
                logger.warn("Upload voice failed writing: (parentId={})", parent.getId(), ex.getMessage());
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_SUBJECTIVE_UPLOAD_FAIL_MSG);
                return resultMap;
            }
        }
    }


    private Boolean checkIsOverLimit(Long parentId, String paragraphId) {

        Long alreadyUploadCount = asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                .ParentShareTextReadLimitCacheManager_get(parentId, paragraphId)
                .take();
        return alreadyUploadCount >= maxUploadCount;
    }

    private Map<String, Object> generateShareMap(ParentShareTextRead parentShareTextRead) {
        if (parentShareTextRead == null)
            return null;
        Map<String, Object> map = new LinkedHashMap<>();
        String studentName = "孩子的";
        Long studentId = parentShareTextRead.getStudentId();
        if (studentId != null) {
            Student student = studentLoaderClient.loadStudent(studentId);
            if (student != null)
                studentName = student.fetchRealname();
        }

        map.put(RES_SHARE_TITLE, "我分享了" + studentName + "的语文课本朗读录音");
        map.put(RES_SHARE_CONTENT, "家长通，小学生成长教育的爸妈帮手");
        Integer duration = parentShareTextRead.getDuration();
        int minutes = duration / 60;
        int seconds = duration % 60;
        String durationFormat;
        if (minutes == 0)
            durationFormat = seconds + "秒";
        else
            durationFormat = minutes + " 分" + seconds + "秒";
        String lessonId = getLessonIdFromParagraphId(parentShareTextRead.getParagraphId());
        Integer paragraph = getParagraphFromParagraphId(parentShareTextRead.getParagraphId());
        String voiceUrlEncode = urlEncode(parentShareTextRead.getVoiceUrl());
        String studentNameEncode = urlEncode(studentName);
        String lessonTitleEncode = urlEncode(getLessonTitleForShare(parentShareTextRead.getParagraphId()));
        String durationEncode = urlEncode(durationFormat);
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        String url;
        if (VersionUtil.compareVersion(version, "1.8.8.0") >= 0)
            url = MessageFormat.format(shareReadUrlV2, fetchMainsiteUrlByCurrentSchema(), voiceUrlEncode, studentNameEncode, lessonTitleEncode, durationEncode, lessonId, parentShareTextRead.getParagraphId());
        else
            url = MessageFormat.format(shareReadUrl, fetchMainsiteUrlByCurrentSchema(), voiceUrlEncode, studentNameEncode, lessonTitleEncode, durationEncode, lessonId, paragraph);
        map.put(RES_SHARE_URL, url);
        return map;
    }

    private String getLessonTitleForShare(String paragraphId) {

        String lessonId = getLessonIdFromParagraphId(paragraphId);
        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(lessonId);
        if (lesson == null)
            return "";
        List<NewBookCatalogAncestor> ancestors = lesson.getAncestors();
        if (CollectionUtils.isEmpty(ancestors))
            return "";
        String bookId = ancestors.stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(new NewBookCatalogAncestor()).getId();
        if (StringUtils.isBlank(bookId))
            return "";
        NewBookProfile book = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
        if (book == null)
            return "";
        String bookName = book.getName();
        String term = Term.of(book.getTermType()).name();
        String lessonName = lesson.getName();

        return bookName + " " + term + " " + lessonName;
    }

    //前提是id格式已验证是正确的啊
    private static String getLessonIdFromParagraphId(String paragraphId) {
        String[] splitParagraphId = paragraphId.split("_");
        if (splitParagraphId.length <= 1)
            return "";
        Integer targetParagraph = SafeConverter.toInt(splitParagraphId[0]);
        if (targetParagraph == 0)
            return "";
        return paragraphId.substring(paragraphId.indexOf("_") + 1, paragraphId.length());
    }

    //前提是id格式已验证是正确的啊
    private static Integer getParagraphFromParagraphId(String paragraphId) {
        String[] splitParagraphId = paragraphId.split("_");
        if (splitParagraphId.length <= 1)
            return null;
        Integer targetParagraph = SafeConverter.toInt(splitParagraphId[0]);
        if (targetParagraph == 0)
            return null;
        return targetParagraph;
    }


    private String urlEncode(String content) {
        try {
            return URLEncoder.encode(content, "utf-8");
        } catch (Exception e) {
            return "";
        }
    }

    //上传文件,成功后记录家长的上传记录,同时当天的段落上传次数+1;
    private MapMessage uploadAndSaveShareInfo(ParentShareTextRead parentShareTextRead, MultipartFile file,
                                              ParentShareTextReadService parentShareTextReadService, AsyncPiclistenCacheServiceClient asyncPiclistenCacheServiceClient) {
        String fileUrl;
        if (file != null) {
            fileUrl = doUploadResult(parentShareTextRead.getParentId(), file, "textread");
            if (fileUrl == null)
                return MapMessage.errorMessage("上传失败,请稍后重试");
            //上传成功
            parentShareTextRead.setVoiceUrl(fileUrl);
            //计数需要加一
            asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                    .ParentShareTextReadLimitCacheManager_incr(parentShareTextRead.getParentId(), parentShareTextRead.getParagraphId(), 1L)
                    .awaitUninterruptibly();
            ParentShareTextRead saveResult = parentShareTextReadService.save(parentShareTextRead);
            if (saveResult != null)
                return MapMessage.successMessage();
            else
                return MapMessage.errorMessage("上传失败,请稍后重试");

        } else
            return MapMessage.errorMessage("上传失败,请稍后重试");
    }

    public String doUploadResult(Long userId, MultipartFile file, String activityName) {
        if (StringUtils.isBlank(activityName)) return null;
        return OSSManageUtils.upload(file, activityName, null);
    }

}
