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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.api.mapper.DisplayMathLessonMapper;
import com.voxlearning.utopia.service.content.api.mapper.ExLesson;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;

/**
 * @author Maofeng Lu
 * @since 14-11-20 下午4:34
 */
@Controller
@RequestMapping("/teacher/smartclazzresource")
public class TeacherSmartClazzResourceController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    /**
     * 课堂资源首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return "redirect:/teacher/smartclazz/list.vpage";
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(getRequestLong("clazzId"));
        if (clazz == null) {
            return "redirect:/teacher/index.vpage";
        }

        if (!hasClazzTeachingPermission(teacher.getId(), clazz.getId())) {
            logger.warn("此班不属于您,clazzId:{},teacherId:{}", clazz.getId(), teacher.getId());
            return "redirect:/teacher/index.vpage";
        }
        model.addAttribute("clazz", clazz);
        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        model.addAttribute("specifiedSubjects", getSpecifiedSubjectsByTeacherIdAndClazzId(teacher.getId(), clazz.getId()));

        switch (teacher.getSubject()) {
            case ENGLISH:
                return "/teacherv3/smartclazz/resource/english/index";
            case MATH:
            default:
                //数学下线
                return "redirect:/teacher/smartclazz/list.vpage";
        }
    }

    /**
     * 教材列表
     */
    @RequestMapping(value = "books.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage bookList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() != ENGLISH) {
            return MapMessage.errorMessage("老师信息错误");
        }
        long clazzId = getRequestLong("clazzId");
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null || !hasClazzTeachingPermission(teacher.getId(), clazz.getId())) {
            return MapMessage.errorMessage("班级信息错误");
        }
        Term term = SchoolYear.newInstance().currentTerm();
        // 班级使用过的教材
        NewBookProfile book = null;
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
        if (groupMapper != null) {
            NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(groupMapper.getId())
                    .subject(teacher.getSubject())
                    .toList()
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                    .findFirst()
                    .orElse(null);
            if (newClazzBookRef != null && newClazzBookRef.getBookId() != null) {
                book = newContentLoaderClient.loadBook(newClazzBookRef.getBookId());
            }
        }
        // 根据年级，学期拿到的
        List<NewBookProfile> newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, clazz.getClazzLevel().getLevel(), term.getKey());
        if (book == null && CollectionUtils.isEmpty(newBookProfiles)) {
            return MapMessage.errorMessage("未找到合适的教材");
        }
        List<Map<String, Object>> bookList = new ArrayList<>();
        if (book != null) {
            bookList.add(MiscUtils.m("bookId", book.getId(), "bookName", book.getName(), "defaultBook", false));
        }
        if (CollectionUtils.isNotEmpty(newBookProfiles)) {
            for (NewBookProfile newBookProfile : newBookProfiles) {
                if (book == null || !Objects.equals(newBookProfile.getId(), book.getId())) {
                    bookList.add(MiscUtils.m("bookId", newBookProfile.getId(), "bookName", newBookProfile.getName(), "defaultBook", false));
                }
            }
        }
        bookList.get(0).put("defaultBook", true);
        return MapMessage.successMessage().add("books", bookList);
    }

    /**
     * 单元列表
     */
    @RequestMapping(value = "units.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitList() {
        String bookId = getRequestString("bookId");
        List<NewBookCatalog> moduleList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.MODULE).get(bookId);
        List<Map<String, Object>> units;
        if (CollectionUtils.isEmpty(moduleList)) {
            List<NewBookCatalog> unitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).get(bookId);
            units = unitList.stream()
                    .sorted(new NewBookCatalog.RankComparator())
                    .map(unit -> MiscUtils.m("unitId", unit.getId(), "unitName", unit.getAlias()))
                    .collect(Collectors.toList());
        } else {
            List<String> moduleIdList = moduleList.stream()
                    .map(NewBookCatalog::getId)
                    .collect(Collectors.toList());
            Map<String, List<NewBookCatalog>> unitMap = newContentLoaderClient.loadChildren(moduleIdList, BookCatalogType.UNIT);
            units = new ArrayList<>();
            for (String moduleId : moduleIdList) {
                List<NewBookCatalog> newBookCatalogList = unitMap.getOrDefault(moduleId, Collections.emptyList());
                units.addAll(newBookCatalogList.stream()
                        .sorted(new NewBookCatalog.RankComparator())
                        .map(unit -> MiscUtils.m("unitId", unit.getId(), "unitName", unit.getAlias()))
                        .collect(Collectors.toList()));
            }
        }
        return MapMessage.successMessage().add("units", units);
    }


    /**
     * 根据单元获取练习
     * ENGLISH : 获取基础练习
     * MATH    : 数学获取基础练习(包括专项知识点练习)
     */
    @RequestMapping(value = "lesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lesson(HttpServletRequest request) {
        Long unitId = conversionService.convert(request.getParameter("unitId"), Long.class);

        MapMessage message = MapMessage.successMessage();
        Teacher teacher = getSubjectSpecifiedTeacher();
        switch (teacher.getSubject()) {
            case ENGLISH:
                Unit unit = englishContentLoaderClient.loadEnglishUnit(unitId);
                if (unit == null) {
                    return MapMessage.errorMessage("单元ID错误unitId:" + unitId);
                }
                List<ExLesson> lessons = englishContentLoaderClient.loadUnitExLessons(unitId);
                for (ExLesson exLesson : lessons) {
                    for (Sentence sentence : englishContentLoaderClient.loadEnglishLessonSentences(exLesson.getId())) {
                        exLesson.getPointList().add(sentence.getEnText());
                    }
                }
                message.add("total", lessons.size());
                message.add("rows", lessons);
                break;
            case MATH:
                List<DisplayMathLessonMapper> mathLessons = mathContentLoaderClient.getExtension().loadMathLessonPracticeTypeInfoByUnitId(unitId);
                List<DisplayMathLessonMapper> specLessons = new ArrayList<>();
                MathUnit mathUnit = mathContentLoaderClient.loadMathUnit(unitId);
                if (mathUnit == null) {
                    return MapMessage.errorMessage("math unit not exists");
                }
                if (mathLessons.size() == 0) {
                    mathLessons = mathContentLoaderClient.getExtension().findMathLessons(mathLessons, mathUnit);
                    mathContentLoaderClient.getExtension().splitMathSpecLessonsByLessons(StudyType.smartclazzDemo, mathLessons);
                    if (mathLessons.size() > 0) {
                        message.add("info", "本单元没有计算知识点，推荐您布置以下计算内容，或者您可以选取其他单元计算知识点进行布置");
                    }
                } else {
                    //因为lessons包含specLessons，如果lessons为空specLessons肯定为空
                    specLessons = mathContentLoaderClient.getExtension().splitMathSpecLessonsByLessons(StudyType.smartclazzDemo, mathLessons);
                }

                message.setSuccess(true);
                message.add("unitId", unitId);
                message.add("bookId", mathUnit.getBookId());
                message.add("rows", mathLessons);
                message.add("specialRows", specLessons);
                break;
            default:

        }

        return message;
    }

    /**
     * 英语老师根据单元获取课外阅读练习
     */
    @RequestMapping(value = "readinglesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage readingLesson(HttpServletRequest request) {
        return MapMessage.errorMessage("功能已下线");
    }


    /**
     * 老师听力材料页面
     */
    @RequestMapping(value = "ttslistening.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage listening(HttpServletRequest request) {
        int currentPage = ConversionUtils.toInt(request.getParameter("pageNum"));
        if (currentPage <= 0) {
            currentPage = 1;
        }
        String bookId = getRequestString("bookId");
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile != null && newBookProfile.getOldId() != null) {
            Pageable pageable = new PageRequest(currentPage - 1, 10);
            Page<TtsListeningPaper> listeningPaperList = ttsListeningServiceClient.getRemoteReference().getListenPaperPageByUidAndBid(currentUserId(), newBookProfile.getOldId(), pageable);
            return MapMessage.successMessage().add("paperPage", listeningPaperList);
        }
        return MapMessage.errorMessage("教材错误");
    }

    /**
     * 绘本阅读预览,打开新页面(redmine #23107)
     */
    @RequestMapping(value = "readingpreview.vpage", method = RequestMethod.POST)
    public String readingPreview(Model model, HttpServletRequest request) {
        String readingId = request.getParameter("readingId");
        model.addAttribute("readingId", readingId);
        model.addAttribute("tts_url", ProductConfig.getMainSiteBaseUrl() + "/tts.vpage");
        model.addAttribute("readingFlashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, "/resources/apps/flash/Reading.swf"));
        return "/teacherv3/smartclazz/resource/english/readingpreview";
    }
}
