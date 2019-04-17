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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.service.business.api.entity.GridFileInfo;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.storage.api.client.StorageLoaderClient;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.TeacherResourceDownloader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/resource")
public class TeacherResourceController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private StorageLoaderClient storageLoaderClient;
    @Inject private TeacherResourceDownloader teacherResourceDownloader;

    /**
     * 老师资源管理页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        Teacher teacher = currentTeacher();
        if (!teacher.hasValidSubject() || !teacher.hasValidKtwelve()) {
            return "redirect:/teacher/index.vpage";
        }
        return "/teacherv3/resource/index";
    }


    /**
     * 老师阅读管理页
     */
    @RequestMapping(value = "reading.vpage", method = RequestMethod.GET)
    public String reading(Model model) {
        //#21817阅读绘本制作工具 - 下线
        return "redirect:/teacher/index.vpage";
    }


    /**
     * 创建阅读首页
     */
    @RequestMapping(value = "reading/index.vpage", method = RequestMethod.GET)
    public String readingIndex(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "reading/index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingIndex(@RequestBody Map<String, Object> jsonMap) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "reading/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readingIndex() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "reading/view.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage readingView() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 获取书本的二级菜单
     *
     * @param request http request
     * @return json
     */
    @RequestMapping(value = "getbookinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String getBookInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String type = request.getParameter("type");
            String queryStr = request.getParameter("queryStr");
            List<Map<String, String>> bookInfo = businessHomeworkServiceClient.getBookInfo(type, queryStr);
            result.put("successful", true);
            result.put("bookInfo", bookInfo);
            return JsonUtils.toJson(result);
        } catch (Exception ex) {
            result.put("successful", true);
            return JsonUtils.toJson(result);
        }
    }

    /**
     * 获取地域信息二三级菜单
     *
     * @param request http request
     * @return json
     */
    @RequestMapping(value = "getregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String getRegion(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String pcode = request.getParameter("pcode");
            List<ExRegion> regions = raikouSystem.getRegionBuffer().loadChildRegions(Integer.parseInt(pcode));
            result.put("successful", true);
            result.put("regions", regions);
            return JsonUtils.toJson(result);
        } catch (Exception ex) {
            result.put("successful", true);
            return JsonUtils.toJson(result);
        }
    }

    /**
     * 我的资源
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String listResources() {
        Teacher teacher = currentTeacher();
        // TODO
        if (teacher.matchSubject(Subject.ENGLISH) || teacher.matchSubject(Subject.MATH)) {
            // 跳转到共享资源
            return "redirect:/teacher/resource/share.vpage";
        } else {
            return "/teacherv3/resource/resource/list";
        }
    }

    /**
     * 我的资源分页
     */
    @RequestMapping(value = "listhtml.vpage", method = RequestMethod.GET)
    public String pageableListResources(@RequestParam("currentPage") int currentPage, Model model) {
        Sort sort = new Sort(Sort.Direction.DESC, "createTimestamp");
        Pageable pageable = new PageRequest(currentPage - 1, 20, sort);
        Page<GridFileInfo> page = storageLoaderClient.getStorageLoader().loadGridFileInfoPage(currentUserId(), pageable);

        model.addAttribute("teacherResources", page);
        model.addAttribute("currentPage", currentPage);
        return "/teacherv3/resource/resource/listhtml";
    }

    /**
     * 1.我的资源。 1.2.1下载资源。
     */
    @RequestMapping(value = "download.vpage", method = RequestMethod.GET)
    public void downloadResource(@RequestParam("resourceId") String resourceId) {
        DownloadContent downloadContent = teacherResourceDownloader.downloadTeacherResource(resourceId);
        if (downloadContent != null) {
            try {
                getWebRequestContext().downloadOctetStreamFile(
                        downloadContent.getFilename(),
                        downloadContent.getContent());
            } catch (IOException ex) {
                logger.error("Error occurs when downloading resource '{}'", resourceId, ex);
            }
        }
    }


    /**
     * 老师资源管理页 -- 我的教材(UGC)
     */
    @RequestMapping(value = "mybook/list.vpage", method = RequestMethod.GET)
    public String getMyBookList(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 老师资源管理页 -- 我的教材(UGC) -- 上传页
     */
    @RequestMapping(value = "mybook/upload.vpage", method = RequestMethod.GET)
    public String uploadMyBook() {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 老师资源管理页 -- 共享资源
     */
    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    public String share(Model model) {
        Teacher teacher = currentTeacher();

        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(relTeacherIds);
        model.addAttribute("subjects", teachers.values().stream().map(Teacher::getSubject).collect(Collectors.toSet()));
        model.addAttribute("curSubject", StringUtils.cleanXSS(getRequestParameter("subject", teacher.getSubject().name())));
        return "/teacherv3/resource/share";
    }

    /**
     * 老师资源管理页 -- 资源平台
     */
    @RequestMapping(value = "platform_resource.vpage", method = RequestMethod.GET)
    public String platformresource() {
        return "/teacherv3/resource/platform_resource";
    }
}
