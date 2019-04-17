///*
// * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
// *
// * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains the property of
// * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Shanghai Sunny
// * Education, Inc. and its suppliers and may be covered by patents, patents
// * in process, and are protected by trade secret or copyright law. Dissemination
// * of this information or reproduction of this material is strictly forbidden
// * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
// */
//
//package com.voxlearning.washington.controller.mobile.student.reward;
//
//import com.voxlearning.alps.annotation.meta.UserType;
//import com.voxlearning.alps.core.util.MapUtils;
//import com.voxlearning.alps.lang.util.MapMessage;
//import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
//import com.voxlearning.utopia.service.reward.api.mapper.newversion.*;
//import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.HpProductEntity;
//import com.voxlearning.utopia.service.reward.constant.RewardProductType;
//import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
//import com.voxlearning.utopia.service.reward.entity.RewardCategory;
//import com.voxlearning.utopia.service.reward.mapper.LoadRewardProductContext;
//import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
//import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
//import com.voxlearning.utopia.service.user.api.entities.User;
//import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
//import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
//import com.voxlearning.washington.controller.mobile.AbstractMobileController;
//import com.voxlearning.washington.controller.open.ApiConstants;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.inject.Inject;
//import java.util.*;
//
//import static com.voxlearning.alps.calendar.DateUtils.dateToString;
//import static com.voxlearning.alps.lang.convert.SafeConverter.toLong;
//import static java.util.stream.Collectors.toList;
//
///**
// * 公益
// *
// * @author haitian.gan
// * @since 2018/6/11
// */
//@Controller
//@Slf4j
//@RequestMapping(value = "/userMobile/rewardCenter")
//public class MobileRewardCenterController extends AbstractMobileController {
//
//    @Inject private RewardCenterClient rewardCenterClient;
//    @Inject private AsyncUserServiceClient asyncUserServiceClient;
//
//    /**
//     * 首页我的托比秀
//     * @return
//     */
//    @RequestMapping(value = "/tobyShow.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getHomePageTobyShow() {
//        if (currentUser() == null) {
//            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
//        }
//
//        MapMessage resultMsg = MapMessage.successMessage();
//        User user = currentUser();
//        if ((user instanceof StudentDetail) == false) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//        RewardHomePageTobySowMapper mapper = rewardCenterClient.getHomePageTobyShow(user);
//        resultMsg.add("integralNum", mapper.getIntegralNum());
//        resultMsg.add("powerPillar", mapper.getPowerPillar());
//        resultMsg.add("myToby", mapper.getMyToby());
//        resultMsg.add("publicGoodPlaque", mapper.getPublicGoodPlaque());
//        return resultMsg;
//    }
//
//    /**
//     * 获取首页广告位的广告列表
//     *
//     * @return
//     */
//    @RequestMapping(value = "/ads.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getHomePageAdList() {
//        if (currentUser() == null) {
//            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
//        }
//
//        MapMessage resultMsg = MapMessage.successMessage();
//        User user = currentUser();
//        if ((user instanceof StudentDetail) == false) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//
//        List<HpAdMapper> mappers = rewardCenterClient.getHomePageAdList();
//        resultMsg.add("adList", mappers);
//
//        return resultMsg;
//    }
//
//    /**
//     * 获取首页一起公益
//     *
//     * @return
//     */
//    @RequestMapping(value = "/publicGoods.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getHomePagePublicGoodList() {
//        MapMessage resultMsg = MapMessage.successMessage();
//        User user = currentUser();
//        Validate.isTrue(user != null,"请重新登录!");
//        if ((user instanceof StudentDetail) == false) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//
//        try{
//            List<HpPublicGoodMapper> mappers = rewardCenterClient.getHomePagePublicGoodList();
//            resultMsg.add("publicGoodList", mappers);
//        }catch (IllegalArgumentException e){
//            logger.error(String.format("getHomePagePublicGoodList error userId:%s", user.getId()),e);
//            resultMsg = MapMessage.errorMessage(e.getMessage());
//        }
//        return resultMsg;
//    }
//
//    /**
//     * 获得课间广场游戏和视频
//     *
//     * @return
//     */
//    @RequestMapping(value = "/playtimeSquare.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getHomePagePlaytimeSquare() {
//        MapMessage resultMsg = MapMessage.successMessage();
//        User user = currentUser();
//        Validate.isTrue(user != null,"请重新登录!");
//        if ((user instanceof StudentDetail) == false) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//
//        try{
//            HpPlaytimeSquareMapper mapper = rewardCenterClient.getHomePagePlaytimeSquare();
//            resultMsg.add("game",mapper.getGame());
//            resultMsg.add("video", mapper.getVideo());
//            return resultMsg;
//        }catch (Exception e){
//            logger.error(String.format("getHomePagePlaytimeSquare error userId:%s", user.getId()),e);
//            return MapMessage.errorMessage(e.getMessage());
//        }
//    }
//
//    /**
//     * 获取首页商品列表
//     *
//     * @return
//     */
//    @RequestMapping(value = "/products.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage getHomePageProducts() {
//        MapMessage resultMsg = MapMessage.successMessage();
//        User user = currentUser();
//        Validate.isTrue(user != null,"请重新登录!");
//        if ((user instanceof StudentDetail) == false) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//        try{
//
//            // 记录访问的轨迹
//            if(Objects.equals(user.getUserType(), UserType.TEACHER.getType()))
//                rewardServiceClient.recordAccessTrace(user.getId());
//
//            // 老师目前只取实物商品
//            List<RewardCategory> allCategories = new ArrayList<>(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_SHIWU, user.fetchUserType()));
//            // 老师端也添加虚拟类别
//            allCategories.addAll(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_TIYAN, user.fetchUserType()));
//
//            List<RewardCategory> topknotList = new ArrayList<>();
//            List<RewardCategory> hotPublicGoodTagList = new ArrayList<>();
//            List<RewardCategory> hotProductList = new ArrayList<>();
//
//
//            allCategories.stream()
//                    .filter(e -> !user.isStudent() ||
//                            (currentStudentDetail() != null && currentStudentDetail().isPrimaryStudent() && Boolean.TRUE.equals(e.getPrimaryVisible())) ||
//                            (currentStudentDetail() != null && currentStudentDetail().isJuniorStudent() && Boolean.TRUE.equals(e.getJuniorVisible())))
//                    // 按照Display Order排序
//                    .sorted(Comparator.comparingInt(RewardCategory::getDisplayOrder))
//                    .forEach(t -> {
//                        if (t.getCategoryCode() == RewardCategory.SubCategory.HEAD_WEAR.getName()) {
//                            topknotList.add(t);
//                        }
//                        hotPublicGoodTagList.add(t);
//                        hotProductList.add(t);
//                    });
//
//            boolean flag = rewardServiceClient.tryIntegralOffsetFreightTip(user.getId(), new Date());
//            String unit = fetchUnit(user);
//
//            HpProductMapper mapper = rewardCenterClient.getHomePageProductMapper();
//            resultMsg.add("hotProductList", topknotList);
//            resultMsg.add("hotPublicGoodTagList", hotPublicGoodTagList);
//            resultMsg.add("topknotList", hotProductList);
//            resultMsg.add("integral_text", unit)
//                    .add("tipShowFlag", flag);
//
//        }catch (Exception e){
//            logger.error(String.format("getHomePageProducts error userId:%s", user.getId()),e);
//            resultMsg = MapMessage.errorMessage(e.getMessage());
//        }
//        return resultMsg;
//    }
//
//    /**
//     * 获取商品列表
//     *
//     * @param user
//     * @return
//     */
//    private List<RewardProductDetail> loadProducts(User user, LoadRewardProductContext context) {
//
//        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
//                u -> asyncUserServiceClient.getAsyncUserService()
//                        .loadUserSchool(u)
//                        .getUninterruptibly()
//                ,null);
//
//        pagination.getContent().forEach(t -> {
//            t.setImage(combineCdbUrl(t.getImage()));
//            t.getImages().forEach(p -> p.setLocation(combineCdbUrl(p.getLocation())));
//        });
//
//        return pagination.getContent();
//    }
//
//}
