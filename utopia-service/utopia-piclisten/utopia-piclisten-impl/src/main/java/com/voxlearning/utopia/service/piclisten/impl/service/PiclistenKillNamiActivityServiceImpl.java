package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheBuilder;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.galaxy.service.activity.api.AssistGroupActivityService;
import com.voxlearning.galaxy.service.activity.api.constants.assistgroup.AssistStatus;
import com.voxlearning.galaxy.service.activity.api.constants.assistgroup.AssistType;
import com.voxlearning.galaxy.service.activity.api.entity.assistgroup.AssistConfigMapper;
import com.voxlearning.galaxy.service.activity.api.entity.assistgroup.AssistGroup;
import com.voxlearning.galaxy.service.activity.api.entity.assistgroup.AssistGroupMemberRef;
import com.voxlearning.galaxy.service.activity.api.entity.assistgroup.AssistTypeConfig;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.PiclistenKillNamiActivityService;
import com.voxlearning.utopia.service.piclisten.api.mapper.PiclistenKillNamiActivityContext;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.PicListenShareSendCouponCacheManager;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.impl.support.PiclistenKillNamiActivity;
import com.voxlearning.utopia.service.piclisten.support.PiclistenBookImgUtils;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-08-23 上午10:56
 **/
@Named
@ExposeServices({
        @ExposeService(interfaceClass = PiclistenKillNamiActivityService.class, version = @ServiceVersion(version = "2018.09.04")),
        @ExposeService(interfaceClass = PiclistenKillNamiActivityService.class, version = @ServiceVersion(version = "2018.09.17"))
})
public class PiclistenKillNamiActivityServiceImpl extends SpringContainerSupport implements PiclistenKillNamiActivityService {

    @Inject
    private PicListenCommonServiceImpl picListenCommonService;

    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;

    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;

    @ImportService(interfaceClass = AssistGroupActivityService.class)
    private AssistGroupActivityService assistGroupActivityService;

    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    private PicListenShareSendCouponCacheManager picListenShareSendCouponCacheManager;


    @Inject
    private CouponServiceClient couponServiceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        UtopiaCache cache = CacheBuilder.Loader.getCacheBuilder(CacheSystem.CBS).getCache("persistence");
        picListenShareSendCouponCacheManager = new PicListenShareSendCouponCacheManager(cache);
    }

    @Override
    public MapMessage recBooks(PiclistenKillNamiActivityContext context) {
        return recBooks(context, "");
    }

    @Override
    public MapMessage recBooks(PiclistenKillNamiActivityContext context, String productId) {
        if (context.getStudentDetail() == null && context.loginUserIsParent()) {
            List<User> users = studentLoaderClient.loadParentStudents(context.getLoginUser().getId());
            if (CollectionUtils.isNotEmpty(users)) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(users.get(0).getId());
                if (studentDetail != null) {
                    context.setStudentDetail(studentDetail);
                }
            }
        }
        Integer clazzLevel = getClazzLevelForRecBook(context.getStudentDetail());
        AlpsFuture<List<Map<String, Object>>> buyerListFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_fetch();

        Boolean isAuth = context.isLogin() ? picListenCommonService.userIsAuthForPicListen(context.getLoginUser()) : false;
        NewBookProfile englishBook = null;
        NewBookProfile chineseBook = null;
        if (context.isLogin()) {
            List<String> parentSelectBookIds = getUserSelectBookIds(context.getLoginUser().getId());
            if (CollectionUtils.isNotEmpty(parentSelectBookIds)) {
                Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(parentSelectBookIds);
                List<NewBookProfile> bookProfiles = new ArrayList<>(bookProfileMap.values());
                if (CollectionUtils.isNotEmpty(bookProfiles)) {
                    for (NewBookProfile newBookProfile : bookProfiles) {
                        if (newBookProfile.getSubjectId().equals(Subject.ENGLISH.getId())) {
                            englishBook = newBookProfile;
                        } else if (newBookProfile.getSubjectId().equals(Subject.CHINESE.getId())) {
                            chineseBook = newBookProfile;
                        }
                    }
                } else {
                    englishBook = recBook(context.getStudentDetail(), Subject.ENGLISH, context.getSys(), isAuth, clazzLevel);
                }
            } else {
                englishBook = recBook(context.getStudentDetail(), Subject.ENGLISH, context.getSys(), isAuth, clazzLevel);
//                chineseBook = recBook(context.getStudentDetail(), Subject.CHINESE, context.getSys(), isAuth, clazzLevel);
            }
        } else {
            englishBook = recBook(context.getStudentDetail(), Subject.ENGLISH, context.getSys(), isAuth, clazzLevel);
//            chineseBook = recBook(context.getStudentDetail(), Subject.CHINESE, context.getSys(), isAuth, clazzLevel);
        }
        if (StringUtils.isNotBlank(productId)) {
            List<OrderProductItem> bookItem = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (bookItem.size() == 1) {
                String bookId = bookItem.get(0).getAppItemId();
                if (StringUtils.isNotBlank(bookId)) {
                    NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                    if (newBookProfile != null) {
                        switch (Subject.fromSubjectId(newBookProfile.getSubjectId())) {
                            case CHINESE:
                                chineseBook = newBookProfile;
                                break;
                            case ENGLISH:
                                englishBook = newBookProfile;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (englishBook == null && chineseBook == null) {
            return MapMessage.errorMessage(" 推荐教材出错了呢！");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("buyer_list", buyerListFuture.getUninterruptibly());
        Long buyerCount = picListenCommonService.loadPicListenPurchaseCount();
        mapMessage.put("buyer_count", buyerCount);

        ////////////
        Map<String, Object> generateBookListPackageMap = generateBookListPackageMap(englishBook, chineseBook, context);
        mapMessage.putAll(generateBookListPackageMap);
        AssistTypeConfig assistConfig = getAssistConfig();
        return mapMessage.add("assist_config_count", assistConfig == null ? 2 : assistConfig.getSucceeMemeberCount());
    }

    @Override
    public MapMessage changeSelectBooks(PiclistenKillNamiActivityContext context, List<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds) || bookIds.size() < 1) {
            return MapMessage.errorMessage("教材数量不够啊亲！");
        }

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIds);
//        Set<Subject> subjectSet = new HashSet<>();
//        subjectSet.add(Subject.ENGLISH);
//        subjectSet.add(Subject.CHINESE);
        NewBookProfile chineseBook = null;
        NewBookProfile englishBook = null;
        for (NewBookProfile bookProfile : bookProfileMap.values()) {
            Subject subject = Subject.fromSubjectId(bookProfile.getSubjectId());
            if (subject == Subject.ENGLISH) {
                englishBook = bookProfile;
            }
            if (subject == Subject.CHINESE) {
                chineseBook = bookProfile;
            }
//            subjectSet.remove(subject);
        }
//        if (CollectionUtils.isNotEmpty(subjectSet)) {
//            return MapMessage.errorMessage("传入教材 id 不是一个英语一个语文呢！");
//        }
        if (englishBook == null && chineseBook == null) {
            return MapMessage.errorMessage("获取教材出错了呢！");
        }

        if (context.isLogin()) {
            saveUserSelectBookIds(context.getLoginUser().getId(), bookIds);
        }

        Map<String, Object> generateBookListPackageMap = generateBookListPackageMap(englishBook, chineseBook, context);
        MapMessage successMessage = MapMessage.successMessage();
        successMessage.putAll(generateBookListPackageMap);
        return successMessage;
    }

    @Override
    public MapMessage createAssist(PiclistenKillNamiActivityContext context, String productId) {
        MapMessage mapMessage = checkProductIdCouldAssist(productId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        AssistGroup newAssistGroup = assistGroupActivityService.createNewAssistGroup(context.getLoginUser().getId(), AssistType.PICLISTEN_201809, productId);
        if (newAssistGroup != null) {
            AssistTypeConfig assistTypeConfig = getAssistConfig();
            if (assistTypeConfig != null) {
//                Long time = (newAssistGroup.getCreateTime().getTime() / 1000) + assistTypeConfig.getExpireInSeconds() - PiclistenKillNamiActivity.expireRemindTime;
//                piclistenKillNamiAssistExpireRemindDelayMessageSupport.pushDelayMessage(time, newAssistGroup.getId());
            } else {
                return MapMessage.errorMessage("助力配置丢失");
            }

            return MapMessage.successMessage().add("assist_id", newAssistGroup.getId());
        }
        return MapMessage.errorMessage("创建助力失败！");
    }

    @Override
    public MapMessage doAssist(String assistId, String openId) {
        if (PiclistenKillNamiActivity.maxAssistCount > 0) {
            Long assistCount = incrOpenIdAssistCount(openId, 1L);
            if (PiclistenKillNamiActivity.limitAssistCount() && assistCount > PiclistenKillNamiActivity.maxAssistCount) {
                return MapMessage.errorMessage("您已经助力过其他人啦！");
            }
        }
        MapMessage mapMessage = assistGroupActivityService.doAssist(assistId, openId);
        if (!mapMessage.isSuccess()) {
            incrOpenIdAssistCount(openId, -1L);
        }
        return mapMessage;
    }

    private String generateOpenIdAssistCountKey(String openId) {
        return CacheKeyGenerator.generateCacheKey("piclistenKillNamiDoAssistOpenIdCount", new String[]{"OID"}, new Object[]{openId});
    }

    private Long incrOpenIdAssistCount(String openId, Long delta) {
        String key = generateOpenIdAssistCountKey(openId);
        return PiclistenCache.getPersistenceCache().incr(key, delta, 1L, (int) (PiclistenKillNamiActivity.endDate.getTime() / 1000));
    }

    private Long loadOpenIdAssistCount(String openId) {
        String key = generateOpenIdAssistCountKey(openId);
        return SafeConverter.toLong(PiclistenCache.getPersistenceCache().load(key));

    }

    @Override
    public MapMessage assistDetail(String assistId, String openId) {
        AssistGroup assistGroup = assistGroupActivityService.loadAssistGroupById(assistId);
        if (assistGroup == null) {
            return MapMessage.errorMessage("助力不存在哦！");
        }
        AssistTypeConfig assistConfig = getAssistConfig();
        if (assistConfig == null) {
            return MapMessage.errorMessage("助力配置不存在");
        }
        List<AssistGroupMemberRef> assistGroupMemberRefs = assistGroupActivityService.loadAssistMembers(assistId);
        Map<String, Object> assistMap = new HashMap<>();
        assistMap.put("status", assistGroup.getStatus().name());
        assistMap.put("assisted", StringUtils.isNoneBlank(openId) && assistGroupMemberRefs.stream().anyMatch(t -> t.getAssistMember().getOpenId().equals(openId)));
        Long assistCount = StringUtils.isBlank(openId) ? 0L : loadOpenIdAssistCount(openId);
        boolean hasChance = !PiclistenKillNamiActivity.limitAssistCount() || assistCount < PiclistenKillNamiActivity.maxAssistCount;
        assistMap.put("has_chance", hasChance);
        boolean showCountdown = hasChance && assistGroup.getStatus() == AssistStatus.assisting;
        if (showCountdown) {
            assistMap.put("countdown", (assistGroup.getCreateTime().getTime() / 1000) + assistConfig.getExpireInSeconds() - (System.currentTimeMillis() / 1000));
        }
        assistMap.put("has_chance", !PiclistenKillNamiActivity.limitAssistCount() || assistCount < PiclistenKillNamiActivity.maxAssistCount);
        Long purchaseCount = picListenCommonService.loadPicListenPurchaseCount();
        return MapMessage.successMessage().add("buyer_count", purchaseCount)
                .add("assist_info", assistMap);
    }

    private String assistGroupStatus(AssistGroup assistGroup) {
        if (assistGroup == null)
            return "none";
        if (assistGroup.safeIsExpire()) {
            String expireReason = assistGroup.getExpireReason();
            if ("couponExpire".equals(expireReason)) {
                return "purchase_expire";
            } else {
                return "expire";
            }
        } else {
            return assistGroup.getStatus().name();
        }
    }

    @Override
    public MapMessage assistMembers(PiclistenKillNamiActivityContext context, String assistId) {
        AssistGroup assistGroup = assistGroupActivityService.loadAssistGroupById(assistId);
        if (assistGroup == null) {
            return MapMessage.errorMessage("助力不存在！");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        AssistTypeConfig assistConfig = getAssistConfig();
        if (assistConfig == null) {
            return MapMessage.errorMessage("助力配置丢了！");
        }

        String productId = assistGroup.getTargetId();
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(productId);
        Map<String, PicListenBookPayInfo> picListenBuyInfoMap = picListenCommonService.parentPicListenBuyInfoMap(assistGroup.getUserId(), false);
        boolean purchased = orderProductItems.stream().map(OrderProductItem::getAppItemId).allMatch(picListenBuyInfoMap::containsKey);
        mapMessage.put("purchased", purchased);
        if (!purchased) {
            if (assistGroup.safeIsAssisting()) {
                Date createTime = assistGroup.getCreateTime();
                Integer expireInSeconds = assistConfig.getExpireInSeconds();
                long countdown = createTime.getTime() / 1000 + expireInSeconds - System.currentTimeMillis() / 1000;
                mapMessage.add("countdown", countdown);
            } else if (assistGroup.safeIsSuccess()) {
                Date successDate = assistGroup.getSuccessDate();
                long countdown = successDate.getTime() / 1000 + PiclistenKillNamiActivity.couponExpireTime - System.currentTimeMillis() / 1000;
                mapMessage.add("countdown", countdown);
            }
        }
        mapMessage.add("assist_status", assistGroupStatus(assistGroup));

        List<AssistGroupMemberRef> assistGroupMemberRefs = assistGroupActivityService.loadAssistMembers(assistId);
        List<Map<String, Object>> memberMapList = assistGroupMemberRefs.stream().sorted(Comparator.comparing(AssistGroupMemberRef::getCreateDate)).map(t -> {
            Map<String, Object> map = new HashMap<>();
            AssistGroupMemberRef.AssistMember assistMember = t.getAssistMember();
            if (assistMember == null)
                return null;
            map.put("name", assistMember.getName());
            map.put("avatar_url", assistMember.getAvatarUrl());
            map.put("time", DateUtils.dateToString(t.getCreateDate(), "yyyy.MM.dd"));
            return map;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return mapMessage.add("member_list", memberMapList).add("rest_count", assistConfig.getSucceeMemeberCount() - memberMapList.size());
    }

    @Override
    public MapMessage publisherList(Subject subject) {
        List<TextBookMapper> textBookMapperList = textBookManagementLoader.getTextBookManagementBuffer().getTextBookMapperList();
        List<String> publisherList = textBookMapperList.stream().sorted(Comparator.comparingInt(TextBookMapper::getRank))
                .map(TextBookMapper::getPublisherShortName).collect(Collectors.toList());
        return MapMessage.successMessage().add("publisher_list", publisherList);
    }

    @Override
    public MapMessage bookList(Subject subject, String publisherName, Integer clazzLevel, String sys) {
        List<TextBookManagement> textBookManagementList =
                textBookManagementLoader.getTextBookManagementByClazzLevel(clazzLevel);
        List<String> bookIdList = textBookManagementList.stream()
                .filter(t -> t != null
                        && t.getShortPublisherName().equals(publisherName)
                        && Objects.equals(subject.getId(), t.getSubjectId())
                        && textBookManagementLoader.picListenShow(t.getBookId(), sys, true))
                .map(TextBookManagement::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        List<Map<String, Object>> bookMapList = bookProfileMap.values().stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("book_id", t.getId());
            map.put("name", t.getShortName());
            return map;
        }).collect(Collectors.toList());
        return MapMessage.successMessage().add("book_list", bookMapList);
    }

    @Override
    public MapMessage purchaseInfo(PiclistenKillNamiActivityContext context, String orderId) {
        if (!context.loginUserIsParent()) {
            return MapMessage.errorMessage("未登录");
        }
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            return MapMessage.errorMessage("订单错误");
        }
        if (!userOrder.getUserId().equals(context.getLoginUser().getId())) {
            return MapMessage.errorMessage("订单权限错误");
        }
        List<UserOrderPaymentHistory> userOrderPaymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(context.getLoginUser().getId());
        UserOrderPaymentHistory paymentHistory = userOrderPaymentHistories.stream().filter(t -> Objects.equals(t.getOrderId(), userOrder.getId()) && t.getPaymentStatus() == PaymentStatus.Paid).findFirst().orElse(null);
        if (paymentHistory == null) {
            return MapMessage.errorMessage("未支付成功！");
        }
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(userOrder.getProductId());
        List<String> bookIdList = itemList.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        List<String> imageList = bookProfileMap.values().stream().filter(t -> !"少英报".equals(t.getShortPublisher())).map(t -> {
            if (StringUtils.isBlank(t.getImgUrl())) {
                return "";
            } else {
                return context.getImgCdn() + t.getImgUrl();
            }
        }).collect(Collectors.toList());
        return MapMessage.successMessage().add("images", imageList).add("product_name", userOrder.getProductName())
                .add("pay_money", paymentHistory.getPayAmount().intValue() + "元")
                .add("pay_time", DateUtils.dateToString(paymentHistory.getPayDatetime()));
    }

    @Override
    public Boolean addShareRecord(Long parentId, String productId) {
        return picListenShareSendCouponCacheManager.addRecord(parentId, productId);
    }

    @Override
    public Long loadShareRecord(Long parentId, String productId) {
        return picListenShareSendCouponCacheManager.load(parentId, productId);
    }

    @Override
    public MapMessage sendCouponForShare(String productId, Long parentId) {
        MapMessage mapMessage = checkProductIdCouldAssist(productId);
        if (!mapMessage.isSuccess()) {
            logger.error("点读机活动，产品 id 错误： " + mapMessage.getInfo());
            return MapMessage.errorMessage("产品 id 错误： " + mapMessage.getInfo());
        }
        String couponId = chooseCouponId(mapMessage);
        if (StringUtils.isBlank(couponId)) {
            logger.error("点读机活动，选择优惠券失败！");
            MapMessage.errorMessage("点读机活动，选择优惠券失败！");
        }
        // 发送优惠券
        MapMessage sendCoupon = couponServiceClient.sendCoupon(couponId, parentId);
        if (!sendCoupon.isSuccess()) {
            return MapMessage.errorMessage("点读机活动，发送优惠券失败！：" + sendCoupon.getInfo());
        }

        return sendCoupon;
    }

    @Override
    public MapMessage newBookList(String publisherName, Integer clazzLevel, String sys, PiclistenKillNamiActivityContext piclistenKillNamiActivityContext, Boolean isPackage, Boolean hasClazzLevel, Boolean isSameSubject) {
        List<TextBookManagement> textBookManagementList;
        if (piclistenKillNamiActivityContext.getStudentDetail() != null && clazzLevel == 0) {
            clazzLevel = getClazzLevelForRecBook(piclistenKillNamiActivityContext.getStudentDetail());
        } else if (clazzLevel == 0) {
            clazzLevel = 1;
        }
        if (hasClazzLevel) {
            textBookManagementList = textBookManagementLoader.getTextBookManagementByClazzLevel(clazzLevel);
        } else {
            textBookManagementList = textBookManagementLoader.getTextBookManagementList();
        }
        Boolean isAuth = piclistenKillNamiActivityContext.isLogin() ? picListenCommonService.userIsAuthForPicListen(piclistenKillNamiActivityContext.getLoginUser()) : false;
        List<String> bookIdList = textBookManagementList.stream()
                .filter(t -> t != null
                        && t.getShortPublisherName().equals(publisherName)
                        && textBookManagementLoader.picListenShow(t.getBookId(), sys, isAuth))
                .map(TextBookManagement::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        Map<String, PicListenBookPayInfo> buyInfoMap = piclistenKillNamiActivityContext.isLogin() ? picListenCommonService.userBuyBookPicListenLastDayMap(piclistenKillNamiActivityContext.getLoginUser(), false) : Collections.emptyMap();
        List<Map<String, Object>> returnList = new ArrayList<>();
        if (isPackage) {
            Map<String, List<OrderProduct>> productByAppItemIds = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIdList);
            Set<String> packageProductSet = new HashSet<>();
            for (Map.Entry<String, List<OrderProduct>> entry : productByAppItemIds.entrySet()) {
                List<OrderProduct> orderProductList = entry.getValue();
                List<OrderProduct> packageProducts = orderProductList.stream().filter(t -> isPackage(t) && OrderProductServiceType.safeParse(t.getProductType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(packageProducts)) {
                    continue;
                }
                for (OrderProduct packageProduct : packageProducts) {
                    if (!packageProductSet.add(packageProduct.getId())) {
                        continue;
                    }
                    List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(packageProduct.getId());
                    List<String> packageBookIdList = orderProductItems.stream().filter(e -> bookIdList.contains(e.getAppItemId())).map(OrderProductItem::getAppItemId).collect(Collectors.toList());
                    if (packageBookIdList.size() == 2 && (packageBookIdList.stream().noneMatch(e -> buyInfoMap.keySet().contains(e)) || buyInfoMap.keySet().containsAll(packageBookIdList))) {
                        NewBookProfile book1 = bookProfileMap.get(packageBookIdList.get(0));
                        NewBookProfile book2 = bookProfileMap.get(packageBookIdList.get(1));
                        if (StringUtils.equals("人教版", publisherName)) {
                            if (isSameSubject && !book1.getSubjectId().equals(book2.getSubjectId())) {
                                continue;
                            } else if (!isSameSubject && book1.getSubjectId().equals(book2.getSubjectId())) {
                                continue;
                            }
                        }
                        returnList.add(packageProductMap(book1, book2, packageProduct, buyInfoMap));
                    }
                }
            }
        } else {
            Map<String, OrderProduct> productMap = loadBookOrderProducts(bookProfileMap.keySet());
            bookProfileMap.values().forEach(e -> {
                returnList.add(bookMap(e, productMap.get(e.getId()), buyInfoMap));
            });
        }
        return MapMessage.successMessage().add("new_book_list", returnList).add("clazz_level", clazzLevel);
    }

    @Override
    public Long loadNewRecord(Long parentId) {
        return picListenShareSendCouponCacheManager.loadNewSendRecord(parentId);
    }

    @Override
    public MapMessage sendNewCoupon(Long parentId) {
        return MapMessage.errorMessage("活动已结束！");
//        // 发送优惠券
//        MapMessage sendCoupon = couponServiceClient.sendCoupon(PiclistenKillNamiActivity.couponIdMap.get("NEW_COUPON"), parentId);
//        if (!sendCoupon.isSuccess()) {
//            return MapMessage.errorMessage("点读机活动，发送优惠券失败！：" + sendCoupon.getInfo());
//        }
//        return sendCoupon;
    }

    @Override
    public Boolean addNewRecord(Long parentId) {
        return picListenShareSendCouponCacheManager.addNewSendRecord(parentId);
    }

    public MapMessage checkProductIdCouldAssist(String productId) {
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
        if (orderProduct == null) {
            return MapMessage.errorMessage("错误的助力产品 id");
        }
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) != OrderProductServiceType.PicListenBook) {
            return MapMessage.errorMessage("错误的助力产品类型");
        }
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(productId);
        List<String> bookIdList = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        NewBookProfile noSupportAssistBook = bookProfileMap.values().stream().filter(t -> !bookSupportAssist(t)).findFirst().orElse(null);
        if (noSupportAssistBook == null || noSupportAssistBook.getShortPublisher().equals("少英报")) {
            return MapMessage.successMessage()
                    .add("product", orderProduct)
                    .add("items", orderProductItems)
                    .add("bookMap", bookProfileMap);
        } else {
            return MapMessage.errorMessage("错误的助力产品教材");
        }
    }

    public String chooseCouponId(MapMessage mapMessage) {
        try {
//            OrderProduct product = OrderProduct.class.cast(mapMessage.get("product"));
//            List<OrderProductItem> itemList = (List<OrderProductItem>) mapMessage.get("items");
            Map<String, NewBookProfile> bookProfileMap = (Map<String, NewBookProfile>) mapMessage.get("bookMap");
            if (bookProfileMap.size() == 1) {
                NewBookProfile bookProfile = bookProfileMap.values().stream().findFirst().orElse(null);
                Subject subject = Subject.fromSubjectId(bookProfile.getSubjectId());
                if (subject == Subject.CHINESE) {
                    return PiclistenKillNamiActivity.couponIdMap.get("CHINESE");
                } else if (subject == Subject.ENGLISH) {
                    return PiclistenKillNamiActivity.couponIdMap.get("ENGLISH");
                }
            } else {
                return PiclistenKillNamiActivity.couponIdMap.get("PACKAGE");
            }
            return "";
        } catch (Exception e) {
            logger.error("chooseCouponId error ", e);
            return "";
        }
    }

    private AssistTypeConfig getAssistConfig() {
        AssistConfigMapper assistConfigMapper = pageBlockContentServiceClient.loadConfigObject(AssistGroupActivityService.assistConfigPage, AssistGroupActivityService.assistConfigBlock,
                AssistConfigMapper.class);
        if (assistConfigMapper != null) {
            return assistConfigMapper.get(AssistType.PICLISTEN_201809);
        }
        return null;
    }

    private Map<String, Object> generateBookListPackageMap(NewBookProfile englishBook, NewBookProfile chineseBook, PiclistenKillNamiActivityContext context) {

        Map<String, PicListenBookPayInfo> buyInfoMap = context.isLogin() ? picListenCommonService.userBuyBookPicListenLastDayMap(context.getLoginUser(), false) : Collections.emptyMap();
        List<NewBookProfile> newBookProfiles = new ArrayList<>();
        if (englishBook != null) {
            newBookProfiles.add(englishBook);
        }
        if (chineseBook != null) {
            newBookProfiles.add(chineseBook);
        }
        Map<OrderProduct, List<NewBookProfile>> packageBookMap = new HashMap<>();
//        List<NewBookProfile> newBookProfiles = Arrays.asList(englishBook, chineseBook);
        List<OrderProduct> packageProductList = getRenjiaoPackageProduct(newBookProfiles, buyInfoMap);
        if (CollectionUtils.isNotEmpty(packageProductList)) {
            for (OrderProduct orderProduct : packageProductList) {
                List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
                List<String> bookIds = orderProductItems.stream().filter(e -> StringUtils.isNotBlank(e.getAppItemId())).map(OrderProductItem::getAppItemId).collect(Collectors.toList());
                Map<String, NewBookProfile> books = newContentLoaderClient.loadBooks(bookIds);
                if (MapUtils.isNotEmpty(books)) {
                    List<NewBookProfile> profiles = books.values().stream().filter(e -> e.getShortPublisher().equals("人教版")).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(profiles)) {
                        packageBookMap.put(orderProduct, profiles);
                    }
                }
            }

        }

        Map<String, OrderProduct> bookProductMap = loadBookOrderProducts(newBookProfiles.stream().map(NewBookProfile::getId).collect(Collectors.toList()));
        List<String> productIdList = bookProductMap.values().stream().map(ObjectIdEntity::getId).collect(Collectors.toList());
        if (MapUtils.isNotEmpty(packageBookMap)) {
            List<String> packageIds = packageBookMap.keySet().stream().map(OrderProduct::getId).collect(Collectors.toList());
            productIdList.addAll(packageIds);
        }
//        Map<String, AssistGroup> assistGroupMap = Collections.emptyMap();
//        if (context.loginUserIsParent()) {
//            assistGroupMap = assistGroupActivityService.loadAssistGroupByUserIdTypeAndTargetIds(context.getLoginUser().getId(), AssistType.PICLISTEN_201809, productIdList);
//        }

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        if (englishBook != null) {
            OrderProduct englishProduct = bookProductMap.get(englishBook.getId());
            bookMapList.add(bookMap(englishBook, englishProduct, buyInfoMap));
        }

        if (chineseBook != null) {
            OrderProduct chineseProduct = bookProductMap.get(chineseBook.getId());
            bookMapList.add(bookMap(chineseBook, chineseProduct, buyInfoMap));
        }

        map.put("book_list", bookMapList);
        List<Map<String, Object>> returnPackageList = new ArrayList<>();
        if (MapUtils.isNotEmpty(packageBookMap)) {
            packageBookMap.forEach((k, v) -> {
//                NewBookProfile packageEnglishBook = v.stream().filter(e -> Subject.ENGLISH.getId() == e.getSubjectId()).findFirst().orElse(new NewBookProfile());
//                NewBookProfile packageChineseBook = v.stream().filter(e -> Subject.CHINESE.getId() == e.getSubjectId()).findFirst().orElse(new NewBookProfile());
                //打包教材小于2，不处理
                if (v.size() < 2) {
                    return;
                }
                returnPackageList.add(packageProductMap(v.get(0), v.get(1), k, buyInfoMap));
            });

            map.put("package_info", returnPackageList);
        }
        return map;
    }


    private String generateUserSelectBooksKey(Long userId) {
        return CacheKeyGenerator.generateCacheKey("PiclistenKillNamiSelectBookId", new String[]{"pid"}, new Object[]{userId});
    }


    private void saveUserSelectBookIds(Long userId, List<String> bookIds) {
        String key = generateUserSelectBooksKey(userId);
        PiclistenCache.getPersistenceCache().set(key, (int) (PiclistenKillNamiActivity.endDate.getTime() / 1000), bookIds);
    }

    private List<String> getUserSelectBookIds(Long userId) {
        String key = generateUserSelectBooksKey(userId);
        List<String> bookIds = PiclistenCache.getPersistenceCache().load(key);
        if (bookIds == null)
            return Collections.emptyList();
        return bookIds;
    }


    private Map<String, Object> packageProductMap(NewBookProfile chineseBook, NewBookProfile englishBook, OrderProduct packageProduct, Map<String, PicListenBookPayInfo> payInfoMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("img_url_0", imgUrl(englishBook));
        map.put("img_url_1", imgUrl(chineseBook));
        boolean purchased;
        if (packageProduct != null) {
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(packageProduct.getId());
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                OrderProductItem orderProductItem = orderProductItems.stream().filter(e -> StringUtils.equals(chineseBook.getId(), e.getAppItemId())).findFirst().orElse(null);
                if (orderProductItem != null) {
                    map.put("period", orderProductItem.getPeriod());
                }
            }
//            if (context.loginUserIsParent()) {
//                Long shareRecord = picListenShareSendCouponCacheManager.load(context.getLoginUser().getId(), packageProduct.getId());
//                if (shareRecord != 0L) {
//                    Date date = new Date(shareRecord);
//                    map.put("has_coupon", Boolean.TRUE);
//                    map.put("count_down", (date.getTime() / 1000) + 86400 - (System.currentTimeMillis() / 1000));
//                } else {
//                    map.put("has_coupon", Boolean.FALSE);
//                }
//            } else {
//                map.put("has_coupon", Boolean.FALSE);
//            }
            map.put("product_id", packageProduct.getId());
            map.put("name", packageProduct.getName());
            map.put("original_price", packageProduct.getOriginalPrice());
            map.put("price", packageProduct.getPrice());
//            map.put("assist_info", assistMap(assistGroup, packageProduct.getPrice().intValue() - 5));
            boolean supportAssist = bookSupportAssist(chineseBook);
            map.put("is_discount", supportAssist);
            map.put("purchased", payInfoMap.containsKey(chineseBook.getId()) && payInfoMap.containsKey(englishBook.getId()));
        }
        map.put("show_assist_member_entry", true);
        return map;
    }

    Map<String, Object> bookMap(NewBookProfile bookProfile, OrderProduct orderProduct, Map<String, PicListenBookPayInfo> payInfoMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("img_url", imgUrl(bookProfile));
        map.put("book_id", bookProfile.getId());
        map.put("name", bookProfile.getName());
        map.put("book_subject", bookProfile.getSubjectId() != null ? Subject.fromSubjectId(bookProfile.getSubjectId()).name() : "");
        boolean purchased;
        if (orderProduct != null) {
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isNotEmpty(orderProductItems) && orderProductItems.size() == 1) {
                map.put("period", orderProductItems.get(0).getPeriod());
            }
            purchased = payInfoMap.containsKey(bookProfile.getId());
//            if (context.loginUserIsParent() && !purchased) {
//                Long shareRecord = picListenShareSendCouponCacheManager.load(context.getLoginUser().getId(), orderProduct.getId());
//                if (shareRecord != 0L) {
//                    Date date = new Date(shareRecord);
//                    map.put("has_coupon", Boolean.TRUE);
//                    map.put("count_down", (date.getTime() / 1000) + 86400 - (System.currentTimeMillis() / 1000));
//                } else {
//                    map.put("has_coupon", Boolean.FALSE);
//                }
//            } else {
//                map.put("has_coupon", Boolean.FALSE);
//            }
            map.put("purchased", purchased);
            map.put("product_id", orderProduct.getId());
            map.put("original_price", orderProduct.getOriginalPrice());
            map.put("price", orderProduct.getPrice());
        }
        boolean supportAssist = orderProduct != null && bookSupportAssist(bookProfile);
        map.put("is_discount", supportAssist);
//        boolean noShowEntry = purchased && assistGroup == null;
//        map.put("show_assist_member_entry", supportAssist && !noShowEntry);
//        if (!supportAssist)
//            return map;
//        Map<String, Object> assistMap = assistMap(assistGroup, orderProduct.getPrice().intValue() - 5);
//        map.put("assist_info", assistMap);
        return map;
    }

    private Map<String, Object> assistMap(AssistGroup assistGroup, Integer assistPrice) {
        Map<String, Object> assistInfo = new HashMap<>();
        assistInfo.put("assist_price", assistPrice);
        assistInfo.put("assist_status", assistGroupStatus(assistGroup));
        assistInfo.put("assist_id", assistGroup == null || assistGroup.safeIsExpire() ? "" : assistGroup.getId());
        return assistInfo;
    }

    private boolean bookSupportAssist(NewBookProfile bookProfile) {
        return PiclistenKillNamiActivity.activePublisherSet.contains(bookProfile.getShortPublisher());
    }

    private String imgUrl(NewBookProfile bookProfile) {
        return PiclistenBookImgUtils.getCompressBookImg(bookProfile.getImgUrl());
    }

    private Map<String, OrderProduct> loadBookOrderProducts(Collection<String> bookIds) {
        Map<String, List<OrderProduct>> orderProductByAppItemIds = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);
        Map<String, OrderProduct> map = new HashMap<>();
        for (Map.Entry<String, List<OrderProduct>> entry : orderProductByAppItemIds.entrySet()) {
            List<OrderProduct> orderProductList = entry.getValue();
            String bookId = entry.getKey();
            OrderProduct product = orderProductList.stream().filter(t -> !isPackage(t) && OrderProductServiceType.safeParse(t.getProductType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
            if (product != null) {
                map.put(bookId, product);
            }
        }
        return map;
    }


    private List<OrderProduct> getRenjiaoPackageProduct(Collection<NewBookProfile> books, Map<String, PicListenBookPayInfo> buyInfoMap) {
        if (CollectionUtils.isEmpty(books))
            return null;
        boolean partBuyed = books.stream().anyMatch(t -> buyInfoMap.containsKey(t.getId()));
        if (partBuyed) {
            return null;
        }
        if (books.stream().anyMatch(t -> !t.getShortPublisher().equals("人教版"))) {
            return null;
        }
        List<OrderProduct> packageList = new ArrayList<>();
        List<String> bookIdList = books.stream().map(NewBookProfile::getId).collect(Collectors.toList());
        Map<String, List<OrderProduct>> itemId2ProductListMap = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIdList);
        for (Map.Entry<String, List<OrderProduct>> entry : itemId2ProductListMap.entrySet()) {
            List<OrderProduct> orderProductList = entry.getValue();
            List<OrderProduct> packageProducts = orderProductList.stream().filter(t -> isPackage(t) && OrderProductServiceType.safeParse(t.getProductType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(packageProducts))
                continue;
            for (OrderProduct packageProduct : packageProducts) {
                List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(packageProduct.getId());
                Set<String> packageBookIdSet = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
                if (packageBookIdSet.containsAll(bookIdList) && packageBookIdSet.stream().noneMatch(e -> buyInfoMap.keySet().contains(e)))
                    packageList.add(packageProduct);
            }
        }

        return packageList;
    }

    private Boolean isPackage(OrderProduct orderProduct) {
        return orderProduct != null && orderProduct.fetchAttribute("piclisten_package_id") != null;
    }


    private NewBookProfile recBook(StudentDetail studentDetail, Subject subject, String sys, Boolean parentAuth, Integer clazzLevel) {

        List<NewClazzBookRef> clazzBookRefList = studentDetail == null ? Collections.emptyList() : loadStudentHomewordBookRefList(studentDetail.getId(), subject);
        List<NewClazzBookRef> englishBookRefs = clazzBookRefList.stream().filter(t -> subject.name().equals(t.getSubject())).collect(Collectors.toList());
        NewClazzBookRef lastEnglishBookRef = getLastUseOne(englishBookRefs);
        if (lastEnglishBookRef != null) {
            String bookId = lastEnglishBookRef.getBookId();
            Boolean picListenShow = textBookManagementLoader.picListenShow(bookId, sys, parentAuth);
            if (picListenShow) {
                return newContentLoaderClient.loadBook(bookId);
            }
        }
        List<TextBookManagement> textBookList = textBookManagementLoader.getTextBookManagementByClazzLevel(clazzLevel);
        String defaultBookId = textBookList.stream()
                .filter(e -> StringUtils.equals("外研版", e.getShortPublisherName()))
                .map(TextBookManagement::getBookId)
                .findFirst().orElse(null);
//        String bookId = defaultBookId(clazzLevel, subject);
        if (defaultBookId != null) {
            return newContentLoaderClient.loadBook(defaultBookId);
        }
        return null;
    }

    private NewClazzBookRef getLastUseOne(List<NewClazzBookRef> newClazzBookRefs) {
//        Set<String> bookIds = newClazzBookRefs.stream().map(NewClazzBookRef::getBookId).collect(Collectors.toSet());
//        if (CollectionUtils.isNotEmpty(bookIds)) {
//            Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIds);
//            Set<String> correctbookIds = bookProfileMap.values().stream().filter(e -> !StringUtils.equals("人教版", e.getShortPublisher())).map(NewBookProfile::getId).collect(Collectors.toSet());
//            newClazzBookRefs = newClazzBookRefs.stream().filter(e -> correctbookIds.contains(e.getBookId())).collect(Collectors.toList());
//        } else {
//            return null;
//        }
        return newClazzBookRefs.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime())).findFirst().orElse(null);
    }

    private List<NewClazzBookRef> loadStudentHomewordBookRefList(Long studentId, Subject... subjects) {
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
        List<Subject> subjectsList = Arrays.asList(subjects);
        List<Long> groupIds = groupMappers.stream()
                .filter(t -> subjectsList.contains(t.getSubject()))
                .map(GroupMapper::getId).collect(Collectors.toList());
        return newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
    }

    private Integer getClazzLevelForRecBook(StudentDetail studentDetail) {
        if (studentDetail == null) {
            return 3;
        }
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()))
            return 6;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null) {
            ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentDetail.getId());
            if (channelCUserAttribute != null && channelCUserAttribute.getClazzJie() != null) {
                ChannelCUserAttribute.ClazzCLevel clazzCLevelByClazzJie = ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie());
                if (clazzCLevelByClazzJie != null) {
                    if (clazzCLevelByClazzJie == ChannelCUserAttribute.ClazzCLevel.JUNIOR_GRADE) {
                        return 6;
                    }
                    if(clazzCLevelByClazzJie == ChannelCUserAttribute.ClazzCLevel.PRESCHOOL_GRADE){
                        return 1;
                    }
                    return clazzCLevelByClazzJie.getLevel();
                }
            }
            return 3;
        }
        return studentDetail.getClazzLevelAsInteger();
    }

    public String defaultBookId(Integer clazzLevel, Subject subject) {
        if (clazzLevel == null)
            clazzLevel = 3;
        if (subject == Subject.ENGLISH) {
            switch (clazzLevel) {
                case 0:
                case 1:
                    return "BK_10300001722068";
                case 2:
                    return "BK_10300001724304";
                case 3:
                    return "BK_10300000265057";
                case 4:
                    return "BK_10300000266810";
                case 5:
                    return "BK_10300000263225";
                case 6:
                case 7:
                    return "BK_10300000262593";
                default:
                    return null;
            }
        }
        if (subject == Subject.CHINESE) {
            switch (clazzLevel) {
                case 0:
                case 1:
                    return "BK_10100002551703";
                case 2:
                    return "BK_10100000004683";
                case 3:
                    return "BK_10100000013407";
                case 4:
                    return "BK_10100000005225";
                case 5:
                    return "BK_10100000007851";
                case 6:
                case 7:
                    return "BK_10100000011387";
                default:
                    return null;
            }
        }
        return null;
    }
}
