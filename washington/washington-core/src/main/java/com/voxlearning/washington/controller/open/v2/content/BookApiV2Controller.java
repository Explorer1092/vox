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

package com.voxlearning.washington.controller.open.v2.content;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.*;
import static com.voxlearning.utopia.service.content.api.constant.BookCatalogType.UNIT;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_ID;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_OPEN_EXAM;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * 向第三方提供教材相关信息的公共api
 * <p>
 * <p>
 * FIXME：请在方法的注释中标注目前调用这个接口的第三方应用
 * FIXME：请在方法的注释中标注目前调用这个接口的第三方应用
 * FIXME：请在方法的注释中标注目前调用这个接口的第三方应用
 *
 * @author Ruib
 * @since 2016/9/18
 */
@Controller
@RequestMapping(value = "/v2/content/book")
public class BookApiV2Controller extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 获取各学科默认教材，目前只有数学语文英语
     * <p>
     * 目前调用的第三方：GreatAdventure
     *
     * @return 返回各个学科的默认教材
     */
    @RequestMapping(value = "/defaultbooks.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchDefaultBooks() {
        MapMessage mesg = new MapMessage();

        List<Subject> subjects = Arrays.asList(ENGLISH, Subject.MATH, Subject.CHINESE);

        try {
            if (StringUtils.isNotEmpty(getRequestString(REQ_STUDENT_ID))) {
                validateRequiredNumber(REQ_STUDENT_ID, "学生ID");
                validateRequest(REQ_STUDENT_ID);
            } else {
                validateRequest();
            }
        } catch (IllegalArgumentException ex) {
            return noUserResult;
        }

        User user = getApiRequestUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return noUserResult;

        StudentDetail student;
        if (user.isParent()) {
            Long studentId = getRequestLong(REQ_STUDENT_ID, Long.MIN_VALUE);
            if (studentId == Long.MIN_VALUE) {
                mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mesg.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return mesg;
            }
            if (!checkStudentParentRef(studentId, user.getId())) {
                mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mesg.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return mesg;
            }
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : studentLoaderClient.loadStudentDetail(user.getId());
        }

        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);

        List<Map<String, Object>> books = new ArrayList<>();
        for (Subject subject : subjects) {
            GroupMapper group = groups.stream().filter(g -> g.getSubject() == subject).findFirst().orElse(null);
            NewBookProfile book;
            switch (subject) {
                case ENGLISH: {
                    book = clazzBookLoaderClient.loadDefaultEnglishBook(student, group,
                            new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                    break;
                }
                case MATH: {
                    book = clazzBookLoaderClient.loadDefaultMathBook(student, group);
                    break;
                }
                case CHINESE: {
                    book = clazzBookLoaderClient.loadDefaultChineseBook(student, group);
                    break;
                }
                default:
                    book = null;
            }
            if (book == null) continue;

            Map<String, Object> bookInfo = new LinkedHashMap<>();
            bookInfo.put(RES_BOOK_SUBJECT, subject);
            bookInfo.put(RES_BOOK_ID, book.getId());
            bookInfo.put(RES_CNAME, book.getName());
            bookInfo.put(RES_BOOK_CLASS_LEVEL, book.getClazzLevel());
            bookInfo.put(RES_BOOK_LATEST_VERSION, book.getLatestVersion() == 1);
            NewBookCatalog catalog = newContentLoaderClient.loadBookCatalogByCatalogId(book.getSeriesId());
            if (catalog == null) continue;
            BookPress press = BookPress.getBySubjectAndPress(subject, catalog.getName());
            if (press != null) {
                bookInfo.put(RES_BOOK_COLOR, press.getColor());
                bookInfo.put(RES_BOOK_VIEW_CONTENT, press.getViewContent());
            }
            books.add(bookInfo);
        }

        mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
        mesg.add(RES_USER_BOOK, books);
        return mesg;
    }

    /**
     * 根据年级和学科获取可用教材列表
     * <p>
     * 目前调用的第三方：GreatAdventure
     *
     * @return 返回指定学科指定年级的可用教材列表
     */
    @RequestMapping(value = "/gradesubjectbooks.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchBookListByGradeAndSubject() {
        MapMessage mesg = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "学科");
            validateEnum(REQ_SUBJECT, "学科", ENGLISH.name().toLowerCase(), MATH.name().toLowerCase(), CHINESE.name().toLowerCase());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL);
        } catch (IllegalArgumentException e) {
            return noUserResult;
        }

        List<String> AFENTI_BOOK_BLACK_LIST = Collections.unmodifiableList(
                Arrays.asList("BK_10300000556152", "BK_10300000557518", "BK_10300000558795", "BK_10300000559044",
                        "BK_10300000560861", "BK_10300000561978", "BK_10300000562568", "BK_10300000563511",
                        "BK_10300000564121", "BK_10300000565787", "BK_10300000566950", "BK_10300000567735",
                        "BK_10300000583490", "BK_10300000584160", "BK_10300000585752", "BK_10300000586129",
                        "BK_10300000587874", "BK_10300000588031", "BK_10300000589334", "BK_10300000590599",
                        "BK_10300001526860", "BK_10300001547859", "BK_10300001674057"
                )
        );

        Subject subject = Subject.ofWithUnknown(getRequestString(REQ_SUBJECT).toUpperCase());
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        List<NewBookProfile> books = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(subject, 0, level)
                .stream().filter(b -> !AFENTI_BOOK_BLACK_LIST.contains(b.getId())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(books)) {
            mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
            mesg.add(RES_BOOK_LIST, new ArrayList<>());
            return mesg;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        List<String> seriesIds = books.stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> id_series_map = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIds);
        for (NewBookProfile book : books) {
            NewBookCatalog series = id_series_map.get(book.getSeriesId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_BOOK_ID, book.getId());
            map.put(RES_BOOK_SUBJECT, subject);
            map.put(RES_BOOK_TYPE, book.getSubjectId() / 100); // subjectId的第一位表示小学初中高中
            map.put(RES_BOOK_CLASS_LEVEL, book.getClazzLevel());
            map.put(RES_BOOK_START_CLASS_LEVEL, book.getStartClazzLevel());
            map.put(RES_BOOK_TERM, book.getTermType());
            map.put(RES_BOOK_PRESS, book.getPublisher());
            map.put(RES_CNAME, book.getName());
            map.put(RES_ENAME, book.getAlias());
            map.put(RES_BOOK_LATEST_VERSION, book.getLatestVersion());
            map.put(RES_BOOK_OPEN_EXAM, "");
            if (series != null) {
                BookPress bookPress = BookPress.getBySubjectAndPress(subject, series.getName());
                if (bookPress != null) {
                    map.put(RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                    map.put(RES_BOOK_COLOR, bookPress.getColor());
                }
            }
            list.add(map);
        }

        mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
        mesg.add(RES_BOOK_LIST, list);
        return mesg;
    }

    /**
     * 根据教材获取单元信息
     * <p>
     * 目前调用的第三方：GreatAdventure
     *
     * @return 返回单元信息列表
     */
    @RequestMapping(value = "/units.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchBookUnits() {
        MapMessage mesg = new MapMessage();

        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            return noUserResult;
        }

        String bookId = getRequestString(REQ_BOOK_ID);
        List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(bookId), UNIT).get(bookId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (NewBookCatalog unit : units) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_UNIT_ID, unit.getId());
            map.put(RES_UNIT_CNAME, unit.getName());
            map.put(RES_RANK, unit.getRank());
            list.add(map);
        }

        mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
        mesg.add(RES_UNIT_LIST, list);
        return mesg;
    }
}
