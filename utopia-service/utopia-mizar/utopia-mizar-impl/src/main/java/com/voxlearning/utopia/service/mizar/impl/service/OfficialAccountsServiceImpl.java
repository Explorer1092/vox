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

package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.mizar.api.entity.oa.*;
import com.voxlearning.utopia.service.mizar.api.service.OfficialAccountsService;
import com.voxlearning.utopia.service.mizar.impl.dao.oa.*;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.OfficialAccountsTargetType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import lombok.Getter;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/7/4.
 */
@Named
@Service(interfaceClass = OfficialAccountsService.class)
@ExposeServices({
        @ExposeService(interfaceClass = OfficialAccountsService.class,version = @ServiceVersion(version = "1.3.STABLE")),
        @ExposeService(interfaceClass = OfficialAccountsService.class,version = @ServiceVersion(version = "1.2.STABLE"))
})
public class OfficialAccountsServiceImpl extends SpringContainerSupport implements OfficialAccountsService {

    @Getter
    @AlpsQueueProducer(queue = "utopia.officialaccount.message.queue")
    private MessageProducer producer;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private OfficialAccountsArticlePersistence officialAccountsArticlePersistence;
    @Inject private OfficialAccountsPersistence officialAccountsPersistence;
    @Inject private OfficialAccountsTargetPersistence officialAccountsTargetPersistence;
    @Inject private OfficialAccountsToolsPersistence officialAccountsToolsPersistence;
    @Inject private StudentLoaderClient studentLoader;
    @Inject private UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject private UserLoaderClient userLoader;
    @Inject private UserOfficialAccountsRefPersistence userOfficialAccountsRefPersistence;

    @Inject private AppMessageLoaderClient appMessageLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private MessageLoaderClient messageLoaderClient;

    private static String userOfficialListCacheKeyFix = "com.voxlearning.utopia.service.mizar.api.service.OfficialAccountsService:USER_ACCOUNTS=";
    // 公众号资料详情页的url
    private static String ACCOUNTS_DETAIL_URL = "/view/mobile/parent/information/public_index?accountId={0}";
    private static final Long SEND_TIME_DISCRETE_BASED_SECONDS = 5 * 60L;

    @Override
    public List<OfficialAccounts> loadAllOfficialAccounts() {
        return officialAccountsPersistence.findAll();
    }

    @Override
    public OfficialAccounts loadAccountById(Long accountId) {
        return officialAccountsPersistence.load(accountId);
    }

    @Override
    public OfficialAccounts loadAccountByKey(String accountKey) {
        return officialAccountsPersistence.loadByAccountsKey(accountKey);
    }

    @Override
    public MapMessage createAccounts(OfficialAccounts accounts) {
        try {
            officialAccountsPersistence.insert(accounts);
            return MapMessage.successMessage().add("id", accounts.getId());
        } catch (DuplicateKeyException ex) {
            return MapMessage.errorMessage("公众号账户名已经存在，不能重复插入数据！");
        }
    }

    @Override
    public MapMessage updateAccounts(Long accountId, OfficialAccounts accounts) {
        accounts.setId(accountId);
        try {
            OfficialAccounts modified = officialAccountsPersistence.replace(accounts);
            return modified != null ? MapMessage.successMessage() : MapMessage.errorMessage();
        } catch (DuplicateKeyException ex) {
            return MapMessage.errorMessage("公众号账户名已经存在，不能重复插入数据！");
        }
    }

    @Override
    public MapMessage uploadAccountImg(Long accountId, String fileName) {
        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null) {
            return MapMessage.errorMessage("无效的公众号ID");
        }
        accounts.setImgUrl(fileName);
        accounts.setUpdateDatetime(new Date());
        officialAccountsPersistence.replace(accounts);
        return MapMessage.successMessage();
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByAccountsId(Long accountId) {
        return officialAccountsArticlePersistence.loadByAccountsId(accountId);
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByAccountsId(
            Long accountId,
            Date startDate,
            Date endDate,
            Collection<String> statusList) {
        return officialAccountsArticlePersistence.loadByAccountsId(accountId, startDate, endDate, statusList);
    }

    @Override
    public OfficialAccountsArticle loadArticleById(Long articleId) {
        return officialAccountsArticlePersistence.load(articleId);
    }

    @Override
    public List<OfficialAccountsArticle> loadArticlesByBundleId(String bundleId) {
        return officialAccountsArticlePersistence.loadByBundleId(bundleId);
    }

    @Override
    public MapMessage createAccountArticle(OfficialAccountsArticle accountsArticle) {
        officialAccountsArticlePersistence.insert(accountsArticle);
        return MapMessage.successMessage().add("id", accountsArticle.getId());
    }

    @Override
    public MapMessage updateAccountArticle(Long articleId, OfficialAccountsArticle article) {
        article.setId(articleId);
        OfficialAccountsArticle modified = officialAccountsArticlePersistence.replace(article);
        return modified != null ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @Override
    public MapMessage updateAccountStatus(Long accountId, OfficialAccounts.Status status) {
        int rows = officialAccountsPersistence.updateStatus(accountId, status);
        if (rows > 0) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("没有数据被更新");
        }
    }

    @Override
    public MapMessage updateArticle(long articleId, OfficialAccountsArticle article) {
        article.setId(articleId);
        OfficialAccountsArticle modifier = officialAccountsArticlePersistence.replace(article);
        return modifier != null ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @Override
    public MapMessage updateArticleStatus(Long accountId, String bundleId, OfficialAccountsArticle.Status status) {
        int rows = officialAccountsArticlePersistence.updateStatus(accountId, bundleId, status);
        if (rows > 0) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("没有数据被更新");
        }
    }

    @Override
    public Map<Integer, List<OfficialAccountsTarget>> loadAccountTargetsGroupByType(Long accountId) {
        if (accountId == null || accountId == 0L) {
            return Collections.emptyMap();
        }
        return officialAccountsTargetPersistence.findByAccountId(accountId)
                .stream().collect(Collectors.groupingBy(OfficialAccountsTarget::getTargetType, Collectors.toList()));
    }

    @Override
    public MapMessage saveAccountTargets(Long accountId, Integer type, Collection<String> targetList, Boolean isAppend) {
        if (accountId == null || accountId == 0L || type == 0 || CollectionUtils.isEmpty(targetList)) {
            return MapMessage.errorMessage("参数异常！");
        }
        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null) {
            return MapMessage.errorMessage("公众号信息异常！");
        }
        // 追加模式不清除之前的数据
        if (!isAppend) {
            officialAccountsTargetPersistence.clearAccountTarget(accountId, type);
            // 更新时间
            accounts.setUpdateDatetime(new Date());
            officialAccountsPersistence.replace(accounts);
        }
        targetList = CollectionUtils.toLinkedHashSet(targetList);
        List<OfficialAccountsTarget> list = targetList.stream()
                .filter(StringUtils::isNotBlank)
                .map(target -> {
                    OfficialAccountsTarget at = new OfficialAccountsTarget();
                    at.setAccountId(accountId);
                    at.setTargetType(type);
                    at.setTargetStr(target);
                    return at;
                })
                .collect(Collectors.toList());
        officialAccountsTargetPersistence.inserts(list);
        // 更新时间
        accounts.setUpdateDatetime(new Date());
        officialAccountsPersistence.replace(accounts);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage clearAccountTargets(Long accountId, Integer type) {
        if (accountId == null || accountId == 0L || type == 0) {
            return MapMessage.errorMessage("参数异常！");
        }
        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null) {
            return MapMessage.errorMessage("公众号信息异常！");
        }
        officialAccountsTargetPersistence.clearAccountTarget(accountId, type);
        accounts.setUpdateDatetime(new Date());
        officialAccountsPersistence.replace(accounts);
        return MapMessage.successMessage();
    }

    @Override
    public List<OfficialAccounts> loadUserOfficialAccounts(Long parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        String cacheKey = userOfficialListCacheKeyFix + parentId;
        List<OfficialAccounts> dataList = CacheSystem.CBS.getCache("flushable").load(cacheKey);
        if (dataList != null) {
            return dataList;
        }

        User user = userLoader.loadUser(parentId);
        if (user == null || user.fetchUserType() != UserType.PARENT) {
            return Collections.emptyList();
        }

        // 获取所有公众号
        List<OfficialAccounts> accountsList = loadAllOfficialAccounts();
        if (CollectionUtils.isEmpty(accountsList)) {
            return Collections.emptyList();
        }
        accountsList = accountsList.stream().filter(a -> !a.getDisabled() && a.getStatus() == OfficialAccounts.Status.Online).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(accountsList)) {
            return Collections.emptyList();
        }

        // 根据家长孩子的信息过滤公众号配置
        // 获取家长孩子
        List<User> userList = studentLoader.loadParentStudents(parentId);
        List<Integer> regionCodeList = new ArrayList<>();
        boolean isInPaymentBlackList = false;
        for (User child : userList) {
            StudentDetail studentDetail = studentLoader.loadStudentDetail(child.getId());
            if (!isInPaymentBlackList) {
                isInPaymentBlackList = userBlacklistServiceClient.isInBlackListByParent(user, child);
            }
            if (studentDetail != null) {
                regionCodeList.add(studentDetail.getCityCode());
                regionCodeList.add(studentDetail.getRootRegionCode());
                regionCodeList.add(studentDetail.getStudentSchoolRegionCode());
            }
        }

        // 获取用户关注公众号所有的关系
        List<UserOfficialAccountsRef> refs = userOfficialAccountsRefPersistence.loadByUserId(parentId);
        List<Long> followIds = new ArrayList<>();
        List<Long> unFollowIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(refs)) {
            for (UserOfficialAccountsRef ref : refs) {
                if (ref.getStatus() != null && ref.getStatus() == UserOfficialAccountsRef.Status.UnFollow) {
                    unFollowIds.add(ref.getOfficialAccountsId());
                } else {
                    followIds.add(ref.getOfficialAccountsId());
                }
            }
        }

        List<OfficialAccounts> filterList = new ArrayList<>();
        for (OfficialAccounts account : accountsList) {
            // 用户主动取消关注的 直接过滤
            if (unFollowIds.contains(account.getId())) {
                continue;
            }
            // 用户黑名单过滤
            if (account.getPaymentBlackLimit() && isInPaymentBlackList) {
                // 黑名单用户 主动关注的也不让看
                if (followIds.contains(account.getId())) {
                    followIds.remove(account.getId());
                }
                continue;
            }

            Map<Integer, List<OfficialAccountsTarget>> targetMap = loadAccountTargetsGroupByType(account.getId());
            // 校验是否投放所有用户
            List<OfficialAccountsTarget> targetList = targetMap.get(OfficialAccountsTargetType.TARGET_TYPE_ALL.getType());
            if (CollectionUtils.isNotEmpty(targetList)) {
                OfficialAccountsTarget target = targetList.stream().filter(t -> SafeConverter.toBoolean(t.getTargetStr()))
                        .findAny().orElse(null);
                if (target != null) {
                    filterList.add(account);
                    continue;
                }
            }
            // 校验 TARGET_TYPE_REGION 投放区域过滤
            // 这里是 || 的关系
            targetList = targetMap.get(OfficialAccountsTargetType.TARGET_TYPE_REGION.getType());
            if (checkRegionNew(regionCodeList, targetList)) {
                // 命中条件 直接返回
                filterList.add(account);
            }
        }
        // 去除已经关注的
        for (OfficialAccounts account : filterList) {
            if (followIds.contains(account.getId())) {
                followIds.remove(account.getId());
            }
        }
        if (CollectionUtils.isNotEmpty(followIds)) {
            // 校验是否已经下线
            Collection<OfficialAccounts> accountsFollows = officialAccountsPersistence.loads(followIds).values();
            if (CollectionUtils.isNotEmpty(accountsFollows)) {
                accountsFollows = accountsFollows.stream().filter(a -> !a.getDisabled() && a.getStatus() == OfficialAccounts.Status.Online).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(accountsFollows)) {
                    filterList.addAll(accountsFollows);
                }
            }
        }
        return filterList;
    }

    // 获取文章 以及用户公众号发送的消息
    @Override
    public List<Map<String, Object>> loadArticlesByAccountsIdAndCreateDate(Long accountId, Date startDate, Long parentId) {
        if (accountId == null || startDate == null) {
            return Collections.emptyList();
        }
        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<OfficialAccountsArticle> articles = officialAccountsArticlePersistence.loadByAccountsId(accountId);
        // 过滤最近一个月的数据
        if (CollectionUtils.isNotEmpty(articles)) {
            articles = articles.stream()
                    // 这里修改成按发布时间查询
                    // 这里再改回最近一次修改时间，因为解决不了修改旧文章以及撤回文章的问题
                    .filter(a -> a.getUpdateDatetime().after(startDate))
                    .filter(a -> a.getStatus() != null
                            && a.getStatus() != OfficialAccountsArticle.Status.Online) // 过滤未发布的数据
                    .collect(Collectors.toList());

            Map<String, List<OfficialAccountsArticle>> articleMap =
                    articles.stream().collect(Collectors.groupingBy(OfficialAccountsArticle::getBundleId, Collectors.toList()));
            for (Map.Entry<String, List<OfficialAccountsArticle>> entry : articleMap.entrySet()) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("updateTime", DateUtils.dateToString(entry.getValue().get(0).getUpdateDatetime(), "yyyy-MM-dd HH:mm"));
                objectMap.put("articles", getArticleList(entry.getValue()));
                objectMap.put("status", entry.getValue().get(0).getStatus());
                objectMap.put("bundleId", entry.getValue().get(0).getBundleId());
                objectMap.put("type", "article");
                dataList.add(objectMap);
            }
        }
        // 获取家长消息列表
        if (parentId != null && parentId != 0) {
            List<AppMessage.Location> locations = messageLoaderClient.getMessageLoader().loadAppMessageLocations(parentId);
            Set<String> userMessageIds = locations.stream().filter(m -> ParentMessageType.OFFICIAL_MSG.getType().equals(m.getMessageType()))
                    .filter(m -> m.getCreateTime() != null && new Date(m.getCreateTime()).after(startDate))
                    .map(AppMessage.Location::getId)
                    .collect(Collectors.toSet());
            Map<String, AppMessage> userMessages = messageLoaderClient.getMessageLoader().loadAppMessageByIds(userMessageIds);
            if (MapUtils.isNotEmpty(userMessages)) {
                List<AppMessage> userMessageList = userMessages.values().stream()
                        .filter(m -> MapUtils.isNotEmpty(m.getExtInfo()) &&
                                Objects.equals(SafeConverter.toString(m.getExtInfo().get("accountsKey")), SafeConverter.toString(accounts.getAccountsKey())))
                        .collect(Collectors.toList());
                for (AppMessage message : userMessageList) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("updateTime", DateUtils.dateToString(new Date(message.getCreateTime()), "yyyy-MM-dd HH:mm"));
                    objectMap.put("title", message.getTitle());
                    objectMap.put("content", message.getContent());

                    // 如果为空的话，设置成空白页
                    // 改为公众号资料详情页...
                    if (StringUtils.isEmpty(message.getLinkUrl()))
                        objectMap.put("url", generateAccountDetailUrl(accountId));
                    else
                        objectMap.put("url", message.getLinkUrl());

                    objectMap.put("type", "message");
                    objectMap.put("messageId", message.getId());
                    dataList.add(objectMap);
                }
            }

            // load全局的消息
            appMessageLoaderClient.findByMessageSource(AppMessageSource.PARENT.name())
                    .stream()
                    .filter(m -> Objects.equals(m.getMessageType(), ParentMessageType.OFFICIAL_MSG.type))
                    .filter(m -> MapUtils.isNotEmpty(m.getExtInfo()) &&
                            Objects.equals(SafeConverter.toString(m.getExtInfo().get("accountsKey")), SafeConverter.toString(accounts.getAccountsKey())))
                    .filter(m -> m.getCreateTime() != null && new Date(m.getCreateTime()).after(startDate))
                    // 限制截止时间，避免看到延迟发放的未来数据
                    .filter(m -> m.getCreateTime() != null && new Date(m.getCreateTime()).before(new Date()))
                    .forEach(message -> {
                        Map<String, Object> msgMap = new HashMap<>();

                        msgMap.put("updateTime", DateUtils.dateToString(new Date(message.getCreateTime()), "yyyy-MM-dd HH:mm"));
                        msgMap.put("url", message.getLinkUrl());
                        msgMap.put("type", "message");
                        msgMap.put("messageId", message.getId());
                        msgMap.put("title", message.getTitle());
                        msgMap.put("content", message.getContent());
                        dataList.add(msgMap);
                    });
        }
        dataList.sort((o1, o2) -> {
            Date time1 = DateUtils.stringToDate(SafeConverter.toString(o1.get("updateTime")), "yyyy-MM-dd HH:mm");
            Date time2 = DateUtils.stringToDate(SafeConverter.toString(o2.get("updateTime")), "yyyy-MM-dd HH:mm");
            return time2.compareTo(time1);
        });
        return dataList;
    }

    @Override
    public List<OfficialAccountsTools> loadAccountToolsByAccountId(Long accountId) {
        return officialAccountsToolsPersistence.loadByAccountsId(accountId);
    }

    @Override
    public MapMessage deleteAccountTool(Long toolId) {
        officialAccountsToolsPersistence.deleteTool(toolId);
        return MapMessage.successMessage();
    }

    // 用户是否对于公众号有关注关系
    @Override
    public boolean isFollow(Long accountId, Long parentId) {
        if (accountId == null || parentId == null) {
            return false;
        }
        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null) {
            return false;
        }
        List<OfficialAccounts> accountsList = loadUserOfficialAccounts(parentId);
        if (CollectionUtils.isEmpty(accountsList)) {
            return false;
        }
        OfficialAccounts acc = accountsList.stream().filter(o -> o.getId() != null && Objects.equals(o.getId(), accountId)).findAny().orElse(null);
        return acc != null;
    }

    @Override
    public MapMessage updateFollowStatus(Long parentId, Long accountId, UserOfficialAccountsRef.Status refStatus) {
        if (parentId == null || accountId == null || refStatus == null) {
            return MapMessage.errorMessage("参数错误");
        }

        OfficialAccounts accounts = officialAccountsPersistence.load(accountId);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在！");

        boolean firstFollow = false;
        if (!isFollow(accountId, parentId) &&
                (refStatus == UserOfficialAccountsRef.Status.Follow
                        || refStatus == UserOfficialAccountsRef.Status.AutoFollow))
            firstFollow = true;


        UserOfficialAccountsRef ref = loadUserOfficialAccountsRef(parentId, accountId);
        if (ref == null) {
            ref = new UserOfficialAccountsRef();
            ref.setStatus(refStatus);
            ref.setOfficialAccountsId(accountId);
            ref.setUserId(parentId);
            ref.setAccountsKey(accounts.getAccountsKey());
            userOfficialAccountsRefPersistence.insert(ref);
        } else {
            ref.setUpdateDatetime(new Date());
            ref.setStatus(refStatus);
            userOfficialAccountsRefPersistence.updateFollowStatus(ref);
        }

        // 首次关注时，发送回复消息
        if (firstFollow) {
            String greetings = accounts.getGreetings();
            // 默认关注回复
            if (StringUtils.isEmpty(greetings))
                greetings = "您已经成功关注" + accounts.getName() + "公众号。";

            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("accountsKey", accounts.getAccountsKey());
            // 手动设置模板生成时间比关注操作时间晚
            extInfo.put("createTime", ref.getUpdateDatetime().getTime() + TimeUnit.SECONDS.toMillis(1L));

            String extInfoStr = JsonUtils.toJson(extInfo);

            sendMessage(
                    Collections.singletonList(parentId),
                    "关注成功",
                    greetings,
                    generateAccountDetailUrl(accountId),// 为了防止老版本崩溃，跳转到资料详情页
                    extInfoStr,
                    true);
        }

        // 清除缓存
        String cacheKey = userOfficialListCacheKeyFix + parentId;
        CacheSystem.CBS.getCache("flushable").delete(cacheKey);
        return MapMessage.successMessage();
    }

    private UserOfficialAccountsRef loadUserOfficialAccountsRef(Long userId, Long accountId) {
        List<UserOfficialAccountsRef> refList = userOfficialAccountsRefPersistence.loadByUserId(userId);
        UserOfficialAccountsRef ref = null;
        if (CollectionUtils.isNotEmpty(refList)) {
            ref = refList.stream().filter(r -> Objects.equals(r.getOfficialAccountsId(), accountId)).findAny().orElse(null);
        }
        return ref;
    }

    /**
     * 生成公众号资料详情页的站内url
     *
     * @param accountsId
     * @return
     */
    private String generateAccountDetailUrl(Long accountsId) {
        return MessageFormat.format(ACCOUNTS_DETAIL_URL, accountsId);
    }

    @Override
    public MapMessage sendMessage(List<Long> parentIds, String title, String content, String linkUrl, String extInfoStr, Boolean sendPush) {
        if (CollectionUtils.isEmpty(parentIds) || StringUtils.isBlank(title) || StringUtils.isBlank(content) || StringUtils.isBlank(extInfoStr)) {
            return MapMessage.errorMessage("参数错误");
        }
        // 校验公众号消息相关
        Map<String, Object> extInfo = JsonUtils.convertJsonObjectToMap(extInfoStr);
        if (MapUtils.isEmpty(extInfo) || extInfo.get("accountsKey") == null) {
            return MapMessage.errorMessage("不支持公众号以外的消息");
        }
        // 获取公众号ID
        OfficialAccounts accounts = officialAccountsPersistence.loadByAccountsKey(SafeConverter.toString(extInfo.get("accountsKey")));
        if (accounts == null) {
            return MapMessage.errorMessage("公众号不存在");
        }

        // 如果外部有传入指定的创建时间，则应用。否则取当前的时间
        // 这样做是为了校准其它数据生成时间与模板生成时间，避免漏数据的情况
        // 由于全局消息用了buffer，这里将消息生成和推送的时间都延迟
        Long createTime = SafeConverter.toLong(extInfo.get("createTime"));
        if (createTime == 0L)
            createTime = System.currentTimeMillis();

        List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(parentIds, ParentMessageType.OFFICIAL_MSG.getType(),
                title, content, "", linkUrl, 0, extInfoStr);
        for (AppMessage m : userMessageList) {
            m.setCreateTime(createTime);

            final Map<String, Object> messageBody = new LinkedHashMap<>();
            messageBody.put("userMessage", m);
            messageBody.put("sendPush", sendPush);
            messageBody.put("officialAccounts", accounts);
            messageBody.put("createTime", createTime);
            messageBody.put("jpushContent", accounts.getName() + "：" + content);

            Message message = Message.newMessage();
            message.withPlainTextBody(JsonUtils.toJson(messageBody));
            producer.produce(message);
        }

        return MapMessage.successMessage("发送成功");
    }

    /**
     * 给关注公众号的所有用户发送模板信息
     *
     * @param accountsKey
     * @param title
     * @param content
     * @param linkUrl
     * @param durationTime 发送时速，指定多长时间内发完。单位是分钟
     * @return
     */
    @Override
    public MapMessage sendGlobalMessage(String accountsKey,
                                        String title,
                                        String content,
                                        Integer linkType,
                                        String linkUrl,
                                        Boolean sendPush,
                                        Integer durationTime) {
        OfficialAccounts accounts = loadAccountByKey(accountsKey);
        if (accounts == null)
            return MapMessage.errorMessage("公众号不存在");

        AppGlobalMessage msg = new AppGlobalMessage();
        msg.setMessageSource(AppMessageSource.PARENT.name());
        msg.setTitle(title);
        msg.setContent(content);
        msg.setLinkType(linkType);
        msg.setLinkUrl(linkUrl);
        msg.setMessageType(ParentMessageType.OFFICIAL_MSG.type);

        Map<String, Object> extInfoMap = new HashMap<>();
        extInfoMap.put("accountsKey", accountsKey);
        msg.setExtInfo(extInfoMap);

        // 和个人消息模板一样，需要校准模板消息的时间和推送报文的时间
        Long createTime = System.currentTimeMillis();
        // 设置为10分钟后，并且朝5分钟向后取齐
        createTime = createTime + 10 * 60 * 1000;
        createTime = sendTimeCeil(createTime) * 1000L;

        msg.setCreateTime(createTime);

        if(durationTime == null)
            durationTime = 0;
        // 最少都是30分钟的推送时速
        durationTime = Math.max(30,durationTime);

        messageCommandServiceClient.getMessageCommandService().createAppGlobalMessage(msg);

        // 根据Push开关控制是否发送push
        if (sendPush != null && sendPush) {
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
            jpushExtInfo.put("timestamp", msg.getCreateTime());
            String jpushContent = accounts.getName() + "：" + content;

            appMessageServiceClient.sendAppJpushMessageByTags(
                    jpushContent,
                    AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accountsKey)),
                    null,
                    jpushExtInfo,
                    durationTime,
                    createTime);
        }

        return MapMessage.successMessage();
    }

    public static Long sendTimeCeil(Long originEpochMilli) {
        if (Long.MAX_VALUE < originEpochMilli || originEpochMilli <= 0) {
            return 0L;
        }

        Long originepochSecond = originEpochMilli / 1000;

        if (originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS == 0) {
            return originepochSecond;
        }

        return originepochSecond - originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS + SEND_TIME_DISCRETE_BASED_SECONDS;
    }

    @Override
    public void saveAccountTool(OfficialAccountsTools tools) {
        officialAccountsToolsPersistence.__persist(tools);
    }

    @Override
    public List<UserOfficialAccountsRef> loadUserOfficialAccoutnsRef(Long userId) {
        return userOfficialAccountsRefPersistence.loadByUserId(userId);
    }

    private List<Map<String, Object>> getArticleList(List<OfficialAccountsArticle> value) {
        if (CollectionUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        value.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (OfficialAccountsArticle article : value) {
            Map<String, Object> map = new HashMap<>();
            map.put("articleUrl", article.getArticleUrl());
            map.put("imgUrl", article.getImgUrl());
            map.put("articleTitle", article.getArticleTitle());
            map.put("bundleId", article.getBundleId());
            map.put("updateTime", article.getUpdateDatetime());
            map.put("bindSid", article.getBindSid());
            // 打点需要用到
            map.put("articleId", article.getId());
            dataList.add(map);
        }
        return dataList;
    }

    private boolean checkRegionNew(List<Integer> regionCodeList, List<OfficialAccountsTarget> targetList) {
        if (CollectionUtils.isNotEmpty(targetList)) {
            for (OfficialAccountsTarget target : targetList) {
                for (Integer code : regionCodeList) {
                    if (StringUtils.equals(target.getTargetStr(), String.valueOf(code)))
                        return true;
                }
            }
        }
        return false;
    }
}
