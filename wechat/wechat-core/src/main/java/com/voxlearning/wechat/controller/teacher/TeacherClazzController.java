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

package com.voxlearning.wechat.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.wechat.controller.AbstractTeacherWebController;
import com.voxlearning.wechat.support.mapper.teacher.BookCatalogMapper;
import com.voxlearning.wechat.support.mapper.teacher.BookProfileMapper;
import com.voxlearning.wechat.support.mapper.teacher.ClazzSummaryMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 19/1/2016.
 */
@Controller
@RequestMapping(value = "/teacher/clazz")
public class TeacherClazzController extends AbstractTeacherWebController {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    //查询老师有效的班级摘要(可布置作业和不可布置作业)
    @RequestMapping(value = "/summary.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAssignableClazzSummaryByTeacherId() {
        Long teacherId = getTeacherIdBySubject();

        try {
            //查出老师所有班级
            List<Clazz> clazzs = getTeacherClazzs();
            if (CollectionUtils.isEmpty(clazzs)) return MapMessage.errorMessage("老师还未创建班级");

            //查出老师可布置作业班级
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师");

            List<ExClazz> assignableClazzs = newHomeworkContentServiceClient.findTeacherClazzsCanBeAssignedHomework(teacher);
            if (CollectionUtils.isEmpty(assignableClazzs))
                return MapMessage.successMessage().add("summary", new ArrayList<>());
            Map<Long, ExClazz> assignableClazzsMap = new HashMap<>();
            assignableClazzs.forEach(c -> assignableClazzsMap.put(c.getId(), c));

            //得到最终需要的班级列表
            List<ClazzSummaryMapper> summarys = new ArrayList<>();
            clazzs.forEach(c -> {
                if (assignableClazzsMap.containsKey(c.getId())) {
                    ClazzSummaryMapper mapper = new ClazzSummaryMapper(assignableClazzsMap.get(c.getId()));
                    mapper.setAssignable(true);
                    summarys.add(mapper);
                } else {
                    summarys.add(new ClazzSummaryMapper(c));
                }
            });

            return MapMessage.successMessage().add("summary", summarys);
        } catch (Exception ex) {
            logger.error("Get Assignable Clazz Summary error,tid:{}", teacherId, ex);
            return MapMessage.errorMessage("查询失败");
        }
    }

    //查询班级的默认教材
    @RequestMapping(value = "/defaultbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getClazzsDefaultBook(@RequestParam String clazzIds) {
        if (StringUtils.isBlank(clazzIds)) return MapMessage.errorMessage("请选择班级");

        Long teacherId = getTeacherIdBySubject();

        try {
            Set<Long> clzIds = new HashSet<>(StringUtils.toLongList(clazzIds));
            if (CollectionUtils.isEmpty(clzIds)) return MapMessage.errorMessage("请选择班级");

            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
            if (CollectionUtils.isEmpty(clazzs)) return MapMessage.errorMessage("老师还没有创建班级");

            List<Long> teacherClzIds = clazzs.stream().map(Clazz::getId).collect(Collectors.toList());
            if (clzIds.stream().anyMatch(id -> !teacherClzIds.contains(id))) return MapMessage.errorMessage("您没有权限");

            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) return MapMessage.errorMessage("未查询到老师");

            Optional<BookProfileMapper> bookProfileMapper = loadClazzLatestBook(teacher, clzIds, teacher.getSubject());

            if (bookProfileMapper.isPresent())
                return MapMessage.successMessage().add("book", bookProfileMapper.get());

            return MapMessage.errorMessage("未查询到教材");
        } catch (Exception ex) {
            logger.error("Get clazz default book error,tid:{}", teacherId, ex);
            return MapMessage.errorMessage("查询教材失败");
        }
    }

    //查询教材单元列表
    @RequestMapping(value = "/book/units.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUnitsByBookId(@RequestParam String clazzIds, @RequestParam String bookId) {
        if (StringUtils.isBlank(bookId) || StringUtils.isBlank(clazzIds)) return MapMessage.errorMessage("请选择班级和教材");

        try {
            List<BookCatalogMapper> units = new LinkedList<>();

            Set<Long> clzIds = new HashSet<>(StringUtils.toLongList(clazzIds));
            if (CollectionUtils.isEmpty(clzIds)) return MapMessage.errorMessage("请选择班级");

            Teacher teacher = getTeacherBySubject();
            if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");


            Map<String, List<NewBookCatalog>> catalogsMap = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT);
            if (MapUtils.isNotEmpty(catalogsMap)) {
                List<NewBookCatalog> catalogs = catalogsMap.get(bookId);
                if (CollectionUtils.isNotEmpty(catalogs)) {
                    catalogs.forEach(c -> units.add(new BookCatalogMapper(c)));
                }
            }

            List<BookCatalogMapper> sortedUnits = units.stream().sorted((u1, u2) -> u1.getRank().compareTo(u2.getRank())).collect(Collectors.toList());
            Optional<NewClazzBookRef> clazzBookRef = loadClazzLatestBookRef(teacher, clzIds, teacher.getSubject());
            //默认单元判断
            boolean hasDefaultUnit = false;
            for (BookCatalogMapper unit : sortedUnits) {
                if (clazzBookRef.isPresent() && StringUtils.isNotBlank(clazzBookRef.get().getUnitId())
                        && Objects.equals(clazzBookRef.get().getUnitId(), unit.getId())) {
                    unit.setIsDefault(true);
                    hasDefaultUnit = true;
                    break;
                }
            }
            if (!hasDefaultUnit && sortedUnits.size() > 0) {
                sortedUnits.get(0).setIsDefault(true);
            }

            List<Map<String, Object>> moduleList = Collections.emptyList();
            if (Subject.ENGLISH == teacher.getSubject()) {
                List<NewBookCatalog> bookModuleList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.MODULE)
                        .getOrDefault(bookId, Collections.emptyList()).stream()
                        .sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(bookModuleList)) {
                    Map<String, List<BookCatalogMapper>> moduleUnitMaps = new HashMap<>();
                    for (BookCatalogMapper unit : sortedUnits) {
                        String parentId = unit.getParentId();
                        if (StringUtils.isNoneBlank(parentId)) {
                            List<BookCatalogMapper> moduleUnits = moduleUnitMaps.get(parentId);
                            if (moduleUnits == null) {
                                moduleUnits = new ArrayList<>();
                                moduleUnitMaps.put(parentId, moduleUnits);
                            }
                            moduleUnits.add(unit);
                        }
                    }
                    moduleList = bookModuleList.stream()
                            .filter(m -> moduleUnitMaps.containsKey(m.getId()))
                            .map(m -> MiscUtils.m("moduleName", m.getAlias(), "units", moduleUnitMaps.get(m.getId())))
                            .collect(Collectors.toList());
                }
            }
            return MapMessage.successMessage()
                    .add("units", sortedUnits)
                    .add("modules", moduleList);
        } catch (Exception ex) {
            logger.error("Get units of book {} error.", bookId, ex);
            return MapMessage.errorMessage("查询单元信息失败");
        }
    }

    //查询单元内的Sections
    @RequestMapping(value = "/book/unit/lessons.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUnitSections(@RequestParam String unitId) {
        if (null == unitId) {
            return MapMessage.errorMessage("请选择单元");
        }

        Teacher teacher = getTeacherBySubject();
        if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

        try {
            List<BookCatalogMapper> lessonMappers = new LinkedList<>();

            Optional<NewClazzBookRef> ref = loadClazzLatestBookRef(teacher, getTeacherClazzs().stream().map(Clazz::getId).collect(Collectors.toList()), teacher.getSubject());

            //查出所有lesson
            Map<String, List<NewBookCatalog>> lessonsMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
            if (MapUtils.isNotEmpty(lessonsMap)) {
                List<NewBookCatalog> lessons = lessonsMap.get(unitId).stream().sorted((l1, l2) -> l1.getRank().compareTo(l2.getRank())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(lessons)) {
                    lessons.forEach(l -> lessonMappers.add(new BookCatalogMapper(l)));
                }
            }

            //查出所有section
            Set<String> lessonIds = lessonMappers.stream().map(BookCatalogMapper::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(lessonIds)) {
                Map<String, List<NewBookCatalog>> sectionsMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);
                if (MapUtils.isNotEmpty(sectionsMap)) {
                    //将section挂到lesson上
                    List<String> sectionIds = new LinkedList<>();
                    lessonMappers.forEach(l -> {
                        List<NewBookCatalog> sections = sectionsMap.get(l.getId());
                        if (CollectionUtils.isNotEmpty(sections)) {
                            sections = sections.stream().sorted((s1, s2) -> s1.getRank().compareTo(s2.getRank())).collect(Collectors.toList());

                            for (NewBookCatalog section : sections) {
                                sectionIds.add(section.getId());

                                BookCatalogMapper mapper = new BookCatalogMapper(section);
                                l.getChildren().add(mapper);
                            }
                        }
                    });

                    //默认课时为上次布置的课时的下一课时,如果没有就以第一个课时为默认课时
                    boolean hasFindDefaultSection = false;
                    if (ref.isPresent() && null != ref.get().getSectionID()) {
                        for (int i = 0; i < sectionIds.size(); i++) {
                            final int index = i;
                            if (Objects.equals(sectionIds.get(i), ref.get().getSectionID())) {
                                if (i < sectionIds.size() - 1) {
                                    lessonMappers.forEach(l -> l.getChildren().forEach(s -> s.setIsDefault(Objects.equals(s.getId(), sectionIds.get(index + 1)))));
                                    hasFindDefaultSection = true;
                                }
                            }
                        }
                    }
                    if (!hasFindDefaultSection && lessonMappers.get(0).getChildren().size() > 0) {
                        lessonMappers.get(0).getChildren().get(0).setIsDefault(true);
                    }
                }
            }

            return MapMessage.successMessage().add("lessons", lessonMappers);
        } catch (Exception ex) {
            logger.error("Get sections of unit {} error.", unitId, ex);
            return MapMessage.errorMessage("查询知道点失败");
        }
    }

    //查询可更换教材列表
    @RequestMapping(value = "/books.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getBooksForChange(@RequestParam Integer level, @RequestParam Integer term) {
        if (null == level || null == term) {
            return MapMessage.errorMessage("参数错误");
        }

        Teacher teacher = getTeacherBySubject();
        if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

        try {
            if (null == teacher.getSubject()) return MapMessage.errorMessage("您还没有设置学科");

            List<NewBookProfile> newBookProfiles = newHomeworkContentServiceClient.loadBooks(teacher, level, term);
            List<BookProfileMapper> bookProfileMappers = Collections.emptyList();
            if (CollectionUtils.isNotEmpty(newBookProfiles)) {
                bookProfileMappers = newBookProfiles.stream()
                        .map(BookProfileMapper::new)
                        .collect(Collectors.toList());
            }

            return MapMessage.successMessage().add("books", bookProfileMappers);
        } catch (Exception ex) {
            logger.error("Get books for change error,tid:{},level:{},term:{}", teacher.getId(), level, term, ex);
        }
        return MapMessage.errorMessage("查询教材失败");
    }

    //更换教材
    @RequestMapping(value = "/book/change.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeClazzBook(@RequestParam String clazzAndGroupIds, @RequestParam String bookId) {
        Teacher teacher = getTeacherBySubject();
        if (null == teacher) return MapMessage.errorMessage("未查询到老师信息");

        Long teacherId = teacher.getId();
        if (StringUtils.isBlank(clazzAndGroupIds) || null == bookId) {
            return MapMessage.errorMessage("请选择班级和教材");
        }

        try {
            List<String> clazzIds = new ArrayList<>();
            String[] idgs = clazzAndGroupIds.split(",");
            for (String idg : idgs) {
                String[] ids = idg.split("_");
                if (ids.length == 2) {
                    clazzIds.add(ids[0]);
                }
            }

            ChangeBookMapper command = new ChangeBookMapper();
            command.setBooks(bookId);
            command.setClazzs(StringUtils.join(clazzIds, ","));
            return newContentServiceClient.getRemoteReference().setClazzBook(teacher, command);
        } catch (Exception ex) {
            logger.error("Change clazz book error,tid:{},gcIds:{},bookId:{}", teacherId, clazzAndGroupIds, bookId, ex);
        }
        return MapMessage.errorMessage("更换教材失败");
    }

    /**
     * 查询老师的班级列表
     */
    @RequestMapping(value = "/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getClazzList() {
        try {
            List<Clazz> clazzs = getTeacherClazzs();
            List<ClazzSummaryMapper> clazzMappers = new ArrayList<>();
            clazzs.forEach(clazz -> clazzMappers.add(new ClazzSummaryMapper(clazz)));

            return MapMessage.successMessage().add("clazzs", clazzMappers);
        } catch (Exception ex) {
            logger.error("Get teacher's clazz list error,tid:{}", getRequestContext().getUserId(), ex);
        }
        return MapMessage.errorMessage("查询班级失败");
    }

    private Optional<BookProfileMapper> loadClazzLatestBook(Teacher teacher, Collection<Long> clazzIds, Subject subject) {
        if (CollectionUtils.isEmpty(clazzIds)) {
            return Optional.empty();
        }

        Optional<NewClazzBookRef> clazzBookRef = loadClazzLatestBookRef(teacher, clazzIds, subject);

        if (clazzBookRef.isPresent()) {
            String bookId = clazzBookRef.get().getBookId();
            Map<String, NewBookProfile> booksMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));
            NewBookProfile book = booksMap.get(bookId);
            if (book != null) {
                int latestVersion = SafeConverter.toInt(book.getLatestVersion());
                int subjectId = SafeConverter.toInt(book.getSubjectId());
                // 填坑。
                // 小学数学 latestVersion=0 重新推一本默认教材
                if (subjectId == 102 && latestVersion == 0) {
                    book = null;
                }
            }
            if (book != null) {
                return Optional.of(new BookProfileMapper(book));
            }
        }
        // 找不到班级已使用教材,推默认教材
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzIds.iterator().next());
        if (null != clazz) {
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (null != school) {
                String bookId = newContentLoaderClient.initializeClazzBook(subject, clazz.getClazzLevel(), school.getRegionCode());
                if (null != bookId) {
                    Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(Collections.singleton(bookId));
                    if (MapUtils.isNotEmpty(bookProfileMap) && !Objects.isNull(bookProfileMap.get(bookId))) {
                        return Optional.of(new BookProfileMapper(bookProfileMap.get(bookId)));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<NewClazzBookRef> loadClazzLatestBookRef(Teacher teacher, Collection<Long> clazzIds, Subject subject) {
        if (CollectionUtils.isEmpty(clazzIds)) {
            return Optional.empty();
        }

        //可布置作业的班级里面还有可布置分组的概念,所以需要将可布置班级里的非可布置分组过滤掉,然后再去查教材
        List<ExClazz> canBeAssignedClazzList = newHomeworkContentServiceClient.findTeacherClazzsCanBeAssignedHomework(teacher);
        List<Long> canBeAssignedGroupIds = new ArrayList<>();
        canBeAssignedClazzList.forEach(c -> c.getCurTeacherArrangeableGroups().forEach(m -> canBeAssignedGroupIds.add(m.getId())));

        Map<Long, List<GroupMapper>> groupMap = groupLoaderClient.loadClazzGroups(clazzIds);
        if (MapUtils.isEmpty(groupMap)) return Optional.empty();

        Set<Long> groupIds = new HashSet<>();
        groupMap.forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                v.forEach(m -> {
                    if (canBeAssignedGroupIds.contains(m.getId())) groupIds.add(m.getId());
                });
            }
        });

        if (CollectionUtils.isEmpty(groupIds)) return Optional.empty();

        return newClazzBookLoaderClient.loadGroupBookRefs(groupIds).subject(subject)
                .toList()
                .stream()
                .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                .findFirst();
    }
}
