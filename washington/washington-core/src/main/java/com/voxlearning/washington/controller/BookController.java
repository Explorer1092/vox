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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.content.api.entity.MathUnit;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/book")
@Slf4j
@NoArgsConstructor
@SuppressWarnings("MVCPathVariableInspection")
public class BookController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 获取用户课本列表
     */
    @RequestMapping(value = "mybooklist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMyBookList() {

        MapMessage mapMessage = new MapMessage();
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return MapMessage.errorMessage("请登录才能使用");

        List<Map<String, Object>> bookMaps = new ArrayList<>();
        switch (teacher.getSubject()) {
            case ENGLISH:
                List<Book> englishBooks = userBookLoaderClient.loadUserEnglishBooks(teacher);
                engPaintedSkin(englishBooks);
                for (Book book : englishBooks) {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getId());
                    bookMap.put("name", book.getCname());
                    bookMap.put("imgUrl", book.getImgUrl());
                    bookMap.put("createTime", book.getCreateTime());
                    bookMap.put("clazzLevel", book.getClassLevel());
                    bookMap.put("latestVersion", book.getLatestVersion());
                    BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                    if (bookPress != null) {
                        bookMap.put("viewContent", bookPress.getViewContent());
                        bookMap.put("color", bookPress.getColor());
                    }
                    bookMaps.add(bookMap);
                }
                break;
            case MATH:
                List<MathBook> mathBooks = userBookLoaderClient.loadUserMathBooks(teacher);
                mathPaintedSkin(mathBooks);
                for (MathBook mathBook : mathBooks) {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", mathBook.getId());
                    bookMap.put("name", mathBook.getCname());
                    bookMap.put("imgUrl", mathBook.getImgUrl());
                    bookMap.put("createTime", mathBook.getCreateDatetime());
                    bookMap.put("clazzLevel", mathBook.getClassLevel());
                    bookMap.put("latestVersion", mathBook.getVersions());
                    BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, mathBook.getPress());
                    if (bookPress != null) {
                        bookMap.put("viewContent", bookPress.getViewContent());
                        bookMap.put("color", bookPress.getColor());
                    }
                    bookMaps.add(bookMap);
                }
                break;
        }
        mapMessage.add("books", bookMaps);
        return mapMessage;
    }

    @RequestMapping(value = "selectbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage selectbook(@RequestBody Map<String, Object> command) {

        if (command.get("level") == null || command.get("subject") == null) {
            return MapMessage.errorMessage("参数有问题，请重试。");
        }

        int level = conversionService.convert(command.get("level"), Integer.class);
        Subject subject = Subject.of(conversionService.convert(command.get("subject"), String.class));
        ClazzLevel clazzLevel = ClazzLevel.of(level);
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage();
        }
        ExRegion region = userLoaderClient.loadUserRegion(user);
        if (region == null) {
            return MapMessage.errorMessage();
        }

        switch (subject) {
            case ENGLISH: {
                List<Book> books = englishContentLoaderClient.getExtension()
                        .loadBooksByRegionCodeAndClassLevelSortRegionCode(region.getCountyCode(),
                                clazzLevel,
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                engPaintedSkin(books);  // 画皮
                List<Map<String, Object>> bookMaps = new ArrayList<>();
                for (Book book : books) {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getId());
                    bookMap.put("name", book.getCname());
                    bookMap.put("imgUrl", book.getImgUrl());
                    bookMap.put("term", book.getTermType());
                    bookMap.put("createTime", book.getCreateTime());
                    bookMap.put("latestVersion", book.getLatestVersion());
                    BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                    if (bookPress != null) {
                        bookMap.put("viewContent", bookPress.getViewContent());
                        bookMap.put("color", bookPress.getColor());
                    }
                    bookMaps.add(bookMap);
                }
                MapMessage message = MapMessage.successMessage();
                message.add("total", bookMaps.size());
                message.add("rows", bookMaps);
                return message;
            }
            case MATH: {
                List<MathBook> mathBooks = mathContentLoaderClient.getExtension()
                        .loadMathBooksByClassLevelWithSortByRegionCode(clazzLevel,
                                region.getCountyCode(),
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                mathPaintedSkin(mathBooks);  // 画皮
                List<Map<String, Object>> bookMaps = new ArrayList<>();
                for (MathBook book : mathBooks) {
                    Map<String, Object> bookMap = new HashMap<>();
                    bookMap.put("id", book.getId());
                    bookMap.put("name", book.getCname());
                    bookMap.put("imgUrl", book.getImgUrl());
                    bookMap.put("term", book.getTerm());
                    bookMap.put("createTime", book.getCreateDatetime());
                    bookMap.put("latestVersion", book.getVersions());
                    BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, book.getPress());
                    if (bookPress != null) {
                        bookMap.put("viewContent", bookPress.getViewContent());
                        bookMap.put("color", bookPress.getColor());
                    }
                    bookMaps.add(bookMap);
                }
                MapMessage message = MapMessage.successMessage();
                message.add("total", bookMaps.size());
                message.add("rows", bookMaps);
                return message;
            }
            default: {
                return MapMessage.errorMessage("老师Subject不存在");
            }
        }
    }

    @RequestMapping(value = "exambooklist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage exambooklist(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 根据课本ID获取单元列表
     */
    @RequestMapping(value = "unitlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUnitList(@RequestBody Map<String, Object> command) {
        MapMessage mapMessage = new MapMessage();
        Long bookId = command.get("bookId") != null ? conversionService.convert(command.get("bookId"), Long.class) : 0L;
        String subjectStr = command.get("subject") != null ? conversionService.convert(command.get("subject"), String.class) : Subject.ENGLISH.name();
        Subject subject = Subject.valueOf(subjectStr);
        switch (subject) {
            case ENGLISH:
                Book book = englishContentLoaderClient.loadEnglishBook(bookId);
                if (book != null) {
                    mapMessage.add("bookId", bookId);
                    mapMessage.add("bookName", book.getCname());
                }
                List<Unit> units = englishContentLoaderClient.loadEnglishBookUnits(bookId);
                List<Map<String, Object>> unitMaps = new ArrayList<>();
                for (Unit unit : units) {
                    Map<String, Object> unitMap = new HashMap<>();
                    unitMap.put("id", unit.getId());
                    unitMap.put("name", unit.getCname());
                    unitMaps.add(unitMap);
                }
                mapMessage.add("units", unitMaps);
                break;
            case MATH:
                MathBook mathBook = mathContentLoaderClient.loadMathBook(bookId);
                if (mathBook != null) {
                    mapMessage.add("bookId", bookId);
                    mapMessage.add("bookName", mathBook.getCname());
                }
                List<MathUnit> mathUnits = mathContentLoaderClient.loadMathBookUnits(bookId);
                List<Map<String, Object>> mathUnitMaps = new ArrayList<>();
                for (MathUnit mathUnit : mathUnits) {
                    Map<String, Object> mathUnitMap = new HashMap<>();
                    mathUnitMap.put("id", mathUnit.getId());
                    mathUnitMap.put("name", mathUnit.getCname());
                    mathUnitMaps.add(mathUnitMap);
                }
                mapMessage.add("units", mathUnitMaps);
                break;
            default:
                break;
        }

        return mapMessage;
    }

}
