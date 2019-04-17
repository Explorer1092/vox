package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.Assertions;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.PicListenFunction;
import com.voxlearning.utopia.mapper.PicListenShelfBookMapper;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramBookService;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.impl.helper.ParentSelfStudyPublicHelper;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.support.PiclistenBookImgUtils;
import com.voxlearning.utopia.service.question.consumer.PicListenLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.PicListenBook;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.WalkerMan;

/**
 * @author RA
 */

@Named
@ExposeServices({
        @ExposeService(interfaceClass = MiniProgramBookService.class, version = @ServiceVersion(version = "20180521")),
        @ExposeService(interfaceClass = MiniProgramBookService.class, version = @ServiceVersion(version = "20190301")),
})
@Slf4j
public class MiniProgramBookServiceImpl implements MiniProgramBookService {


    @Inject
    private ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;


    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;


    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @ImportService(interfaceClass = PicListenCommonService.class)
    private PicListenCommonService picListenCommonService;

    @Inject
    private PicListenLoaderClient picListenLoaderClient;

    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;


    @Override
    public MapMessage classLevelTerm(Long uid) {
        MapMessage mm = MapMessage.successMessage();

        StudentDetail student = studentLoaderClient.loadStudentDetail(uid);
        Integer clazzLevel;
        if (student == null)
            clazzLevel = 3;
        else {
            if (student.getClazz() == null
                    || student.getClazzLevelAsInteger() == null
                    || student.isJuniorStudent()
                    || student.isInfantStudent()
                    || student.getClazz().isTerminalClazz()) {
                clazzLevel = 3;

            } else {
                clazzLevel = student.getClazzLevelAsInteger();
            }
        }

        List<TextBookMapper> textBookMapperList = textBookManagementLoader.getPublisherList();
        if (CollectionUtils.isEmpty(textBookMapperList))
            return MapMessage.successMessage().add("clazz_level_list", new ArrayList<>());


        Map<String, Object[]> shortNameMapper = new HashMap<>();
        textBookMapperList.forEach(x -> {
            Object[] pubInfo = new Object[2];
            pubInfo[0] = x.getRank() == null ? 100 : x.getRank();
            pubInfo[1] = x.getPublisherName();
            shortNameMapper.put(x.getPublisherShortName(), pubInfo);
        });


        Map<Integer, Set<String>> clazzsMapper = new HashMap<>();
        textBookMapperList.forEach(x -> {

            Set<Integer> clazzs = x.getClazzAndTerms().stream().map(TextBookMapper.ClazzAndTerm::getClazzLevel).collect(Collectors.toSet());
            clazzs.forEach(y -> {
                clazzsMapper.computeIfAbsent(y, k -> new HashSet<>());

                clazzsMapper.get(y).add(x.getPublisherShortName());

            });


        });

        List<Map<String, Object>> clazzLevelMapList = new ArrayList<>();
        clazzsMapper.forEach((k, v) -> {
            Map<String, Object> tmp = new HashMap<>();
            tmp.put("clazz_level", k);
            tmp.put("name", ClazzLevel.getDescription(k));

            List<Map<String, Object>> mapList = new ArrayList<>();
            v.forEach(x -> {
                Map<String, Object> pub = new HashMap<>();
                pub.put("publisher_id", x);
                Object[] pubInfo = shortNameMapper.get(x);
                pub.put("publisher_name", pubInfo[1]);
                pub.put("rank", pubInfo[0]);
                mapList.add(pub);
            });

            mapList.sort(Comparator.comparing(
                    m -> Integer.valueOf(m.get("rank").toString()),
                    Comparator.nullsLast(Comparator.naturalOrder()))
            );


            tmp.put("publisher_list", mapList);
            clazzLevelMapList.add(tmp);
        });


        return mm.add("clazz_level_list", clazzLevelMapList).add("student_clazz_level", clazzLevel == null ? 0 : clazzLevel).add("current_term", Term.ofMonth(MonthRange.current().getMonth()).getKey());

    }


    @Override
    public MapMessage bookList(Long uid, Long pid, Integer clazzLevel, String publishId, String sys, String cdnBaseUrl) {

        if (clazzLevel == null) {
            return MapMessage.errorMessage("请选择班级");
        }

        MapMessage mm = MapMessage.successMessage();

        ClazzLevel level = ClazzLevel.parse(clazzLevel);

        List<TextBookManagement> allTextBookList = textBookManagementLoader.getTextBookManagementByClazzLevel(level.getLevel());

        // Exclude any special publishers
        if ("mini_program".equals(sys)) {
            List<String> excludePubs = parentSelfStudyPublicHelper.getExcludePublishers();
            allTextBookList = allTextBookList.stream().filter(book -> !excludePubs.contains(book.getShortPublisherName())).collect(Collectors.toList());
        }

        //Filter
        if (StringUtils.isNotBlank(publishId)) {
            allTextBookList = allTextBookList.stream().filter(book -> publishId.equalsIgnoreCase(book.getShortPublisherName())).collect(Collectors.toList());
        }


        List<String> allBookId = allTextBookList.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());


        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookId);

        List<NewBookProfile> newBookProfileList = new ArrayList<>(bookProfileMap.values());

//        Boolean finalParentIsAuth = parentSelfStudyPublicHelper.isParentAuth(pid);

        List<String> addedBookIds = Collections.emptyList();
        if (pid != null && pid > 0) {
            List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(pid);
            addedBookIds = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toList());
        }

        Map<String, DayRange> picListenBuyLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(pid, false);
        Map<String, DayRange> walkManBuyLastDayMap = picListenCommonService.parentBuyWalkManLastDayMap(pid, false);
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        for (NewBookProfile bookProfile : newBookProfileList) {

            Map<String, DayRange> buyLastDayMap = picListenBuyLastDayMap;
            Boolean bookNeedPay = textBookManagementLoader.picListenBookNeedPay(bookProfile);
            Boolean picListenSupport = textBookManagementLoader.picListenShow(bookProfile.getId(), sys);
            if (!picListenSupport) {
                buyLastDayMap = walkManBuyLastDayMap;
                bookNeedPay = textBookManagementLoader.walkManNeedPay(bookProfile.getId());
            }

            DayRange lastDayRange = buyLastDayMap.get(bookProfile.getId());

            Boolean isPurchased = buyLastDayMap.get(bookProfile.getId()) != null;

            Map<String, Object> bookMap = convert2BookMap(bookProfile, bookNeedPay, isPurchased, lastDayRange);
            bookMap.put("already_added", addedBookIds.contains(bookProfile.getId()));
            bookMapList.add(bookMap);
        }

        // Recommend book
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(uid);
        List<Map<String, Object>> recommendBookList = new ArrayList<>();
        if (StringUtils.isBlank(publishId) && studentDetail != null) {
            recommendBookList = parentSelfStudyPublicHelper.recommendPicListenBook(studentDetail, pid, sys, cdnBaseUrl);
        }
        mm.add("clazz_level", level.getLevel());
        mm.add("recommend_book_list", recommendBookList);
        mm.add("book_list", bookMapList);


        return mm;
    }


    @Override
    public MapMessage bookSelf(Long pid, String sys, String cdnBaseUrl) {

        Assertions.notNull(pid, "parent id must not be null");
        List<String> allBookIdList = new ArrayList<>();

        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(pid);
        if (CollectionUtils.isEmpty(picListenBookShelfs)) { //当用户第一次进入书架一本书都没有的时候,初始化一些书放入书架
            Long allBookCount = parentSelfStudyService.parentPicListenBookShelfCountIncludeDisabled(pid);
            if (allBookCount == 0) { //说明用户第一次进入书架一本书都没有
                Set<String> defaultBookIdSet = parentSelfStudyPublicHelper.picListenDefaultShelfBooks(pid, sys, "");
                parentSelfStudyService.initParentPicListenBookShelfBooks(pid, defaultBookIdSet);
                allBookIdList.addAll(defaultBookIdSet);
            }
        } else
            allBookIdList = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allBookIdList)) {
            return MapMessage.successMessage().add("book_list", new ArrayList<>());
        }

        Map<String, PicListenBookShelf> shelfBookMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(picListenBookShelfs)) {
            for (PicListenBookShelf picListenBookShelf : picListenBookShelfs) {
                shelfBookMap.put(picListenBookShelf.getBookId(), picListenBookShelf);
            }
        }

        Map<String, DayRange> picListenBuyLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(pid, false);
        Map<String, DayRange> walkManBuyLastDayMap = picListenCommonService.parentBuyWalkManLastDayMap(pid, false);

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookIdList);

        List<String> excludePubs = parentSelfStudyPublicHelper.getExcludePublishers();
        List<PicListenShelfBookMapper> mapperList = new ArrayList<>();
        for (String bookId : allBookIdList) {
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null) {
                continue;
            }

            if (!textBookManagementLoader.picListenShow(newBookProfile.getId(), sys) && !textBookManagementLoader.walkManBookShow(newBookProfile.getId(), sys)) {
                continue;
            }


            // Exclude any publishers book
            if (excludePubs.contains(newBookProfile.getShortPublisher())) {
                continue;
            }

            Map<String, DayRange> buyLastDayMap = picListenBuyLastDayMap;
            Boolean bookNeedPay = textBookManagementLoader.picListenBookNeedPay(newBookProfile);
            Boolean picListenSupport = textBookManagementLoader.picListenShow(newBookProfile.getId(), sys);
            if (!picListenSupport) {
                buyLastDayMap = walkManBuyLastDayMap;
                bookNeedPay = textBookManagementLoader.walkManNeedPay(newBookProfile.getId());
            }

            PicListenBookShelf picListenBookShelf = shelfBookMap.get(bookId);
            Boolean isPayed = buyLastDayMap.get(bookId) != null;
            Date createTime = picListenBookShelf == null ? new Date() : picListenBookShelf.getCreateTime();
            mapperList.add(new PicListenShelfBookMapper(bookId, bookNeedPay, isPayed, createTime));
        }
        mapperList.sort(new PicListenShelfBookMapper.ShelfBookComparator(""));


        List<Map<String, Object>> bookMapList = new ArrayList<>();
        for (PicListenShelfBookMapper mapper : mapperList) {
            NewBookProfile newBookProfile = bookProfileMap.get(mapper.getBookId());
            Map<String, DayRange> buyLastDayMap = picListenBuyLastDayMap;
            Boolean picListenSupport = textBookManagementLoader.picListenShow(newBookProfile.getId(), sys);
            if (!picListenSupport) {
                buyLastDayMap = walkManBuyLastDayMap;
            }
            bookMapList.add(convert2BookMap(newBookProfile, mapper.getBookNeedPay(), mapper.getIsPayed(), buyLastDayMap.get(mapper.getBookId())));
        }

        return MapMessage.successMessage().add("book_list", bookMapList);

    }


    @Override
    public MapMessage bookDetail(Long uid, Long pid, String bookId, String sys, String cdnBaseUrl) {
        return bookDetail(uid, pid, bookId, sys, cdnBaseUrl, "");
    }

    @Override
    public MapMessage bookDetail(Long uid, Long pid, String bookId, String sys, String cdnBaseUrl, String ver) {

        if (blank(bookId)) {
            return MapMessage.errorMessage("教材不存在");
        }

        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null)
            return MapMessage.errorMessage("教材不存在");

        StudentDetail student = studentLoaderClient.loadStudentDetail(uid);
        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(pid);
        Set<String> alreadyAddedBookIdSet = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toSet());


        Map<String, DayRange> picListenBuyLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(pid, false);
        Map<String, DayRange> walkManBuyLastDayMap = picListenCommonService.parentBuyWalkManLastDayMap(pid, false);

        //教材状态 已购买  免费  为购买
        Boolean picListenBookNeedPay = textBookManagementLoader.picListenBookNeedPay(newBookProfile.getId());
        Boolean walkManNeedPay = textBookManagementLoader.walkManNeedPay(newBookProfile.getId());


        DayRange walkManLastDayRange = walkManBuyLastDayMap.get(bookId);


        Map<String, DayRange> buyLastDayMap = picListenBuyLastDayMap;
        Boolean bookNeedPay = picListenBookNeedPay;
        Boolean picListenSupport = textBookManagementLoader.picListenShow(newBookProfile.getId(), sys);
        if (!picListenSupport) {
            buyLastDayMap = walkManBuyLastDayMap;
            bookNeedPay = walkManNeedPay;
        }

        DayRange lastDayRange = buyLastDayMap.get(newBookProfile.getId());

        Boolean isPurchased = buyLastDayMap.get(newBookProfile.getId()) != null;


        Map<String, Object> bookMap = convert2BookMap(newBookProfile, bookNeedPay, isPurchased, lastDayRange);

        bookMap.put("already_added", alreadyAddedBookIdSet.contains(bookId));

        Boolean isParentAuth = parentSelfStudyPublicHelper.isParentAuth(pid);

        Map<String, Object> purchaseStatusMap = purchaseStatus(bookNeedPay, lastDayRange, bookId, walkManNeedPay, walkManLastDayRange, isParentAuth, sys, uid);
        bookMap.putAll(purchaseStatusMap);

        bookMap.put("piclisten_sdk", piclistenSdkInfo(bookId, student, pid, ver));

        return MapMessage.successMessage().add("book_detail", bookMap);
    }


    @Override
    public MapMessage productInfo(Long uid, Long pid, String bookId, String cdnBaseUrl, String cdnBaseUrlAvatar) {

        Assertions.notNull(pid, "parent id must not be null");

        if (blank(bookId)) {
            return MapMessage.errorMessage("教材不存在");
        }

        List<OrderProduct> products = getProductsToDisplay(bookId);
        if (CollectionUtils.isEmpty(products)) {
            return MapMessage.errorMessage("未查询到产品信息");
        }
        List<Map<String, Object>> ret = generateProductInfo(products, pid, cdnBaseUrl);

        MapMessage message = MapMessage.successMessage().add("products", ret);


        //如果有bookId，返回的时候带上学科，供前端根据不同的学科选择默认选中的点读机教材
        Subject subject = getBookSubject(bookId);
        if (null != subject) {
            message.add("subject", subject.name());
        }
        return message;

    }


    @Override
    public MapMessage recommend(Long uid, Long pid, String productIds, String cdnBaseUrl) {

        Assertions.notNull(uid, "parent id must not be null");

        if (blank(productIds)) {
            return MapMessage.errorMessage("产品不存在");
        }

        if (0 == uid) {
            return MapMessage.errorMessage("无效的学生id");
        }
        List<String> productIdList = Arrays.asList(productIds.trim().split(","));
        Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(productIdList);
        if (MapUtils.isEmpty(orderProductMap)) {
            return MapMessage.errorMessage("未知的产品");
        }
        Set<Long> userIdsToRecommend = new HashSet<>();
        for (OrderProduct product : orderProductMap.values()) {
            if (gonnaCreateOrderForParent(OrderProductServiceType.safeParse(product.getProductType()))) {
                userIdsToRecommend.add(pid);
            } else {
                userIdsToRecommend.add(uid);
            }
        }

        final Set<OrderProductServiceType> productTypes = orderProductMap.values().stream().map(o -> OrderProductServiceType.safeParse(o.getProductType())).collect(Collectors.toSet());
        List<OrderProduct> orderProducts = userOrderLoaderClient.loadAllOrderProductIncludeOffline();
        if (CollectionUtils.isEmpty(orderProducts)) {
            return MapMessage.successMessage();
        }
        List<String> productIdsOnline = orderProducts.stream()
                .filter(p -> productTypes.contains(OrderProductServiceType.safeParse(p.getProductType())))
                .filter(p -> OrderProductSalesType.TIME_BASED == p.getSalesType())
                .filter(p -> "ONLINE".equals(p.getStatus()))
                .map(OrderProduct::getId).collect(Collectors.toList());
        Map<String, List<OrderProductItem>> productItemsMap = userOrderLoaderClient.loadProductItemsByProductIds(productIdsOnline);
        if (MapUtils.isEmpty(productItemsMap)) {
            return MapMessage.successMessage();
        }
        //打包产品不做推荐
        productIdList = productIdList.stream().filter(id -> productItemsMap.containsKey(id) && productItemsMap.get(id).size() == 1).collect(Collectors.toList());
        if (productIdList.size() == 0) {
            return MapMessage.successMessage();
        }
        //查出当前产品有关联的产品
        Set<String> productIdsRelated = getReleatedProductList(productIdList, productItemsMap);
        //在关联产品中查出需要推荐的产品和不需要推荐的产品
        Set<String> productIdsToRecommend = getRecommendProductList(productIdList, userIdsToRecommend, productItemsMap, productIdsRelated);

        Set<UserOrder> userOrders = new HashSet<>();
        for (Long userId : userIdsToRecommend) {
            List<UserOrder> orders = userOrderLoaderClient.loadUserOrderList(userId);
            if (CollectionUtils.isNotEmpty(orders)) {
                userOrders.addAll(orders);
            }
        }
        Set<String> productIdsAlreadyBuy = userOrders.stream().map(UserOrder::getProductId).collect(Collectors.toSet());
        List<Map<String, Object>> ret = new ArrayList<>();
        for (String productId : productIdsRelated) {
            //如果产品不被推荐，并且用户没有购买过这个产品，则过滤掉
            if (!productIdsToRecommend.contains(productId) && !productIdsAlreadyBuy.contains(productId)) {
                continue;
            }
            OrderProduct product = orderProducts.stream().filter(p -> productId.equals(p.getId())).findFirst().orElse(null);
            if (null == product) continue;

            Map<String, Object> info = new HashMap<>();
            info.put("id", product.getId());
            info.put("name", product.getName());
            info.put("desc", product.getDesc());
            info.put("price", product.getPrice());
            info.put("originalPrice", product.getOriginalPrice());
            info.put("original_price", product.getOriginalPrice());
            info.put("recommend", productIdsToRecommend.contains(productId));
            info.put("type", parseProductType(OrderProductServiceType.safeParse(product.getProductType())));
            List<OrderProductItem> productItems = productItemsMap.get(productId);
            List<Map<String, Object>> items = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(productItems)) {
                for (OrderProductItem item : productItems) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("id", item.getId());
                    itemInfo.put("name", item.getName());
                    itemInfo.put("period", item.getPeriod());
                    if (OrderProductServiceType.safeParse(item.getProductType()) == PicListenBook && StringUtils.isNotBlank(item.getAppItemId())) {
                        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(item.getAppItemId());
                        itemInfo.put("bookName", newBookProfile.getName());
                        itemInfo.put("book_name", newBookProfile.getName());
                        itemInfo.put("bookImgUrl", PiclistenBookImgUtils.getCompressBookImg(newBookProfile.getImgUrl()));
                        itemInfo.put("book_img_url", PiclistenBookImgUtils.getCompressBookImg(newBookProfile.getImgUrl()));
                        itemInfo.put("publisher", newBookProfile.getShortPublisher());
                        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoader.picListenSdkInfo(newBookProfile.getId());
                        if (null != sdkInfo) {
                            itemInfo.put("sdk", sdkInfo.getSdkType().name());
                        }
                    }
                    items.add(itemInfo);
                }
            }
            info.put("items", items);
            ret.add(info);
        }
        return MapMessage.successMessage().add("products", ret);

    }


    private PicListenFunction parseProductType(OrderProductServiceType type) {

        if (type == null) {
            return PicListenFunction.NONE;
        }

        switch (type) {
            case PicListenBook:
                return PicListenFunction.PIC_LISTEN;
            case WalkerMan:
                return PicListenFunction.WALK_MAN;
            case FollowRead:
                return PicListenFunction.FOLLOW_READ;
            default:
                return PicListenFunction.NONE;

        }
    }

    private Map<String, Object> piclistenSdkInfo(String bookId, StudentDetail studentDetail, Long picListenPid, String ver) {
        Map<String, Object> map = new HashMap<>();
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoader.picListenSdkInfo(bookId);
        if (picListenPid != null && picListenPid != 0) {
            map.put("piclisten_pid", picListenPid);
        }


        if (sdkInfo.getSdkType() == TextBookSdkType.renjiao) { //人教 sdk
            boolean noSdk = studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "noRenjiaoSdk", true);
            if (!noSdk) {
                addIntoMap(map, "sdk", sdkInfo.getSdkType().name());
                addIntoMap(map, "sdk_book_id", sdkInfo.getSdkBookIdV2(ver));
            } else {
                addIntoMap(map, "sdk", TextBookSdkType.none.name());
            }
        } else if (sdkInfo.getSdkType() == TextBookSdkType.hujiao) { //沪教 sdk
            boolean hujiaoSdk = studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                    "jzt", "hujiaoSdk", true);
            if (hujiaoSdk) {
                boolean noSdk = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "noHujiaoSdk", true);
                if (!noSdk) {
                    addIntoMap(map, "sdk", sdkInfo.getSdkType().name());
                    addIntoMap(map, "sdk_book_id", sdkInfo.getSdkBookIdV2(ver));
                } else {
                    addIntoMap(map, "sdk", TextBookSdkType.none.name());
                }
            } else {
                addIntoMap(map, "sdk", TextBookSdkType.none.name());
            }
        } else {
            addIntoMap(map, "sdk", sdkInfo.getSdkType().name());
            if (sdkInfo.getSdkType().hasSdk()) {
                addIntoMap(map, "sdk_book_id", sdkInfo.getSdkBookIdV2(ver));
            }
        }


        return map;
    }


    private Boolean functionSupport(PicListenFunction picListenFunction, String bookId, String sys, long pid) {

        switch (picListenFunction) {
            case PIC_LISTEN:
                return textBookManagementLoader.picListenShow(bookId, sys);
            case WALK_MAN:
                return textBookManagementLoader.walkManBookShow(bookId, sys);
            case TEXT_READ:
                return textBookManagementLoader.textReadBookShow(bookId, sys);
            case ENGLISH_WORD_LIST:
                return textBookManagementLoader.englishWordListShow(bookId);
            case CHINESE_WORD_LIST:
                return textBookManagementLoader.chineseWordListShow(bookId);
            case FOLLOW_READ:
                //65804:跟读打分功能，在有效期内用户或者教材有跟读功能的才开放，其余用户不可见
                Boolean hasBuyScore = picListenCommonService.parentHasBuyScore(pid);
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                return textBookManagementLoader.followReadBookSupport(bookId) || (hasBuyScore && StringUtils.equals("人民教育出版社", newBookProfile.getPublisher()) && newBookProfile.getSubjectId().equals(Subject.ENGLISH.getId()));
            case READING:
                return textBookManagementLoader.readingShow(bookId);
            default:
                return false;
        }
    }


    private Map<String, Object> purchaseDesc() {
        Map<String, Object> map = new HashMap<>();
        List<String> txts = new ArrayList<>();
        txts.add("同步校内课本，预习复习两手抓");
        txts.add("即点即读，朝夕间培养语感");
        txts.add("纯正发言，让孩子听得懂学的牢");
        txts.add("标准翻译，便与孩子理解");
        txts.add("学习报告即时推送，使用效果一目了然");
        map.put("txt", txts);
        map.put("url", "https://cdn.17zuoye.com/static/project/luffyimage/tabbar/ranking/group.png");
        return map;
    }


    private Map<String, Object> purchaseStatus(Boolean bookNeedPay, DayRange lastDayRange, String bookId, Boolean walkManNeedPay, DayRange walkManDayRange, boolean isParentAuth, String sys, Long uid) {
        Map<String, Object> map = new HashMap<>();
        String bookStatus;
        String expireDateStr = "";
        String walkManExpireDateStr = "";
        String walkManStatus;
        Map<String, Object> productDesc = new HashMap<>();

        if (!bookNeedPay) {
            bookStatus = "free";
        } else {
            if (lastDayRange == null || lastDayRange.getEndDate().before(new Date())) {
                bookStatus = "not_purchased";
                productDesc = purchaseDesc();
            } else {
                bookStatus = "purchased";
                expireDateStr = DateUtils.dateToString(lastDayRange.getEndDate(), DateUtils.FORMAT_SQL_DATE); //到期时间
            }
        }

        if (!walkManNeedPay) {
            walkManStatus = "free";
        } else {
            if (walkManDayRange == null || walkManDayRange.getEndDate().before(new Date())) {
                walkManStatus = "not_purchased";
                productDesc = purchaseDesc();
            } else {
                walkManStatus = "purchased";
                walkManExpireDateStr = DateUtils.dateToString(walkManDayRange.getEndDate(), DateUtils.FORMAT_SQL_DATE); //到期时间
            }
        }


        // 是否开启只对认证用户开放
        boolean picListenNeedAuth = textBookManagementLoader.picListenBookAuthOnline(bookId);

        boolean parentAuth = !picListenNeedAuth || isParentAuth;

        map.put("pic_listen_support", picListenNeedAuth ? parentAuth : functionSupport(PicListenFunction.PIC_LISTEN, bookId, sys, uid));

        map.put("walk_man_support", functionSupport(PicListenFunction.WALK_MAN, bookId, sys, uid));
        map.put("english_word_list_support", functionSupport(PicListenFunction.ENGLISH_WORD_LIST, bookId, sys, uid));
        map.put("text_read_support", functionSupport(PicListenFunction.TEXT_READ, bookId, sys, uid));
        map.put("chinese_word_list_support", functionSupport(PicListenFunction.CHINESE_WORD_LIST, bookId, sys, uid));
        map.put("follow_read_support", functionSupport(PicListenFunction.FOLLOW_READ, bookId, sys, uid));
        map.put("reading_support", functionSupport(PicListenFunction.READING, bookId, sys, uid));

        map.put("parent_auth", parentAuth);
        map.put("product_desc", productDesc);
        map.put("status", bookStatus);
        map.put("walkManStatus", walkManStatus);
        map.put("walkman_status", walkManStatus);
        map.put("expire", expireDateStr);
        map.put("walkman_expire", walkManExpireDateStr);

        boolean needPay = bookNeedPay;
        if (!bookNeedPay) {
            needPay = walkManNeedPay;
        }
        map.put("book_need_pay", needPay);

        return map;
    }


    private Set<String> getRecommendProductList(Collection<String> productIds, Collection<Long> userIdsToRecommend, Map<String, List<OrderProductItem>> productItemsMap, Set<String> productIdsRelated) {
        Set<String> activatedItemIds = new HashSet<>();
        Set<String> productIdsToRecommend = new HashSet<>();
        List<UserActivatedProduct> userActivatedProducts = new ArrayList<>();
        for (Long userId : userIdsToRecommend) {
            List<UserActivatedProduct> uaps = userOrderLoaderClient.loadUserActivatedProductList(userId);
            if (CollectionUtils.isNotEmpty(uaps)) {
                userActivatedProducts.addAll(uaps);
            }
        }
        if (CollectionUtils.isNotEmpty(userActivatedProducts)) {
            userActivatedProducts = userActivatedProducts.stream().filter(uap -> uap.getServiceEndTime().after(new Date())).collect(Collectors.toList());
            activatedItemIds = userActivatedProducts.stream().map(UserActivatedProduct::getProductItemId).collect(Collectors.toSet());
        }

        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(activatedItemIds);
        Set<String> appItemIds = new HashSet<>();
        if (MapUtils.isNotEmpty(orderProductItemMap)) {
            appItemIds = orderProductItemMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        }
        for (String pid : productIdsRelated) {
            if (productIds.contains(pid)) continue;

            List<OrderProductItem> items = productItemsMap.get(pid);
            if (CollectionUtils.isEmpty(items)) continue;
            Set<String> ids = items.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            Set<String> appIds = items.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
            boolean recommend = true;
            for (String id : ids) {
                if (activatedItemIds.contains(id)) {
                    recommend = false;  //子产品还在服务期，则产品不被推荐
                    break;
                }
            }
            if (OrderProductServiceType.safeParse(items.get(0).getProductType()) == PicListenBook && recommend) {
                //用教材id过滤一下关联产品
                for (String id : appIds) {
                    if (appItemIds.contains(id)) {
                        recommend = false;
                        break;
                    }
                }
            }
            if (recommend) {
                productIdsToRecommend.add(pid);
            }
        }
        return productIdsToRecommend;
    }

    private Set<String> getReleatedProductList(Collection<String> productIds, Map<String, List<OrderProductItem>> productItemsMap) {
        Set<String> itemIds = new HashSet<>();
        Set<String> appItemIds = new HashSet<>();
        for (String productId : productIds) {
            if (!productItemsMap.containsKey(productId)) continue;
            itemIds.addAll(productItemsMap.get(productId).stream().map(OrderProductItem::getId).collect(Collectors.toSet()));
            appItemIds.addAll(productItemsMap.get(productId).stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet()));
        }

        Set<String> productIdsRelated = new HashSet<>();
        for (String pid : productItemsMap.keySet()) {
            if (productIds.contains(pid)) continue;

            List<OrderProductItem> items = productItemsMap.get(pid);
            if (CollectionUtils.isEmpty(items)) continue;
            List<String> ids = items.stream().map(OrderProductItem::getId).collect(Collectors.toList());
            for (String id : itemIds) {
                if (ids.contains(id)) { //如果item有重叠，则认为此产品与当前产品相关
                    productIdsRelated.add(pid);
                    break;
                }
            }
            if (OrderProductServiceType.safeParse(items.get(0).getProductType()) == PicListenBook && !productIdsRelated.contains(pid)) {
                //根据教材id再去找一下关联产品
                Set<String> appIds = items.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
                for (String id : appItemIds) {
                    if (appIds.contains(id)) {
                        productIdsRelated.add(pid);
                        break;
                    }
                }
            }
        }
        return productIdsRelated;
    }


    private Subject getBookSubject(String bookId) {
        Subject subject = null;
        if (StringUtils.isNotBlank(bookId)) {
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            if (null != newBookProfile) {
                subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
            }
        }
        return subject;
    }

    private List<Map<String, Object>> generateProductInfo(List<OrderProduct> orderProducts, Long userId, String cdnBaseUrl) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(userId);

        List<Map<String, Object>> productInfo = new ArrayList<>();
        for (OrderProduct product : orderProducts) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", product.getId());
            info.put("name", product.getName());
            info.put("desc", product.getDesc());
            info.put("price", product.getPrice());
            info.put("originalPrice", product.getOriginalPrice());
            info.put("type", parseProductType(OrderProductServiceType.safeParse(product.getProductType())));

            boolean productPurchased = false;
            //补充productItem信息
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            List<Map<String, Object>> items = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                for (OrderProductItem item : orderProductItems) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("id", item.getId());
                    itemInfo.put("period", item.getPeriod());
                    itemInfo.put("name", item.getName());
                    itemInfo.put("price", item.getOriginalPrice());
                    itemInfo.put("desc", item.getDesc());
                    itemInfo.put("appItemId", item.getAppItemId());
                    if (CollectionUtils.isEmpty(userActivatedProducts)) {
                        itemInfo.put("status", 0);   //未购买此子产品
                    } else {
                        UserActivatedProduct userActivatedProduct = userActivatedProducts.stream().filter(uap -> StringUtils.isNotBlank(uap.getProductItemId()) && uap.getProductItemId().equals(item.getId())).findFirst().orElse(null);
                        if (null == userActivatedProduct) {
                            itemInfo.put("status", 0);
                        } else if (userActivatedProduct.getServiceEndTime().after(new Date())) {
                            itemInfo.put("status", 1);   //已购买未过期
                            itemInfo.put("expire", userActivatedProduct.getServiceEndTime());
                            productPurchased = true;
                        } else {
                            itemInfo.put("status", 2);   //已购买，已过期
                            itemInfo.put("expire", userActivatedProduct.getServiceEndTime());
                        }
                    }
                    if (OrderProductServiceType.safeParse(product.getProductType()) == PicListenBook || OrderProductServiceType.safeParse(product.getProductType()) == WalkerMan) {
                        //补充教材信息
                        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(item.getAppItemId());
                        if (null != newBookProfile) {
                            itemInfo.put("bookName", newBookProfile.getName());
                            itemInfo.put("bookImg", PiclistenBookImgUtils.getCompressBookImg(newBookProfile.getImgUrl()));
                        }
                    }
                    items.add(itemInfo);
                }
            }
            info.put("items", items);

            //是否买过这个产品
            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(product.getProductType(), userId);
            boolean hasPaid = false;
            if (CollectionUtils.isNotEmpty(userOrders)) {
                long count = userOrders.stream().filter(order -> order.getProductId().equals(product.getId())).count();
                hasPaid = count > 0;
            }
            info.put("hasPaid", hasPaid);
            String productStatus = "not_purchased";

            if (productPurchased) {
                productStatus = "purchased";
            }
            info.put("status", productStatus);
            productInfo.add(info);
        }
        return productInfo;
    }

    private List<OrderProduct> getProductsToDisplay(String bookId) {
        List<OrderProduct> products = findPicListenBookOrderProductByBookId(bookId);
        if (null == products) {
            return Collections.emptyList();
        }
        return products;
    }


    /**
     * 根据点读机教材id查找对应的产品配置
     *
     * @param bookId 点读机教材id
     * @return 产品配置 {@link OrderProduct}
     */
    private List<OrderProduct> findPicListenBookOrderProductByBookId(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }

        Map<String, List<OrderProduct>> orderProductMap = userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singletonList(bookId));
        if (MapUtils.isNotEmpty(orderProductMap)) {
            List<OrderProduct> products = orderProductMap.get(bookId);
            if (CollectionUtils.isNotEmpty(products)) {
                return products.stream()
                        .filter(p -> PicListenBook == OrderProductServiceType.safeParse(p.getProductType()) || WalkerMan == OrderProductServiceType.safeParse(p.getProductType()))
                        .filter(p -> {
                            //用book_id来找产品，只能找有1个item的
                            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(p.getId());
                            return CollectionUtils.isNotEmpty(orderProductItems) && orderProductItems.size() == 1;
                        }).collect(Collectors.toList());
            }
        }
        return null;
    }


    /**
     * 判断要处理的产品类型如果创建订单是否是要挂在家长身上
     */
    private boolean gonnaCreateOrderForParent(OrderProductServiceType orderProductServiceType) {
        return null != orderProductServiceType
                && (OrderProductServiceType.PicListenBook == orderProductServiceType || OrderProductServiceType.FollowRead == orderProductServiceType || OrderProductServiceType.WalkerMan == orderProductServiceType || OrderProductServiceType.ChipsEnglish == orderProductServiceType
                || OrderProductServiceType.StudyMates == orderProductServiceType
        );
    }


    private Map<String, Object> convert2BookMap(NewBookProfile bookProfile, Boolean bookNeedPay, Boolean isPurchased, DayRange lastDayRange) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, "book_id", bookProfile.getId());
        addIntoMap(map, "book_name", bookProfile.getShortName());
        addIntoMap(map, "clazz_level", bookProfile.getClazzLevel());
        addIntoMap(map, "clazz_level_name", ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, "term", Term.of(bookProfile.getTermType()).name());
        addIntoMap(map, "cover_url", PiclistenBookImgUtils.getCompressBookImg(bookProfile.getImgUrl()));
        addIntoMap(map, "subject", Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, "publisher", publisher(bookProfile.getPublisher()));
        addIntoMap(map, "short_publisher", publisher(bookProfile.getShortPublisher()));
        map.put("is_purchased", isPurchased);
        map.put("book_need_pay", bookNeedPay);

        String bookStatus = "free";
        if (bookNeedPay) {
            if (lastDayRange == null || lastDayRange.getEndDate().before(new Date())) {
                bookStatus = "not_purchased";
            } else {
                bookStatus = "purchased";
            }
        }
        map.put("status", bookStatus);
        return map;
    }

    private void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value != null) {
            dataMap.put(key, value);
        }
    }


    private String publisher(CharSequence cs) {
        if (blank(cs)) {
            return "未知";
        }
        return cs.toString();
    }

    private String clazzLevelName(TextBookMapper.ClazzAndTerm clazzAndTerm) {
        String clazzLevelName;
        ClazzLevel clazzLevel = ClazzLevel.parse(clazzAndTerm.getClazzLevel());
        if (clazzLevel == null)
            clazzLevelName = "未知年级";
        else
            clazzLevelName = clazzLevel.getDescription();

        Term term = Term.of(clazzAndTerm.getTermType());
        return clazzLevelName + "（" + term.getBrief() + "）";
    }


    private boolean blank(CharSequence src) {
        return StringUtils.isBlank(src);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class PublisherRankMapper implements Comparable, Serializable {
        private static final long serialVersionUID = 4695117057865363689L;
        private Integer rank;
        private String publisherName;

        @Override
        public int hashCode() {
            if (publisherName == null)
                return 0;
            return publisherName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof PublisherRankMapper))
                return false;
            PublisherRankMapper mapper = (PublisherRankMapper) obj;
            if (this == mapper)
                return true;
            if (this.publisherName == null || mapper.getPublisherName() == null)
                return false;
            if (this.publisherName.equals(mapper.getPublisherName()))
                return true;
            return false;
        }


        @Override
        public int compareTo(Object o) {
            if (!(o instanceof PublisherRankMapper))
                return -1;
            if (this.rank == null)
                return 1;
            PublisherRankMapper mapper = (PublisherRankMapper) o;
            return this.rank.compareTo(mapper.getRank());
        }
    }

}
