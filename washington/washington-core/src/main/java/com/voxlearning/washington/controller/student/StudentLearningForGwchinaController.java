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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.BookDat;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.voxlearning.alps.annotation.meta.Subject.*;

/**
 * 绿网OEM专用的Controller CLASS
 * Created by Alex on 14-11-24.
 */
@Controller
@RequestMapping("/student/learning")
public class StudentLearningForGwchinaController extends AbstractController {

    // 绿网专用
    @RequestMapping(value = "gwchina.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // FIXME 需要改写，这么写不好
        StudentDetail student = currentStudentDetail();
        List<Map<String, Object>> books = businessStudentServiceClient.getStudentSelfStudyDefaultBooks(student);
        // 如果传入某个学科的课本，替换
        Long bookId = getRequestLong("bookId");
        String subjectText = getRequestString("subject");
        if (bookId != 0 && StringUtils.isNotBlank("subjectText")) {
            Subject subject = UNKNOWN;
            try {
                subject = Subject.valueOf(subjectText);
            } catch (Exception ignored) {
            }
            switch (subject) {
                case ENGLISH: {
                    contentServiceClient.setUserDefaultBook(student, bookId, subject);
                    Book book = englishContentLoaderClient.loadEnglishBook(bookId);
                    if (book != null) {
                        Map<String, Object> englishBookMap = books.get(0);
                        englishBookMap.put("bookId", book.getId());
                        englishBookMap.put("bookName", book.getCname());
                        englishBookMap.put("bookSubject", ENGLISH);
                        englishBookMap.put("bookImg", StringUtils.contains(book.getImgUrl(), "catalog_new") ? book.getImgUrl() : StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
                        englishBookMap.put("latestVersion", book.getLatestVersion());
                        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                        if (bookPress != null) {
                            englishBookMap.put("viewContent", bookPress.getViewContent());
                            englishBookMap.put("color", bookPress.getColor());
                        }
                    }
                    break;
                }
                case MATH: {
                    contentServiceClient.setUserDefaultBook(student, bookId, subject);
                    MathBook mathBook = mathContentLoaderClient.loadMathBook(bookId);
                    if (mathBook != null) {
                        Map<String, Object> mathBookMap = books.get(1);
                        mathBookMap.put("bookId", mathBook.getId());
                        mathBookMap.put("bookName", mathBook.getCname());
                        mathBookMap.put("bookSubject", MATH);
                        mathBookMap.put("bookImg", StringUtils.contains(mathBook.getImgUrl(), "catalog_new") ? mathBook.getImgUrl() : StringUtils.replace(mathBook.getImgUrl(), "catalog", "catalog_new"));
                        mathBookMap.put("latestVersion", "1".equals(mathBook.getVersions()));
                        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, mathBook.getPress());
                        if (bookPress != null) {
                            mathBookMap.put("viewContent", bookPress.getViewContent());
                            mathBookMap.put("color", bookPress.getColor());
                        }
                    }
                    break;
                }
                case CHINESE: {
                    contentServiceClient.setUserDefaultBook(student, bookId, subject);
                    BookDat chineseBook = chineseContentLoaderClient.loadChineseBook(bookId);
                    if (chineseBook != null) {
                        Map<String, Object> chineseBookMap = books.get(2);
                        chineseBookMap.put("bookId", chineseBook.getId());
                        chineseBookMap.put("bookName", chineseBook.getCname());
                        chineseBookMap.put("bookSubject", CHINESE);
                        chineseBookMap.put("bookImg", StringUtils.contains(chineseBook.getImgUrl(), "catalog_new") ? chineseBook.getImgUrl() : StringUtils.replace(chineseBook.getImgUrl(), "catalog", "catalog_new"));
                        chineseBookMap.put("latestVersion", chineseBook.getLatestVersion());
                        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.CHINESE, chineseBook.getPress());
                        if (bookPress != null) {
                            chineseBookMap.put("viewContent", bookPress.getViewContent());
                            chineseBookMap.put("color", bookPress.getColor());
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            model.addAttribute("subject", subjectText);
            model.addAttribute("currentBookId", bookId);
        }
        model.addAttribute("books", books);
        model.addAttribute("history", getRequestString("history"));
        return "studentv3/learning/gwchina";
    }

}
