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

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.system.RuntimeMode;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.MiscLoaderClient;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractTeacherWebController;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qianlong.yang
 * @version 0.1
 * @since 2016/4/29
 */
@Controller
@RequestMapping(value = "/teacher/activity")
public class TeacherActivityController extends AbstractTeacherWebController {
    @Inject private MiscLoaderClient miscLoaderClient;
    // 教材相关
    @Inject protected EnglishContentLoaderClient englishContentLoaderClient;
    @Inject protected ChineseContentLoaderClient chineseContentLoaderClient;


    @RequestMapping(value = "wxconfig.vapge", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage demo() {
        // 获取JS API 需要的参数MAP
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.PARENT));
        Map<String, Object> jsApiMap = new TreeMap<>();
        jsApiMap.put("signature", wxConfig.sha1Sign());
        jsApiMap.put("appId", ProductConfig.get(WechatType.PARENT.getAppId()));
        jsApiMap.put("noncestr", wxConfig.getNonce());
        jsApiMap.put("timestamp", wxConfig.getTimestamp());

        return MapMessage.successMessage().add("wxConfig", jsApiMap);
    }

    // 东莞521教育大会现场活动支持
    @RequestMapping(value = "dgmeeting.vpage", method = RequestMethod.GET)
    public String dgMeeting(Model model) throws Exception {
        return "redirect:/index.vpage";
    }

    // 新学年推荐换书
    @RequestMapping(value = "recommendbook/index.vpage", method = RequestMethod.GET)
    public String recommendBook(Model model) {
        Long teacherId = getRequestContext().getUserId();
        // 根据学科年纪区域进行换书操作
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        model.addAttribute("currentTeacherDetail", teacherDetail);
        model.addAttribute("cdnBase", ProductConfig.getCdnDomainAvatar());
        return "teacher/activity/recommendbook";
    }

    // 新学年推荐换书-微信专版
    @RequestMapping(value = "recommendbook/wechatindex.vpage", method = RequestMethod.GET)
    public String recommendBookForWechat(Model model) {
        Long teacherId = getRequestContext().getUserId();
        // 根据学科年纪区域进行换书操作
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        model.addAttribute("currentTeacherDetail", teacherDetail);
        model.addAttribute("cdnBase", ProductConfig.getCdnDomainAvatar());
        // 获取JS API 需要的参数MAP
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(WechatType.TEACHER));
        Map<String, Object> jsApiMap = new TreeMap<>();
        jsApiMap.put("signature", wxConfig.sha1Sign());
        jsApiMap.put("appId", ProductConfig.get(WechatType.TEACHER.getAppId()));
        jsApiMap.put("noncestr", wxConfig.getNonce());
        jsApiMap.put("timestamp", wxConfig.getTimestamp());

        model.addAttribute("ret", jsApiMap);
        return "teacher/activity/recommendbookwechat";
    }

    @RequestMapping(value = "/uploadwechatimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImg() {
        long teacherId = getRequestContext().getUserId();
        String mediaId = getRequestString("mediaId");
        String prefix = "recommendbookcover-" + teacherId + "-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
        MapMessage mapMessage = new MapMessage();
        try {
            String accessToken = tokenHelper.getAccessToken(WechatType.PARENT);
            if (StringUtils.isBlank(accessToken)) {
                return MapMessage.errorMessage("上传失败，请重试");
            }

            byte[] imageArray = wechatPictureUploader.downLoadMediaFromWechat(accessToken, mediaId);
            String filename = upload(prefix, "xx.jpeg", imageArray);
            mapMessage.add("url", combineImgUrl(filename));
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("暑期预选教材上传封面图片异常： " + ex.getMessage());
        }
        return mapMessage;
    }

    private List<Map<String, Object>> newBookPaintedSkin(List<NewBookProfile> books) {
        List<Map<String, Object>> bookMaps = new ArrayList<>();
        for (NewBookProfile book : books) {
            NewBookCatalog bookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(book.getSeriesId());
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(book.getSubjectId()), bookCatalog.getName());
            Map<String, Object> bookMap = new HashMap<>();
            bookMap.put("id", book.getId());
            bookMap.put("cname", book.getName());
            bookMap.put("ename", book.getName());
            bookMap.put("bookType", book.getBookType());
            bookMap.put("status", book.getStatus());
            bookMap.put("imgUrl", book.getImgUrl());
            bookMap.put("latestVersion", book.getLatestVersion());
            if (bookPress != null) {
                bookMap.put("viewContent", bookPress.getViewContent());
                bookMap.put("color", bookPress.getColor());
            }
            bookMaps.add(bookMap);
            // book.setImgUrl(StringUtils.replace(book.getImgUrl(), "catalog", "catalog_new"));
        }
        return bookMaps;
    }

    @RequestMapping(value = "recommendbook/sortbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sortbook() {
        long teacherId = getRequestContext().getUserId();
        String code = getRequestString("code");
        String level = getRequestString("level");
        String filterPress = getRequestString("filterPress");
        int regionCode = ConversionUtils.toInt(code);
        // 获取下一个学年可以用的教材
        ClazzLevel clazzLevel = ClazzLevel.of(ConversionUtils.toInt(level) + 1);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return MapMessage.errorMessage("您还没有设置学科及班级，请完成设置后再登录！");
        }
        List<NewBookProfile> newBookProfiles;
        if (!subject.equals(Subject.MATH)) {
            newBookProfiles = newContentLoaderClient.loadBooksByClassLevelWithSortByUpdateTime(subject, clazzLevel, regionCode)
                    .stream()
                    .filter(book -> Objects.equals(book.getTermType(), 1))
                    .collect(Collectors.toList());
        } else {
            newBookProfiles = newContentLoaderClient.loadBooksByClassLevelWithSortByUpdateTimeIncludeOffline(subject, clazzLevel, regionCode)
                    .stream()
                    .filter(book -> Objects.equals(book.getTermType(), 1))
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(newBookProfiles)) {
            List<Map<String, Object>> books = newBookPaintedSkin(newBookProfiles);
            MapMessage message = MapMessage.successMessage();
            message.add("total", books.size());
            message.add("rows", books);
            return message;
        } else {
            return MapMessage.successMessage().add("total", 0).add("rows", Collections.EMPTY_LIST);
        }
    }

    // 获取推荐的图书
    @RequestMapping(value = "recommendbook/loadbooks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadBooks() {
        // 给老师推荐教材，这里设计怎么推荐最适合的教材
        Long teacherId = getRequestContext().getUserId();
        // 根据学科年纪区域进行换书操作
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
        List<Map<String, Object>> clazzBooks = new ArrayList<>();
        for (Clazz clazz : clazzs) {
            // 根据老师的地区、年级和学期给新班级默认一本教材
            // 此处是为寒假推荐下学年上学期开学的教材
            if (clazz.getClazzLevel().getLevel() >= 6) {
                // 过滤掉六年级的班级
                continue;
            }
            Map<String, Object> clazzBookMap = new HashMap<>();
            clazzBookMap.put("clazzId", clazz.getId());
            clazzBookMap.put("clazzName", clazz.formalizeClazzName());
            clazzBookMap.put("clazzLevel", clazz.getClassLevel());
            clazzBookMap.put("recommendBookId", "bookd");
            clazzBooks.add(clazzBookMap);
        }
        return MapMessage.successMessage().set("clazzBooks", clazzBooks);
    }

    // 搜索图书，这个接口用不找了，现在主站是通过前端来搜索的
    @RequestMapping(value = "recommendbook/searchbooks.vpage")
    @ResponseBody
    public MapMessage searchBooks() {
        return MapMessage.successMessage();
    }


    // 选择图书
    @RequestMapping(value = "recommendbook/choosebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chooseBook() {
        long teacherId = getRequestContext().getUserId();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        String clazzId = getRequestString("clazzId");
        String bookId = getRequestString("bookId");
        String bookCname = getRequestString("bookCname");
        String bookEname = getRequestString("bookEname");
        String press = getRequestString("press");
        Map<String, String> map = new HashMap<>();
        String env = RuntimeMode.getCurrentStage();
        map.put("env", env);
        map.put("module", "recommend_book");
        map.put("op", "recommend_book_choose");
        map.put("clazzId", clazzId);
        map.put("bookId", bookId);
        map.put("bookCname", bookCname);
        map.put("bookEname", bookEname);
        map.put("press", press);
        map.put("subject", teacherDetail.getSubject().getValue());
        log(map);
        return MapMessage.successMessage();
    }

    //用GridFs存储
    private String upload(String prefix, String filename, InputStream inStream) {
        String ext = StringUtils.substringAfterLast(filename, ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();

        SupportedFileType fileType;
        try {
            fileType = SupportedFileType.valueOf(ext);
        } catch (Exception ex) {
            logger.warn("Unsupported file type: {}", ext);
            throw new RuntimeException("不支持此格式文件");
        }

        try {
            byte[] content = IOUtils.toByteArray(inStream);
            String fileId = RandomUtils.nextObjectId();
            String fileName = prefix + "-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, bais);
            return fileName;
        } catch (Exception ex) {
            logger.warn("Upload product image: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    //用GridFs存储
    private String upload(String prefix, String filename, byte[] bytes) {
        String ext = StringUtils.substringAfterLast(filename, ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();

        SupportedFileType fileType;
        try {
            fileType = SupportedFileType.valueOf(ext);
        } catch (Exception ex) {
            logger.warn("Unsupported file type: {}", ext);
            throw new RuntimeException("不支持此格式文件");
        }

        try {
            String fileId = RandomUtils.nextObjectId();
            String fileName = prefix + "-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, bais);
            return fileName;
        } catch (Exception ex) {
            logger.warn("Upload product image: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    // 上传封面
    @RequestMapping(value = "recommendbook/uploadcover.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadCover(HttpServletRequest request) {
        logger.debug("debug");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        MultipartFile imgFile = multipartRequest.getFile("upfile");
        if (imgFile.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = imgFile.getOriginalFilename();

        String prefix = "recommendbookcover-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
        MapMessage mapMessage = new MapMessage();
        try {
            @Cleanup InputStream inStream = imgFile.getInputStream();
            String filename = upload(prefix, originalFileName, inStream);
            mapMessage.add("url", combineImgUrl(filename));
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("暑期预选教材上传封面图片异常： " + ex.getMessage());
        }
        return mapMessage;
    }

    //生成预览url
    private String combineImgUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        //使用cdn地址而不是主站地址
        String prePath = "https://" + ProductConfig.getCdnDomainAvatar();
        return prePath + "/gridfs/" + url;
    }

    // 上传教材
    @RequestMapping(value = "recommendbook/uploadbook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBook() {
        // 教材名称
        long teacherId = getRequestContext().getUserId();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        String clazzId = getRequestString("clazzId");
        String bookName = getRequestString("bookName");
        // 封面地址
        String cover = getRequestString("cover");
        Map<String, String> map = new HashMap<>();
        String env = RuntimeMode.getCurrentStage();
        map.put("env", env);
        map.put("module", "recommend_book");
        map.put("op", "recommend_book_upload");
        map.put("clazzId", clazzId);
        map.put("bookName", bookName);
        map.put("cover", cover);
        map.put("subject", teacherDetail.getSubject().getValue());
        log(map);
        return MapMessage.successMessage();
    }

    /* 2016教师节活动 wechat and app 活动页面 */
    @RequestMapping(value = "teachersday2016.vpage", method = RequestMethod.GET)
    public String teachersDay2016(Model model) {
//        Long userId = getRequestContext().getUserId();
//        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
//        List<Map<String, Object>> blessList = teachersDayBlessActivityService.loadTeacherBlessList(teacher.getId());
//        boolean hasBless = true;
//        if (CollectionUtils.isEmpty(blessList)) {
//            hasBless = false;
//            // 获取使用天数
//            long dayCount = com.voxlearning.alps.calendar.DateUtils.dayDiff(new Date(), teacher.getCreateTime());
//            model.addAttribute("dayCount", dayCount);
//        }
//
//        model.addAttribute("hasBless", hasBless);
//        model.addAttribute("blessList", blessList);
//        model.addAttribute("teacherName", teacher.fetchRealname());
//        return "teacher/activity/teachersday2016";
        return infoPage(WechatInfoCode.TEACHER_ACTIVITY_DOWN, model);
    }
}
