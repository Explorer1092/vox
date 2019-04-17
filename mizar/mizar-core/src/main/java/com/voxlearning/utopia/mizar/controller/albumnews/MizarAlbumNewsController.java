package com.voxlearning.utopia.mizar.controller.albumnews;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.utopia.business.api.ParentNewsService;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.misc.ZyParentNewsArticle;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.vendor.api.AsyncNewsCacheService;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiang wei
 * @since 2016/12/22.
 */
@Controller
@RequestMapping(value = "basic/albumnews")
public class MizarAlbumNewsController extends AbstractMizarController {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @ImportService(interfaceClass = AsyncNewsCacheService.class)
    private AsyncNewsCacheService asyncNewsCacheService;

    @Inject private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;

    @ImportService(interfaceClass = CRMVendorService.class) private CRMVendorService crmVendorService;
    @ImportService(interfaceClass = ParentNewsService.class) private ParentNewsService parentNewsService;

    @StorageClientLocation(storage = "17-pmc") private StorageClient storageClient;

    /**
     * Mizar专辑管理页
     */
    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String mizarUserId = getCurrentUser().getUserId();
        String inputAlbumName = getRequestString("albumName");

        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1));
        //这个用户的所有专辑
        List<JxtNewsAlbum> mizarUserAlbumList = getMizarUserAlbumList(mizarUserId).stream()
                .filter(p -> StringUtils.isBlank(inputAlbumName) || p.getTitle().contains(inputAlbumName))
                .collect(Collectors.toList());
        Pageable pageRequest = new PageRequest(pageIndex - 1, 10);
        Page<JxtNewsAlbum> newsAlbumPage = PageableUtils.listToPage(mizarUserAlbumList, pageRequest);
        mizarUserAlbumList = new ArrayList<>(newsAlbumPage.getContent());
        model.addAttribute("mizarAlbumList", mizarUserAlbumList);
        model.addAttribute("albumName", inputAlbumName);
        Set<String> mizarAlbumIds = mizarUserAlbumList.stream().map(JxtNewsAlbum::getId).collect(Collectors.toSet());
//        Map<String, Long> mizarUserAlbumSubCount = vendorCacheClient.getParentJxtCacheManager().loadAlbumSubCount();
        Map<String, Long> mizarUserAlbumSubCount = asyncNewsCacheService
                .JxtNewsCacheManager_loadCacheCount(JxtNewsCacheType.JXT_NEWS_ALBUM_SUB_COUNT, mizarAlbumIds)
                .take();
        model.addAttribute("mizarUserAlbumSubCount", mizarUserAlbumSubCount);
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("totalPage", newsAlbumPage.getTotalPages());
        return "basic/album/index";
    }

    @RequestMapping(value = "/news/index.vpage", method = RequestMethod.GET)
    public String newsIndex(Model model) {
        String mizarUserId = getCurrentUser().getUserId();
        String inputNewsTitle = getRequestString("newsTitle");
        String workFlowStatus = getRequestString("workFlowStatus");
        String contentType = getRequestString("contentType");
        int pageIndex = Integer.max(1, getRequestInt("pageIndex", 1));
        //这个用户的所有专辑
        List<JxtNewsAlbum> mizarUserAlbumList = getMizarUserAlbumList(mizarUserId);
        //专辑内的所有资讯
        Set<String> newsIds = new HashSet<>();
        mizarUserAlbumList.stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getNewsRecordList()))
                .forEach(p -> newsIds.addAll(p.getNewsRecordList().stream().map(JxtNewsAlbum.NewsRecord::getNewsId).collect(Collectors.toSet())));
        //过滤inputNewsTitle
        //过滤contentType
        List<JxtNews> mizarUserNewsList = crmVendorService.$loadsJxtNewsMap(newsIds).values()
                .stream()
                .filter(p -> StringUtils.isBlank(inputNewsTitle) || p.getTitle().contains(inputNewsTitle))
                .filter(p -> StringUtils.isBlank(contentType) || StringUtils.equalsIgnoreCase(p.generateContentType(), contentType))
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());
        Map<String, JxtNewsAlbum> mizarUserAlbumMap = mizarUserAlbumList.stream().collect(Collectors.toMap(JxtNewsAlbum::getId, Function.identity()));
        //可能存在的工作记录
        Set<Long> workFlowRecordIds = mizarUserNewsList.stream().filter(p -> p.getWorkFlowRecordId() != null).map(JxtNews::getWorkFlowRecordId).collect(Collectors.toSet());
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(workFlowRecordIds);
        //过滤workFlowStatus
        mizarUserNewsList = mizarUserNewsList.stream()
                //这是没选择审核状态
                .filter(p -> StringUtils.isBlank(workFlowStatus) ||
                        //这是选了的，并且选择状态是init的
                        (StringUtils.equals("init", workFlowStatus) && p.getWorkFlowRecordId() == null) ||
                        //这是选了。那么资讯的工作流不为空且工作流记录中的状态=选择的状态
                        (p.getWorkFlowRecordId() != null && workFlowRecordMap.get(p.getWorkFlowRecordId()) != null && StringUtils.equalsIgnoreCase(workFlowRecordMap.get(p.getWorkFlowRecordId()).getStatus(), workFlowStatus)))
                .collect(Collectors.toList());
        //把所有被驳回的工作流去查最后一条处理日志的具体原因
        Set<Long> needLoadRejectResionIds = workFlowRecordMap.values().stream().filter(p -> StringUtils.equalsIgnoreCase(p.getStatus(), "rejected")).map(WorkFlowRecord::getId).collect(Collectors.toSet());
        Map<Long, List<WorkFlowProcessHistory>> flowHistoriesMap = workFlowLoaderClient.loadWorkFlowProcessHistoriesByWorkFlowId(needLoadRejectResionIds);


        List<Map<String, Object>> mapList = new ArrayList<>();
        //分页处理
        Pageable pageRequest = new PageRequest(pageIndex - 1, 10);
        Page<JxtNews> jxtNewsPage = PageableUtils.listToPage(mizarUserNewsList, pageRequest);
        mizarUserNewsList = new ArrayList<>(jxtNewsPage.getContent());
        //逐个处理
        for (JxtNews jxtNews : mizarUserNewsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", jxtNews.getId());
            //标题
            map.put("title", jxtNews.getTitle());
            //文章类型
            map.put("contentType", jxtNews.getJxtNewsContentType() != null ? JxtNewsContentType.parse(jxtNews.generateContentType()).getDesc() : "");
            //所属专辑
            if (StringUtils.isNotBlank(jxtNews.getAlbumId())) {
                JxtNewsAlbum jxtNewsAlbum = mizarUserAlbumMap.get(jxtNews.getAlbumId());
                map.put("albumName", jxtNewsAlbum == null ? "" : jxtNewsAlbum.getTitle());
            } else {
                map.put("albumName", "");
            }
            //审核状态
            String flowStatus = "新建";
            //驳回原因
            String rejectReason = "";
            if (jxtNews.getWorkFlowRecordId() != null) {
                WorkFlowRecord workFlowRecord = workFlowRecordMap.get(jxtNews.getWorkFlowRecordId());
                if (workFlowRecord != null) {
                    if (StringUtils.equalsIgnoreCase("init", workFlowRecord.getStatus())) {
                        flowStatus = "新建";
                    } else if (StringUtils.equalsIgnoreCase("lv1", workFlowRecord.getStatus())) {
                        flowStatus = "待审核";
                    } else if (StringUtils.equalsIgnoreCase("processed", workFlowRecord.getStatus())) {
                        flowStatus = "通过";
                    } else if (StringUtils.equalsIgnoreCase("rejected", workFlowRecord.getStatus())) {
                        flowStatus = "驳回";
                    } else {
                        flowStatus = "未知";
                    }
                    List<WorkFlowProcessHistory> histories = flowHistoriesMap.get(workFlowRecord.getId());
                    if (StringUtils.equalsIgnoreCase(workFlowRecord.getStatus(), "rejected") && CollectionUtils.isNotEmpty(histories)) {
                        WorkFlowProcessHistory history = histories.stream().sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).findFirst().orElse(null);
                        rejectReason = history == null ? "" : history.getProcessNotes();
                    }
                }
            }
            map.put("workFlowStatus", flowStatus);
            map.put("rejectReason", rejectReason);
            map.put("onlineStatus", jxtNews.getOnline() ? "已上线" : "未上线");
            mapList.add(map);
        }
        //资讯样式类型
        List<Map<String, Object>> contentTypeList = new ArrayList<>();
        for (JxtNewsContentType type : JxtNewsContentType.values()) {
            if (type.getType() > 3 || type.getType() < 0) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("name", type.name());
            map.put("desc", type.getDesc());
            contentTypeList.add(map);
        }
        model.addAttribute("pageIndex", pageIndex);
        model.addAttribute("totalPage", jxtNewsPage.getTotalPages());
        model.addAttribute("newsTitle", inputNewsTitle);
        model.addAttribute("contentType", contentType);
        model.addAttribute("workFlowStatus", workFlowStatus);
        model.addAttribute("contentTypeList", contentTypeList);
        model.addAttribute("mizarUserNewsList", mapList);
        model.addAttribute("mizarUserAlbumList", mizarUserAlbumList);
        return "basic/album/news/index";
    }

    /**
     * 编辑专辑
     */
    @RequestMapping(value = "albumedit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String albumEdit(Model model) {
        String albumId = getRequestString("albumId");
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(albumId)) {
            JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
            if (album == null) {
                model.addAttribute("专辑" + albumId + "不存在");
                return "basic/albumnews/index";
            }
            map = generateAlbumInfoMap(album);
        }
        model.addAttribute("albumInfo", map);
        return "basic/album/albumedit";
    }

    /**
     * 音频上传
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "uploadaudio.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadAudio(MultipartFile file) {

        return uploadAudioCommon(file);
    }

    /**
     * 新建或编辑文章
     */
    @RequestMapping(value = "/news/albumnewsedit.vpage", method = RequestMethod.GET)
    public String newsEdit(Model model) {
        String mizarUserId = getCurrentUser().getUserId();

        String newsId = getRequestString("newsId");
        model.addAttribute("newsId", newsId);
        if (StringUtils.isNotEmpty(newsId)) {
            JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews != null) {
                model.addAttribute("title", jxtNews.getTitle());
                if (CollectionUtils.isNotEmpty(jxtNews.getCoverImgList())) {
                    model.addAttribute("imgUrl", StringUtils.isNotBlank(jxtNews.getCoverImgList().get(0)) ? generateAliYunImgUrl(jxtNews.getCoverImgList().get(0)) : "");
                    model.addAttribute("imgfile", StringUtils.isNotBlank(jxtNews.getCoverImgList().get(0)) ? jxtNews.getCoverImgList().get(0) : "");
                }
                model.addAttribute("jxtNewsContentType", jxtNews.getJxtNewsContentType());
                model.addAttribute("playTime", StringUtils.isNotBlank(jxtNews.getPlayTime()) ? jxtNews.getPlayTime() : "");
                model.addAttribute("source", StringUtils.isNotBlank(jxtNews.getSource()) ? jxtNews.getSource() : "");
                String albumId = jxtNews.getAlbumId();
                if (StringUtils.isNotBlank(albumId)) {
                    JxtNewsAlbum album = crmVendorService.$loadJxtNewsAlbum(albumId);
                    if (album != null) {
                        model.addAttribute("currentAlbumId", album.getId());
                    }
                }
                ZyParentNewsArticle article = parentNewsService.loadArticleById(jxtNews.getArticleId());
                if (article != null) {
                    model.addAttribute("content", article.getContent());
                }
            }
        }
        //这个用户的所有专辑
        List<JxtNewsAlbum> mizarUserAlbumList = getMizarUserAlbumList(mizarUserId);
        model.addAttribute("mizarUserAlbumList", mizarUserAlbumList);

        return "basic/album/news/albumnewsedit";
    }

    @RequestMapping(value = "/news/addworkflow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addWorkFlow() {
        MizarAuthUser mizarUser = getCurrentUser();
        String newsId = getRequestString("newsId");
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("请选择您提交审核的资讯");
        }
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("您提交审核的资讯不存在");
        }
        if (jxtNews.getWorkFlowRecordId() != null) {
            Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(jxtNews.getWorkFlowRecordId()));
            if (MapUtils.isNotEmpty(workFlowRecordMap)) {
                WorkFlowRecord workFlowRecord = workFlowRecordMap.get(jxtNews.getWorkFlowRecordId());
                if (!StringUtils.equals(workFlowRecord.getStatus(), "rejected")) {
                    return MapMessage.errorMessage("请勿重复提交审核");
                }
            }
        }
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setSourceApp("mizar");
        workFlowRecord.setCreatorAccount(mizarUser.getUserId());
        workFlowRecord.setCreatorName(mizarUser.getAccountName());
        workFlowRecord.setTaskName(newsId);
        workFlowRecord.setTaskContent("提交资讯审核。资讯id为:" + newsId);
        workFlowRecord.setLatestProcessorName(mizarUser.getAccountName());
        workFlowRecord.setStatus("init");
        MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord);
        if (mapMessage.isSuccess()) {
            WorkFlowRecord savedRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
            //写入到jxtNews
            jxtNews.setWorkFlowRecordId(savedRecord.getId());
            crmVendorService.$upsertJxtNews(jxtNews);
            //写入process
            WorkFlowContext workFlowContext = new WorkFlowContext();
            workFlowContext.setSourceApp("mizar");
            workFlowContext.setWorkFlowRecord(savedRecord);
            workFlowContext.setProcessorAccount(mizarUser.getAccountName());
            workFlowContext.setProcessorName("mizar:" + mizarUser.getRealName());
            workFlowContext.setProcessNotes("提交资讯审核:" + jxtNews.getId());
            workFlowContext.setWorkFlowName("mizar_admin_album_news_check");
            mapMessage = workFlowServiceClient.agree(workFlowContext);
        }
        return mapMessage;
    }

    @RequestMapping(value = "/news/edituploadimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ueditorController(MultipartFile inputFile) {
        String activityName = "jxtNews";
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            return uploadFileTo17pmcOss(inputFile, activityName);
        } catch (Exception e) {
            logger.error("Mizar资讯图片上传失败{}", e);
            return MapMessage.errorMessage("上传文件失败");
        }
    }

    /**
     * 保存文章
     */
    @RequestMapping(value = "/news/savenews.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNews() {
        String mizarUserId = getCurrentUser().getUserId();

        // 保存jxtnews
        String newsId = getRequestString("newsId");
        String albumId = getRequestString("albumId");
        String imgUrl = getRequestString("imgUrl");
        String playTime = getRequestString("playTime");
        String source = getRequestString("source");
        if (StringUtils.isBlank(albumId)) {
            return MapMessage.errorMessage("专辑ID不能为空");
        }
        List<JxtNewsAlbum> mizarUserAlbumList = getMizarUserAlbumList(mizarUserId);
        if (!mizarUserAlbumList.stream().anyMatch(p -> p.getId().equals(albumId))) {
            return MapMessage.errorMessage("该专辑不属于您或没有上线");
        }

        JxtNews jxtNews = null;
        String oldAlbumId = "";
        if (StringUtils.isNotEmpty(newsId)) {
            jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews == null) {
                return MapMessage.errorMessage("id为{}的资讯不存在", newsId);
            }
            oldAlbumId = jxtNews.getAlbumId();
            jxtNews.setUpdateTime(new Date());
        }

        if (jxtNews == null) {
            jxtNews = new JxtNews();
        }

        // 编辑操作不改变上下线状态
        // 新增或者是编辑后都是自动下线
        jxtNews.setOnline(Boolean.FALSE);

        // 将编辑好的html存入mongo,返回新生成的id
        //这些是 zyParentNewsArticle
        String content = getRequestString("content");
        Long wordsCount = getRequestLong("words_count");
        String title = getRequestString("title");
        String remark = getRequestString("remark");
        String publisher = getRequestString("publisher");
        String sourceUrl = getRequestString("source_url");
        String rid = getRequestString("rid");
        //摘要
        String digest = "";
        if (StringUtils.isNotBlank(content)) {
            digest = jxtNewsLoaderClient.removeHtml(content);
            if (digest.length() > 200) {
                digest = digest.substring(0, 200);
            }
        }
        String editor = getCurrentUser().getAccountName();

        // String tags = getRequestString("tags");备用
        ZyParentNewsArticle zyParentNewsArticle = new ZyParentNewsArticle();
        zyParentNewsArticle.setContent(content);
        zyParentNewsArticle.setWords_count(wordsCount);
        zyParentNewsArticle.setTitle(title);
        zyParentNewsArticle.setRemark(remark);
        zyParentNewsArticle.setDigest(digest);
        zyParentNewsArticle.setPublisher(publisher);
        zyParentNewsArticle.setSource_url(sourceUrl);
        zyParentNewsArticle.setEditor(editor);
        zyParentNewsArticle.setPushed(true);
        zyParentNewsArticle.setRaw_article_id(rid);
        zyParentNewsArticle.setDisabled(false);
        //zyParentNewsArticle.setCategory(category);

        MapMessage mapMessage;

        String articleId = jxtNews.getArticleId();
        if (StringUtils.isNotBlank(articleId)) {
            parentNewsService.updateArticle(articleId, zyParentNewsArticle);
        } else {
            mapMessage = parentNewsService.newArticle(zyParentNewsArticle);
            articleId = (String) mapMessage.get("id");
        }


        // 在原文中记录下编辑后的文章id
        if (StringUtils.isNotEmpty(rid)) {
            parentNewsService.setEditedFlag(rid, articleId);
        }

        String contentType = getRequestString("contentType");
        jxtNews.setDigest(digest);
        jxtNews.setTitle(title);
        jxtNews.setArticleId(articleId);
        jxtNews.setSourceUrl(sourceUrl);
        jxtNews.setSource(source);
        List<String> imgList = new ArrayList<>();
        imgList.add(imgUrl);
        jxtNews.setCoverImgList(imgList);
        jxtNews.setTagList(Collections.emptyList());
        jxtNews.setOperateUserName(editor);
        jxtNews.setPushType(2);
        jxtNews.setShowAd(Boolean.FALSE);
        jxtNews.setAlbumId(albumId);
        jxtNews.setPlayTime(playTime);
        jxtNews.setFree(Boolean.TRUE);
        jxtNews.setJxtNewsType(JxtNewsType.SMALL_IMAGE);

        // 如果是编辑的话，投稿状态重置
        jxtNews.setJxtNewsContentType(JxtNewsContentType.parse(contentType));
        jxtNews.setJxtNewsStyleType(JxtNewsStyleType.EXTERNAL_ALBUM_NEWS);

        // 推送给全部用户
        jxtNews.setAvailableUserId(0L);

        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (jxtNews == null) {
            return MapMessage.errorMessage();
        }

        newsId = jxtNews.getId();
        // 设置文章已发布
        parentNewsService.setPushedFlag(jxtNews.getArticleId(), newsId);
        JxtNewsAlbum oldAlbum = crmVendorService.$loadJxtNewsAlbum(oldAlbumId);
        final String finalNewsId = newsId;
        if (!StringUtils.equals(oldAlbumId, albumId)) {
            if (oldAlbum != null) {
                List<JxtNewsAlbum.NewsRecord> oldAlbumNewsList = oldAlbum.getNewsRecordList().stream().filter(e -> !StringUtils.equals(e.getNewsId(), finalNewsId)).collect(Collectors.toList());
                oldAlbum.setNewsRecordList(oldAlbumNewsList);
                crmVendorService.$upsertJxtNewsAlbum(oldAlbum);
            }
            if (StringUtils.isNotBlank(albumId)) {
                JxtNewsAlbum newAlbum = crmVendorService.$loadJxtNewsAlbum(albumId);
                if (newAlbum != null) {
                    JxtNewsAlbum.NewsRecord newsRecord1 = newAlbum.getNewsRecordList().stream().sorted((o1, o2) -> Integer.compare(o2.getRank(), o1.getRank())).findFirst().orElse(null);
                    JxtNewsAlbum.NewsRecord newsRecord = new JxtNewsAlbum.NewsRecord();
                    newsRecord.setNewsId(newsId);
                    newsRecord.setCreateTime(new Date());
                    if (newsRecord1 != null) {
                        newsRecord.setRank(newsRecord1.getRank() + 1);
                    } else {
                        newsRecord.setRank(1);
                    }
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
                }
            }
        }

        // 反更新回article的newsId
        zyParentNewsArticle.setNews_id(newsId);
        mapMessage = MapMessage.of(parentNewsService.updateArticle(articleId, zyParentNewsArticle));

        return mapMessage;
    }


    @RequestMapping(value = "jxtnewsoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offlineJxtNews() {
        String newsId = getRequestString("newsId");
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews == null) {
            return MapMessage.errorMessage("下线的资讯不存在：" + newsId);
        }
        jxtNews = new JxtNews();
        jxtNews.setId(newsId);
        jxtNews.setOnline(false);
        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        return new MapMessage().setSuccess(jxtNews != null);
    }

    /**
     * 保存专辑
     */
    @RequestMapping(value = "saveAlbum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAlbum() {
        String albumId = getRequestString("albumId");
        String detail = getRequestString("detail");
        String newsRankList = getRequestString("newsRankList");
        String mizarUserId = getCurrentUser().getUserId();
        try {
            //文章和排序先处理一下，方便后面判断
            List<String> newsIdList = JsonUtils.fromJsonToList(newsRankList, String.class);
            Map<String, Integer> newsRankMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(newsIdList)) {
                newsIdList.forEach(e -> newsRankMap.put(e, newsIdList.size() - newsIdList.indexOf(e)));
            }
            JxtNewsAlbum album = null;
            if (StringUtils.isNotBlank(albumId)) {
                album = crmVendorService.$loadJxtNewsAlbum(albumId);
                if (!StringUtils.equals(album.getMizarUserId(), mizarUserId)) {
                    return MapMessage.errorMessage("您不是该专辑属主");
                }
            }
            if (album == null) {
                return MapMessage.errorMessage("未找到该专辑");
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
                oldList.removeAll(removeList);
                for (JxtNewsAlbum.NewsRecord record : removeList) {
                    JxtNews jxtNews = crmVendorService.$loadJxtNews(record.getNewsId());
                    if (jxtNews != null) {
                        jxtNews.setAlbumId("");
                        jxtNews.setOnline(Boolean.FALSE);
                        crmVendorService.$upsertJxtNews(jxtNews);
                    }
                }

                List<JxtNewsAlbum.NewsRecord> oldNewsList = oldList.stream().filter(newsRecords::contains).collect(Collectors.toList());
                List<JxtNewsAlbum.NewsRecord> copyOldNewsList = new ArrayList<>();
                for (JxtNewsAlbum.NewsRecord newsRecord : oldNewsList) {
                    Integer rank = newsRankMap.get(newsRecord.getNewsId());
                    newsRecord.setRank(rank);
                    copyOldNewsList.add(newsRecord);
                }
                oldList.clear();
                oldList.addAll(copyOldNewsList);
            }
            //详情
            album.setDetail(detail);
            if (CollectionUtils.isNotEmpty(oldList)) {
                album.setNewsRecordList(oldList);
            }
            album.setDisabled(Boolean.FALSE);
            //如果albumId为空说明是首次存储。上线标志置为false
            if (StringUtils.isBlank(albumId)) {
                album.setOnline(Boolean.FALSE);
            }
            album = crmVendorService.$upsertJxtNewsAlbum(album);
            if (album == null) {
                return MapMessage.errorMessage();
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    MapMessage mapMessage = uploadFileTo17pmcOss(imgFile, "jxtnews");
                    if (!mapMessage.isSuccess()) {
                        return mapMessage;
                    }
                    return MapMessage.successMessage()
                            .add("url", mapMessage.get("fileUrl"))
                            .add("title", imgFile.getName())
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
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
        return MapMessage.successMessage().add("fileName", realName).add("fileUrl", fileUrl);
    }


    //生成专辑的详细信息
    private Map<String, Object> generateAlbumInfoMap(JxtNewsAlbum jxtNewsAlbum) {
        Map<String, Object> map = new HashMap<>();
        if (jxtNewsAlbum == null) {
            return map;
        }
        map.put("albumId", jxtNewsAlbum.getId());

        map.put("title", jxtNewsAlbum.getTitle());

        map.put("headImg", combineImgUrl(jxtNewsAlbum.getHeadImg()));

        map.put("headImgName", jxtNewsAlbum.getHeadImg());

        map.put("detail", jxtNewsAlbum.getDetail());

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

        Set<String> newsIds = newsRankMap.keySet();
        Collection<JxtNews> jxtNewsList = crmVendorService.$loadsJxtNewsMap(newsIds).values();
        Map<String, String> idTitleMap = jxtNewsList.stream()
                .collect(Collectors.toMap(JxtNews::getId, JxtNews::getTitle));
        map.put("newsTitleMap", idTitleMap);

        Map<String, Boolean> idOnlineMap = jxtNewsList.stream()
                .collect(Collectors.toMap(JxtNews::getId, JxtNews::getOnline));
        map.put("newsOnlineMap", idOnlineMap);

        Set<Long> workFlowRecordIds = jxtNewsList.stream().map(JxtNews::getWorkFlowRecordId).collect(Collectors.toSet());
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(workFlowRecordIds);
        Map<String, String> idStatusMap = jxtNewsList.stream()
                .collect(Collectors.toMap(JxtNews::getId, e -> workFlowRecordMap.get(e.getWorkFlowRecordId()) != null ? generateWorkFlowStatus(workFlowRecordMap.get(e.getWorkFlowRecordId())) : "新建"));
        map.put("newsStatusMap", idStatusMap);
        return map;
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

    private List<JxtNewsAlbum> getMizarUserAlbumList(String mizarUserId) {
        if (StringUtils.isBlank(mizarUserId)) {
            return Collections.emptyList();
        }
        return crmVendorService.$loadJxtNewsAlbumList()
                .stream()
                .filter(p -> p.getJxtNewsAlbumType() == JxtNewsAlbumType.EXTERNAL_MIZAR)
                .filter(JxtNewsAlbum::getOnline)
                .filter(p -> StringUtils.equals(mizarUserId, p.getMizarUserId()))
                .collect(Collectors.toList());
    }

    private String generateAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + url;
    }

    private String generateWorkFlowStatus(WorkFlowRecord workFlowRecord) {
        if (workFlowRecord == null) {
            return "";
        }
        if (StringUtils.equals(workFlowRecord.getStatus(), "lv1")) {
            return "待审核";
        } else if (StringUtils.equals(workFlowRecord.getStatus(), "processed")) {
            return "通过";
        } else if (StringUtils.equals(workFlowRecord.getStatus(), "rejected")) {
            return "驳回";
        } else {
            return "";
        }
    }
}
