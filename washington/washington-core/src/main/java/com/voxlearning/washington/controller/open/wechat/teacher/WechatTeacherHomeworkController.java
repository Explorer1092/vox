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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.mapper.HomeworkMapper;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.BookDat;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.controller.open.v1.content.AbstractContentLoaderWrapper;
import com.voxlearning.washington.controller.open.v1.content.ContentLoaderWrapperFactory;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.OpenApiReturnCode.SUCCESS_CODE;
import static com.voxlearning.washington.controller.open.OpenApiReturnCode.SYSTEM_ERROR_CODE;

/**
 * 老师布置/检查作业
 * Created by Shuai Huan on 2014/11/17.
 */
@Controller
@RequestMapping(value = "/open/wechat/homework")
@Slf4j
public class WechatTeacherHomeworkController extends AbstractOpenController {
    private static final long MIN_QUIZ_MILLS = TimeUnit.MINUTES.toMillis(5);

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ContentLoaderWrapperFactory contentLoaderWrapperFactory;

    private static int getMaxQuestionCount(int basePlayTime, int maxPlayTime) {
        int maxCount = maxPlayTime / 10 / basePlayTime;
        if (maxCount > 5) {
            maxCount = 5;
        } else if (maxCount == 0) {
            maxCount = 1;
        }

        return maxCount;
    }

    /**
     * 获取可以布置作业的班级数据
     */
    @RequestMapping(value = "loadclazzcanbeassigned.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadClazzCanBeAssigned(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 手动布置英语作业，自主选择题型
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "loadenunitpoints.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadEnUnitPoints(HttpServletRequest request) {

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 手动布置数学作业，自主选择题型
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "loadmathunitpoints.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadMathUnitPoints(HttpServletRequest request) {

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 自动选择英语作业（10分钟或者20分钟）
     */
    @RequestMapping(value = "quickselectenhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext qSelectEnLesson(HttpServletRequest request) {

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 自动选择数学作业
     */
    @RequestMapping(value = "quickselectmathhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext qSelectMathLesson(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }


    @RequestMapping(value = "bookinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bookInfo(HttpServletRequest request) {

        OpenAuthContext context = getOpenAuthContext(request);
        String subject = ConversionUtils.toString(context.getParams().get("subject"));
        Long bookId = ConversionUtils.toLong(context.getParams().get("bookId"));

        if (StringUtils.isBlank(subject) || bookId == 0) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }

        Map<String, Object> bookInfo = new HashMap<>();
        AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject);
        if (loaderWrapper != null) {
            bookInfo = loaderWrapper.loadBookInfo(subject, bookId);
        }

        context.setCode("200");
        context.add("bookInfo", bookInfo);
        return context;
    }

    // 根据班级Ids获取可用学豆最大值
    @RequestMapping(value = "maxic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext calculateMaxIntegralCount(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(context.getParams().get("uid"), Long.MIN_VALUE);
        String clazzIds = SafeConverter.toString(context.getParams().get("cids"));
        if (teacherId == Long.MIN_VALUE || StringUtils.isBlank(clazzIds)) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }
        Set<Long> cids = Arrays.asList(StringUtils.split(clazzIds, ",")).stream()
                .map(ConversionUtils::toLong).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(cids)) {
            context.setCode(SYSTEM_ERROR_CODE);
            return context;
        }

        try {
            Map<String, Object> map = businessTeacherServiceClient
                    .calculateHomeworkMaxIntegralCount(teacher, cids);
            context.add("dc", SafeConverter.toInt(map.get("dc")));
            context.add("mc", SafeConverter.toInt(map.get("mc")));
            context.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            context.setCode(SYSTEM_ERROR_CODE);
        }
        return context;
    }

    /**
     * 布置作业
     */
    @RequestMapping(value = "arrangehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext arrangeHomework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 未检查作业列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext homeworklist(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(context.getParams().get("uid"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(userId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList());
        if (clazzs.isEmpty()) {
            context.setCode("400");
            context.setError("empty class");
            return context;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        Subject subject = teacher.getSubject();
        List<HomeworkMapper> homeworkList = businessTeacherServiceClient.getHomeworkMapperList(teacher.getId(), subject);

        //过期的作业按到期时间排序，作业到期时间越早，排名越靠前
        List<HomeworkMapper> pastDueList = homeworkList.stream().filter(source ->
                source.isPastdue() && StringUtils.isNotEmpty(source.getHomeWorkId())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(pastDueList)) {
            Collections.sort(pastDueList, (o1, o2) -> o1.getEndDate().compareTo(o2.getEndDate()));
        }

        //未过期作业按布置作业时间先后排序，布置作业时间越晚，排名越靠前
        List<HomeworkMapper> nonPastDueList = homeworkList.stream().filter(source ->
                !source.isPastdue() && StringUtils.isNotEmpty(source.getHomeWorkId())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(nonPastDueList)) {
            Collections.sort(nonPastDueList, new Comparator<HomeworkMapper>() {
                @Override
                public int compare(HomeworkMapper o1, HomeworkMapper o2) {
                    return o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                }
            });
        }
        List<HomeworkMapper> result = new LinkedList<>();
        result.addAll(pastDueList);
        result.addAll(nonPastDueList);
        context.setCode("200");
        if (subject != null) {
            context.add("homeworkType", subject.name());
        }
        context.add("checkHomeworkList", result);
        return context;
    }

    @RequestMapping(value = "checkhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext check(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "deletehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext deleteHomework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "homeworkfinishsituation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext homeworkFinishSituation(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 根据班级获取作业历史列表
     */
    @RequestMapping(value = "historylist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext historyList(HttpServletRequest request) {

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "historydetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext historyDetail(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "historydetailbyuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext historyDetailByUser(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "tgchid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext teacherGetCheckHomeworkIntegralDetail(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 手动获取同步习题数据
     */
    @RequestMapping(value = "manualselectexam.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext manualSelectExam(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 换教材
     */
    @RequestMapping(value = "changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext changeBook(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        long bookId = SafeConverter.toLong(context.getParams().get("bookId"), Long.MIN_VALUE);
        String clazzIds = SafeConverter.toString(context.getParams().get("clazzIds"));
        long userId = SafeConverter.toLong(context.getParams().get("uid"), Long.MIN_VALUE);
        int type = SafeConverter.toInt(context.getParams().get("type"), 0);//0:更换，1：删除
        if (bookId == Long.MIN_VALUE || StringUtils.isEmpty(clazzIds) || userId == Long.MIN_VALUE) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }
        ChangeBookMapper mapper = new ChangeBookMapper();
        mapper.setBooks(String.valueOf(bookId));
        mapper.setClazzs(clazzIds);
        mapper.setType(type);
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        MapMessage message = contentServiceClient.setClazzBook(teacher, mapper);
        if (message.isSuccess()) {
            if (teacher != null && Subject.MATH == teacher.getSubject()) {
                NewBookProfile newBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(Subject.MATH, bookId);
                if (newBookProfile != null) {
                    ChangeBookMapper newMapper = new ChangeBookMapper();
                    newMapper.setClazzs(clazzIds);
                    newMapper.setBooks(newBookProfile.getId());
                    newMapper.setType(type);
                    message = newContentServiceClient.getRemoteReference().setClazzBook(teacher, newMapper);
                    if (message.isSuccess()) {
                        context.setCode("200");
                        return context;
                    }
                }
            }
        }
        context.setCode("400");
        context.setError(message.getInfo());
        return context;
    }

    /**
     * 获取班级课本列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "clazzbooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadClazzBooks(HttpServletRequest request) {

        OpenAuthContext context = getOpenAuthContext(request);
        String level = ConversionUtils.toString(context.getParams().get("clazzLevel"));
        Long teacherId = ConversionUtils.toLong(context.getParams().get("uid"));
        if (StringUtils.isBlank(level) || teacherId == 0) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.getSubject() == null) {
            context.setCode("400");
            context.setError("invalid parameters");
            return context;
        }
        ClazzLevel clazzLevel = ClazzLevel.of(ConversionUtils.toInt(level));
        switch (teacher.getSubject()) {
            case ENGLISH:
                List<Book> books = englishContentLoaderClient.getExtension()
                        .loadBookByRegionCodeAndClassLevel(teacher.getRootRegionCode(), clazzLevel,
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                books = books.stream().filter(source -> source.getBookType() != null && teacher.getKtwelve() != null
                        && teacher.getKtwelve().getLevel() == source.getBookType()).collect(Collectors.toList());
                engPaintedSkin(books); // 画皮
                context.add("books", books);
                break;
            case MATH:
                List<MathBook> mathBooks = mathContentLoaderClient.getExtension()
                        .loadMathBooksByClassLevelWithSortByUpdateTime(clazzLevel, teacher.getRootRegionCode(),
                                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                mathPaintedSkin(mathBooks); // 画皮
                context.add("books", mathBooks);
                break;
            case CHINESE:
                List<BookDat> chineseBooks = chineseContentLoaderClient.loadChineseBooks()
                        .enabled()
                        .online()
                        .clazzLevel(clazzLevel)
                        .toList()
                        .stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                        .collect(Collectors.toList());
                chinesePaintedSkin(chineseBooks); // 画皮
                context.add("books", chineseBooks);
                break;
            default:
                break;
        }
        context.setCode("200");
        return context;
    }

    protected void engPaintedSkin(List<Book> books) {
        for (Book book : books) {
            if (!StringUtils.contains(book.getImgUrl(), "catalog_new")) {
                BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, book.getPress());
                if (bookPress != null) {
                    book.setViewContent(bookPress.getViewContent());
                    book.setColor(bookPress.getColor());
                }
                book.setImgUrl(StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
            }
        }
    }

    /**
     * 自动布置作业
     */
    @RequestMapping(value = "autoassignhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext autoAssignHomework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * for wechat adjust homework only
     *
     * @param userId
     * @param homeworkId
     * @param endDateTime
     * @param homeworkType
     * @return
     */
//    private MapMessage updateHomeworkEndDateTime(long userId, String homeworkId, Date endDateTime, String homeworkType) {
//        switch (homeworkType) {
//            case "homework":
//                Homework homework = homeworkLoaderClient.loadEnglishHomework(homeworkId);
//                if (!endDateTime.after(homework.getHomeworkStartTime())) {
//                    return MapMessage.errorMessage("开始时间必须早于截止时间");
//                }
//                homework.setEndDate(endDateTime);
//                return atomicLockManager.wrapAtomic(homeworkServiceClient)
//                        .keys(userId, homeworkType, homeworkId)
//                        .proxy()
//                        .updateEnglishHomeworkEndDate(homework);
//            case "quiz":
//                Quiz quiz = quizLoaderClient.loadQuiz(SafeConverter.toLong(homeworkId));
//                if (!endDateTime.after(quiz.getStartDateTime())) {
//                    return MapMessage.errorMessage("开始时间必须早于截止时间");
//                }
//                if (endDateTime.getTime() - System.currentTimeMillis() < MIN_QUIZ_MILLS) {
//                    return MapMessage.errorMessage("截止时间有误，最短测验时间为5分钟");
//                }
//                quiz.setEndDateTime(endDateTime);
//                return atomicLockManager.wrapAtomic(quizServiceClient.getRemoteReference())
//                        .keys(userId, homeworkType, homeworkId)
//                        .proxy()
//                        .updateQuiz(quiz);
//            case "workbook":
//                WorkbookHomework workbookHomework = workbookHomeworkLoaderClient.loadWorkbookHomework(SafeConverter.toLong(homeworkId));
//                if (!endDateTime.after(workbookHomework.getHomeworkStartTime())) {
//                    return MapMessage.errorMessage("开始时间必须早于截止时间");
//                }
//                workbookHomework.setEndDate(endDateTime);
//                return atomicLockManager.wrapAtomic(workbookHomeworkServiceClient.getRemoteReference())
//                        .keys(userId, homeworkType, homeworkId)
//                        .proxy()
//                        .updateWorkbookHomeworkEndDate(workbookHomework);
//            default:
//                return MapMessage.errorMessage("暂不支持此类型作业的调整");
//        }
//    }

    /**
     * 调整作业:只是为了兼容微信端所以兼容的这个接口
     */
    @RequestMapping(value = "adjusthomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext adjustHomework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "voicelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext voiceList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "voicerecommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext submitVoiceRecommend(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }
}