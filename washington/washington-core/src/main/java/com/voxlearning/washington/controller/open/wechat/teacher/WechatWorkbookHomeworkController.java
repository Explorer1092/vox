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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 微信教辅作业，布置、检查教辅作业
 *
 * @author Jia HuanYin
 * @since 2015/9/6
 */
@Controller
@RequestMapping(value = "/open/wechat/workbookhomework")
@Slf4j
public class WechatWorkbookHomeworkController extends AbstractOpenController {

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    /**
     * 教辅作业可见性
     */
    @RequestMapping(value = "visible.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext visible(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 可布置教辅作业的班级
     */
    @RequestMapping(value = "clazzes.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzes(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 按年级、学期获取老师的教辅
     */
    @RequestMapping(value = "teacher_workbooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext teacherWorkbooks(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 按年级获取教辅
     */
    @RequestMapping(value = "grade_workbooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gradeWorkbooks(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 填充教辅信息
     */
    private List<Map<String, Object>> mapWorkbooks(Collection<XxWorkbook> workbooks) {
        if (workbooks == null) {
            return Collections.emptyList();
        }
        List<Long> bookIds = new LinkedList<>();
        for (XxWorkbook workbook : workbooks) {
            if (workbook.getBook() != null) {
                bookIds.add(workbook.getBook().getBookId());
            }
        }
        List<Map<String, Object>> mapWorkbooks = new LinkedList<>();
        Map<Long, Book> books = englishContentLoaderClient.loadEnglishBooks(bookIds);
        for (XxWorkbook workbook : workbooks) {
            Map<String, Object> mapWorkbook = new LinkedHashMap<>();
            mapWorkbook.put("id", workbook.getId());
            mapWorkbook.put("alias", workbook.getAlias());
            mapWorkbook.put("title", workbook.getTitle());
            mapWorkbook.put("bookPress", bookPress(workbook, books));
            mapWorkbooks.add(mapWorkbook);
        }
        return mapWorkbooks;
    }

    /**
     * 教辅出版社
     */
    private String bookPress(XxWorkbook workbook, Map<Long, Book> books) {
        XxWorkbookBook workbookBook = workbook.getBook();
        Book book = workbookBook == null ? null : books.get(workbookBook.getBookId());
        return book == null ? "" : book.getPress();
    }

    /**
     * 更换教辅
     */
    @RequestMapping(value = "change_workbooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext changeWorkbooks(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 教辅内容
     */
    @RequestMapping(value = "workbook_contents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext workbookContents(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    private static List<Map<String, Object>> buildWorkbookContents(Map<String, List<XxWorkbookContent>> workbookContentMap) {
        if (workbookContentMap == null || workbookContentMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> workbookContents = new ArrayList<>();
        for (String key : workbookContentMap.keySet()) {
            List<Map<String, Object>> contents = new ArrayList<>();
            for (XxWorkbookContent value : workbookContentMap.get(key)) {
                if (value.getListen() == null) {
                    continue;
                }
                Map<String, Object> content = new HashMap<>();
                content.put("contentId", value.getId());
                content.put("listen", value.getListen().getUrl());
                content.put("duration", value.getListen().getDuration());
                List<XxWorkbookCatalog> catalogs = value.getCatalogsType0();
                if (CollectionUtils.isNotEmpty(catalogs)) {
                    catalogs.remove(0);
                }
                String catalogNames = CollectionUtils.isEmpty(catalogs) ? "" :
                        StringUtils.join(catalogs.stream().map(XxWorkbookCatalog::getName).collect(Collectors.toList()), ",");
                content.put("catalogName", catalogNames);
                String contentNames = CollectionUtils.isEmpty(value.getCatalogsType1()) ? "" :
                        StringUtils.join(value.getCatalogsType1().stream().map(XxWorkbookCatalog::getName).collect(Collectors.toList()), ",");
                content.put("contentName", contentNames);
                String orders = CollectionUtils.isEmpty(value.getParts()) ? "" :
                        StringUtils.join(value.getParts().stream().map(XxWorkbookContentPart::getOrder).collect(Collectors.toList()), ",");
                content.put("orders", orders);
                contents.add(content);
            }
            if (CollectionUtils.isNotEmpty(contents)) {
                Map<String, Object> workbookContent = new HashMap<>();
                workbookContent.put("contents", contents);
                String[] unit = StringUtils.split(key, "|");
                String catalogId = "";
                String catalogName = "";
                if (unit.length >= 2) {
                    catalogId = unit[0];
                    catalogName = unit[1];
                }
                workbookContent.put("catalogId", catalogId);
                workbookContent.put("catalogName", catalogName);
                workbookContents.add(workbookContent);
            }
        }
        return workbookContents;
    }

    /**
     * 布置教辅作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext assign(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 教辅作业列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext list(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 检查教辅作业
     */
    @RequestMapping(value = "check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext check(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 删除教辅作业
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext delete(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 教辅作业报告
     */
    @RequestMapping(value = "report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext report(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 教辅作业历史
     */
    @RequestMapping(value = "history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext history(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    private boolean nonTeachingClazz(Long teacherId, Long clazzId) {
        return clazzId != null && !teacherLoaderClient.isTeachingClazz(teacherId, clazzId);
    }
}
