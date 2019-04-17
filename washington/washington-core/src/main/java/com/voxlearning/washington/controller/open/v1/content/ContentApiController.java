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

package com.voxlearning.washington.controller.open.v1.content;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_ID;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_OPEN_EXAM;

/**
 * 英语教材相关的第三方API接口Class.
 * TODO 内容接口API目前数学和语文都只支持小学部分，初高中暂时还没有
 * <p>
 * Created by Alex on 14-10-13.
 */
@Controller
@RequestMapping(value = "/v1/content")
public class ContentApiController extends AbstractApiController {

    @Inject private ContentLoaderWrapperFactory contentLoaderWrapperFactory;

    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;

    @RequestMapping(value = "/press/{subject}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getPressList(@PathVariable("subject") String subject) {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_TYPE, "学段");
            validateEnum(REQ_BOOK_TYPE, "学段", "1", "2", "3");
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Integer bookType = getRequestInt(REQ_BOOK_TYPE);
        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"subject", "bookType"},
                new Object[]{subject, bookType});
        List<String> pressList = washingtonCacheSystem.CBS.flushable.load(cacheKey);
        if (pressList == null) {
            AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
            if (loaderWrapper != null) {
                pressList = loaderWrapper.loadPressByBookType(bookType);
            }

            if (pressList == null) {
                pressList = Collections.emptyList();
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), pressList);
        }

        resultMap.add(RES_PRESS_LIST, pressList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/{press}/book/{subject}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookListByPress(@PathVariable("press") String press, @PathVariable("subject") String subject) {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"press", "subject"},
                new Object[]{press, subject});

        List<Map<String, Object>> bookList = washingtonCacheSystem.CBS.flushable.load(cacheKey);

        if (bookList == null) {
            AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
            if (loaderWrapper != null) {
                bookList = loaderWrapper.loadBooksByPress(press, subject);
            }

            if (bookList == null) {
                bookList = Collections.emptyList();
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), bookList);
        }

        resultMap.add(RES_BOOK_LIST, bookList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/book/{subject}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookListByClazzLevel(@PathVariable("subject") String subject) {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"subject", "clazzLevel"},
                new Object[]{subject, clazzLevel});

        List<Map<String, Object>> bookList = washingtonCacheSystem.CBS.flushable.load(cacheKey);
        if (bookList == null) {
            AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
            if (loaderWrapper != null) {
                bookList = loaderWrapper.loadBooksByClazzLevel(subject, clazzLevel);
            }

            if (bookList == null) {
                bookList = Collections.emptyList();
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), bookList);
        }

        resultMap.add(RES_BOOK_LIST, bookList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/booklist.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookListByClazzLevelForTeacherApp() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateEnum(REQ_SUBJECT, "学科", Subject.ENGLISH.name().toLowerCase(), Subject.MATH.name().toLowerCase(), Subject.CHINESE.name().toLowerCase());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        VendorApps app = getApiRequestApp();
        String appKey = app.getAppKey();
        String sys = getRequestString(REQ_SYS);
        //公共api,GreatAdventure 的返回原来的内容
        if ("GreatAdventure".equals(appKey)) {
            String subject = getRequestString(REQ_SUBJECT);
            Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
            String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                    new String[]{"subject", "clazzLevel"},
                    new Object[]{subject, clazzLevel});

            List<Map<String, Object>> bookList = washingtonCacheSystem.CBS.flushable.load(cacheKey);
            if (bookList == null) {
                AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
                if (loaderWrapper != null) {
                    bookList = loaderWrapper.loadBooksByClazzLevel(subject, clazzLevel);
                }

                if (bookList == null) {
                    bookList = Collections.emptyList();
                }

                washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), bookList);
            }

            List<Long> AFENTI_BOOK_BLACK_LIST = Collections.unmodifiableList(
                    Arrays.asList(100222L, 100225L, 100231L, 100232L, 100366L, 100367L, 100368L, 100369L, 100370L, 100371L,
                            100372L, 100373L, 100219L, 100227L, 100291L, 100292L, 100294L, 100295L, 100299L, 100302L,
                            100505L, 100516L, 100534L, 541L, 554L, 567L, 890L, 891L, 892L, 907L, 918L, 919L, 931L, 958L,
                            959L, 709L, 753L, 772L, 784L, 797L, 932L, 933L, 934L)
            );
            bookList = bookList.stream().filter(b -> !AFENTI_BOOK_BLACK_LIST.contains(SafeConverter.toLong(b.get("book_id"))))
                    .collect(Collectors.toList());

            resultMap.add(RES_BOOK_LIST, bookList);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT).toUpperCase());
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(subject, 0, level);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (CollectionUtils.isEmpty(newBookProfileList))
            return resultMap.add(RES_BOOK_LIST, new ArrayList<>());

        // 2016-10-18 因为版权问题,随声听教材也需要支持过滤 #33595
        if ("17Student".equals(appKey) || "17Parent".equals(appKey)) {
            newBookProfileList = newBookProfileList.stream().filter(t ->
               textBookManagementLoaderClient.walkManBookShow(t.getId(), sys)
            ).collect(Collectors.toList());
        }
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        List<String> seriesIdList = newBookProfileList.stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        for (NewBookProfile bookProfile : newBookProfileList) {
            NewBookCatalog newBookCatalog = stringNewBookCatalogMap.get(bookProfile.getSeriesId());
            bookMapList.add(convertNewBook2BookMap(bookProfile, newBookCatalog));
        }
        resultMap.add(RES_BOOK_LIST, bookMapList);
        return resultMap;

    }

    private Map<String, Object> convertNewBook2BookMap(NewBookProfile bookProfile, NewBookCatalog newBookCatalog) {

        Map<String, Object> bookInfo = new LinkedHashMap<>();
        addIntoMap(bookInfo, RES_BOOK_ID, bookProfile.getId());
        addIntoMap(bookInfo, RES_BOOK_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name().toLowerCase());
        addIntoMap(bookInfo, RES_BOOK_TYPE, 1);//不对
        addIntoMap(bookInfo, RES_BOOK_CLASS_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(bookInfo, RES_BOOK_START_CLASS_LEVEL, bookProfile.getStartClazzLevel());
        addIntoMap(bookInfo, RES_BOOK_TERM, bookProfile.getTermType());
        addIntoMap(bookInfo, RES_BOOK_PRESS, bookProfile.getPublisher());
        addIntoMap(bookInfo, RES_CNAME, bookProfile.getName());
        addIntoMap(bookInfo, RES_ENAME, bookProfile.getAlias());
        addIntoMap(bookInfo, RES_BOOK_LATEST_VERSION, bookProfile.getLatestVersion());
        addIntoMap(bookInfo, RES_BOOK_OPEN_EXAM, "");
        addIntoMap(bookInfo, RES_BOOK_COVER_URL, StringUtils.isBlank(bookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + bookProfile.getImgUrl());

        if (newBookCatalog != null) {
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), newBookCatalog.getName());
            if (bookPress != null) {
                addIntoMap(bookInfo, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                addIntoMap(bookInfo, RES_BOOK_COLOR, bookPress.getColor());
            }
        }
        return bookInfo;
    }

    @RequestMapping(value = "/booklist_old.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookListByClazzLevelForTeacherAppOld() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateEnum(REQ_SUBJECT, "学科", Subject.ENGLISH.name().toLowerCase(), Subject.MATH.name().toLowerCase(), Subject.CHINESE.name().toLowerCase());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String subject = getRequestString(REQ_SUBJECT);
        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"subject", "clazzLevel"},
                new Object[]{subject, clazzLevel});

        List<Map<String, Object>> bookList = washingtonCacheSystem.CBS.flushable.load(cacheKey);
        if (bookList == null) {
            AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
            if (loaderWrapper != null) {
                bookList = loaderWrapper.loadBooksByClazzLevel(subject, clazzLevel);
            }

            if (bookList == null) {
                bookList = Collections.emptyList();
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), bookList);
        }

        resultMap.add(RES_BOOK_LIST, bookList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    @RequestMapping(value = "/book/{subject}/{bookId}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookInfo(@PathVariable("subject") String subject, @PathVariable("bookId") Long bookId) {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"subject", "bookId"},
                new Object[]{subject, bookId});

        Map<String, Object> bookInfo = new HashMap<>();

        AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
        if (loaderWrapper != null) {
            bookInfo = loaderWrapper.loadBookInfo(subject, bookId);
        }

        if (bookInfo == null) {
            bookInfo = Collections.emptyMap();
        }

        resultMap.add(RES_BOOK, bookInfo);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/bookinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getBookInfoForTeacherApp() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateEnum(REQ_SUBJECT, "学科", Subject.ENGLISH.name().toLowerCase(), Subject.MATH.name().toLowerCase(), Subject.CHINESE.name().toLowerCase());
            validateRequiredNumber(REQ_BOOK_ID, "教材ID");
            validateRequest(REQ_SUBJECT, REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String subject = getRequestString(REQ_SUBJECT);
        Long bookId = getRequestLong(REQ_BOOK_ID);

        Map<String, Object> bookInfo = new HashMap<>();

        AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
        if (loaderWrapper != null) {
            bookInfo = loaderWrapper.loadBookInfo(subject, bookId);
        }

        if (bookInfo == null) {
            bookInfo = Collections.emptyMap();
        }

        resultMap.add(RES_BOOK, bookInfo);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/book/{subject}/{bookId}/{unitId}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getUnitInfo(@PathVariable("subject") String subject, @PathVariable("bookId") Long bookId, @PathVariable("unitId") Long unitId) {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"subject", "bookId", "unitId"},
                new Object[]{subject, bookId, unitId});

        Map<String, Object> unitInfo = washingtonCacheSystem.CBS.flushable.load(cacheKey);

        if (unitInfo == null) {
            AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
            if (loaderWrapper != null) {
                unitInfo = loaderWrapper.loadUnitInfo(bookId);
            }
            if (unitInfo == null) {
                unitInfo = Collections.emptyMap();
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), unitInfo);
        }
        resultMap.add(RES_UNIT, unitInfo);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    // =====================================================================================================
    // 给拓词接力棒做的专用接口
    @RequestMapping(value = "/wordinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getWordInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_WORD_TEXT);
            validateRequestNoSessionKey(REQ_WORD_TEXT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String wordText = getRequestString(REQ_WORD_TEXT);

        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
                new String[]{"wordText"}, new Object[]{wordText});

        List<Map<String, Object>> wordInfo = washingtonCacheSystem.CBS.flushable.load(cacheKey);

        if (wordInfo == null) {
            wordInfo = new ArrayList<>();
            List<WordStock> wordStocks = wordStockLoaderClient.loadWordStocksByEntext(wordText);
            if (wordStocks != null && wordStocks.size() > 0) {
                for (WordStock wordStock : wordStocks) {
                    if (wordStock.getKtwelve() != Ktwelve.PRIMARY_SCHOOL) {
                        continue;
                    }

                    Map<String, Object> wordItem = new LinkedHashMap<>();
                    addIntoMap(wordItem, RES_SENTENCE_CNTEXT, wordStock.getCnText());
                    addIntoMap(wordItem, RES_SENTENCE_ENTEXT, wordStock.getEnText());
                    addIntoMap(wordItem, RES_WORDS_PIC_URI, wordStock.getPictureUrl());
                    addIntoMap(wordItem, RES_WORDS_SPEACH_PART, wordStock.getSpeechPart());
                    addIntoMap(wordItem, RES_WORDS_PARAPHRASE1, wordStock.getParaphrase1());
                    addIntoMap(wordItem, RES_WORDS_PARAPHRASE2, wordStock.getParaphrase2());
                    addIntoMap(wordItem, RES_WORDS_PARAPHRASE3, wordStock.getParaphrase3());
                    addIntoMap(wordItem, RES_WORDS_PARAPHRASE4, wordStock.getParaphrase4());
                    addIntoMap(wordItem, RES_WORDS_PRONOUNCE, wordStock.getAudioUS());
                    wordInfo.add(wordItem);
                }
            }

            washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), wordInfo);
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_CDN_URL, getCdnBaseUrlStaticSharedWithSep());
        resultMap.add(RES_WORD, wordInfo);

        return resultMap;
    }

    protected void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value == null) {
            dataMap.put(key, "");
        } else {
            dataMap.put(key, value);
        }
    }
//
//    @RequestMapping(value = "/book/{subject}/{bookId}/{unitId}/{lessonId}.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage getLessonInfo(@PathVariable("subject") String subject, @PathVariable("bookId") Long bookId,
//                                    @PathVariable("unitId") Long unitId, @PathVariable("lessonId") Long lessonId) {
//        MapMessage resultMap = new MapMessage();
//        try {
//            validateRequest();
//        } catch (IllegalArgumentException e) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, e.getMessage());
//            return resultMap;
//        }
//
//        String cacheKey = CacheKeyGenerator.generateCacheKey(ContentApiController.class,
//                new String[]{"subject", "bookId", "unitId", "lessonId"},
//                new Object[]{subject, bookId, unitId, lessonId});
//
//        Map<String, Object> lessonInfo = loadFromCache(cacheKey);
//
//        if (lessonInfo == null) {
//            if (ENGLISH.name().toLowerCase().equals(subject)) {
//                Lesson lesson = contentLoaderClient.loadEnglishLesson(lessonId);
//                if (lesson != null) {
//                    lessonInfo = convertLessonInfo(lesson);
//                    lessonInfo.put(RES_SENTENCE_LIST, loadEnglishBookSentences(bookId, lessonId));
//                }
//
//            } else if (MATH.name().toLowerCase().equals(subject)) {
//                // TODO
//            } else if (CHINESE.name().toLowerCase().equals(subject)) {
//                // TODO
//            } else {
//                lessonInfo = Collections.emptyMap();
//            }
//
//            addIntoCache(cacheKey, todayExpiration(), lessonInfo);
//        }
//        resultMap.add(RES_CDN_URL, getCdnBaseUrlStaticSharedWithSep());
//        resultMap.add(RES_LESSON, lessonInfo);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
//    }
//
//
//    private List<Map<String, Object>> loadEnglishBookUnits(Long bookId) {
//        List<Unit> unitList = contentLoaderClient.loadEnglishBookUnits(bookId);
//        if (unitList == null || unitList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retUnitList = new ArrayList<>();
//        for (Unit unitItem : unitList) {
//            retUnitList.add(convertUnitInfo(unitItem));
//        }
//
//        return retUnitList;
//    }
//
//    private Map<String, Object> convertUnitInfo(Unit unitItem) {
//        if (unitItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> unitInfo = new LinkedHashMap<>();
//        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
//        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
//        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
//        addIntoMap(unitInfo, RES_ENAME, unitItem.getEname());
//        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());
//
//        return unitInfo;
//    }
//
//    private List<Map<String, Object>> loadMathBookUnits(Long bookId) {
//        List<MathUnit> unitList = contentLoaderClient.loadMathBookUnits(bookId);
//        if (unitList == null || unitList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retUnitList = new ArrayList<>();
//        for (MathUnit unitItem : unitList) {
//            retUnitList.add(convertMathUnitInfo(unitItem));
//        }
//
//        return retUnitList;
//    }
//
//    private Map<String, Object> convertMathUnitInfo(MathUnit unitItem) {
//        if (unitItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> unitInfo = new LinkedHashMap<>();
//        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
//        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
//        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
//        addIntoMap(unitInfo, RES_ENAME, null);
//        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());
//
//        return unitInfo;
//    }
//
//    private List<Map<String, Object>> loadChineseBookUnits(Long bookId) {
//        List<UnitDat> unitList = contentDataLoaderClient.loadBookUnits(bookId);
//        if (unitList == null || unitList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retUnitList = new ArrayList<>();
//        for (UnitDat unitItem : unitList) {
//            retUnitList.add(convertChineseUnitInfo(unitItem));
//        }
//
//        return retUnitList;
//    }
//
//    private Map<String, Object> convertChineseUnitInfo(UnitDat unitItem) {
//        if (unitItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> unitInfo = new LinkedHashMap<>();
//        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
//        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
//        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
//        addIntoMap(unitInfo, RES_ENAME, null);
//        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());
//
//        return unitInfo;
//    }
//
//
//    private List<Map<String, Object>> loadEnglishBookLessons(Long unitId) {
//        List<Lesson> lessonList = contentLoaderClient.loadEnglishUnitLessons(unitId);
//        if (lessonList == null || lessonList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retLessonList = new ArrayList<>();
//        for (Lesson lessonItem : lessonList) {
//            retLessonList.add(convertLessonInfo(lessonItem));
//        }
//
//        return retLessonList;
//    }
//
//    private Map<String, Object> convertLessonInfo(Lesson lessonItem) {
//        if (lessonItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> lessonInfo = new LinkedHashMap<>();
//        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
//        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
//        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
//        addIntoMap(lessonInfo, RES_ENAME, lessonItem.getEname());
//        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRank());
//        addIntoMap(lessonInfo, RES_ROLE_COUNT, lessonItem.getRoleCount());
//        addIntoMap(lessonInfo, RES_HAS_DIALOG, lessonItem.hasDialog());
//
//        return lessonInfo;
//    }
//
//    private List<Map<String, Object>> loadMathBookLessons(Long unitId) {
//        List<MathLesson> lessonList = contentLoaderClient.loadMathUnitLessons(unitId);
//        if (lessonList == null || lessonList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retLessonList = new ArrayList<>();
//        for (MathLesson lessonItem : lessonList) {
//            retLessonList.add(convertMathLessonInfo(lessonItem));
//        }
//
//        return retLessonList;
//    }
//
//    private Map<String, Object> convertMathLessonInfo(MathLesson lessonItem) {
//        if (lessonItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> lessonInfo = new LinkedHashMap<>();
//        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
//        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
//        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
//        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRank());
//
//        return lessonInfo;
//    }
//
//    private List<Map<String, Object>> loadChineseBookLessons(Long unitId) {
//        List<LessonDat> lessonList = contentDataLoaderClient.getContentDataLoader().loadUnitLessons(unitId);
//        if (lessonList == null || lessonList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retLessonList = new ArrayList<>();
//        for (LessonDat lessonItem : lessonList) {
//            retLessonList.add(convertChineseLessonInfo(lessonItem));
//        }
//
//        return retLessonList;
//    }
//
//    private Map<String, Object> convertChineseLessonInfo(LessonDat lessonItem) {
//        if (lessonItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> lessonInfo = new LinkedHashMap<>();
//        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
//        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
//        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
//        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRank());
//        addIntoMap(lessonInfo, RES_SHOW_NAME, lessonItem.getShowName());
//
//        return lessonInfo;
//    }
//
//
//    private List<Map<String, Object>> loadEnglishBookSentences(Long bookId, Long lessonId) {
//        Book book = contentLoaderClient.loadEnglishBook(bookId);
//        if (book == null) {
//            return Collections.emptyList();
//        }
//
//        List<Sentence> sentenceList = contentLoaderClient.loadEnglishLessonSentences(lessonId);
//        if (sentenceList == null || sentenceList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retSentenceList = new ArrayList<>();
//        for (Sentence sentenceItem : sentenceList) {
//            retSentenceList.add(convertSentenceInfo(book, sentenceItem));
//        }
//
//        return retSentenceList;
//    }
//
//    private Map<String, Object> convertSentenceInfo(Book book, Sentence sentenceItem) {
//        if (book == null || sentenceItem == null) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> sentenceInfo = new LinkedHashMap<>();
//        addIntoMap(sentenceInfo, RES_LESSON_ID, sentenceItem.getLessonId());
//        addIntoMap(sentenceInfo, RES_SENTENCE_ID, sentenceItem.getId());
//        addIntoMap(sentenceInfo, RES_SENTENCE_TYPE, sentenceItem.getType());
//        addIntoMap(sentenceInfo, RES_SENTENCE_CNTEXT, sentenceItem.getCnText());
//        addIntoMap(sentenceInfo, RES_SENTENCE_ENTEXT, sentenceItem.getEnText());
//        addIntoMap(sentenceInfo, RES_SENTENCE_DIALOG_ROLE, sentenceItem.getDialogRole());
//        addIntoMap(sentenceInfo, RES_SENTENCE_WAVE_URI, sentenceItem.getWaveUri());
//        addIntoMap(sentenceInfo, RES_SENTENCE_WAVE_PLAY_TIME, sentenceItem.getWavePlayTime());
//        addIntoMap(sentenceInfo, RES_SENTENCE_VOICE_TEXT, sentenceItem.getVoiceText());
//
//        WordStock wordStockInfo = getEnglishWordStock(sentenceItem);
//        if (wordStockInfo != null) {
//            if (wordStockInfo.getPicName() != null) {
//                addIntoMap(sentenceInfo, RES_WORDS_PIC_URI, "wordstock/wordimg/web/" + wordStockInfo.getPicName());
//            } else {
//                addIntoMap(sentenceInfo, RES_WORDS_PIC_URI, "");
//            }
//            addIntoMap(sentenceInfo, RES_WORDS_SPEACH_PART, wordStockInfo.getSpeechPart());
//            addIntoMap(sentenceInfo, RES_WORDS_PARAPHRASE1, wordStockInfo.getParaphrase1());
//            addIntoMap(sentenceInfo, RES_WORDS_PARAPHRASE2, wordStockInfo.getParaphrase2());
//            addIntoMap(sentenceInfo, RES_WORDS_PARAPHRASE3, wordStockInfo.getParaphrase3());
//            addIntoMap(sentenceInfo, RES_WORDS_PARAPHRASE4, wordStockInfo.getParaphrase4());
//
//            if ("英音".equals(book.getPronunciation())) {
//                addIntoMap(sentenceInfo, RES_WORDS_PRONOUNCE, wordStockInfo.getPronounceUK());
//                addIntoMap(sentenceInfo, RES_WORDS_PHONETIC_MAP, wordStockInfo.getPhoneticMapUK());
//            } else {
//                addIntoMap(sentenceInfo, RES_WORDS_PRONOUNCE, wordStockInfo.getPronounceUS());
//                addIntoMap(sentenceInfo, RES_WORDS_PHONETIC_MAP, wordStockInfo.getPhoneticMapUS());
//            }
//        }
//
//        return sentenceInfo;
//    }
//
//    private WordStock getEnglishWordStock(Sentence sentenceItem) {
//        Integer sentenceMultiMeaning = sentenceItem.getMultiMeaning();
//        List<WordStock> wordStockList = wordStockLoaderClient.loadWordStocksByEntext(sentenceItem.getEnText());
//        WordStock wordStockItem = null;
//        for (WordStock wordStock : wordStockList) {
//            if (Ktwelve.PRIMARY_SCHOOL.equals(wordStock.getKtwelve())) { // 目前只处理小学的
//                if (sentenceMultiMeaning != null && sentenceMultiMeaning > 0) {
//                    if (sentenceMultiMeaning.equals(wordStock.getMultiMeaning())) {
//                        wordStockItem = wordStock;
//                        break;
//                    }
//                } else {
//                    if (sentenceItem.getEnText().equals(wordStock.getEnText())) {
//                        wordStockItem = wordStock;
//                        break;
//                    }
//                }
//            }
//        }
//
//        return wordStockItem;
//    }
//
//    private void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
//        if (value == null) {
//            dataMap.put(key, "");
//        } else {
//            dataMap.put(key, value);
//        }
//    }
}
