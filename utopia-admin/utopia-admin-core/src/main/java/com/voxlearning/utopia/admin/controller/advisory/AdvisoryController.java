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

package com.voxlearning.utopia.admin.controller.advisory;

import com.aliyuncs.exceptions.ClientException;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.dao.CrmJxtNewsDao;
import com.voxlearning.utopia.admin.data.SnapshotSubmitResult;
import com.voxlearning.utopia.admin.util.AdminAcsManagerUtils;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.ParentNewsService;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.misc.*;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarNotifyServiceClient;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedJxtNewsTagList;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xinqiang.wang
 * @since 2016/6/15
 */

@Controller
@RequestMapping("/advisory")
@Slf4j
public class AdvisoryController extends AbstractAdminSystemController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CrmJxtNewsDao crmJxtNewsDao;

    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private CrmImageUploader crmImageUploader;
    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private JxtNewsServiceClient jxtNewsServiceClient;
    @Inject
    private MizarNotifyServiceClient mizarNotifyServiceClient;
    @Inject
    private MizarUserLoaderClient mizarUserLoaderClient;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;


    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;
    @ImportService(interfaceClass = CRMVendorService.class)
    private CRMVendorService crmVendorService;
    @ImportService(interfaceClass = ParentNewsService.class)
    private ParentNewsService parentNewsService;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient fsDefault;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryIndex() {

        return "advisory/index";
    }

    /**
     * 查看抓取的文章列表页面
     */
    @RequestMapping(value = "/viewrawarticles.vpage", method = RequestMethod.GET)
    public String viewRawArticles(Model model) {
        List<Map<String, Object>> tagMapList = getTopTwoLevelTags();
        model.addAttribute("tags", JsonUtils.toJson(tagMapList));
        String mainSiteBaseUrl = ProductConfig.getMainSiteBaseUrl();
        model.addAttribute("mainSiteBaseUrl", mainSiteBaseUrl);
        return "advisory/viewrawarticles";
    }

    /**
     * load抓取的文章列表
     */
    @RequestMapping(value = "/loadrawarticles.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadRawArticles() {
        int page = getRequestInt("currentPage");
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        String firstLevelTagId = getRequestString("firstLevelTagId");
        String secondLevelTagId = getRequestString("secondLevelTagId");
        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");
        boolean disabled = getRequestBool("disabled");
        Date startDatetime = StringUtils.isNotEmpty(startTime) ? DateUtils.stringToDate(startTime) : null;
        if (startDatetime != null) {
            startDatetime = DateUtils.addHours(startDatetime, 8);
        }
        Date endDatetime = StringUtils.isNotEmpty(endTime) ? DateUtils.stringToDate(endTime) : null;
        if (endDatetime != null) {
            endDatetime = DateUtils.addHours(endDatetime, 8);
        }
        String publishers_str = getRequestString("publishers");
        String[] publishers;
        if (StringUtils.isNotEmpty(publishers_str)) {
            publishers = publishers_str.split(",");
        } else {
            publishers = new String[0];
        }
        boolean edited = getRequestBool("edited");
        String title = getRequestString("title");
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "publish_datetime");
        Page<ZyParentNewsRawArticle> zyParentNewsRawArticleList = parentNewsService.loadNextPageRawArticles(pageable, publishers, edited, startDatetime, endDatetime, firstLevelTagId, secondLevelTagId, disabled, title);
        return MapMessage.successMessage().add("articles", zyParentNewsRawArticleList);
    }


    /**
     * load article列表
     */
    @RequestMapping(value = "/loadarticles.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadArticles() {
        int page = getRequestInt("currentPage");
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        boolean pushed = getRequestBool("pushed");
        String title = getRequestString("title");
        String editor = getRequestString("editor");
        int category = getRequestInt("category");
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "update_datetime");
        Page<ZyParentNewsArticle> zyParentNewsArticleList = parentNewsService.loadNextPageArticles(pageable, pushed, title, editor, category);
        return MapMessage.successMessage().add("articles", zyParentNewsArticleList);
    }

    /**
     * 获取抓取的文章内容根据id
     */
    @RequestMapping(value = "/loadrawarticlebyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadRawArticleById() {
        String id = getRequestString("id");
        ZyParentNewsRawArticle zyParentNewsRawArticle = parentNewsService.loadRawArticleById(id);
        return MapMessage.successMessage().add("article", zyParentNewsRawArticle);
    }

    /**
     * 根据id删除
     */
    @RequestMapping(value = "/deleterawarticlebyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteRawArticleById() {
        String id = getRequestString("id");
        boolean success = parentNewsService.deleteRawArticleById(id);
        if (success) {
            return MapMessage.successMessage("删除成功");
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }

    /**
     * 根据ids删除
     */
    @RequestMapping(value = "/deleterawarticlebyids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteRawArticleByIds() {
        String idsStr = getRequestString("ids");
        List<String> ids = Arrays.asList(idsStr.split(","));
        boolean success = parentNewsService.deleteRawArticleByIds(ids);
        if (success) {
            return MapMessage.successMessage("批量删除成功");
        } else {
            return MapMessage.errorMessage("批量删除失败");
        }
    }

    /**
     * batch online raw article
     */
    @RequestMapping(value = "/batchonlinerawarticles.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchOnlineRawArticles() {
        String styleType = getRequestString("type");
        String idsStr = getRequestString("ids");
        List<String> ids = Arrays.asList(idsStr.split(","));
        int count = 0;
        try {
            count = batchOnlineRawArticles(ids, styleType);
        } catch (Exception e) {
            return MapMessage.errorMessage("批量上线失败！");
        }
        return MapMessage.successMessage("批量完成，请去线上验证").add("count", count);
    }


    /**
     * 根据id获取article内容
     */
    @RequestMapping(value = "/loadarticlebyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadArticleById() {
        String id = getRequestString("id");
        ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(id);
        return MapMessage.successMessage().add("article", zyParentNewsArticle);
    }

    /**
     * 新建/编辑 内容
     */
    @RequestMapping(value = "contentedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryContentEdit(Model model) {
        String rid = getRequestString("rid");
        model.addAttribute("rawId", rid);
        return "advisory/contentedit";
    }

    /**
     * 更新已编辑过的内容
     */
    @RequestMapping(value = "contentupdateedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String contentUpdateEdit(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "advisory/contentupdateedit";
    }

    /**
     * ueditor controller
     */
    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();
        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();

                String prefix = "parentnews-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
                try {
                    @Cleanup InputStream inStream = imgFile.getInputStream();
                    String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
                    mapMessage.add("url", combineImgUrl(filename));
                    mapMessage.add("title", filename);
                    mapMessage.add("state", "SUCCESS");
                    mapMessage.add("original", originalFileName);
                    mapMessage.setSuccess(true);
                } catch (Exception ex) {
                    mapMessage.setSuccess(false);
                    log.error("上传咨询图片异常： " + ex.getMessage());
                }
        }
        return mapMessage;
    }

    /**
     * 编辑时上传图片
     */
    @RequestMapping(value = "edituploadimage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage editUploadImage(MultipartFile imgFile) {
        //上传图片
        //返回预览的完整路径和图片的相对路径
        MapMessage mapMessage = new MapMessage();
        if (imgFile.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }
        String originalFileName = imgFile.getOriginalFilename();

        String prefix = "parentnews-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
        try {
            @Cleanup InputStream inStream = imgFile.getInputStream();
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);
            mapMessage.add("url", combineImgUrl(filename));
            mapMessage.add("fileName", filename);
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            log.error("上传咨询图片异常： " + ex.getMessage());
        }
        return mapMessage;
    }

    /**
     * 保存内容
     */
    @RequestMapping(value = "newarticle.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage newArticle() {
        // 将编辑好的html存入mongo,返回新生成的id
        String content = getRequestString("content");
        int category = getRequestInt("category");
        Long wordsCount = getRequestLong("words_count");
        String title = getRequestString("title");
        String remark = getRequestString("remark");
        String digest = getRequestString("digest");
        String publisher = getRequestString("publisher");
        String sourceUrl = getRequestString("source_url");
        String rid = getRequestString("rid");
        String content_by_album_news = getRequestString("content_by_album_news");
//        String video_url = getRequestString("video_url");
        // 这里需要把推荐的头图和tags做处理后，放入edited_article中
        ZyParentNewsRawArticle parentNewsRawArticle = parentNewsService.loadRawArticleById(rid);
        List<String> recommendHeadFigures = new ArrayList<>();
        List<Long> tagIds = new ArrayList<>();
        if (parentNewsRawArticle != null) {
            List<String> headFigures = parentNewsRawArticle.getHead_figures();
            if (headFigures != null && headFigures.size() > 0) {
                // 现在把那本默认是一张小图模式，以后有需求再该这块
                recommendHeadFigures.addAll(headFigures);
            }
            List<Map<String, Object>> sourceTags = parentNewsRawArticle.getTags();
            if (sourceTags != null) {
                // todo:这块待确认，龙龙或者大数据会否把弄了，要不然我就方了
                // --将大数据给的tags映射到运营人工构建的标签树上面去
                // trick，这里简单存入一个标签255
                tagIds.addAll(sourceTags.stream().map(sourceTag -> SafeConverter.toLong(sourceTag.get("id"))).collect(Collectors.toList()));
            }
        }
        String editor = getCurrentAdminUser().getAdminUserName();

        // String tags = getRequestString("tags");备用
        addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "保存家长咨询内容成功");
        ZyParentNewsArticle zyParentNewsArticle = new ZyParentNewsArticle();
        zyParentNewsArticle.setContent(content);
        zyParentNewsArticle.setWords_count(wordsCount);
        zyParentNewsArticle.setTitle(title);
        zyParentNewsArticle.setRemark(remark);
        zyParentNewsArticle.setDigest(digest);
        zyParentNewsArticle.setPublisher(publisher);
        zyParentNewsArticle.setSource_url(sourceUrl);
        zyParentNewsArticle.setEditor(editor);
        zyParentNewsArticle.setPushed(false);
        zyParentNewsArticle.setRaw_article_id(rid);
        zyParentNewsArticle.setDisabled(false);
        zyParentNewsArticle.setCategory(category);
        zyParentNewsArticle.setRecommend_head_figures(recommendHeadFigures);
        zyParentNewsArticle.setTagIds(tagIds);
        zyParentNewsArticle.setContent_by_album_news(content_by_album_news);
//        zyParentNewsArticle.setVideo_url(video_url);
        MapMessage mapMessage = parentNewsService.newArticle(zyParentNewsArticle);
        String articleId = (String) mapMessage.get("id");
        // 在原文中记录下编辑后的文章id
        if (StringUtils.isNotEmpty(rid)) {
            parentNewsService.setEditedFlag(rid, articleId);
        }
        return mapMessage;
    }

    /**
     * 更新编辑内容
     */
    @RequestMapping(value = "updatearticle.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateArticle() {
        // 将编辑好的html存入mongo,返回新生成的id
        String articleString = getRequestString("article");
        Map<String, Object> article = JsonUtils.fromJson(articleString);
        String id = MapUtils.getString(article, "id", "");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage("id不存在");
        }
        ZyParentNewsArticle zyParentNewsArticle = ZyParentNewsArticle.init(article);
        // 更新了文章内容，顺带更新一下news的update time，因为现在页面内容已经走静态化了
        String newsId = zyParentNewsArticle.getNews_id();
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews != null) {
            jxtNews.setUpdateTime(new Date());
            crmVendorService.$upsertJxtNews(jxtNews);
        }
        String editor = getCurrentAdminUser().getAdminUserName();
        zyParentNewsArticle.setEditor(editor);
        // String tags = getRequestString("tags");备用
        addAdminLog("message-管理员" + getCurrentAdminUser().getAdminUserName() + "更新家长资讯内容成功");
        return parentNewsService.updateArticle(id, zyParentNewsArticle);
    }


    /**
     * 内容管理
     */
    @RequestMapping(value = "viewarticles.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewArticles(Model model) {
        String mainSiteBaseUrl = ProductConfig.getMainSiteBaseUrl();
        model.addAttribute("mainSiteBaseUrl", mainSiteBaseUrl);
        AuthCurrentAdminUser user = getCurrentAdminUser();
        model.addAttribute("currentUser", user.getAdminUserName());
        return "advisory/viewarticles";
    }


    /**
     * delete article
     */
    @RequestMapping(value = "deletearticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteArticle() {
        String id = getRequestString("id");
        return parentNewsService.deleteArticle(id);
    }

    /**
     * preview raw article
     */
    @RequestMapping(value = "previewrawarticle.vpage", method = RequestMethod.GET)
    public String previewRawArticle(Model model) {
        String id = getRequestString("id");
        ZyParentNewsRawArticle zyParentNewsRawArticle = parentNewsService.loadRawArticleById(id);
        model.addAttribute("article", zyParentNewsRawArticle);
        zyParentNewsRawArticle.setContent(StringUtils.replace(zyParentNewsRawArticle.getContent(), "data-src", "src"));
        return "advisory/previewarticle";
    }

    /**
     * preview article
     */
    @RequestMapping(value = "previewarticle.vpage", method = RequestMethod.GET)
    public String previewArticle(Model model) {
        String id = getRequestString("id");
        ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(id);
        model.addAttribute("article", zyParentNewsArticle);
        return "advisory/previewarticle";
    }

    /**
     * preview article
     */
    @RequestMapping(value = "generatestaticpage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateStaticPage() {
        String id = getRequestString("id");
        ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(id);
        // 使用freemaker来渲染页面，然后存入mongofs
        String html = "<html><head><title>" + zyParentNewsArticle.getTitle() + "</title></head><body>" + zyParentNewsArticle.getContent() + "</body></html>";
        try {
            byte[] content = html.getBytes();
            String fileId = RandomUtils.nextObjectId();
            String fileName = "parentnewsstatic" + "-" + id + ".vpage";

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);

            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType("text/html");
            fsDefault.uploadWithId(bais, fileId, fileName, null, metadata);

            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.warn("Parent news generate static page: failed writing into mongo gfs", ex.getMessage());
            return MapMessage.errorMessage();
        }
    }

    /**
     * 评论管理
     */
    @RequestMapping(value = "commentlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryComment(Model model) {
        String newsId = getRequestString("newsId");

        model.addAttribute("newsId", newsId);
        return "advisory/commentlist";
    }

    /**
     * 获取评论列表
     */
    @RequestMapping(value = "getcommentlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCommentList() {
        int currentPage = getRequestInt("currentPage");
        String newsId = getRequestString("newsId");
        List<JxtNewsComment> commentList = jxtNewsLoaderClient.getCommentListByTypeId(newsId);
        commentList = commentList.stream().filter(m -> !m.getIsDisabled()).collect(Collectors.toList());
        int totalCount = commentList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();

        Comparator<JxtNewsComment> c = ((o1, o2) -> (o2.getIsShow() ? 1 : 0) - (o1.getIsShow() ? 1 : 0));
        c = c.thenComparing((o1, o2) -> (o2.getComment().length() > 50 ? 1 : 0) - (o1.getComment().length() > 50 ? 1 : 0));
        c = c.thenComparing((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        commentList = commentList.stream().sorted(c).collect(Collectors.toList());
        int total = commentList.size();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = total > (statIndex + 10) ? statIndex + 10 : total;
        if (statIndex > endIndex) {
            commentList = new ArrayList<>();
        } else {
            commentList = commentList.subList(statIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateCommentList(commentList);
        return MapMessage.successMessage().add("newsId", newsId).add("commentList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }

    /**
     * 评论筛选
     */
    @RequestMapping(value = "commentpick.vpage", method = RequestMethod.GET)
    public String commentPick(Model model) {
        Date defaultEndTime = new Date();
        Date defaultStartTime = DateUtils.addHours(defaultEndTime, -24);
        model.addAttribute("startTime", DateUtils.dateToString(defaultStartTime));
        return "advisory/commentpick";
    }

    /**
     * 根据各种条件获取相关评论列表
     *
     * @return
     */
    @RequestMapping(value = "getcommentpicklist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCommentPickList() {
        int currentPage = getRequestInt("currentPage");
        if (currentPage == 0) {
            currentPage = 1;
        }

        String status = getRequestString("status");
        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");
        //默认字数过滤是10字以上
        int wordLength = getRequestInt("wordLength", 10);
        String userName = getRequestString("userName");
        Long userId = getRequestLong("userId");
        String articleTitle = getRequestString("articleTitle");

        //测试环境选100天的
        int maxDaysDelta = 100;
        if (RuntimeMode.ge(Mode.TEST)) {
            maxDaysDelta = 3;
        }

        //开始时间为空，默认为maxDaysDelta天前
        Date startDate = StringUtils.isBlank(startTime) ? DateUtils.addDays(new Date(), -maxDaysDelta) : DateUtils.stringToDate(startTime);
        Date endDate = StringUtils.isBlank(endTime) ? new Date() : DateUtils.stringToDate(endTime);

        if (DateUtils.dayDiff(endDate, startDate) > maxDaysDelta) {
            return MapMessage.errorMessage("最大只允许查询" + maxDaysDelta + "天内的评论");
        }

        //时间过滤
        List<JxtNewsComment> jxtNewsCommentList = jxtNewsLoaderClient.getAllCommentsForPeriod(startDate, endDate, "createTime");
        Set<String> newsIds = jxtNewsCommentList.stream().map(JxtNewsComment::getNewsId).collect(Collectors.toSet());
        Map<String, JxtNews> jxtNewsMap = crmVendorService.$loadsJxtNewsMap(newsIds);

        jxtNewsCommentList = jxtNewsCommentList.stream()
                .filter(e -> e.getComment() != null && e.getComment().trim().length() >= wordLength)
                .filter(e -> StringUtils.isBlank(userName) || (StringUtils.isNotBlank(e.getUserName()) && e.getUserName().contains(userName)))
                .filter(e -> userId == 0L || (e.getUserId() != null && e.getUserId().equals(userId)))
                .filter(e -> StringUtils.isBlank(articleTitle) || (jxtNewsMap.get(SafeConverter.toString(e.getNewsId(), ""))) != null && jxtNewsMap.get(e.getNewsId()).getTitle().contains(articleTitle))
                .collect(Collectors.toList());

        if ("pending".equals(status)) {
            //待处理
            jxtNewsCommentList = jxtNewsCommentList.stream().filter(e -> !e.getIsShow() && e.getState() == null).collect(Collectors.toList());
        } else if ("spam".equals(status)) {
            //垃圾
            jxtNewsCommentList = jxtNewsCommentList.stream().filter(e -> !e.getIsShow() && "spam".equals(e.getState())).collect(Collectors.toList());
        } else if ("show".equals(status)) {
            //展示
            jxtNewsCommentList = jxtNewsCommentList.stream().filter(e -> e.getIsShow()).collect(Collectors.toList());
        } else if ("hide".equals(status)) {
            //隐藏
            jxtNewsCommentList = jxtNewsCommentList.stream().filter(e -> !e.getIsShow() && "hide".equals(e.getState())).collect(Collectors.toList());
        }

        int totalCount = jxtNewsCommentList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(20), BigDecimal.ROUND_UP).intValue();
        int startIndex = (currentPage - 1) * 20;

        int endIndex = totalCount > (startIndex + 20) ? startIndex + 20 : totalCount;
        if (startIndex > endIndex) {
            jxtNewsCommentList = new ArrayList<>();
        } else {
            jxtNewsCommentList = jxtNewsCommentList.subList(startIndex, endIndex);
        }

        List<Map<String, Object>> commentList = generateCommentList(jxtNewsCommentList);

        return MapMessage.successMessage().add("commentList", commentList).add("totalPage", totalPage).add("currentPage", currentPage).add("commentCount", totalCount);
    }

    @RequestMapping(value = "hidecomments.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage hideComments() {
        String commentIdsStr = getRequestString("commentIdsStr");
        String[] commentIdsSplit = commentIdsStr.split(",");
        List<String> commentIds = new ArrayList<>();
        for (String commentId : commentIdsSplit) {
            if (StringUtils.isNotBlank(commentId)) {
                commentIds.add(commentId);
            }
        }

        return jxtNewsServiceClient.hideComments(commentIds);
    }


    /**
     * 展示评论
     */
    @RequestMapping(value = "onliecomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage onlineComment() {
        String commentId = getRequestString("commentId");
        JxtNewsComment comment = jxtNewsLoaderClient.getCommentById(commentId);
        if (comment == null) {
            return MapMessage.errorMessage("id{}的评论不存在", commentId);
        }

        MapMessage mapMessage = jxtNewsServiceClient.onlineJxtNewsComment(comment.getId());
        if (mapMessage.isSuccess()) {
//            jxtNewsServiceClient.incJxtNewsNoticeCount(comment.getUserId());
//            vendorCacheClient.getParentJxtCacheManager().incrTabNoticeCount(comment.getUserId().toString());
            asyncNewsCacheService
                    .JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_TAB_NOTICE_COUNT, comment.getUserId().toString())
                    .awaitUninterruptibly();

            //该条评论第一次被选为精选评论的时候才会发送系统信息，并且记录此信息，确保进行取消展示后再展示评论不会再次发送系统信息
//            Set<String> commentIds = vendorCacheClient.getParentJxtCacheManager().loadOnlineCommentRecord(comment.getNewsId());
            Set<String> commentIds = asyncNewsCacheService
                    .JxtNewsCacheManager_loadRecordCommentIds(comment.getNewsId())
                    .take();
            if (CollectionUtils.isEmpty(commentIds) || !commentIds.contains(commentId)) {
                //评论被选为精选时，给用户发送一条系统消息
                AppMessage appUserMessage = new AppMessage();
                appUserMessage.setUserId(comment.getUserId());
                appUserMessage.setMessageType(ParentMessageType.REMINDER.getType());
                String content = "我的评论被选为精选评论:";
                JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(comment.getNewsId());
                //评论内容
                content = content + "\n\"" + comment.getComment() + "\"";
                //资讯标题
                content = content + "\n《" + jxtNews.getTitle() + "》";
                appUserMessage.setContent(content);
                appUserMessage.setLinkType(1);

                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("tag", ParentMessageTag.互动消息.name());
                //新版消息内容相关字段
                extInfo.put("img", "/public/skin/parentMobile/images/new_icon/jzt.png");
                extInfo.put("operatorName", "家长通小编");
                extInfo.put("title", "我的评论被选为精选评论");
                extInfo.put("comment", comment.getComment());
                extInfo.put("newsTitle", "《" + jxtNews.getTitle() + "》");
                extInfo.put("newsId", jxtNews.getId());
                //这个字段用来标记类型，以便前端采用对应的样式
                extInfo.put("style", "interaction_message");
                appUserMessage.setExtInfo(extInfo);

                messageCommandServiceClient.getMessageCommandService().createAppMessage(appUserMessage);
//                vendorCacheClient.getParentJxtCacheManager().recordOnlineComment(comment.getNewsId(), commentId);
                asyncNewsCacheService
                        .JxtNewsCacheManager_recordCommentIdToSet(comment.getNewsId(), commentId)
                        .awaitUninterruptibly();
            }

        }
        return mapMessage;
    }

    /**
     * 批量展示评论
     */
    @RequestMapping(value = "batchshowcomments.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage batchShowComments() {
        String commentsStr = getRequestString("comments");
        String[] commentIds = commentsStr.split(",");
        for (String commentId : commentIds) {
            JxtNewsComment comment = jxtNewsLoaderClient.getCommentById(commentId);
            if (comment == null) {
                continue;
            }

            MapMessage mapMessage = jxtNewsServiceClient.onlineJxtNewsComment(comment.getId());
            if (mapMessage.isSuccess()) {
//                vendorCacheClient.getParentJxtCacheManager().incrTabNoticeCount(comment.getUserId().toString());
                asyncNewsCacheService
                        .JxtNewsCacheManager_incrCacheCount(JxtNewsCacheType.JXT_NEWS_TAB_NOTICE_COUNT, comment.getUserId().toString())
                        .awaitUninterruptibly();
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 取消展示评论
     */
    @RequestMapping(value = "offlinecomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage offlineComment() {
        String commentId = getRequestString("commentId");
        JxtNewsComment comment = jxtNewsLoaderClient.getCommentById(commentId);
        if (comment == null) {
            return MapMessage.errorMessage("id{}的评论不存在", commentId);
        }
        MapMessage mapMessage = jxtNewsServiceClient.offlineJxtNewsComment(comment.getId());
        if (mapMessage.isSuccess()) {
//            vendorCacheClient.getParentJxtCacheManager().decrTabNoticeCount(comment.getUserId().toString());
            asyncNewsCacheService
                    .JxtNewsCacheManager_decrCacheCount(JxtNewsCacheType.JXT_NEWS_TAB_NOTICE_COUNT, comment.getUserId().toString())
                    .awaitUninterruptibly();
        }
        return mapMessage;
    }

    /**
     * 回复
     */
    @RequestMapping(value = "replycomment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage replyComment() {
        String commentId = getRequestString("commentId");
        String replyComment = getRequestString("replyComment");
        if (StringUtils.isBlank(replyComment)) {
            return MapMessage.errorMessage("回复内容不能为空");
        }
        JxtNewsComment comment = jxtNewsLoaderClient.getCommentById(commentId);
        if (comment == null) {
            return MapMessage.errorMessage("id{}的评论不存在", commentId);
        }
        JxtNewsComment commentReply = new JxtNewsComment();
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();
        commentReply.setComment(replyComment);
        commentReply.setUserName(currentAdminUser.getAdminUserName());
        commentReply.setIsShow(Boolean.FALSE);
        commentReply.setIsDisabled(Boolean.FALSE);
        commentReply.setTypeId(commentId);
        commentReply.setNewsId(comment.getNewsId());

        String id = jxtNewsServiceClient.saveJxtNewsComment(commentReply);
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("回复评论{}失败", commentId);
        } else {
            //家长通小编回复用户评论，给用户发送一条系统消息
            AppMessage appUserMessage = new AppMessage();
            appUserMessage.setUserId(comment.getUserId());
            appUserMessage.setMessageType(ParentMessageType.REMINDER.getType());
            String content = "家长通小编回复了我的评论:";
            JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(comment.getNewsId());
            //回复内容
            content = content + "\n\"" + replyComment + "\"";
            //资讯标题
            content = content + "\n《" + jxtNews.getTitle() + "》";
            appUserMessage.setContent(content);
            appUserMessage.setLinkType(1);

            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("tag", ParentMessageTag.互动消息.name());
            //新版消息内容相关字段
            extInfo.put("img", "/public/skin/parentMobile/images/new_icon/jzt.png");
            extInfo.put("operatorName", "家长通小编");
            extInfo.put("title", "回复了我的评论:");
            extInfo.put("comment", replyComment);
            extInfo.put("newsTitle", "《" + jxtNews.getTitle() + "》");
            extInfo.put("newsId", jxtNews.getId());
            //这个字段用来标记类型，以便前端采用对应的样式
            extInfo.put("style", "interaction_message");

            appUserMessage.setExtInfo(extInfo);

            messageCommandServiceClient.getMessageCommandService().createAppMessage(appUserMessage);
            return MapMessage.successMessage();
        }
    }

    /**
     * 展示回复
     */
    @RequestMapping(value = "onliecommentreply.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage onlineCommentReply() {
        String commentId = getRequestString("commentId");
        List<JxtNewsComment> commentReplyList = jxtNewsLoaderClient.getCommentListByTypeId(commentId);
        if (commentReplyList.size() < 1) {
            return MapMessage.errorMessage("id{}的评论不存在", commentId);
        }
        JxtNewsComment commentReply = commentReplyList.get(0);
        return jxtNewsServiceClient.onlineJxtNewsComment(commentReply.getId());
    }

    /**
     * 取消展示回复
     */
    @RequestMapping(value = "offlinecommentreply.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage offlineCommentReply() {
        String commentId = getRequestString("commentId");
        List<JxtNewsComment> commentReplyList = jxtNewsLoaderClient.getCommentListByTypeId(commentId);
        if (commentReplyList.size() < 1) {
            return MapMessage.errorMessage("id{}的评论不存在", commentId);
        }
        JxtNewsComment commentReply = commentReplyList.get(0);
        return jxtNewsServiceClient.offlineJxtNewsComment(commentReply.getId());
    }


    /**
     * 内容发布
     */
    @RequestMapping(value = "jxtnewslist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryContentPush() {
        return "advisory/jxtnewslist";
    }


    /**
     * 文章统计
     */
    @RequestMapping(value = "jxtnewscountlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String newsCount() {
        return "advisory/jxtnewscountlist";
    }


    /**
     * 获取内容发布列表
     */
    @RequestMapping(value = "getArticleList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getArticleList() {
        int currentPage = getRequestInt("currentPage");
        //如果是文章统计页面访问这个接口，这个参数就是true,用来控制后面过滤外部文章的条件用
        Boolean countFlag = getRequestBool("countFlag");
        if (currentPage == 0) {
            currentPage = 1;
        }
        // 如果制定了id，直接根据id过滤得到一个，其他条件不命中
        String id = getRequestString("id");
        List<JxtNews> jxtNewsList;
        if (StringUtils.isNotEmpty(id)) {
            jxtNewsList = new ArrayList<>();
            JxtNews jxtNews = crmVendorService.$loadJxtNews(id);
            if (jxtNews != null) {
                jxtNewsList.add(jxtNews);
            }
        } else {

            // 文章源
            String source = getRequestString("source");
            // 标题
            String title = getRequestString("title");
            // 文章类型
            String contentType = getRequestString("contentType");
            // 内容类型
            String styleType = getRequestString("styleType");
            // 文章状态，已上线or未上线
            String status = getRequestString("status");
            String pushUser = getRequestString("pushUser");
            long tag = getRequestLong("tag");
            String startTime = getRequestString("startTime");
            String endTime = getRequestString("endTime");
            String isTop = getRequestString("isTop");

            Date startDatetime = StringUtils.isNotEmpty(startTime) ? DateUtils.stringToDate(startTime) : null;
            Date endDatetime = StringUtils.isNotEmpty(endTime) ? DateUtils.stringToDate(endTime) : null;
            jxtNewsList = crmJxtNewsDao.loadAllFromSecondary();
            // todo:这个过滤的过程简直头痛
            jxtNewsList = jxtNewsList.stream().filter(p -> tag == 0 || p.getTagList().contains(tag))
                    .filter(p -> StringUtils.isBlank(pushUser) || (StringUtils.isNotBlank(p.getPushUser()) && StringUtils.equals(p.getPushUser(), pushUser)))
                    .filter(p -> StringUtils.isBlank(contentType) || p.getJxtNewsContentType() == JxtNewsContentType.parse(contentType))
                    .filter(p -> StringUtils.isBlank(styleType) || p.getJxtNewsStyleType() == JxtNewsStyleType.parse(styleType))
                    .filter(p -> StringUtils.isBlank(source) || (StringUtils.isNotBlank(p.getSource()) && p.getSource().contains(source)))
                    .filter(p1 -> StringUtils.isBlank(title) || (StringUtils.isNotBlank(p1.getTitle()) && p1.getTitle().contains(title)))
                    .filter(e -> !JxtNewsContentType.SUBJECT.equals(e.getJxtNewsContentType()))
                    .filter(e -> countFlag ? !JxtNewsStyleType.OFFICIAL_ACCOUNT.equals(e.getJxtNewsStyleType()) : !JxtNewsStyleType.OFFICIAL_ACCOUNT.equals(e.getJxtNewsStyleType()) && !JxtNewsStyleType.EXTERNAL_ALBUM_NEWS.equals(e.getJxtNewsStyleType()))
                    .filter(p -> tag == 0 || (p.getTagList() != null && p.getTagList().contains(tag)))
                    .filter(p -> startDatetime == null || (p.getPushTime() != null && p.getPushTime().after(startDatetime)))
                    .filter(p -> endDatetime == null || (p.getPushTime() != null && p.getPushTime().before(endDatetime)))
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toList());
            if (!StringUtils.equals(status, "all")) {
                boolean statusBool = StringUtils.equals(status, "online");
                jxtNewsList = jxtNewsList.stream().filter(p -> p.getOnline() == statusBool).collect(Collectors.toList());
            }
            if (!StringUtils.equals(isTop, "all")) {
                boolean top = SafeConverter.toBoolean(isTop);
                jxtNewsList = jxtNewsList.stream().filter(p -> top == SafeConverter.toBoolean(p.getIsTop())).collect(Collectors.toList());
            }
        }
        int totalCount = jxtNewsList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        int startIndex = (currentPage - 1) * 10;

        int endIndex = totalCount > (startIndex + 10) ? startIndex + 10 : totalCount;
        if (startIndex > endIndex) {
            jxtNewsList = new ArrayList<>();
        } else {
            jxtNewsList = jxtNewsList.subList(startIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewsCountMap(jxtNewsList);
        return MapMessage.successMessage().add("jxtNewsList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }


    /**
     * 新建、编辑资讯
     */
    @RequestMapping(value = "jxtnewsedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String articleEdit(Model model) {
        String articleId = getRequestString("articleId");
        String newsId = getRequestString("newsId");
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(newsId)) {
            //编辑
            JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
            map = generateJxtNewsInfoMap(jxtNews);
            if (jxtNews != null) {
                ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(jxtNews.getArticleId());
                map.put("content", zyParentNewsArticle.getContent());
            } else {
                return "advisory/viewarticles";
            }
        } else if (StringUtils.isNotBlank(articleId)) {
            //新建
            ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(articleId);
            if (zyParentNewsArticle == null) {
                getAlertMessageManager().addMessageError("文章" + articleId + "不存在");
                return "advisory/jxtnewsedit";
            }
            map.put("articleId", articleId);
            map.put("source", zyParentNewsArticle.getPublisher());
            map.put("sourceUrl", zyParentNewsArticle.getSource_url());
            map.put("title", zyParentNewsArticle.getTitle());
            String articleContent = zyParentNewsArticle.getContent();
            map.put("content", articleContent);
            // 判断音视频，初始为图文
            JxtNewsContentType contentType = JxtNewsContentType.IMG_AND_TEXT;
            if (articleContent.contains("<video")) {
                contentType = JxtNewsContentType.VIDEO;
            } else if (articleContent.contains("<audio")) {
                contentType = JxtNewsContentType.AUDIO;
            }
            map.put("contentType", contentType);
            List<String> recommendHeadFigures = zyParentNewsArticle.getRecommend_head_figures();
            if (CollectionUtils.isNotEmpty(recommendHeadFigures)) {
                Map<String, Object> imgUrlMap = new HashMap<>();
                Map<String, Object> fileNameMap = new HashMap<>();
                // 如果有>3张图，使用三张小图样式，否则使用一张小图
                if (recommendHeadFigures.size() >= 3 && RandomUtils.hitProbability(0.5d)) {
                    for (int i = 0; i < 3; i++) {
                        String url = recommendHeadFigures.get(i);
                        String filename = url.substring(url.lastIndexOf('/') + 1);
                        imgUrlMap.put("url" + i, url);
                        fileNameMap.put("url" + i, filename);
                    }
                    map.put("imgUrl", imgUrlMap);
                    map.put("fileName", fileNameMap);
                    map.put("type", JxtNewsType.THREE_IMAGES);
                } else {
                    String url = recommendHeadFigures.get(0);
                    imgUrlMap.put("url0", url);
                    // 根据url截断出gridfs中的filename
                    String filename = url.substring(url.lastIndexOf('/') + 1);
                    fileNameMap.put("url0", filename);
                    map.put("type", JxtNewsType.SMALL_IMAGE);
                    map.put("imgUrl", imgUrlMap);
                    map.put("fileName", fileNameMap);
                }
            } else {
                map.put("type", JxtNewsType.TEXT);
            }
            List<Long> tagIds = zyParentNewsArticle.getTagIds();
            // 如果是旧版抓取的文章这里没有自动推荐的标签，直接用一个empty list代替
            Map<Long, String> tagsMap;
            if (CollectionUtils.isNotEmpty(tagIds)) {
                Collection<JxtNewsTag> tags = crmVendorService.$loadJxtNewsTagList().stream()
                        .filter(e -> tagIds.contains(e.getId()))
                        .collect(Collectors.toList());
                tagsMap = tags.stream().collect(Collectors.toMap(JxtNewsTag::getId, JxtNewsTag::getTagName));
            } else {
                // 如果没有推荐的，那就是空的标签，前端需要编辑再选择一个
                tagsMap = new HashMap<>();
            }
            map.put("tags", tagsMap);
        }
        Map<String, String> newsTypeMap = new HashMap<>();
        for (JxtNewsType type : JxtNewsType.values()) {
            if (type != JxtNewsType.UNKNOWN) {
                newsTypeMap.put(type.name(), type.getDesc());
            }
        }
        map.put("totalType", newsTypeMap);

        //资讯内容类型
        Map<String, String> newsContentTypeMap = new HashMap<>();
        for (JxtNewsContentType type : JxtNewsContentType.values()) {
            if (type != JxtNewsContentType.UNKNOWN
                    && type != JxtNewsContentType.SUBJECT
                    && type != JxtNewsContentType.OFFICIAL_ACCOUNT
                    && type != JxtNewsContentType.OFFICIAL_ACCOUNT_SUBMIT
            ) {
                newsContentTypeMap.put(type.name(), type.getDesc());
            }
        }
        map.put("totalContentType", newsContentTypeMap);

        // 内容样式
        Map<String, String> newsStyleTypeMap = Arrays.stream(JxtNewsStyleType.values())
                .filter(type -> type != JxtNewsStyleType.UNKNOWN)
                .collect(Collectors.toMap(k -> k.name(), v -> v.getDesc(), (k, v) -> null, LinkedHashMap::new));
        map.put("styleTypeList", newsStyleTypeMap);

        model.addAttribute("jxtNewsInfo", map);

        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(0, "root", jxtNewsTags, false);
        model.addAttribute("tagTree", JsonUtils.toJson(tagTree));
        return "advisory/jxtnewsedit";
    }

    //根据内容类型返回相应的页面版式
    @RequestMapping(value = "contentTypeAndNewsType.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage contentTypeAndNewsType() {
        String contentType = getRequestString("contentType");
        Map<String, String> newsTypeMapForVideo = new HashMap<>();
        if (Objects.equals(contentType, JxtNewsContentType.AUDIO.name()) || Objects.equals(contentType, JxtNewsContentType.VIDEO.name())) {
            for (JxtNewsType type : JxtNewsType.values()) {
                if (type != JxtNewsType.UNKNOWN && type != JxtNewsType.TEXT && type != JxtNewsType.THREE_IMAGES) {
                    newsTypeMapForVideo.put(type.name(), type.getDesc());
                }
            }
        } else {
            for (JxtNewsType type : JxtNewsType.values()) {
                if (type != JxtNewsType.UNKNOWN) {
                    newsTypeMapForVideo.put(type.name(), type.getDesc());
                }
            }
        }

        return MapMessage.successMessage().add("newsTypeMapForVideo", newsTypeMapForVideo);
    }

    private List<String> saveCoverToTestGridfs(List<String> covers) {
        List<String> newCovers = new ArrayList<>();
        for (String cover : covers) {
            if (!cover.startsWith("parent")) {
                String url;
                if (!cover.startsWith("http")) {
                    url = "http://cdn-cnc.17zuoye.cn/gridfs/" + cover;
                } else {
                    url = cover;
                    cover = cover.substring(cover.lastIndexOf('/') + 1);
                }

                // 把这个图从线上gridfs下载后然后存入测试环境的
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
                String prefix = "parentnews-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
                try {
                    @Cleanup InputStream inStream = new ByteArrayInputStream(response.getOriginalResponse());
                    String filename = crmImageUploader.upload(prefix, cover, inStream);
                    newCovers.add(filename);
                } catch (Exception ex) {
                    log.error("上传咨询图片异常： " + ex.getMessage());
                }
            } else {
                // 截取图片地址
                String filename = cover.substring(cover.lastIndexOf('/') + 1);
                newCovers.add(filename);
            }
        }
        return newCovers;
    }


    @RequestMapping(value = "savejxtnews.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveJxtNews() {
        String newsId = getRequestString("newsId");

        String title = getRequestString("title");
        String articleId = getRequestString("articleId");
        String imgStr = getRequestString("imgStr");
        String tagStr = getRequestString("tagStr");
        String categoryStr = getRequestString("categoryStr");
        String source = getRequestString("source");
        String sourceUrl = getRequestString("sourceUrl");
        Long availableUserId = getRequestLong("availableUserId");
        String newsType = getRequestString("newsType");
        String contentType = getRequestString("contentType");
        String styleType = getRequestString("styleType");
        String chatGroupId = getRequestString("chatGroupId");
        String chatGroupWelcomeContext = getRequestString("chatGroupWelcomeContent");
        Boolean showAd = getRequestBool("showAd");
        Integer pushTypeId = getRequestInt("pushType");
        String regionIdStr = getRequestString("regionIds");
        String albumId = getRequestString("albumId");
        Integer newsRank = getRequestInt("newsRank");
        String playTime = getRequestString("playTime");
        Boolean free = getRequestBool("free");
        Boolean isSnapshot = getRequestBool("isSnapshot");
        String videoSnapshotUrl = getRequestString("videoSnapshotUrl");
        Boolean isTop = getRequestBool("isNewsTop");
        Integer topOrder = getRequestInt("topOrder");

        String video_url = getRequestString("video_url");

        //付费资讯不能出现在免费专辑里
        if (!free && StringUtils.isNotBlank(albumId)) {
            JxtNewsAlbum jxtNewsAlbum = jxtNewsLoaderClient.getJxtNewsAlbum(albumId);
            if (null == jxtNewsAlbum) {
                return MapMessage.errorMessage("未查询到专辑");
            }
            if (null == jxtNewsAlbum.getFree() || jxtNewsAlbum.getFree()) {
                return MapMessage.errorMessage("免费专辑里不能添加付费资讯");
            }
        }

        JxtNewsPushType pushType = JxtNewsPushType.ofWithUnKnow(pushTypeId);
        if (pushType == JxtNewsPushType.UNKNOWN) {
            return MapMessage.errorMessage("推送方式错误");
        }
        List<String> imgList = new ArrayList<>();
        String[] imgSplit = imgStr.split(",");
        for (String s : imgSplit) {
            if (StringUtils.isNotBlank(s)) {
                imgList.add(s);
            }
        }
        if (isSnapshot) {
            String imgBySnapshot = generateVideoImgBySnapshot(videoSnapshotUrl, imgList.get(0));
            imgList.clear();
            imgList.add(imgBySnapshot);
        } else {
            // mark:因为资讯抓取的时候龙龙推荐的头图是放在线上gridfs中的，所以这里把头图往测试环境的gridfs也写一份
            if (RuntimeMode.lt(Mode.STAGING)) {
                imgList = saveCoverToTestGridfs(imgList);
            }
        }
        List<Long> tagList = new ArrayList<>();
        String[] tagSplit = tagStr.split(",");
        for (String s : tagSplit) {
            if (StringUtils.isNotBlank(s)) {
                tagList.add(SafeConverter.toLong(s));
            }

        }
        List<Long> categoryList = new ArrayList<>();
        String[] categorySplit = categoryStr.split(",");
        for (String s : categorySplit) {
            if (StringUtils.isNotBlank(s)) {
                categoryList.add(SafeConverter.toLong(s));
            }
        }
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        JxtNews jxtNews = null;
        String oldAlbumId = "";
        if (StringUtils.isNotBlank(newsId)) {
            jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews == null) {
                return MapMessage.errorMessage("id为{}的资讯不存在", newsId);
            }
            if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
                oldAlbumId = jxtNews.getAlbumId();
            }
            jxtNews.setUpdateTime(new Date());
        }
        if (jxtNews == null) {
            jxtNews = new JxtNews();
            //编辑操作不改变上下线状态
            jxtNews.setOnline(Boolean.FALSE);
        }
        //推送方式
        jxtNews.setPushType(pushType.getType());
        if (pushType == JxtNewsPushType.ALL_USER) {
            jxtNews.setAvailableUserId(0L);
            jxtNews.setRegionCodeList(null);
        } else if (pushType == JxtNewsPushType.SIGNAL_USER) {
            jxtNews.setAvailableUserId(availableUserId);
            jxtNews.setRegionCodeList(null);
        } else {
            jxtNews.setAvailableUserId(null);
            Set<Integer> regionIds = new HashSet<>();
            String[] split = regionIdStr.split(",");

            for (String id : split) {
                regionIds.add(SafeConverter.toInt(id));
            }
            Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
            if (MapUtils.isNotEmpty(exRegionMap)) {
                jxtNews.setRegionCodeList(exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList()));
            }
        }
        ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(articleId);
        String digest = "";
        if (zyParentNewsArticle != null && StringUtils.isNotBlank(zyParentNewsArticle.getContent())) {
            digest = jxtNewsLoaderClient.removeHtml(zyParentNewsArticle.getContent());
            if (digest.length() > 200) {
                digest = digest.substring(0, 200);
            }
        }

        //摘要
        digest = digest.replace("\n", "");
        jxtNews.setDigest(digest);
        jxtNews.setTitle(title);
        jxtNews.setArticleId(articleId);
        jxtNews.setSource(source);
        jxtNews.setSourceUrl(sourceUrl);
        jxtNews.setCoverImgList(imgList);
        jxtNews.setTagList(tagList);
        jxtNews.setCategoryList(categoryList);
        jxtNews.setOperateUserName(adminUser.getAdminUserName());
        jxtNews.setJxtNewsType(JxtNewsType.parse(newsType));
        jxtNews.setJxtNewsContentType(JxtNewsContentType.parse(contentType));
        jxtNews.setJxtNewsStyleType(JxtNewsStyleType.parse(styleType));
        jxtNews.setChatGroupWelcomeContent(chatGroupWelcomeContext);
        jxtNews.setChatGroupId(chatGroupId);
        jxtNews.setShowAd(showAd);
        jxtNews.setAlbumId(albumId);
        jxtNews.setPlayTime(playTime);
        jxtNews.setFree(free);
        jxtNews.setVideo_url(video_url);
        jxtNews.setIsTop(isTop);
        jxtNews.setTopOrder(topOrder);
        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (jxtNews == null) {
            return MapMessage.errorMessage();
        }
        String id = jxtNews.getId();
        // 设置文章已发布
        parentNewsService.setPushedFlag(jxtNews.getArticleId(), id);
        JxtNewsAlbum oldAlbum = crmVendorService.$loadJxtNewsAlbum(oldAlbumId);
        if (!StringUtils.equals(oldAlbumId, albumId)) {
            if (oldAlbum != null) {
                List<JxtNewsAlbum.NewsRecord> oldAlbumNewsList = oldAlbum.getNewsRecordList().stream().filter(e -> !StringUtils.equals(e.getNewsId(), newsId)).collect(Collectors.toList());
                oldAlbum.setNewsRecordList(oldAlbumNewsList);
                crmVendorService.$upsertJxtNewsAlbum(oldAlbum);
            }
            if (StringUtils.isNotBlank(albumId)) {
                JxtNewsAlbum newAlbum = crmVendorService.$loadJxtNewsAlbum(albumId);
                JxtNewsAlbum.NewsRecord newsRecord = new JxtNewsAlbum.NewsRecord();
                newsRecord.setNewsId(id);
                newsRecord.setCreateTime(new Date());
                newsRecord.setRank(newsRank);
                if (newAlbum != null) {
                    List<JxtNewsAlbum.NewsRecord> newsRecordList = newAlbum.getNewsRecordList();
                    if (CollectionUtils.isNotEmpty(newsRecordList)) {
                        if (!newsRecordList.contains(newsRecord)) {
                            newsRecordList.add(newsRecord);
                        }
                    } else {
                        newsRecordList = new ArrayList<>();
                        newsRecordList.add(newsRecord);
                    }
                    newAlbum.setNewsRecordList(newsRecordList);
                    crmVendorService.$upsertJxtNewsAlbum(newAlbum);
//                    jxtNewsServiceClient.sendUpdateAlbumMessageToSubUser(albumId, 1);
                }
            }
        } else {
            if (oldAlbum != null) {
                List<JxtNewsAlbum.NewsRecord> oldAlbumNewsList = oldAlbum.getNewsRecordList();
                JxtNewsAlbum.NewsRecord newsRecord = oldAlbumNewsList.stream().filter(p -> StringUtils.equals(p.getNewsId(), id)).findFirst().orElse(null);
                if (newsRecord != null) {
                    oldAlbumNewsList.remove(newsRecord);
                    newsRecord.setRank(newsRank);
                    oldAlbumNewsList.add(newsRecord);
                    oldAlbum.setNewsRecordList(oldAlbumNewsList);
                    crmVendorService.$upsertJxtNewsAlbum(oldAlbum);
                }
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "jxtnewsonline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onlineJxtNews() {
        String newsId = getRequestString("newsId");
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("您要上线的资讯不存在：" + newsId);
        }
        jxtNews.setOnline(true);
        // 只有第一次上线的时候才记录发布人和上线时间，by pm 林熙蕾
        if (jxtNews.getPushTime() == null) {
            jxtNews.setPushTime(new Date());
            jxtNews.setPushUser(getCurrentAdminUser().getAdminUserName());
        }
        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
            sendAlbumReminder(jxtNews.getAlbumId());
            //jxtNewsServiceClient.sendUpdateAlbumMessageToSubUser(jxtNews.getAlbumId());
        }
        addAdminLog(getCurrentAdminUser().getAdminUserName() + "上线了文章：" + newsId);
        return new MapMessage().setSuccess(jxtNews != null);
    }


    /**
     * 定时上线文章
     */
    @RequestMapping(value = "jxtnewstimeronline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage timeronlineJxtNews() {
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage().setInfo("未选择资讯!");
        }
        try {
            String[] newsIds = newsId.split(",");
            List<String> newsIdList = Arrays.asList(newsIds);
            String publishDate = getRequestString("publishDate");
            Date publishTime = DateUtils.stringToDate(publishDate, DateUtils.FORMAT_SQL_DATETIME);
            if (StringUtils.isBlank(publishDate)) {
                return MapMessage.errorMessage("未传入定时时间");
            }
            Map<String, JxtNews> jxtNewsMap = crmVendorService.$loadsJxtNewsMap(newsIdList);
            final Date finalPublishTime = publishTime;
            newsIdList.stream().forEach(e -> {
                JxtNews jxtNews = jxtNewsMap.get(e);
                if (jxtNews != null) {
                    jxtNews.setPublishTime(finalPublishTime.getTime());
                    if (StringUtils.isEmpty(jxtNews.getPushUser())) {
                        jxtNews.setPushUser(getCurrentAdminUser().getAdminUserName());
                    }
                    jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
                }
            });
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    /**
     * 取消定时上线
     */
    @RequestMapping(value = "jxtnewscanceltimeronline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage canceltimeronlineJxtNews() {
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage().setInfo("未选择资讯!");
        }
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews != null) {
            //当前时间
            jxtNews.setPublishTime(0L);
            crmVendorService.$upsertJxtNews(jxtNews);
        }
        return MapMessage.successMessage();
    }


    /**
     * 获取推送文章的当前状态
     */
    @RequestMapping(value = "jxtnewscurrentstatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage comparePushTimeAndPublishTime() {
        String newsIds = getRequestString("newsIds");
        String[] newsIdArray;
        List<String> newsIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(newsIds)) {
            newsIdArray = newsIds.split(",");
            newsIdList = Arrays.asList(newsIdArray);
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, JxtNews> jxtNewsMap = crmVendorService.$loadsJxtNewsMap(newsIdList);
        for (String newsId : newsIdList) {
            JxtNews jxtNews = jxtNewsMap.get(newsId);
            if (jxtNews != null) {
                Map<String, Object> timerResultMap = new HashMap<>();
                if (jxtNews.getPublishTime() != null && jxtNews.getPublishTime() > 0 && !jxtNews.getOnline()) {
                    timerResultMap.put("newsId", jxtNews.getId());
                    timerResultMap.put("online", jxtNews.getOnline());
                    timerResultMap.put("publishTime", DateUtils.dateToString(new Date(jxtNews.getPublishTime())));
                    mapList.add(timerResultMap);
                } else {
                    timerResultMap.put("newsId", jxtNews.getId());
                    timerResultMap.put("online", jxtNews.getOnline());
                    mapList.add(timerResultMap);
                }
            }
        }
        return MapMessage.successMessage().add("result", mapList);
    }


    @RequestMapping(value = "inserttestcomment.vpage", method = RequestMethod.GET)
    public void insertTestComment() {
        String newsId = getRequestString("newsId");
        JxtNewsComment comment = new JxtNewsComment();
        comment.setTypeId(newsId);
        comment.setComment("我是评论我是评论");
        User user = userLoaderClient.loadUser(20001L);
        comment.setUserId(20001L);
        comment.setUserName(user.fetchRealname());
        comment.setUserType(user.getUserType());
        comment.setIsShow(Boolean.FALSE);
        comment.setIsDisabled(Boolean.FALSE);
        jxtNewsServiceClient.saveJxtNewsComment(comment);
    }

    @RequestMapping(value = "jxtnewsoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offlineJxtNews() {
        String newsId = getRequestString("newsId");
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("您要下线的资讯不存在：" + newsId);
        }
        jxtNews = new JxtNews();
        jxtNews.setId(newsId);
        jxtNews.setOnline(false);
        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(jxtNews.getAlbumId());
            if (album != null && CollectionUtils.isNotEmpty(album.getNewsRecordList())) {
                JxtNews finalJxtNews = jxtNews;
                album.setNewsRecordList(album.getNewsRecordList().stream().filter(e -> !StringUtils.equals(e.getNewsId(), finalJxtNews.getId())).collect(Collectors.toList()));
                crmVendorService.$upsertJxtNewsAlbum(album);
            }
        }
        addAdminLog(getCurrentAdminUser().getAdminUserName() + "下线了文章：" + newsId);
        return MapMessage.successMessage();
    }

    /**
     * 上传图片到aliyun
     *
     * @param inputFile
     * @return
     */
    @RequestMapping(value = "/uploadImgToOss.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImgToOss(MultipartFile inputFile) {
        String activityName = "news_video";
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            return uploadFileTo17pmcOss(inputFile, activityName);
        } catch (Exception e) {
            logger.error("视频封面上传失败{}", e);
            return MapMessage.errorMessage("视频封面上传失败");
        }
    }

    /**
     * 推送管理
     */
    @RequestMapping(value = "pushmanage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryPushManage() {
        return "advisory/pushmanage";
    }

    /**
     * 推送管理列表
     */
    @RequestMapping(value = "getPushManageList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getPushManageList() {
        int currentPage = getRequestInt("currentPage");
        List<JxtNewsPushRecord> pushRecordList = crmVendorService.$loadJxtNewsPushRecordList();
        int totalCount = pushRecordList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        pushRecordList = pushRecordList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        int total = pushRecordList.size();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = total > (statIndex + 10) ? statIndex + 10 : total;
        if (statIndex > endIndex) {
            pushRecordList = new ArrayList<>();
        } else {
            pushRecordList = pushRecordList.subList(statIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewsPushRecordMap(pushRecordList);
        return MapMessage.successMessage().add("pushRecordList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }

    /**
     * 新建、编辑推送
     */
    @RequestMapping(value = "pushedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String pushEdit(Model model) {
        String pushRecordId = getRequestString("pushRecordId");
        model.addAttribute("pushRecordId", pushRecordId);
        if (StringUtils.isNotBlank(pushRecordId)) {
            List<Map<String, String>> mapList = new ArrayList<>();
            JxtNewsPushRecord pushRecord = crmVendorService.$loadJxtNewsPushRecord(pushRecordId);
            if (pushRecord != null) {
                if (CollectionUtils.isNotEmpty(pushRecord.getJxtNewsIdList())) {
                    Map<String, String> jxtNewsCoverImgMap = pushRecord.getJxtNewsCoverImgMap();
                    pushRecord.getJxtNewsIdList().forEach(p -> {
                        Map<String, String> map = new HashMap<>();
                        String img = SafeConverter.toString(jxtNewsCoverImgMap.get(p));
                        map.put("newsId", p);
                        map.put("fileName", img);
                        map.put("imgUrl", combineImgUrl(img));
                        mapList.add(map);
                    });
                    model.addAttribute("newsIdAndImgList", mapList);
                }
                model.addAttribute("isSendPush", SafeConverter.toBoolean(pushRecord.getIsSendPush()));
                model.addAttribute("pushContent", SafeConverter.toString(pushRecord.getPushContent(), ""));
                model.addAttribute("subHeading", SafeConverter.toString(pushRecord.getSubHeading(), ""));
                model.addAttribute("duration", SafeConverter.toInt(pushRecord.getDuration()));
                model.addAttribute("startTime", pushRecord.getStartTime());
                model.addAttribute("pushType", pushRecord.generatePushType());
                if (!pushRecord.generatePushType().equals(JxtNewsPushType.REGION.getType())) {
                    model.addAttribute("availableUserId", pushRecord.getAvailableUserId());
                } else {
                    //区域ID和名字
                    if (CollectionUtils.isNotEmpty(pushRecord.getRegionCodeList())) {
                        List<Integer> regionIds = pushRecord.getRegionCodeList();
                        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
                        regionIds = exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList());
                        List<String> regionNames = exRegionMap.values().stream().map(ExRegion::getName).collect(Collectors.toList());
                        //这里再处理一次是怕区域调整的时候这个区域已经不存在了。这样重新编辑的时候就能修正这样的数据了
                        if (CollectionUtils.isNotEmpty(regionIds)) {
                            model.addAttribute("regionIds", StringUtils.join(regionIds, ","));
                            model.addAttribute("regionNames", StringUtils.join(regionNames, ","));
                        }
                    }
                }
            }
        }
        return "advisory/pushedit";
    }

    /**
     * 保存推送
     */
    @RequestMapping(value = "savepushrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage savePushRecord() {
        String pushRecordId = getRequestString("pushRecordId");
        String firstNewsId = getRequestString("firstNewsId");
        String firstNewsImg = getRequestString("firstNewsImg");
        String totalNewsStr = getRequestString("totalNews");
        Long availableUserId = getRequestLong("availableUserId");
        String startTimeStr = getRequestString("startTime");
        Boolean isSendPush = getRequestBool("isSendPush");
        String pushContent = getRequestString("pushContent");
        String subHeading = getRequestString("subHeading");
        Integer duration = getRequestInt("duration", 0);
        Date startTime = DateUtils.stringToDate(startTimeStr, "yyyy-MM-dd HH:mm");

        //只有在需要发送push，并且push时间在编辑时间之后，才检查push时间是否在提交时间之后10分钟
        if (isSendPush && startTime.after(new Date()) && DateUtils.minuteDiff(startTime, new Date()) < 10) {
            return MapMessage.errorMessage("推送时间请至少设置为提交时间之后10分钟");
        }

        String regionIdStr = getRequestString("regionIds");
        Integer pushTypeId = getRequestInt("pushType");
        Map<String, String> totalNewsImg = JsonUtils.fromJsonToMapStringString(totalNewsStr);
        JxtNewsPushType pushType = JxtNewsPushType.ofWithUnKnow(pushTypeId);
        if (pushType == JxtNewsPushType.UNKNOWN) {
            return MapMessage.errorMessage("推送方式错误");
        }
        List<String> jxtNewsIdList = new ArrayList<>();
        Map<String, String> jxtNewsCoverImgMap = new HashMap<>();
        //第一篇的先放进去
        jxtNewsIdList.add(firstNewsId);
        jxtNewsCoverImgMap.put(firstNewsId, firstNewsImg);
        //循环剩下的资讯
        //资讯没有头图。又没有传
        for (String newsId : totalNewsImg.keySet()) {
            String newsImg = totalNewsImg.get(newsId);
            if (StringUtils.isNotBlank(newsId)) {
                jxtNewsIdList.add(newsId);
                if (StringUtils.isBlank(newsImg)) {
                    JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
                    List<String> coverImgList = jxtNews.getCoverImgList();
                    if (CollectionUtils.isEmpty(coverImgList)) {
                        //资讯没有头图。又没有传
                        jxtNewsCoverImgMap.put(newsId, "");
                    } else {
                        jxtNewsCoverImgMap.put(newsId, coverImgList.get(0));
                    }
                } else {
                    jxtNewsCoverImgMap.put(newsId, newsImg);
                }
            }
        }
        JxtNewsPushRecord pushRecord = null;
        if (StringUtils.isNotBlank(pushRecordId)) {
            pushRecord = crmVendorService.$loadJxtNewsPushRecord(pushRecordId);
            if (pushRecord == null) {
                return MapMessage.errorMessage("id为{}的推送不存在", pushRecordId);
            }
            //更新时间
            pushRecord.setUpdateTime(new Date());
        }
        if (pushRecord == null) {
            pushRecord = new JxtNewsPushRecord();
            pushRecord.setOnline(Boolean.FALSE);
        }
        //推送方式
        pushRecord.setPushType(pushType.getType());
        if (pushType == JxtNewsPushType.ALL_USER) {
            pushRecord.setAvailableUserId(0L);
            pushRecord.setRegionCodeList(null);
        } else if (pushType == JxtNewsPushType.SIGNAL_USER) {
            pushRecord.setAvailableUserId(availableUserId);
            pushRecord.setRegionCodeList(null);
        } else {
            pushRecord.setAvailableUserId(null);
            Set<Integer> regionIds = new HashSet<>();
            String[] split = regionIdStr.split(",");

            for (String id : split) {
                regionIds.add(SafeConverter.toInt(id));
            }
            Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
            if (MapUtils.isNotEmpty(exRegionMap)) {
                pushRecord.setRegionCodeList(exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList()));
            }
        }
        pushRecord.setJxtNewsIdList(jxtNewsIdList);
        pushRecord.setJxtNewsCoverImgMap(jxtNewsCoverImgMap);
        pushRecord.setIsSendPush(isSendPush);
        pushRecord.setPushContent(pushContent);
        pushRecord.setSubHeading(subHeading);
        pushRecord.setDuration(duration);
        pushRecord.setStartTime(startTime);
        pushRecord.setIsDisabled(Boolean.FALSE);
        pushRecord.setOnline(Boolean.TRUE);

        pushRecord = crmVendorService.$upsertJxtNewsPushRecord(pushRecord);
        if (pushRecord == null) {
            return MapMessage.errorMessage("保存推送失败");
        } else {
            return MapMessage.successMessage();
        }
    }

    /**
     * 上线推送
     */
    @RequestMapping(value = "onlinepushrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage onlinePushRecord() {
        String pushRecordId = getRequestString("pushRecordId");
        if (StringUtils.isBlank(pushRecordId)) {
            return MapMessage.errorMessage("id不能为空");
        }
        JxtNewsPushRecord pushRecord = crmVendorService.$loadJxtNewsPushRecord(pushRecordId);
        if (pushRecord == null) {
            return MapMessage.errorMessage("id为{}的推送不存在", pushRecordId);
        }
        pushRecord = new JxtNewsPushRecord();
        pushRecord.setId(pushRecordId);
        pushRecord.setOnline(true);
        pushRecord = crmVendorService.$upsertJxtNewsPushRecord(pushRecord);
        return new MapMessage().setSuccess(pushRecord != null);
    }

    /**
     * 下线推送
     */
    @RequestMapping(value = "offlinepushrecord.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage offlinePushRecord() {
        String pushRecordId = getRequestString("pushRecordId");
        if (StringUtils.isBlank(pushRecordId)) {
            return MapMessage.errorMessage("id不能为空");
        }
        JxtNewsPushRecord pushRecord = crmVendorService.$loadJxtNewsPushRecord(pushRecordId);
        if (pushRecord == null) {
            return MapMessage.errorMessage("id为{}的推送不存在", pushRecordId);
        }
        pushRecord = new JxtNewsPushRecord();
        pushRecord.setId(pushRecordId);
        pushRecord.setOnline(false);
        pushRecord = crmVendorService.$upsertJxtNewsPushRecord(pushRecord);
        return new MapMessage().setSuccess(pushRecord != null);
    }

    /**
     * 根据资讯Id获取图片
     */
    @RequestMapping(value = "newsimg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getJxtNewsImg() {
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("id不能为空");
        }
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("id为{}的资讯不存在", newsId);
        }
        String fileName = CollectionUtils.isEmpty(jxtNews.getCoverImgList()) ? "" : jxtNews.getCoverImgList().get(0);
        String imgUrl = combineImgUrl(fileName);
        return MapMessage.successMessage().add("newsId", jxtNews.getId()).add("fileName", fileName).add("imgUrl", imgUrl);
    }


    /**
     * 获取标签列表
     */
    @RequestMapping(value = "gettaglist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTagList() {
        try {
            List<JxtNewsTag> jxtNewsTagList = crmVendorService.$loadJxtNewsTagList()
                    .stream()
                    .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                    .filter(e -> e.getParentId() != 0)
                    .collect(Collectors.toList());
            List<Map<String, Long>> tagList = new ArrayList<>();
            jxtNewsTagList.forEach(e -> {
                Map<String, Long> map = new HashMap<>();
                map.put(e.getTagName(), e.getId());
                tagList.add(map);
            });
            return MapMessage.successMessage().add("tagList", tagList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 获取内容类别列表
     */
    @RequestMapping(value = "getcategorylist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCategoryList() {
        try {
            List<JxtNewsCategory> jxtNewsCategoryList = jxtNewsLoaderClient.findAllCategoriesWithoutDisabled();
            List<Map<String, Long>> categoryList = new ArrayList<>();
            jxtNewsCategoryList.stream()
                    .forEach(e -> {
                        Map<String, Long> map = new HashMap<>();
                        map.put(e.getCategoryName(), e.getId());
                        categoryList.add(map);
                    });
            return MapMessage.successMessage().add("tagList", categoryList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 评论混排-时间倒序
     */
    @RequestMapping(value = "commenttimeline.vpage", method = RequestMethod.GET)
    public String commenttimeline(Model model) {
        Date defaultEndTime = new Date();
        Date defaultStartTime = DateUtils.addHours(defaultEndTime, -24);
        model.addAttribute("startTime", DateUtils.dateToString(defaultStartTime));
        model.addAttribute("endTime", DateUtils.dateToString(defaultEndTime));
        return "advisory/commenttimeline";
    }

    /**
     * 获取最近品论列表
     */
    @RequestMapping(value = "getrecentcomments.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getrecentcomments() {
        boolean isShow = getRequestBool("isShow");
        // endTime默认为当前时间,startTime默认是endTime过去七天
        String startTimeStr = getRequestString("startTime");
        String endTimeStr = getRequestString("endTime");
        Date startTime;
        Date endTime;
        // 测试的时候默认100
        int maxDaysDelta = 100;
        if (RuntimeMode.ge(Mode.TEST)) {
            maxDaysDelta = 3;
        }
        if (StringUtils.isEmpty(endTimeStr)) {
            endTime = new Date();
        } else {
            endTime = DateUtils.stringToDate(endTimeStr);
        }
        if (StringUtils.isEmpty(startTimeStr)) {
            startTime = DateUtils.addHours(endTime, -maxDaysDelta);
        } else {
            startTime = DateUtils.stringToDate(startTimeStr);
        }

        if (DateUtils.dayDiff(endTime, startTime) > maxDaysDelta) {
            return MapMessage.errorMessage("最大只允许查询3天的评论!");
        }
        // 未展示的按评论创建时间排序，
        String sortBy = isShow ? "updateTime" : "createTime";
        try {
            // 只允许查看过去一周的消息
            List<JxtNewsComment> allComments = jxtNewsLoaderClient.getAllCommentsForPeriod(startTime, endTime, sortBy);
            // 在这里过滤isShow和disabled
            allComments = allComments.stream().filter(o1 -> o1.getIsShow().equals(isShow) && !o1.getIsDisabled()).collect(Collectors.toList());
            List<Map<String, Object>> allMapList = generateCommentList(allComments);
            return MapMessage.successMessage().add("allComments", allMapList);
        } catch (Exception e) {
            logger.error("获取家长资讯最新评论失败，msg:{}", e.getMessage(), e);
            return MapMessage.errorMessage("加载最近评论列表失败");
        }
    }

    //获取资讯/推送的投放区域
    @RequestMapping(value = "load_region.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String loadRegionTree() {
        //是资讯还是推送
        String type = getRequestString("type");
        String typeId = getRequestString("typeId");
        List<Integer> regionList;
        if (StringUtils.isBlank(typeId)) {
            regionList = Collections.emptyList();
        } else if ("jxt_news".equals(type)) {
            JxtNews jxtNews = crmVendorService.$loadJxtNews(typeId);
            if (jxtNews == null) {
                return "资讯" + typeId + "不存在";
            }
            if (jxtNews.getRegionCodeList() != null) {
                regionList = jxtNews.getRegionCodeList();
            } else {
                regionList = Collections.emptyList();
            }
        } else if ("jxt_news_push".equals(type)) {
            JxtNewsPushRecord pushRecord = crmVendorService.$loadJxtNewsPushRecord(typeId);
            if (pushRecord == null) {
                return "推送" + typeId + "不存在";
            }
            if (pushRecord.getRegionCodeList() != null) {
                regionList = pushRecord.getRegionCodeList();
            } else {
                regionList = Collections.emptyList();
            }
        } else {
            return "类型错误";
        }
        return generateRegionCodeTree(regionList);

    }

    @RequestMapping(value = "loadallnewssource.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadAllNewsSource() {
        List<ZyParentNewsSource> zyParentNewsSources = parentNewsService.loadAllNewsSource();
        return MapMessage.successMessage().add("zyParentNewsSources", zyParentNewsSources);
    }

    //进入文章源设置页面

    @RequestMapping(value = "view_news_source.vpage", method = RequestMethod.GET)
    public String viewNewsSource() {
        return "advisory/newssourcelist";
    }

    //加载文章源列表
    @RequestMapping(value = "load_news_source.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loadNewsSource() {
        int currentPage = getRequestInt("currentPage");
        String source = getRequestString("source");
        List<ZyParentNewsSource> zyParentNewsSources = parentNewsService.loadAllNewsSource();
        zyParentNewsSources = zyParentNewsSources.stream()
                .filter(p -> StringUtils.isBlank(source) || (StringUtils.isNotBlank(p.getNewsSourceName()) && p.getNewsSourceName().contains(source)))
                .sorted((o1, o2) -> o2.getCreate_datetime().compareTo(o1.getCreate_datetime()))
                .collect(Collectors.toList());
        int totalCount = zyParentNewsSources.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = totalCount > (statIndex + 10) ? statIndex + 10 : totalCount;
        if (statIndex > endIndex) {
            zyParentNewsSources = new ArrayList<>();
        } else {
            zyParentNewsSources = zyParentNewsSources.subList(statIndex, endIndex);
        }
        return MapMessage.successMessage().add("zyParentNewsSources", zyParentNewsSources).add("totalPage", totalPage).add("currentPage", currentPage);

    }

    //编辑文章源
    @RequestMapping(value = "edit_news_source.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage editNewsSource() {
        String id = getRequestString("id");
        String sourceNum = getRequestString("num");
        String sourceName = getRequestString("name");
        String sourceGrade = getRequestString("grade");
        MapMessage mapMessage = parentNewsService.updateNewsSource(id, sourceName, sourceNum, sourceGrade, Boolean.FALSE);
        if (mapMessage.isSuccess()) {
            return MapMessage.successMessage("编辑保存成功");
        }
        return MapMessage.errorMessage("编辑保存失败");
    }

    //删除文章源
    @RequestMapping(value = "delete_news_source.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteNewsSource() {
        String id = getRequestString("sourceId");
        MapMessage mapMessage = parentNewsService.deleteNewsSource(id);
        if (mapMessage.isSuccess()) {
            return MapMessage.successMessage("删除文章源成功");
        }
        return MapMessage.errorMessage("删除文章源失败");
    }

    //添加文章源
    @RequestMapping(value = "insert_news_source.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage insertNewsSource() {
        String sourceNum = getRequestString("num");
        String sourceName = getRequestString("name");
        String sourceGrade = getRequestString("grade");
        ZyParentNewsSource zyParentNewsSource = new ZyParentNewsSource();
        zyParentNewsSource.setNewsSourceNum(sourceNum);
        zyParentNewsSource.setNewsSourceName(sourceName);
        zyParentNewsSource.setSourceGrade(sourceGrade);
        zyParentNewsSource.setDisabled(Boolean.FALSE);
        MapMessage mapMessage = parentNewsService.insertNewsSource(zyParentNewsSource);
        if (mapMessage.isSuccess()) {
            return MapMessage.successMessage("添加文章源成功");
        }
        return MapMessage.errorMessage("添加文章源失败");
    }


    /**
     * 微信公众号抓取配置
     */
    @RequestMapping(value = "wechatcrawleradmin.vpage", method = RequestMethod.GET)
    public String wechatCrawlerAdmin() {
        return "advisory/wechatcrawleradmin";
    }

    /**
     * 加载微信crawlers
     */
    @RequestMapping(value = "loadwechatcrawlers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadwechatcrawlers() {
        int page = getRequestInt("currentPage");
        int size = getRequestInt("size", 10);
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        // 按照时间倒序
        Pageable pageable = new PageRequest(page, size, Sort.Direction.DESC, "_id");
        Page<ZyParentNewsWechatCrawler> crawlers = parentNewsService.loadNextPageWechatCrawler(pageable);
        return MapMessage.successMessage().add("crawlers", crawlers);
    }

    /**
     * 加载微信crawlers
     */
    @RequestMapping(value = "upsertwechatcrawler.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertWechatCrawler() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String biz = getRequestString("biz");
        String uin = getRequestString("uin");
        String key = getRequestString("key");
        String last_crawled_msg_id = getRequestString("last_crawled_msg_id");
        Boolean disabled = getRequestBool("disabled");
        ZyParentNewsWechatCrawler zyParentNewsWechatCrawler = new ZyParentNewsWechatCrawler();
        zyParentNewsWechatCrawler.setName(name);
        zyParentNewsWechatCrawler.setBiz(biz);
        zyParentNewsWechatCrawler.setUin(uin);
        zyParentNewsWechatCrawler.setKey(key);
        zyParentNewsWechatCrawler.setKey_valid(true);
        zyParentNewsWechatCrawler.setRunning(false);
        zyParentNewsWechatCrawler.setError("");
        zyParentNewsWechatCrawler.setLast_crawled_msg_id(last_crawled_msg_id);
        zyParentNewsWechatCrawler.setDisabled(disabled);
        if (!StringUtils.isEmpty(id)) {
            zyParentNewsWechatCrawler.setId(id);
        }
        parentNewsService.upsertWechatCrawler(zyParentNewsWechatCrawler);
        return MapMessage.successMessage();
    }

    /**
     * 发送http请求启动crawler
     */
    @RequestMapping(value = "startcrawler.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage startCrawler() {
        String id = getRequestString("id");
        // 先零时把请求打到wechat teacher
        String wechatUrl = ProductConfig.get("wechat.url");
        String url = wechatUrl + "/teacher/others/startwechatcrawler";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).addParameter("id", id).addParameter("key", "17zy").execute();
        if (response == null || response.hasHttpClientException() || response.getStatusCode() != 200) {
            return MapMessage.errorMessage("启动失败，http错误");
        }
        MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (!resultMap.isSuccess()) {
            return MapMessage.errorMessage("未知错误，请管理员查看日志");
        }
        return MapMessage.successMessage("启动成功");
    }

    /**
     * 发送http请求启动wechat sogou crawler
     */
    @RequestMapping(value = "startwechatsogoucrawler.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage startWechatSogouCrawler() {
        // 先零时把请求打到wechat teacher
        String _id = getRequestString("_id");
        String type = getRequestString("type");
        String wechatUrl = ProductConfig.get("wechat.url");
        String url = wechatUrl + "/teacher/others/startwechatsogoucrawler";
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).addParameter("_id", _id).addParameter("type", type).addParameter("key", "17zy").execute();
            if (response == null || response.hasHttpClientException() || response.getStatusCode() != 200) {
                return MapMessage.errorMessage("启动失败，http错误");
            }
            MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
            if (!resultMap.isSuccess()) {
                return MapMessage.errorMessage("未知错误，请管理员查看日志");
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("启动失败，http错误");
        }
        return MapMessage.successMessage("启动成功");
    }

    //进入内容设置页面
    @RequestMapping(value = "view_tag_list_keywords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewTagListKeywords() {
        return "advisory/newstaglistandkeywords";
    }

    //进入内容设置页面
    @RequestMapping(value = "keywordsmgn.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String keywordsMgn(Model model) {
        String firstId = getRequestString("firstId");
        String secondId = getRequestString("secondId");
        model.addAttribute("firstId", firstId);
        model.addAttribute("secondId", secondId);
        return "advisory/keywordsmgn";
    }

    //load关键字列表
    @RequestMapping(value = "load_tag_keywords.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadTagKeyWords() {
        String secondId = getRequestString("secondId");
        if (StringUtils.isBlank(secondId)) {
            return MapMessage.errorMessage("二级标签id不能为空");
        }
        List<ZyParentNewsTagKeyWords> zyParentNewsTagKeyWords = parentNewsService.loadTagKeyWordsBySecondId(secondId);

        return MapMessage.successMessage().add("keyWordsList", zyParentNewsTagKeyWords);
    }

    private List<Map<String, Object>> getTopTwoLevelTags() {
        List<JxtNewsTag> allTagsWithoutDisabled = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        List<Map<String, Object>> tagMapList = new ArrayList<>();
        for (JxtNewsTag jxtNewsTag : allTagsWithoutDisabled) {
            if (jxtNewsTag.getParentId() == 0) {
                Map<String, Object> map = new HashMap<>();
                List<JxtNewsTag> tagList = allTagsWithoutDisabled.stream().filter(tag -> Objects.equals(tag.getParentId(), jxtNewsTag.getId())).collect(Collectors.toList());
                map.put("parentTagId", jxtNewsTag.getId());
                map.put("parentTagName", jxtNewsTag.getTagName());
                map.put("childList", tagList);
                tagMapList.add(map);
            }
        }
        return tagMapList;
    }

    //load内容列表
    @RequestMapping(value = "load_tag_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadTagList() {
        int currentPage = getRequestInt("currentPage");
        List<Map<String, Object>> tagMapList = getTopTwoLevelTags();
        int totalCount = tagMapList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = totalCount > (statIndex + 10) ? statIndex + 10 : totalCount;
        if (statIndex > endIndex) {
            tagMapList = new ArrayList<>();
        } else {
            tagMapList = tagMapList.subList(statIndex, endIndex);
        }
        return MapMessage.successMessage().add("tagMapList", tagMapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }

    //进入内容设置页面
    @RequestMapping(value = "viewtagtree.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewtagtree(Model model) {
        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(0, "root", jxtNewsTags, true);
        model.addAttribute("tagTree", JsonUtils.toJson(tagTree));
        return "advisory/viewtagtree";
    }

    // load tag tree 加载标签树，返回适应fancy tree的json结构
    @RequestMapping(value = "loadtagtree.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadTagTree() {
        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(0, "root", jxtNewsTags, true);
        return MapMessage.successMessage().add("tagTree", tagTree);
    }

    // add tag 添加标签
    @RequestMapping(value = "addtag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addtag() {
        long parentId = getRequestLong("parentId");
        String name = getRequestString("name");
        JxtNewsTag parentTag = new JxtNewsTag();
        parentTag.setTagName(name);
        parentTag.setParentId(parentId);
        parentTag = crmVendorService.$upsertJxtNewsTag(parentTag);
        MapMessage mapMessage;
        if (parentTag != null) {
            mapMessage = MapMessage.successMessage("添加成功").add("tagId", parentTag.getId());
        } else {
            mapMessage = MapMessage.errorMessage("添加失败");
        }
        return mapMessage;
    }

    // move tag 移动标签
    @RequestMapping(value = "movetag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage moveTag() {
        long fromId = getRequestLong("fromId");
        long toId = getRequestLong("toId");
        JxtNewsTag jxtNewsTag = crmVendorService.$loadJxtNewsTag(fromId);
        if (jxtNewsTag == null) {
            return MapMessage.errorMessage("标签不存在");
        }
        jxtNewsTag.setParentId(toId);
        crmVendorService.$upsertJxtNewsTag(jxtNewsTag);
        return MapMessage.successMessage("移动成功");
    }

    private boolean deleteTree(Map<String, Object> tree) {
        long id = SafeConverter.toLong(tree.get("id"));
        // delete this id
        JxtNewsTag jxtNewsTag = crmVendorService.$loadJxtNewsTag(id);
        if (jxtNewsTag != null) {
            jxtNewsTag.setDisabled(true);
            crmVendorService.$upsertJxtNewsTag(jxtNewsTag);
        }
        // and its children,if has children
        Collection<Map<String, Object>> children = (Collection<Map<String, Object>>) tree.get("children");
        if (children.size() > 0) {
            children.forEach(this::deleteTree);
        }
        return true;
    }

    // delete tag 删除标签
    @RequestMapping(value = "deletetag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletetag() {
        long id = getRequestLong("id");
        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(id, "root", jxtNewsTags, false);
        deleteTree(tagTree);
        return MapMessage.successMessage();
    }

    // 重命名标签
    @RequestMapping(value = "renametag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage renametag() {
        long tagId = getRequestLong("tagId");
        String newTagName = getRequestString("newTagName");
        JxtNewsTag jxtNewsTag = crmVendorService.$loadJxtNewsTag(tagId);
        if (jxtNewsTag == null) {
            return MapMessage.errorMessage("标签id不存在");
        }
        jxtNewsTag.setTagName(newTagName);
        crmVendorService.$upsertJxtNewsTag(jxtNewsTag);
        return MapMessage.successMessage("插入成功");
    }

    //编辑tag
    @RequestMapping(value = "edit_tag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editTag() {
        String parentTagId = getRequestString("parentTagId");
        String parentTagName = getRequestString("parentTagName");
        String secondTagId = getRequestString("secondTagId");
        String secondTagName = getRequestString("secondTagName");
        JxtNewsTag parentTag = crmVendorService.$loadJxtNewsTag(Long.valueOf(parentTagId));
        if (parentTag != null) {
            parentTag.setTagName(parentTagName);
            parentTag = crmVendorService.$upsertJxtNewsTag(parentTag);
        }
        MapMessage mapMessage;
        JxtNewsTag secondTag = crmVendorService.$loadJxtNewsTag(Long.valueOf(secondTagId));
        if (secondTag != null) {
            secondTag.setTagName(secondTagName);
            secondTag = crmVendorService.$upsertJxtNewsTag(secondTag);
        }
        if (parentTag != null && secondTag != null) {
            mapMessage = MapMessage.successMessage("修改标签成功");
        } else {
            mapMessage = MapMessage.errorMessage("修改标签失败");
        }
        return mapMessage;
    }

    //添加一级内容
    @RequestMapping(value = "add_parent_tag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addParentTag() {
        String parentTagName = getRequestString("parentTagName");
        JxtNewsTag parentTag = new JxtNewsTag();
        parentTag.setTagName(parentTagName);
        parentTag.setParentId(0L);
        parentTag = crmVendorService.$upsertJxtNewsTag(parentTag);
        MapMessage mapMessage;
        if (parentTag != null) {
            mapMessage = MapMessage.successMessage("添加一级内容类型成功");
        } else {
            mapMessage = MapMessage.errorMessage("添加一级内容类型失败");
        }
        return mapMessage;
    }

    //添加二级内容
    @RequestMapping(value = "add_second_tag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSecondTag() {
        String parentTagId = getRequestString("parentTagId");
        String secondTagName = getRequestString("secondTagName");
        JxtNewsTag secondTag = new JxtNewsTag();
        secondTag.setTagName(secondTagName);
        secondTag.setParentId(Long.valueOf(parentTagId));
        secondTag = crmVendorService.$upsertJxtNewsTag(secondTag);
        MapMessage mapMessage;
        if (secondTag != null) {
            mapMessage = MapMessage.successMessage("添加一级内容类型成功");
        } else {
            mapMessage = MapMessage.errorMessage("添加一级内容类型失败");
        }
        return mapMessage;
    }

    //删除tag
    @RequestMapping(value = "del_tag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delTag() {
        String parentTagId = getRequestString("tagId");
        long id = SafeConverter.toLong(parentTagId, -1);
        if (id == -1) {
            return MapMessage.errorMessage();
        }
        JxtNewsTag tag = crmVendorService.$loadJxtNewsTag(id);
        ;
        if (tag == null) {
            return MapMessage.errorMessage();
        }

        tag = new JxtNewsTag();
        tag.setId(id);
        tag.setDisabled(true);
        tag = crmVendorService.$upsertJxtNewsTag(tag);

        MapMessage mapMessage;
        if (tag != null) {
            mapMessage = MapMessage.successMessage("删除标签成功");
        } else {
            mapMessage = MapMessage.errorMessage("删除标签失败");
        }
        return mapMessage;
    }

    //增加或者删除标签后，重新给抓取的文章打标签
    private void freshArticleTagsAfterAddTag() {
        List<Map<String, Object>> tagMapList = getTopTwoLevelTags();
        List<ZyParentNewsTagKeyWords> keywordTags = parentNewsService.loadAllTagKeywords();
        // 关键词->一级标签对照表，一对多
        Map<String, Set<String>> firstLevelKeywordTagMap = new HashMap<>();
        // 关键词->二级标签对照表，一对多
        Map<String, Set<String>> secondLevelKeywordTagMap = new HashMap<>();
        for (ZyParentNewsTagKeyWords keywordTag : keywordTags) {
            String keyword = keywordTag.getKeyword();
            String firstId = keywordTag.getFirstId();
            String secondId = keywordTag.getSecondId();
            if (firstLevelKeywordTagMap.containsKey(keyword)) {
                firstLevelKeywordTagMap.get(keyword).add(firstId);
            } else {
                Set<String> tags = new HashSet<>();
                tags.add(firstId);
                firstLevelKeywordTagMap.put(keyword, tags);
            }
            if (secondLevelKeywordTagMap.containsKey(keyword)) {
                secondLevelKeywordTagMap.get(keyword).add(secondId);
            } else {
                Set<String> tags = new HashSet<>();
                tags.add(secondId);
                secondLevelKeywordTagMap.put(keyword, tags);
            }
        }
        // 每次一万个，分批把整个文章都更改一遍
        int shard = 100;
        int page = 0;
        while (true) {
            Pageable pageable = new PageRequest(page, shard, Sort.Direction.DESC, "publish_datetime");
            Page<ZyParentNewsRawArticle> rawArticlesPage = parentNewsService.traverseRawArticleByPage(pageable);
            for (ZyParentNewsRawArticle zyParentNewsRawArticle : rawArticlesPage.getContent()) {
                String title = zyParentNewsRawArticle.getTitle();
                Set<String> firstLevelTags = new HashSet<>();
                Set<String> secondLevelTags = new HashSet<>();
                for (String keyword : secondLevelKeywordTagMap.keySet()) {
                    if (title.contains(keyword)) {
                        // 命中关键字
                        firstLevelTags.addAll(firstLevelKeywordTagMap.get(keyword));
                        secondLevelTags.addAll(secondLevelKeywordTagMap.get(keyword));
                    }
                }
                // upsert
                logger.debug("update raw article tags");
                zyParentNewsRawArticle.setFirstLevelTagIds(firstLevelTags.stream().collect(Collectors.toList()));
                zyParentNewsRawArticle.setSecondLevelTagIds(secondLevelTags.stream().collect(Collectors.toList()));
                parentNewsService.upsertRawArticle(zyParentNewsRawArticle);
            }
            if (!rawArticlesPage.hasNext()) {
                logger.debug("no more raw articles");
                // 没有了，直接跳出
                break;
            } else {
                logger.debug("current page is " + page);
                // page + 1
                page += 1;
            }
        }
    }

    //添加关键词
    @RequestMapping(value = "add_tag_keywords.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTagKeyWords() {
        String firstId = getRequestString("firstId");
        String secondId = getRequestString("secondId");
        String keyWord = getRequestString("keyWord");
        MapMessage mapMessage = parentNewsService.saveTagKeyWords(firstId, secondId, keyWord);
        // 添加keywords后增加一个后处理程序，去更改现有的文章的标签
//        utopiaThreadPool.submit(() -> {
//            freshArticleTagsAfterAddTag();
//        });
        if (mapMessage.isSuccess()) {
            return MapMessage.successMessage("添加关键词成功");
        }
        return MapMessage.errorMessage("添加关键词失败");
    }

    //删除关键词
    @RequestMapping(value = "del_tag_keywords.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delTagKeyWords() {
        String secondId = getRequestString("secondId");
        String delKeyWords = getRequestString("delKeyWords");
        String[] delKeyWordsArray = delKeyWords.split(",");
        List<String> delKeyWordsList = Arrays.asList(delKeyWordsArray);
        for (String delKeyWord : delKeyWordsList) {
            boolean success = parentNewsService.deleteTagKeywordById(delKeyWord);
        }
        // 删除keywords后增加一个后处理程序，去更改现有的文章的标签
//        utopiaThreadPool.submit(() -> {
//            freshArticleTagsAfterAddTag();
//        });
        return MapMessage.successMessage();
    }

    // 当关键词树改变后，人工手动点击刷新标签列表
    @RequestMapping(value = "refreshkeywordtags.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage refreshkeywordtags() {
        AlpsThreadPool.getInstance().submit(() -> {
            freshArticleTagsAfterAddTag();
        });
        return MapMessage.successMessage();
    }


    /**
     * stop wechat sogou crawler
     */
    @RequestMapping(value = "stopwechatsogoucrawler.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage stopWechatSogouCrawler() {
        //todo 使用信号去停止scrapy spider
        String _id = getRequestString("_id");
        String type = getRequestString("type");
        if (StringUtils.equals("wechat_token", type)) {
            ZyParentNewsWechatCrawler zyParentNewsWechatCrawler = parentNewsService.loadWechatCrawlerById(_id);
            zyParentNewsWechatCrawler.setRunning(false);
            parentNewsService.upsertWechatCrawler(zyParentNewsWechatCrawler);
        }
        return MapMessage.successMessage();
    }

    //按tag查询资讯数量的页面
    @RequestMapping(value = "tagNewsCount.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String tagNewsCount() {
        return "advisory/jxttagnewslist";
    }


    //按tag查询资讯数量的方法
    @RequestMapping(value = "loadNewsCountByTag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadNewsCountByTag() {
        int page = getRequestInt("currentPage");
        page = page < 1 ? 1 : page;
        VersionedJxtNewsTagList versionedJxtNewsTagList = jxtNewsLoaderClient.loadVersionedJxtNewsTagList(0);
        List<JxtNewsTag> jxtNewsTagList = versionedJxtNewsTagList.getJxtNewsTagList();
        List<Map<String, Object>> tagNewsCount = new ArrayList<>();
        for (JxtNewsTag j : jxtNewsTagList) {
            List<JxtNews> jxtNewsListByTag = jxtNewsLoaderClient.getJxtNewsListByTag(j.getId());
            Map<String, Object> tagNewsMap = new HashMap<>();
            tagNewsMap.put("tagId", j.getId());
            tagNewsMap.put("tagName", j.getTagName());
            tagNewsMap.put("tagNewsNum", jxtNewsListByTag.size());
            tagNewsCount.add(tagNewsMap);
        }

        Pageable pageable = new PageRequest(page, 10);
        Page<Map<String, Object>> listToPage = PageableUtils.listToPage(tagNewsCount, pageable);
        return MapMessage.successMessage().add("tagNewsCount", listToPage.getContent()).add("currentPage", page).add("totalPage", listToPage.getTotalPages());
    }

    /**
     * 频道管理
     */
    @RequestMapping(value = "channelsmgr.vpage", method = {RequestMethod.GET})
    public String channelsmgr(Model model) {
        List<JxtNewsTag> jxtNewsTags = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .collect(Collectors.toList());
        // 自上而下产生树
        Map<String, Object> tagTree = generateTagTree(0, "root", jxtNewsTags, true);
        model.addAttribute("tags", JsonUtils.toJson(jxtNewsTags));
        model.addAttribute("tagTree", JsonUtils.toJson(tagTree));
        return "advisory/channelsmgr";
    }

    /**
     * 获取所有频道
     */
    @RequestMapping(value = "loadchannels.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loadchannels() {
        List<JxtNewsChannel> jxtNewsChannels = crmVendorService.loadJxtNewsEnabledChannel();
        // sort by rank
        Collections.sort(jxtNewsChannels, (o1, o2) -> {
            long rc1 = ConversionUtils.toLong(o1.getRank());
            long rc2 = ConversionUtils.toLong(o2.getRank());
            return Long.compare(rc1, rc2);
        });
        return MapMessage.successMessage().add("channels", jxtNewsChannels);
    }

    /**
     * upsert频道
     */
    @RequestMapping(value = "upsertchannel.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage upsertchannel() {
        String channelString = getRequestString("channel");
        Map<String, Object> channelJson = JsonUtils.fromJson(channelString);
        JxtNewsChannel jxtNewsChannel = JxtNewsChannel.init(channelJson);
        // validate data
        if (jxtNewsChannel.getName().length() > 4) {
            return MapMessage.errorMessage("频道名过长");
        }
        if (jxtNewsChannel.getName().length() < 2) {
            return MapMessage.errorMessage("频道名过短");
        }
        boolean exists = crmVendorService.isChannelRankExists(jxtNewsChannel);
        if (exists) {
            return MapMessage.errorMessage("数序位置重复，请检查！");
        }
        String editor = getCurrentAdminUser().getAdminUserName();
        jxtNewsChannel.setEditor(editor);
        jxtNewsChannel = crmVendorService.upsertJxtNewsChannel(jxtNewsChannel);
        return MapMessage.successMessage().add("id", jxtNewsChannel);
    }


    /**
     * 专题管理
     */
    @RequestMapping(value = "subjectmanage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisorySubjectManage() {
        return "advisory/subjectmanage";
    }

    /**
     * 获取专题列表
     */
    @RequestMapping(value = "getSubjectList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getSubjectManageList() {
        int currentPage = getRequestInt("currentPage");
        if (currentPage == 0) {
            currentPage = 1;
        }
        boolean published = getRequestBool("published");
        List<JxtNewsSubject> subjectList = crmVendorService.$loadJxtNewsSubjectList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()) && published == SafeConverter.toBoolean(e.getPublished()))
                .collect(Collectors.toList());
        int totalCount = subjectList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        subjectList = subjectList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        int total = subjectList.size();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = total > (statIndex + 10) ? statIndex + 10 : total;
        if (statIndex > endIndex) {
            subjectList = new ArrayList<>();
        } else {
            subjectList = subjectList.subList(statIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewsSubjectMap(subjectList);
        return MapMessage.successMessage().add("subjectList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }

    /**
     * 编辑专题
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "subjectedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String subjectEdit(Model model) {
        String subjectId = getRequestString("subjectId");
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(subjectId)) {
            JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
            if (subject == null) {
                getAlertMessageManager().addMessageError("专题" + subjectId + "不存在");
                return "advisory/subjectedit";
            }
            map = generateSubjectInfoMap(subject);
        }
        model.addAttribute("subjectInfo", map);
        return "advisory/subjectedit";
    }

    /**
     * 获取专题中包含的资讯title
     *
     * @return
     */
    @RequestMapping(value = "getJxtNewsTitle.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getJxtNewsTitle() {
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) return MapMessage.errorMessage("参数错误");

        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);

        if (null == jxtNews) return MapMessage.errorMessage("资讯未查到");

        return MapMessage.successMessage().add("title", jxtNews.getTitle()).add("free", jxtNews.getFree());
    }

    /**
     * 保存专题
     *
     * @return
     */
    @RequestMapping(value = "saveSubject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSubject() {
        String subjectId = getRequestString("subjectId");
        String title = getRequestString("title");
        String headImg = getRequestString("headImg");
        String introduction = getRequestString("introduction");
        String categoryRank = getRequestString("categoryRank");
        String categoryNews = getRequestString("categoryNews");
        String newsRank = getRequestString("newsRank");
        Integer advertisementType = getRequestInt("advertisementType");
        String adList = getRequestString("adList");

        JxtNewsSubject subject = null;
        if (StringUtils.isNotBlank(subjectId)) {
            subject = crmVendorService.$loadJxtNewsSubject(subjectId);
            if (subject == null) {
                return MapMessage.errorMessage("id为{}的专题不存在", subjectId);
            }
        }
        if (subject == null) {
            subject = new JxtNewsSubject();
            subject.setDisabled(Boolean.TRUE);
        }
        //标题
        subject.setTitle(title);
        //头图
        subject.setHeadImg(headImg);
        //引言
        subject.setIntroduction(introduction);
        //分类-排序
        Map<String, Integer> categoryRankMap = JsonUtils.fromJsonToMap(categoryRank, String.class, Integer.class);
        subject.setCategoryRankMap(categoryRankMap);
        //分类-文章Id
        Map<String, List<String>> categoryNewsMap = toListString(JsonUtils.fromJsonToMap(categoryNews, String.class, List.class));
        subject.setCategoryNewsMap(categoryNewsMap);
        //文章-排序
        Map<String, Integer> newsRankMap = JsonUtils.fromJsonToMap(newsRank, String.class, Integer.class);
        subject.setNewsRankMap(newsRankMap);
        //广告类型
        subject.setAdvertisementType(advertisementType);
        //广告图片
        if (StringUtils.isNotBlank(adList)) {
            List<Map<String, String>> advertisementList = new ArrayList<>();
            String[] adMaps = adList.split("#");
            for (String adMap : adMaps) {
                Map<String, String> map = JsonUtils.fromJsonToMap(adMap, String.class, String.class);
                advertisementList.add(map);
            }
            subject.setAdList(advertisementList);
        } else {
            subject.setAdList(new ArrayList<>());
        }

        String editor = getCurrentAdminUser().getAdminUserName();
        subject.setEditor(editor);

        subject.setDisabled(Boolean.FALSE);
        //如果subjectId为空说明是首次存储。发布标志置为false
        if (StringUtils.isBlank(subjectId)) {
            subject.setPublished(Boolean.FALSE);
        }

        subject = crmVendorService.$upsertJxtNewsSubject(subject);
        if (subject == null) {
            return MapMessage.errorMessage();
        }

        return MapMessage.successMessage();
    }

    /**
     * 删除专题
     *
     * @return
     */
    @RequestMapping(value = "deletesubject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteSubject() {
        String subjectId = getRequestString("subjectId");
        if (StringUtils.isBlank(subjectId)) {
            return MapMessage.errorMessage("subjectId为空");
        }
        JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
        if (subject == null) {
            return MapMessage.errorMessage("subjectId为{}的专题不存在", subjectId);
        }
        subject.setDisabled(Boolean.TRUE);
        subject = crmVendorService.$upsertJxtNewsSubject(subject);
        if (subject == null) {
            return MapMessage.errorMessage("删除专题失败");
        }
        return MapMessage.successMessage();
    }

    /**
     * 新建、编辑专题资讯
     */
    @RequestMapping(value = "jxtnewssubjectedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String jxtNewsSubjectEdit(Model model) {
        String subjectId = getRequestString("subjectId");
        String newsId = getRequestString("newsId");
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(newsId)) {
            JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
            map = generateJxtNewsSubjectInfoMap(jxtNews);
        } else if (StringUtils.isNotBlank(subjectId)) {
            map = new HashMap<>();
            JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
            if (subject == null) {
                getAlertMessageManager().addMessageError("专题" + subjectId + "不存在");
                return "advisory/jxtnewssubjectedit";
            }
            map.put("subjectId", subjectId);
            map.put("title", subject.getTitle());
            map.put("headContent", SafeConverter.toString(subject.getHeadContent(), ""));
        }
        Map<String, String> newsTypeMap = new HashMap<>();
        for (JxtNewsType type : JxtNewsType.values()) {
            if (type != JxtNewsType.UNKNOWN && type != JxtNewsType.TEXT) {
                newsTypeMap.put(type.name(), type.getDesc());
            }
        }
        map.put("totalType", newsTypeMap);
        model.addAttribute("jxtNewsInfo", map);
        return "advisory/jxtnewssubjectedit";
    }

    /**
     * 获取专题头部或者尾部信息
     *
     * @return
     */
    @RequestMapping(value = "getpubliccontent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getPublicContent() {
        String contentType = getRequestString("contentType");
        String subjectId = getRequestString("subjectId");
        JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
        if (subject == null) {
            return MapMessage.errorMessage("专题" + subjectId + "不存在");
        } else {
            if ("headContent".equals(contentType)) {
                return MapMessage.successMessage().add("content", SafeConverter.toString(subject.getHeadContent(), ""));
            } else if ("tailContent".equals(contentType)) {
                return MapMessage.successMessage().add("content", SafeConverter.toString(subject.getTailContent(), ""));
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 存储专题头部或尾部信息
     *
     * @return
     */
    @RequestMapping(value = "savepubliccontent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage savePublicContent() {
        String contentType = getRequestString("contentType");
        String content = getRequestString("content");
        String subjectId = getRequestString("subjectId");
        JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
        if (subject == null) {
            return MapMessage.errorMessage("专题" + subjectId + "不存在");
        } else {
            if ("headContent".equals(contentType)) {
                subject.setHeadContent(content);
            } else if ("tailContent".equals(contentType)) {
                subject.setTailContent(content);
            }
            subject = crmVendorService.$upsertJxtNewsSubject(subject);
            if (subject == null) {
                return MapMessage.errorMessage();
            }

            //更新头部或者尾部信息之后，要设置专题下所有文章的updateTime，这样从cdn取值，才能取到新的内容
            List<String> newsIdList = subject.getNewsRankMap().keySet().stream().collect(Collectors.toList());
            MapMessage mapMessage = crmVendorService.$updateJxtNewsByIds(newsIdList);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage("更新专题下文章的updateTime失败");
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 保存专题资讯
     *
     * @return
     */
    @RequestMapping(value = "savesubjectjxtnews.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSubjectJxtNews() {
        String newsId = getRequestString("newsId");

        String title = getRequestString("title");
        String subjectId = getRequestString("subjectId");
        String imgStr = getRequestString("imgStr");
        String tagStr = getRequestString("tagStr");
        Long availableUserId = getRequestLong("availableUserId");
        String newsType = getRequestString("newsType");
        Integer pushTypeId = getRequestInt("pushType");
        String regionIdStr = getRequestString("regionIds");
        JxtNewsPushType pushType = JxtNewsPushType.ofWithUnKnow(pushTypeId);
        if (pushType == JxtNewsPushType.UNKNOWN) {
            return MapMessage.errorMessage("推送方式错误");
        }
        List<String> imgList = new ArrayList<>();
        String[] imgSplit = imgStr.split(",");
        for (String s : imgSplit) {
            if (StringUtils.isNotBlank(s)) {
                imgList.add(s);
            }
        }
        List<Long> tagList = new ArrayList<>();
        String[] tagSplit = tagStr.split(",");
        for (String s : tagSplit) {
            if (StringUtils.isNotBlank(s)) {
                tagList.add(SafeConverter.toLong(s));
            }

        }

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        JxtNews jxtNews = null;
        if (StringUtils.isNotBlank(newsId)) {
            jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews == null) {
                return MapMessage.errorMessage("id为{}的资讯不存在", newsId);
            }
            jxtNews.setUpdateTime(new Date());
        }
        if (jxtNews == null) {
            jxtNews = new JxtNews();
            //编辑操作不改变上下线状态
            jxtNews.setOnline(Boolean.FALSE);
        }
        //推送方式
        jxtNews.setPushType(pushType.getType());
        if (pushType == JxtNewsPushType.ALL_USER) {
            jxtNews.setAvailableUserId(0L);
            jxtNews.setRegionCodeList(null);
        } else if (pushType == JxtNewsPushType.SIGNAL_USER) {
            jxtNews.setAvailableUserId(availableUserId);
            jxtNews.setRegionCodeList(null);
        } else {
            jxtNews.setAvailableUserId(null);
            Set<Integer> regionIds = new HashSet<>();
            String[] split = regionIdStr.split(",");

            for (String id : split) {
                regionIds.add(SafeConverter.toInt(id));
            }
            Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
            if (MapUtils.isNotEmpty(exRegionMap)) {
                jxtNews.setRegionCodeList(exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList()));
            }
        }

        jxtNews.setTitle(title);
        jxtNews.setArticleId(subjectId);
        jxtNews.setCoverImgList(imgList);
        jxtNews.setTagList(tagList);
        jxtNews.setOperateUserName(adminUser.getAdminUserName());
        jxtNews.setJxtNewsType(JxtNewsType.parse(newsType));
        jxtNews.setJxtNewsContentType(JxtNewsContentType.SUBJECT);

        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (jxtNews == null) {
            return MapMessage.errorMessage();
        }
        String id = jxtNews.getId();
        // 设置专题已发布
        JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(subjectId);
        subject.setPublished(Boolean.TRUE);
        subject.setNewsId(id);
        subject = crmVendorService.$upsertJxtNewsSubject(subject);
        if (subject == null) {
            return MapMessage.errorMessage();
        }

        return MapMessage.successMessage();
    }

    /**
     * 专题内容发布
     */
    @RequestMapping(value = "jxtnewssubjectlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String jxtNewsSubjectPush() {
        return "advisory/jxtnewssubjectlist";
    }

    /**
     * 获取专题内容发布列表
     */
    @RequestMapping(value = "getjxtnewssubjectList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getJxtNewsSubjectList() {
        int currentPage = getRequestInt("currentPage");
        if (currentPage == 0) {
            currentPage = 1;
        }
        List<JxtNews> jxtNewsList = crmJxtNewsDao.loadAllFromSecondary();
        jxtNewsList = jxtNewsList.stream()
                .filter(e -> JxtNewsContentType.SUBJECT.equals(e.getJxtNewsContentType()))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        int totalCount = jxtNewsList.size();
        double totalPage = new BigDecimal(totalCount).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        int startIndex = (currentPage - 1) * 10;

        int endIndex = totalCount > (startIndex + 10) ? startIndex + 10 : totalCount;
        if (startIndex > endIndex) {
            jxtNewsList = new ArrayList<>();
        } else {
            jxtNewsList = jxtNewsList.subList(startIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewsSubjectCountMap(jxtNewsList);
        return MapMessage.successMessage().add("jxtNewsList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }

    /**
     * 专辑管理
     */
    @RequestMapping(value = "albumManage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advisoryAlbumManage() {
        return "advisory/jxtnewsalbumlist";
    }

    /**
     * 获取专辑列表
     */
    @RequestMapping(value = "getAlbumList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAlbumManageList() {
        int currentPage = getRequestInt("currentPage");
        String status = getRequestString("status");
        String pushUser = getRequestString("pushUser");
        String type = getRequestString("type");
        String albumType = getRequestString("albumType");
        String albumId = getRequestString("id");
        String tags = getRequestString("tags");
        String contentType = getRequestString("albumContentType");
        if (currentPage == 0) {
            currentPage = 1;
        }
        List<Long> tagList = new ArrayList<>();
        if (StringUtils.isNotBlank(tags)) {
            tagList = JsonUtils.fromJsonToList(tags, Long.class);
        }
        List<JxtNewsAlbum> albumList = crmVendorService.$loadJxtNewsAlbumList();
        List<Long> finalTagList = tagList;
        albumList = albumList.stream()
                .filter(a -> StringUtils.isBlank(pushUser) || (StringUtils.isNotBlank(a.getEditor()) && StringUtils.equals(a.getEditor(), pushUser)))
                .filter(a -> StringUtils.isBlank(type) || ("free".equals(type) && (null == a.getFree() || a.getFree())) || ("unfree".equals(type) && null != a.getFree() && !a.getFree()))
                .filter(a -> StringUtils.isBlank(albumType) || (JxtNewsAlbumType.parse(albumType) == a.getJxtNewsAlbumType()))
                .filter(a -> StringUtils.isBlank(contentType) || (a.getJxtNewsAlbumContentType() != null && JxtNewsAlbumContentType.valueOf(contentType) == a.getJxtNewsAlbumContentType()))
                .filter(a -> StringUtils.isBlank(albumId) || (StringUtils.isNotBlank(albumId) && StringUtils.equals(a.getId(), albumId)))
                .filter(a -> CollectionUtils.isEmpty(finalTagList) || (CollectionUtils.isNotEmpty(finalTagList) && a.getTagList().stream().anyMatch(o -> finalTagList.stream().anyMatch(e -> Objects.equals(e, o)))))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        if (!StringUtils.equals(status, "all")) {
            boolean statusBool = StringUtils.equals(status, "online");
            albumList = albumList.stream().filter(p -> p.getOnline() == statusBool).collect(Collectors.toList());
        }
        int total = albumList.size();
        double totalPage = new BigDecimal(total).divide(new BigDecimal(10), BigDecimal.ROUND_UP).intValue();
        int statIndex = (currentPage - 1) * 10;

        int endIndex = total > (statIndex + 10) ? statIndex + 10 : total;
        if (statIndex > endIndex) {
            albumList = new ArrayList<>();
        } else {
            albumList = albumList.subList(statIndex, endIndex);
        }
        List<Map<String, Object>> mapList = generateJxtNewsAlbumMap(albumList);
        return MapMessage.successMessage().add("albumList", mapList).add("totalPage", totalPage).add("currentPage", currentPage);
    }


    /**
     * 编辑专辑
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "albumedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String albumEdit(Model model) {
        String albumId = getRequestString("albumId");
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(albumId)) {
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
            if (album == null) {
                getAlertMessageManager().addMessageError("专辑" + albumId + "不存在");
                return "advisory/jxtnewsalbumedit";
            }
            map = generateAlbumInfoMap(album);
        }
        //查出点读机产品列表
        List<OrderProduct> products = userOrderLoaderClient.loadAvailableProductForCrm();
        products = products.stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == OrderProductServiceType.PicListen).collect(Collectors.toList());

        List<Map<String, Object>> productInfos = new ArrayList<>();
        products.forEach(p -> {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("id", p.getId());
            productInfo.put("name", p.getName());
            productInfos.add(productInfo);
        });
        model.addAttribute("products", productInfos);

        model.addAttribute("albumInfo", map);
        return "advisory/jxtnewsalbumedit";
    }

    /**
     * 校验文章是否正确
     *
     * @return
     */
    @RequestMapping(value = "checkNewsExist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkNewsExist() {
        String newsRankMap = getRequestString("newsRankMap");
        Map<String, String> newsRank = JsonUtils.fromJsonToMapStringString(newsRankMap);
        List<String> jxtNewsIds = new ArrayList<>();
        if (MapUtils.isNotEmpty(newsRank)) {
            jxtNewsIds.addAll(newsRank.keySet().stream().filter(newsId -> StringUtils.isNotBlank(newsId) && crmVendorService.$loadJxtNews(newsId) == null).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(jxtNewsIds)) {
            return MapMessage.errorMessage().add("notExist", Boolean.TRUE).add("newsIds", jxtNewsIds);
        } else {
            return MapMessage.successMessage().add("notExist", Boolean.FALSE);
        }
    }

    /**
     * 校验文章是否上线
     *
     * @return
     */
    @RequestMapping(value = "checkNewsOnline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkNewsOnline() {
        String newsRankMap = getRequestString("newsRankMap");
        Map<String, String> newsRank = JsonUtils.fromJsonToMapStringString(newsRankMap);
        List<String> jxtNewsIds = new ArrayList<>();
        if (MapUtils.isNotEmpty(newsRank)) {
            jxtNewsIds.addAll(newsRank.keySet().stream()
                    .filter(newsId -> {
                        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
                        return null != jxtNews && !jxtNews.getOnline();
                    }).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(jxtNewsIds)) {
            return MapMessage.errorMessage().add("isNotOnline", Boolean.TRUE).add("newsIds", jxtNewsIds);
        } else {
            return MapMessage.successMessage().add("isNotOnline", Boolean.FALSE);
        }
    }


    /**
     * 校验专辑Id是否正确
     *
     * @return
     */
    @RequestMapping(value = "checkAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkAlbum() {
        String albumId = getRequestString("albumId");
        JxtNewsAlbum result = null;
        if (StringUtils.isNotBlank(albumId)) {
            result = crmVendorService.$loadJxtNewsAlbum(albumId);
        }
        if (result != null) {
            return MapMessage.successMessage().add("albumTitle", result.getTitle());
        } else {
            return MapMessage.errorMessage();
        }
    }

    /**
     * 校验文章是否已经在某专辑内
     *
     * @return
     */
    @RequestMapping(value = "checkNewsInAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkNewsInAlbum() {
        String albumId = getRequestString("albumId");
        String newsRankMap = getRequestString("newsRankMap");
        Map<String, String> newsRank = JsonUtils.fromJsonToMapStringString(newsRankMap);
        List<String> jxtNewsIds = new ArrayList<>();
        if (MapUtils.isNotEmpty(newsRank)) {
            jxtNewsIds.addAll(newsRank.keySet().stream()
                    .filter(newsId -> {
                        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
                        return null != jxtNews && StringUtils.isNotBlank(jxtNews.getAlbumId()) && !StringUtils.equals(jxtNews.getAlbumId(), albumId);
                    }).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(jxtNewsIds)) {
            return MapMessage.errorMessage().add("isAlbum", Boolean.TRUE).add("newsIds", jxtNewsIds);
        } else {
            return MapMessage.successMessage().add("isAlbum", Boolean.FALSE);
        }
    }


    /**
     * 校验专辑名称是否重复
     *
     * @return
     */
    @RequestMapping(value = "checkAlbumTitle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkAlbumTitle() {
        String albumTitle = getRequestString("title");
        Boolean flag = Boolean.FALSE;
        if (StringUtils.isNotBlank(albumTitle)) {
            List<JxtNewsAlbum> jxtNewsAlbumList = crmVendorService.$loadJxtNewsAlbumList();
            flag = jxtNewsAlbumList.stream().anyMatch(p -> StringUtils.equals(albumTitle, p.getTitle()));
        }
        return MapMessage.successMessage().add("flag", flag);
    }


    //检查免费专辑里是否存在付费资讯
    @RequestMapping(value = "checkNewsFree.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkNewsFree() {
        String newsRankMap = getRequestString("newsIds");
        if (StringUtils.isBlank(newsRankMap)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            List<String> ids = JsonUtils.fromJsonToList(newsRankMap, String.class);
            for (String id : ids) {
                JxtNews jxtNews = crmVendorService.$loadJxtNews(id);
                if (null != jxtNews.getFree() && !jxtNews.getFree()) {
                    return MapMessage.errorMessage("免费专辑里不能添加付费资讯");
                }
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 保存专辑
     *
     * @return
     */
    @RequestMapping(value = "saveAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAlbum() {
        String albumId = getRequestString("albumId");
        String title = getRequestString("title");
        String headImg = getRequestString("headImg");
        String bigImg = getRequestString("bigImg");
        String author = getRequestString("author");
        String detail = getRequestString("detail");
        String tagStr = getRequestString("tagStr");
        String newsRank = getRequestString("newsRank");
        String innerType = getRequestString("innerType");
        String type = getRequestString("type");
        String price = getRequestString("price");
        String originalPrice = getRequestString("originalPrice");
        String productId = getRequestString("productId");
        String albumType = getRequestString("albumType");
        String mizarUserName = getRequestString("mizarUserName");
        List<MizarUser> mizarUserList = mizarUserLoaderClient.loadAllUsers();
        MizarUser mizarUser = mizarUserList.stream().filter(p -> StringUtils.equals(mizarUserName, p.getAccountName())).findFirst().orElse(null);
        String dayStr = getRequestString("dayStr");
        dayStr = dayStr.replaceAll("\n|\r|\t", "").trim();
        String[] dayStrArray = dayStr.split(",");
        List<String> dayStrList = Arrays.asList(dayStrArray);
        String updateAlbumTime = getRequestString("updateAlbumTime");
        String subTitle = getRequestString("subTitle");
        String albumContentType = getRequestString("albumContentType");
        if (StringUtils.isBlank(type)) {
            return MapMessage.errorMessage("请选择专辑类型");
        }
        if (StringUtils.isBlank(albumContentType)) {
            return MapMessage.errorMessage("请选择专辑的内容类型");
        }
        if ("unfree".equals(type)) {
            if (SafeConverter.toDouble(price) <= 0) {
                return MapMessage.errorMessage("请输入正确的价格");
            }
            if (StringUtils.isNotBlank(originalPrice) && (SafeConverter.toDouble(originalPrice) <= 0 || SafeConverter.toDouble(originalPrice) <= SafeConverter.toDouble(price))) {
                return MapMessage.errorMessage("原价格需高于实际价格");
            }
            if (null == productId) {
                return MapMessage.errorMessage("请选择产品类型");
            }
        }
        //校验选了外部资讯。就必须有mizar用户
        JxtNewsAlbumType jxtNewsAlbumType = JxtNewsAlbumType.parse(albumType);
        String mizarUserId = "";
        if (jxtNewsAlbumType == JxtNewsAlbumType.UNKNOWN) {
            return MapMessage.errorMessage("专辑类型不正确");
        }
        if (jxtNewsAlbumType == JxtNewsAlbumType.EXTERNAL_MIZAR && mizarUser == null) {
            return MapMessage.errorMessage("该专辑属主不存在，请重新填写");
        }
        if (mizarUser != null) {
            mizarUserId = mizarUser.getId();
        }

        try {
            //文章和排序先处理一下，方便后面判断
            Map<String, String> newsRankStringMap = JsonUtils.fromJsonToMapStringString(newsRank);
            Map<String, Integer> newsRankMap = new HashMap<>();
            if (MapUtils.isNotEmpty(newsRankStringMap)) {
                for (Map.Entry<String, String> newsRanks : newsRankStringMap.entrySet()) {
                    newsRankMap.put(newsRanks.getKey(), Integer.valueOf(newsRanks.getValue()));
                }
            }
            JxtNewsAlbum album = null;
            if (StringUtils.isNotBlank(albumId)) {
                album = crmVendorService.$loadJxtNewsAlbum(albumId);
            }
            if (album == null) {
                album = new JxtNewsAlbum();
                String editor = getCurrentAdminUser().getAdminUserName();
                album.setEditor(editor);
                album.setDisabled(Boolean.TRUE);
                //如果是编辑，则不能修改类型，只有新建才会设置类型
                album.setFree(type.equals("free"));
            }
            if (SafeConverter.toDouble(price) > 0) {
                album.setPrice(SafeConverter.toDouble(price));
            }
            if (SafeConverter.toDouble(originalPrice) > 0) {
                album.setOriginalPrice(SafeConverter.toDouble(originalPrice));
            }
            if (productId != null) { //免费专辑不会关联产品
                album.setOrderProductId(productId);
            }

            List<JxtNewsAlbum.NewsRecord> newsRecords = new ArrayList<>();
            for (Map.Entry<String, Integer> map : newsRankMap.entrySet()) {
                JxtNewsAlbum.NewsRecord newsRecord = new JxtNewsAlbum.NewsRecord();
                newsRecord.setNewsId(map.getKey());
                newsRecord.setCreateTime(new Date());
                newsRecord.setRank(SafeConverter.toInt(map.getValue()));
                newsRecords.add(newsRecord);
            }
            List<JxtNewsAlbum.NewsRecord> oldList = album.getNewsRecordList();
            if (CollectionUtils.isNotEmpty(oldList)) {
                List<JxtNewsAlbum.NewsRecord> removeList = oldList.stream().filter(e -> !newsRecords.contains(e)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(removeList)) {
                    addAdminLog(getCurrentAdminUser().getAdminUserName() + "删除了专辑" + album.getId() + "文章", albumId, StringUtils.join(removeList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toList()).toArray(), ","));
                }
                oldList.removeAll(removeList);
                for (JxtNewsAlbum.NewsRecord record : removeList) {
                    JxtNews jxtNews = crmVendorService.$loadJxtNews(record.getNewsId());
                    if (jxtNews != null) {
                        jxtNews.setAlbumId("");
                        if (album.getJxtNewsAlbumType() == JxtNewsAlbumType.EXTERNAL_MIZAR) {
                            jxtNews.setOnline(Boolean.FALSE);
                        }
                        crmVendorService.$upsertJxtNews(jxtNews);
                        generateAndSendNewsMizarNotify(jxtNews, album);
                    }
                }

                List<JxtNewsAlbum.NewsRecord> oldNewsList = oldList.stream().filter(newsRecords::contains).collect(Collectors.toList());
                List<JxtNewsAlbum.NewsRecord> copyOldNewsList = new ArrayList<>();
                for (JxtNewsAlbum.NewsRecord newsRecord : oldNewsList) {
                    Integer rank = newsRankMap.get(newsRecord.getNewsId());
                    newsRecord.setRank(rank);
                    copyOldNewsList.add(newsRecord);
                }
                List<JxtNewsAlbum.NewsRecord> newList = newsRecords.stream().filter(e -> !oldList.contains(e)).collect(Collectors.toList());
                oldList.clear();
                oldList.addAll(copyOldNewsList);
                oldList.addAll(newList);
                for (JxtNewsAlbum.NewsRecord record : newList) {
                    JxtNews jxtNews = crmVendorService.$loadJxtNews(record.getNewsId());
                    if (jxtNews != null) {
                        jxtNews.setAlbumId(albumId);
                        crmVendorService.$upsertJxtNews(jxtNews);
                    }
                }
                if (CollectionUtils.isNotEmpty(newList)) {
                    Set<String> newsIds = newList.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
                    List<JxtNews> onlineNews = crmVendorService.$loadsJxtNewsMap(newsIds).values().stream().filter(JxtNews::getOnline).collect(Collectors.toList());
                    if (onlineNews.size() > 0) {
                        sendAlbumReminder(albumId);
                        //jxtNewsServiceClient.sendUpdateAlbumMessageToSubUser(albumId);
                    }
                }
            }
            //标签
            List<Long> tagList = new ArrayList<>();
            String[] tagSplit = tagStr.split(",");
            for (String s : tagSplit) {
                if (StringUtils.isNotBlank(s)) {
                    tagList.add(SafeConverter.toLong(s));
                }
            }
            album.setTagList(tagList);
            //标题
            album.setTitle(title);
            //封面小图
            album.setHeadImg(headImg);
            //封面大图
            album.setBigImgUrl(bigImg);
            //作者
            album.setAuthor(author);
            //详情
            album.setDetail(detail);
            //内部、外部
            album.setJxtNewsAlbumType(JxtNewsAlbumType.parse(innerType));
            //更新日期
            List<JxtNewsAlbum.AlbumUpdateDate> albumUpdateDateList = new ArrayList<>();
            for (String weekDay : dayStrList) {
                JxtNewsAlbum.AlbumUpdateDate updateDate = new JxtNewsAlbum.AlbumUpdateDate();
                if (SafeConverter.toInt(weekDay) > 0) {
                    updateDate.setWeekDay(SafeConverter.toInt(weekDay));
                    updateDate.setUpdateTime(updateAlbumTime);
                }
                albumUpdateDateList.add(updateDate);
            }
            album.setUpdateDateList(albumUpdateDateList);
            if (CollectionUtils.isNotEmpty(oldList)) {
                album.setNewsRecordList(oldList);
            } else {
                album.setNewsRecordList(newsRecords);
            }
            album.setDisabled(Boolean.FALSE);
            //如果albumId为空说明是首次存储。上线标志置为false
            if (StringUtils.isBlank(albumId)) {
                album.setOnline(Boolean.FALSE);
            }
            album.setMizarUserId(mizarUserId);
            album.setJxtNewsAlbumType(jxtNewsAlbumType);
            album.setSubTitle(subTitle);
            JxtNewsAlbumContentType jxtNewsAlbumContentType = JxtNewsAlbumContentType.parse(albumContentType);
            if (jxtNewsAlbumContentType == null || jxtNewsAlbumContentType == JxtNewsAlbumContentType.UNKNOWN) {
                return MapMessage.errorMessage("专辑内容类型不正确");
            }
            album.setJxtNewsAlbumContentType(jxtNewsAlbumContentType);

            album = crmVendorService.$upsertJxtNewsAlbum(album);
            if (album == null) {
                return MapMessage.errorMessage();
            }
            if (CollectionUtils.isEmpty(oldList)) {
                for (JxtNewsAlbum.NewsRecord record : newsRecords) {
                    JxtNews jxtNews = crmVendorService.$loadJxtNews(record.getNewsId());
                    if (jxtNews != null) {
                        jxtNews.setAlbumId(album.getId());
                        crmVendorService.$upsertJxtNews(jxtNews);
                    }
                }
                Set<String> newsIds = newsRecords.stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
                List<JxtNews> onlineNews = crmVendorService.$loadsJxtNewsMap(newsIds).values().stream().filter(JxtNews::getOnline).collect(Collectors.toList());
                if (onlineNews.size() > 0) {
                    sendAlbumReminder(albumId);
                    //jxtNewsServiceClient.sendUpdateAlbumMessageToSubUser(albumId);
                }
            }
            addAdminLog("管理员：" + getCurrentAdminUser().getAdminUserName() + "保存专辑内容成功");
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 生成视频的截图
     *
     * @return
     */
    @RequestMapping(value = "generateSnapshotFromVideo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateSnapshotFromVideo() throws ClientException {
        String video_url = getRequestString("video_url");
        if (StringUtils.isBlank(video_url)) {
            return MapMessage.errorMessage("视频地址为空");
        }
        SnapshotSubmitResult snapshotSubmitResult = AdminAcsManagerUtils.submitSnapshotJob(video_url);
        List<String> snapshots = snapshotSubmitResult.getSnapshots();

        return MapMessage.successMessage().add("snapshots", snapshots);
    }


    /**
     * 上线专辑
     *
     * @return
     */
    @RequestMapping(value = "jxtnewsalbumonline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onlineJxtNewsAlbum() {
        String albumId = getRequestString("albumId");
        JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
        if (album == null) {
            return MapMessage.errorMessage("您要上线的专辑不存在：" + albumId);
        }
        album = new JxtNewsAlbum();
        album.setId(albumId);
        album.setOnline(true);
        album.setOnlineTime(new Date());
        album = crmVendorService.$upsertJxtNewsAlbum(album);
        if (album != null) {
            addAdminLog("管理员：" + getCurrentAdminUser().getAdminUserName() + "上线专辑内容成功");
        }
        return new MapMessage().setSuccess(album != null);
    }

    /**
     * 下线专辑
     *
     * @return
     */
    @RequestMapping(value = "jxtnewsalbumoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offlineJxtNewsAlbum() {
        String albumId = getRequestString("albumId");
        JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
        if (album == null) {
            return MapMessage.errorMessage("您要下线的专辑不存在：" + albumId);
        }
        album = new JxtNewsAlbum();
        album.setId(albumId);
        album.setOnline(false);
        album = crmVendorService.$upsertJxtNewsAlbum(album);
        if (album != null) {
            if (CollectionUtils.isNotEmpty(album.getNewsRecordList())) {
                Set<String> newsIds = album.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet());
                Map<String, JxtNews> jxtNewsMap = crmVendorService.$loadsJxtNewsMap(newsIds);
                if (MapUtils.isNotEmpty(jxtNewsMap)) {
                    jxtNewsMap.values().forEach(e -> {
                        e.setOnline(Boolean.FALSE);
                        crmVendorService.$upsertJxtNews(e);
                    });
                }
                generateAndSendAlbumMizarNotify(album);
            }
            addAdminLog("管理员：" + getCurrentAdminUser().getAdminUserName() + "下线专辑内容成功");
        }
        return new MapMessage().setSuccess(album != null);
    }

    /**
     * 公众号敏感词管理
     **/
    @RequestMapping(value = "/wechatmassbadwords.vpage", method = RequestMethod.GET)
    public String wechatmassbadwords(Model model) {
        return "advisory/wechatmassbadwords";
    }

    /**
     * load微信公众号敏感词
     */
    @RequestMapping(value = "/loadwechatmassbadwords.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadwechatmassbadwords() {
        int page = getRequestInt("currentPage");
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        String word = getRequestString("word");
        int category = getRequestInt("category");
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "create_datetime");
        Page<ZyParentNewsWechatMassBadword> zyParentNewsWechatMassBadwordses = crmVendorService.loadNextPageInWechatBadword(word, category, pageable);
        return MapMessage.successMessage().add("badwords", zyParentNewsWechatMassBadwordses);
    }


    /**
     * upsert微信公众号敏感词
     */
    @RequestMapping(value = "/upsertwechatmassbadword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertwechatmassbadword() {
        String badwordString = getRequestString("badword");
        Map<String, Object> badword = JsonUtils.fromJson(badwordString);
        String editor = getCurrentAdminUser().getAdminUserName();
        ZyParentNewsWechatMassBadword zyParentNewsWechatMassBadword = ZyParentNewsWechatMassBadword.init(badword);
        zyParentNewsWechatMassBadword.setEditor(editor);
        crmVendorService.upsertWechatBadword(zyParentNewsWechatMassBadword);
        return MapMessage.successMessage();
    }

    /**
     * 公众号独特过滤管理
     **/
    @RequestMapping(value = "/wechatmassfilters.vpage", method = RequestMethod.GET)
    public String wechatmassfilters(Model model) {
        return "advisory/wechatmassfilters";
    }

    /**
     * load微信公众号过滤器
     */
    @RequestMapping(value = "/loadwechatmassfilters.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadwechatmassfilters() {
        int page = getRequestInt("currentPage");
        page = page - 1;
        if (page < 0) {
            page = 0;
        }
        String name = getRequestString("name");
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "create_datetime");
        Page<ZyParentNewsWechatMassFilter> zyParentNewsWechatMassFilters = crmVendorService.loadNextPageInWechatMassFilter(pageable, name);
        return MapMessage.successMessage().add("filters", zyParentNewsWechatMassFilters);
    }


    /**
     * upsert微信公众号过滤器
     */
    @RequestMapping(value = "/upsertwechatmassfilter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertwechatmassfilter() {
        String filterString = getRequestString("filter");
        Map<String, Object> filter = JsonUtils.fromJson(filterString);
        String editor = getCurrentAdminUser().getAdminUserName();
        ZyParentNewsWechatMassFilter zyParentNewsWechatMassFilter = ZyParentNewsWechatMassFilter.init(filter);
        zyParentNewsWechatMassFilter.setEditor(editor);
        // 如果是插入的话，检验biz是否存在了
        if (!filter.containsKey("id")) {
            if (crmVendorService.exsitsBiz((String) filter.get("biz"))) {
                return MapMessage.errorMessage("biz已存在");
            }
        }
        crmVendorService.upsertWechatMassFilter(zyParentNewsWechatMassFilter);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/crawlerVideoView.vpage", method = RequestMethod.GET)
    public String crawlerVideoView() {
        return "advisory/videocrawler";
    }

    @RequestMapping(value = "/crawlerVideo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage crawlerVideo() {
        String url = getRequestString("url");
        List<String> soundIds = generateXiMaLaYaUrl(url);
        if (CollectionUtils.isEmpty(soundIds)) {
            return MapMessage.errorMessage("未找到音频id");
        }
//        List<Map<String, String>> mapList = downloadAnduploadXiMaLaYa(soundIds);
//        List<Map<String, String>> returnList = new ArrayList<>();
//        mapList.forEach(e -> {
//            String id = e.get("id");
//            String env = "/";
//            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
//                env = "/test/";
//            }
//            String oss_news_video_host = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_news_video_host")) + "class" + env + id + ".m4a";
//            AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().head(oss_news_video_host).execute();
//            e.put("status", SafeConverter.toString(execute.getStatusCode()));
//            e.put("file_url", oss_news_video_host);
//            returnList.add(e);
//        });
        return MapMessage.successMessage().add("returnList", soundIds);
    }

    @RequestMapping(value = "/videoInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage videoInfo() {
        String sound_id = getRequestString("sound_id");
        if (StringUtils.isBlank(sound_id)) {
            return MapMessage.errorMessage("未找到音频id");
        }
        Map<String, String> map = xiMaLaYaInfo(sound_id);
        if (MapUtils.isEmpty(map)) {
            return MapMessage.errorMessage("未找到对应的音频信息");
        }
        String id = map.get("id");
        String env = "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "/test/";
        }
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("news-video-content");
        Objects.requireNonNull(config);
        String oss_news_video_host = "https://" + StringUtils.defaultString(config.getHost()) + "class" + env + id + ".m4a";
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().head(oss_news_video_host).execute();
        map.put("status", SafeConverter.toString(execute.getStatusCode()));
        map.put("file_url", oss_news_video_host);

        return MapMessage.successMessage().add("returnMap", map);
    }

    //==============
    //private method
    private List<Map<String, Object>> generateJxtNewsSubjectCountMap(List<JxtNews> jxtNewsList) {
        List<String> newsIds = jxtNewsList.stream().map(JxtNews::getId).collect(Collectors.toList());
        List<String> articleIds = jxtNewsList.stream().map(JxtNews::getArticleId).collect(Collectors.toList());

        //资讯阅读数
//        Map<String, Long> readCount = vendorCacheClient.getParentJxtCacheManager().loadReadCount(newsIds);
        Map<String, Long> readCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds)
                .take();
        //资讯点赞数
//        Map<String, Long> voteCount = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(newsIds);
        Map<String, Long> voteCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_VOTE_COUNT, newsIds)
                .take();
        // 没帮助数
//        Map<String, Long> unhelpCount = vendorCacheClient.getParentJxtCacheManager().loadNotHelpVoteCount(newsIds);
        Map<String, Long> unhelpCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_VOTE_NOT_HELP_COUNT, newsIds)
                .take();
        //收藏数
//        Map<String, Long> collectCount = vendorCacheClient.getParentJxtCacheManager().loadCollectCount(newsIds);
        Map<String, Long> collectCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COLLECTED_COUNT, newsIds)
                .take();
        //资讯评论数
//        Map<String, Long> commentCount = vendorCacheClient.getParentJxtCacheManager().loadCommentCount(newsIds);
        Map<String, Long> commentCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_COUNT, newsIds)
                .take();
        //资讯分享数
//        Map<String, Long> shareCount = vendorCacheClient.getParentJxtCacheManager().loadShareCount(newsIds);
        Map<String, Long> shareCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_SHARE_COUNT, newsIds)
                .take();
        Map<String, List<JxtNewsComment>> commentListMap = jxtNewsLoaderClient.getCommentListByTypeIds(newsIds);
        Set<String> allCommentIds = commentListMap.values().stream().flatMap(Collection::stream).map(JxtNewsComment::getId).collect(Collectors.toSet());
        //评论的点赞数
//        Map<String, Long> commentVoteMap = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(allCommentIds);
        Map<String, Long> commentVoteMap = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_VOTE_COUNT, allCommentIds)
                .take();
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return mapList;
        }
        jxtNewsList = jxtNewsList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        jxtNewsList.stream().forEach(jxtNews -> {
            if (jxtNews != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("newsId", jxtNews.getId());
                map.put("title", jxtNews.getTitle());
                map.put("createDate", DateUtils.dateToString(jxtNews.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("readCount", SafeConverter.toLong(readCount.get(jxtNews.getId())));
                map.put("voteCount", SafeConverter.toLong(voteCount.get(jxtNews.getId())));
                map.put("commentCount", SafeConverter.toLong(commentCount.get(jxtNews.getId())));
                map.put("shareCount", SafeConverter.toLong(shareCount.get(jxtNews.getId())));
                map.put("unHelpCount", SafeConverter.toLong(unhelpCount.get(jxtNews.getId())));
                map.put("collectCount", SafeConverter.toLong(collectCount.get(jxtNews.getId())));
                Set<String> commentIds = new HashSet<>();
                if (commentListMap.get(jxtNews.getId()) != null) {
                    commentIds = commentListMap.get(jxtNews.getId()).stream().map(JxtNewsComment::getId).collect(Collectors.toSet());
                }
                Long commentVoteCount = 0L;
                for (String commentId : commentIds) {
                    commentVoteCount += commentVoteMap.get(commentId) == null ? 0 : commentVoteMap.get(commentId);
                }
                map.put("commentVoteCount", commentVoteCount);
                map.put("isOnline", jxtNews.getOnline());
                mapList.add(map);
            }
        });
        return mapList;
    }

    private Map<String, Object> generateJxtNewsSubjectInfoMap(JxtNews jxtNews) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNews == null) {
            return map;
        }
        //标题
        map.put("title", jxtNews.getTitle());

        map.put("newsId", jxtNews.getId());

        //专题Id
        map.put("subjectId", jxtNews.getArticleId());

        //专题共享内容,第一次默认返回头部内容
        JxtNewsSubject subject = crmVendorService.$loadJxtNewsSubject(jxtNews.getArticleId());
        map.put("headContent", subject == null ? "" : SafeConverter.toString(subject.getHeadContent(), ""));

        //封面图片预览地址
        List<String> coverImgList = jxtNews.getCoverImgList();
        Map<String, Object> imgMap = new HashMap<>();
        Map<String, Object> fileMap = new HashMap<>();
        if (CollectionUtils.isEmpty(coverImgList)) {
            imgMap.put("url", "");
            fileMap.put("url", "");
        } else {
            for (String imgUrl : coverImgList) {
                imgMap.put("url" + coverImgList.indexOf(imgUrl), combineImgUrl(imgUrl));
                fileMap.put("url" + coverImgList.indexOf(imgUrl), imgUrl);
            }
        }
        map.put("imgUrl", imgMap);
        //封面图片地址
        map.put("fileName", fileMap);
        //标签
        Collection<JxtNewsTag> tags = crmVendorService.$loadJxtNewsTagList().stream()
                .filter(e -> jxtNews.getTagList().contains(e.getId()))
                .collect(Collectors.toList());
        map.put("tags", tags.stream().collect(Collectors.toMap(JxtNewsTag::getTagName, JxtNewsTag::getId)));
        //页面布局类型
        map.put("type", jxtNews.getJxtNewsType() == null ? "" : jxtNews.getJxtNewsType().name());
        map.put("pushType", jxtNews.generatePushType());
        if (!jxtNews.generatePushType().equals(JxtNewsPushType.REGION.getType())) {
            map.put("availableUserId", jxtNews.getAvailableUserId());
        } else {
            //区域ID和名字
            if (CollectionUtils.isNotEmpty(jxtNews.getRegionCodeList())) {
                List<Integer> regionIds = jxtNews.getRegionCodeList();
                Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
                regionIds = exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList());
                List<String> regionNames = exRegionMap.values().stream().map(ExRegion::getName).collect(Collectors.toList());
                //这里再处理一次是怕区域调整的时候这个区域已经不存在了。这样重新编辑的时候就能修正这样的数据了
                if (CollectionUtils.isNotEmpty(regionIds)) {
                    map.put("regionIds", StringUtils.join(regionIds, ","));
                    map.put("regionNames", StringUtils.join(regionNames, ","));
                }
            }
        }
        return map;
    }

    private Map<String, List<String>> toListString(Map<String, List> from) {
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, List> entry : from.entrySet()) {
            List<String> value = (entry.getValue() == null) ? null : (List<String>) entry.getValue();
            map.put(entry.getKey(), value);
        }
        return map;
    }

    //专题里表页各种数据汇总
    private List<Map<String, Object>> generateJxtNewsSubjectMap(List<JxtNewsSubject> subjectList) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        subjectList = subjectList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        subjectList.stream().forEach(subject -> {
            if (subject != null) {
                String subjectId = subject.getId();
                Map<String, Object> map = new HashMap<>();
                map.put("newsId", subject.getNewsId());
                map.put("subjectId", subjectId);
                map.put("title", subject.getTitle());
                map.put("editor", subject.getEditor());
                map.put("published", SafeConverter.toBoolean(subject.getPublished()));
                mapList.add(map);
            }
        });

        return mapList;
    }

    private Map<String, Object> generateSubjectInfoMap(JxtNewsSubject subject) {
        Map<String, Object> map = new HashMap<>();
        if (subject == null) {
            return map;
        }
        map.put("subjectId", subject.getId());
        map.put("title", subject.getTitle());
        //头图地址
        map.put("headImg", StringUtils.isBlank(subject.getHeadImg()) ? "" : combineImgUrl(subject.getHeadImg()));
        //头图名称
        map.put("headImgName", StringUtils.isBlank(subject.getHeadImg()) ? "" : subject.getHeadImg());
        //引言
        map.put("introduction", SafeConverter.toString(subject.getIntroduction(), ""));
        //分类排名对应map
        map.put("categoryRankMap", subject.getCategoryRankMap());
        //分类和文章id对应map
        map.put("categoryNewsMap", subject.getCategoryNewsMap());
        //文章排名对应map
        map.put("newsRankMap", subject.getNewsRankMap());
        //文章标题Map
        Set<String> newIds = subject.getNewsRankMap().keySet();
        List<JxtNews> jxtNewsList = crmJxtNewsDao.loadAllFromSecondary()
                .stream()
                .filter(e -> newIds.contains(e.getId()))
                .collect(Collectors.toList());
        Map<String, String> idTitleMap = jxtNewsList.stream()
                .collect(Collectors.toMap(JxtNews::getId, JxtNews::getTitle));
        map.put("newsTitleMap", idTitleMap);
        //广告类型
        map.put("advertisementType", subject.getAdvertisementType());
        //广告图片
        List<Map<String, String>> adList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subject.getAdList())) {
            subject.getAdList().stream().forEach(e -> {
                if (MapUtils.isNotEmpty(e)) {
                    Map<String, String> adverImgNameMap = new HashMap<>();
                    adverImgNameMap.put("imgName", e.get("img"));
                    adverImgNameMap.put("imgUrl", combineImgUrl(e.get("img")));
                    adverImgNameMap.put("url", e.get("adUrl"));
                    adList.add(adverImgNameMap);
                }
            });
            map.put("adList", adList);
        }

        return map;
    }

    //生成专辑的列表信息
    private List<Map<String, Object>> generateJxtNewsAlbumMap(List<JxtNewsAlbum> albumList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Set<String> albumIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(albumList)) {
            albumIds = albumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
        }
//        Map<String, Long> subCounts = vendorCacheClient.getParentJxtCacheManager().loadAlbumSubCount(albumIds);
        Map<String, Long> subCounts = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, albumIds)
                .take();
        albumList = albumList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        Set<String> mizarUserIds = albumList.stream().map(JxtNewsAlbum::getMizarUserId).collect(Collectors.toSet());
        Map<String, MizarUser> mizarUserMap = mizarUserLoaderClient.loadUsers(mizarUserIds);
        albumList.stream().forEach(album -> {
            if (album != null) {
                String albumId = album.getId();
                Map<String, Object> map = new HashMap<>();
                map.put("title", album.getTitle());
                map.put("id", albumId);
                map.put("createTime", DateUtils.dateToString(album.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("onlineTime", album.getOnlineTime() != null ? DateUtils.dateToString(album.getOnlineTime(), DateUtils.FORMAT_SQL_DATE) : "");
                map.put("subCount", SafeConverter.toInt(subCounts.get(albumId)));
                map.put("albumInsideType", album.generateJxtNewsAlbumType());
                map.put("albumContentType", album.getJxtNewsAlbumContentType() != null ? album.getJxtNewsAlbumContentType().getDesc() : "");
                MizarUser mizarUser = mizarUserMap.get(album.getMizarUserId());
                map.put("mizarUserName", mizarUser == null ? "" : mizarUser.getAccountName());
                map.put("isOnline", album.getOnline());
                map.put("editor", album.getEditor());
                map.put("free", null == album.getFree() ? true : album.getFree());
                mapList.add(map);
            }
        });
        return mapList;
    }

    //生成专辑的详细信息
    private Map<String, Object> generateAlbumInfoMap(JxtNewsAlbum jxtNewsAlbum) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNewsAlbum == null) {
            return map;
        }
        map.put("albumId", jxtNewsAlbum.getId());

        map.put("title", jxtNewsAlbum.getTitle());

        map.put("subTitle", jxtNewsAlbum.getSubTitle());

        map.put("headImg", combineImgUrl(jxtNewsAlbum.getHeadImg()));

        map.put("headImgName", jxtNewsAlbum.getHeadImg());

        map.put("bigImg", generateAliYunImgUrl(jxtNewsAlbum.getBigImgUrl()));

        map.put("bigImgName", jxtNewsAlbum.getBigImgUrl());

        map.put("author", jxtNewsAlbum.getAuthor());

        map.put("detail", jxtNewsAlbum.getDetail());
        map.put("albumType", jxtNewsAlbum.generateJxtNewsAlbumType());
        map.put("albumContentType", jxtNewsAlbum.getJxtNewsAlbumContentType() != null ? jxtNewsAlbum.getJxtNewsAlbumContentType().name() : "");
        String mizarUserId = jxtNewsAlbum.getMizarUserId();
        MizarUser mizarUser = null;
        if (StringUtils.isNotBlank(mizarUserId)) {
            mizarUser = mizarUserLoaderClient.loadUser(mizarUserId);
        }
        map.put("albumOwner", mizarUser != null ? mizarUser.getAccountName() : "");
        map.put("free", jxtNewsAlbum.getFree());
        map.put("price", jxtNewsAlbum.getPrice());
        map.put("originalPrice", jxtNewsAlbum.getOriginalPrice());
        map.put("productId", jxtNewsAlbum.getOrderProductId());

        Collection<JxtNewsTag> tags = crmVendorService.$loadJxtNewsTagList().stream()
                .filter(e -> jxtNewsAlbum.getTagList().contains(e.getId()))
                .collect(Collectors.toList());
        map.put("tags", tags.stream().collect(Collectors.toMap(JxtNewsTag::getId, JxtNewsTag::getTagName)));


        Map<String, Integer> newsRankMap = new LinkedHashMap<>();
        List<JxtNewsAlbum.NewsRecord> newsRecordList = jxtNewsAlbum.getNewsRecordList();
        if (CollectionUtils.isNotEmpty(newsRecordList)) {
            Comparator<JxtNewsAlbum.NewsRecord> c = (a, b) -> Integer.compare(SafeConverter.toInt(b.getRank()), SafeConverter.toInt(a.getRank()));
            c = c.thenComparing((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            newsRecordList = newsRecordList.stream().sorted(c).collect(Collectors.toList());
            for (JxtNewsAlbum.NewsRecord record : newsRecordList) {
                newsRankMap.put(record.getNewsId(), record.getRank());
            }
        }
        map.put("newsRankMap", newsRankMap);

        Set<String> newIds = newsRankMap.keySet();
        List<JxtNews> jxtNewsList = crmJxtNewsDao.loadAllFromSecondary()
                .stream()
                .filter(e -> newIds.contains(e.getId()))
                .collect(Collectors.toList());
        Map<String, String> idTitleMap = jxtNewsList.stream()
                .collect(Collectors.toMap(JxtNews::getId, JxtNews::getTitle));
        map.put("newsTitleMap", idTitleMap);

        //资讯是否需要付费
        Map<String, Boolean> newsFreeMap = new HashMap<>();
        jxtNewsList.forEach(n -> {
            if (null == n.getFree() || n.getFree()) {
                newsFreeMap.put(n.getId(), true);
            } else {
                newsFreeMap.put(n.getId(), false);
            }
        });
        map.put("newsFreeMap", newsFreeMap);

        List<JxtNewsAlbum.AlbumUpdateDate> list = jxtNewsAlbum.getUpdateDateList();
        if (CollectionUtils.isNotEmpty(list)) {
            List<Integer> weekDays = list.stream().map(JxtNewsAlbum.AlbumUpdateDate::getWeekDay).collect(Collectors.toList());
            map.put("weekDays", weekDays);
            //TODO:1.9.0版本这里的设计是支持星期可多选，时间不可多选，后端数据结构是支持都可多选的
            JxtNewsAlbum.AlbumUpdateDate updateDate = list.get(0);
            if (updateDate != null) {
                String albumUpdateTime = updateDate.getUpdateTime();
                if (StringUtils.isNotBlank(albumUpdateTime)) {
                    map.put("albumUpdateTime", albumUpdateTime);
                }
            }
        }

        return map;
    }

    //资讯列表页各种数据汇总
    private List<Map<String, Object>> generateJxtNewsCountMap(List<JxtNews> jxtNewsList) {
        List<String> newsIds = jxtNewsList.stream().map(JxtNews::getId).collect(Collectors.toList());
        List<String> articleIds = jxtNewsList.stream().map(JxtNews::getArticleId).collect(Collectors.toList());
        // 文章字数
        Map<String, ZyParentNewsArticle> zyParentNewsArticleMap = parentNewsService.loadArticleByIds(articleIds);
        Map<String, Long> wordsCount = zyParentNewsArticleMap.values().stream()
                .collect(Collectors.toMap(ZyParentNewsArticle::getId, zyParentNewsArticle ->
                        SafeConverter.toLong(zyParentNewsArticle.getWords_count())));
        //资讯阅读数
//        Map<String, Long> readCount = vendorCacheClient.getParentJxtCacheManager().loadReadCount(newsIds);
        Map<String, Long> readCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds)
                .take();
        //资讯点赞数
//        Map<String, Long> voteCount = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(newsIds);
        Map<String, Long> voteCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_VOTE_COUNT, newsIds)
                .take();
        // 没帮助数
//        Map<String, Long> unhelpCount = vendorCacheClient.getParentJxtCacheManager().loadNotHelpVoteCount(newsIds);
        Map<String, Long> unhelpCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_VOTE_NOT_HELP_COUNT, newsIds)
                .take();
        //收藏数
//        Map<String, Long> collectCount = vendorCacheClient.getParentJxtCacheManager().loadCollectCount(newsIds);
        Map<String, Long> collectCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COLLECTED_COUNT, newsIds)
                .take();
        //资讯评论数
//        Map<String, Long> commentCount = vendorCacheClient.getParentJxtCacheManager().loadCommentCount(newsIds);
        Map<String, Long> commentCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_COUNT, newsIds)
                .take();
        //资讯分享数
//        Map<String, Long> shareCount = vendorCacheClient.getParentJxtCacheManager().loadShareCount(newsIds);
        Map<String, Long> shareCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_SHARE_COUNT, newsIds)
                .take();

        Map<String, List<JxtNewsComment>> commentListMap = jxtNewsLoaderClient.getCommentListByTypeIds(newsIds);
        Set<String> allCommentIds = commentListMap.values().stream().flatMap(Collection::stream).map(JxtNewsComment::getId).collect(Collectors.toSet());
        //评论的点赞数
//        Map<String, Long> commentVoteMap = vendorCacheClient.getParentJxtCacheManager().loadVoteCount(allCommentIds);
        Map<String, Long> commentVoteMap = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_COMMENT_VOTE_COUNT, allCommentIds)
                .take();
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return mapList;
        }
//        jxtNewsList = jxtNewsList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        jxtNewsList.stream().forEach(jxtNews -> {
            if (jxtNews != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("newsId", jxtNews.getId());
                map.put("title", jxtNews.getTitle());
                map.put("pushDate", jxtNews.getPushTime() == null ? "" : DateUtils.dateToString(jxtNews.getPushTime(), DateUtils.FORMAT_SQL_DATETIME));
                map.put("updateDate", DateUtils.dateToString(jxtNews.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("createDate", DateUtils.dateToString(jxtNews.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("publishTime", jxtNews.getPublishTime() == null || jxtNews.getPublishTime() == 0L ? null : DateUtils.dateToString(new Date(jxtNews.getPublishTime())));
                map.put("pushUser", jxtNews.getPushUser());
                map.put("readCount", SafeConverter.toLong(readCount.get(jxtNews.getId())));
                map.put("voteCount", SafeConverter.toLong(voteCount.get(jxtNews.getId())));
                map.put("commentCount", SafeConverter.toLong(commentCount.get(jxtNews.getId())));
                map.put("shareCount", SafeConverter.toLong(shareCount.get(jxtNews.getId())));
                map.put("wordsCount", SafeConverter.toLong(wordsCount.get(jxtNews.getArticleId())));
                map.put("unhelpCount", SafeConverter.toLong(unhelpCount.get(jxtNews.getId())));
                map.put("collectCount", SafeConverter.toLong(collectCount.get(jxtNews.getId())));
                Set<String> commentIds = new HashSet<>();
                if (commentListMap.get(jxtNews.getId()) != null) {
                    commentIds = commentListMap.get(jxtNews.getId()).stream().map(JxtNewsComment::getId).collect(Collectors.toSet());
                }
                Long commentVoteCount = 0L;
                for (String commentId : commentIds) {
                    commentVoteCount += commentVoteMap.get(commentId) == null ? 0 : commentVoteMap.get(commentId);
                }
                map.put("commentVoteCount", commentVoteCount);
                map.put("isOnline", jxtNews.getOnline());
                map.put("source", jxtNews.getSource());
                if (jxtNews.getJxtNewsContentType() != null) {
                    map.put("contentType", jxtNews.getJxtNewsContentType().getDesc());
                } else {
                    map.put("contentType", "");
                }
                // 内容样式
                if (jxtNews.getJxtNewsStyleType() != null)
                    map.put("styleType", jxtNews.getJxtNewsStyleType().getDesc());
                else
                    map.put("styleType", "");

                mapList.add(map);
            }
        });
        return mapList;
    }

    //编辑资讯时返回资讯的已有信息
    private Map<String, Object> generateJxtNewsInfoMap(JxtNews jxtNews) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNews == null) {
            return map;
        }
        //标题
        map.put("title", jxtNews.getTitle());

        map.put("newsId", jxtNews.getId());

        //文章Id
        map.put("articleId", jxtNews.getArticleId());
        //封面图片预览地址
        List<String> coverImgList = jxtNews.getCoverImgList();
        Map<String, Object> imgMap = new HashMap<>();
        Map<String, Object> fileMap = new HashMap<>();
        if (CollectionUtils.isEmpty(coverImgList)) {
            imgMap.put("url", "");
            fileMap.put("url", "");
        } else {
            for (String imgUrl : coverImgList) {
                imgMap.put("url" + coverImgList.indexOf(imgUrl), combineImgUrl(imgUrl));
                fileMap.put("url" + coverImgList.indexOf(imgUrl), imgUrl);
            }
        }
        map.put("imgUrl", imgMap);
        //封面图片地址
        map.put("fileName", fileMap);
        //标签
        Collection<JxtNewsTag> tags = crmVendorService.$loadJxtNewsTagList().stream()
                .filter(e -> jxtNews.getTagList().contains(e.getId()))
                .collect(Collectors.toList());
        map.put("tags", tags.stream().collect(Collectors.toMap(JxtNewsTag::getId, JxtNewsTag::getTagName)));
        //内容类别
        List<JxtNewsCategory> categories = jxtNewsLoaderClient.findCategoriesByIds(jxtNews.getCategoryList());
        map.put("categories", categories.stream().collect(Collectors.toMap(JxtNewsCategory::getCategoryName, JxtNewsCategory::getId)));
        //文章来源
        map.put("source", jxtNews.getSource());
        //文章原地址
        map.put("sourceUrl", jxtNews.getSourceUrl());
        //页面布局类型
        map.put("type", jxtNews.getJxtNewsType() == null ? "" : jxtNews.getJxtNewsType().name());
        //资讯内容类型
        map.put("contentType", jxtNews.getJxtNewsContentType() == null ? "" : jxtNews.getJxtNewsContentType().name());
        if (jxtNews.getJxtNewsContentType() != null && (JxtNewsContentType.AUDIO.equals(jxtNews.getJxtNewsContentType()) || JxtNewsContentType.VIDEO.equals(jxtNews.getJxtNewsContentType()))) {
            map.put("video_url", jxtNews.getVideo_url());
        }
        map.put("styleType", jxtNews.getJxtNewsStyleType() == null ? "" : jxtNews.getJxtNewsStyleType().name());
        //群引导文案
        map.put("chatGroupWelcomeContent", jxtNews.getChatGroupWelcomeContent());
        //群号
        map.put("chatGroupId", jxtNews.getChatGroupId());
        //是否显示广告
        map.put("showAd", jxtNews.getShowAd());
        map.put("pushType", jxtNews.generatePushType());
        //视频和音频的播放时长
        map.put("playTime", jxtNews.getPlayTime() == null ? "" : jxtNews.getPlayTime());
        String albumId = jxtNews.getAlbumId();
        if (StringUtils.isNotBlank(albumId)) {
            //资讯所属专辑ID
            map.put("albumId", albumId);
            //资讯在专辑中的排序
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
            if (album != null) {
                List<JxtNewsAlbum.NewsRecord> newsRecordList = album.getNewsRecordList();
                if (CollectionUtils.isNotEmpty(newsRecordList)) {
                    JxtNewsAlbum.NewsRecord newsRecord = newsRecordList.stream().filter(p -> StringUtils.equals(p.getNewsId(), jxtNews.getId())).findFirst().orElse(null);
                    if (newsRecord != null) {
                        map.put("newsRank", newsRecord.getRank());
                    }
                }
            }
        }

        if (!jxtNews.generatePushType().equals(JxtNewsPushType.REGION.getType())) {
            map.put("availableUserId", jxtNews.getAvailableUserId());
        } else {
            //区域ID和名字
            if (CollectionUtils.isNotEmpty(jxtNews.getRegionCodeList())) {
                List<Integer> regionIds = jxtNews.getRegionCodeList();
                Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
                regionIds = exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList());
                List<String> regionNames = exRegionMap.values().stream().map(ExRegion::getName).collect(Collectors.toList());
                //这里再处理一次是怕区域调整的时候这个区域已经不存在了。这样重新编辑的时候就能修正这样的数据了
                if (CollectionUtils.isNotEmpty(regionIds)) {
                    map.put("regionIds", StringUtils.join(regionIds, ","));
                    map.put("regionNames", StringUtils.join(regionNames, ","));
                }
            }
        }

        map.put("free", jxtNews.getFree());
        map.put("isTop", jxtNews.getIsTop());
        map.put("topOrder", jxtNews.getTopOrder());
        return map;
    }

    //评论列表
    private List<Map<String, Object>> generateCommentList(List<JxtNewsComment> commentList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(commentList)) {
            return mapList;
        }

        String cdnDomainAvatar = CdnConfig.getAvatarDomain().getValue();
        commentList.stream().forEach(comment -> {
            if (comment != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("commentId", comment.getId());
                map.put("userName", comment.getUserName());
                map.put("userId", comment.getUserId());
                User user = userLoaderClient.loadUser(comment.getUserId());
                String avatar = user.getProfile().getImgUrl();
                if (avatar.length() > 0) {
                    map.put("avatar", cdnDomainAvatar + "/gridfs/" + avatar);
                } else {
                    map.put("avatar", "");
                }
                map.put("comment", comment.getComment());
                JxtNews jxtNews = crmVendorService.$loadJxtNews(comment.getNewsId());
                // 如果连资讯都没有了，那就算了吧，这条评论也没有展示的必要了，直接删除

                Long showCommentCount = SafeConverter.toLong(jxtNewsLoaderClient.getAllOnlineCommentsByUserId(comment.getUserId()).size());

                map.put("showCommentCount", showCommentCount);

                if (jxtNews != null) {
                    map.put("articleTitle", jxtNews.getTitle());
                    List<JxtNewsComment> commentReplyList = jxtNewsLoaderClient.getCommentListByTypeId(comment.getId());
                    Collections.sort(commentReplyList, (o1, o2) -> o2.getCreateTime().after(o1.getCreateTime()) ? 1 : -1);
                    JxtNewsComment reply = CollectionUtils.isEmpty(commentReplyList) ? null : commentReplyList.get(0);
                    map.put("replyComment", reply == null ? "" : reply.getComment());
                    map.put("replyIsShow", reply != null && reply.getIsShow());
                    map.put("isShow", comment.getIsShow());
                    map.put("createTime", DateUtils.dateToString(comment.getCreateTime()));
                    map.put("updateTime", DateUtils.dateToString(comment.getUpdateTime()));
                    mapList.add(map);
                }
            }
        });
        return mapList;
    }

    //推送列表页数据汇总
    private List<Map<String, Object>> generateJxtNewsPushRecordMap(List<JxtNewsPushRecord> pushRecordList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(pushRecordList)) {
            return mapList;
        }
        pushRecordList = pushRecordList.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).collect(Collectors.toList());
        //所有资讯id
        Set<String> newsIds = new HashSet<>();
        pushRecordList.stream().forEach(p -> {
            if (CollectionUtils.isNotEmpty(p.getJxtNewsIdList())) {
                newsIds.addAll(p.getJxtNewsIdList());
            }
        });
//        Map<String, Long> readCount = vendorCacheClient.getParentJxtCacheManager().loadReadCount(newsIds);
        Map<String, Long> readCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_READ_COUNT, newsIds)
                .take();
        pushRecordList.stream().forEach(pushRecord -> {
            if (pushRecord != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("pushRecordId", pushRecord.getId());
                map.put("createTime", DateUtils.dateToString(pushRecord.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("pushTime", DateUtils.dateToString(pushRecord.getStartTime(), "yyyy-MM-dd HH:mm"));
                String count = "";
                for (String newsId : pushRecord.getJxtNewsIdList()) {
                    if (StringUtils.isBlank(count)) {
                        count = readCount.getOrDefault(newsId, 0L).toString();
                    } else {
                        count = count + "," + readCount.getOrDefault(newsId, 0L);
                    }
                }
                map.put("count", count);
                map.put("isOnline", pushRecord.getOnline());
                mapList.add(map);
            }
        });
        return mapList;
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

    //生成投放区域的树
    @SuppressWarnings("unchecked")
    private String generateRegionCodeTree(List<Integer> selectedRegionCode) {
        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();
        List<Region> regions = new ArrayList<>(allRegions.values());

        Map<String, Map<String, Object>> retMap = new HashMap<>();
        for (Region region : regions) {
            // 转换成要使用的HashMap对象
            Map<String, Object> regionItemMap = new HashMap<>();
            regionItemMap.put("title", region.getName());
            regionItemMap.put("key", String.valueOf(region.getCode()));
            if (region.getPcode() != 0) {
                regionItemMap.put("pcode", String.valueOf(region.getPcode()));
            }
            regionItemMap.put("children", new ArrayList());

            retMap.put(String.valueOf(region.getCode()), regionItemMap);
        }

        // 第二次循环，根据Id和ParentID构建父子关系
        for (Region region : regions) {
            Integer pcode = region.getPcode();
            if (pcode == 0) {
                continue;
            }

            Map<String, Object> parentObj = retMap.get(String.valueOf(pcode));
            Map<String, Object> childObj = retMap.get(String.valueOf(region.getCode()));

            // 如果父节点存在，将此结点加入到父结点的子节点中
            if (parentObj != null) {
                List children = (List) parentObj.get("children");
                if (!children.contains(childObj)) {
                    children.add(childObj);
                }
            }
        }
        //开始组装选中状态
        retMap.remove("0"); // 去除区域未知
        for (Integer regionCode : selectedRegionCode) {
            Map<String, Object> regionInfo = retMap.get(String.valueOf(regionCode));
            if (regionInfo == null) {
                continue;
            }
            regionInfo.put("selected", Boolean.TRUE);
            retMap.put(String.valueOf(regionCode), regionInfo);
        }

        List<Map<String, Object>> retList = new ArrayList<>();

        // 此处不做权限的判断，而通过功能权限来控制是否可以访问本节点 By Wyc 2016-05-09
        Set<String> allKeySet = retMap.keySet();
        for (String regionCode : allKeySet) {
            Map<String, Object> regionItem = retMap.get(regionCode);
            if (regionItem.get("pcode") == null) {
                retList.add(regionItem);
            }
        }
        return JsonUtils.toJson(retList);
    }

    private int batchOnlineRawArticles(List<String> rawArticleIds, String styleType) {
        int count = 0;
        long toUserId = 0l;
        for (String rawArticleId : rawArticleIds) {
            ZyParentNewsRawArticle rawArticle = parentNewsService.loadRawArticleById(rawArticleId);
            if (rawArticle == null) {
                continue;
            }
            String content = rawArticle.getContent();
            // todo:文章是否是图文，音频，视频
            // 判断音视频，初始为图文
            JxtNewsContentType contentType = JxtNewsContentType.IMG_AND_TEXT;
            if (content.contains("<video")) {
                contentType = JxtNewsContentType.VIDEO;
            } else if (content.contains("<audio")) {
                contentType = JxtNewsContentType.AUDIO;
            }
            int category = 0;
            int wordsCount = jxtNewsLoaderClient.removeHtml(content).length();
            String title = rawArticle.getTitle();
            String remark = "";
            String digest = rawArticle.getDigest();
            String publisher = rawArticle.getPublisher();
            String sourceUrl = rawArticle.getUrl();
            String rid = rawArticle.getId();
            // 这里需要把推荐的头图和tags做处理后，放入edited_article中
            List<String> recommendHeadFigures = new ArrayList<>();
            List<Long> tagIds = new ArrayList<>();
            if (rawArticle != null) {
                List<String> headFigures = rawArticle.getHead_figures();
                if (headFigures != null && headFigures.size() > 0) {
                    // 现在把那本默认是一张小图模式，以后有需求再该这块
                    recommendHeadFigures.addAll(headFigures);
                }
                List<Map<String, Object>> sourceTags = rawArticle.getTags();
                if (sourceTags != null) {
                    // todo:这块待确认，龙龙或者大数据会否把弄了，要不然我就方了
                    // --将大数据给的tags映射到运营人工构建的标签树上面去
                    // trick，这里简单存入一个标签255
                    tagIds.addAll(sourceTags.stream().map(sourceTag -> SafeConverter.toLong(sourceTag.get("id"))).collect(Collectors.toList()));
                }
            }
            String editor = "auto";

            // String tags = getRequestString("tags");备用
            ZyParentNewsArticle zyParentNewsArticle = new ZyParentNewsArticle();
            zyParentNewsArticle.setContent(content);
            zyParentNewsArticle.setWords_count(SafeConverter.toLong(wordsCount));
            zyParentNewsArticle.setTitle(title);
            zyParentNewsArticle.setRemark(remark);
            zyParentNewsArticle.setDigest(digest);
            zyParentNewsArticle.setPublisher(publisher);
            zyParentNewsArticle.setSource_url(sourceUrl);
            zyParentNewsArticle.setEditor(editor);
            zyParentNewsArticle.setPushed(false);
            zyParentNewsArticle.setRaw_article_id(rid);
            zyParentNewsArticle.setDisabled(false);
            zyParentNewsArticle.setCategory(category);
            zyParentNewsArticle.setRecommend_head_figures(recommendHeadFigures);
            zyParentNewsArticle.setTagIds(tagIds);
            MapMessage mapMessage = parentNewsService.newArticle(zyParentNewsArticle);
            String articleId = (String) mapMessage.get("id");
            // 在原文中记录下编辑后的文章id
            if (StringUtils.isNotEmpty(rid)) {
                parentNewsService.setEditedFlag(rid, articleId);
                // step two:to vox_jxt_news
                // 如果没有tag就不放到线上了
                if (CollectionUtils.isEmpty(zyParentNewsArticle.getTagIds())) {
                    continue;
                }
                JxtNewsType jxtNewsType;
                List<String> imgList = new ArrayList<>();
                if (contentType == JxtNewsContentType.IMG_AND_TEXT) {
                    if (CollectionUtils.isNotEmpty(recommendHeadFigures)) {
                        // 如果有三张图就采用三张小图模式，如果只有一张图，就采用一张小图模式
                        if (recommendHeadFigures.size() >= 3 && RandomUtils.hitProbability(0.5d)) {
                            // 为了防止三张小图过多，这里55开，有一半变成一张小图
                            // 如果是音频或者是视频，就只支持一张大图或者小图
                            imgList.addAll(recommendHeadFigures.subList(0, 3));
                            jxtNewsType = JxtNewsType.THREE_IMAGES;
                        } else {
                            String url = recommendHeadFigures.get(0);
                            imgList.add(url);
                            jxtNewsType = JxtNewsType.SMALL_IMAGE;
                        }
                    } else {
                        jxtNewsType = JxtNewsType.TEXT;
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(recommendHeadFigures)) {
                        // 大图小图55开
                        String url = recommendHeadFigures.get(0);
                        imgList.add(url);
                        if (RandomUtils.hitProbability(0.5d)) {
                            jxtNewsType = JxtNewsType.SMALL_IMAGE;
                        } else {
                            jxtNewsType = JxtNewsType.BIG_IMAGE;
                        }
                    } else {
                        // 视频或者音频尽然没有配图
                        jxtNewsType = JxtNewsType.TEXT;
                    }
                }

                // 如果是旧版抓取的文章这里没有自动推荐的标签，直接用一个empty list代替


                String source = zyParentNewsArticle.getPublisher();
                Long availableUserId = toUserId;
                // todo: auto generate news use which chat group
                String chatGroupId = "";
                String chatGroupWelcomeContext = "";
                Boolean showAd = false;
                String regionIdStr = "";
                String albumId = "";
                String playTime = "";
                JxtNewsPushType pushType = JxtNewsPushType.ALL_USER;
                List<String> coverImgList = new ArrayList<>();
                // mark:因为资讯抓取的时候龙龙推荐的头图是放在线上gridfs中的，所以这里把头图往测试环境的gridfs也写一份
                if (RuntimeMode.lt(Mode.STAGING)) {
                    coverImgList = saveCoverToTestGridfs(imgList);
                } else {
                    // 因为头图直接用的时gridfs中的文件名，所以这里要strip前面的／
                    for (String cover : imgList) {
                        if (cover.contains("/")) {
                            String filename = cover.substring(cover.lastIndexOf('/') + 1);
                            coverImgList.add(filename);
                        } else {
                            coverImgList.add(cover);
                        }
                    }
                }
                List<Long> tagList = zyParentNewsArticle.getTagIds();
                List<Long> categoryList = new ArrayList<>();
                JxtNews jxtNews = new JxtNews();
                //推送方式
                jxtNews.setPushType(pushType.getType());
                if (pushType == JxtNewsPushType.ALL_USER) {
                    jxtNews.setAvailableUserId(0L);
                    jxtNews.setRegionCodeList(null);
                } else if (pushType == JxtNewsPushType.SIGNAL_USER) {
                    jxtNews.setAvailableUserId(availableUserId);
                    jxtNews.setRegionCodeList(null);
                } else {
                    jxtNews.setAvailableUserId(null);
                    Set<Integer> regionIds = new HashSet<>();
                    String[] split = regionIdStr.split(",");

                    for (String id : split) {
                        regionIds.add(SafeConverter.toInt(id));
                    }
                    Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);
                    if (MapUtils.isNotEmpty(exRegionMap)) {
                        jxtNews.setRegionCodeList(exRegionMap.values().stream().map(ExRegion::getId).collect(Collectors.toList()));
                    }
                }
                if (StringUtils.isEmpty(digest)) {
                    if (zyParentNewsArticle != null && StringUtils.isNotBlank(zyParentNewsArticle.getContent())) {
                        digest = jxtNewsLoaderClient.removeHtml(zyParentNewsArticle.getContent());
                        if (digest.length() > 200) {
                            digest = digest.substring(0, 200);
                        }
                    }
                }


                //摘要
                digest = digest.replace("\n", "");
                jxtNews.setDigest(digest);
                jxtNews.setTitle(title);
                jxtNews.setArticleId(articleId);
                jxtNews.setSource(source);
                jxtNews.setSourceUrl(sourceUrl);
                jxtNews.setCoverImgList(coverImgList);
                jxtNews.setTagList(tagList);
                jxtNews.setCategoryList(categoryList);
                jxtNews.setOperateUserName("auto");
                jxtNews.setJxtNewsType(jxtNewsType);
                jxtNews.setJxtNewsContentType(contentType);
                jxtNews.setChatGroupWelcomeContent(chatGroupWelcomeContext);
                jxtNews.setChatGroupId(chatGroupId);
                jxtNews.setShowAd(showAd);
                jxtNews.setAlbumId(albumId);
                jxtNews.setPlayTime(playTime);
                jxtNews.setOnline(false);
                jxtNews.setPushUser("auto");
                jxtNews.setJxtNewsStyleType(JxtNewsStyleType.valueOf(styleType));
//                jxtNews.setPushTime(new Date());
                jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
                count += 1;
                String id = jxtNews.getId();
                // 设置文章已发布
                parentNewsService.setPushedFlag(jxtNews.getArticleId(), id);
            }
        }
        return count;
    }

    private MapMessage generateAndSendAlbumMizarNotify(JxtNewsAlbum jxtNewsAlbum) {
        if (jxtNewsAlbum != null && StringUtils.isNotBlank(jxtNewsAlbum.getId())) {
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(jxtNewsAlbum.getId());
            if (album != null) {
                MizarNotify mizarNotify = new MizarNotify();
                if (!jxtNewsAlbum.getOnline()) {
                    mizarNotify.setTitle("专辑被下线");
                    mizarNotify.setContent("《" + jxtNewsAlbum.getTitle() + "》专辑和专辑内的文章被管理员下线，如有疑问请联系管理员");
                }
                mizarNotify.setType("ADMIN_NOTICE");
                return mizarNotifyServiceClient.sendNotify(mizarNotify, Collections.singleton(album.getMizarUserId()));
            }
        }
        return MapMessage.errorMessage();
    }


    private MapMessage generateAndSendNewsMizarNotify(JxtNews jxtNews, JxtNewsAlbum jxtNewsAlbum) {
        if (jxtNews != null && StringUtils.isNotBlank(jxtNews.getId())) {
            jxtNews = crmVendorService.$loadJxtNews(jxtNews.getId());
            if (jxtNews != null) {
                MizarNotify mizarNewsNotify = new MizarNotify();
                if (!jxtNews.getOnline()) {
                    mizarNewsNotify.setTitle("文章被下线");
                    mizarNewsNotify.setContent("文章：《" + jxtNews.getTitle() + "》被管理员移出专辑《" + jxtNewsAlbum.getTitle() + "》并下线，如有疑问请联系管理员");
                }
                mizarNewsNotify.setType("ADMIN_NOTICE");
                return mizarNotifyServiceClient.sendNotify(mizarNewsNotify, Collections.singleton(jxtNewsAlbum.getMizarUserId()));
            }
        }
        return MapMessage.errorMessage();
    }

    private MapMessage uploadFileTo17pmcOss(MultipartFile inputFile, String activityName) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = activityName + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = activityName + "/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
    }


    private String generateAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + url;
    }


    private String generateVideoImgBySnapshot(String url, String fileName) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
        String prefix = "parentnews-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
        try {
            @Cleanup InputStream inStream = new ByteArrayInputStream(response.getOriginalResponse());
            return crmImageUploader.upload(prefix, fileName, inStream);
        } catch (Exception ex) {
            log.error("上传咨询图片异常： " + ex.getMessage());
        }
        return "";
    }

    private List<String> generateXiMaLaYaUrl(String list_url) {
        if (StringUtils.isBlank(list_url)) {
            return Collections.emptyList();
        }
        List<String> soundIds = new ArrayList<>();
        try {
            Document document = Jsoup.connect(list_url).get();
            Elements album_soundlist = document.body().getElementsByClass("album_soundlist");
            if (CollectionUtils.isNotEmpty(album_soundlist)) {
                album_soundlist.forEach(e -> {
                    Elements li = e.getElementsByTag("li");
                    if (CollectionUtils.isNotEmpty(li)) {
                        li.forEach(o -> {
                            soundIds.add(o.attr("sound_id"));
                        });
                    }
                });
            }

        } catch (IOException e) {
            log.warn("fail parse ximalaya_url");
        }
        return soundIds;
    }


    private List<Map<String, String>> downloadAnduploadXiMaLaYa(List<String> soundIds) {
        if (CollectionUtils.isEmpty(soundIds)) {
            return Collections.emptyList();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        List<Map<String, String>> returnList = new ArrayList<>();
        soundIds.forEach(e -> {
            Map<String, String> map = new HashMap<>();
            String real_url = "http://www.ximalaya.com/tracks/" + e + ".json";
            AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(real_url).contentType("application/json;charset=utf-8").headers(headers).execute();
            String result_json = execute.getResponseString();
            if (StringUtils.isBlank(result_json)) {
                return;
            }
            Map<String, String> resultMap = JsonUtils.fromJsonToMapStringString(result_json);
            String play_path = resultMap.get("play_path");
            String id = resultMap.get("id");
            String title = resultMap.get("title");
            map.put("id", id);
            map.put("title", title);
            map.put("play_path", play_path);
            returnList.add(map);

        });

        return returnList;
    }

    private Map<String, String> xiMaLaYaInfo(String soundId) {
        if (StringUtils.isBlank(soundId)) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        Map<String, String> map = new HashMap<>();
        String real_url = "http://www.ximalaya.com/tracks/" + soundId + ".json";
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(real_url).contentType("application/json;charset=utf-8").headers(headers).execute();
        String result_json = execute.getResponseString();
        if (StringUtils.isBlank(result_json)) {
            return Collections.emptyMap();
        }
        Map<String, String> resultMap = JsonUtils.fromJsonToMapStringString(result_json);
        String play_path = resultMap.get("play_path");
        String id = resultMap.get("id");
        String title = resultMap.get("title");
        map.put("id", id);
        map.put("title", title);
        map.put("play_path", play_path);
        return map;
    }


    private void sendAlbumReminder(String albumId) {
        if (StringUtils.isBlank(albumId)) {
            return;
        }
        FlightRecorder.closeLog();
        AlpsThreadPool.getInstance().submit(() -> {
            Pageable pageable = new PageRequest(0, 1000);
            Page<Long> userIdsByPage = jxtNewsLoaderClient.getAlbumSubUserIdsByAlbumId(albumId, pageable);
            Set<Long> userIds = new HashSet<>();
            if (userIdsByPage.getTotalElements() == 0) {
                return;
            }
            //FIXME:这里处理第一次
            userIds.addAll(userIdsByPage.getContent());
            while (userIdsByPage.hasNext()) {
                //FIXME:这里处理第二至第N次
                pageable = userIdsByPage.nextPageable();
                userIdsByPage = jxtNewsLoaderClient.getAlbumSubUserIdsByAlbumId(albumId, pageable);
                userIds.addAll(userIdsByPage.getContent());
            }
            List<List<Long>> userSplitIds = CollectionUtils.splitList(new ArrayList<>(userIds), userIds.size() / 5000 + 1);
            for (List<Long> userIdList : userSplitIds) {
                jxtNewsServiceClient.sendMessageToReminder(userIdList);
                ThreadUtils.sleepCurrentThread(5, TimeUnit.SECONDS);
            }
        });
    }

}
