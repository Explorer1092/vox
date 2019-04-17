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

package com.voxlearning.utopia.agent.controller.oral;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.apply.ApplyService;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * oral manage
 */
@Controller
@RequestMapping("/oral")
@Slf4j
public class OralController extends AbstractAgentController {

    @Inject private ApplyService applyService;

    private Map<String, String> getSubjectsForNewExam() {
        Map<String, String> subjectMap = new LinkedHashMap<>();
        for (Subject subject : Subject.values()) {
            if (subject.getKey() <= Subject.JCHINESE.getKey() && subject != Subject.UNKNOWN) {
                if (subject.getKey() <= Subject.CHINESE.getKey()) {
                    subjectMap.put(ConversionUtils.toString(subject.getId()), "小学" + subject.getValue());
                } else {
                    subjectMap.put(ConversionUtils.toString(subject.getId()), subject.getValue());
                }
            }
        }
        return subjectMap;
    }

    /**
     * 口语管理
     * --- 相关资料下载
     */
    @RequestMapping(value = "manage/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "oral/index";
    }

    @RequestMapping(value = "manage/downloadtemplate.vpage", method = RequestMethod.POST)
    public void down(HttpServletRequest request, HttpServletResponse response) {
        String fileName = request.getParameter("fileName");
        String userUsedFileName = request.getParameter("userFileName");
        try {
            if (StringUtils.isBlank(fileName) || StringUtils.indexOf(fileName, "/") > 0) {
                return;
            }
            if (StringUtils.isBlank(userUsedFileName) || StringUtils.indexOf(userUsedFileName, "/") > 0) {
                return;
            }
            String contextPath = request.getRealPath("/");
            String filePath = contextPath + "/public/downloadtemplate/" + fileName;
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            String ext = StringUtils.substringAfterLast(fileName, ".");
            ext = StringUtils.defaultString(ext).trim().toLowerCase();
            String outputFileName = userUsedFileName + "." + ext;
            String contentType;
            try {
                contentType = SupportedFileType.valueOf(ext).getContentType();
            } catch (Exception ex) {
                contentType = "application/octet-stream";
            }

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                HttpRequestContextUtils.currentRequestContext().downloadFile(outputFileName, contentType, IOUtils.toByteArray(fileInputStream));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        } catch (Exception ex) {
            log.error("downloadtemplate Error", ex.getMessage());
        }
    }


    /**
     * 口语管理
     * ----口语测评列表
     */
    @RequestMapping(value = "manage/list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("subjects", getSubjectsForNewExam());
        return "oral/list";
    }

    /**
     * 口语管理
     * ----口语测评列表筛选
     */
    @RequestMapping(value = "manage/search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralSearch() {
        return MapMessage.successMessage()
                .add("dataList", new ArrayList<>())
                .add("iTotalRecords", 0)
                .add("iTotalDisplayRecords", 0);
    }

    /**
     * 口语管理
     * ----查看单个口语测评
     */
    @RequestMapping(value = "manage/oraldetail.vpage", method = RequestMethod.GET)
    public String viewOral(Model model) {
        return "oral/htmlchip/oraldetail";
    }

    /**
     * 口语管理
     * ----编辑口语测评
     */
    @RequestMapping(value = "manage/editoral.vpage", method = RequestMethod.GET)
    public String editOral(Model model) {
        return "oral/editoral";
    }

    /**
     * 口语管理
     * ----编辑口语测评POST
     */
    @RequestMapping(value = "manage/editoraltime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateOralTime(@RequestBody Map<String, Object> map) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 口语管理
     * ----删除口语测评
     */
    @RequestMapping(value = "manage/deleteoral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteOral() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 口语管理
     * ----添加口语测评
     */
    @RequestMapping(value = "manage/addoral.vpage", method = RequestMethod.GET)
    public String addOral(Model model) {
        model.addAttribute("nowDate", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
        model.addAttribute("subjects", getSubjectsForNewExam());

        Map<Integer, String> clazzLevelMap = new LinkedHashMap<>();
        for (ClazzLevel clazzLevel : ClazzLevel.values()) {
            if (clazzLevel.getLevel() <= ClazzLevel.NINTH_GRADE.getLevel()) {
                clazzLevelMap.put(clazzLevel.getLevel(), clazzLevel.getDescription());
            }
        }
        model.addAttribute("clazzLevelMap", JsonUtils.toJson(clazzLevelMap));

        return "oral/addoral";
    }

    /**
     * 口语管理
     * ----添加口语测评POST
     */
    @RequestMapping(value = "manage/saveoral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveOral(@RequestBody Map<String, Object> map) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "manage/uploadoralfile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadOrlFile(MultipartHttpServletRequest multipartRequest) {
        try {
            MultipartFile inputFile = multipartRequest.getFile("uploadfile");
            if (inputFile.isEmpty()) {
                return MapMessage.errorMessage("文件为空");
            }
            if (inputFile.getSize() > 1024 * 1024 * 10) {
                return MapMessage.errorMessage("文件大小不能超过10M");
            }
            // 获取文件类型
            String originalFileName = inputFile.getOriginalFilename();
            String ext = StringUtils.substringAfterLast(originalFileName, ".");
            ext = StringUtils.defaultString(ext).trim().toLowerCase();

            SupportedFileType fileType;
            try {
                fileType = SupportedFileType.valueOf(ext);
            } catch (Exception ex) {
                throw new RuntimeException("不支持此格式文件");
            }

            String fileId = RandomUtils.nextObjectId();
            String fileName = "oralApply-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            @Cleanup InputStream inStream = inputFile.getInputStream();
            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, inStream);
            return MapMessage.successMessage("上传成功").add("fileUrl", "gridfs/" + fileName);
        } catch (Exception ex) {
            log.error("上传失败,msg:{}", ex.getMessage(), ex);
            return MapMessage.errorMessage(StringUtils.formatMessage("上传失败,msg:{}", ex.getMessage()));
        }
    }

    /**
     * 口语管理
     * ----查询区域下学校
     */
    @RequestMapping(value = "manage/searchschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchSchool(@RequestBody Map<String, Object> map) {
        List<String> regions = (List<String>) map.get("regions");
        //如果不存在，默认为小学
        SchoolLevel schoolLevel = SchoolLevel.safeParse(ConversionUtils.toInt(map.get("schoolLevel"), 1), SchoolLevel.JUNIOR);
        List<Integer> regionSet = new LinkedList<>();
        for (String code : regions) {
            int regionCode = ConversionUtils.toInt(code);
            if (regionCode > 0) {
                regionSet.add(regionCode);
            }
        }
        if (CollectionUtils.isEmpty(regionSet)) {
            return MapMessage.successMessage().add("schools", new LinkedList<>());
        }
        Map<Integer, ExRegion> regionMap = applyService.loadRegions(regionSet);
        List<Integer> regionList = new LinkedList<>();
        for (ExRegion exRegion : regionMap.values()) {
            if (exRegion.fetchRegionType() == RegionType.COUNTY) {
                regionList.add(exRegion.getId());
            }
        }
        List<School> schoolList = applyService.searchSchool(regionList, schoolLevel);

        return MapMessage.successMessage().add("schools", schoolList);
    }
}
