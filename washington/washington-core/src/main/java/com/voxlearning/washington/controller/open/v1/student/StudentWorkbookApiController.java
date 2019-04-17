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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.content.api.entity.UserWorkbookRef;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.REQ_PAGE_NUMBER;

/**
 * 教辅api
 * Created by Shuai Huan on 2015/8/24.
 */
@Controller
@RequestMapping(value = "/v1/student/workbook")
@Slf4j
public class StudentWorkbookApiController extends AbstractStudentApiController {

    private static final int pageSize = 10;

    /**
     * 获取我的教辅列表
     *
     * @return
     */
    @RequestMapping(value = "/get.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getWorkbookList() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();

        Map<String, Object> teacherRecommendedMap = getComposedRecommendedBooks(studentDetail);
        Collection<XxWorkbook> teacherWorkbookList = (Collection<XxWorkbook>) teacherRecommendedMap.get("teacherWorkbookList");

        List<Map<String, Object>> teacherBookList = new ArrayList<>();
        teacherWorkbookList.stream().forEach(w -> {
            Map<String, Object> workBookMap = new HashMap<>();
            workBookMap.put(RES_WORKBOOK_TITLE, w.getTitle());
            workBookMap.put(RES_WORKBOOK_ALIAS, w.getAlias());
            workBookMap.put(RES_WORKBOOK_ID, w.getId());
            workBookMap.put(RES_WORKBOOK_COVER, "");
//            workBookMap.put(RES_WORKBOOK_COVER, w.getCover().getMedium_url());
            teacherBookList.add(workBookMap);
        });
        resultMap.add(RES_TEACHER_WORKBOOK_LIST, teacherBookList);

        // 拿到学生自己选的教辅
        List<UserWorkbookRef> userWorkbookRefList = userWorkbookLoaderClient.loadUserWorkbooks(studentDetail.getId(), ENGLISH);
        Set<String> studentWorkbookIds = userWorkbookRefList.stream().map(UserWorkbookRef::getWorkbookId).collect(Collectors.toSet());
        List<XxWorkbook> workbooks = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooks(studentWorkbookIds);

        List<Map<String, Object>> studentBookList = new ArrayList<>();
        workbooks.stream().forEach(w -> {
            Map<String, Object> workBookMap = new HashMap<>();
            workBookMap.put(RES_WORKBOOK_TITLE, w.getTitle());
            workBookMap.put(RES_WORKBOOK_ALIAS, w.getAlias());
            workBookMap.put(RES_WORKBOOK_ID, w.getId());
//            workBookMap.put(RES_WORKBOOK_COVER, w.getCover().getMedium_url());
            workBookMap.put(RES_WORKBOOK_COVER, "");
            studentBookList.add(workBookMap);
        });

        resultMap.add(RES_STUDENT_WORKBOOK_LIST, studentBookList);
        resultMap.add(RES_WORKBOOK_RECOMMEND_FLAG, teacherRecommendedMap.get("teacherRecommended"));
        resultMap.add(RES_WORKBOOK_MORE_FLAG, teacherRecommendedMap.get("moreBooks"));
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }


    /**
     * 选择/设置我的教辅
     *
     * @return
     */
    @RequestMapping(value = "/set.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setWorkBook() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_WORKBOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String workBookId = getRequestString(REQ_WORKBOOK_ID);

        StudentDetail studentDetail = getCurrentStudentDetail();
        SchoolYear schoolYear = SchoolYear.newInstance();

        try {
            MapMessage mapMessage = atomicLockManager.wrapAtomic(contentServiceClient)
                    .keyPrefix("STUDENT_CHOOSE_WORKBOOK")
                    .keys(studentDetail.getId())
                    .proxy()
                    .addUserWorkbook(
                            studentDetail.getId(),
                            Arrays.asList(StringUtils.split(workBookId, ",")),
                            schoolYear.currentTerm(),
                            studentDetail.getClazzLevel(),
                            ENGLISH,
                            RoleType.ROLE_STUDENT);
            if (!mapMessage.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, mapMessage.getInfo());
                return resultMap;
            }
        } catch (DuplicatedOperationException ex) {
            resultMap.add(RES_RESULT, RES_RESULT_DUPLICATE_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        } catch (Exception ex) {
            logger.error("addUserWorkbook error.workBookId:{},studentId:{}", workBookId, studentDetail.getId(), ex);
            resultMap.add(RES_RESULT, RES_RESULT_DUPLICATE_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DUPLICATE_OPERATION);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 删除我的教辅
     *
     * @return
     */
    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteWorkBook() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_WORKBOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String workbookId = getRequestString(REQ_WORKBOOK_ID);
        Student student = getCurrentStudent();

        MapMessage mapMessage = contentServiceClient.deleteUserWorkbook(student.getId(), ENGLISH, Arrays.asList(StringUtils.split(workbookId, ",")));
        if (!mapMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 获取教辅目录
     *
     * @return
     */
    @RequestMapping(value = "/getcatalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getWorkbookCatalog() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_WORKBOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String workbookId = getRequestString(REQ_WORKBOOK_ID);

        List<XxWorkbookCatalog> workbookCatalogs = xxWorkbookContentLoaderClient.getRemoteReference().mobileXxWorkbookCatalogs(workbookId, Subject.ENGLISH.getId());


        if (CollectionUtils.isEmpty(workbookCatalogs)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_SUP_NOT_EXIST_MSG);
            return resultMap;
        }

        List<Map<String, Object>> contentList = new LinkedList<>();
        List<XxWorkbookCatalog> parentCatalogs = workbookCatalogs.stream().filter(e -> StringUtils.isBlank(e.getParent_id())).collect(Collectors.toList());

        Map<String, XxWorkbookCatalog> catalogMap = new LinkedHashMap<>();
        for (XxWorkbookCatalog catalog : parentCatalogs) {
            catalogMap.put(catalog.getId(), catalog);
        }

        parentCatalogs = catalogMap.values().stream().collect(Collectors.toList());


        for (XxWorkbookCatalog catalog : parentCatalogs) {
            Map<String, Object> unitMap = new LinkedHashMap<>();
            unitMap.put(RES_UNIT_CNAME, catalog.getName());
            unitMap.put(StudentApiConstants.RES_UNIT_ID, catalog.getId());
            List<Map<String, Object>> lessonList = new LinkedList<>();
            workbookCatalogs.stream().
                    filter(e -> StringUtils.equals(catalog.getId(), e.getParent_id()))
                    .collect(Collectors.toList()).stream()
                    .forEach(w -> {
                        Map<String, Object> lessonMap = new HashMap<>();
                        lessonMap.put(RES_LESSON_CNAME, w.getName());
                        lessonMap.put(StudentApiConstants.RES_LESSON_ID, w.getId());
                        lessonList.add(lessonMap);
                    });
            unitMap.put(RES_LESSON_LIST, lessonList);
            contentList.add(unitMap);
        }

        resultMap.add(RES_CONTENT_LIST, contentList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 获取内容目录
     *
     * @return
     */
    @RequestMapping(value = "/getcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getWorkbookContent() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_WORKBOOK_ID, REQ_WORKBOOK_CATALOG_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String workBookId = getRequestString(REQ_WORKBOOK_ID);
        String catalogId = getRequestString(REQ_WORKBOOK_CATALOG_ID);

        List<XxWorkbookContent> contentResult = new LinkedList<>();
        List<XxWorkbookContent> workbookContents = xxWorkbookContentLoaderClient.getRemoteReference().mobileXxWorkbookContents(workBookId, Subject.ENGLISH.getId());
        for (XxWorkbookContent content : workbookContents) {
            List<XxWorkbookCatalog> catalogsType0List = content.getCatalogsType0();
            if (CollectionUtils.isEmpty(catalogsType0List)) {
                continue;
            }
            for (XxWorkbookCatalog catalog : catalogsType0List) {
                if (StringUtils.equals(catalog.getId(), catalogId)) {
                    contentResult.add(content);
                    break;
                }
            }
        }


        XxWorkbook workbook = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooksAsMap(Collections.singletonList(workBookId)).get(workBookId);

        List<Map<String, Object>> contentList = new LinkedList<>();
        for (XxWorkbookContent content : contentResult) {
            List<XxWorkbookCatalog> catalogType1 = content.getCatalogsType1();
            if (content.getListen() == null) {
                continue;
            }
            Map<String, Object> contentMap = new HashMap<>();
            List<String> workBookCatalogList = content.getCatalogsType0().stream().map(XxWorkbookCatalog::getName).collect(Collectors.toList());
            contentMap.put(RES_WORKBOOK_CATALOG, workBookCatalogList);
            List<String> contentCatalogList = new ArrayList<>();
            if (catalogType1 != null) {
                contentCatalogList = catalogType1.stream().map(XxWorkbookCatalog::getName).collect(Collectors.toList());
            }

            List<XxWorkbookContentPart> parts = content.getParts();
            String pageContent = "";
            if (CollectionUtils.isEmpty(parts)) {
                contentMap.put(RES_CONTENT_START_PAGE, 0);
            } else {
                List<String> orders = new LinkedList<>();
                for (XxWorkbookContentPart part : parts) {
                    orders.add(part.getOrder());
                }
                List<String> pages = parts.stream().filter(e -> StringUtils.isNotBlank(e.getPage())).map(XxWorkbookContentPart::getPage).collect(Collectors.toList()).stream().sorted().collect(Collectors.toList());
                contentMap.put(RES_CONTENT_START_PAGE, pages.get(0));
                pageContent = StringUtils.join(orders, ",");
                pageContent = "第" + pageContent + "题";
            }
            // 如果内容目录为空，就把题号弄进去
            if (CollectionUtils.isEmpty(contentCatalogList)) {
                contentCatalogList = Collections.singletonList(pageContent);
            }
            contentMap.put(RES_CONTENT_CATALOG, contentCatalogList);
            contentMap.put(RES_CONTENT_WAVE_URI, content.getListen().getUrl());
            contentMap.put(RES_CONTENT_WAVE_DURATION, content.getListen().getDuration());
            contentMap.put(RES_WORKBOOK_TITLE, workbook.getTitle());
            contentList.add(contentMap);
        }

        resultMap.add(RES_CONTENT_LIST, contentList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    /**
     * 获取所有教辅（学生换教辅用）
     * 需要过滤
     * 1.用户已有的教辅
     * 2.老师已经推荐的教辅
     * 3.不是学生本地区的教辅
     *
     * @return
     */
    @RequestMapping(value = "/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAllWorkbookList() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_PAGE_NUMBER, "页码");
            validateRequest(REQ_PAGE_NUMBER);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        SchoolYear schoolYear = SchoolYear.newInstance();
        StudentDetail detail = getCurrentStudentDetail();
        // 学生区代码为空，则返回空列表
        if (detail.getStudentSchoolRegionCode() == null) {
            resultMap.add(RES_WORKBOOK_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }
//        List<XxWorkbook> workbooks = xxWorkbookLoaderClient.loadXxWorkbooksByTermClass(
//                Collections.singletonList(schoolYear.currentTerm().getKey()),
//                Collections.singletonList(detail.getClazzLevel().getLevel()),
//                ENGLISH.getId());

        // 拿到所有本地区本学期本年级的所有教辅
        WorkbookConfig workbookConfig = new WorkbookConfig();
        workbookConfig.setSubject_id(ENGLISH.getId());
        workbookConfig.setTerm_type(schoolYear.currentTerm().getKey());
        workbookConfig.setClass_level(detail.getClazzLevelAsInteger());
        workbookConfig.setProvince_id(detail.getRootRegionCode());
        workbookConfig.setCity_id(detail.getCityCode());
        workbookConfig.setRegion_id(detail.getStudentSchoolRegionCode());
        List<XxWorkbook> workbooks = new LinkedList<>(workbookConfigLoaderClient.loadRecWorkbookList(workbookConfig).values());

        // 拿到学生已经选过的教辅
        List<UserWorkbookRef> refs = userWorkbookLoaderClient.loadUserWorkbooks(detail.getId(), ENGLISH);
        Set<String> workBookSet = refs.stream().map(UserWorkbookRef::getWorkbookId).collect(Collectors.toSet());

        // 拿到地区推荐或者老师推荐的教辅
        Map<String, Object> teacherRecommendedMap = getComposedRecommendedBooks(detail);
        Collection<XxWorkbook> teacherWorkbookList = (Collection<XxWorkbook>) teacherRecommendedMap.get("teacherWorkbookList");
        Set<String> teacherWorkBookSet = teacherWorkbookList.stream().map(XxWorkbook::getId).collect(Collectors.toSet());

        // 过滤掉以上两个集合
        for (Iterator<XxWorkbook> iterator = workbooks.iterator(); iterator.hasNext(); ) {
            XxWorkbook xxWorkbook = iterator.next();
            if (workBookSet.contains(xxWorkbook.getId()) || teacherWorkBookSet.contains(xxWorkbook.getId())) {
                iterator.remove();
            }
        }

        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER);

        int total = workbooks.size();
        int begin = (currentPage - 1) * pageSize;
        if (begin > total) {
            resultMap.add(RES_WORKBOOK_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }
        int end = total > currentPage * pageSize ? currentPage * pageSize : total;

        workbooks = workbooks.subList(begin, end);

        List<Map<String, Object>> workBookList = new ArrayList<>();
        workbooks.stream().forEach(w -> {
            Map<String, Object> workBookMap = new HashMap<>();
            workBookMap.put(RES_WORKBOOK_TITLE, w.getTitle());
            workBookMap.put(RES_WORKBOOK_ALIAS, w.getAlias());
            workBookMap.put(RES_WORKBOOK_ID, w.getId());
//            workBookMap.put(RES_WORKBOOK_COVER, w.getCover().getMedium_url());
            workBookMap.put(RES_WORKBOOK_COVER, "");
            workBookList.add(workBookMap);
        });
        resultMap.add(RES_WORKBOOK_LIST, workBookList);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    // 获取各种数据
    // 1.老师或者地区推荐的教辅列表
    // 2.老师或者地区推荐的标志
    // 3.学生能否选择添加更多
    private Map<String, Object> getComposedRecommendedBooks(StudentDetail detail) {
        SchoolYear schoolYear = SchoolYear.newInstance();
        List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachers(detail.getId());
        clazzTeachers = clazzTeachers.stream().filter(clazzTeacher -> clazzTeacher.getTeacher().getSubject() == Subject.ENGLISH).collect(Collectors.toList());
        Set<String> teacherWorkbookIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(clazzTeachers)) {
            Teacher englishTeacher = clazzTeachers.get(0).getTeacher();
            List<UserWorkbookRef> workbookRefs = userWorkbookLoaderClient.loadUserWorkbooksByTermClazzLevel(englishTeacher.getId(), schoolYear.currentTerm(), detail.getClazzLevel(), englishTeacher.getSubject());
            teacherWorkbookIds = workbookRefs.stream().map(UserWorkbookRef::getWorkbookId).collect(Collectors.toSet());
        }

        Collection<XxWorkbook> teacherWorkbookList;
        boolean teacherRecommended = false;
        if (CollectionUtils.isEmpty(teacherWorkbookIds)) {
            // 地区推荐的教辅
            WorkbookConfig workbookConfig = new WorkbookConfig();
            workbookConfig.setSubject_id(ENGLISH.getId());
            workbookConfig.setTerm_type(schoolYear.currentTerm().getKey());
            workbookConfig.setClass_level(detail.getClazzLevelAsInteger());
            workbookConfig.setProvince_id(detail.getRootRegionCode());
            workbookConfig.setCity_id(detail.getCityCode());
            workbookConfig.setRegion_id(detail.getStudentSchoolRegionCode());
            teacherWorkbookList = workbookConfigLoaderClient.loadRecWorkbookList(workbookConfig).values();
        } else {
            teacherWorkbookList = xxWorkbookLoaderClient.getRemoteReference().loadXxWorkbooks(teacherWorkbookIds);
            teacherRecommended = true;
        }

        // 如果是老师推荐的，则比较老师推荐的数量跟当地教辅的数量差值
        // 是否还有更多的书供学生选择，体现在app里是否有【选择更多】的菜单
        // teacherWorkbookList：所有老师推荐或者地区推荐的教辅列表
        // teacherWorkbookIds:老师推荐的教辅列表
        // studentWorkbookRefs:学生已经选过的教辅列表
        List<UserWorkbookRef> studentWorkbookRefs = userWorkbookLoaderClient.loadUserWorkbooks(detail.getId(), ENGLISH);
        boolean moreBooks = (teacherWorkbookList.size() - teacherWorkbookIds.size() - studentWorkbookRefs.size()) > 0;
        Map<String, Object> result = new HashMap<>();
        result.put("teacherWorkbookList", teacherWorkbookList);
        result.put("teacherRecommended", teacherRecommended);
        result.put("moreBooks", moreBooks);
        return result;
    }
}
