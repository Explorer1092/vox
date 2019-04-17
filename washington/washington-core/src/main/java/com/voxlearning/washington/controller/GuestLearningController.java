/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller;


import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.constant.BookType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.api.mapper.*;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 提供常州青果在线引用
 *
 * @author Maofeng Lu
 * @since 15-2-28 下午2:11
 */
@Controller
@RequestMapping("/guest/learning")
public class GuestLearningController extends AbstractController {

    private final static String GUEST_ENGLISH_BOOK_COOKIE = "guest_english_book_cookie";
    private final static String GUEST_MATH_BOOK_COOKIE = "guest_math_book_cookie";
    private final static String GUEST_CHINESE_BOOK_COOKIE = "guest_chinese_book_cookie";

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String subjectText = getRequestString("subject");
        Subject subject = Subject.ofWithUnknown(subjectText);
        model.addAttribute("subject", subjectText);
        //课本列表
        List<Map<String, Object>> books = new LinkedList<>();
        long bookId = getRequestLong("bookId");
        if (bookId != 0) {
            model.addAttribute("currentBookId", bookId);
        }
        Book englishBook = null;
        if (bookId != 0 && subject == Subject.ENGLISH) {
            englishBook = englishContentLoaderClient.loadEnglishBook(bookId);
        }
        if (englishBook != null) {
            //存储到cookie
            getCookieManager().setCookie(GUEST_ENGLISH_BOOK_COOKIE, ConversionUtils.toString(bookId), 60 * 24 * 60 * 60);
        } else {
            String englishBookId = getCookieManager().getCookie(GUEST_ENGLISH_BOOK_COOKIE, "");
            if (StringUtils.isNotBlank(englishBookId)) {
                englishBook = englishContentLoaderClient.loadEnglishBook(ConversionUtils.toLong(englishBookId));
                //课本已经无效或不存在
                if (englishBook == null) {
                    List<Book> bookList = englishContentLoaderClient.loadEnglishBooks()
                            .enabled()
                            .filter(t -> t.getBookType() == BookType.PRIMARY.getKey())
                            .toList();
                    if (bookList != null && bookList.size() > 0) {
                        englishBook = bookList.get(bookList.size() - 1);
                    }
                }
            } else {
                List<Book> bookList = englishContentLoaderClient.loadEnglishBooks()
                        .enabled()
                        .filter(t -> t.getBookType() == BookType.PRIMARY.getKey())
                        .toList();
                if (bookList != null && bookList.size() > 0) {
                    englishBook = bookList.get(bookList.size() - 1);
                }
            }
        }
        if (englishBook != null) {
            Map<String, Object> englishBookMap = getBookMap(Subject.ENGLISH, englishBook.getId(), englishBook.getCname(), englishBook.getImgUrl(), englishBook.getLatestVersion(), englishBook.getPress());
            books.add(englishBookMap);
        }

        //数学课本
        MathBook mathBook = null;
        if (bookId != 0 && subject == Subject.MATH) {
            mathBook = mathContentLoaderClient.loadMathBook(bookId);
        }
        if (mathBook != null) {
            getCookieManager().setCookie(GUEST_MATH_BOOK_COOKIE, ConversionUtils.toString(bookId), 60 * 24 * 60 * 60);
        } else {
            String mathBookId = getCookieManager().getCookie(GUEST_MATH_BOOK_COOKIE, "");
            if (StringUtils.isNotBlank(mathBookId)) {
                mathBook = mathContentLoaderClient.loadMathBook(ConversionUtils.toLong(mathBookId));
                if (mathBook == null) {
                    mathBook = mathContentLoaderClient.loadMathBooks().enabled().findFirst();
                }
            } else {
                mathBook = mathContentLoaderClient.loadMathBooks().enabled().findFirst();
            }
        }
        if (mathBook != null) {
            Map<String, Object> mathMap = getBookMap(Subject.MATH, mathBook.getId(), mathBook.getCname(), mathBook.getImgUrl(), "1".equals(mathBook.getVersions()), mathBook.getPress());
            books.add(mathMap);
        }
        //语文课本
        BookDat chineseBook = null;
        if (bookId != 0 && subject == Subject.CHINESE) {
            chineseBook = chineseContentLoaderClient.loadChineseBook(bookId);
        }
        if (chineseBook != null) {
            getCookieManager().setCookie(GUEST_CHINESE_BOOK_COOKIE, ConversionUtils.toString(bookId), 60 * 24 * 60 * 60);
        } else {
            String chineseBookId = getCookieManager().getCookie(GUEST_CHINESE_BOOK_COOKIE, "");
            if (StringUtils.isNotBlank(chineseBookId)) {
                chineseBook = chineseContentLoaderClient.loadChineseBook(ConversionUtils.toLong(chineseBookId));
                if (chineseBook == null) {
                    chineseBook = chineseContentLoaderClient.loadChineseBooks()
                            .enabled()
                            .clazzLevel_termType_ASC()
                            .findFirst();
                }
            } else {
                chineseBook = chineseContentLoaderClient.loadChineseBooks()
                        .enabled()
                        .clazzLevel_termType_ASC()
                        .findFirst();
            }
        }
        if (chineseBook != null) {
            Map<String, Object> chineseBookMap = getBookMap(Subject.CHINESE, chineseBook.getId(), chineseBook.getCname(), chineseBook.getImgUrl(), chineseBook.getLatestVersion(), chineseBook.getPress());
            books.add(chineseBookMap);
        }

        model.addAttribute("books", books);
        return "project/learning/index";
    }

    private Map<String, Object> getBookMap(Subject subject, Long bookId, String bookName, String bookImgUrl, Boolean latestVersion, String press) {
        Map<String, Object> bookMap = new LinkedHashMap<>();
        bookMap.put("bookId", bookId);
        bookMap.put("bookName", bookName);
        bookMap.put("bookSubject", subject);
        bookMap.put("bookImg", StringUtils.contains(bookImgUrl, "catalog_new") ? bookImgUrl : StringUtils.replace(bookImgUrl, "catalog", "catalog_new"));
        bookMap.put("latestVersion", latestVersion);
        BookPress bookPress = BookPress.getBySubjectAndPress(subject, press);
        if (bookPress != null) {
            bookMap.put("viewContent", bookPress.getViewContent());
            bookMap.put("color", bookPress.getColor());
        }
        return bookMap;
    }


    // 换教材
    @RequestMapping(value = "books.vpage", method = RequestMethod.GET)
    public String clickChangeBook(Model model) {
        Subject subject = Subject.of(getRequest().getParameter("subjectType"));
        model.addAttribute("subjectType", subject);
        return "project/learning/books";
    }

    /**
     * 换教材
     * -- 教材数据
     */
    @RequestMapping(value = "bookschip.vpage", method = RequestMethod.GET)
    public String changeBook(Model model) {
        // 如果没有参数level，则使用1年级
        Integer level = getRequestInt("level");
        if (level == 0) {
            level = 1;
        }
        ClazzLevel clazzLevel = ClazzLevel.parse(level);
        Subject subject = Subject.of(getRequest().getParameter("subjectType"));
        final Term term = SchoolYear.newInstance().currentTerm();
        model.addAttribute("subjectType", subject);
        model.addAttribute("level", level);
        switch (subject) {
            case ENGLISH:
                List<Book> books = new LinkedList<>(englishContentLoaderClient.getExtension().loadEnglishBooks(clazzLevel));
                Collections.sort(books, new Comparator<Book>() {
                    @Override
                    public int compare(Book o1, Book o2) {
                        int u1 = o1.fetchtTermType() == term || o1.fetchtTermType() == Term.全年 ? 0 : 1;
                        int u2 = o2.fetchtTermType() == term || o2.fetchtTermType() == Term.全年 ? 0 : 1;
                        return Integer.compare(u1, u2);
                    }
                });
                engPaintedSkin(books); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(books) ? new LinkedList<>() : books);
                break;
            case MATH:
                List<MathBook> mathBooks = mathContentLoaderClient.getExtension().loadMathBooksByClassLevelOrderByTerm(clazzLevel, term);
                mathPaintedSkin(mathBooks); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(mathBooks) ? new LinkedList<>() : mathBooks);
                break;
            case CHINESE:
                List<BookDat> chineseBooks = chineseContentLoaderClient.loadChineseBooks()
                        .enabled()
                        .online()
                        .clazzLevel(clazzLevel)
                        .sorted((o1, o2) -> {
                            int t1 = o1.getTermType() == term.getKey() || o1.getTermType() == Term.全年.getKey() ? 0 : 1;
                            int t2 = o2.getTermType() == term.getKey() || o2.getTermType() == Term.全年.getKey() ? 0 : 1;
                            return Integer.compare(t1, t2);
                        })
                        .toList();
                chinesePaintedSkin(chineseBooks); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(chineseBooks) ? new LinkedList<>() : chineseBooks);
                break;
            default:
                logger.warn("subjectType param value {} unknown in studentSelfStudy method", subject.name());
        }
        return "project/learning/bookschip";
    }

    // 根据bookId查询units
    @RequestMapping(value = "book/units.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningUnit() {
        Long bookId = getRequestLong("bookId");
        String subjectStr = getRequest().getParameter("subjectType");
        Subject subject = Subject.of(subjectStr);

        switch (subject) {
            case ENGLISH: {
                return MapMessage.successMessage().add("units", englishContentLoaderClient.loadEnglishBookUnits(bookId));
            }
            case MATH: {
                return MapMessage.successMessage().add("units", mathContentLoaderClient.loadMathBookUnits(bookId));
            }
            case CHINESE: {
                return MapMessage.successMessage().add("units", chineseContentLoaderClient.loadChineseBookUnits(bookId));
            }
            default:
                return MapMessage.errorMessage("学科不存在");
        }
    }

    // 英语 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "book/lessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");
        Unit unit = englishContentLoaderClient.loadEnglishUnit(unitId);
        List<ExLesson> lessons = englishContentLoaderClient.loadUnitExLessons(unitId);
        //过滤掉需要登录的应用
        if (CollectionUtils.isNotEmpty(lessons)) {
            for (ExLesson exLesson : lessons) {
                List<ExPracticeType> exPracticeTypeList = exLesson.getPracticeTypes();
                exPracticeTypeList = exPracticeTypeList.stream().filter(source -> !source.isLoginRequired())
                        .collect(Collectors.toList());

                exLesson.getPracticeTypes().addAll(exPracticeTypeList);
            }
        }
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }

    // 数学 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "book/mathlessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningMathLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");

        MathUnit unit = mathContentLoaderClient.loadMathUnit(unitId);
        List<DisplayMathLessonMapper> lessons = mathContentLoaderClient.getExtension().loadMathLessonPracticeTypeInfoByUnitId(unitId);
        // 将专用知识点的应用分拆
        List<DisplayMathLessonMapper> specLessons = new ArrayList<>();
        for (int i = lessons.size() - 1; i >= 0; i--) {
            DisplayMathLessonMapper mathLessonMapper = lessons.get(i);
            DisplayMathLessonMapper newMathLessonMapper = null;

            List<DisplayMathPointMapper> mathPointMapperList = mathLessonMapper.getMathPointMapperList();
            for (int j = mathPointMapperList.size() - 1; j >= 0; j--) {
                DisplayMathPointMapper mathPointMapper = mathPointMapperList.get(j);

                //专项知识点应用练习中，屏蔽掉类别为”知识点应用_老师端“的应用 或者 练习中需要用户登录的
                List<DisplayPracticeTypeMapper> practiceTypeMapperList = mathPointMapper.getPracticeTypeMapperList();
                practiceTypeMapperList = practiceTypeMapperList.stream().filter(source ->
                        !StringUtils.equals(source.getCategoryName(), "知识点应用_老师端") || !source.isLoginRequired()
                ).collect(Collectors.toList());

                mathPointMapper.setPracticeTypeMapperList(practiceTypeMapperList);

                if (mathPointMapper.isSpecialMathPoint()) {
                    if (newMathLessonMapper == null) { // create a new instance
                        newMathLessonMapper = mathLessonMapper.copy();
                    }

                    mathPointMapper.setDataTypeCountList(mathContentLoaderClient.getExtension().loadPointBaseTypeAndCount(mathPointMapper.getId()));
                    newMathLessonMapper.getMathPointMapperList().add(mathPointMapper);
                    mathLessonMapper.getMathPointMapperList().remove(j);
                }
            }
            if (newMathLessonMapper != null) {
                specLessons.add(newMathLessonMapper);
            }
            if (mathLessonMapper.getMathPointMapperList().size() == 0) {
                lessons.remove(i);
            }
        }
        lessons.addAll(specLessons);
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }

    // 语文 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "book/chineselessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningchineseLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");

        UnitDat unit = chineseContentLoaderClient.loadChineseUnit(unitId);
        List<ExLessonDat> lessons = chineseContentLoaderClient.loadChineseUnitExLessons(unitId);
        // 过滤掉需要用户登录的练习
        if (CollectionUtils.isNotEmpty(lessons)) {
            for (ExLessonDat dat : lessons) {
                List<ExPracticeType> exPracticeTypeList = dat.getPracticeTypes();
                exPracticeTypeList = exPracticeTypeList.stream().filter(source -> !source.isLoginRequired())
                        .collect(Collectors.toList());
                dat.setPracticeTypes(exPracticeTypeList);
            }
        }
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }
}
