package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-08-14 下午6:09
 **/
@Controller
@RequestMapping(value = "/parentMobile/piclisten_package")
@Slf4j
public class MobileParentPicListenPackageController extends AbstractMobileParentSelfStudyController {

    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;


//    @RequestMapping(value = "/index_back.vpage", method = {RequestMethod.GET})
//    @ResponseBody
//    public MapMessage index222() {
//        User parent = currentParent();
//        if (parent == null)
//            return noLoginResult;
//        long sid = getRequestLong("sid");
//        if (sid == 0L)
//            return noLoginResult;
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
//        if (studentDetail == null)
//            return MapMessage.errorMessage("获取学生信息失败");
//
//        String bookId = getRequestString("book_id");
//        OrderProduct product;
//        if (StringUtils.isBlank(bookId)){
//            Boolean parentAuth = parentSelfStudyPublicHelper.isParentAuth(parent.getId());
//            List<String> bookIds = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, getAppVersion(), parentAuth, null, Subject.ENGLISH);
//            if (CollectionUtils.isEmpty(bookIds))
//                return MapMessage.errorMessage("没有推荐的教材");
//           bookId = bookIds.get(0);
//        }
//        Integer clazzLevelForRecBook = getClazzLevelForRecBook(studentDetail);
//        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
//        if (!renjiaoEnglishSeriesIdSet.contains(newBookProfile.getSeriesId())){
//
//            bookId = defaultRenjiaoEnglishBookId(clazzLevelForRecBook);
//            if (StringUtils.isBlank(bookId))
//                return MapMessage.errorMessage("没有推荐的教材");
//        }
//        Map<String, OrderProduct> packageProductByEnglishBookIds = getPackageProductByEnglishBookIds(Collections.singleton(bookId));
//        if (MapUtils.isEmpty(packageProductByEnglishBookIds))
//            return MapMessage.errorMessage("没有推荐的教材");
//        Map.Entry<String, OrderProduct> entry = packageProductByEnglishBookIds.entrySet().stream().findAny().orElse(null);
//        if (entry == null )
//            return MapMessage.errorMessage("没有推荐的教材");
//        product = entry.getValue();
//        if (product == null)
//            return MapMessage.errorMessage("没有推荐的教材");
//        AlpsFuture<List<Map<String, Object>>> buyerListFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_fetch();
//        AlpsFuture<Long> buyCountFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_loadBuyCount();
//
//        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
//        Set<String> bookIds = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
//        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
//        if (MapUtils.isEmpty(textBookByIds))
//            return MapMessage.errorMessage("推荐教材没有上线");
//        Set<String> bookIdSet = textBookByIds.values().stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
//        Map<String, NewBookProfile> stringNewBookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);
//        String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
//        List<String> imgUrlList = stringNewBookProfileMap.values().stream()
//                .filter(t -> !t.getShortPublisher().equals("少英报"))
//                .sorted(Comparator.comparingInt(o -> Subject.fromSubjectId(o.getSubjectId()).getKey()))
//                .map(t -> cdnUrl + t.getImgUrl()).collect(Collectors.toList());
//        MapMessage result = MapMessage.successMessage();
//        if (studentDetail.getClazzLevelAsInteger() != null)
//            result.add("clazz_level", studentDetail.getClazzLevelAsInteger());
//        Long buyCount = buyCountFuture.getUninterruptibly() + 5000L;
//        result.add("buyer_count", buyCount);
//        //是否购买过这个包
//        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(parent.getId());
//        long orderCount = userOrders.stream().filter(t -> t.getProductId().equals(product.getId()) && t.getOrderStatus() == OrderStatus.Confirmed).count();
//        Map<String, DayRange> dayRangeMap = parentSelfStudyPublicHelper.parentBuyBookPicListenLastDayMap(parent.getId());
//        boolean partPurchased = bookIds.stream().anyMatch(dayRangeMap::containsKey);
//        Map<String, Object> packageInfoMap = new HashMap<>();
//        packageInfoMap.put("name", product.getName());
//        packageInfoMap.put("price", product.getPrice());
//        packageInfoMap.put("product_id", product.getId());
//        packageInfoMap.put("img_urls", imgUrlList);
//        packageInfoMap.put("is_purchased", orderCount >= 1);
//        packageInfoMap.put("is_part_purchased", partPurchased);
//
//        result.add("package_info", packageInfoMap);
//
//        result.add("purchase_list", buyerListFuture.getUninterruptibly());
//        return result;
//
//    }

    private Map<String, Object> packageInfoMap(OrderProduct product , StudentDetail studentDetail, User parent){
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        Set<String> bookIds = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
        if (MapUtils.isEmpty(textBookByIds))
            return MapMessage.errorMessage("推荐教材没有上线");
        Set<String> bookIdSet = textBookByIds.values().stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> stringNewBookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);
        String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
        List<String> imgUrlList = stringNewBookProfileMap.values().stream()
                .filter(t -> !t.getShortPublisher().equals("少英报"))
                .sorted(Comparator.comparingInt(o -> Subject.fromSubjectId(o.getSubjectId()).getKey()))
                .map(t -> cdnUrl + t.getImgUrl()).collect(Collectors.toList());

        //是否购买过这个包
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(parent.getId());
        long orderCount = userOrders.stream().filter(t -> t.getProductId().equals(product.getId()) && t.getOrderStatus() == OrderStatus.Confirmed).count();
        if (orderCount >=1)
            return null;
        Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);
        boolean partPurchased = bookIds.stream().anyMatch(dayRangeMap::containsKey);
        if (partPurchased)
            return null;
        Map<String, Object> packageInfoMap = new HashMap<>();
        packageInfoMap.put("name", product.getName());
        packageInfoMap.put("price", product.getPrice());
        packageInfoMap.put("product_id", product.getId());
        packageInfoMap.put("img_urls", imgUrlList);
        packageInfoMap.put("is_purchased", false);
        packageInfoMap.put("is_part_purchased", false);

        return packageInfoMap;
    }

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage index() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long sid = getRequestLong("sid");
        if (sid == 0L)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return MapMessage.errorMessage("获取学生信息失败");
        Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(parent);

        List<String> recRenjiaoEnglishBookIds = recRenjiaoBookIds(studentDetail, getAppVersion(), parentAuth, Subject.ENGLISH);
        List<String> recRenjiaoChineseBookIds = recRenjiaoBookIds(studentDetail, getAppVersion(), parentAuth, Subject.CHINESE);

        Map<String, OrderProduct> packageProductByEnglishBookIds = getPackageProductByEnglishBookIds(recRenjiaoEnglishBookIds);

        AlpsFuture<List<Map<String, Object>>> buyerListFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_fetch();
        AlpsFuture<Long> buyCountFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_loadBuyCount();

        List<Map<String, Object>> packageMapList= new ArrayList<>();

        for (OrderProduct product : packageProductByEnglishBookIds.values()) {
            Map<String, Object> map = packageInfoMap(product, studentDetail, parent);
            if (map != null)
                packageMapList.add(map);
        }

        Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);


        List<String> allBookIds = new ArrayList<>(recRenjiaoChineseBookIds);
        allBookIds.addAll(recRenjiaoEnglishBookIds);
        Map<String, NewBookProfile> books = newContentLoaderClient.loadBooks(allBookIds);
        List<Map<String, Object>> singleBookMapList = new ArrayList<>();
        String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
        boolean inRenjiaoPeriod = inRenjiaoPeriod();
        books.values().stream().sorted( (o1, o2) -> {
            int i1 = o1.getSubjectId().compareTo(o2.getSubjectId());
            if (i1 != 0)
                return i1;
            int i = o1.getClazzLevel().compareTo(o2.getClazzLevel());
            if (i == 0){
                return o1.getTermType().compareTo(o2.getTermType());
            }else
                return i;
        }).forEach(book -> {
            Map<String, Object> map = new HashMap<>();
            map.put("img", cdnUrl + book.getImgUrl());
            map.put("name", book.getShortName());
            map.put("price", inRenjiaoPeriod ? 16:20);
            map.put("period", inRenjiaoPeriod ? "270天":"180天");
            map.put("book_id", book.getId());
            map.put("is_purchased", dayRangeMap.containsKey(book.getId()));
            singleBookMapList.add(map);
        });

        MapMessage result = MapMessage.successMessage();
        if (studentDetail.getClazzLevelAsInteger() != null)
            result.add("clazz_level", studentDetail.getClazzLevelAsInteger());
        result.add("package_list", packageMapList);
        Long buyCount = buyCountFuture.getUninterruptibly() + 5000L;
        result.add("buyer_count", buyCount);
        result.add("single_book_list", singleBookMapList);
        result.add("purchase_list", buyerListFuture.getUninterruptibly());
        return result;
    }

    private boolean inRenjiaoPeriod(){
        int dayOfMonth = LocalDateTime.now().getDayOfMonth();
        Month month = LocalDateTime.now().getMonth();
        if (RuntimeMode.lt(Mode.PRODUCTION)){
            return month== Month.NOVEMBER && (dayOfMonth == 8 || dayOfMonth == 10 || dayOfMonth == 9);
        }else {
            return month== Month.NOVEMBER && (dayOfMonth == 11 || dayOfMonth == 10 || dayOfMonth == 12);
        }
    }

    List<String> recRenjiaoBookIds(StudentDetail studentDetail, String version, Boolean parentIsAuth, Subject subject){
        List<String> recBookIds = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, version, parentIsAuth, null, subject);
        if (CollectionUtils.isEmpty(recBookIds))
            return Collections.emptyList();
        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(recBookIds);
        Integer clazzLevelForRecBook = getClazzLevelForRecBook(studentDetail);
        if (subject == Subject.ENGLISH){
            if (bookMap.values().stream().noneMatch(t -> renjiaoEnglishSeriesIdSet.contains(t.getSeriesId()))){
                return defaultRenjiaoEnglishBookId(clazzLevelForRecBook);
            }

        }
        if (subject == Subject.CHINESE){
            if(bookMap.values().stream().noneMatch(t -> t.getShortPublisher().equalsIgnoreCase("人教版"))){
                return defaultRenjiaoChineseBookId(clazzLevelForRecBook);
            }
        }
        return recBookIds;
    }



    @RequestMapping(value = "/waiyan_index.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage indexWaiyan() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long sid = getRequestLong("sid");
        if (sid == 0L)
            return noLoginResult;
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return MapMessage.errorMessage("获取学生信息失败");


        AlpsFuture<List<Map<String, Object>>> buyerListFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_waiyanFetch();
        AlpsFuture<Long> buyCountFuture = asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_loadBuyCount();


        List<String> studentDefaultEnglishBook = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, getAppVersion(), true, null, Subject.ENGLISH);
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(studentDefaultEnglishBook);
        Collection<NewBookProfile> bookProfiles = bookProfileMap.values();
        NewBookProfile newBookProfile = bookProfiles.stream().findAny().orElse(null);
        if (newBookProfile == null)
            return MapMessage.errorMessage("没有推荐的教材");
        String seriesId = newBookProfile.getSeriesId();
        if (!seriesId.equals("BKC_10300026399628") && !seriesId.equals("BKC_10300029414422"))
            seriesId = "BKC_10300026399628";
        Integer clazzLevelForRecBook = getClazzLevelForRecBook(studentDetail);
        OrderProduct product = waiyanPackageProductByClazzLevelAndSeriesId(clazzLevelForRecBook, seriesId);
        if (product == null)
            return MapMessage.errorMessage("没有对应的打包产品");
        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        Set<String> bookIds = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
        if (MapUtils.isEmpty(textBookByIds))
            return MapMessage.errorMessage("推荐教材没有上线");
        Set<String> bookIdSet = textBookByIds.values().stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> stringNewBookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);
        String cdnUrl = getCdnBaseUrlStaticSharedWithSep();
        List<String> imgUrlList = stringNewBookProfileMap.values().stream().filter(t -> !t.getShortPublisher().equals("少英报"))
                .sorted(Comparator.comparingInt(NewBookProfile::getTermType)).map(t -> cdnUrl + t.getImgUrl()).collect(Collectors.toList());
        MapMessage result = MapMessage.successMessage();
        if (studentDetail.getClazzLevelAsInteger() != null)
            result.add("clazz_level", studentDetail.getClazzLevelAsInteger());
        Long buyCount = buyCountFuture.getUninterruptibly() + 5000L;
        result.add("buyer_count", buyCount);
        //是否购买过这个包
        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(parent.getId());
        long orderCount = userOrders.stream().filter(t -> t.getProductId().equals(product.getId()) && t.getOrderStatus() == OrderStatus.Confirmed).count();
        Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);


        boolean partPurchased = bookIds.stream().anyMatch(dayRangeMap::containsKey);
        Map<String, Object> packageInfoMap = new HashMap<>();
        packageInfoMap.put("name", product.getName());
        packageInfoMap.put("price", product.getPrice());
        packageInfoMap.put("original_price", product.getOriginalPrice());
        packageInfoMap.put("product_id", product.getId());
        packageInfoMap.put("img_urls", imgUrlList);
        packageInfoMap.put("is_purchased", orderCount >= 1);
        packageInfoMap.put("is_part_purchased", partPurchased);
        packageInfoMap.put("period", "2年");

        result.add("package_list", Collections.singleton(packageInfoMap));


        Map<String, NewBookProfile> books = newContentLoaderClient.loadBooks(bookIds);
        List<Map<String, Object>> singleBookMapList = new ArrayList<>();
        books.values().stream().sorted((o1, o2) -> {
            int i = o1.getClazzLevel().compareTo(o2.getClazzLevel());
            if (i == 0) {
                return o1.getTermType().compareTo(o2.getTermType());
            } else
                return i;
        }).forEach(book -> {
            Map<String, Object> map = new HashMap<>();
            map.put("img", cdnUrl + book.getImgUrl());
            map.put("name", book.getShortName());
            map.put("price", getSingleBookPrice());
            map.put("period", "1年");
            map.put("book_id", book.getId());
            map.put("is_purchased", dayRangeMap.containsKey(book.getId()));
            singleBookMapList.add(map);
        });

        result.add("purchase_list", buyerListFuture.getUninterruptibly()).add("single_book_list", singleBookMapList);
        return result;

    }

    private double getSingleBookPrice() {
        if (RuntimeModeLoader.getInstance().current() == Mode.TEST) {   //测试环境8－11号卖40元
            if (LocalDateTime.now().getMonth() == Month.NOVEMBER && LocalDateTime.now().getDayOfMonth() >= 8 && LocalDateTime.now().getDayOfMonth() <= 11) {
                return 40;
            }
        } else {    //其它环境11号卖40元
            if (LocalDateTime.now().getMonth() == Month.NOVEMBER && LocalDateTime.now().getDayOfMonth() == 11) {
                return 40;
            }
        }
        return 50;
    }


    //通过关系找到 bookId，然后通过
    private OrderProduct waiyanPackageProductByClazzLevelAndSeriesId(Integer clazzLevel, String seriesId) {
        String waiYanBookId = findWaiYanBookId(clazzLevel, seriesId);
        if (StringUtils.isBlank(waiYanBookId))
            return null;
        return getPackageProductByEnglishBookIds(Collections.singleton(waiYanBookId)).get(waiYanBookId);
    }

    private static Map<String, String> clazzLeveQi2BookIdMap = new HashMap<>();

    static {
        //key 一年级起1 三年级起3 _ 年级
        clazzLeveQi2BookIdMap.put("1_1", "BK_10300000560861");
        clazzLeveQi2BookIdMap.put("1_2", "BK_10300000562568");
        clazzLeveQi2BookIdMap.put("1_3", "BK_10300000564121");
        clazzLeveQi2BookIdMap.put("1_4", "BK_10300000565787");
        clazzLeveQi2BookIdMap.put("1_5", "BK_10300000566950");
        clazzLeveQi2BookIdMap.put("1_6", "BK_10300000567735");
        clazzLeveQi2BookIdMap.put("3_3", "BK_10300000585752");
        clazzLeveQi2BookIdMap.put("3_4", "BK_10300000587874");
        clazzLeveQi2BookIdMap.put("3_5", "BK_10300000589334");
        clazzLeveQi2BookIdMap.put("3_6", "BK_10300000590599");
    }

    // todo 通过写死的关系，找到一本外研教材
    // 一起 BKC_10300026399628   三起 BKC_10300029414422
    private String findWaiYanBookId(Integer clazzLevel, String seriesId) {
        if (seriesId.equals("BKC_10300026399628")) //一年级起
            return clazzLeveQi2BookIdMap.get("1_" + clazzLevel);
        else if (seriesId.equals("BKC_10300029414422"))//三年级起
            return clazzLeveQi2BookIdMap.get("3_" + clazzLevel);
        else
            return null;
    }


    private static Set<String> renjiaoEnglishSeriesIdSet = new HashSet<>();
    static {
        renjiaoEnglishSeriesIdSet.add("BKC_10300008060767"); //PEP
        renjiaoEnglishSeriesIdSet.add("BKC_10300031638488");//新起点
        renjiaoEnglishSeriesIdSet.add("BKC_10300010769960");//精通

    }

    @RequestMapping(value = "/clazz_level.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage clazzLevel() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long sid = currentRequestStudentId();
        if (sid == 0L)
            return noLoginResult;
        Integer clazzLevel = getRequestInt("clazz_level");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null)
            return MapMessage.errorMessage("获取学生信息失败");
        if (clazzLevel <= 0 || clazzLevel > 6) {
            clazzLevel = getClazzLevelForRecBook(studentDetail);
        }


        List<TextBookManagement> subjectClazzLevelList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.ENGLISH, clazzLevel);
        Set<String> renjiaoTextBookIds = subjectClazzLevelList.stream().filter(t -> t.getShortPublisherName().equals("人教版")).
                map(TextBookManagement::getBookId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(renjiaoTextBookIds))
            return MapMessage.errorMessage("获取年级教材失败");
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(renjiaoTextBookIds);
        Map<String, DayRange> lastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, List<NewBookProfile>> seriesId2BookListMap = bookProfileMap.values().stream().collect(Collectors.groupingBy(NewBookProfile::getSeriesId));
        Set<String> seriesIds = seriesId2BookListMap.keySet();
        Map<String, NewBookCatalog> seriesMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIds);
        long finalStudentId = sid;
        seriesIds.stream().filter(renjiaoEnglishSeriesIdSet::contains).forEach((String seriesId) -> {
            NewBookCatalog seriesNode = seriesMap.get(seriesId);
            if (seriesNode == null)
                return;
            List<NewBookProfile> newBookProfiles = seriesId2BookListMap.get(seriesId);
            if (CollectionUtils.isEmpty(newBookProfiles))
                return;
            Map<String, Object> map = new HashMap<>();
            map.put("series_name", seriesNode.getName());

            List<Map<String, Object>> bookList = new ArrayList<>();
            newBookProfiles.forEach((NewBookProfile bookProfile) -> {
                Map<String, Object> bookMap = parentSelfStudyPublicHelper.bookMap(bookProfile, lastDayMap.containsKey(bookProfile.getId()), finalStudentId);
                bookMap.put("img", getCdnBaseUrlStaticSharedWithSep() + bookMap.get("img"));
                bookList.add(bookMap);
            });
            map.put("book_list", bookList);
            mapList.add(map);
        });
        return MapMessage.successMessage().add("clazz_level", clazzLevel).add("series_book_list", mapList);
    }


    public Integer getClazzLevelForRecBook(StudentDetail studentDetail) {
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()))
            return 6;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null)
            return 3;
        return studentDetail.getClazzLevelAsInteger();
    }

    public List<String> defaultRenjiaoEnglishBookId(Integer clazzLevel) {
        if (clazzLevel == null)
            clazzLevel = 3;
        List<String> list = new ArrayList<>();
        switch (clazzLevel) {
            case 0:
            case 1:
                list.add("BK_10300001722068"); list.add("BK_10300001807246");break;
            case 2:
                list.add("BK_10300001724304"); list.add("BK_10300001808902");break;
            case 3:
                list.add("BK_10300001723050"); list.add("BK_10300001809567");break;
            case 4:
                list.add("BK_10300001725952"); list.add("BK_10300001811288");break;
            case 5:
                list.add("BK_10300001727218"); list.add("BK_10300001810866");break;
            case 6:
            case 7:
                list.add("BK_10300001728872"); list.add("BK_10300001812518");break;
        }
        return list;
    }

    public List<String> defaultRenjiaoChineseBookId(Integer clazzLevel){
        if (clazzLevel == null)
            clazzLevel = 3;
        List<String> list = new ArrayList<>();
        switch (clazzLevel) {
            case 0:
            case 1:
                list.add("BK_10100001675679"); list.add("BK_10100002551703");break;
            case 2:
                list.add("BK_10100000004683"); list.add("BK_10100000003482");break;
            case 3:
                list.add("BK_10100000013407"); list.add("BK_10100000008693");break;
            case 4:
                list.add("BK_10100000005225"); list.add("BK_10100000006594");break;
            case 5:
                list.add("BK_10100000007851"); list.add("BK_10100000002989");break;
            case 6:
            case 7:
                list.add("BK_10100000011387"); list.add("BK_10100000012766");break;
        }
        return list;
    }


}
