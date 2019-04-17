package com.voxlearning.utopia.mizar.controller.officialaccount;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.business.api.ParentNewsService;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.misc.ZyParentNewsArticle;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsArticle;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsContentType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsStyleType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公众号 Controller
 * Created by haitian.gan on 2016/11/7.
 */
@Controller
@RequestMapping(value = "/basic/officialaccount")
public class OfficialAccountManageController extends AbstractMizarController {

    private static final int QUERY_NEWS_RANGE = -2;
    private static List<String> IMG_SUFFIX = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png", "mp3"); // 此处维护上传图片的后缀
    private static final int SELECT_NUM = 5;

    // url生成模板
    private static final String GEN_URL_TPL =
            ProductConfig.getMainSiteBaseUrl().replace("http", "https") +
                    "/view/mobile/parent/information/detail?" +
                    "rel=list_0&" +
                    "newsId={0}&" +
                    "ut={4,number,#.##}&" +
                    "content_type={1}&" +
                    "style_type={2}&" +
                    "accountsKey={3}";

    @Inject
    private OfficialAccountsServiceClient officialAccountsServiceClient;

    @ImportService(interfaceClass = ParentNewsService.class)
    private ParentNewsService parentNewsService;

    @ImportService(interfaceClass = CRMVendorService.class)
    private CRMVendorService crmVendorService;

    /**
     * 公众号基本信息
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        List<OfficialAccounts> accountList = getAccountList();
        model.addAttribute("accountList", accountList);
        return "basic/officialaccount/index";
    }

    /**
     * 当前用户下的公众号列表
     *
     * @return
     */
    @RequestMapping(value = "accountlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage accountList() {
        return MapMessage.successMessage().add("accountList", getAccountList());
    }

    @RequestMapping(value = "account.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateAccount() {
        Long accountId = getRequestLong("accountId", 0L);
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在!");

        // 更新问候语
        String greetings = getRequestString("greetings");
        String title = getRequestString("title");// 副标题
        accounts.setGreetings(greetings);
        accounts.setTitle(title);

        return officialAccountsServiceClient.updateAccounts(accountId, accounts);
    }

    /**
     * 素材管理页
     */
    @RequestMapping(value = "material.vpage", method = RequestMethod.GET)
    public String material() {

        return "basic/officialaccount/material";
    }

    /**
     * 素材管理页
     */

    @RequestMapping(value = "articleedit.vpage", method = RequestMethod.GET)
    public String editArticle(Model model) {
        Long accountId = getRequestLong("accountId");
        model.addAttribute("accountId", accountId);

        String bundleId = getRequestString("bundleId");
        List<OfficialAccountsArticle> articles = officialAccountsServiceClient.loadArticlesInTpl(bundleId);

        if (articles.size() > 0) {
            model.addAttribute("hasSend", articles.get(0).getHasSend());
            model.addAttribute("bindSid", articles.get(0).getBindSid());

            // 撤回状态的话，不能再保存了
            model.addAttribute("isSavable",
                    articles.get(0).getStatus() != OfficialAccountsArticle.Status.Offline);
        } else {
            model.addAttribute("isSavable", true);
        }

        // 选取最近的五篇素材
        List<Map<String, Object>> materials = getJxtNewsList(accountId, null);
        materials = materials.subList(0, Math.min(materials.size(), SELECT_NUM));

        model.addAttribute("articles", articles);
        model.addAttribute("materials", materials);
        return "basic/officialaccount/articleedit";
    }

    /**
     * 素材管理，查询文章
     */
    @RequestMapping(value = "materiallist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryMaterials() {
        /*int page = getRequestInt("currentPage");
        page = page - 1;
        if(page < 0)
            page = 0;

        String title = getRequestString("title");
        Pageable pageable = new PageRequest(page, 10, Sort.Direction.DESC, "update_datetime");
        Page<ZyParentNewsArticle> zyParentNewsArticleList = parentNewsService.loadNextPageArticles(
                pageable, false, title, "", 0);
        return MapMessage.successMessage().add("articles", zyParentNewsArticleList);*/
        Long accountId = pickAccountId();
        if (accountId == 0L)
            return MapMessage.errorMessage("此账号未关联公众号！");

        // 标题
        String title = getRequestString("title");
        List<Map<String, Object>> materials = getJxtNewsList(accountId, title);

        return MapMessage.successMessage().add("jxtNewsList", materials);
    }

    /**
     * 获得家长通资讯信息列表
     *
     * @param accountId
     * @param title
     * @return
     */
    private List<Map<String, Object>> getJxtNewsList(Long accountId, String title) {

        if (accountId == null)
            return null;

        // 查询两个月范围以内的
        Date now = new Date();
        Date startDatetime = DateUtils.addMonths(now, QUERY_NEWS_RANGE);

        List<JxtNews> jxtNewsList = crmVendorService.$loadJxtNewsByRelateId(
                accountId.toString(), startDatetime, now);
        List<Map<String, Object>> materials = jxtNewsList.stream()
                .filter(p1 -> StringUtils.isEmpty(title) ||
                        (StringUtils.isNotEmpty(p1.getTitle()) && p1.getTitle().contains(title)))
                .sorted((o1, o2) -> o1.getUpdateTime().before(o2.getUpdateTime()) ? 1 : -1)
                .map(p -> {

                    Map<String, Object> materialData = new HashMap<>();
                    materialData.put("id", p.getId());
                    materialData.put("title", p.getTitle());
                    materialData.put("generateUrl", getNewsUrl(p));
                    materialData.put("createTime", DateUtils.dateToString(p.getCreateTime()));
                    materialData.put("updateTime", DateUtils.dateToString(p.getUpdateTime()));

                    // 投稿标志
                    if (p.getJxtNewsStyleType() == JxtNewsStyleType.OFFICIAL_ACCOUNT_SUBMIT) {
                        materialData.put("submitted", true);
                    } else
                        materialData.put("submitted", false);

                    return materialData;
                })
                .collect(Collectors.toList());

        return materials;
    }

    /**
     * 素材投稿
     *
     * @return
     */
    @RequestMapping(value = "submitmaterial.vpage")
    @ResponseBody
    public MapMessage submitMaterial() {
        String newsId = getRequestString("id");
        if (StringUtils.isEmpty(newsId))
            return MapMessage.errorMessage("id为空");

        JxtNews news = crmVendorService.$loadJxtNews(newsId);
        if (news == null) {
            return MapMessage.errorMessage("资讯文章不存在");
        }

        news.setJxtNewsContentType(JxtNewsContentType.IMG_AND_TEXT);
        news.setJxtNewsStyleType(JxtNewsStyleType.OFFICIAL_ACCOUNT_SUBMIT);
        crmVendorService.$upsertJxtNews(news);
        return MapMessage.successMessage();
    }

    /**
     * 素材管理，新建或编辑文章
     *
     * @return
     */
    @RequestMapping(value = "materialedit.vpage", method = RequestMethod.GET)
    public String newMaterials(Model model) {

        String newsId = getRequestString("id");
        boolean submitted = getRequestBool("submitted");
        model.addAttribute("newsId", newsId);
        model.addAttribute("submitted", submitted);
        if (StringUtils.isNotEmpty(newsId)) {
            JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews != null) {
                model.addAttribute("generateUrl", getNewsUrl(jxtNews));
                model.addAttribute("title", jxtNews.getTitle());
                model.addAttribute("digest", jxtNews.getDigest());
                model.addAttribute("sourceUrl", jxtNews.getSourceUrl());

                ZyParentNewsArticle article = parentNewsService.loadArticleById(jxtNews.getArticleId());
                if (article != null)
                    model.addAttribute("content", article.getContent());
            }
        }

        return "basic/officialaccount/materialedit";
    }

    /**
     * 拼接url
     *
     * @param news
     * @return
     */
    private String getNewsUrl(JxtNews news) {
        return MessageFormat.format(
                GEN_URL_TPL,
                news.getId(),
                news.generateContentType(),
                news.getJxtNewsStyleType() == null ?
                        JxtNewsStyleType.OFFICIAL_ACCOUNT.name() : news.generateStyleType(),
                news.getRelateId(),
                System.currentTimeMillis());
    }

    /**
     * 素材管理，保存文章
     *
     * @return
     */
    @RequestMapping(value = "savematerial.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveMaterial() {

        boolean updateMode = false;
        // 保存jxtnews
        String newsId = getRequestString("newsId");
        JxtNews jxtNews = null;
        if (StringUtils.isNotEmpty(newsId)) {
            jxtNews = crmVendorService.$loadJxtNews(newsId);
            if (jxtNews == null) {
                return MapMessage.errorMessage("id为{}的素材不存在", newsId);
            }

            jxtNews.setUpdateTime(new Date());
            updateMode = true;
        }

        Long accountId = pickAccountId();
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在!");

        if (jxtNews == null) {
            jxtNews = new JxtNews();
        }

        // 编辑操作不改变上下线状态
        // 新增或者是编辑后都是自动下线
        jxtNews.setOnline(Boolean.FALSE);

        // 将编辑好的html存入mongo,返回新生成的id
        String content = getRequestString("content");
        Long wordsCount = getRequestLong("words_count");
        String title = getRequestString("title");
        String remark = getRequestString("remark");
        String digest = getRequestString("digest");
        String publisher = getRequestString("publisher");
        String sourceUrl = getRequestString("source_url");
        String rid = getRequestString("rid");
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
        zyParentNewsArticle.setPushed(false);
        zyParentNewsArticle.setRaw_article_id(rid);
        zyParentNewsArticle.setDisabled(false);
        //zyParentNewsArticle.setCategory(category);

        MapMessage mapMessage;
        String articleId;

        if (updateMode) {
            articleId = jxtNews.getArticleId();
            mapMessage = parentNewsService.updateArticle(articleId, zyParentNewsArticle);
        } else {
            mapMessage = parentNewsService.newArticle(zyParentNewsArticle);
            articleId = (String) mapMessage.get("id");
        }

        // 在原文中记录下编辑后的文章id
        if (StringUtils.isNotEmpty(rid)) {
            parentNewsService.setEditedFlag(rid, articleId);
        }

        jxtNews.setDigest(digest);
        jxtNews.setTitle(title);
        jxtNews.setArticleId(articleId);
        jxtNews.setSourceUrl(sourceUrl);
        jxtNews.setSource(accounts.getName());
        //jxtNews.setCoverImgList(imgList);
        jxtNews.setTagList(Collections.emptyList());
        //jxtNews.setCategoryList(categoryList);
        jxtNews.setOperateUserName(editor);
        jxtNews.setJxtNewsType(JxtNewsType.TEXT);

        // 新建的话，默认是未投稿状态
        // 如果是编辑的话，投稿状态重置
        //jxtNews.setJxtNewsContentType(JxtNewsContentType.OFFICIAL_ACCOUNT);
        jxtNews.setJxtNewsContentType(JxtNewsContentType.IMG_AND_TEXT);
        jxtNews.setJxtNewsStyleType(JxtNewsStyleType.OFFICIAL_ACCOUNT);

        //jxtNews.setChatGroupWelcomeContent(chatGroupWelcomeContext);
        //jxtNews.setChatGroupId(chatGroupId);
        //jxtNews.setShowAd(showAd);
        // 未上线的都不置值
        //jxtNews.setPushTime(new Date());
        //jxtNews.setPushUser(getCurrentUser().getAccountName());

        // 关联上公众号
        jxtNews.setRelateId(String.valueOf(accounts.getId()));
        jxtNews.setRelateContent(accounts.getName());

        // 推送给全部用户
        jxtNews.setAvailableUserId(0L);

        jxtNews = crmVendorService.$upsertJxtNews(jxtNews);
        if (jxtNews == null) {
            return MapMessage.errorMessage();
        }

        newsId = jxtNews.getId();
        // 设置文章已发布
        parentNewsService.setPushedFlag(jxtNews.getArticleId(), newsId);

        // 反更新回article的newsId
        zyParentNewsArticle.setNews_id(newsId);
        mapMessage.of(parentNewsService.updateArticle(articleId, zyParentNewsArticle));

        return mapMessage;
    }

    @RequestMapping(value = "articlelist.vpage", method = RequestMethod.GET)
    public String articleList() {

        return "basic/officialaccount/articlelist";
    }

    private Long pickAccountId() {
        return getAccountList()
                .stream()
                .findFirst()
                .map(a -> a.getId())
                .orElse(0L);
    }

    /**
     * 发布管理，获得公众号下面的文章列表
     *
     * @return
     */
    @RequestMapping(value = "articlelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage queryArticle() {
        Long accountId = getRequestLong("accountId");
        // 如果为空，默认选第一个公众号的
        if (accountId == 0L)
            accountId = pickAccountId();

        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在!");

        String startDate = getRequestString("start");
        String endDate = getRequestString("end");
        String status = getRequestString("status");

        List<Map<String, Object>> articles = officialAccountsServiceClient.queryArticle(
                accountId, startDate, endDate, status);

        MapMessage resultMsg = MapMessage.successMessage()
                .add("accountList", getAccountList())
                .add("accountId", accountId)
                .add("result", articles);

        MapMessage getLimitTimesMsg = officialAccountsServiceClient.getPublishNumsLimit(accountId);
        if (getLimitTimesMsg.isSuccess()) {
            resultMsg.putAll(getLimitTimesMsg);
        }

        return resultMsg;
    }

    // 保存文章 POST
    @RequestMapping(value = "savearticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveArticle(@RequestBody Map<String, Object> jsonObject) {
        // 获取参数
        if (MapUtils.isEmpty(jsonObject)) {
            return MapMessage.errorMessage("错误参数");
        }

        // 获取文章列表
        List<Map<String, Object>> articleList = (List<Map<String, Object>>) jsonObject.get("articleList");
        if (CollectionUtils.isEmpty(articleList)) {
            return MapMessage.errorMessage("文章内容不能为空");
        }

        // 公众号ID
        Long accountId = SafeConverter.toLong(jsonObject.get("accountId"));
        if (accountId == 0L) {
            return MapMessage.errorMessage("公众号ID不存在");
        }

        // 是否需要发送JPUSH
        boolean sendJpush = SafeConverter.toBoolean(jsonObject.get("sendJpush"));
        // 是否拼接SID
        boolean bindSid = SafeConverter.toBoolean(jsonObject.get("bindSid"));

        return officialAccountsServiceClient.saveArticle(articleList, sendJpush, bindSid, accountId);
    }

    /**
     * 撤回文章
     *
     * @return
     */
    @RequestMapping(value = "articleoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage articleOffline() {
        String bundleId = getRequestString("bundleId");
        Long accountId = getRequestLong("accountId");
        if (StringUtils.isEmpty(bundleId) || accountId == 0L) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            MapMessage returnMsg = officialAccountsServiceClient.updateArticleStatus(accountId, bundleId, OfficialAccountsArticle.Status.Offline);
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to offline official account article:id={},bundleId={},ex={}", accountId, bundleId, ex.getMessage(), ex);
            return MapMessage.errorMessage("公众号文章撤回失败:{}", ex.getMessage(), ex);
        }
    }

    // 发布文章
    @RequestMapping(value = "publisharticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishArticle() {

        String bundleId = getRequestString("bundleId");
        List<OfficialAccountsArticle> articles = officialAccountsServiceClient.loadArticlesByBundleId(bundleId);

        boolean sendJpush = articles.get(0).getHasSend();
        String jpushContent = articles.get(0).getArticleTitle();
        long accountId = articles.get(0).getAccountId();

        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在");

        // 如果发布次数用光，则报错返回!
        if (!officialAccountsServiceClient.hasSubmitNumsLeft(accountId))
            return MapMessage.errorMessage("当天/本月发布次数已达到最大值，无法再新建发布");

        // 置上发布时间和操作员
        articles.forEach(a -> {
            a.setPublishDatetime(new Date());
            a.setPublishUser(getCurrentUser().getAccountName());
            a.setStatus(OfficialAccountsArticle.Status.Published);

            officialAccountsServiceClient.updateArticle(a.getId(), a);
        });

        if (articles.size() <= 0)
            return MapMessage.errorMessage("文章不存在！");

        // 增加发布次数
        accounts.setPublishNums(accounts.getPublishNums() + 1);
        officialAccountsServiceClient.updateAccounts(accounts.getId(), accounts);

        if (sendJpush) {
            /*// 有配置 按照tag发送
            List<String> pushSchoolOrRegionTags = new ArrayList<>();
            // 发送 目前公众号只当家长端发送
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("url", "");
            jpushExtInfo.put("tag", ParentMessageTag.公众号.name());
            jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW);
            jpushExtInfo.put("shareContent", "");
            jpushExtInfo.put("shareUrl", "");
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
            jpushExtInfo.put("ext_tab_message_type", accounts.getId());
            jpushExtInfo.put("officialAccountName", accounts.getName());
            jpushExtInfo.put("officialAccountID", accounts.getId());
            jpushContent = accounts.getName() + "：" + jpushContent;
            // 根据tag发送
            pushSchoolOrRegionTags.add(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accounts.getAccountsKey()));
            appMessageServiceClient.sendAppJpushMessageByTags(jpushContent, AppMessageSource.PARENT,
                    pushSchoolOrRegionTags, null, jpushExtInfo, 0);*/
        }

        return MapMessage.successMessage();
    }

    // 上传文章封面图片
    @RequestMapping(value = "uploadarticleimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadArticleImg(MultipartFile file) {
        Integer width = getRequestInt("width");
        Integer height = getRequestInt("height");
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的素材！");
        }
        try {
            MapMessage validMsg = validateImg(file, width, height);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            String fileName = MizarOssManageUtils.upload(file);
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("素材保存失败！");
            }
            if (MizarOssManageUtils.invalidFile.equals(fileName)) {
                return MapMessage.errorMessage("无效的文件类型！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
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
     * 上传素材的图片资源
     *
     * @return
     */
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
                    String url = $uploadFile("upfile");
                    return MapMessage.successMessage()
                            .add("url", url)
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

    private MapMessage validateImg(MultipartFile file, Integer width, Integer height) {
        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
        if (!IMG_SUFFIX.contains(ext)) {
            return MapMessage.errorMessage("图片格式只能是");
        } else {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                int h = image.getHeight();
                int w = image.getWidth();
                // 弹窗的类型仅仅校验图片的最大尺寸
                if (h != height || w != width) {
                    return MapMessage.errorMessage("图片大小不匹配，请重新选择尺寸为" + width + " x " + height + "大小的图片！");
                }
                return MapMessage.successMessage();
            } catch (Exception ex) {
                logger.error("Failed validate Img, ex={}", ex);
                return MapMessage.errorMessage("图片校验异常！");
            }
        }
    }

    private List<OfficialAccounts> getAccountList() {

        List<String> accountKeys = getCurrentUser().getOfficialAccountKeyList();
        if (CollectionUtils.isEmpty(accountKeys))
            return Collections.emptyList();

        return accountKeys.stream()
                .filter(key -> StringUtils.isNotEmpty(key))
                .map(key -> officialAccountsServiceClient.loadAccountByKey(key))
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

}
