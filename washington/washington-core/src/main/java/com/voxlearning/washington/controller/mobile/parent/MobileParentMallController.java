package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.entity.Coin;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.galaxy.service.mall.api.*;
import com.voxlearning.galaxy.service.mall.api.cache.CommodityCacheManager;
import com.voxlearning.galaxy.service.mall.api.constant.CommodityCategory;
import com.voxlearning.galaxy.service.mall.api.constant.CommodityColumn;
import com.voxlearning.galaxy.service.mall.api.constant.OrderStatus;
import com.voxlearning.galaxy.service.mall.api.constant.SendStatus;
import com.voxlearning.galaxy.service.mall.api.data.CommodityDetail;
import com.voxlearning.galaxy.service.mall.api.entity.Commodity;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityOrder;
import com.voxlearning.galaxy.service.mall.api.entity.CommoditySub;
import com.voxlearning.galaxy.service.mall.api.support.CommodityConvert;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.parent.api.MonitorRecruitV2Service;
import com.voxlearning.utopia.service.parent.api.StudyTogetherService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.newkol.KolMonitorStatusRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/06/20
 */
@Controller
@RequestMapping(value = "parentMobile/mall/")
@Slf4j
public class MobileParentMallController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = DPCommodityLoader.class)
    private DPCommodityLoader dpCommodityLoader;
    @ImportService(interfaceClass = DPCommodityService.class)
    private DPCommodityService dpCommodityService;
    @ImportService(interfaceClass = MonitorRecruitV2Service.class)
    private MonitorRecruitV2Service monitorRecruitV2Service;
    @ImportService(interfaceClass = DPCoinLoader.class)
    private DPCoinLoader dpCoinLoader;
    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;
    @ImportService(interfaceClass = DPCommodityOrderLoader.class)
    private DPCommodityOrderLoader dpCommodityOrderLoader;
    @ImportService(interfaceClass = DPCommodityOrderService.class)
    private DPCommodityOrderService dpCommodityOrderService;
    @Inject
    private CommodityBufferLoaderClient commodityBufferLoaderClient;
    @ImportService(interfaceClass = StudyTogetherService.class)
    private StudyTogetherService studyTogetherService;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @RequestMapping(value = "commodity/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage commodityInfo() {
        Long studentId = getRequestLong("sid");
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return MapMessage.errorMessage("获取学生信息错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return MapMessage.errorMessage("学生与家长无关联");
        }
        try {
            List<Commodity> commodities = commodityBufferLoaderClient.getCommodityList()
                    .stream()
                    .filter(commodity -> commodity.getOnSale() && !SafeConverter.toBoolean(commodity.getDisabled()))
                    .collect(Collectors.toList());
            List<Integer> ids = commodities.stream().map(Commodity::getId).collect(Collectors.toList());
            Map<Integer, CommoditySub> commoditySubMap = dpCommodityLoader.loadAllCommoditySub(ids);
            List<CommodityDetail> details = new ArrayList<>();
            commodities.forEach(commodity -> {
                CommoditySub commoditySub = commoditySubMap.get(commodity.getId());
                if (commoditySub != null) {
                    details.add(CommodityConvert.toDetail(commodity, commoditySub));
                }
            });

            Map<String, List<CommodityDetail>> detailMap = details.stream()
                    .filter(detail -> StringUtils.isNotBlank(detail.getColumn()))
                    .collect(Collectors.groupingBy(CommodityDetail::getColumn));
            //精选推荐栏目被移除，现在这个栏目展示的是从班长专享和兑换好礼里面推荐出来的，所以要单独处理
            List<CommodityDetail> recommendCommodities = details.stream().filter(detail -> SafeConverter.toBoolean(detail.getRecommendFlag())).collect(Collectors.toList());
            detailMap.put(CommodityColumn.RECOMMEND.name(), recommendCommodities);
            List<Map<String, Object>> commodityList = new ArrayList<>();
            for (CommodityColumn column : CommodityColumn.values()) {
                if (detailMap.containsKey(column.name())) {
                    Map<String, Object> columnMap = new HashMap<>();
                    columnMap.put("column", column.getCode());
                    columnMap.put("commodities", getCommodityList(column, detailMap.get(column.name())));
                    commodityList.add(columnMap);
                }
            }

            Integer coinCount = 0;
            Coin coin = dpCoinLoader.loadCoin(studentId);
            if (coin != null) {
                coinCount = coin.getTotalCount();
            }

            return MapMessage.successMessage().add("commodities", commodityList)
                    .add("monitorStatus", getMonitorStatus(parent.getId()))
                    .add("applyMonitorFlag", getApplyMonitorFlag(studentId))
                    .add("coinCount", coinCount)
                    .add("studentName", student.fetchRealname());
        } catch (Exception ex) {
            logger.error("获取商品列表失败, pid:{}", parent.getId(), ex);
            return MapMessage.errorMessage("获取商品列表失败");
        }
    }

    @RequestMapping(value = "commodity/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        Integer commodityId = getRequestInt("commodityId");
        try {
            Commodity commodity = commodityBufferLoaderClient.getCommodity(commodityId);
            if (commodity == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }
            CommoditySub commoditySub = dpCommodityLoader.loadCommoditySub(commodityId);
            if (commoditySub == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }
            Map<String, Object> commodityMap = getCommodityMap(CommodityConvert.toDetail(commodity, commoditySub), null, Boolean.TRUE);
            boolean hasBuy = CommodityCacheManager.INSTANCE.hasBuyCommodity(studentId, commodityId);
            commodityMap.put("hasBuy", hasBuy);

            Integer monitorStatus = 999;
            if (parent != null) {
                monitorStatus = getMonitorStatus(parent.getId());
            }

            Integer coinCount = 0;
            Coin coin = dpCoinLoader.loadCoin(studentId);
            if (coin != null) {
                coinCount = coin.getTotalCount();
            }
            return MapMessage.successMessage()
                    .add("commodity", commodityMap)
                    .add("coinCount", coinCount)
                    .add("monitorStatus", monitorStatus)
                    .add("applyMonitorFlag", getApplyMonitorFlag(studentId));
        } catch (Exception ex) {
            logger.error("获取商品详情失败， commodityId:{}, sid:{}", commodityId, studentId, ex);
            return MapMessage.errorMessage("获取商品详情失败");
        }
    }

    @RequestMapping(value = "receive/info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage receiveInfo() {
        Long studentId = getRequestLong("sid");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("获取学生信息错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        MapMessage message = MapMessage.successMessage();
        Integer commodityId = getRequestInt("commodityId");
        Integer countyCode = getRequestInt("countyCode");
        try {
            Commodity commodity = commodityBufferLoaderClient.getCommodity(commodityId);
            if (commodity == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }
            CommoditySub commoditySub = dpCommodityLoader.loadCommoditySub(commodityId);
            if (commoditySub == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }

            CommodityCategory category = CommodityCategory.parse(commodity.getCategory());

            if (category == null) {
                return MapMessage.errorMessage("商品信息错误");
            }

            if (category.getLevel() != 1) {
                message.add("remark", "此商品兑换成功后无法退货");
            }

            Integer monitorStatus = getMonitorStatus(parent.getId());
            Map<String, Object> commodityMap = getCommodityInfo(CommodityConvert.toDetail(commodity, commoditySub), monitorStatus);
            commodityMap.put("category", category.getLevel());
            message.add("commodity", commodityMap);

            Map<String, Object> receiveInfo = getReceiveInfo(commodity, parent.getId(), studentDetail, countyCode);
            message.add("receiveInfo", receiveInfo);

            return message;
        } catch (Exception ex) {
            logger.error("获取商品详情失败， commodityId:{}, pid:{}", commodityId, parent.getId(), ex);
            return MapMessage.errorMessage("获取商品详情失败");
        }
    }

    @RequestMapping(value = "createOrder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrder() {
        Long studentId = getRequestLong("sid");
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return MapMessage.errorMessage("获取学生信息错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        Integer commodityId = getRequestInt("commodityId");
        Integer countyCode = getRequestInt("countyCode");
        try {
            Commodity commodity = commodityBufferLoaderClient.getCommodity(commodityId);
            if (commodity == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }
            CommoditySub commoditySub = dpCommodityLoader.loadCommoditySub(commodityId);
            if (commoditySub == null) {
                return MapMessage.errorMessage("商品不存在，请刷新重试");
            }

            boolean hasBuyCommodity = CommodityCacheManager.INSTANCE.hasBuyCommodity(studentId, commodityId);
            if (hasBuyCommodity && !commodity.getAllowRepeat()) {
                return MapMessage.errorMessage("您已经兑换过此商品，此商品不能重复兑换");
            }
            MapMessage mapMessage = validateOrderInfo(CommodityConvert.toDetail(commodity, commoditySub), parent.getId(), countyCode);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
            CommodityOrder order = (CommodityOrder) mapMessage.get("order");
            Integer monitorStatus = getMonitorStatus(parent.getId());
            MapMessage orderMessage = createOrder(order, commodity, studentId, monitorStatus);
            if (!orderMessage.isSuccess()) {
                return orderMessage;
            }

            //修改库存和已售数量
            commoditySub.setStock(commoditySub.getStock() - 1);
            commoditySub.setSoldNum(SafeConverter.toInt(commoditySub.getSoldNum()) + 1);
            dpCommodityService.upsertCommoditySub(commoditySub);

            Integer coinType;
            if (RuntimeMode.current().le(Mode.TEST)) {
                coinType = 39;
            } else {
                coinType = 24;
            }
            CoinHistory history = new CoinHistoryBuilder().withType(coinType)
                    .withUserId(studentId)
                    .withCount(monitorStatus > 0 ? commodity.getMonitorCoinS() : commodity.getOrdinaryCoinS())
                    .withOperator(parent.fetchRealname())
                    .build();
            MapMessage coinMessage = dpCoinService.changeCoin(history);
            if (!coinMessage.isSuccess()) {
                return MapMessage.errorMessage("创建订单异常");
            } else {
                CommodityCacheManager.INSTANCE.recordUserBuyCommodity(studentId, commodityId);
                return MapMessage.successMessage().add("orderId", order.getId());
            }
        } catch (Exception ex) {
            logger.error("创建订单失败，commodityId:{}", commodityId, ex);
            return MapMessage.errorMessage("创建订单失败");
        }
    }

    @RequestMapping(value = "exchangeList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage exchangeList() {
        Long studentId = getRequestLong("sid");
        Student student = studentLoaderClient.loadStudent(studentId);
        if (student == null) {
            return MapMessage.errorMessage("获取学生信息错误");
        }
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        List<CommodityOrder> commodityOrders = dpCommodityOrderLoader.loadByUserId(studentId)
                .stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("orderList", getOrderList(commodityOrders));
    }

    @RequestMapping(value = "orderDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orderDetail() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String orderId = getRequestString("orderId");
        CommodityOrder order = dpCommodityOrderLoader.loadById(orderId);
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        return MapMessage.successMessage()
                .add("sendStatus", getSendStatus(order))
                .add("category", order.getCommodityCategory().getLevel())
                .add("order", getOrderInfo(order))
                .add("receiveInfo", getOrderReceiveInfo(order));
    }

    private Map<String, Object> getOrderInfo(CommodityOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("commodityId", order.getCommodityId());
        String remark = "我们将在一周内尽快给您处理，如有问题请直接联系客服 400-160-1717";
        if (order.getCommodityCategory().getLevel() == 1) {
            remark = "如有问题请直接联系客服 400-160-1717";
        }
        map.put("orderStatus", getOrderStatus(order));
        map.put("remark", remark);
        map.put("image", getCommodityImage(order.getCommodityId()));
        map.put("commodityName", order.getCommodityName());
        map.put("price", order.getCoin());
        map.put("createDate", DateUtils.dateToString(order.getCreateTime(), "yyyy-MM-dd"));

        return map;
    }

    private Map<String, Object> getOrderReceiveInfo(CommodityOrder order) {
        Map<String, Object> map = new HashMap<>();
        if (order.getCommodityCategory().getLevel() == 1) {
            map.put("logisticsCode", SafeConverter.toString(order.getLogisticsCode(), ""));
            map.put("sendWay", order.getSendWay() == null ? "" : order.getSendWay().getDesc());
            map.put("userName", order.getUserName());
            ExRegion exRegion = raikouSystem.loadRegion(order.getCountyCode());
            if (exRegion != null) {
                String address = exRegion.getProvinceName() + exRegion.getCityName() + exRegion.getCountyName() + order.getAddress();
                map.put("address", address);
            }
        } else if (order.getCommodityCategory().getLevel() == 2) {
            map.put("studentName", order.getUserName());
            map.put("age", order.getAge());
            map.put("clazzLevel", order.getClazzLevel());
        }
        map.put("phone", SensitiveLib.decodeMobile(order.getPhone()));
        return map;
    }

    private String getOrderStatus(CommodityOrder order) {
        String orderStatus;
        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            orderStatus = "退币成功";
        } else {
            if (order.getCommodityCategory().getLevel() != 1) {
                if (order.getSendStatus() == SendStatus.ALREADY_USED) {
                    orderStatus = "兑换成功，已使用";
                } else {
                    orderStatus = "兑换成功，待处理";
                }
            } else {
                if (order.getSendStatus() == SendStatus.SEND_PENDING) {
                    orderStatus = "兑换成功，预计两周内发货";
                } else {
                    orderStatus = "兑换成功，已发货";
                }
            }
        }
        return orderStatus;
    }

    /**
     * 订单状态
     * 1-虚拟商品兑换成功(没有收货信息)
     * 2-实物商品兑换成功(有收货信息)
     * 3-实物商品已寄送(有收货信息和运单信息)
     */
    private Integer getSendStatus(CommodityOrder order) {
        Integer sendStatus = 1;
        if (order.getCommodityCategory().getLevel() == 1) {
            if (order.getSendStatus() == SendStatus.SEND_PENDING) {
                sendStatus = 2;
            } else {
                sendStatus = 3;
            }
        }

        return sendStatus;
    }

    private List<Map<String, Object>> getOrderList(List<CommodityOrder> commodityOrders) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(commodityOrders)) {
            for (CommodityOrder order : commodityOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getId());
                map.put("commodityName", order.getCommodityName());
                map.put("image", getCommodityImage(order.getCommodityId()));
                map.put("date", DateUtils.dateToString(order.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                map.put("orderStatus", getOrderStatus(order));
                map.put("commodityId", order.getCommodityId());
                list.add(map);
            }
        }
        return list;
    }

    private String getCommodityImage(Integer commodityId) {
        Commodity commodity = commodityBufferLoaderClient.getCommodity(commodityId);
        String image = "";
        if (commodity != null) {
            if (CollectionUtils.isNotEmpty(commodity.getImages())) {
                image = getOssImgUrl(commodity.getImages().get(0));
            }
        }
        return image;
    }

    private MapMessage createOrder(CommodityOrder order, Commodity commodity, Long studentId, Integer monitorStatus) {
        Integer needCoinCount = monitorStatus > 0 ? commodity.getMonitorCoinS() : commodity.getOrdinaryCoinS();
        Integer totalCoinCount = 0;
        Coin coin = dpCoinLoader.loadCoin(studentId);
        if (coin != null) {
            totalCoinCount = coin.getTotalCount();
        }
        if (totalCoinCount < needCoinCount) {
            return MapMessage.errorMessage("学习币数量不够");
        }

        List<String> userTypes = commodity.getUserTypes();
        if (userTypes.size() == 1 && userTypes.contains("MONITOR") && monitorStatus < 0) {
            return MapMessage.errorMessage("抱歉，您不是班长，不能兑换此商品");
        }

        order.setUserId(studentId);
        order.setOrderStatus(OrderStatus.PAID);
        order.setCommodityId(commodity.getId());
        order.setCommodityName(commodity.getName());
        CommodityCategory category = CommodityCategory.parse(commodity.getCategory());
        if (category == null) {
            return MapMessage.errorMessage("商品分类错误");
        }
        order.setCommodityCategory(category);
        order.setPurchase(commodity.getPurchase());
        order.setDispatchPrice(commodity.getDispatchPrice());
        order.setCoin(needCoinCount);
        if (category.getLevel() == 1) {
            order.setSendStatus(SendStatus.SEND_PENDING);
        } else {
            order.setSendStatus(SendStatus.NOT_USED);
        }

        CommodityOrder upsert = atomicLockManager.wrapAtomic(dpCommodityOrderService)
                .keyPrefix("createOrder")
                .keys(studentId)
                .proxy()
                .saveCommodityOrder(order, "");
        if (upsert == null) {
            return MapMessage.errorMessage("创建订单失败");
        }
        return MapMessage.successMessage();
    }

    private MapMessage validateOrderInfo(CommodityDetail commodity, Long parentId, Integer countyCode) {
        if (SafeConverter.toInt(commodity.getStock()) == 0) {
            return MapMessage.errorMessage("该商品已售罄，请选择其他商品吧");
        }
        if (!SafeConverter.toBoolean(commodity.getOnSale())) {
            return MapMessage.errorMessage("该商品已下架，请选择其他商品吧");
        }
        CommodityCategory category = CommodityCategory.parse(commodity.getCategory());
        if (category == null) {
            return MapMessage.errorMessage("商品信息错误");
        }
        CommodityOrder order = new CommodityOrder();
        order.setId(CommodityOrder.generateId());
        if (category.getLevel() == 1) {
            ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parentId);
            if (parentExtAttribute == null || CollectionUtils.isEmpty(parentExtAttribute.getAddress())) {
                return MapMessage.errorMessage("收货人信息错误");
            }
            ParentExtAttribute.Address defaultAddress;
            if (countyCode > 0) {
                defaultAddress = parentExtAttribute.getAddress()
                        .stream()
                        .filter(e -> countyCode.equals(e.getCountyCode()))
                        .findFirst()
                        .orElse(null);
            } else {
                defaultAddress = parentExtAttribute.getAddress()
                        .stream()
                        .filter(ParentExtAttribute.Address::getDefaultAddress)
                        .findFirst()
                        .orElse(null);
            }

            if (defaultAddress == null) {
                return MapMessage.errorMessage("收货人信息错误");
            }
            order.setUserName(defaultAddress.getConsigneeName());
            order.setPhone(SensitiveLib.encodeMobile(defaultAddress.getPhoneNumber()));
            order.setCountyCode(defaultAddress.getCountyCode());
            order.setAddress(defaultAddress.getAddressDetail());
        } else if (category.getLevel() == 2) {
            String studentName = getRequestString("studentName");
            if (StringUtils.isBlank(studentName)) {
                return MapMessage.errorMessage("孩子姓名不能为空");
            }
            String phone = getRequestString("phone");
            if (StringUtils.isBlank(phone)) {
                return MapMessage.errorMessage("家长电话不能为空");
            }
            Integer age = getRequestInt("age");
            if (age == 0) {
                return MapMessage.errorMessage("孩子年龄错误");
            }
            Integer clazzLevel = getRequestInt("clazzLevel");
            List<Integer> clazzLevels = new ArrayList<>();
            for (ClazzLevel clazzLevel1 : ClazzLevel.values()) {
                clazzLevels.add(clazzLevel1.getLevel());
            }
            if (!clazzLevels.contains(clazzLevel)) {
                return MapMessage.errorMessage("年级错误");
            }
            order.setUserName(studentName);
            order.setPhone(SensitiveLib.encodeMobile(phone));
            order.setAge(age);
            order.setClazzLevel(clazzLevel);
        } else {
            String phone = getRequestString("phone");
            if (StringUtils.isBlank(phone)) {
                return MapMessage.errorMessage("家长电话不能为空");
            }
            order.setPhone(SensitiveLib.encodeMobile(phone));
        }

        return MapMessage.successMessage().add("order", order);
    }

    /**
     * 实物商品需要获取用户默认收货信息
     */
    private Map<String, Object> getReceiveInfo(Commodity commodity, Long parentId, StudentDetail studentDetail, Integer countyCode) {
        Map<String, Object> map = new HashMap<>();
        CommodityCategory category = CommodityCategory.parse(commodity.getCategory());
        if (category != null) {
            if (category.getLevel() == 1) {
                ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parentId);
                if (parentExtAttribute != null && CollectionUtils.isNotEmpty(parentExtAttribute.getAddress())) {
                    ParentExtAttribute.Address address;
                    if (countyCode > 0) {
                        address = parentExtAttribute.getAddress()
                                .stream()
                                .filter(e -> countyCode.equals(e.getCountyCode()))
                                .findFirst()
                                .orElse(null);
                    } else {
                        address = parentExtAttribute.getAddress()
                                .stream()
                                .filter(ParentExtAttribute.Address::getDefaultAddress)
                                .findFirst()
                                .orElse(null);
                    }

                    if (address != null) {
                        map.put("phone", address.getPhoneNumber());
                        map.put("consigneeName", address.getConsigneeName());
                        ExRegion exRegion = raikouSystem.loadRegion(address.getCountyCode());
                        if (exRegion != null) {
                            String detailAddress = exRegion.getProvinceName() + exRegion.getCityName() + exRegion.getCountyName() + address.getAddressDetail();
                            map.put("address", detailAddress);
                        }
                    }
                }
            } else if (category.getLevel() == 2) {
                map.put("studentName", studentDetail.fetchRealname());
                String phone = sensitiveUserDataServiceClient.showUserMobile(parentId, "学习币商城显示家长手机号", "");
                map.put("phone", phone);
                map.put("age", getAge(studentDetail));
                Clazz clazz = studentDetail.getClazz();
                map.put("clazzLevel", clazz != null ? clazz.getClassLevel() : 0);
            } else {
                String phone = sensitiveUserDataServiceClient.showUserMobile(parentId, "学习币商城显示家长手机号", "");
                map.put("phone", phone);
            }
        }
        return map;
    }

    private Integer getAge(StudentDetail studentDetail) {
        UserProfile profile = studentDetail.getProfile();
        int birthYear = SafeConverter.toInt(profile.getYear());
        if (birthYear != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int nowYear = calendar.get(Calendar.YEAR);
            return nowYear - birthYear;
        }
        return 0;
    }

    /**
     * 兑换页的商品信息
     *
     * @param detail        商品
     * @param monitorStatus 班长状态
     */
    private Map<String, Object> getCommodityInfo(CommodityDetail detail, Integer monitorStatus) {
        Map<String, Object> map = new HashMap<>();
        String image = "";
        if (CollectionUtils.isNotEmpty(detail.getImages())) {
            image = getOssImgUrl(detail.getImages().get(0));
        }
        map.put("commodityId", detail.getId());
        map.put("image", image);
        map.put("name", detail.getName());
        map.put("price", monitorStatus > 0 ? detail.getMonitorCoinS() : detail.getOrdinaryCoinS());
        map.put("stock", SafeConverter.toInt(detail.getStock()));
        map.put("onSale", SafeConverter.toBoolean(detail.getOnSale()));
        map.put("allowRepeat", SafeConverter.toBoolean(detail.getAllowRepeat()));
        return map;
    }

    /**
     * 首页商品list
     */
    private List<Map<String, Object>> getCommodityList(CommodityColumn column, List<CommodityDetail> details) {
        List<Map<String, Object>> list;
        if (CollectionUtils.isEmpty(details)) {
            list = Collections.emptyList();
        } else {
            list = new ArrayList<>();
            if (column == CommodityColumn.MONITOR) {
                details = details.stream().sorted((o1, o2) -> o2.getOrder().compareTo(o1.getOrder())).limit(3).collect(Collectors.toList());
            } else if (column == CommodityColumn.RECOMMEND) {
                //精选推荐展示时间过滤
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                Integer weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                //这一步是为了第六个商品有并列order时，随机去一个而不是固定的一个
                Collections.shuffle(details);
                details = details.stream().filter(detail -> detail.getRecommendStartDate() == null || detail.getRecommendStartDate().before(new Date()))
                        .filter(detail -> detail.getRecommendEndDate() == null || detail.getRecommendEndDate().after(new Date()))
                        .filter(detail -> CollectionUtils.isEmpty(detail.getWeekDayList()) || detail.getWeekDayList().contains(weekDay))
                        .sorted((o1, o2) -> o2.getRecommendOrder().compareTo(o1.getRecommendOrder()))
                        .limit(6)
                        .collect(Collectors.toList());
            }
            for (CommodityDetail detail : details) {
                list.add(getCommodityMap(detail, column, false));
            }
        }
        return list;
    }

    /**
     * 生成单个商品的信息
     *
     * @param detail     商品
     * @param needDetail 详细信息
     */
    private Map<String, Object> getCommodityMap(CommodityDetail detail, CommodityColumn column, Boolean needDetail) {
        Map<String, Object> map = new HashMap<>();
        Integer commodityId = detail.getId();
        String commodityName = detail.getName();
        Integer soldNum = SafeConverter.toInt(detail.getSoldNum());
        Integer monitorPrice = detail.getMonitorCoinS();
        Integer ordinaryPrice = detail.getOrdinaryCoinS();

        map.put("commodityId", commodityId);
        map.put("commodityName", commodityName);
        map.put("soldNum", SafeConverter.toInt(soldNum));
        map.put("monitorPrice", monitorPrice);
        map.put("ordinaryPrice", ordinaryPrice);
        map.put("stock", SafeConverter.toInt(detail.getStock()));
        map.put("allowRepeat", detail.getAllowRepeat());
        map.put("onSale", SafeConverter.toBoolean(detail.getOnSale()));

        List<String> userTypes = detail.getUserTypes();
        boolean needBeMonitor = userTypes.size() == 1 && userTypes.contains("MONITOR");
        map.put("needBeMonitor", needBeMonitor);

        List<String> images = detail.getImages();
        if (needDetail) {
            List<String> detailImages = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(images)) {
                for (int i = 1; i < images.size(); i++) {
                    detailImages.add(getOssImgUrl(images.get(i)));
                }
            }
            map.put("images", detailImages);
            map.put("description", detail.getDescription());
        } else {
            String image = "";
            if (column == CommodityColumn.RECOMMEND) {
                image = getOssImgUrl(detail.getRecommendImage());
            } else {
                if (CollectionUtils.isNotEmpty(images)) {
                    image = getOssImgUrl(images.get(0));
                }
            }
            map.put("image", image);
        }
        return map;
    }

    private boolean getApplyMonitorFlag(Long studentId) {
        List<StudyGroup> studyGroups = studyTogetherService.loadStudentActiveLessonGroups(studentId);
        List<Long> lessonIds = studyGroups.stream().map(t -> SafeConverter.toLong(t.getLessonId())).collect(Collectors.toList());
        boolean applyMonitorFlag = false;
        Date now = new Date();
        Map<Long, StudyLesson> studyLessons = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLessons(lessonIds);
        Set<StudyLesson> lessonSet = studyLessons.values()
                .stream()
                .filter(lesson -> !lesson.isClosed())
                .filter(lesson -> lesson.safeGetActiveType() != 3
                        && SafeConverter.toInt(lesson.getParent().getType()) != 1
                        && !lesson.safeIsDirectActive())
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(lessonSet)) {
            return false;
        }
        for (Long lessonId : lessonIds) {
            StudyLesson lesson = studyLessons.get(lessonId);
            if (lesson != null && now.after(lesson.getShowDate()) && now.before(lesson.getCloseDate())) {
                applyMonitorFlag = true;
                break;
            }
        }
        return applyMonitorFlag;
    }

    private String getOssImgUrl(String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return "";
        }
        return ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + relativeUrl;
    }

    private Integer getMonitorStatus(Long parentId) {
        Integer monitorStatus = 0;
        KolMonitorStatusRecord statusRecord = monitorRecruitV2Service.getParentLatestMonitorRecord(parentId);
        if (statusRecord != null) {
            monitorStatus = SafeConverter.toInt(statusRecord.getLevel());
        }
        return monitorStatus;
    }
}
