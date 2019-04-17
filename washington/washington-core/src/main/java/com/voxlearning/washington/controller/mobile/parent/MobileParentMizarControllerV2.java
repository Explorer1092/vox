package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.payment.PaymentRequest;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "/mizar/v2")
@Slf4j
public class MobileParentMizarControllerV2  extends AbstractMobileController {

    @Inject
    private MizarLoaderClient mizarLoaderClient;
    @Inject
    private MizarServiceClient mizarServiceClient;
    /**
     * 商品详情
     * @return
     */
    @RequestMapping(value = "goods/detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage detail() {
        addCrossHeaderForXdomain();
        String gId = getRequestString("gId");
        if (StringUtils.isBlank(gId)) {
            return MapMessage.errorMessage("gId不能为空");
        }
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(gId);
        if (goods == null) {
            return MapMessage.errorMessage("课程不存在");
        }
        // 需要显示的字段值
        List<Map> inputList = new LinkedList<>();

        // 年龄段
        List<Integer> targetList = MizarShopGoods.featureRange(goods.getTarget());
        if (CollectionUtils.isNotEmpty(targetList)) {
            if (targetList.size() == 1) {
                inputList.add(MapUtils.m("inputType", "tel", "placeholder", "请输入年龄", "name", "age", "errorText", "年龄必须为数字"));
            } else {
                inputList.add(MapUtils.m("inputType", "select", "placeholder", "请选择年龄", "name", "age", "list",
                        targetList.stream().map(i -> MapUtils.m("value", i, "text", i+"岁")).collect(Collectors.toList())));
            }
        }
        // 地址
        if (goods.featureRequireAddress() == 1) {
            inputList.add(MapUtils.m("inputType", "address", "placeholder", "请输入收货地址", "name", "address"));
        }
        // 年级
        List<Integer> clazzLevelList = MizarShopGoods.featureRange(goods.getClazzLevel());
        if (CollectionUtils.isNotEmpty(clazzLevelList)) {
            if (clazzLevelList.size() == 1) {
                inputList.add(MapUtils.m("inputType", "tel", "placeholder", "请输入年级", "name", "clazzLevel", "errorText", "年级必须为数字"));
            } else {
                inputList.add(MapUtils.m("inputType", "select", "placeholder", "请选择年级", "name", "clazzLevel", "list",
                        clazzLevelList.stream().map(i -> MapUtils.m("value", i, "text", i+"年级")).collect(Collectors.toList())));
            }
        }
        // 学校
        if (goods.featureRequireSchool() == 1) {
            inputList.add(MapUtils.m("inputType", "text", "placeholder", "请输入学校", "name", "school"));
        }
        // 校区
        String schoolAreas = goods.getSchoolAreas();
        if (StringUtils.isNotBlank(schoolAreas)) {
            inputList.add(MapUtils.m("inputType", "select", "placeholder", "请选择校区", "name","schoolArea", "list",
                    Arrays.stream(schoolAreas.split(",")).map(s -> MapUtils.m("value", s, "text", s)).collect(Collectors.toList())));
        }

        // 姓名
        if (goods.featureRequireStudentName() == 1) {
            inputList.add(MapUtils.m("inputType", "text", "placeholder", "请输入孩子姓名", "name", "studentName"));
        }
        // 地区
        if (goods.featureRequireRegion() == 1) {
            inputList.add(MapUtils.m("inputType", "city", "placeholder", "请输入地区", "name", "regionId"));
        }

        Long daySellCount = mizarLoaderClient.loadDaySellCount(gId);
        Long sellCount = mizarLoaderClient.loadSellCount(gId);
        long surplus = (goods.getTotalLimit() != null && goods.getTotalLimit() >= 0) ? Math.max(goods.getTotalLimit() - sellCount, 0) : -1;
        long daySurplus =  (goods.getDayLimit() != null &&   goods.getDayLimit() >= 0)? Math.max(goods.getDayLimit() - daySellCount , 0) : -1;
        daySurplus = surplus < 0 ? daySurplus : Math.min(daySurplus, surplus);
        boolean offline = goods.getStatus() != MizarGoodsStatus.ONLINE ||
                daySurplus == 0 || surplus == 0;

        Map<String, Object> data = MapUtils.m("gId", goods.getId(),
                "goodsName", goods.getGoodsName(),
                "dayLimit", goods.getDayLimit(),
                "totalLimit", goods.getTotalLimit(),
                "sellCount", sellCount,
                "daySellCount", daySellCount,
                "surplus", surplus,
                "daySurplus", daySurplus,
                "requireAddress", goods.featureRequireAddress(),
                "buttonText", goods.getButtonText(),
                "buttonColor", goods.getButtonColor(),
                "buttonTextColor", goods.getButtonTextColor(),
                "successText", goods.getSuccessText(),
                "bannerPhoto", goods.getBannerPhoto(),
                "topImage", goods.getTopImage(),
                "detail", goods.getDetail(),
                "offlineText", goods.getOfflineText(),
                "offline", offline,
                "inputBGColor", goods.getInputBGColor(),
                "price", goods.getPrice() == null ? 0 :goods.getPrice(),
                "inputList", inputList);
        return MapMessage.successMessage().add("data", data);
    }

    /**
     * 报名
     * @return
     */
    @RequestMapping(value = "reserve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reserve() {
        addCrossHeaderForXdomain();
        User parent = currentUser();
        if (parent == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        String gId = getRequestString("gId");
        String mobile = getRequestString("mobile");
        String address = getRequestString("address");
        String school = getRequestString("school");
        String schoolArea = getRequestString("schoolArea");
        int clazzLevel = getRequestInt("clazzLevel", -1);
        String orderReferer = getRequestString("orderReferer");
        String studentName = getRequestString("studentName");
        int regionId = getRequestInt("regionId");
        int age = getRequestInt("age", -1);
        if (StringUtils.isAnyBlank(gId, mobile)) {
            return MapMessage.errorMessage("参数错误");
        }
        Long parentId = parent.getId();
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(gId);
        if (goods.getStatus() != MizarGoodsStatus.ONLINE) {
            return MapMessage.errorMessage("课程未上线");
        }
        if (goods.getTotalLimit() != null && goods.getTotalLimit() >= 0 && mizarLoaderClient.loadSellCount(gId) >= goods.getTotalLimit()) {
            return MapMessage.errorMessage("名额已满");
        }
        if (goods.getDayLimit() != null && goods.getDayLimit() >=0 && mizarLoaderClient.loadDaySellCount(gId) >= goods.getDayLimit()) {
            return MapMessage.errorMessage("今日名额已满");
        }
        UserOrder userOrder = userOrderLoaderClient.loadUserOrderList(parentId).stream().filter(o -> Objects.equals(o.getProductId(), goods.getId())).findFirst().orElse(null);
        if(Objects.nonNull(userOrder) && userOrder.getPaymentStatus() == PaymentStatus.Paid){
            return MapMessage.successMessage("重复报名").add("repetition", true);
        }
        String orderId;
        // 创建订单
        if (userOrder == null) {
            MapMessage orderMessage = userOrderServiceClient.getUserOrderService().createBusinessActivityOrder(
                    parentId,
                    "YiQiXueDiversion",
                    goods.getId(),
                    BigDecimal.valueOf(goods.getPrice()),
                    goods.getGoodsName(),
                    orderReferer,
                    getRequestString("ref"),
                    "mizar");
            if (!orderMessage.isSuccess()) {
                return orderMessage;
            }
            orderId = SafeConverter.toString(orderMessage.get("orderId"));
        } else {
            orderId = userOrder.genUserOrderId();
        }

        MizarReserveRecord reserveRecord = mizarLoaderClient.loadShopReserveByParentId(parentId).stream().filter(r -> r.getShopGoodsId().equals(gId)).findFirst().orElse(null);
        // 存报名信息
        if (reserveRecord == null) {
            reserveRecord = new MizarReserveRecord();
        }
        reserveRecord.setParentId(parentId);
        reserveRecord.setShopGoodsId(gId);
        reserveRecord.setShopId(goods.getShopId());
        reserveRecord.setMobile(SensitiveLib.encodeMobile(mobile));
        reserveRecord.setOrderId(orderId);
        reserveRecord.setStatus(goods.getPrice() <= 0 ? MizarReserveRecord.Status.Payment : MizarReserveRecord.Status.New);
        reserveRecord.setAddress(address);
        reserveRecord.setAge(age);
        reserveRecord.setClazzLevel(clazzLevel);
        reserveRecord.setSchool(school);
        reserveRecord.setSchoolArea(schoolArea);
        reserveRecord.setStudentName(studentName);
        if (regionId > 0) {
            reserveRecord.setRegionId(regionId);
        }
        MapMessage mapMessage = atomicLockManager.wrapAtomic(mizarServiceClient)
                .keyPrefix("MIZAR_RESERVE")
                .keys(parentId, goods.getShopId())
                .proxy()
                .saveMizarReserve(reserveRecord);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        if (goods.getPrice() <= 0) {
            // 支付
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPayAmount(BigDecimal.ZERO);
            paymentRequest.setPayUser(parent.getId());
            paymentRequest.setTradeNumber(SafeConverter.toString(orderId));
            PaymentCallbackContext paymentCallbackContext = buildPaymentCallbackContext(paymentRequest);
            businessUserOrderServiceClient.processUserOrderPayment(paymentCallbackContext);
            return MapMessage.successMessage();
        }
        return MapMessage.successMessage().set("orderId", orderId);
    }
}
