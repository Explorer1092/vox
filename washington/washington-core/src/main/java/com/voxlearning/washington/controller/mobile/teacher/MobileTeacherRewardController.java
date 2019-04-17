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

package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.RewardProductPriceUnit;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.mapper.LoadRewardProductContext;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 弃用,改用MobileUserRewardCenterController 里的方法.h5路伟会用新的地址,后续可干掉.
 * Created by jiangpeng on 16/4/18.
 */
@Deprecated
@Controller
@RequestMapping(value = "/teacherMobile/giftMall")
@Slf4j
public class MobileTeacherRewardController extends AbstractMobileTeacherController {

    final static String CHECK_RESULT_TYPE = "status";

    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    @Inject private NewRewardServiceClient newRewardServiceClient;

    @RequestMapping(value = "/integral/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getIntegral() {

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());

        return MapMessage.successMessage().add("integral", getIntegral(teacherDetail));


    }

    private Long getIntegral(TeacherDetail teacherDetail) {
        UserIntegral userIntegral = teacherDetail.getUserIntegral();
        if (userIntegral != null)
            return userIntegral.getUsable();
        else
            return 0L;
    }


    @RequestMapping(value = "/category/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage allCategory() {

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        //老师目前只取实物商品
        List<RewardCategory> allCategories = new ArrayList<>(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_SHIWU, teacher.fetchUserType()));
        allCategories.stream().sorted((o1, o2) -> Integer.compare(o1.getDisplayOrder(), o2.getDisplayOrder()));
        List<Map<String, Object>> categoryList = new ArrayList<>();
        categoryList.add(new LinkedHashMap<String, Object>() {
            {
                put("id", 0);
            }

            {
                put("name", "全部");
            }
        });
        allCategories.stream().forEach(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getCategoryName());
            categoryList.add(map);
        });
        String unit = fetchUnit(teacher);
        
        return MapMessage.successMessage().add("categoryList", categoryList).add("integral_text", unit);

    }

    @RequestMapping(value = "/products/get_by_category.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadProducts() {

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        Long categoryId = getRequestLong("categoryId", 0L);


        int pageNum = getRequestInt("pageNum", 0);
        int pageSize = getRequestInt("pageSize", 21);

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());

        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = categoryId;
        context.twoLevelTagId = 0L;
        context.orderBy = "teacherOrderValue";
        context.upDown = "down";
        context.pageNumber = pageNum;
        context.pageSize = pageSize;
        context.canExchangeFlag = false;
        context.loadPage = "all";

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(teacherDetail, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                null);
        pagination.getContent().forEach(t -> {
            t.setImage(combineCdbUrl(t.getImage()));
            t.getImages().forEach(p -> {
                p.setLocation(combineCdbUrl(p.getLocation()));
            });
        });
        List<RewardProductDetail> list = pagination.getContent().stream().sorted((a, b) -> {
            int result = a.getDiscountPrice().compareTo(b.getDiscountPrice());
            if (result == 0)
                return a.getCreateDatetime().compareTo(b.getCreateDatetime());
            else
                return result;
        }).collect(Collectors.toList());

        return MapMessage.successMessage().add("productList", list);
    }


    @RequestMapping(value = "/product/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage productDetail() {

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());


        Long productId = getRequestLong("productId");
        RewardProductDetail rewardProductDetail = newRewardLoaderClient.generateRewardProductDetail(teacherDetail, productId);
        if (rewardProductDetail == null) {
            return MapMessage.errorMessage("错误商品");
        }

        RewardSku sku = getProductSku(rewardProductDetail.getId());
        if (sku == null)
            return MapMessage.errorMessage("错误商品");
        else{
            rewardProductDetail.addSku(sku);
        }

        rewardProductDetail.setSellAbleCount(sku.getInventorySellable());
        MapMessage resultMap = MapMessage.successMessage();
        MapMessage checkMessage = checkCanBuy(teacherDetail, rewardProductDetail, 1, sku);
        Map<String, Object> statusMap = new LinkedHashMap<>();
        if (!checkMessage.isSuccess()) {
            String msg = checkMessage.getInfo();
            String status = (String) checkMessage.get(CHECK_RESULT_TYPE);
            statusMap.put("message", msg);
            statusMap.put("status", status);
        } else {
            statusMap.put("message", "立即兑换");
            statusMap.put("status", "ok");
        }
        resultMap.add("productStatus", statusMap);
        rewardProductDetail.setImage(combineCdbUrl(rewardProductDetail.getImage()));
        rewardProductDetail.getImages().stream().forEach(t -> {
            t.setLocation(combineCdbUrl(t.getLocation()));
        });
        resultMap.add("productDetail", rewardProductDetail);
        return resultMap;
    }

    /**
     * 此处有坑,虽然有商品sku概念,但需求方并不设置多个sku,而是统一设置一个随机.在此只取第一个sku,作为默认sku.
     *
     * @return RewardSku
     */
    private RewardSku getProductSku(Long productId) {
        List<RewardSku> skus = rewardLoaderClient.loadProductSku(productId);
        if (CollectionUtils.isEmpty(skus)) {
            return null;
        }
        return skus.get(0);
    }

    /**
     * 兑换的时候,由于坑的存在,只取第一个ｓｋｕ,所以只需要productId.
     *
     * @return
     */
    @RequestMapping(value = "/product/exchage.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exchangeProduct() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        Long productId = getRequestLong("productId");
        Integer quantity = getRequestInt("quantity");
        if (quantity <= 0) {
            return MapMessage.errorMessage("请输入正确的奖品数量");
        }
        RewardProductDetail productDetail = newRewardLoaderClient.generateRewardProductDetail(teacherDetail, productId);
        if (productDetail == null || !productDetail.getOnline()) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }

        RewardSku sku = getProductSku(productDetail.getId());
        if (sku == null)
            return MapMessage.errorMessage("错误商品");
        else{
            productDetail.addSku(sku);
        }

        MapMessage checkMesage = checkCanBuy(teacherDetail, productDetail, quantity, sku);
        if (!checkMesage.isSuccess()) {
            return checkMesage;
        }
        MapMessage message = newRewardServiceClient.createRewardOrder(teacherDetail, productDetail, sku, quantity, null, RewardOrder.Source.app, null);
        //TODO 这是干啥的 需要么?
        if (message.isSuccess()) {
            //记录老师点亮标签
            if (teacherDetail.fetchUserType() == UserType.TEACHER) {
                ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(teacherDetail.getId(),
                        MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER, UserTagEventType.AMBASSADOR_MENTOR_REWARD_ORDER));
            }
        }

        return message;

    }


    /**
     * 获取用户兑换历史
     *
     * @return
     */
    @RequestMapping(value = "/order/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage orderList() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }

        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        pageNum = pageNum <= 0 ? 1 : pageNum;
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);


        String type = getRequestString("status");
        if (StringUtils.isBlank(type))
            type = "wait";
        if (!type.equals("wait") && !type.equals("deliver"))
            return MapMessage.errorMessage("参数错误");

        List<RewardOrder> rewardOrders = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(teacher.getId());
        if (CollectionUtils.isEmpty(rewardOrders))
            return MapMessage.successMessage().add("orderList", new ArrayList<>());
        List<RewardOrder> orderList = null;
        if (type.equals("wait")) {
            orderList = rewardOrders.stream().filter(t -> !RewardOrderStatus.DELIVER.name().equals(t.getStatus())).collect(Collectors.toList());
        } else if (type.equals("deliver")) {
            orderList = rewardOrders.stream().filter(t -> RewardOrderStatus.DELIVER.name().equals(t.getStatus())).collect(Collectors.toList());
        } else
            orderList = new ArrayList<>();


        Page<Map<String, Object>> page = rewardLoaderClient.rewardOrdersToPage(orderList, pageable);
        page.getContent().stream().forEach(t -> {
            if (new Double(0).equals(t.get("price"))) {
                t.put("type", "try");
            } else
                t.put("type", "normal");
            t.put("statusName", RewardOrderStatus.valueOf(t.get("status").toString()).getDesc());
        });
        page.getContent().stream().forEach(t -> {
            String image = (String) t.remove("image");
            t.put("image", combineCdbUrl(image));
            Date createDate = (Date) t.remove("createDatetime");
            t.put("createDatetime", DateUtils.dateToString(createDate, DateUtils.FORMAT_SQL_DATETIME));
        });
        return MapMessage.successMessage().add("orderList", page.getContent());
    }


    /**
     * 用户取消兑换
     *
     * @return
     */
    @RequestMapping(value = "/order/cancel.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage cancelOrder() {

        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return noLoginResult;
        }
        Long orderId = getRequestLong("orderId");
        if (orderId == 0L)
            return MapMessage.errorMessage("订单错误");
        RewardOrder order = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(orderId);
        if (order == null)
            return MapMessage.errorMessage("订单错误");
        if (!order.getStatus().equals(RewardOrderStatus.SUBMIT.name()))
            return MapMessage.errorMessage("只能待审核订单可以取消");

        return rewardServiceClient.deleteRewardOrder(teacher, order);
    }


    private MapMessage checkCanBuy(TeacherDetail teacherDetail, RewardProductDetail rewardProductDetail, Integer quantity, RewardSku sku) {


        //判断用户学豆是否足够
        Long userIntegral = getIntegral(teacherDetail);
        BigDecimal total = new BigDecimal(rewardProductDetail.getDiscountPrice()).multiply(new BigDecimal(quantity));
        int totalPrice = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (userIntegral < totalPrice) {
            String unit = fetchUnit(teacherDetail);
            return MapMessage.errorMessage(unit + "不足").add(CHECK_RESULT_TYPE, "noEnoughIntegral");
        }

        //判断单品是否存在以及是否售完
        if (sku == null) {
            return MapMessage.errorMessage("单品不存在。");
        }
        if (sku.getInventorySellable() < quantity) {
            return MapMessage.errorMessage("对不起,您兑换的奖品目前缺货,暂时无法兑换。").add(CHECK_RESULT_TYPE, "noEnoughProducts");
        }

        //判断是否已验证
        if (teacherDetail.getAuthenticationState() != AuthenticationState.SUCCESS.getState()) {
            return MapMessage.errorMessage("你还没有认证，暂时不能兑换奖品哦~").add(CHECK_RESULT_TYPE, "noAuth");
        }

        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacherDetail.getId());
        //判断用户绑手机
        if (!authentication.isMobileAuthenticated()) {
            return MapMessage.errorMessage("你还没有绑定手机，暂时不能兑换奖品哦~").add(CHECK_RESULT_TYPE, "noBindMobile");
        }
        //收获地址为空
        UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddress(teacherDetail.getId());
        if (shippingAddress == null || StringUtils.isBlank(shippingAddress.getDetailAddress())) {
            return MapMessage.errorMessage("你还没有填写收货地址，暂时不能兑换奖品哦~").add(CHECK_RESULT_TYPE, "noAddress");
        }
        //等级判断
        if (rewardProductDetail.getAmbassadorLevel() > 0) {
            //大使等级判断
            if (!teacherDetail.isSchoolAmbassador()) {
                return MapMessage.errorMessage("只有校园大使才能兑换").add(CHECK_RESULT_TYPE, "default");
            } else {
                //是大使  判断等级
                AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacherDetail.getId());
                AmbassadorLevel level = AmbassadorLevel.SHI_XI;
                if (levelDetail != null) {
                    level = levelDetail.getLevel();
                }
                if (level.getLevel() < rewardProductDetail.getAmbassadorLevel()) {
                    return MapMessage.errorMessage("对不起，你的等级不能兑换该奖品").add(CHECK_RESULT_TYPE, "default");
                }
            }
        } else {
            //老师等级判断
            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherDetail.getId());
            int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());

            if (rewardProductDetail.getTeacherLevel() > 0 && teacherLevel < rewardProductDetail.getTeacherLevel()) {
                return MapMessage.errorMessage("对不起，你的等级不能兑换该奖品").add(CHECK_RESULT_TYPE, "default");
            }
        }
        return MapMessage.successMessage();
    }
}
