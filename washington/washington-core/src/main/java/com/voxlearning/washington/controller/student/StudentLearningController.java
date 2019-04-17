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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.BookDat;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkHistoryDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkHistoryMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.*;

@Controller
@RequestMapping("/student/learning")
public class StudentLearningController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    // 2014暑期改版 -- 自学中心
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "studentv3/learning/offlinenotice";
    }

    @RequestMapping(value = "changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook() {
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
            return MapMessage.successMessage().add("subject", subjectText).add("currentBookId", bookId);
        }
        return MapMessage.errorMessage("Missing params");
    }

    // 2014暑期改版 -- 自学中心 -- 换教材
    @RequestMapping(value = "books.vpage", method = RequestMethod.GET)
    public String clickChangeBook(Model model) {
        Subject subject = Subject.of(getRequest().getParameter("subjectType"));
        model.addAttribute("subjectType", subject);
        return "studentv3/learning/books";
    }

    /**
     * 2014暑期改版 -- 自学中心 -- 换教材
     * 1. 学生有班级，根据班级所在区域和年级查询课本
     * 2. 学生无班级，根据年级和学期查询课本
     */
    @RequestMapping(value = "bookschip.vpage", method = RequestMethod.GET)
    public String changeBook(Model model) {
        StudentDetail student = currentStudentDetail();
        // 如果没有参数level，则使用当前用户所在班级的年级，如果用户没有班级，1年级
        Integer level = getRequestInt("level");
        if (level == 0) {
            level = student.getClazz() == null ? 1 : ConversionUtils.toInt(student.getClazz().getClassLevel());
        }
        ClazzLevel clazzLevel = ClazzLevel.of(level);
        Subject subject = Subject.of(getRequest().getParameter("subjectType"));
        final Term term = SchoolYear.newInstance().currentTerm();
        ExRegion exRegion = userLoaderClient.loadUserRegion(currentUser());
        model.addAttribute("subjectType", subject);
        model.addAttribute("level", getRequestInt("level"));
        switch (subject) {
            case ENGLISH:
                List<Book> engLishBooks;
                if (exRegion != null) {
                    engLishBooks = englishContentLoaderClient.getExtension()
                            .loadBookByRegionCodeAndClassLevel(exRegion.getCode(), clazzLevel,
                                    new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                } else {
                    List<Book> books = new LinkedList<>(englishContentLoaderClient.getExtension().loadEnglishBooks(clazzLevel));
                    Collections.sort(books, (o1, o2) -> {
                        int u1 = o1.fetchtTermType() == term || o1.fetchtTermType() == Term.全年 ? 0 : 1;
                        int u2 = o2.fetchtTermType() == term || o2.fetchtTermType() == Term.全年 ? 0 : 1;
                        return Integer.compare(u1, u2);
                    });
                    engLishBooks = books;
                }
                engLishBooks = engLishBooks.stream().filter(source ->
                        source.getBookType() != null && Ktwelve.PRIMARY_SCHOOL.getLevel() == source.getBookType()
                ).collect(Collectors.toList());
                engPaintedSkin(engLishBooks); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(engLishBooks) ? new LinkedList<>() : engLishBooks);
                break;
            case MATH:
                List<MathBook> mathBooks;
                if (exRegion != null) {
                    mathBooks = mathContentLoaderClient.getExtension()
                            .loadMathBooksByClassLevelWithSortByUpdateTime(clazzLevel, exRegion.getCode(),
                                    new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                } else {
                    mathBooks = mathContentLoaderClient.getExtension().loadMathBooksByClassLevelOrderByTerm(clazzLevel, term);
                }
                mathPaintedSkin(mathBooks); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(mathBooks) ? new LinkedList<>() : mathBooks);
                break;
            case CHINESE:
                List<BookDat> chineseBooks;
                if (exRegion != null) {
                    chineseBooks = chineseContentLoaderClient.loadChineseBooks()
                            .enabled()
                            .online()
                            .clazzLevel(clazzLevel)
                            .toList()
                            .stream()
                            .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                            .collect(Collectors.toList());
                } else {
                    chineseBooks = chineseContentLoaderClient.loadChineseBooks()
                            .enabled()
                            .online()
                            .clazzLevel(clazzLevel)
                            .sorted((o1, o2) -> {
                                int t1 = o1.getTermType() == term.getKey() || o1.getTermType() == Term.全年.getKey() ? 0 : 1;
                                int t2 = o2.getTermType() == term.getKey() || o2.getTermType() == Term.全年.getKey() ? 0 : 1;
                                return Integer.compare(t1, t2);
                            })
                            .toList();
                }
                chinesePaintedSkin(chineseBooks); // 画皮
                model.addAttribute("books", CollectionUtils.isEmpty(chineseBooks) ? new LinkedList<>() : chineseBooks);
                break;
            default:
                logger.warn("subjectType param value {} unknown in studentSelfStudy method", subject.name());
        }
        return "studentv3/learning/bookschip";
    }

    // 2014暑期改版 -- 英语作业历史
    @RequestMapping(value = "/history/english.vpage", method = RequestMethod.GET)
    public String englishHomeworkHistoryList() {
        return "redirect:/student/learning/history/list.vpage?subject=ENGLISH";
    }

    @RequestMapping(value = "/history/detail/english.vpage", method = RequestMethod.GET)
    public String englishHomeworkHistoryDetailList(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "viewexam.vpage", method = RequestMethod.GET)
    public String viewExamDetail(HttpServletRequest request, Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "/history/detail/subjective.vpage", method = RequestMethod.GET)
    public String subjectiveHomeworkHistoryDetailList(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "/history/detail/math.vpage", method = RequestMethod.GET)
    public String mathHomeworkHistoryDetailList(Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "/history/detail/chinese.vpage", method = RequestMethod.GET)
    public String chineseHomeworkHistoryDetailList(Model model) {
        return "redirect:/student/index.vpage";
    }

    // 2014暑期改版 -- 英语测验历史
    @RequestMapping(value = "/history/englishquiz.vpage", method = RequestMethod.GET)
    public String englishQuizHistoryList(Model model) {
        return "redirect:/student/index.vpage";
    }

    // 2014暑期改版 -- 数学测验历史
    @RequestMapping(value = "/history/mathquiz.vpage", method = RequestMethod.GET)
    public String mathQuizHistoryList(Model model) {
        return "redirect:/student/index.vpage";
    }

    // 2015暑期改版 -- 查看测验试卷详情
    @RequestMapping(value = "viewquizpaper.vpage", method = RequestMethod.GET)
    public String viewQuizPaperDetail(HttpServletRequest request, Model model) {
        return "redirect:/student/index.vpage";
    }

    @RequestMapping(value = "/history/detail/oral.vpage", method = RequestMethod.GET)
    public String viewOralQuiz(HttpServletRequest request, Model model) {
        return "redirect:/student/index.vpage";
    }


    // 教辅历史列表
    @RequestMapping(value = "/history/englishworkbook.vpage", method = RequestMethod.GET)
    public String englishWorkbookHistoryList(Model model) {
        return "redirect:/student/index.vpage";
    }

    /**
     * 课本随身听
     */
    @RequestMapping(value = "downloadapp.vpage", method = RequestMethod.GET)
    public String downloadStudentApp() {
        return "studentv3/learning/downloadapp";
    }

    /**
     * 模拟考试
     */
    @RequestMapping(value = "examination.vpage", method = RequestMethod.GET)
    public String exam() {
        return "studentv3/learning/examination";
    }

    /**
     * 接口：查询作业历史列表
     * xuesong.zhang
     */
    @RequestMapping(value = "/history/newhomework/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage newHomeworkHistoryList() {
        String subjectStr = getRequestString("subject");
        // 页数
        int currentPage = getRequestInt("page", 1);
        Pageable page = new PageRequest((currentPage < 1) ? 0 : currentPage - 1, 5);
        Page<HomeworkHistoryMapper> history = new PageImpl<>(Collections.emptyList(), page, 0);

        Long userId = currentUserId();
        if (userId == null) {
            return MapMessage.errorMessage("没有权限访问").setErrorUrl("/login.vpage");
        }
        if (StringUtils.isBlank(subjectStr)) {
            return MapMessage.errorMessage("学科错误");
        }
        GroupMapper mapper = deprecatedGroupLoaderClient.loadStudentGroups(userId, false).stream()
                .filter(o -> GroupType.TEACHER_GROUP.equals(o.getGroupType()) && StringUtils.equals(o.getSubject().name(), subjectStr))
                .findFirst()
                .orElse(null);

        if (mapper == null || mapper.getId() == null) {
            return MapMessage.successMessage().add("history", history);
        }

        Long groupId = mapper.getId();
        Subject subject = Subject.of(subjectStr);
        history = newHomeworkServiceClient.loadStudentHomeworkHistory(groupId, subject,  userId, page);
        return MapMessage.successMessage().add("history", history);
    }


    //pc学生历史报告接口
    @RequestMapping(value = "/history/newhomework/timelimitlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage timeLimitNewHomeworkHistoryList(){
        String subjectStr = getRequestString("subject");
        // 页数
        int currentPage = getRequestInt("page", 1);
        Pageable page = new PageRequest((currentPage < 1) ? 0 : currentPage - 1, 5);
        Page<HomeworkHistoryMapper> history = new PageImpl<>(Collections.emptyList(), page, 0);

        Long userId = currentUserId();
        if (userId == null) {
            return MapMessage.errorMessage("没有权限访问").setErrorUrl("/login.vpage");
        }
        if (StringUtils.isBlank(subjectStr)) {
            return MapMessage.errorMessage("学科错误");
        }
        String beginStr = this.getRequestString("begin");
        if (StringUtils.isBlank(beginStr)) {
            return MapMessage.errorMessage("开始时间参数错误");
        }
        Date begin = DateUtils.stringToDate(beginStr, "yyyy-MM-dd");
        if (begin == null) {
            return MapMessage.errorMessage("开始时间参数错误");
        }
        Date end = DateUtils.nextDay(begin, NewHomeworkConstants.LIMIT_SELECT_OLD_HOMEWORK);


        GroupMapper mapper = deprecatedGroupLoaderClient.loadStudentGroups(userId, false).stream()
                .filter(o -> GroupType.TEACHER_GROUP.equals(o.getGroupType()) && StringUtils.equals(o.getSubject().name(), subjectStr))
                .findFirst()
                .orElse(null);

        if (mapper == null || mapper.getId() == null) {
            return MapMessage.successMessage().add("history", history);
        }

        Long groupId = mapper.getId();
        Subject subject = Subject.of(subjectStr);
        history = newHomeworkServiceClient.loadStudentHomeworkHistoryWithTimeLimit(groupId, subject, begin, end, userId, page);
        return MapMessage.successMessage().add("history", history);
    }



    /**
     * 接口：查询作业详情
     * xuesong.zhang
     */
    @RequestMapping(value = "/history/newhomework/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newHomeworkHistoryDetail() {
        User user = currentUser();


        String homeworkId = getRequestString("homeworkId");
        if (user == null || StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("没有权限访问").setErrorUrl("/login.vpage");
        }
        HomeworkHistoryDetail detail = newHomeworkServiceClient.loadStudentHomeworkHistoryDetail(homeworkId, currentUserId());
        if(detail == null){
            return MapMessage.errorMessage("作业不存在或者没有这个作业的权限，请重新登录");
        }else {
            return MapMessage.successMessage().add("detail", detail);
        }

    }

    /**
     * @return base_app category type details
     */
    @RequestMapping(value = "report/detailsbaseapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage detailsBaseApp() {
        String homeworkId = getRequestString("homeworkId");
        String categoryId = getRequestString("categoryId");
        String lessonId = getRequestString("lessonId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.BASIC_APP.name()));
        User user = currentStudent();
        if (StringUtils.isBlank(homeworkId) || StringUtils.isBlank(categoryId) || StringUtils.isBlank(lessonId) || Objects.isNull(user) || objectiveConfigType == null) {
            return MapMessage.errorMessage();
        }
        Long studentId = user.getId();
        return newHomeworkReportServiceClient.reportDetailsBaseApp(homeworkId, categoryId, lessonId, studentId, objectiveConfigType);
    }

    /**
     * 获取学生某个阅读绘本的结果详情
     */
    @RequestMapping(value = "report/personalreadingdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalReadingDetail() {
        String homeworkId = getRequestString("homeworkId");
        String readingId = getRequestString("readingId");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(getRequestParameter("objectiveConfigType", ObjectiveConfigType.READING.name()));

        User user = currentStudent();
        if (StringUtils.isBlank(homeworkId) || user == null) {
            return MapMessage.errorMessage("homeworkId or studentId is null");
        }
        Long studentId = user.getId();
        MapMessage mapMessage = newHomeworkReportServiceClient.personalReadingDetail(homeworkId, studentId, readingId, null,objectiveConfigType);
        mapMessage.add("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt,
                MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));

        mapMessage.add("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt,
                MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));
        return mapMessage;
    }


    /**
     * 口语交际个人二级详情页面
     * @return
     */
    @RequestMapping(value = "report/personaloralcommunicationdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personalOralCommunicationScoreDetail() {
        String homeworkId = getRequestString("homeworkId");
        String stoneId = getRequestString("stoneId");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id为空");
        }
        if (StringUtils.isBlank(stoneId)) {
            return MapMessage.errorMessage("题包为空");
        }
        User user = currentStudent();
        if (StringUtils.isBlank(homeworkId) || user == null) {
            return MapMessage.errorMessage("homeworkId or studentId is null");
        }
        Long studentId = user.getId();
        if (studentId <= 0) {
            return MapMessage.errorMessage("学生id为空");
        }
        if (StringUtils.isBlank(stoneId)) {
            return MapMessage.errorMessage("题包为空");
        }
        try {
            return newHomeworkReportServiceClient.personalOralCommunicationDetail(homeworkId, studentId, stoneId, null);
        } catch (Exception ex) {
            logger.error("student Failed to load personalOralCommunicationScoreDetail homeworkId:{},studentId{},stoneId{}", homeworkId, stoneId, ex);
            return MapMessage.errorMessage("获取口语交际个人二级详情异常");
        }
    }

    /**
     * 接口：个人答题详情查看
     * xuesong.zhang
     */
    @RequestMapping(value = "history/newhomework/answerdetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newHomeworkHistoryAnswerDetail() {
        String homeworkId = getRequestString("homeworkId");
        User user = currentUser();
        NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
        if (user == null) {
            return MapMessage.errorMessage("没有权限访问").setErrorUrl("/login.vpage");
        }
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        Long studentId = user.getId();
        MapMessage mapMessage;
        mapMessage = newHomeworkReportServiceClient.loadNewHomeworkReportExamErrorRates(homeworkId, studentId, null);
        mapMessage.add("questionUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions" + Constants.AntiHijackExt,
                MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));

        mapMessage.add("completedUrl", UrlUtils.buildUrlQuery("/student/exam/newhomework/questions/answer" + Constants.AntiHijackExt,
                MapUtils.m("homeworkId", homeworkId, "objectiveConfigType", "")));
        mapMessage.add("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return mapMessage;
    }

    /**
     * 学生作业报告页 陈萌 2016.3.23
     */
    @RequestMapping(value = "/history/newhomework/homeworkreport.vpage", method = RequestMethod.GET)
    public String getStudentHomeworkReport(Model model) {
        model.addAttribute("subjectName", getRequestParameter("subject", "MATH"));
        model.addAttribute("homeworkId", getRequestParameter("homeworkId", ""));
        model.addAttribute("navLink", getRequestParameter("navLink", "record"));
        return "studentv3/learning/history/newhomework/homeworkreport";
    }

    /**
     * 学生作业详情 陈萌 2016.3.24
     */
    @RequestMapping(value = "/history/newhomework/homeworkdetail.vpage", method = RequestMethod.GET)
    public String getStudentHomeworkDetail(Model model) {
        model.addAttribute("subjectName", getRequestParameter("subject", "MATH"));
        model.addAttribute("homeworkId", getRequestParameter("homeworkId", ""));
        if(grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(currentStudentDetail(), "PCHomework", "UseVenus")){
            return "studentv3/learning/history/newhomeworkv5/homeworkdetail";
        }else{
            return "studentv3/learning/history/newhomework/homeworkdetail";
        }
    }

    /**
     * 学生作业列表 陈萌 2016.3.24
     */
    @RequestMapping(value = "/history/list.vpage", method = RequestMethod.GET)
    public String getStudentHomeworkMath(Model model) {
        model.addAttribute("subjectName", getRequestParameter("subject", "MATH"));
        return "studentv3/learning/history/newhomework/homeworklist";
    }

    @RequestMapping(value = "history/earlylist.vpage", method = RequestMethod.GET)
    public String getStudentEarlyHomeworkMath(Model model) {
        //添加学生作业历史查询时间

        Date endDate = NewHomeworkConstants.STUDENT_ALLOW_SEARCH_HOMEWORK_START_TIME;
        Date startDate = DateUtils.calculateDateDay(endDate,-30);
        model.addAttribute("startDate",DateUtils.dateToString(startDate, DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("subjectName", getRequestParameter("subject", "MATH"));
        //这个跳转页面待定
        return "studentv3/learning/history/newhomework/earlyhomeworklist";
    }

    /**
     * 获取某个基础应用类别的结果详情
     */
    @RequestMapping(value = "/history/categorydetail.vpage", method = RequestMethod.GET)
    public String getStudentCategoryDetail(Model model) {
        model.addAttribute("detailUrl", UrlUtils.buildUrlQuery("/student/learning/report/detailsbaseapp.vpage",
                MapUtils.m("homeworkId", getRequestString("hid"),
                        "lessonId", getRequestString("lessonId"),
                        "categoryId", getRequestString("categoryId"),
                        "objectiveConfigType", getRequestString("objectiveConfigType"))));
        return "studentv3/learning/history/newhomework/categorydetail";
    }

    /**
     * 获取某个基础应用类别的结果详情
     */
    @RequestMapping(value = "/history/readingdetail.vpage", method = RequestMethod.GET)
    public String getStudentReadingdetail(Model model) {
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        model.addAttribute("detailUrl", "/student/learning/report/personalreadingdetail.vpage");
        return "studentv3/learning/history/newhomework/readingdetail";
    }

    /**
     * 学生作业详情 陈萌 2016.3.24
     */
    @RequestMapping(value = "/history/newhomework/singleoralcommunicationpackagedetail.vpage", method = RequestMethod.GET)
    public String getSingleOralCommunicationPackageDetail(Model model) {
        model.addAttribute("homeworkId",getRequestString("hid"));
        return "studentv3/learning/history/newhomeworkv5/singleoralcommunicationpackagedetail";
    }
}
