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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.BookStatus;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.api.mapper.ClazzEnglishBook;
import com.voxlearning.utopia.service.content.api.mapper.ClazzMathBook;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher book controller implementation.
 *
 * @author Xiaohai Zhang
 * @since 2013-07-22 11:20
 */
@Controller
@RequestMapping("/teacher/book")
public class TeacherBookController extends AbstractTeacherController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "sortbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sortbook(@RequestParam("code") String code, @RequestParam("level") String level) {
        String filterPress = getRequestString("filterPress");
        int regionCode = ConversionUtils.toInt(code);
        ClazzLevel clazzLevel = ClazzLevel.of(ConversionUtils.toInt(level));
        final TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        switch (subject) {
            case ENGLISH: {
                List<Book> books = englishContentLoaderClient.getExtension()
                        .loadBookByRegionCodeAndClassLevel(regionCode, clazzLevel,
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                books = books.stream().filter(source ->
                        source.getBookType() != null && teacher.getKtwelve() != null
                                && teacher.getKtwelve().getLevel() == source.getBookType()
                ).collect(Collectors.toList());

                engPaintedSkin(books); // 画皮
                MapMessage message = MapMessage.successMessage();
                message.add("total", books.size());
                message.add("rows", books);
                return message;
            }
            case MATH: {
                List<MathBook> mathBooks = mathContentLoaderClient.getExtension()
                        .loadMathBooksByClassLevelWithSortByUpdateTime(clazzLevel, regionCode,
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));

                if (StringUtils.isNotBlank(filterPress)) {
                    mathBooks = mathBooks.stream().filter(mb -> mb.getPress().equals(filterPress)).collect(Collectors.toList());
                }
                mathPaintedSkin(mathBooks); // 画皮
                MapMessage message = MapMessage.successMessage();
                message.add("total", mathBooks.size());
                message.add("rows", mathBooks);
                return message;
            }
            case CHINESE: {
                List<BookDat> chineseBooks = chineseContentLoaderClient.loadChineseBooks()
                        .enabled()
                        .online()
                        .clazzLevel(clazzLevel)
                        .toList()
                        .stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                        .collect(Collectors.toList());
                chinesePaintedSkin(chineseBooks); // 画皮
                MapMessage message = MapMessage.successMessage();
                message.add("total", chineseBooks.size());
                message.add("rows", chineseBooks);
                return message;
            }
            default: {
                return MapMessage.errorMessage();
            }
        }
    }

    @RequestMapping(value = "samebookbyclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sameBookByClazz(@RequestBody Map<String, Object> mapper) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null)
            return MapMessage.errorMessage("老师不存在");
        Set<Long> clazzIds = new LinkedHashSet<>(StringHelper.toLongList(ConversionUtils.toString(mapper.get("clazzIds"))));
        Map<Long, GroupMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        if (teacher.matchSubject(Subject.ENGLISH)) {
            Map<Long, Book> allBooks = new HashMap<>();
            List<Set<Long>> bookIdsList = new LinkedList<>();
            // load online books belong to each clazz
            for (Long clazzId : clazzIds) {
                GroupMapper group = groups.get(clazzId);
                if (group == null) {
                    logger.error("cannot find group for teacher {} and clazz {}", teacher.getId(), clazzId);
                    continue;
                }
                List<ClazzEnglishBook> list = clazzBookLoaderClient.loadGroupEnglishBooks(clazzId);
                List<Book> books = new LinkedList<>();
                for (ClazzEnglishBook clazzEnglishBook : list) {
                    books.add(clazzEnglishBook.getBook());
                }
                books = books.stream().filter(source ->
                        source != null && source.fetchBookStatus() == BookStatus.ONLINE
                ).collect(Collectors.toList());

                Set<Long> bookIds = new TreeSet<>();
                for (Book book : books) {
                    allBooks.put(book.getId(), book);
                    bookIds.add(book.getId());
                }
                bookIdsList.add(bookIds);
            }

            // find out same book ids
            Set<Long> sameBookIds = CollectionUtils.intersection(bookIdsList);

            // return books
            if (sameBookIds.isEmpty()) {
                return MapMessage.errorMessage();
            } else {
                List<Book> books = new LinkedList<>();
                for (Long bookId : sameBookIds) {
                    books.add(allBooks.get(bookId));
                }
                engPaintedSkin(books); // 画皮
                MapMessage message = MapMessage.successMessage();
                message.add("total", books.size());
                message.add("rows", books);
                return message;
            }
        } else if (teacher.matchSubject(Subject.MATH)) {
            Map<Long, MathBook> allBooks = new HashMap<>();
            List<Set<Long>> bookIdsList = new LinkedList<>();
            // load online books belong to each clazz
            for (Long clazzId : clazzIds) {
                GroupMapper group = groups.get(clazzId);
                if (group == null) {
                    logger.error("cannot find group for teacher {} and clazz {}", teacher.getId(), clazzId);
                    continue;
                }
                List<MathBook> books = ClazzMathBook.toBookList(clazzBookLoaderClient.loadGroupMathBooks(group.getId()));
                books = books.stream().filter(source ->
                        source != null && source.fetchBookStatus() == BookStatus.ONLINE
                ).collect(Collectors.toList());

                Set<Long> bookIds = new TreeSet<>();
                for (MathBook book : books) {
                    allBooks.put(book.getId(), book);
                    bookIds.add(book.getId());
                }
                bookIdsList.add(bookIds);
            }

            // find out same book ids
            Set<Long> sameBookIds = CollectionUtils.intersection(bookIdsList);

            // return books
            if (sameBookIds.isEmpty()) {
                return MapMessage.errorMessage();
            } else {
                List<MathBook> books = new LinkedList<>();
                for (Long bookId : sameBookIds) {
                    books.add(allBooks.get(bookId));
                }
                mathPaintedSkin(books); // 画皮
                MapMessage message = MapMessage.successMessage();
                message.add("total", books.size());
                message.add("rows", books);
                return message;
            }
        }
        return MapMessage.errorMessage("老师学科不正确");
    }

    /**
     * 根据作业（数学、英语）类型查找课本
     */
    @RequestMapping(value = "loadBooks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBooksByHomeworkType() {
        Long clazzId = getRequestLong("clazzId");
        String homeworkType = getRequest().getParameter("homeworkType");
        HomeworkType type = HomeworkType.valueOf(homeworkType);
        if (!hasClazzTeachingPermission(currentUserId(), clazzId)) {
            return MapMessage.errorMessage("此班级不属于您");
        }
        GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(currentUserId(), clazzId, false);
        if (group == null) {
            return MapMessage.errorMessage("找不到学生资源");
        }
        if (type == HomeworkType.ENGLISH || type == HomeworkType.VACATION_ENGLISH) {
            List<ClazzEnglishBook> list = clazzBookLoaderClient.loadGroupEnglishBooks(group.getId());
            List<Book> books = new LinkedList<>();
            for (ClazzEnglishBook clazzEnglishBook : list) {
                books.add(clazzEnglishBook.getBook());
            }
            engPaintedSkin(books);
            return MapMessage.successMessage().add("rows", books).add("total", books.size());
        } else if (type == HomeworkType.MATH || type == HomeworkType.VACATION_MATH) {
            List<MathBook> mathBooks = ClazzMathBook
                    .toBookList(clazzBookLoaderClient.loadGroupMathBooks(group.getId()));
            mathPaintedSkin(mathBooks);
            return MapMessage.successMessage().add("rows", mathBooks).add("total", mathBooks.size());
        } else {
            return MapMessage.errorMessage("不存在的作业类型" + homeworkType);
        }
    }

    @RequestMapping(value = "sentencecontext-{lessonId}.vpage", method = RequestMethod.GET)
    public String getSentenceContext(@PathVariable("lessonId") Long lessonId, Model model) {
        List<Sentence> sentences = englishContentLoaderClient.loadEnglishLessonSentences(lessonId);
        model.addAttribute("sentences", sentences);
        return "htmlchip/common/keypoint";
    }

    @RequestMapping(value = "unit.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage unit(@RequestParam("bookId") Long bookId) {
        MapMessage message = new MapMessage();
        Teacher teacher = currentTeacher();
        if (!Subject.ENGLISH.equals(teacher.getSubject())) {
            return MapMessage.errorMessage("非英语老师请重新登录").add("subject", teacher.getSubject());
        }
        Book book = englishContentLoaderClient.loadEnglishBook(bookId);
        List<Unit> units = englishContentLoaderClient.loadEnglishBookUnits(bookId);
        List<Map<String, Object>> unitsForWeb = new ArrayList<>();
        for (Unit unit : units) {
            Map<String, Object> m = JsonUtils.fromJson(JsonUtils.toJson(unit));
            m.put("lessons", englishContentLoaderClient.loadEnglishUnitLessons(unit.getId()));
            unitsForWeb.add(m);
        }
        Boolean examGame = false;
        message.setSuccess(true);
        message.add("row", examGame);
        message.add("total", unitsForWeb.size());
        message.add("rows", unitsForWeb);
        message.setInfo(book.getUgcAuthor());
        return message;

    }

    @RequestMapping(value = "upgradeclazzbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map upgradeClazzBook() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在");
        }
        if (teacher.getSubject() != Subject.ENGLISH) {
            return MapMessage.errorMessage("非英语老师，无法使用自动升级教材功能");
        }
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());

        for (Clazz clazz : clazzs) {
            teacherClazzServiceClient.upgradeClazzBook(clazz.getId(), teacher);
        }

        //跳过的将value设置成false，执行过的将value设置为true
        userAttributeServiceClient.setExtensionAttribute(currentUserId(), businessTeacherServiceClient.generateUpgradeBookKey(), "true");

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "donotshowupgradeclazzbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map donotShowUpgradeClazzBook() {
        //跳过的将value设置成false，执行过的将value设置为true
        userAttributeServiceClient.setExtensionAttribute(currentUserId(), businessTeacherServiceClient.generateUpgradeBookKey(), "false");
        return MapMessage.successMessage();
    }

    /**
     * 记录提示更换教材弹窗 一学期一次
     */
    @RequestMapping(value = "remindbook.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage saveRemindBookAttr(@RequestParam("clazzLevel") Integer clazzLevel) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || clazzLevel == null) {
            return MapMessage.errorMessage("老师或年级不存在");
        }
        return userAttributeServiceClient.setExtensionAttribute(teacher.getId(),
                UserExtensionAttributeKeyType.REMIND_TEACHER_CHANGE_BOOK.name() + "_" + clazzLevel,
                DateUtils.dateToString(new Date()));
    }
}
