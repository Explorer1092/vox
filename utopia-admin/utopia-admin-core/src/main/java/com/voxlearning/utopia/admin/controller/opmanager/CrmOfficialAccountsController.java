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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.athena.SearchEngineServiceClient;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.mizar.api.entity.oa.*;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.OfficialAccountsTargetType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

/**
 * Created by Summer Yang on 2016/7/4.
 * 公众号管理
 */
@Controller
@RequestMapping("/opmanager/officialaccounts")
public class CrmOfficialAccountsController extends OpManagerAbstractController {

    private static List<String> IMG_SUFFIX = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png"); // 此处维护上传图片的后缀
    private static final String DEFAULT_LINE_SEPARATOR = "\n";
    private static final String TOP_AUDITOR = "OFFICIAL_ACCOUNT_AUDITOR_TOP";

    @Inject private RaikouSystem raikouSystem;
    @Inject private SearchEngineServiceClient searchEngineServiceClient;

    @Inject
    private OfficialAccountsServiceClient officialAccountsServiceClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private UserBlacklistServiceClient userBlacklistServiceClient;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    // 列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 获取全部的公众号
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);

        final String adminUser = getCurrentAdminUser().getAdminUserName();
        boolean isTopAuditor = false;

        List<OfficialAccounts> accounts = officialAccountsServiceClient.loadAllOfficialAccounts();

        if (!(isTopAuditor = isTopAuditor())) {
            // 筛选管理员权限下面的记录
            accounts = accounts.parallelStream()
                    .filter(account -> {
                        String userStr = account.getGeneralAdminUsers() + "," + account.getSeniorAdminUsers();
                        return Arrays.asList(userStr.split(",")).contains(adminUser);
                    })
                    .collect(Collectors.toList());
        }

        // 按照创建时间倒序
        accounts.sort((a, b) -> a.getCreateDatetime().before(b.getCreateDatetime()) ? 1 : -1);

        // 权限参数，是否平台管理员
        model.addAttribute("isTopAuditor", isTopAuditor);
        model.addAttribute("currUser", adminUser);

        Page<OfficialAccounts> accountsPage = PageableUtils.listToPage(accounts, pageable);
        model.addAttribute("accountsPage", accountsPage);
        model.addAttribute("currentPage", accountsPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", accountsPage.getTotalPages());
        model.addAttribute("hasPrev", accountsPage.hasPrevious());
        model.addAttribute("hasNext", accountsPage.hasNext());

        return "opmanager/officialaccounts/index";
    }

    // 判断是不是平台管理员
    private boolean isTopAuditor() {
        String userName = getCurrentAdminUser().getAdminUserName();
        String[] topUserNames = crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> PRIMARY_PLATFORM_GENERAL.getType().equals(e.getCategoryName()))
                .filter(e -> e.getConfigKeyName().equals(TOP_AUDITOR))
                .map(e -> e.getConfigKeyValue())
                .findAny().orElse("")
                .split(",");

        return Arrays.asList(topUserNames).contains(userName);
    }

    // 获得某个公众号的详细信息
    @RequestMapping(value = "getaccountinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAccountInfo() {
        Long accountId = getRequestLong("accountId");
        if (accountId != 0L) {
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts != null) {
                MapMessage message = MapMessage.successMessage();
                message.add("account", accounts);

                return message;
            }
        }

        return MapMessage.errorMessage("公众号不存在！");
    }

    // 显示公众号详细页面
    @RequestMapping(value = "accountdetail.vpage", method = RequestMethod.GET)
    public String activityDetail(Model model) {
        Long accountId = getRequestLong("accountId");
        if (accountId != 0L) {
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts != null) {
                model.addAttribute("accounts", accounts);

                String currUser = getCurrentAdminUser().getAdminUserName();
                // 是否为高级管理员
                model.addAttribute("isSeniorAuditor",
                        Optional.ofNullable(accounts.getSeniorAdminUsers()).orElse("").contains(currUser));
            }
        }

        model.addAttribute("isTopAuditor", isTopAuditor());
        model.addAttribute("accountId", accountId);
        model.addAttribute("status", OfficialAccounts.Status.values());
        return "opmanager/officialaccounts/accountdetail";
    }

    @RequestMapping(value = "updateadminusers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateAdminUsers() {
        Long accountId = getRequestLong("accountId");
        return updateOfficialAccount(accountId,
                (account) -> {
                    String gAdminUserStr = getRequestString("gAdminUsers");
                    String sAdminUserStr = getRequestString("sAdminUsers");

                    List<String> gAdminUsers = Arrays.asList(gAdminUserStr.split(","));
                    List<String> sAdminUsers = Arrays.asList(sAdminUserStr.split(","));

                    // 高级管理员和普通管理员同时存在的话，只添加为高级管理员
                    gAdminUsers = gAdminUsers.stream().filter(user -> !sAdminUsers.contains(user))
                            .collect(Collectors.toList());

                    account.setGeneralAdminUsers(StringUtils.join(gAdminUsers, ","));
                    account.setSeniorAdminUsers(sAdminUserStr);

                    return account;
                });

    }

    @RequestMapping(value = "updateinstruction.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateInstruction() {
        Long accountId = getRequestLong("accountId");
        String instruction = getRequestString("instruction");

        MapMessage message = updateOfficialAccount(accountId,
                (account) -> {
                    account.setInstruction(instruction);
                    return account;
                });

        if (message.isSuccess()) {
            message.add("newInstruction", instruction);
        }
        return message;
    }

    private MapMessage updateOfficialAccount(long accountId, Function<OfficialAccounts, OfficialAccounts> applyFunc) {
        if (accountId != 0L) {
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts != null) {
                accounts = applyFunc.apply(accounts);
                return officialAccountsServiceClient.updateAccounts(accountId, accounts);
            }
        }

        return MapMessage.errorMessage();
    }
    // 添加编辑跳转
    /*@RequestMapping(value = "accountdetail.vpage", method = RequestMethod.GET)
    public String activityDetail(Model model) {
        Long accountId = getRequestLong("accountId");
        if (accountId != 0L) {
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts != null) {
                model.addAttribute("accounts", accounts);
            }
        }
        model.addAttribute("accountId", accountId);
        model.addAttribute("status", OfficialAccounts.Status.values());
        model.addAttribute("prePath", getPrePath());
        return "opmanager/officialaccounts/accountdetail";
    }*/

    // 添加编辑 post
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAccount() {
        // 获取参数
        Long accountId = getRequestLong("accountId");
        String accountsKey = getRequestString("accountsKey");
        String accountName = getRequestString("name");
        String instruction = getRequestString("instruction");
        boolean paymentBlackLimit = getRequestBool("paymentBlackLimit");
        boolean followLimit = getRequestBool("followLimit");
        int publishLimitDay = getRequestInt("maxPublishNumsD");
        int publishLimitMonth = getRequestInt("maxPublishNumsM");

        try {
            OfficialAccounts accounts;
            if (accountId == 0L) {
                accounts = new OfficialAccounts();
            } else {
                accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
                if (accounts == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
            }

            accounts.setAccountsKey(accountsKey);
            accounts.setName(accountName);
            // 编辑的话，保持原来的状态
            if (accounts.getStatus() == null)
                accounts.setStatus(OfficialAccounts.Status.Offline);
            accounts.setInstruction(instruction);
            accounts.setPaymentBlackLimit(paymentBlackLimit);
            accounts.setFollowLimit(followLimit);
            accounts.setMaxPublishNumsD(publishLimitDay);
            accounts.setMaxPublishNumsM(publishLimitMonth);

            // 保存实体
            MapMessage returnMsg;
            String op = "新建公众号";
            if (accountId == 0L) {
                returnMsg = officialAccountsServiceClient.createAccounts(accounts);
                accountId = SafeConverter.toLong(returnMsg.get("id"));
            } else {
                accounts.setUpdateDatetime(new Date());
                returnMsg = officialAccountsServiceClient.updateAccounts(accountId, accounts);
                op = "编辑公众号";
            }
            returnMsg.setInfo(returnMsg.isSuccess() ? "保存成功！" : "保存失败!");
            saveOperationLog("OFFICIAL_ACCOUNTS_TRACE", accountId, op, returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Save official account error! id={}, ex={}", accountId, ex.getMessage(), ex);
            if (ex.getCause() instanceof DuplicateKeyException) {
                return MapMessage.errorMessage("保存失败，accountsKey已经存在！", ex.getMessage(), ex);
            } else {
                return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
            }
        }
    }

    @RequestMapping(value = "uploadsrc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadSource(MultipartFile file) {
        Long accountId = getRequestLong("accountId");
        if (accountId == 0L) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的素材！");
        }
        try {
            OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (account == null || account.getDisabled()) {
                return MapMessage.errorMessage("无效的公众号!");
            }
            MapMessage validMsg = validateImg(file, 100, 100);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            String fileName = AdminOssManageUtils.upload(file, "oa");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("素材保存失败！");
            }
            return officialAccountsServiceClient.uploadAccountImg(accountId, fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "clearsrc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearSource() {
        Long accountId = getRequestLong("accountId");
        if (accountId == 0L) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (account == null || account.getDisabled()) {
                return MapMessage.errorMessage("无效的公众号!");
            }
            return officialAccountsServiceClient.uploadAccountImg(accountId, "");
        } catch (Exception ex) {
            logger.error("Failed to clear img, ex={}", ex);
            return MapMessage.errorMessage("清除素材图片失败：" + ex.getMessage());
        }
    }


    // 文章管理列表页
    @RequestMapping(value = "articlelist.vpage", method = RequestMethod.GET)
    public String articleList(Model model) {
        Long accountId = getRequestLong("accountId");

        // 获取公众号
        OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);

        // 组装数据
        List<Map<String, Object>> articlesList = officialAccountsServiceClient.queryArticle(
                accountId, null, null, null);

        model.addAttribute("account", account);
        model.addAttribute("articlesList", articlesList);
        model.addAttribute("accountId", accountId);

        // 计算发布次数时间限制
        MapMessage resultMsg = officialAccountsServiceClient.getPublishNumsLimit(accountId);
        if (resultMsg.isSuccess()) {
            model.addAllAttributes(resultMsg);
        }

        return "opmanager/officialaccounts/articlelist";
    }

    @RequestMapping(value = "articleindex.vpage", method = RequestMethod.GET)
    public String queryArticle(Model model) {
        Long accountId = getRequestLong("accountId");

        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts != null)
            model.addAttribute("account", accounts);

        String startDate = getRequestString("start");
        String endDate = getRequestString("end");
        String status = getRequestString("status");

        model.addAttribute("start", startDate);
        model.addAttribute("end", endDate);
        model.addAttribute("status", status);

        // 查询
        List<Map<String, Object>> articlesList = officialAccountsServiceClient.queryArticle(
                accountId, startDate, endDate, status);

        model.addAttribute("articlesList", articlesList);
        model.addAttribute("accountId", accountId);

//        model.addAttribute("leftPublishNumsD",
//                Math.max(accounts.getMaxPublishNumsD() - accounts.getPublishNums(),0));
//        model.addAttribute("leftPublishNumsM",
//                Math.max(accounts.getMaxPublishNumsM() - accounts.getPublishNums(),0));

        // 计算发布次数时间限制
        MapMessage resultMsg = officialAccountsServiceClient.getPublishNumsLimit(accountId);
        if (resultMsg.isSuccess()) {
            model.addAllAttributes(resultMsg);
        }

        return "opmanager/officialaccounts/articlelist";
    }

    // 工具栏管理列表页
    @RequestMapping(value = "toollist.vpage", method = RequestMethod.GET)
    public String toolList(Model model) {
        Long accountId = getRequestLong("accountId");
        // 获取工具栏列表
        List<OfficialAccountsTools> toolList = officialAccountsServiceClient.loadAccountToolsByAccountId(accountId);
        model.addAttribute("toolList", toolList);
        model.addAttribute("accountId", accountId);
        model.addAttribute("isTopAuditor", isTopAuditor());

        OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (account != null) {
            String currUser = getCurrentAdminUser().getAdminUserName();
            // 是否为高级管理员
            model.addAttribute("isSeniorAuditor",
                    Optional.ofNullable(account.getSeniorAdminUsers()).orElse("").contains(currUser));
        }

        return "opmanager/officialaccounts/toollist";
    }

    // 新建待发布的文章
    @RequestMapping(value = "articlesend.vpage", method = RequestMethod.GET)
    public String articleDetail(Model model) {
        Long accountId = getRequestLong("accountId");
        model.addAttribute("accountId", accountId);

        String bundleId = getRequestParameter("bundleId", null);
        List<OfficialAccountsArticle> articles = officialAccountsServiceClient.loadArticlesInTpl(bundleId);
        int orgSize = articles.size();

        if (orgSize > 0) {
            model.addAttribute("hasSend", articles.get(0).getHasSend());
            model.addAttribute("bindSid", articles.get(0).getBindSid());

            // 撤回状态的话，不能再保存了
            model.addAttribute("isSavable",
                    articles.get(0).getStatus() != OfficialAccountsArticle.Status.Offline);
        } else {
            model.addAttribute("isSavable", true);
        }

        model.addAttribute("articles", articles);
        return "opmanager/officialaccounts/articlesend";
    }

    // 添加工具栏
    @RequestMapping(value = "addtool.vpage", method = RequestMethod.GET)
    public String addTool(Model model) {
        Long accountId = getRequestLong("accountId");
        model.addAttribute("accountId", accountId);

        String bundleId = getRequestString("bundleId");
        List<OfficialAccountsArticle> articleList = officialAccountsServiceClient.loadArticlesByBundleId(bundleId);

        model.addAttribute("articles", articleList);
        return "opmanager/officialaccounts/addtool";
    }

    // 发布文章
    @RequestMapping(value = "publisharticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishArticle() {

        String bundleId = getRequestString("bundleId");
        List<OfficialAccountsArticle> articles = officialAccountsServiceClient.loadArticlesByBundleId(bundleId);
        MapMessage mapMessage = MapMessage.successMessage();

        if (articles.size() <= 0)
            return MapMessage.errorMessage("文章不存在！");

        boolean sendJpush = articles.get(0).getHasSend();
        String jpushContent = articles.get(0).getArticleTitle();
        long accountId = articles.get(0).getAccountId();
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在");

        // 如果发布次数用光，则报错返回!
        if (!officialAccountsServiceClient.hasSubmitNumsLeft(accountId))
            return MapMessage.errorMessage("当天/本月发布次数已达到最大值，无法再新建发布");

        Date publishTime = new Date();
        // 置上发布时间和操作员
        articles.forEach(a -> {
            a.setPublishDatetime(publishTime);
            a.setPublishUser(getCurrentAdminUser().getAdminUserName());
            a.setStatus(OfficialAccountsArticle.Status.Published);

            mapMessage.of(officialAccountsServiceClient.updateArticle(a.getId(), a));
        });

        if (sendJpush) {
            // 有配置 按照tag发送
            List<String> pushSchoolOrRegionTags = new ArrayList<>();
            // 发送 目前公众号只当家长端发送
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("url", "");
            jpushExtInfo.put("tag", ParentMessageTag.公众号.name());
            jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
            jpushExtInfo.put("shareContent", "");
            jpushExtInfo.put("shareUrl", "");
            jpushExtInfo.put("ext_tab_message_type", accounts.getId());
            jpushExtInfo.put("officialAccountName", accounts.getName());
            jpushExtInfo.put("officialAccountID", accounts.getId());
            // 为了解决文章推送时间和发布时间混乱导致的小红点问题
            Long pushTime = publishTime.getTime() + TimeUnit.SECONDS.toMillis(1L);
            jpushExtInfo.put("timestamp", pushTime);

            jpushContent = accounts.getName() + "：" + jpushContent;
            // 根据tag发送
            pushSchoolOrRegionTags.add(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accounts.getAccountsKey()));
            appMessageServiceClient.sendAppJpushMessageByTags(jpushContent, AppMessageSource.PARENT,
                    pushSchoolOrRegionTags, null, jpushExtInfo, 0);
        }

        // 更新发布次数
        accounts.setPublishNums(accounts.getPublishNums() + 1);
        officialAccountsServiceClient.updateAccounts(accounts.getId(), accounts);

        return mapMessage;
    }

    // 保存文章 POST
    @RequestMapping(value = "savearticle.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveArticle(@RequestBody Map<String, Object> jsonObject) {
        // 获取参数
        if (MapUtils.isEmpty(jsonObject)) {
            return MapMessage.errorMessage("参数错误");
        }

        // 获取文章列表
        List<Map<String, Object>> articleList = (List<Map<String, Object>>) jsonObject.get("articleList");
        if (CollectionUtils.isEmpty(articleList)) {
            return MapMessage.errorMessage("文章内容不能为空");
        }

        // 是否需要发送JPUSH
        boolean sendJpush = SafeConverter.toBoolean(jsonObject.get("sendJpush"));
        // 是否拼接SID
        boolean bindSid = SafeConverter.toBoolean(jsonObject.get("bindSid"));
        // 公众号ID
        Long accountId = SafeConverter.toLong(jsonObject.get("accountId"));
        if (accountId == 0L) {
            return MapMessage.errorMessage("公众号ID不存在");
        }

        return officialAccountsServiceClient.saveArticle(articleList, sendJpush, bindSid, accountId);
    }

    // 删除工具栏
    @RequestMapping(value = "deletetool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteTool() {
        Long toolId = getRequestLong("toolId");
        try {
            MapMessage returnMsg = officialAccountsServiceClient.deleteAccountTool(toolId);
            saveOperationLog("OFFICIAL_ACCOUNTS_TOOL_TRACE", toolId, "删除工具栏", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to delete official account tool:id={},ex={}", toolId, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除工具栏失败:{}", ex.getMessage(), ex);
        }
    }

    // 保存工具栏
    @RequestMapping(value = "addtool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTool() {
        // 获取参数
        Long accountId = getRequestLong("accountId");
        String toolName = getRequestString("toolName");
        String toolUrl = getRequestString("toolUrl");
        Boolean bindSid = getRequestBool("bindSid");
        try {
            // 校验文字长度
            if (StringUtils.isNotBlank(toolName) && toolName.trim().length() > 6) {
                return MapMessage.errorMessage("工具栏名称最多支持6个文字");
            }
            // 校验工具栏个数
            List<OfficialAccountsTools> toolsList = officialAccountsServiceClient.loadAccountToolsByAccountId(accountId);
            if (CollectionUtils.isNotEmpty(toolsList) && toolsList.size() >= 3) {
                return MapMessage.errorMessage("工具栏最多支持配置3个");
            }
            OfficialAccountsTools tools = new OfficialAccountsTools();
            tools.setAccountId(accountId);
            tools.setBindSid(bindSid);
            tools.setToolName(toolName);

            // 加入accountsKey参数
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts != null) {
                String accountsKeyParam = "accountsKey=" + accounts.getAccountsKey();
                // 如果已经存在则替换
                if (toolUrl.contains("accountsKey"))
                    toolUrl = toolUrl.replaceAll("accountsKey=\\w+", accountsKeyParam);
                else {
                    String paramSuffix = toolUrl.contains("?") ? "&" : "?";
                    toolUrl += paramSuffix + accountsKeyParam;
                }
            }

            tools.setToolUrl(toolUrl);
            tools.setDisabled(false);
            officialAccountsServiceClient.saveAccountTool(tools);
            saveOperationLog("OFFICIAL_ACCOUNTS_TOOL_TRACE", accountId, "添加工具栏", "成功");
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Save official account tool error! accountId={}, ex={}", accountId, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
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
            String fileName = AdminOssManageUtils.upload(file, "oa");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("素材保存失败！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }

    // 上线公众号
    @RequestMapping(value = "accountonline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage accountOnline(@RequestParam Long accountId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts == null || accounts.getDisabled()) {
                return MapMessage.errorMessage("该公众号已经被删除，请刷新重新加载页面！");
            }
            if (OfficialAccounts.Status.Online.equals(accounts.getStatus())) {
                return MapMessage.successMessage();
            }
            MapMessage returnMsg = officialAccountsServiceClient.updateAccountStatus(accountId, OfficialAccounts.Status.Online);
            saveOperationLog("OFFICIAL_ACCOUNTS_TRACE", accountId, "公众号上线", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to online official account:id={},ex={}", accountId, ex.getMessage(), ex);
            return MapMessage.errorMessage("上线公众号失败:{}", ex.getMessage(), ex);
        }
    }

    // 下线公众号
    @RequestMapping(value = "accountoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage accountOffline(@RequestParam Long accountId) {
        try {
            // 操作之前做状态检查, 防止重复操作
            OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
            if (accounts == null || accounts.getDisabled()) {
                return MapMessage.errorMessage("该公众号已经被删除，请刷新重新加载页面！");
            }
            if (OfficialAccounts.Status.Offline.equals(accounts.getStatus())) {
                return MapMessage.successMessage();
            }
            MapMessage returnMsg = officialAccountsServiceClient.updateAccountStatus(accountId, OfficialAccounts.Status.Offline);
            saveOperationLog("OFFICIAL_ACCOUNTS_TRACE", accountId, "公众号下线", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to offline official account:id={},ex={}", accountId, ex.getMessage(), ex);
            return MapMessage.errorMessage("下线公众号失败:{}", ex.getMessage(), ex);
        }
    }

    // 撤回文章
    @RequestMapping(value = "articleoffline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage articleOffline() {
        String bundleId = getRequestString("bundleId");
        Long accountId = getRequestLong("accountId");
        if (StringUtils.isBlank(bundleId) || accountId == 0L) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            MapMessage returnMsg = officialAccountsServiceClient.updateArticleStatus(accountId, bundleId, OfficialAccountsArticle.Status.Offline);
            saveOperationLog("OFFICIAL_ACCOUNTS_TRACE", accountId, "公众号文章撤回", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("Failed to offline official account article:id={},bundleId={},ex={}", accountId, bundleId, ex.getMessage(), ex);
            return MapMessage.errorMessage("公众号文章撤回失败:{}", ex.getMessage(), ex);
        }
    }

    // 配置广告标签
    @RequestMapping(value = "accountconfig.vpage", method = RequestMethod.GET)
    public String adTarget(Model model) {
        Long accountId = getRequestLong("accountId");
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (accounts == null || accounts.getDisabled()) {
            model.addAttribute("error", "无效的公众号信息");
            return "opmanager/officialaccounts/accountconfig";
        }

        model.addAttribute("accounts", accounts);
        generateDetailTargets(accountId, model);

        model.addAttribute("isTopAuditor", isTopAuditor());
        return "opmanager/officialaccounts/accountconfig";
    }

    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {
        Long accountId = getRequestLong("accountId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");
        if (OfficialAccountsTargetType.of(type) != OfficialAccountsTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }
        try {

            List<String> regionList = Arrays.asList(regions.split(","));
            MapMessage returnMsg = officialAccountsServiceClient.saveAccountTargets(accountId, type, regionList, false);
            saveOperationLog("OFFICIAL_ACCOUNT_TARGET_TRACE", accountId, "修改公众号投放区域", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放地区失败! id={},type={}, ex={}", accountId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        Long accountId = getRequestLong("accountId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        Boolean append = getRequestBool("append");
        OfficialAccountsTargetType targetType = OfficialAccountsTargetType.of(type);
        if (targetType != OfficialAccountsTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }
        try {
            // 没有校验用户输入是否符合规范
            List<String> targetList = Arrays.asList(targetIds.split(DEFAULT_LINE_SEPARATOR))
                    .stream().map(t -> t.replaceAll("\\s", "")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            MapMessage returnMsg = officialAccountsServiceClient.saveAccountTargets(accountId, type, targetList, append);
            saveOperationLog("OFFICIAL_ACCOUNT_TARGET_TRACE", accountId, "修改公众号投放对象(" + targetType.getDesc() + ")", returnMsg.isSuccess() ? "操作成功" : returnMsg.getInfo());
            return returnMsg;
        } catch (Exception ex) {
            logger.error("保存投放用户失败:id={},type={},ex={}", accountId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        Long accountId = getRequestLong("accountId");
        Integer type = getRequestInt("type");
        OfficialAccountsTargetType targetType = OfficialAccountsTargetType.of(type);
        if (targetType != OfficialAccountsTargetType.TARGET_TYPE_REGION
                && targetType != OfficialAccountsTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return officialAccountsServiceClient.clearAccountTargets(accountId, type);
        } catch (Exception ex) {
            logger.error("清空投放对象失败:id={},type={},ex={}", accountId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "usermanagement.vpage", method = RequestMethod.GET)
    public String queryOfficialAccountUsers(Model model) {
        Long accountId = getRequestLong("accountId");
        // 获取公众号
        OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        model.addAttribute("account", account);
        model.addAttribute("accountId", accountId);
        model.addAttribute("isTopAuditor", isTopAuditor());

        Long userId = getRequestLong("userId");
        if (userId == 0L) {
            return "opmanager/officialaccounts/usermanagment";
        }

        model.addAttribute("userId", userId);
        List<UserOfficialAccountsRef> userRefs = officialAccountsServiceClient.loadUserOfficialAccoutnsRef(userId);
        // 获取该家长下面的公众号
        UserOfficialAccountsRef userRef =
                userRefs.stream()
                        .filter(
                                user -> user.getOfficialAccountsId() == accountId)
                        .findFirst()
                        .orElse(null);

        if (userRef != null) {
            model.addAttribute("userRef", userRef);

            // 取家长随便第一个孩子的地区数据
            List<User> children = studentLoaderClient.loadParentStudents(userRef.getUserId());
            if (!CollectionUtils.sizeIsEmpty(children)) {

                StudentDetail detail = studentLoaderClient.loadStudentDetail(children.get(0).getId());
                if (detail != null && detail.getCityCode() != null) {
                    model.addAttribute("region", raikouSystem.loadRegion(detail.getCityCode()).getProvinceName());

                }
            }
        }

        return "opmanager/officialaccounts/usermanagment";
    }

    // 导入新关注的用户到公众号下面
    @RequestMapping(value = "importfollowaccountusers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importFollowAccountUsers() {
        Long accountId = getRequestLong("accountId");
        String userIds = getRequestString("userIds");

        // 获取公众号
        OfficialAccounts account = officialAccountsServiceClient.loadAccountByIdIncludeDisabled(accountId);
        if (account != null && StringUtils.isNotEmpty(userIds)) {
            List<Long> notExistIdList = new ArrayList<>();// 不存在的用户列表
            List<Long> existUserList = new ArrayList<>();// 重复导入的列表

            Arrays.stream(userIds.split(DEFAULT_LINE_SEPARATOR))
                    .map(t -> t.replaceAll("\\s", ""))
                    .filter(StringUtils::isNotBlank)
                    .filter(StringUtils::isNumeric)
                    .map(Long::parseLong)
                    .forEach(userId -> {

                        User user = raikouSystem.loadUser(userId);
                        if (user == null || !user.isParent()) {
                            notExistIdList.add(userId);
                            return;
                        }

                        // 不能重复关注
                        // 现在修改为允许重复关注
                        // 如果需要黑名单检验
                        if (account.getPaymentBlackLimit()) {
                            if (studentLoaderClient
                                    .loadParentStudents(userId)
                                    .stream()
                                    .anyMatch(s -> userBlacklistServiceClient.isInBlackListByParent(user, s))) {
                                return;
                            }
                        }

                        officialAccountsServiceClient.updateFollowStatus(userId, accountId, UserOfficialAccountsRef.Status.AutoFollow);
                    });

            saveOperationLog("OFFICIAL_ACCOUNTS_IMPORT_FOLLOW", accountId, "用户管理导入关注", "操作成功");

            MapMessage message = MapMessage.successMessage();
            message.add("notExistIdList", StringUtils.join(notExistIdList, ";"));
            message.add("existUserList", StringUtils.join(existUserList, ";"));
            return message;
        }

        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "savedatemessageforjob.vpage", method = RequestMethod.GET)
    public String saveDateMessageForJob(Model model) {
        Long accountId = getRequestLong("accountId");
        OfficialAccounts accounts = officialAccountsServiceClient.loadAccountById(accountId);
        if (accounts == null) {
            getAlertMessageManager().addMessageError("公众号不存在");
            return redirect("/opmanager/officialaccounts/accountdetail.vpage?accountId=" + accountId);
        }
        if (!StringUtils.equalsIgnoreCase(accounts.getAccountsKey(), "dianduji")) {
            getAlertMessageManager().addMessageError("该功能目前仅支持点读机");
            return redirect("/opmanager/officialaccounts/accountdetail.vpage?accountId=" + accountId);
        }
        String key = "date_message_official_account_dianduji";
        CacheObject<List<Map<String, String>>> cacheObject = CacheSystem.CBS.getCache("persistence").get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            List<Map<String, String>> messageList = cacheObject.getValue().stream().sorted((o1, o2) -> o1.get("date").compareTo(o2.get("date"))).collect(Collectors.toList());
            model.addAttribute("messageList", messageList);
        }
        model.addAttribute("accountId", accountId);
        model.addAttribute("accounts", accounts);
        return "opmanager/officialaccounts/datemessage";
    }

    @RequestMapping(value = "savedatemessageforjob.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveMessage() {
        String date = getRequestString("date");
        String content = getRequestString("content");
        String creator = getCurrentAdminUser().getAdminUserName();
        if (StringUtils.isBlank(date)) {
            return MapMessage.errorMessage("日期不能为空");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }
        if (content.length() > 30) {
            return MapMessage.errorMessage("内容长度不能超过30个字");
        }
        Map<String, String> map = new HashMap<>();
        map.put("date", date);
        map.put("content", content);
        map.put("creator", creator);
        ChangeCacheObject<List<Map<String, String>>> modifier = currentValue -> {
            currentValue = new ArrayList<>(currentValue);
            currentValue.add(map);
            return currentValue;
        };
        String key = "date_message_official_account_dianduji";
        CacheObject<List<Map<String, String>>> cacheObject = CacheSystem.CBS.getCache("persistence").get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            if (cacheObject.getValue().stream().anyMatch(p -> StringUtils.equalsIgnoreCase(p.get("date"), date))) {
                return MapMessage.errorMessage("填写的发送日期" + date + "已经配置过运营内容");
            }
            CacheValueModifierExecutor<List<Map<String, String>>> executor = CacheSystem.CBS.getCache("persistence").createCacheValueModifier();
            executor.key(key)
                    .expiration(0)
                    .modifier(modifier)
                    .execute();
        } else {
            CacheSystem.CBS.getCache("persistence").add(key, 0, Collections.singletonList(map));
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "deletemessageforjob.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMessage() {
        String date = getRequestString("date");
        if (StringUtils.isBlank(date)) {
            return MapMessage.errorMessage("日期不能为空");
        }
        String key = "date_message_official_account_dianduji";
        CacheObject<List<Map<String, String>>> cacheObject = CacheSystem.CBS.getCache("persistence").get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            ChangeCacheObject<List<Map<String, String>>> modifier = currentValue -> {
                currentValue = new ArrayList<>(currentValue);
                currentValue = currentValue.stream().filter(p -> !StringUtils.equalsIgnoreCase(p.get("date"), date)).collect(Collectors.toList());
                return currentValue;
            };
            CacheValueModifierExecutor<List<Map<String, String>>> executor = CacheSystem.CBS.getCache("persistence").createCacheValueModifier();
            executor.key(key)
                    .modifier(modifier)
                    .expiration(0)
                    .execute();
        }
        return MapMessage.successMessage();
    }

    private void generateDetailTargets(Long accountId, Model model) {
        Map<Integer, List<OfficialAccountsTarget>> targetMap = officialAccountsServiceClient.loadAccountTargetsGroupByType(accountId);
        int type = 5;
        List<Integer> regions = new ArrayList<>();
        List<Set<String>> labels = new ArrayList<>();
        if (targetMap.get(OfficialAccountsTargetType.TARGET_TYPE_REGION.getType()) != null) {
            type = OfficialAccountsTargetType.TARGET_TYPE_REGION.getType();
            regions = targetMap.get(type).stream().map(ad -> SafeConverter.toInt(ad.getTargetStr())).collect(Collectors.toList());
        }
        List<KeyValuePair<Integer, String>> targetTypes = OfficialAccountsTargetType.toKeyValuePairs();
        for (KeyValuePair<Integer, String> target : targetTypes) {
            model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
        }
        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(crmRegionService.buildRegionTree(regions)));
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
        model.addAttribute("targetLabel", labels);
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

}
