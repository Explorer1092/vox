package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.PicListenFunction;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramBookService;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.question.api.entity.PicListenResourcesPackage;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.mapper.SelfStudyAdInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;


@Controller
@RequestMapping(value = "/v3/parent/selfstudy")
@Slf4j
public class ParentSelfStudyV3Controller extends AbstractSelfStudyApiController {


    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private static final String FEED_BACK_URL = "/view/mobile/parent/send_question?dest_id=9610&qs_type=question_readingmachine&origin=点读机";


    @RequestMapping(value = "/services/status.vpage")
    @ResponseBody
    public MapMessage serviceStatus() {

        return wrapper(mm -> {
            String ret=commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue("PRIMARY_PLATFORM_PARENT", "NEW_CUSTOMER_SERVICE_STATUS");
            if (blank(ret)) {
                ret = "0";
            }
            mm.add("status", ret);
        });

    }


    @RequestMapping(value = "/clazz_publisher.vpage")
    @ResponseBody
    public MapMessage clazzLevelTermList() {
        return wrapper(mm -> {
            mm.putAll(bookService().classLevelTerm(uid()));
        });

    }


    @RequestMapping(value = "/book/list.vpage")
    @ResponseBody
    public MapMessage bookList() {

        Long pid = pid();
        Long uid = uid();

        int clazzLevel = getRequestInt("clazz_level");
        String publishId = getRequestString("publisher_id");

        if (clazzLevel <= 0) {
            clazzLevel = 3; // Default
            if (uid != null && uid > 0) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(uid);
                if (studentDetail != null) {
                    if (getClazzLevelcBook(studentDetail) > 0) {
                        int tmp = getClazzLevelcBook(studentDetail);
                        // Only match 1~13
                        if(!studentDetail.isInfantStudent()){
                            clazzLevel=tmp;
                        }
                    }
                }
            }
        }

        int clazz = clazzLevel;
        String sys = sys();

        return wrapper((mm) -> {
            MapMessage map = bookService().bookList(uid, pid, clazz, publishId, sys, getCdnBaseUrlStaticSharedWithSep());
            List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("book_list");
            Map<Object, List<Map<String, Object>>> groupList = list.stream().collect(Collectors.groupingBy(x -> x.get("publisher")));
            map.put("book_list", sortedBookList(groupList));
            mm.putAll(map);
        });
    }


    private List sortedBookList(Map<Object, List<Map<String, Object>>> groupList) {


        List list = new ArrayList();
        // Sort by publisher
        List<TextBookMapper> textBookMapperList = textBookManagementLoaderClient.getPublisherList();

        if (CollectionUtils.isNotEmpty(textBookMapperList)) {
            Map<Integer, String> pubMapper = new TreeMap<>();
            textBookMapperList.forEach(x -> {

                if (groupList.get(x.getPublisherName()) == null) {
                    return;
                }
                pubMapper.put(x.getRank() == null ? 100 : x.getRank(), x.getPublisherName());

            });

            pubMapper.values().forEach(x -> {
                Map<Object, Object> mapList = new LinkedHashMap<>();
                mapList.put("publisher", x);
                mapList.put("books", groupList.get(x));
                list.add(mapList);
            });

        } else {
            groupList.forEach((x, y) -> {
                Map<Object, Object> mapList = new LinkedHashMap<>();
                mapList.put("publisher", x);
                mapList.put("books", y);
                list.add(mapList);
            });
        }

        return list;
    }


    //~ Must need login follow

    /**
     * 书架教材列表
     *
     * @return
     */
    @RequestMapping(value = "/book_shelf.vpage")
    @ResponseBody
    public MapMessage bookShelf() {
        String sys = sys();
        Long pid = pid();

        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }

        return wrapper((mm) -> {
            mm.putAll(bookService().bookSelf(pid, sys, getCdnBaseUrlStaticSharedWithSep()));
            // Copy from v1
            mm.add(RES_BANNERS, picListenAdMapList(null, SelfStudyAdPosition.PIC_LISTEN_BOOK_SHELF_BANNER))
                    .add(RES_PURCHASE_TEXT, "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。")
                    .add(RES_FEEDBACK_URL, fetchMainsiteUrlByCurrentSchema() + FEED_BACK_URL). add(RES_EXTRA_ENTRY, extraEntry(pid));



        });

    }

    @RequestMapping(value = "/book_shelf/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addBook2Shelf() {

        Long pid = pid();
        String bookId = getRequestString("book_id");

        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }

        if (blank(bookId)) {
            return failMessage("请选择一本教材!");
        }

        return wrapper((mm) -> {
            MapMessage ret = AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("addBook").keys(pid)
                    .proxy().addBook2PicListenShelf(pid, bookId);
            mm.putAll(ret);
        });
    }

    @RequestMapping(value = "/book_shelf/delete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteBook2Shelf() {
        Long pid = pid();
        String bookId = getRequestString("book_ids");

        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }


        if (blank(bookId)) {
            return MapMessage.errorMessage("请选择一本教材!");
        }


        List<String> bookIds = Arrays.asList(bookId.split(","));

        return wrapper((mm) -> {
            MapMessage ret = AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("deleteBook").keys(pid)
                    .proxy().deleteBooksFromPicListenShelf(pid, bookIds);
            mm.putAll(ret);
        });

    }


    @RequestMapping(value = "/book/product_info.vpage")
    @ResponseBody
    public MapMessage productInfo() {
        Long pid = pid();
        Long uid = uid();
        String bookId = getRequestString("book_id");


        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }


        return wrapper((mm) -> {
            mm.putAll(bookService().productInfo(uid, pid, bookId, getCdnBaseUrlStaticSharedWithSep(), getCdnBaseUrlAvatarWithSep()));
        });

    }


    @RequestMapping(value = "/book/recommend.vpage")
    @ResponseBody
    public MapMessage recommend() {
        Long pid = pid();
        Long uid = uid();
        String productIds = getRequestString("product_ids");


        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }

        if (uid <= 0) {
            return successMessage().add("products", Collections.emptyList());
        }

        return wrapper((mm) -> {
            mm.putAll(bookService().recommend(uid, pid, productIds, getCdnBaseUrlStaticSharedWithSep()));
        });

    }


    @RequestMapping(value = "/book/createorder.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage createOrder() {
        Long parentId = pid();

        // Check login
        if (parentId <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }

        String productId = getRequestString("product_id");
        String refer = getRequestString("refer");

        if (StringUtils.isBlank(productId)) {
            return failMessage("参数错误");
        }

        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (product == null || (OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.PicListenBook
                && OrderProductServiceType.safeParse(product.getProductType()) != OrderProductServiceType.WalkerMan)) {
            return failMessage("不支持的产品类型");
        }

        if (!availableForRenew(product, parentId)) {
            return failMessage("下单失败，当前产品（或当前产品的部分内容）仍在服务期，不支持续费");
        }

        UserOrder order = UserOrder.newOrder(OrderType.pic_listen, parentId);
        order.setUserId(parentId);
        order.setProductAttributes(product.getAttributes());
        order.setOrderPrice(product.getPrice());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setOrderProductServiceType(product.getProductType());
        order.setOrderReferer(refer);
        order.setUserReferer(SafeConverter.toString(parentId));

        if (OrderProductServiceType.PicListenBook == OrderProductServiceType.safeParse(product.getProductType())) {
            OrderSynchronizeContext context = generateAttrForPicListenBook(product);
            order.setExtAttributes(JsonUtils.toJson(context));
        }
        MapMessage message = userOrderServiceClient.saveUserOrder(order);
        if (message.isSuccess()) {
            String orderId = order.genUserOrderId();
            return successMessage().add("pay_url", genPayMentGateWay(orderId)).add("orderId", orderId);

        } else {
            return failMessage("生成订单失败");
        }

    }


    //~ 这个接口历史遗留，由于客户端原因，原有逻辑不变，新增加字段　>_<

    @RequestMapping(value = "/book/detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bookDetail() {
        Long pid = pid();
        Long uid = uid();

        // Check login
        if (pid <= 0) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "请先登录");
        }


        String bookId = getRequestString(REQ_BOOK_ID);
        String version = getRequestString(REQ_APP_NATIVE_VERSION);

        TextBookManagement textBookManagement = textBookManagementLoaderClient.getTextBook(bookId);
        if (textBookManagement == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        //如果穿进来的教材id是纳米盒教材id,并且该教材有对应的自制教材,用自制教材;

        NewBookProfile bookProfile = newContentLoaderClient.loadBook(bookId);
        if (bookProfile == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        MapMessage successMessage = successMessage();
        successMessage.add(RES_BOOK_NAME, bookProfile.getShortName());

        User user = new User();
        user.setId(pid);
        String sys = getRequestString(REQ_SYS);


        Map<String, Object> appendMap = new HashMap<>();
        Map<PicListenFunction, Object> productAppendMap = new HashMap<>();

        // Reduce data
        MapMessage detailResult = miniProgramBookServiceClient.getRemoteReference().bookDetail(uid, pid, bookId, sys, getCdnBaseUrlStaticSharedWithSep());
        MapMessage productResult = miniProgramBookServiceClient.getRemoteReference().productInfo(uid, pid, bookId, getCdnBaseUrlStaticSharedWithSep(), getCdnBaseUrlAvatarWithSep());
        // Append ew filed
        String[] fields = {"is_purchased", "book_need_pay","status","walkman_status",
                "expire","walkman_expire","already_added", "publisher", "short_publisher",
                "product_desc","parent_auth"};
        appendDetailFields(appendMap, detailResult, fields);
        appendProductFields(productAppendMap, productResult);

        successMessage.add("detail", appendMap);

        Map<String, Object> headMap = generateBookDetailHead(appendMap,productAppendMap,bookProfile, sys, user, version);

        List<Map<String, Object>> bodyListMap = generateBookDetailBody(appendMap,productAppendMap,bookProfile, sys, user, version);

        Map<String, Object> footMap = new LinkedHashMap<>();
//        footMap.put(RES_USER_IMG_URL, "");// TODO: 2017/4/17 底图url 底部图暂时没有

        long studentId = uid;
        StudentDetail studentDetail = null;
        if (studentId != 0L) {
            studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            /**
             *  磨耳朵活动期间，如果通过推荐教材第一次判定该学生是外研版教材，则进入此页面（接口）即算该用户今天完成不考虑读时长
             */
            if (GrindEarActivity.isInActivityPeriod()) {
                Boolean isWaiyan = false;
                String key = grindEarService.waiyanKey(studentId);
                CacheObject<Object> cacheObject = VendorCache.getVendorPersistenceCache().get(key);
                if (cacheObject == null || cacheObject.getValue() == null) {
                    studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    if (studentDetail != null) {
                        List<String> bookIds = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, getClientVersion(), true,
                                null, Subject.ENGLISH);
                        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
                        if (MapUtils.isNotEmpty(textBookByIds) && textBookByIds.values().stream().anyMatch(t -> t.getShortPublisherName().equals("外研版"))) {
                            PiclistenCache.getPersistenceCache().set(key, (int) (GrindEarActivity.endDay.getEndTime() / 1000), "true");
                            isWaiyan = true;
                        } else
                            PiclistenCache.getPersistenceCache().set(key, (int) (GrindEarActivity.endDay.getEndTime() / 1000), "false");
                    }
                } else {
                    isWaiyan = SafeConverter.toBoolean(cacheObject.getValue());
                }
                if (isWaiyan) {
                    grindEarService.mockPushRecord(studentId, new Date());
                }
            }
        }
        long picListenPid = SafeConverter.toLong(headMap.get(RES_PICLISTEN_PARENT_ID));
        if (picListenPid == 0) {
            MapMessage mapMessage = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(studentId);
            if (mapMessage.isSuccess()) {
                Object parentObj = mapMessage.get("parent");
                if (parentObj instanceof User) {
                    User parent = User.class.cast(parentObj);
                    picListenPid = parent.getId();
                }
            }
        }

        return successMessage.add(RES_HEAD, headMap).add(RES_BODY, bodyListMap).add(RES_FOOT, footMap).add(RES_PICLISTEN_INFO, piclistenInfo(bookId, studentDetail, picListenPid));

    }


    private void appendProductFields(Map reduce, MapMessage combine) {

        Double price, originalPrice;
        String desc;
        try {
            if (combine.isSuccess()) {
                List<Map> products = (List<Map>) combine.get("products");
                for (Map item : products) {
                    price = SafeConverter.toDouble(item.get("price"));
                    originalPrice = SafeConverter.toDouble(item.get("originalPrice"));
                    desc = SafeConverter.toString(item.get("desc"));

                    reduce.put(item.get("type"), genPriceMap(price, originalPrice, desc));
                }
            }
            reduce.put(PicListenFunction.NONE, genPriceMap(0.0, 0.0, ""));
        } catch (Exception e) {
            log.error("Reduce book product info failed, case: {}", e.getMessage());
        }

    }

    private Map<String, Object> genPriceMap(Double price, Double originalPrice, String desc) {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("price", price);
        tmp.put("originalPrice", originalPrice);
        tmp.put("desc", desc);
        return tmp;
    }

    private void appendDetailFields(Map reduce, MapMessage combine, String[] fields) {


        Map<String, Object> detail = new HashMap<>();

        try {
            if (combine.isSuccess()) {
                detail = (Map<String, Object>) combine.get("book_detail");
            }

            for (String name : fields) {
                reduce.put(name, detail.get(name));
            }

            // AND (status && walkman_status)
            String status = SafeConverter.toString(reduce.get("status"));
            String walkman_status = SafeConverter.toString(reduce.get("walkman_status"));
            if ("not_purchased".equals(walkman_status)) {
                reduce.put("status", "not_purchased");
                reduce.put("is_purchased", false);
            }
            reduce.put("piclisten_status", status);
        } catch (Exception e) {
            log.error("Reduce book detail info failed, case: {}", e.getMessage());
        }


    }


    private String genPayMentGateWay(String orderId) {
        String path = "%s/view/mobile/parent/17my_shell/affirm.vpage?oid=%s";
        //
        Mode mode = RuntimeMode.current();
        String domain = "https://www.test.17zuoye.net";
        if (mode == Mode.STAGING) {
            domain="https://www.staging.17zuoye.net";
        }else if (mode.gt(Mode.STAGING)) {
            domain = "https://www.17zuoye.com";
        }
        return String.format(path, domain, orderId);
    }


    private boolean availableForRenew(OrderProduct orderProduct, Long userId) {
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.PicListenBook) {
            List<OrderProductItem> productItems = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId());
            if (CollectionUtils.isEmpty(productItems)) {
                return false;
            }
            List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(userId);
            if (CollectionUtils.isEmpty(userActivatedProducts)) {
                return true;
            }
            Set<String> productItemIds = productItems.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            final Date currentDay = new Date();
            long count = userActivatedProducts.stream()
                    .filter(uap -> productItemIds.contains(uap.getProductItemId()))
                    .filter(uap -> uap.getServiceEndTime().after(currentDay))
                    .count();
            return count == 0;
        }
        return true;
    }

    private OrderSynchronizeContext generateAttrForPicListenBook(OrderProduct product) {
        OrderSynchronizeContext context = new OrderSynchronizeContext();

        if (StringUtils.isNotBlank(product.getAttributes())) {
            Map<String, Object> productAttr = JsonUtils.fromJson(product.getAttributes());
            if (MapUtils.isNotEmpty(productAttr) && productAttr.containsKey("piclisten_package_id")) {
                context.setPackageId(productAttr.get("piclisten_package_id").toString());
            }
        }

        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        if (CollectionUtils.isNotEmpty(orderProductItems)) {
            for (OrderProductItem item : orderProductItems) {
                if (OrderProductServiceType.safeParse(item.getProductType()) != OrderProductServiceType.PicListenBook) continue;
                TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(item.getAppItemId());
                if (null == sdkInfo || StringUtils.isBlank(sdkInfo.getSdkBookIdV2())) continue;

                context.addBook(sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(), item.getPeriod(), BigDecimal.valueOf(item.getOriginalPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
            }
        }
        return context;
    }


    private String mapV(Map map,String key) {
        Object v = map.get(key);
        return v==null?"":String.valueOf(v);
    }


    /**
     * 头图就是点读机
     * 有点读机就显示入口
     * 没有点读机 就显示 不支持点读机的图。
     *
     * @return
     */
    private Map<String, Object> generateBookDetailHead(Map detailMap,Map productMap,NewBookProfile bookProfile, String sys, User user, String version) {

        Boolean picListenShow = functionSupport(PicListenFunction.PIC_LISTEN, bookProfile.getId(), sys, user, version);
        Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
        PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookProfile.getId());
        PicListenResourcesPackage picListenResourcesPackage = picListenLoaderClient.loadPicListenResourcesPackage(PicListenResourcesPackage.PackageType.BOOK, bookProfile.getId());

        if (!picListenShow) { // 如果不支持点读 返回空数据
            return Collections.emptyMap();
        }


        Map<String, Object> result= functionMap(mapV(detailMap,"piclisten_status"), mapV(detailMap,"expire"),
                PicListenFunction.PIC_LISTEN, PicListenFunction.PIC_LISTEN.getV3ImageUrl(),
                picListenResourcesPackage, picListenBookPayInfo != null ? picListenBookPayInfo.getParentId() : null);

        Map append = (Map) productMap.get(PicListenFunction.PIC_LISTEN);
        if ( append != null) {
            result.putAll(append);
        }else{
            result.putAll((Map) productMap.get(PicListenFunction.NONE));
        }
        return result;

    }


    private List<Map<String, Object>> generateBookDetailBody(Map detailMap,Map productMap,NewBookProfile bookProfile, String sys, User user, String version) {
        List<Map<String, Object>> bodyMapList = new ArrayList<>();
        //自制教材不支持点读,对应纳米盒子教材支持点读,这种要隐藏单词表入口
        String bookId = bookProfile.getId();
        Long picListenParentId = null;

        //如果点读机收费,则其他功能去掉免费收费标签 47211
        Boolean picListenBookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookId);
        for (PicListenFunction picListenFunction : PicListenFunction.bodyFunctionList) {
            //如果教材点读机需要付费,并且不是sdk教材,并且未购买,body第一位显示购买页入口  remove
            if (picListenFunction == PicListenFunction.PIC_LISTEN_BUY_PAGE) {
                continue;
//                Map<String, Object> purchasePageMap = functionMap(false, null, picListenFunction, picListenFunction.getSmallImageUrl(), null, bookProfile, picListenBookNeedPay, null);
//                purchasePageMap.put(RES_FUNCTION_KEY, purchaseUrl(PicListenFunction.PIC_LISTEN, bookProfile.getId()));
//               // bodyMapList.add(purchasePageMap);
            } else {
                if (!functionSupport(picListenFunction, bookId, sys, user, version)){
                    continue;
                }
                String status = "";
                String expire = "";

                Map appendProduct =null ;
                if(picListenFunction == PicListenFunction.WALK_MAN){
                    status = mapV(detailMap, "walkman_status");
                    expire = mapV(detailMap, "walkman_expire");
                    appendProduct= (Map) productMap.get(picListenFunction);
                }


                Map<String, Object> functionMap = functionMap(status,expire, picListenFunction, picListenFunction.getV3ImageUrl(), null, null);
                if (appendProduct != null) {
                    functionMap.putAll(appendProduct);
                }else {
                    functionMap.putAll((Map) productMap.get(PicListenFunction.NONE));
                }
                bodyMapList.add(functionMap);
            }
        }
        bodyMapList.sort(Comparator.comparingInt(o -> SafeConverter.toInt(o.get(RES_FUNCTION_ORDER))));

//        if (bodyMapList.size() % 2 != 0)
//            bodyMapList.add(functionMap(false, null, PicListenFunction.NONE, PicListenFunction.NONE.getSmallImageUrl(), null, bookProfile, false, null));

        return bodyMapList;
    }


    private Map<String, Object> functionMap(String status,String expire,
                                            PicListenFunction picListenFunction, String img,
                                            PicListenResourcesPackage picListenResourcesPackage, Long picListenParentId) {

        Map<String, Object> functionMap = new LinkedHashMap<>();
        functionMap.put(RES_USER_IMG_URL, getCdnBaseUrlStaticSharedWithSep() + img);
        functionMap.put(RES_PRODUCT_TYPE, picListenFunction.getProductServiceType());
        functionMap.put(RES_PURCHASE_TEXT, purchaseText(picListenFunction));
        functionMap.put(RES_FUNCTION_TYPE, picListenFunction.getFunctionType());
        functionMap.put(RES_FUNCTION_KEY, generateFunctionKey(picListenFunction));
        functionMap.put(RES_FUNCTION_ORDER, picListenFunction.getOrder());
        functionMap.put(RES_STATUS, status);
        functionMap.put(RES_EXPIRE_DATE, expire);
        if (picListenResourcesPackage != null) {
            functionMap.put(RES_ZIP_URL, picListenResourcesPackage.getPackageUrl());
            functionMap.put(RES_ZIP_MD5, picListenResourcesPackage.getPackageMd5());
        }
        if (picListenParentId != null) {
            functionMap.put(RES_PICLISTEN_PARENT_ID, picListenParentId);
        }
        return functionMap;
    }

    private String purchaseText(PicListenFunction picListenFunction) {
        if (PicListenFunction.PIC_LISTEN == picListenFunction || PicListenFunction.WALK_MAN == picListenFunction)
            return "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。";
        if (PicListenFunction.FOLLOW_READ == picListenFunction)
            return "跟读的评分功能需要付费解锁后才能查看";
        return "";
    }


    private String generateFunctionKey(PicListenFunction function) {
        if (function.getFunctionType().equals("H5"))
            return fetchMainsiteUrlByCurrentSchema() + function.getUrl();
        else
            return function.name();
    }


    private List<Map<String, Object>> picListenAdMapList(NewBookProfile newBookProfile, SelfStudyAdPosition selfStudyAdPosition) {
        List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(selfStudyAdPosition, newBookProfile);
        List<Map<String, Object>> bannerMapList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(selfStudyAdInfoList)) {
            selfStudyAdInfoList.forEach(ad -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_USER_IMG_URL, ad.getImgUrl());
                if (StringUtils.isNotBlank(ad.getJumpUrl()))
                    map.put(RES_JUMP_URL, ad.getJumpUrl());
                bannerMapList.add(map);
            });
        }
        return bannerMapList;
    }

    private Map<String, Object> extraEntry(Long parentId){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(RES_MY_RECORD_URL, fetchMainsiteUrlByCurrentSchema() + "/view/wx/parent/reading/records");
        //点读报告,灰度入口
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isNotEmpty(studentParentRefs)){
            Set<Long> studentIdSet = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
            Boolean hit = false;
            for (StudentDetail studentDetail : studentDetailMap.values()) {
                hit= grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jztpiclisen", "reportentry");
                if (hit)
                    break;
            }
            if (hit)
                map.put(RES_PICLISTEN_REPORT_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/read_report/index.vpage");

            //排行版入口
            map.put(RES_RANGE_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/read_report/rank.vpage");
        }
        return map;
    }


    private Object piclistenInfo(String bookId, StudentDetail studentDetail, Long picListenPid) {
        Map<String, Object> map = new HashMap<>();
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookId);
        addIntoMapSdk(map, sdkInfo, studentDetail);
        if (picListenPid != null && picListenPid != 0)
            map.put(RES_PICLISTEN_PARENT_ID, picListenPid);
        return map;
    }

    private boolean bookFunctionNeedPay(PicListenFunction picListenFunction, String bookId) {
        //目前已知  点读,跟读需要付费,其他都默认不收费。。。
        //加一个随身听的付费配置
        if (picListenFunction == PicListenFunction.PIC_LISTEN) {
            return textBookManagementLoaderClient.picListenBookNeedPay(bookId);
        }
//        if (picListenFunction == PicListenFunction.FOLLOW_READ)
//            return true;
        if (picListenFunction == PicListenFunction.WALK_MAN) {
            return textBookManagementLoaderClient.walkManNeedPay(bookId);
        }
        return false;
    }

    private DayRange buyLastDayRange(PicListenFunction picListenFunction, String bookId, Boolean needPay, User user) {
        if (needPay) {
            if (picListenFunction == PicListenFunction.PIC_LISTEN) {
                Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
                PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookId);
                return picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null;
            }
            if (picListenFunction == PicListenFunction.FOLLOW_READ) {
                return picListenCommonService.parentBuyScoreLastDay(user.getId());
            }
            if (picListenFunction == PicListenFunction.WALK_MAN) {
                Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyWalkManLastDayMap(user.getId(), false);
                return dayRangeMap.get(bookId) != null ? dayRangeMap.get(bookId) : null;
            }
            return null;
        } else
            return null;
    }

    /**
     * 这本教材的某个点读功能是否支持
     * 如果是纳米盒子教材,就看该教材的支持情况
     * 如果是自制教材,要看下对应的纳米盒子教材的支持情况
     *
     * @param picListenFunction
     * @param bookId
     * @param sys
     * @param user
     * @return
     */
    private Boolean functionSupport(PicListenFunction picListenFunction, String bookId, String sys, User user, String version) {

        switch (picListenFunction) {
            case PIC_LISTEN:
                Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(user);
                return textBookManagementLoaderClient.picListenShow(bookId, sys, parentAuth);
            case WALK_MAN:
                return textBookManagementLoaderClient.walkManBookShow(bookId, sys) && (textBookManagementLoaderClient.walkManLeastSupportVersion(bookId, version) || !textBookManagementLoaderClient.walkManNeedPay(bookId));
            case TEXT_READ:
                return textBookManagementLoaderClient.textReadBookShow(bookId, sys);
            case ENGLISH_WORD_LIST:
                return textBookManagementLoaderClient.englishWordListShow(bookId);
            case CHINESE_WORD_LIST:
                return textBookManagementLoaderClient.chineseWordListShow(bookId);
            case FOLLOW_READ:
                //65804:跟读打分功能，在有效期内用户或者教材有跟读功能的才开放，其余用户不可见
                Boolean hasBuyScore = picListenCommonService.parentHasBuyScore(user.getId());
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                return textBookManagementLoaderClient.followReadBookSupport(bookId) || (hasBuyScore && StringUtils.equals("人民教育出版社", newBookProfile.getPublisher()) && newBookProfile.getSubjectId().equals(Subject.ENGLISH.getId()));
            case READING:
                return textBookManagementLoaderClient.readingShow(bookId);
            default:
                return false;
        }
    }


    private Integer getClazzLevelcBook(StudentDetail studentDetail) {
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()))
            return 7;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null)
            return 3;
        return studentDetail.getClazzLevelAsInteger();
    }


    protected String sys() {
        return getRequestString("sys");
    }

    protected Long uid() {
        String sid = getRequestString("sid");
        return SafeConverter.toLong(sid);
    }

    protected Long pid() {
        User user = getApiRequestUser();
        if (user != null) {
            return user.getId();
        }
        return 0L;
    }


    private MapMessage wrapper(Consumer<MapMessage> wrapper) {

        MapMessage mm = successMessage();
        mm.setSuccess(true);
        try {
            wrapper.accept(mm);

            if (!mm.isSuccess()) {
                mm.set(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mm.set(RES_MESSAGE, mm.getInfo());
            }
        }catch (DuplicatedOperationException e) {
            mm = failMessage("您点击太快了，请重试");
        } catch (Exception e) {
            if (RuntimeMode.current().lt(Mode.STAGING)) {
                mm = failMessage(e.getMessage());
            } else {
                mm = failMessage("服务器繁忙，请稍后再试");
            }
            log.error(e.getMessage());
        }
        return mm;
    }


    private boolean blank(CharSequence cs) {
        return StringUtils.isBlank(cs);
    }


    private MiniProgramBookService bookService() {
        return miniProgramBookServiceClient.getRemoteReference();
    }

}
