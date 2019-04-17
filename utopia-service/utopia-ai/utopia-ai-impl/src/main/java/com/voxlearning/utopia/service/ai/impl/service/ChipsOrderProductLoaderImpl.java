package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.ai.api.ChipsOrderProductLoader;
import com.voxlearning.utopia.service.ai.cache.manager.UserProductBuyCacheManager;
import com.voxlearning.utopia.service.ai.data.ShortTravelProductConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.CollectionExtUtil;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.api.SchoolLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/8/15
 */
@Named
@ExposeService(interfaceClass = ChipsOrderProductLoader.class)
public class ChipsOrderProductLoaderImpl implements ChipsOrderProductLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private UserProductBuyCacheManager userProductBuyCacheManager;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @ImportService(interfaceClass = SchoolLoader.class)
    private SchoolLoader schoolLoader;

    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;

    @Inject
    private GroupLoaderClient groupLoaderClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private ChipsUserService chipsUserService;

    @Override
    public MapMessage loadOnSaleShortLevelProductInfo(Long parentId) {
        Long studentId = Optional.ofNullable(parentId).map(user -> studentLoaderClient.loadParentStudents(user)).filter(CollectionUtils::isNotEmpty).map(e -> e.get(0)).map(User::getId).orElse(null);
        return loadOnSaleShortLevelProductInfo(parentId, studentId, false);
    }

    @Override
    public MapMessage loadOnSaleShortLevelProductInfo() {
        return loadOnSaleShortLevelProductInfo(null, null, false);
    }

    @Override
    public MapMessage loadOfficialProductInfoByType(String typeName, Long userId) {
        if (StringUtils.isBlank(typeName)) {
            return MapMessage.successMessage("参数错误");
        }

        List<ChipsUserCourse> chipsUserCourses = userId != null ? chipsUserService.loadUserEffectiveCourseIncludeUnactive(userId) : Collections.emptyList();
        List<String> userBooks = Optional.ofNullable(chipsUserCourses)
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> e.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()))
                .map(ids -> userOrderLoaderClient.loadOrderProductItems(ids))
                .map(map -> map.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        int minSur = 0;
        List<Map> productInfoList = new ArrayList<>();
        List<Map> configs = chipsContentService.loadOfficialConfig(typeName);
        for (Map config : configs) {
            List<String> books = Arrays.stream(SafeConverter.toString(config.get("books"), "").split(",")).collect(Collectors.toList());
            config.put("paid", (CollectionUtils.isNotEmpty(chipsUserCourses) && CollectionExtUtil.hasIntersection(books, userBooks)));
            List<String> productIds = Arrays.stream(SafeConverter.toString(config.get("productId"), "").split(",")).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(productIds)) {
                continue;
            }

            Map<String, ChipsEnglishProductTimetable> timetableMap = chipsEnglishProductTimetableDao.loads(productIds);
            if (MapUtils.isEmpty(timetableMap)) {
                continue;
            }

            ChipsEnglishProductTimetable timetable = timetableMap.values().stream().filter(e -> e.getBeginDate() != null)
                    .sorted(Comparator.comparing(ChipsEnglishProductTimetable::getBeginDate)).findFirst().orElse(null);
            if (timetable == null) {
                continue;
            }
            config.put("beginDate", timetable.getBeginDate());
            Collection<OrderProduct> orderProductList = userOrderLoaderClient.loadOrderProducts(productIds).values();
            if (CollectionUtils.isEmpty(orderProductList)) {
                continue;
            }
            BigDecimal price = orderProductList.stream().map(OrderProduct::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(0);
            config.put("price", price);
            BigDecimal origPrice = orderProductList.stream().map(OrderProduct::getOriginalPrice).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(0);
            config.put("originalPrice", origPrice);
            int buyNumber = userProductBuyCacheManager.getRecord(productIds.get(0)).size();
            int cfgNumber = chipsContentService.loadProductNumber(productIds.get(0));
            int surplus = Math.max(0, cfgNumber - buyNumber);
            if (surplus > 0) {
                minSur = minSur > 0 ? Math.min(minSur, surplus) : surplus;
            }
            config.put("surplus", surplus);
            productInfoList.add(config);
        }
        Integer clazzLevel = null;
        Integer localCode = null;
        if (userId != null) {
            User user = userLoaderClient.loadUser(userId, UserType.PARENT);
            if (user != null && user.isParent()) {
                StudentDetail studentDetail = Optional.ofNullable(studentLoaderClient.loadParentStudents(user.getId()))
                        .filter(CollectionUtils::isNotEmpty)
                        .map(e -> e.get(0))
                        .map(u -> studentLoaderClient.loadStudentDetail(u.getId()))
                        .orElse(null);
                clazzLevel = Optional.ofNullable(studentDetail).map(StudentDetail::getClazzLevelAsInteger).orElse(null);
                localCode = Optional.ofNullable(studentDetail).map(StudentDetail::getClazz).map(Clazz::getSchoolId)
                        .map(schoolLoader::loadSchool)
                        .map(e -> e.getUninterruptibly())
                        .map(school -> raikouSystem.loadRegion(school.getRegionCode()))
                        .map(ExRegion::getCityCode)
                        .orElse(null);
            }
        }
        return MapMessage.successMessage()
                .set("productList", productInfoList)
                .set("clazzLevel", clazzLevel)
                .set("localCode", localCode)
                .set("surplus", minSur);
    }

    @Override
    public Boolean checkBookBoughtMutex(List<String> userBooks, List<String> targetBooks) {
        if (CollectionUtils.isEmpty(userBooks) || CollectionUtils.isEmpty(targetBooks)) {
            return false;
        }

        Map<String, List<String>> bookMap = chipsContentService.loadBookMutexMap();
        if (MapUtils.isEmpty(bookMap)) {
            return false;
        }

        for (String book : userBooks) {
            List<String> mutexList = bookMap.get(book);
            if (CollectionUtils.isEmpty(mutexList)) {
                continue;
            }

            boolean hasInter = CollectionExtUtil.hasIntersection(mutexList, targetBooks);
            if (hasInter) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapMessage loadOnSaleShortLevelProductInfo(Long parentId, Long studentId, Boolean checkStudent) {
        return loadOnSaleShortLevelProductInfo(parentId, studentId, checkStudent, "");
    }

    @Override
    public String loadShortProductAdPath(Long userId) {
        return chipsContentService.shortProductPath(userId);
    }


    @Override
    public MapMessage loadOnSaleShortLevelProductInfo(Long parentId, Long studentId, Boolean checkStudent, String type) {
        ProductInfo productInfo = doLoadShortTravelOrderProduct(studentId, checkStudent, type);
        if (productInfo == null) {
            return MapMessage.errorMessage("no product config");
        }

        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(productInfo.getProductId());
        if (CollectionUtils.isEmpty(orderProductItems)) {
            return MapMessage.errorMessage("no products items");
        }

        MapMessage res = MapMessage.successMessage();
        res.add("beginDate", DateUtils.dateToString(productInfo.getBeginDate(), "yyyy年MM月dd日"))
                .add("endDate", DateUtils.dateToString(DateUtils.addDays(productInfo.getBeginDate(), orderProductItems.get(0).getPeriod()), "yyyy年MM月dd日"))
                .add("sellOutDate", DateUtils.dateToString(productInfo.getSellOutDate(), "yyyy年MM月dd日"))
                .add("productName", productInfo.getProductName())
                .add("sellOut", productInfo.isSaleOut() || new Date().after(productInfo.getSellOutEndDate()))
                .add("surplus", productInfo.getSurplus())
                .add("productId", productInfo.getProductId())
                .add("originalPrice", productInfo.getOriginalPrice())
                .add("video", productInfo.getVideo())
                .add("videoImage", productInfo.getVideoImage())
                .add("images", productInfo.getCalendarImages())
                .add("contentImages", productInfo.getContentImages())
                .add("beImages", productInfo.getAdImages())
                .add("beImages2", productInfo.getAdImages2())
                .add("cardImages", productInfo.getCardImages())
                .add("description", productInfo.getDescription())
                .add("trailBookId", productInfo.getTrialBookId())
                .add("trailUnitId", productInfo.getTrialUnitId())
                .add("price", productInfo.getPrice())
                .set("rank", productInfo.getRank());

        PaymentStatus status = PaymentStatus.Unpaid;
        if (parentId != null) {
            res.add("userId", parentId);
            List<ChipsUserCourse> chipsUserCourses = chipsUserService.loadUserEffectiveCourse(parentId);
            List<String> userBooks = Optional.ofNullable(chipsUserCourses)
                    .filter(CollectionUtils::isNotEmpty)
                    .map(e -> e.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()))
                    .map(ids -> userOrderLoaderClient.loadOrderProductItems(ids))
                    .map(map -> map.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
            List<String> orderBooks = orderProductItems.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(chipsUserCourses) && (CollectionExtUtil.hasIntersection(userBooks, orderBooks) || checkBookBoughtMutex(userBooks, orderBooks))) {
                status = PaymentStatus.Paid;
            }
        }
        res.put("status", status.name());
        return res;
    }

    private ProductInfo doLoadShortTravelOrderProduct(Long studentId, Boolean checkStudent, String type) {
        String bookId = Optional.ofNullable(studentId)
                .map(studentLoaderClient::loadStudentDetail)
                .map(studentDetail -> {
                    Integer clazzLevel = studentDetail.getClazzLevelAsInteger();
                    if (clazzLevel == null || clazzLevel.compareTo(1) < 0) {
                        return "SD_10300001055259";
                    }
                    Long clazzId = studentDetail.getClazz().getId();
                    Set<Long> groupIds = groupLoaderClient.getGroupLoader().loadGroupsByClazzId(clazzId).getUninterruptibly().stream().filter(group -> group.getSubject() == Subject.ENGLISH).map(Group::getId).collect(Collectors.toSet());
                    NewClazzBookRef newClazzBook = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList().stream().filter(t -> Subject.ENGLISH.name().equals(t.getSubject())).sorted(Comparator.comparing(NewClazzBookRef::getUpdateDatetime).reversed()).findFirst().orElse(null);
                    if (newClazzBook == null) {
                        return "SD_10300001055259";
                    }
                    Map<String, NewBookProfile> bookProfileMap =
                            newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(newClazzBook.getBookId()));
                    NewBookProfile bookProfile;
                    if (MapUtils.isEmpty(bookProfileMap) || (bookProfile = bookProfileMap.get(newClazzBook.getBookId())) == null) {
                        return "SD_10300001055259";
                    }
                    Integer bookStartLevel = bookProfile.getStartClazzLevel();
                    if (bookStartLevel == null) {
                        return "SD_10300001055259";
                    }

                    if (bookStartLevel.compareTo(1) == 0 && clazzLevel.compareTo(2) <= 0) { //一年级起的且三年级及以上的
                        return "SD_10300001253782";
                    }

                    if (bookStartLevel.compareTo(3) == 0 && clazzLevel.compareTo(3) <= 0) { //三年级起的且四年级及以上的
                        return "SD_10300001253782";
                    }
                    return "SD_10300001055259";
                }).orElse(checkStudent != null && checkStudent ? "SD_10300001253782" : "SD_10300001055259");
        Date now = new Date();
        ShortTravelProductConfig config = Optional.ofNullable(chipsContentService.loadShortProductConfig(type))
                .filter(CollectionUtils::isNotEmpty)
                .map(configList -> {
                    List<ShortTravelProductConfig> selectList = configList.stream()
                            .filter(cfg -> StringUtils.isNotBlank(cfg.getBookId()) && cfg.getBookId().equals(bookId))
                            .sorted(Comparator.comparing(ShortTravelProductConfig::getRank))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(selectList)) {
                        return configList.stream().filter(cfg -> StringUtils.isBlank(cfg.getBookId())).findFirst().orElse(null);
                    }

                    for (ShortTravelProductConfig con : selectList) {
                        if (now.before(con.getSellOutEndDate())) {
                            return con;
                        }
                    }
                    return selectList.get(selectList.size() - 1);
                })
                .orElse(null);
        if (config == null) {
            return null;
        }

        ProductInfo info = new ProductInfo();
        info.setCalendarImages(config.getCalendar());
        info.setProductId(config.getProductId());
        info.setVideo(config.getVideo());
        info.setSaleOut(now.after(config.getSellOutEndDate()));
        info.setSurplus(info.isSaleOut() ? 0 : 1);

        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(config.getProductId());
        info.setOriginalPrice(Optional.ofNullable(orderProduct).map(OrderProduct::getOriginalPrice).orElse(BigDecimal.ZERO));
        info.setPrice(Optional.ofNullable(orderProduct).map(OrderProduct::getPrice).orElse(BigDecimal.ZERO));
        info.setProductName(Optional.ofNullable(orderProduct).map(OrderProduct::getName).orElse(""));
        info.setRank(Optional.ofNullable(orderProduct).map(OrderProduct::getAttributes).map(JsonUtils::fromJson).map(ma -> SafeConverter.toInt(ma.get("rank"))).orElse(1));

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(info.getProductId());
        info.setBeginDate(Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date()));
        info.setEndDate(Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date()));
        info.setSellOutDate(DateUtils.addDays(info.getBeginDate(), -config.getEndDay()));
        info.setSellOutEndDate(config.getSellOutEndDate());
        info.setContentImages(config.getContentCards());
        info.setDescription(config.getDescription());
        info.setVideoImage(config.getVideoImage());
        info.setTrialUnitId(config.getTrialUnitId());
        info.setTrialBookId(config.getTrialBookId());
        info.setAdImages(config.getAdImages());
        info.setAdImages2(config.getAdImages2());
        info.setCardImages(config.getCardImages());
        return info;
    }


    @Setter
    @Getter
    private static class ProductInfo {
        private String productId;
        private String productName;
        private Date beginDate;
        private Date endDate;
        private Date sellOutDate;
        private Date sellOutEndDate;
        private List<String> contentImages;
        private List<String> cardImages;
        private List<String> adImages2;
        private String description;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private List<String> calendarImages;
        private List<String> adImages;
        private String video;
        private boolean saleOut;
        private Integer surplus;
        private String videoImage;
        private String trialBookId;
        private String trialUnitId;
        private Integer rank;
    }
}
