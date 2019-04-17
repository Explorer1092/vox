/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.mobile.student;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.ListUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.SingletonMap;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.entity.misc.UgcAnswers;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.mapper.TeacherRewardMapper;
import com.voxlearning.utopia.mapper.UgcRecordMapper;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.document.UserPrivilege;
import com.voxlearning.utopia.service.action.api.support.PrivilegeOrigin;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.clazz.client.SmartClazzServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.reward.api.RewardService;
import com.voxlearning.utopia.service.reward.api.enums.RewardTagEnum;
import com.voxlearning.utopia.service.reward.api.enums.support.RewardTagNode;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.*;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.entity.*;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.ProductFilterEntity;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardBufferLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.LoadRewardProductContext;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.reward.mapper.product.CategoryMapper;
import com.voxlearning.utopia.service.reward.mapper.product.MobileHomePageCategoryMapper;
import com.voxlearning.utopia.service.reward.util.GrayFuncMngCallback;
import com.voxlearning.utopia.service.reward.util.RewardProductDetailUtils;
import com.voxlearning.utopia.service.reward.util.RewardTagUtils;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.mapper.StudentLikeLogPerDayMapper;
import com.voxlearning.washington.mapper.StudentPrivilegeMapper;
import com.voxlearning.washington.mapper.tobyavatar.TobyAvatarMapper;
import com.voxlearning.washington.support.TobyAvatar;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.reward.entity.RewardCategory.SubCategory;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_INTEGRAL_REWARD_HOMEWORK_ID;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.REQ_INTEGRAL_REWARD_HOMEWORK_TYPE;


/**
 * 移动端学生个人中心
 *
 * @author Sir0xb
 * @author Rui Bao
 * @since 2013-12-13
 */

@Controller
@RequestMapping("/studentMobile/center")
@NoArgsConstructor
@Slf4j
public class MobileStudentCenterController extends AbstractMobileController {

    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @Inject private RewardCenterClient rewardCenterClient;
    @Inject private RewardDetailManager rewardDetailManager;
    @Inject private SmartClazzServiceClient smartClazzServiceClient;
    @Inject private UserLikeServiceClient userLikeServiceClient;
    @Inject private NewRewardBufferLoaderClient newRewardBufferLoaderClient;
    @Inject private NewRewardServiceClient newRewardServiceClient;

    @ImportService(interfaceClass = RewardService.class)
    private RewardService rewardService;

    private GrayFuncMngCallback grayFuncMngCallBack = (user, func1, func2) -> {
        if (user instanceof StudentDetail) {
            return grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable((StudentDetail) user, func1, func2, false);
        } else if (user instanceof TeacherDetail) {
            return grayFunctionManagerClient.getTeacherGrayFunctionManager()
                    .isWebGrayFunctionAvailable((TeacherDetail) user, func1, func2, false);
        }

        return false;
    };

    // 按兑换量排序
    private static final Comparator<RewardProductDetail> soldQuantityComparator = new Comparator<RewardProductDetail>() {
        @Override
        public int compare(RewardProductDetail o1, RewardProductDetail o2) {
            if (o1.getSoldQuantity() == null) {
                o1.setSoldQuantity(0);
            }
            if (o2.getSoldQuantity() == null) {
                o2.setSoldQuantity(0);
            }
            return o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
        }
    };

    /**
     * 奖品中心首页苏数据获取
     *
     * @return
     */
    @RequestMapping(value = "/homePageData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewHomePageTobyShow() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        User user = currentUser();

        MapMessage resultMsg = MapMessage.successMessage();
        StudentDetail studentDetail = currentStudentDetail();
        RewardHomePageTobySowMapper mapper = rewardCenterClient.getHomePageTobyShow(studentDetail);

        resultMsg.add("integralNum", mapper.getIntegralNum());
        resultMsg.add("isHasNewLeaveWord", mapper.getIsHasNewLeaveWord());
        resultMsg.add("powerPillar", mapper.getPowerPillar());
        resultMsg.add("isProductArea", mapper.getIsProductArea());
        resultMsg.add("toby", mapper.getToby());
        boolean isPowerFull = mapper.getIsPoweFull();
        resultMsg.add("fullPowerNumber", mapper.getFullPowerNumber());
        resultMsg.add("isPowerFull", isPowerFull);
        resultMsg.add("isShowRenameTip", mapper.getIsShowRenameTip());
        resultMsg.add("publicGoodPlaque", mapper.getPublicGoodPlaque());

        List<MobileHomePageCategoryMapper> mobileCategoryMappers = newRewardLoaderClient.getMobileHomePageCategoryMapper(studentDetail);

        if (user.isStudent()) {//

            //已拥有订单
            List<StudentPrivilegeMapper> mappers = new ArrayList<>();
            StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
            List<UserPrivilege> userPrivileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), PrivilegeType.Head_Wear);
            alreadyOwned(userPrivileges, mappers, studentInfo);
            Set<Long> relateProductIdSet = mappers.stream().filter(StudentPrivilegeMapper::getEffective).map(StudentPrivilegeMapper::getRelateProductId).collect(Collectors.toSet());
            Map<String, Privilege> privilegeMap = privilegeBufferServiceClient.getPrivilegeBuffer().dump().getData().stream().collect(Collectors.toMap(Privilege::getId, Function.identity(), (o1, o2) -> o2));
            mobileCategoryMappers.stream()
                    .forEach(category -> {
                        category.getProductList().stream()
                                .forEach(product -> {
                                    newRewardLoaderClient.proHeadWearExtra(relateProductIdSet, privilegeMap, product);
                                });
                    });

            Long tipTime = newRewardServiceClient.fetchWearProductExpireTipTime(user.getId());
            String headWearExpireTip = headWearExpireTip(userPrivileges, tipTime);
            String tobyWearExpireTip = tobyWearExpireTip(user.getId(), tipTime);
            resultMsg.add("headWearExpireTip", headWearExpireTip);
            resultMsg.add("tobyWearExpireTip", tobyWearExpireTip);
            if (StringUtils.isNotBlank(headWearExpireTip) || StringUtils.isNotBlank(tobyWearExpireTip)) {
                tipTime = new Date().getTime();
                newRewardServiceClient.setWearProductExpireTipTime(user.getId(), tipTime);
            }
        }

        Long activitySize = rewardCenterClient.getDonationCount(user.getId());

        resultMsg.add("activitySize", activitySize);
        resultMsg.add("productRegion", mobileCategoryMappers);

        int tipShowFlag = rewardServiceClient.tryShowTipFlag(currentUser());
        String unit = fetchUnit(studentDetail);
        resultMsg.add("integral_text", unit);
        resultMsg.add("tipShowFlag", tipShowFlag);

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute == null) {
            studentExtAttribute = new StudentExtAttribute();
        }
        resultMsg.add("closeIntegralFairyland", studentExtAttribute.fetchCloseIntegralFairyland());

        return resultMsg;
    }

    /**
     * 托比过期提示（前4天到后一天过期）
     *
     * @param tipTime     上次提示时间
     */
    private String tobyWearExpireTip(Long userId, Long tipTime) {
        tipTime = Objects.isNull(tipTime) ? 0L:tipTime;
        Long fiveDayAgo = DayRange.current().previous().previous().previous().previous().previous().getStartDate().getTime();
        Long todayEnd = DayRange.current().next().getStartTime();
        Long fourDayAgo = DayRange.current().previous().previous().previous().previous().getStartDate().getTime();
        Long startTime = fourDayAgo>tipTime ? fourDayAgo:tipTime;
        if (tipTime > fiveDayAgo) {
            return StringUtils.EMPTY;
        }
        Map<Long, TobyImageCVRecord> imageCVRecordMap = rewardCenterClient.loadTobyImageListByExpireTimeRegion(userId, startTime, todayEnd);
        if (MapUtils.isNotEmpty(imageCVRecordMap)) {
            return "你的托比装扮马上到期喽，快去再选一款吧";
        }

        Map<Long, TobyCountenanceCVRecord> countenanceCVRecordMap = rewardCenterClient.loadTobyCountenanceListByExpireTimeRegion(userId, startTime, todayEnd);
        if (MapUtils.isNotEmpty(countenanceCVRecordMap)) {
            return "你的托比装扮马上到期喽，快去再选一款吧";
        }

        Map<Long, TobyPropsCVRecord> propsCVRecordMap = rewardCenterClient.loadTobyPropsListByByExpireTimeRegion(userId, startTime, todayEnd);
        if (MapUtils.isNotEmpty(propsCVRecordMap)) {
            return "你的托比装扮马上到期喽，快去再选一款吧";
        }

        Map<Long, TobyAccessoryCVRecord> accessoryCVRecordMap = rewardCenterClient.loadTobyAccessoryListByExpireTimeRegion(userId, startTime, todayEnd);
        if (MapUtils.isNotEmpty(accessoryCVRecordMap)) {
            return "你的托比装扮马上到期喽，快去再选一款吧";
        }

        return StringUtils.EMPTY;
    }

    /**
     * 头饰过期提示（前4天到后一天过期的头饰）
     *
     * @param privileges  所有特权集合
     * @param tipTime     上次提示时间
     */
    private String headWearExpireTip(List<UserPrivilege> privileges, Long tipTime) {
        Long fiveDayAgo = DayRange.current().previous().previous().previous().previous().previous().getStartDate().getTime();
        Long todayDed = DayRange.current().next().getStartTime();
        Long fourDayAgo = DayRange.current().previous().previous().previous().previous().getStartDate().getTime();
        if (Objects.nonNull(tipTime) && tipTime > fiveDayAgo) {
            return StringUtils.EMPTY;
        }
        privileges = privileges.stream()
                .filter(p -> PrivilegeType.Head_Wear.name().equals(p.getType()))
                .filter(p -> Objects.nonNull(p.getExpiryDate()))
                .filter(p -> p.getExpiryDate().getTime() > fourDayAgo && p.getExpiryDate().getTime() < todayDed)
                .filter(p -> Objects.isNull(tipTime) || Objects.equals(tipTime, 0L) || p.getExpiryDate().getTime() > tipTime)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(privileges)) {
            return "你的头像装扮马上到期喽，快去再选一款吧";
        }
        return StringUtils.EMPTY;
    }

    @RequestMapping(value = "/productList.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getNewProductList() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }
        MapMessage rewaultMsg = MapMessage.successMessage();

        Long categoryId = getRequestLong("categoryId", 0);
        Integer categoryType = getRequestInt("categoryType", 0);
        Long oneLevelFilterId = getRequestLong("oneLevelFilterId", 0);
        Long twoLevelFilterId = getRequestLong("twoLevelFilterId", 0);

        Boolean showAffordable = getRequestBool("showAffordable", false);
        Integer pageNum = getRequestInt("pageNum", 0);
        Integer pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        String upDown = getRequestString("upDown");

        Boolean have = getRequestBool("have");
        String searchName = getRequestString("searchName");
        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        //如果没有分类类型，给一个默认值
        if (Objects.equals(categoryId, 0L)) {
            List<CategoryMapper> mappers = newRewardLoaderClient.getCategoryMapper(user, userVisibleType, RewardDisplayTerminal.PC.name());
            CategoryMapper categoryMapper = mappers.stream().findFirst().orElse(null);
            if (Objects.nonNull(categoryMapper)) {
                categoryId = categoryMapper.getId();
                categoryType = categoryMapper.getType();
            }
        }

        if (Objects.equals(categoryId, 0L)) {
            return MapMessage.successMessage("分类类型错误！");
        }

        RewardProductDetailPagination pagination;

        List<StudentPrivilegeMapper> mappers = new ArrayList<>();
        Predicate<RewardProduct> productShowFilter = detail -> true;
        if (StringUtils.isNotBlank(searchName)) {//如果是搜索名称默认搜索一级类别下的所有商品
            oneLevelFilterId = 0L;
            twoLevelFilterId = 0L;
            showAffordable = false;
            have = false;
            productShowFilter = productShowFilter.and(product -> product.getProductName().contains(searchName));
        }

        if (Objects.equals(categoryType, MapperCategoryType.CATEGORY.intType()) && newRewardLoaderClient.isHeadWear(categoryId)) {
            List<RewardProductDetail> productList;

            productList = newRewardLoaderClient.loadProductList(user, RewardDisplayTerminal.Mobile.name(),
                    categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, 1d, productShowFilter);
            //先排序，否则加入的已拥有的头饰排序报错
            productList = RewardProductDetailUtils.orderProducts(user, productList, orderBy, upDown);
            Set<String> relateVirtualItemIdSet = productList.stream().map(RewardProductDetail::getRelateVirtualItemId).collect(Collectors.toSet());

            StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
            List<UserPrivilege> userPrivileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), PrivilegeType.Head_Wear);
            alreadyOwned(userPrivileges, mappers, studentInfo);
            Long finalOneLevelFilterId = oneLevelFilterId;
            List<RewardProductDetail> ownHeadWearList = mappers.stream()
                    .filter(StudentPrivilegeMapper::getEffective)
                    .filter(mapper -> Objects.equals(finalOneLevelFilterId, 0L) && !relateVirtualItemIdSet.contains(mapper.getPrivilegeId()))
                    .map( mapper -> {
                        RewardProductDetail detail = new RewardProductDetail();
                        detail.setOneLevelCategoryType(OneLevelCategoryType.JPZX_HEADWEAR.intType());
                        detail.setRelateVirtualItemId(mapper.getPrivilegeId());
                        detail.setOrigin(PrivilegeOrigin.valueOf(mapper.getOrigin()));
                        detail.setHave(mapper.getEffective());
                        detail.setHeadWearImg(mapper.getImgUrl());
                        detail.setProductName(mapper.getName());

                        return detail;
                    }).collect(Collectors.toList());
            if (have) {
                productList = ownHeadWearList;
            } else {
                if (CollectionUtils.isNotEmpty(productList)) {
                    productList.addAll(ownHeadWearList);
                }
            }

            long total = productList.size();
            int start = pageNum * pageSize;
            int end = Math.min((int) total, (pageNum + 1) * pageSize);
            if (start > total) {
                pagination = new RewardProductDetailPagination(Collections.<RewardProductDetail>emptyList());
            } else {
                productList = new LinkedList<>(productList.subList(start, end));
                pagination = new RewardProductDetailPagination(
                        productList,
                        new PageRequest(pageNum, pageSize),
                        total);
            }
        } else {
            pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.Mobile.name(),
                    categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, pageNum, pageSize,
                    orderBy, upDown, 1d, productShowFilter);
        }

        List<ProductFilterEntity> filter = newRewardLoaderClient.buildFilterItem(userVisibleType, categoryId, categoryType, oneLevelFilterId, twoLevelFilterId);

        String titleName = StringUtils.EMPTY;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            ProductSet productSet = newRewardLoaderClient.loadProductSetById(categoryId);
            if (Objects.nonNull(productSet)) {
                titleName = productSet.getName();
            }
        } else {
            ProductCategory productCategory = newRewardLoaderClient.loadProductCategoryById(categoryId);
            if (Objects.nonNull(productCategory)) {
                titleName = productCategory.getName();
            }
        }

        String avatar = StringUtils.EMPTY;
        StudentPrivilegeMapper currentPrivilege = null;
        if (Objects.equals(MapperCategoryType.CATEGORY.intType(), categoryType) && newRewardLoaderClient.isHeadWear(categoryId)) {
            avatar = getUserAvatarImgUrl(user.fetchImageUrl());
            StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
            // 当前装扮的头饰
            if (studentInfo != null) {
                currentPrivilege = mappers.stream().filter(Objects::nonNull).filter(mapper -> Objects.equals(mapper.getPrivilegeId(), studentInfo.getHeadWearId())).findFirst().orElse(null);
            }
        }

        Integer oneLevelCategoryType = Objects.equals(categoryType, MapperCategoryType.CATEGORY.intType()) ? newRewardBufferLoaderClient.getProductCategoryBuffer().getOneLevelCategoryType(categoryId):0;

        //已拥有头饰
        Map<String, Privilege> privilegeMap = privilegeBufferServiceClient.getPrivilegeBuffer().dump().getData().stream().collect(Collectors.toMap(Privilege::getId, Function.identity(), (o1, o2) -> o2));
        Set<Long> relateProductIdSet = mappers.stream().filter(StudentPrivilegeMapper::getEffective).map(StudentPrivilegeMapper::getRelateProductId).collect(Collectors.toSet());

        pagination.getContent().stream().forEach(product -> {
            newRewardLoaderClient.proHeadWearExtra(relateProductIdSet, privilegeMap, product);
        });

        Long activitySize = 0L;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType) && Objects.equals(SetIdLogicRelation.TEACHER_PUBLIC_GOOD.getNumber(), categoryId.intValue())) {
            activitySize = rewardCenterClient.getDonationCount(user.getId());
        }
        return rewaultMsg
                .add("titleName",titleName)
                .add("oneLevelCategoryType", oneLevelCategoryType)
                .add("rows", pagination.getContent())
                .add("filter", filter)
                .add("hasNext", CollectionUtils.isNotEmpty(pagination.getContent()) && pagination.getContent().size()>=pageSize)
                .add("avatar", avatar)
                .add("currentPrivilege", currentPrivilege)
                .add("activitySize", activitySize);
    }

    @RequestMapping(value = "/minicourseList.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getMiniCourseList() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long oneLevelFilterId = getRequestLong("oneLevelFilterId", 0);
        Long twoLevelFilterId = getRequestLong("twoLevelFilterId", 0);

        Boolean showAffordable = getRequestBool("showAffordable", false);
        Integer pageNum = getRequestInt("pageNum", 0);
        Integer pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        String upDown = getRequestString("upDown");

        Long categoryId = newRewardBufferLoaderClient.getProductCategoryBuffer().getOneLevelCategoryId(OneLevelCategoryType.JPZX_MINI_COURSE.intType());
        Integer categoryType = MapperCategoryType.CATEGORY.intType();

        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        RewardProductDetailPagination pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.Mobile.name(),
                categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, pageNum, pageSize, orderBy, upDown, 1d);

        List<ProductFilterEntity> filter = newRewardLoaderClient.buildFilterItem(userVisibleType, categoryId, categoryType, oneLevelFilterId, twoLevelFilterId);

        String titleName = StringUtils.EMPTY;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            ProductSet productSet = newRewardLoaderClient.loadProductSetById(categoryId);
            if (Objects.nonNull(productSet)) {
                titleName = productSet.getName();
            }
        } else {
            ProductCategory productCategory = newRewardLoaderClient.loadProductCategoryById(categoryId);
            if (Objects.nonNull(productCategory)) {
                titleName = productCategory.getName();
            }
        }

        return MapMessage.successMessage()
                .add("titleName",titleName)
                .add("oneLevelCategoryType", OneLevelCategoryType.JPZX_MINI_COURSE.intType())
                .add("rows", pagination.getContent())
                .add("filter", filter)
                .add("hasNext", CollectionUtils.isNotEmpty(pagination.getContent()) && pagination.getContent().size()>=pageSize)
                .add("avatar", "")
                .add("currentPrivilege", null);
    }

    @RequestMapping(value = "/yiqiexclusiveList.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getYiqiExclusiveList() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long oneLevelFilterId = getRequestLong("oneLevelFilterId", 0);
        Long twoLevelFilterId = getRequestLong("twoLevelFilterId", 0);

        Boolean showAffordable = getRequestBool("showAffordable", false);
        Integer pageNum = getRequestInt("pageNum", 0);
        Integer pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        String upDown = getRequestString("upDown");
        String searchName = getRequestString("searchName");

        Long categoryId = SetIdLogicRelation.TEACHER_YIQI.getNumber().longValue();
        Integer categoryType = MapperCategoryType.SET.intType();

        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        Predicate<RewardProduct> productShowFilter = detail -> true;
        if (StringUtils.isNotBlank(searchName)) {//如果是搜索名称默认搜索一级类别下的所有商品
            oneLevelFilterId = 0L;
            twoLevelFilterId = 0L;
            showAffordable = false;
            productShowFilter = productShowFilter.and(product -> product.getProductName().contains(searchName));
        }

        RewardProductDetailPagination pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.Mobile.name(),
                categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, pageNum, pageSize, orderBy, upDown, 1d, productShowFilter);

        //已拥有头饰
        List<StudentPrivilegeMapper> mappers = new ArrayList<>();
        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
        List<UserPrivilege> userPrivileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), PrivilegeType.Head_Wear);
        alreadyOwned(userPrivileges, mappers, studentInfo);
        Set<Long> relateProductIdSet = mappers.stream().filter(StudentPrivilegeMapper::getEffective).map(StudentPrivilegeMapper::getRelateProductId).collect(Collectors.toSet());
        Map<String, Privilege> privilegeMap = privilegeBufferServiceClient.getPrivilegeBuffer().dump().getData().stream().collect(Collectors.toMap(Privilege::getId, Function.identity(), (o1, o2) -> o2));

        pagination.getContent().stream().forEach(product -> newRewardLoaderClient.proHeadWearExtra(relateProductIdSet, privilegeMap, product));

        List<ProductFilterEntity> filter = newRewardLoaderClient.buildFilterItem(userVisibleType, categoryId, categoryType, oneLevelFilterId, twoLevelFilterId);

        String titleName = StringUtils.EMPTY;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            ProductSet productSet = newRewardLoaderClient.loadProductSetById(categoryId);
            if (Objects.nonNull(productSet)) {
                titleName = productSet.getName();
            }
        } else {
            ProductCategory productCategory = newRewardLoaderClient.loadProductCategoryById(categoryId);
            if (Objects.nonNull(productCategory)) {
                titleName = productCategory.getName();
            }
        }

        return MapMessage.successMessage()
                .add("titleName",titleName)
                .add("oneLevelCategoryType", 0)
                .add("rows", pagination.getContent())
                .add("filter", filter)
                .add("hasNext", CollectionUtils.isNotEmpty(pagination.getContent()) && pagination.getContent().size()>=pageSize)
                .add("avatar", "")
                .add("currentPrivilege", null);
    }

    /**
     * 我的Toby 装扮数据获取
     * @return
     */
    @RequestMapping(value = "/tobyDress.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewTobyDress() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        Long categoryId = newRewardBufferLoaderClient.getProductCategoryBuffer().getOneLevelCategoryId(OneLevelCategoryType.JPZX_TOBY.intType());
        Integer categoryType = MapperCategoryType.CATEGORY.intType();
        Long oneLevelFilterId = 0L;
        Long twoLevelFilterId = 0L;
        Boolean showAffordable = false;

        Integer pageNum = 0;
        Integer pageSize = 100;
        String orderBy = StringUtils.EMPTY;
        String upDown = StringUtils.EMPTY;

        RewardProductDetailPagination pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.Mobile.name(),
                categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, pageNum, pageSize, orderBy, upDown, 1d);

        return getTobyDress(user, pagination.getContent());
    }

    private MapMessage getTobyDress(User user, List<RewardProductDetail> productList) {
        MapMessage resultMsg = MapMessage.successMessage();

        List<TobyImageEntity> imageList = new ArrayList<>();
        List<TobyCountenanceEntity> countenanceList = new ArrayList<>();
        List<TobyPropsEntity> propsList = new ArrayList<>();
        List<TobyAccessoryEntity> accessoryList = new ArrayList<>();

        Map<Long, TobyCountenanceCVRecord> countenanceRecordMap = rewardCenterClient.loadTobyCountenanceListByUserId(user.getId());
        Map<Long, TobyImageCVRecord> imageRecordMap = rewardCenterClient.loadTobyImageListByUserId(user.getId());
        Map<Long, TobyPropsCVRecord> propsRecordMap = rewardCenterClient.loadTobyPropsListByUserId(user.getId());
        Map<Long, TobyAccessoryCVRecord> accessoryCVRecordMap = rewardCenterClient.loadTobyAccessoryListByUserId(user.getId());

        List<TobyDressMapper.TobyDress> tobyDressList = rewardCenterClient.loadTobyDress().getTobyDressList();
        Map<Long, TobyDressMapper.TobyDress> longTobyDressMap = tobyDressList
                .stream()
                .collect(Collectors.toMap(t -> t.getId(), Function.identity(), (o1, o2) -> o2));


        List<Long> productIdList = new ArrayList<>();
        if (imageRecordMap != null && !imageRecordMap.isEmpty()) {
            productIdList.addAll(imageRecordMap.keySet());
        }
        if (countenanceRecordMap != null && !countenanceRecordMap.isEmpty()) {
            productIdList.addAll(countenanceRecordMap.keySet());
        }
        if (propsRecordMap != null && !propsRecordMap.isEmpty()) {
            productIdList.addAll(propsRecordMap.keySet());
        }
        if (accessoryCVRecordMap != null && !accessoryCVRecordMap.isEmpty()) {
            productIdList.addAll(accessoryCVRecordMap.keySet());
        }
        Map<Long, RewardProduct> ownMap = rewardLoaderClient.loadProductByIdList(productIdList);
        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIdList);
        if (imageRecordMap != null && !imageRecordMap.isEmpty()) {
            for (Map.Entry<Long, TobyImageCVRecord> entry : imageRecordMap.entrySet()) {

                if (ownMap.containsKey(entry.getKey())) {
                    RewardProduct dress = ownMap.get(entry.getKey());
                    TobyImageCVRecord record = entry.getValue();
                    TobyImageEntity entity = new TobyImageEntity();
                    entity.setCvAuthority(true);
                    if (Objects.equals(record.getStatus(), TobyImageCVRecord.Status.USING.getStatus())) {
                        entity.setCvAuthority(false);
                        entity.setUnallowCVTip("您已拥有该装扮！");
                    }
                    entity.setId(entry.getKey());
                    entity.setName(dress.getProductName());
                    entity.setStatus(record.getStatus());
                    List<RewardImage> images = rewardImages.get(entry.getKey());
                    String defaultImage = RewardConstants.DEFAULT_PRODUCT_IMAGE_URL;
                    if (CollectionUtils.isNotEmpty(images)) {
                        defaultImage = images.iterator().next().getLocation();
                    }
                    String url = longTobyDressMap.containsKey(NumberUtils.toLong(dress.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(dress.getRelateVirtualItemId())).getUrl() : defaultImage;
                    entity.setUrl(url);
                    imageList.add(entity);
                }
            }
        }

        if (countenanceRecordMap != null && !countenanceRecordMap.isEmpty()) {
            for (Map.Entry<Long, TobyCountenanceCVRecord> entry : countenanceRecordMap.entrySet()) {

                if (ownMap.containsKey(entry.getKey())) {
                    RewardProduct dress = ownMap.get(entry.getKey());
                    TobyCountenanceCVRecord record = entry.getValue();
                    TobyCountenanceEntity entity = new TobyCountenanceEntity();
                    entity.setCvAuthority(true);
                    if (Objects.equals(record.getStatus(), TobyImageCVRecord.Status.USING.getStatus())) {
                        entity.setCvAuthority(false);
                        entity.setUnallowCVTip("您已拥有该装扮！");
                    }
                    entity.setId(entry.getKey());
                    entity.setName(dress.getProductName());
                    entity.setStatus(record.getStatus());
                    List<RewardImage> images = rewardImages.get(entry.getKey());
                    String defaultImage = RewardConstants.DEFAULT_PRODUCT_IMAGE_URL;
                    if (CollectionUtils.isNotEmpty(images)) {
                        defaultImage = images.iterator().next().getLocation();
                    }
                    String url = longTobyDressMap.containsKey(NumberUtils.toLong(dress.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(dress.getRelateVirtualItemId())).getUrl() : defaultImage;
                    entity.setUrl(url);
                    countenanceList.add(entity);
                }
            }
        }

        if (propsRecordMap != null && !propsRecordMap.isEmpty()) {
            for (Map.Entry<Long, TobyPropsCVRecord> entry : propsRecordMap.entrySet()) {

                if (ownMap.containsKey(entry.getKey())) {
                    RewardProduct dress = ownMap.get(entry.getKey());
                    TobyPropsCVRecord record = entry.getValue();
                    TobyPropsEntity entity = new TobyPropsEntity();
                    entity.setCvAuthority(true);
                    if (Objects.equals(record.getStatus(), TobyImageCVRecord.Status.USING.getStatus())) {
                        entity.setCvAuthority(false);
                        entity.setUnallowCVTip("您已拥有该装扮！");
                    }
                    entity.setId(entry.getKey());
                    entity.setName(dress.getProductName());
                    entity.setStatus(record.getStatus());
                    List<RewardImage> images = rewardImages.get(entry.getKey());
                    String defaultImage = RewardConstants.DEFAULT_PRODUCT_IMAGE_URL;
                    if (CollectionUtils.isNotEmpty(images)) {
                        defaultImage = images.iterator().next().getLocation();
                    }
                    String url = longTobyDressMap.containsKey(NumberUtils.toLong(dress.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(dress.getRelateVirtualItemId())).getUrl() : defaultImage;
                    entity.setUrl(url);
                    propsList.add(entity);
                }
            }
        }

        if (accessoryCVRecordMap != null && !accessoryCVRecordMap.isEmpty()) {
            for (Map.Entry<Long, TobyAccessoryCVRecord> entry : accessoryCVRecordMap.entrySet()) {

                if (ownMap.containsKey(entry.getKey())) {
                    RewardProduct dress = ownMap.get(entry.getKey());
                    TobyAccessoryCVRecord record = entry.getValue();
                    TobyAccessoryEntity entity = new TobyAccessoryEntity();
                    entity.setCvAuthority(true);
                    if (Objects.equals(record.getStatus(), TobyImageCVRecord.Status.USING.getStatus())) {
                        entity.setCvAuthority(false);
                        entity.setUnallowCVTip("您已拥有该装扮！");
                    }
                    entity.setId(entry.getKey());
                    entity.setName(dress.getProductName());
                    entity.setStatus(record.getStatus());
                    List<RewardImage> images = rewardImages.get(entry.getKey());
                    String defaultImage = RewardConstants.DEFAULT_PRODUCT_IMAGE_URL;
                    if (CollectionUtils.isNotEmpty(images)) {
                        defaultImage = images.iterator().next().getLocation();
                    }
                    String url = longTobyDressMap.containsKey(NumberUtils.toLong(dress.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(dress.getRelateVirtualItemId())).getUrl() : defaultImage;
                    entity.setUrl(url);
                    accessoryList.add(entity);
                }
            }
        }

        productList
                .stream()
                //过滤掉已拥有的装扮 原因是获取商品列表的时候过滤了库存为0的商品，但是库存为0的已拥有的还是需要现实，
                // 故无论库存是否为0，都在已拥有里给了，在这需要过滤掉 fixme 公共方法加载商品库存为0不应该过滤，是个坑需要改
                .filter(t -> !ownMap.containsKey(t.getId()))
                .forEach(t -> {
                    Set<Long> twoLevelCategoryIdSet = newRewardBufferLoaderClient.getProductCategoryRefBuffer().getProductCategory(t.getId());
                    //如果是形象
                    if (twoLevelCategoryIdSet.contains(newRewardBufferLoaderClient.getProductCategoryBuffer().getTwoLevelCategoryId(TwoLevelCategoryType.TOBY_IMG.intType()))) {
                        TobyImageEntity entity = new TobyImageEntity();
                        String url = longTobyDressMap.containsKey(NumberUtils.toLong(t.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(t.getRelateVirtualItemId())).getUrl() : t.getImage();
                        entity.setId(t.getId());
                        entity.setUrl(url);
                        entity.setName(t.getProductName());
                        this.processImageTag(user.getId(), t.getTags(), entity);

                        newRewardLoaderClient.proTobyAndHeadWearPrice(t);

                        List<TobyDressCVOption> cvOption = new ArrayList<>();
                        List<RewardProductDetail.PriceDay> priceDays = t.getPriceDay();
                        if (priceDays != null) {
                            cvOption = priceDays
                                    .stream()
                                    .map(p -> {
                                        TobyDressCVOption option = new TobyDressCVOption();
                                        option.setActiveDay(p.getDay());
                                        option.setPrice(p.getPrice());
                                        option.setPriceNumType(p.getQuantity());
                                        return option;
                                    }).collect(Collectors.toList());
                        }

                        entity.setCvOption(cvOption);
                        entity.setSpendType(t.getSpendType());
                        imageList.add(entity);
                    }
                    //如果是表情
                    else if (twoLevelCategoryIdSet.contains(newRewardBufferLoaderClient.getProductCategoryBuffer().getTwoLevelCategoryId(TwoLevelCategoryType.TOBY_COUNTENANCE.intType()))) {
                        TobyCountenanceEntity entity = new TobyCountenanceEntity();
                        String url = longTobyDressMap.containsKey(NumberUtils.toLong(t.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(t.getRelateVirtualItemId())).getUrl() : t.getImage();
                        entity.setId(t.getId());
                        entity.setUrl(url);
                        entity.setName(t.getProductName());
                        this.processCountenanceTag(user.getId(), t.getTags(), entity);

                        newRewardLoaderClient.proTobyAndHeadWearPrice(t);

                        List<TobyDressCVOption> cvOption = new ArrayList<>();
                        List<RewardProductDetail.PriceDay> priceDays = t.getPriceDay();
                        if (priceDays != null) {
                            cvOption = priceDays
                                    .stream()
                                    .map(p -> {
                                        TobyDressCVOption option = new TobyDressCVOption();
                                        option.setActiveDay(p.getDay());
                                        option.setPrice(p.getPrice());
                                        option.setPriceNumType(p.getQuantity());
                                        return option;
                                    }).collect(Collectors.toList());
                        }

                        entity.setCvOption(cvOption);
                        entity.setSpendType(t.getSpendType());
                        countenanceList.add(entity);
                    }
                    //如果是道具
                    else if (twoLevelCategoryIdSet.contains(newRewardBufferLoaderClient.getProductCategoryBuffer().getTwoLevelCategoryId(TwoLevelCategoryType.TOBY_PROPS.intType()))) {
                        TobyPropsEntity entity = new TobyPropsEntity();
                        String url = longTobyDressMap.containsKey(NumberUtils.toLong(t.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(t.getRelateVirtualItemId())).getUrl() : t.getImage();
                        entity.setId(t.getId());
                        entity.setUrl(url);
                        entity.setName(t.getProductName());
                        this.processPropsTag(user.getId(), t.getTags(), entity);

                        newRewardLoaderClient.proTobyAndHeadWearPrice(t);
                        List<TobyDressCVOption> cvOption = new ArrayList<>();
                        List<RewardProductDetail.PriceDay> priceDays = t.getPriceDay();
                        if (priceDays != null) {
                            cvOption = priceDays
                                    .stream()
                                    .map(p -> {
                                        TobyDressCVOption option = new TobyDressCVOption();
                                        option.setActiveDay(p.getDay());
                                        option.setPrice(p.getPrice());
                                        option.setPriceNumType(p.getQuantity());
                                        return option;
                                    }).collect(Collectors.toList());
                        }

                        entity.setCvOption(cvOption);
                        entity.setSpendType(t.getSpendType());
                        propsList.add(entity);
                    }
                    //如果是饰品
                    else if (twoLevelCategoryIdSet.contains(newRewardBufferLoaderClient.getProductCategoryBuffer().getTwoLevelCategoryId(TwoLevelCategoryType.TOBY_ACCESSORY.intType()))) {
                        TobyAccessoryEntity entity = new TobyAccessoryEntity();
                        String url = longTobyDressMap.containsKey(NumberUtils.toLong(t.getRelateVirtualItemId())) ? longTobyDressMap.get(NumberUtils.toLong(t.getRelateVirtualItemId())).getUrl() : t.getImage();
                        entity.setId(t.getId());
                        entity.setUrl(url);
                        entity.setName(t.getProductName());
                        this.processAccessoryTag(user.getId(), t.getTags(), entity);

                        // 计算价格列表
                        BigDecimal price = new BigDecimal(t.getPrice()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
                        int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

                        RewardProductDetail.PriceDay priceDay1 = new RewardProductDetail.PriceDay();
                        priceDay1.setDay(t.getExpiryDate());
                        priceDay1.setPrice(t.getPrice());
                        priceDay1.setQuantity(1);

                        RewardProductDetail.PriceDay priceDay2 = new RewardProductDetail.PriceDay();
                        priceDay2.setDay(t.getExpiryDate() * 2);
                        priceDay2.setPrice(SafeConverter.toDouble(totalPrice));
                        priceDay2.setQuantity(2);
                        t.setPriceDay(Arrays.asList(priceDay1, priceDay2));
                        List<TobyDressCVOption> cvOption = new ArrayList<>();
                        List<RewardProductDetail.PriceDay> priceDays = t.getPriceDay();
                        if (priceDays != null) {
                            cvOption = priceDays
                                    .stream()
                                    .map(p -> {
                                        TobyDressCVOption option = new TobyDressCVOption();
                                        option.setActiveDay(p.getDay());
                                        option.setPrice(p.getPrice());
                                        option.setPriceNumType(p.getQuantity());
                                        return option;
                                    }).collect(Collectors.toList());
                        }

                        entity.setCvOption(cvOption);
                        entity.setSpendType(t.getSpendType());
                        accessoryList.add(entity);
                    }
                });

        resultMsg.add("countenanceList", countenanceList);
        resultMsg.add("imageList", imageList);
        resultMsg.add("propsList", propsList);
        resultMsg.add("accessoryList", accessoryList);

        return resultMsg;
    }

    /**
     * 能量柱奖品池
     *
     * @return
     */
    @RequestMapping(value = "/powerPrizePool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPowerPizePool() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if ((user instanceof StudentDetail) == false) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        PowerPizePoolMapper mapper = rewardCenterClient.loadPowerPrizePool(user);
        resultMsg.add("fragmentNum", 0);
        resultMsg.add("fullPowerNumber", mapper.getFullPowerNumber());
        resultMsg.add("powerPillar", mapper.getPowerPillar());
        resultMsg.add("realGoodsList", mapper.getRealGoodsList());

        return resultMsg;
    }

    /**
     * 尝试获取能量柱奖励
     *
     * @return
     */
    @RequestMapping(value = "/powerPrize.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tryPowerPize() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        TryPowerPizeMapper mapper;

        MapMessage resultMsg = MapMessage.successMessage();
        if ((user instanceof StudentDetail) == false) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        try {
            mapper =  AtomicCallbackBuilderFactory.getInstance()
                    .<TryPowerPizeMapper>newBuilder()
                    .keyPrefix("tryPowerPize:userId")
                    .keys(user.getId())
                    .callback(() ->  rewardCenterClient.tryPowerPize(user))
                    .build()
                    .execute();
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        }

        resultMsg.add("isPrize", mapper.getIsPize());
        resultMsg.add("tip", mapper.getTip());
        resultMsg.add("prizeList", mapper.getPrizeList());

        return resultMsg;
    }

    /**
     * 我的背包
     *
     * @return
     */
    @RequestMapping(value = "/myBackpack.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMyBackpack() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if ((user instanceof StudentDetail) == false) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        List<MyBackpackMapper> mappers = rewardCenterClient.loadMyBackpack(user);
        resultMsg.add("myPrizeList", mappers);

        return resultMsg;
    }

    /**
     * 我的Toby 数据获取
     *
     * @return
     */
    @RequestMapping(value = "/myTobyShow.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMyTobyShow() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        MyTobyShowMapper mapper = rewardCenterClient.loadMyTobyShow(user);
        resultMsg.add("integralNum", mapper.getIntegralNum());
        resultMsg.add("fragmentNum", 0);
        resultMsg.add("expiryDressNameList", mapper.getExpiryDressNameList());
        resultMsg.add("isChangeAvatar", mapper.getIsChangeAvatar());
        resultMsg.add("toby", mapper.getToby());

        return resultMsg;
    }

    /**
     * 兑换Toby装扮
     *
     * @return
     */
    @RequestMapping(value = "/cvTobyDress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cvTobyDress() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        String json = getRequestParameter("data", "");
        Map<String, Object> map = JsonUtils.fromJson(json);
        if (map == null || map.isEmpty()) {
            return MapMessage.errorMessage();
        }
        Map<String, Integer> countenanceMap = (Map<String, Integer>) map.get("countenance");
        Map<String, Integer> imageMap = (Map<String, Integer>) map.get("image");
        Map<String, Integer> propsMap = (Map<String, Integer>) map.get("props");
        Map<String, Integer> accessoryMap = (Map<String, Integer>) map.get("accessory");
        try {
            long cvImageId = 0;
            long cvCountenanceId = 0;
            long cvPropsId = 0;
            long cvAccessoryId = 0;
            long useImageId = 0;
            long useCountenanceId = 0;
            long usePropsId = 0;
            long useAccessoryId = 0;
            int imageType = 0;
            int countenanceType = 0;
            int propsType = 0;
            int accessoryType = 0;
            int imagePriceNumType = 0;
            int countenancePriceNumType = 0;
            int propsPriceNumType = 0;
            int accessoryPriceNumType = 0;
            if (countenanceMap != null && !countenanceMap.isEmpty()) {
                countenanceType = countenanceMap.get("type");
                if (countenanceType == CVTobyDressArgsEntity.CVType.USE.intValue()) {
                    useCountenanceId = countenanceMap.get("id");
                } else {
                    countenancePriceNumType = countenanceMap.containsKey("priceNumType") ? countenanceMap.get("priceNumType") : 0;
                    cvCountenanceId = countenanceMap.get("id");
                }
            }
            if (accessoryMap != null && !accessoryMap.isEmpty()) {
                accessoryType = accessoryMap.get("type");
                if (accessoryType == CVTobyDressArgsEntity.CVType.USE.intValue()) {
                    useAccessoryId = accessoryMap.get("id");
                } else {
                    accessoryPriceNumType = accessoryMap.containsKey("priceNumType") ? accessoryMap.get("priceNumType") : 0;
                    cvAccessoryId = accessoryMap.get("id");
                }
            }
            if (imageMap != null && !imageMap.isEmpty()) {
                imageType = imageMap.get("type");
                if (imageType == CVTobyDressArgsEntity.CVType.USE.intValue()) {
                    useImageId = imageMap.get("id");
                } else {
                    imagePriceNumType = imageMap.containsKey("priceNumType") ? imageMap.get("priceNumType") : 0;
                    cvImageId = imageMap.get("id");
                }
            }
            if (propsMap != null && !propsMap.isEmpty()) {
                propsType = propsMap.get("type");
                if (propsType == CVTobyDressArgsEntity.CVType.USE.intValue()) {
                    usePropsId = propsMap.get("id");
                } else {
                    cvPropsId = propsMap.get("id");
                    propsPriceNumType = propsMap.containsKey("priceNumType") ? propsMap.get("priceNumType") : 0;
                }
            }
            Map<Long, Integer> productIdSpendNumMap = new HashMap<>();

            if (cvImageId != 0L) {
                productIdSpendNumMap.put(cvImageId, imagePriceNumType);
            }
            if (cvCountenanceId != 0L) {
                productIdSpendNumMap.put(cvCountenanceId, countenancePriceNumType);
            }
            if (cvAccessoryId != 0L) {
                productIdSpendNumMap.put(cvAccessoryId, accessoryPriceNumType);
            }
            if (cvPropsId != 0L) {
                productIdSpendNumMap.put(cvPropsId, propsPriceNumType);
            }
            List<Long> useProductIdList = new ArrayList<>();
            if (useImageId != 0L) {
                useProductIdList.add(useImageId);
            }
            if (useCountenanceId != 0L) {
                useProductIdList.add(useCountenanceId);
            }
            if (useAccessoryId != 0L) {
                useProductIdList.add(useAccessoryId);
            }
            if (usePropsId != 0L) {
                useProductIdList.add(usePropsId);
            }

            if (productIdSpendNumMap != null && !productIdSpendNumMap.isEmpty()) {
                boolean checkResult = rewardCenterClient.checkCost(user, productIdSpendNumMap);
                if (!checkResult) {
                    return MapMessage.errorMessage("兑换失败，学豆或者碎片不足！");
                }
            }

            for (Map.Entry<Long, Integer> entry : productIdSpendNumMap.entrySet()) {
                int quantity = entry.getValue();
                RewardProductDetail productDetail = rewardLoaderClient.generateUserRewardProductDetail(user, entry.getKey());
                if (productDetail == null) {
                    logger.warn("cvTobyDress warn productDetail is empty productId:{}", entry.getKey());
                    continue;
                }
                try {

                    AtomicCallbackBuilderFactory.getInstance()
                            .<MapMessage>newBuilder()
                            .keyPrefix("RewardService:createRewardOrder")
                            .keys(user.getId(), entry.getKey())
                            .callback(() -> newRewardServiceClient.createRewardOrder(user, productDetail, null, quantity, null, RewardOrder.Source.app, null))
                            .build()
                            .execute();
                } catch (CannotAcquireLockException ex) {
                    logger.error("Failed to create reward order (user={},product={},quantity={}): DUPLICATED OPERATION",
                            user.getId(), productDetail.getId(), quantity);
                    MapMessage.errorMessage();
                } catch (Exception ex) {
                    logger.error("Failed to create reward order (user={},product={},quantity={})",
                            user.getId(), productDetail.getId(), quantity, ex);
                    MapMessage.errorMessage();
                }
                rewardCenterClient.ctTobyDress(user.getId(), productDetail);
            }

            if (!useProductIdList.isEmpty()) {
                useProductIdList.stream().forEach(t -> {
                    RewardProductDetail productDetail = rewardLoaderClient.generateUserRewardProductDetail(user, t);
                    if (productDetail == null) {
                        logger.warn("cvTobyDress warn productDetail is empty productId:{}", t);
                    } else {
                        rewardCenterClient.ctTobyDress(user.getId(), productDetail);
                    }
                });
            }

        } catch (Exception e) {
            resultMsg = MapMessage.errorMessage();
        }

        return resultMsg;
    }

    public void processCountenanceTag(long userId, String tags, TobyCountenanceEntity entity) {
        if (tags.contains(RewardTagEnum.托比公益.getName())) {
            Long publicGoodsTime = rewardCenterClient.getDonationCount(userId);
            entity.setPublicGoodsTimes(publicGoodsTime == null ? 0 : publicGoodsTime.intValue());
            if (publicGoodsTime != null && publicGoodsTime >= 5) {
                entity.setCvAuthority(true);
                entity.setUnallowCVTip(null);
            } else {
                entity.setCvAuthority(false);
                entity.setUnallowCVTip("7月13日后参加过" + publicGoodsTime + "/5次公益活动才有兑换资格");
            }
            entity.setTag(RewardTagEnum.托比公益.getName());
        } else {
            entity.setCvAuthority(true);
            entity.setUnallowCVTip(null);
            entity.setTag(null);
        }
    }

    public void processImageTag(long userId, String tags, TobyImageEntity entity) {
        if (tags.contains(RewardTagEnum.托比公益.getName())) {
            Long publicGoodsTime = rewardCenterClient.getDonationCount(userId);
            entity.setPublicGoodsTimes(publicGoodsTime == null ? 0 : publicGoodsTime.intValue());
            if (publicGoodsTime != null && publicGoodsTime >= 5) {
                entity.setCvAuthority(true);
                entity.setUnallowCVTip(null);
            } else {
                entity.setCvAuthority(false);
                entity.setUnallowCVTip("7月13日后参加过" + publicGoodsTime + "/5次公益活动才有兑换资格");
            }
            entity.setTag(RewardTagEnum.托比公益.getName());
        } else if (tags.contains(RewardTagEnum.托比抓一抓.getName())) {
            entity.setCvAuthority(false);
            entity.setUnallowCVTip("小丑托比需要从抓一抓获得哦");
            entity.setTag(RewardTagEnum.托比抓一抓.getName());
        } else {
            entity.setCvAuthority(true);
            entity.setUnallowCVTip(null);
            entity.setTag(null);
        }
    }

    public void processPropsTag(long userId, String tags, TobyPropsEntity entity) {
        if (tags.contains(RewardTagEnum.托比公益.getName())) {
            Long publicGoodsTime = rewardCenterClient.getDonationCount(userId);
            entity.setPublicGoodsTimes(publicGoodsTime == null ? 0 : publicGoodsTime.intValue());
            if (publicGoodsTime != null && publicGoodsTime >= 5) {
                entity.setCvAuthority(true);
                entity.setUnallowCVTip(null);
            } else {
                entity.setCvAuthority(false);
                entity.setUnallowCVTip("7月13日后参加过" + publicGoodsTime + "/5次公益活动才有兑换资格");
            }
            entity.setTag(RewardTagEnum.托比公益.getName());
        } else {
            entity.setCvAuthority(true);
            entity.setUnallowCVTip(null);
            entity.setTag(null);
        }
    }

    public void processAccessoryTag(long userId, String tags, TobyAccessoryEntity entity) {
        if (tags.contains(RewardTagEnum.托比公益.getName())) {
            Long publicGoodsTime = rewardCenterClient.getDonationCount(userId);
            entity.setPublicGoodsTimes(publicGoodsTime == null ? 0 : publicGoodsTime.intValue());
            if (publicGoodsTime != null && publicGoodsTime >= 5) {
                entity.setCvAuthority(true);
                entity.setUnallowCVTip(null);
            } else {
                entity.setCvAuthority(false);
                entity.setUnallowCVTip("7月13日后参加过" + publicGoodsTime + "/5次公益活动才有兑换资格");
            }
            entity.setTag(RewardTagEnum.托比公益.getName());
        } else {
            entity.setCvAuthority(true);
            entity.setUnallowCVTip(null);
            entity.setTag(null);
        }
    }

    /**
     * 保存toby装备为头像
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/saveTobyAvatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTobuAvatar(@RequestParam MultipartFile avatar_dat) {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg;
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        try {
            String id = RandomUtils.nextObjectId();
            String filename = userImageUploader.uploadAvatarFromMultipartFile(user.getId(), id, avatar_dat);
            userServiceClient.userImageUploaded(user.getId(), filename, id);
            rewardCenterClient.updateAvaterType(user.getId(), 1);
            return MapMessage.successMessage().add("avatarUrl", getUserAvatarImgUrl(filename));
        } catch (Exception e) {
            resultMsg = MapMessage.errorMessage();
        }

        return resultMsg;
    }

    /**
     * 保存toby装备为头像
     *
     * @return
     */
    @RequestMapping(value = "/saveTobyAvatarSafe.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTobyAvatarSafe(@RequestBody TobyAvatarMapper mapper) {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg;
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        if (mapper==null) {
            return MapMessage.errorMessage("图片无效！");
        }
        try {
            Map<Long, RewardProduct> productMap = new HashMap<>();
            BufferedImage image = TobyAvatar.getDefaultImg();
            Map<Long, TobyAvatarMapper.TobyAvatarUnit> tobyAvatarUnitMap = new HashMap<>();
            if (mapper.getTobyAccessory() != null && SafeConverter.toLong(mapper.getTobyAccessory().getProductId()) > 0) {
                RewardProduct product = rewardLoaderClient.loadRewardProduct(mapper.getTobyAccessory().getProductId());
                productMap.put(mapper.getTobyAccessory().getProductId(), product);
                tobyAvatarUnitMap.put(mapper.getTobyAccessory().getProductId(),mapper.getTobyAccessory());
            }
            if (mapper.getTobyCountenance() != null && SafeConverter.toLong(mapper.getTobyCountenance().getProductId()) > 0) {
                image = TobyAvatar.getBGImg();
                RewardProduct product = rewardLoaderClient.loadRewardProduct(mapper.getTobyCountenance().getProductId());
                productMap.put(mapper.getTobyCountenance().getProductId(), product);
                tobyAvatarUnitMap.put(mapper.getTobyCountenance().getProductId(),mapper.getTobyCountenance());
            }
            if (mapper.getTobyImage() != null && SafeConverter.toLong(mapper.getTobyImage().getProductId()) > 0) {
                RewardProduct product = rewardLoaderClient.loadRewardProduct(mapper.getTobyImage().getProductId());
                productMap.put(mapper.getTobyImage().getProductId(), product);
                tobyAvatarUnitMap.put(mapper.getTobyImage().getProductId(),mapper.getTobyImage());
            }
            if (mapper.getTobyProps() != null && SafeConverter.toLong(mapper.getTobyProps().getProductId()) > 0) {
                RewardProduct product = rewardLoaderClient.loadRewardProduct(mapper.getTobyProps().getProductId());
                productMap.put(mapper.getTobyProps().getProductId(), product);
                tobyAvatarUnitMap.put(mapper.getTobyProps().getProductId(),mapper.getTobyProps());
            }

            if (MapUtils.isEmpty(tobyAvatarUnitMap)) {
                return upLoadAvatar(user.getId(), TobyAvatar.getDefaultImgBytes());
            }

            Map<Long, Long> tobyDerssIdMap = productMap.values()
                    .stream()
                    .filter(rewardProduct -> StringUtils.isNotBlank(rewardProduct.getRelateVirtualItemId()) && SafeConverter.toLong(rewardProduct.getRelateVirtualItemId()) > 0)
                    .collect(Collectors.toMap(rewardProduct -> SafeConverter.toLong(rewardProduct.getRelateVirtualItemId()), rewardProduct -> rewardProduct.getId(), (ov, nv) -> nv));

            List<TobyDress> tobyDressList = rewardCenterClient.loadTobyDressByIds(tobyDerssIdMap.keySet());
            Map<Long, String> productIdUrlMap = tobyDressList.stream()
                    .filter(dress -> tobyDerssIdMap.containsKey(dress.getId()))
                    .map(dress ->  new AbstractMap.SimpleEntry<Long, String>(tobyDerssIdMap.get(dress.getId()), dress.getUrl()) )
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

            //盖图片顺序不能乱
            if (productIdUrlMap.containsKey(mapper.getTobyImage().getProductId()) && tobyAvatarUnitMap.containsKey(mapper.getTobyImage().getProductId())) {
                TobyAvatarMapper.TobyAvatarUnit tobyAvatarUnit = tobyAvatarUnitMap.get(mapper.getTobyImage().getProductId());
                image = TobyAvatar.pressImage(image, productIdUrlMap.get(mapper.getTobyImage().getProductId()), tobyAvatarUnit.getX(), tobyAvatarUnit.getY());
            }
            if (productIdUrlMap.containsKey(mapper.getTobyCountenance().getProductId()) && tobyAvatarUnitMap.containsKey(mapper.getTobyCountenance().getProductId())) {
                TobyAvatarMapper.TobyAvatarUnit tobyAvatarUnit = tobyAvatarUnitMap.get(mapper.getTobyCountenance().getProductId());
                image = TobyAvatar.pressImage(image, productIdUrlMap.get(mapper.getTobyCountenance().getProductId()), tobyAvatarUnit.getX(), tobyAvatarUnit.getY());
            }
            if (productIdUrlMap.containsKey(mapper.getTobyAccessory().getProductId()) && tobyAvatarUnitMap.containsKey(mapper.getTobyAccessory().getProductId())) {
                TobyAvatarMapper.TobyAvatarUnit tobyAvatarUnit = tobyAvatarUnitMap.get(mapper.getTobyAccessory().getProductId());
                image = TobyAvatar.pressImage(image, productIdUrlMap.get(mapper.getTobyAccessory().getProductId()), tobyAvatarUnit.getX(), tobyAvatarUnit.getY());
            }
            if (productIdUrlMap.containsKey(mapper.getTobyProps().getProductId()) && tobyAvatarUnitMap.containsKey(mapper.getTobyProps().getProductId())) {
                TobyAvatarMapper.TobyAvatarUnit tobyAvatarUnit = tobyAvatarUnitMap.get(mapper.getTobyProps().getProductId());
                image = TobyAvatar.pressImage(image, productIdUrlMap.get(mapper.getTobyProps().getProductId()), tobyAvatarUnit.getX(), tobyAvatarUnit.getY());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return upLoadAvatar(user.getId(), out.toByteArray());
        } catch (Exception e) {
            resultMsg = MapMessage.errorMessage();
        }
        return resultMsg;
    }

    private MapMessage upLoadAvatar(Long userId, byte[] bytes) throws IOException {
        String id = RandomUtils.nextObjectId();
        String filename = userImageUploader.uploadAvatar(userId, id, bytes);
        userServiceClient.userImageUploaded(userId, filename, id);
        rewardCenterClient.updateAvaterType(userId, 1);
        return MapMessage.successMessage().add("avatarUrl", getUserAvatarImgUrl(filename));
    }

    /**
     * 留言 数据获取
     *
     * @return
     */
    @RequestMapping(value = "/zoneDynamicsList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getZoneDynamicsList() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        Long businessUserId = getRequestLong("userId", 0L);
        if (businessUserId == 0) {
            businessUserId = user.getId();
        }
        List<ZoneDynamicsMapper> mappers = rewardCenterClient.loadZoneDynamics(businessUserId);
        if (mappers != null && !mappers.isEmpty()) {
            mappers.stream()
                    .forEach(yearData -> {
                        ListUtils.emptyIfNull(yearData.getYearDataList())
                                .stream()
                                .forEach(dayData -> {
                                    ListUtils.emptyIfNull(dayData.getDayDataList())
                                            .stream()
                                            .forEach(data -> {
                                                data.setVisitorPortraitUrl(getUserAvatarImgUrl(data.getVisitorPortraitUrl()));
                                            });
                                });
                    });
        }
        resultMsg.add("leaveWordList", mappers);

        return resultMsg;
    }

    /**
     * 留言 数据获取
     *
     * @return
     */
    @RequestMapping(value = "/leaveWordList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLeaveWordList() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        List<LeaveWordMapper> mappers = null;

        Long businessUserId = getRequestLong("userId", 0L);
        if (businessUserId == 0) {
            mappers = rewardCenterClient.loadUnreadLeaveWordList(user.getId());
            resultMsg.add("isHasNewLeaveWord", rewardCenterClient.isHasUnreadLeaveWord(user.getId()));
        } else {
            mappers = rewardCenterClient.loadLeaveWordList(businessUserId);
            if (mappers != null && mappers.size() > 10) {
                mappers = mappers.subList(0, 9);
            }
        }
        if (mappers != null) {
            mappers.stream()
                    .forEach(t -> {
                        t.setVisitorPortraitUrl(getUserAvatarImgUrl(t.getVisitorPortraitUrl()));
                    });
        }
        resultMsg.add("leaveWordList", mappers);

        return resultMsg;
    }

    /**
     * 留言物品 数据获取
     *
     * @return
     */
    @RequestMapping(value = "/leaveWordGoodsList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getLeaveWordGoodsList() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        LeaveWordGoodsListMapper mapper = rewardCenterClient.loadLeaveWordGoodsList(user);
        resultMsg.add("leaveWordGoodsList", mapper.getLeaveWordGoodsList());
        resultMsg.add("fragmentNum", mapper.getFragmentNum());
        resultMsg.add("integralNum", mapper.getIntegralNum());

        return resultMsg;
    }


    /**
     * 获取同学的Toby秀
     *
     * @return
     */
    @RequestMapping(value = "/classmateTobyList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClassmateTobyList() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        StudentDetail student = (StudentDetail) user;
        long clazzId = student.getClazzId();
        Map<Long, List<User>> clazzStudentMap = studentLoaderClient.loadClazzStudents(Collections.singleton(clazzId));
        if (clazzStudentMap == null || clazzStudentMap.isEmpty() || !clazzStudentMap.containsKey(clazzId)) {
            logger.warn(String.format("loadClazzStudents is empty student:%s", student.toString()));
            return MapMessage.errorMessage("班级非法的请求！");
        }

        Map<Long, User> userMap = clazzStudentMap.get(clazzId)
                .stream()
                .filter(p -> !Objects.equals(p.fetchRealname(), UserConstants.EXPERIENCE_ACCOUNT_NAME))
                .collect(Collectors.toMap(u -> u.getId(), Function.identity()));

        List<RewardCenterToby> tobyList = rewardCenterClient.loadClassmateTobyList(new HashSet<>(userMap.keySet()));
        List<ClassmateTobyListMapper> mappers = tobyList
                .stream()
                .filter(t -> !user.getId().equals(t.getUserId()))
                .map(t -> {

                    ClassmateTobyListMapper mapper = new ClassmateTobyListMapper();
                    mapper.setUserId(t.getUserId());
                    if (userMap.containsKey(t.getUserId())) {
                        mapper.setUserName(userMap.get(t.getUserId()).fetchRealname());
                        mapper.setUserPortraitUrl(getUserAvatarImgUrl(userMap.get(t.getUserId())));
                    } else {
                        logger.warn(String.format("userMap:%s is mot include userId:%s", userMap, t.getUserId()));
                    }
                    ClassmateTobyListMapper.ClassmateToby toby = mapper.new ClassmateToby();
                    ClassmateTobyListMapper.Accessory accessory = mapper.new Accessory();
                    ClassmateTobyListMapper.Countenance countenance = mapper.new Countenance();
                    ClassmateTobyListMapper.Image image = mapper.new Image();
                    ClassmateTobyListMapper.Props props = mapper.new Props();

                    countenance.setId(t.getCountenanceId());
                    countenance.setUrl(t.getCountenanceUrl());
                    image.setId(t.getImageId());
                    image.setUrl(t.getImageUrl());
                    image.setName(t.getImageName());
                    if (t.getPropsId() != null) {
                        props.setId(t.getPropsId());
                        props.setUrl(t.getPropsUrl());
                        toby.setProps(props);
                    }
                    if (t.getAccessoryId() != null) {
                        accessory.setId(t.getAccessoryId());
                        accessory.setUrl(t.getAccessoryUrl());
                        toby.setAccessory(accessory);
                    }

                    toby.setCountenance(countenance);
                    toby.setImage(image);
                    mapper.setToby(toby);

                    return mapper;
                }).collect(Collectors.toList());
        resultMsg.add("classmateList", mappers);

        return resultMsg;
    }

    /**
     * 获取同学的Toby秀
     *
     * @return
     */
    @RequestMapping(value = "/classmateToby.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClassmateToby() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        Long businessUserId = getRequestLong("userId", 0L);
        StudentDetail student = studentLoaderClient.loadStudentDetail(businessUserId);
        if (student == null) {
            logger.warn(String.format("student is empty userId:%s, businessUserId:%s", user.getId(), businessUserId));
            return MapMessage.errorMessage("学生非法的请求！");
        }
        RewardCenterToby t = rewardCenterClient.loadClassmateToby(businessUserId);
        ClassmateTobyMapper mapper = new ClassmateTobyMapper();
        ClassmateTobyMapper.ClassmateToby toby = mapper.new ClassmateToby();

        if (t.getAccessoryId() != null) {
            ClassmateTobyMapper.Accessory accessory = mapper.new Accessory();
            accessory.setId(t.getAccessoryId());
            accessory.setUrl(t.getAccessoryUrl());
            toby.setAccessory(accessory);
        }
        if (t.getCountenanceId() != null) {
            ClassmateTobyMapper.Countenance countenance = mapper.new Countenance();
            countenance.setId(t.getCountenanceId());
            countenance.setUrl(t.getCountenanceUrl());
            toby.setCountenance(countenance);
        }
        if (t.getImageId() != null) {
            ClassmateTobyMapper.Image image = mapper.new Image();
            image.setId(t.getImageId());
            image.setUrl(t.getImageUrl());
            toby.setImage(image);
        }
        if (t.getPropsId() != null) {
            ClassmateTobyMapper.Props props = mapper.new Props();
            props.setId(t.getPropsId());
            props.setUrl(t.getPropsUrl());
            toby.setProps(props);
        }

        mapper.setToby(toby);

        resultMsg.add("userId", student.getId());
        resultMsg.add("userName", student.fetchRealname());
        resultMsg.add("toby", mapper.getToby());

        return resultMsg;
    }

    /**
     * 给同学留言
     *
     * @return
     */
    @RequestMapping(value = "/leaveWord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage leaveWord() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        Long businessUserId = getRequestLong("userId", 0L);
        Long leaveWordId = getRequestLong("leaveWordId", 0L);
        return rewardCenterClient.leaveWord(user, businessUserId, leaveWordId);
    }

    // ------------------- 抓一抓小游戏 华丽的分割一下----------------------

    /**
     * 抓一抓游戏场次入口
     *
     * @return
     */
    @RequestMapping(value = "/prizeClawGame.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPrizeClawGame() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }

        int site = getRequestInt("site", 1);
        boolean fromParentPage = getRequestBool("fromParentPage", false);
        List<PrizeClawMapper> mappers = rewardCenterClient.loadPrizeClawGame(site);
        StudentDetail studentDetail = (StudentDetail) user;
        // fixme 在controller中拿次数很不方便，先写死
        long integralNum = studentDetail.getUserIntegral().getUsable();
        if ((site == 1 && integralNum < 5) || (site == 2 && integralNum < 15)) {
            resultMsg.add("isLessIntegral", true);
        } else {
            resultMsg.add("isLessIntegral", false);
        }
        if (fromParentPage) {
            resultMsg.add("isShowNewcomersTip", rewardCenterClient.isShowNewcomersTip(user.getId()));
        } else {
            resultMsg.add("isShowNewcomersTip", false);
        }

        Boolean isOneDayLimit = rewardCenterClient.tryGameOneDayLimit(user.getId());
        resultMsg.add("isOneDayLimit", isOneDayLimit);
        resultMsg.add("prizeList", mappers);
        return resultMsg;
    }

    /**
     * 抓一抓中奖纪录
     *
     * @return
     */
    @RequestMapping(value = "/prizeClawRecord.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPrizeClawRecord() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        User user = currentUser();
        if (!user.isStudent()) {
            return MapMessage.errorMessage("身份非法的请求！");
        }
        PrizeClawWinningRecordMapper mapper = rewardCenterClient.loadPrizeClawWinningRecord(user.getId());
        resultMsg.add("consumeSum", mapper.getConsumeSum());
        resultMsg.add("winningSum", mapper.getWinningSum());
        resultMsg.add("prizeRecordList", mapper.getPrizeRecordList());
        return resultMsg;
    }

    /**
     * 抓一抓游戏逻辑
     *
     * @return
     */
    @RequestMapping(value = "/prizeClawJudge.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage prizeClawJudge() {
        return MapMessage.errorMessage("功能已下线，试试别的吧!");
//        if (currentUser() == null) {
//            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
//        }
//
//        User user = currentUser();
//        if (!user.isStudent()) {
//            return MapMessage.errorMessage("身份非法的请求！");
//        }
//
//        int id = getRequestInt("id");
//        return rewardCenterClient.clawJudge(user, id);
    }

    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page) {
        StudentDetail studentDetail = currentStudentDetail();
        if (studentDetail == null) {
            return "studentmobile/logininvalid";
        }
        //一直无法升级的旧版本还在访问已被删除的ranklist页面。故直接重定向到下载页面
        if ("ranklist".equals(page)) {
            return "redirect:http://wx.17zuoye.com/download/17studentapp?cid=100158";
        }

        String file = "studentmobile";
        String version = getRequestString("app_version");
        String newPagesList = "likelist, fairyland, myprivilege, downloadparentapp,fairylandBack"; //如果studentmobile下有老ftl页面，请在这里配置 只进新studentmobilev3 目录下ftl , 防止没有version进到老目录下就报错
        if (StringUtils.isNotBlank(version) && VersionUtil.compareVersion(version, "2.7.0.0") >= 0 || newPagesList.contains(page)) {
            file = "studentmobilev3";

            //灰度新版课外乐园
            if ("fairyland".equals(page)) {
                page = "fairylandNew";
            }
        }

        return file + "/center/" + page;
    }

    //  返回学豆记录（原生控制入口）
    @RequestMapping(value = "integral.vpage")
    public String integral() {
        return "redirect:/view/mobile/student/center/integral";
    }

    /**
     * 移动端学生个人中心首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        User user = currentUser();
        return "";
    }

    /**
     * 移动端学生个人中心 -- 金币历史页面
     */
    @RequestMapping(value = "mygold.vpage", method = RequestMethod.GET)
    public String integralHistory(Model model) {
        return "";
    }

    /**
     * 移动端学生个人中心 -- 账号安全页面
     */
    @RequestMapping(value = "security.vpage", method = RequestMethod.GET)
    public String teacherAccountSecurity(Model model) {
        return "";
    }

    /**
     * 土豪榜
     */
    @RequestMapping(value = "silverrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage silverRank() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        StudentDetail student = currentStudentDetail();
        final Clazz clazz = student.getClazz();
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (clazz != null) {
            rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(zoneLoaderClient.getZoneLoader())
                    .expiration(1800)
                    .keyPrefix("CLAZZ_WEALTHIEST_RANK")
                    .keys(clazz.getId(), student.getId())
                    .proxy()
                    .silverRank(clazz, student.getId());
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 学霸榜
     */
    @RequestMapping(value = "smcountrank.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage smCountRank() {

        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        StudentDetail student = currentStudentDetail();
        Clazz clazz = student.getClazz();
        List<Map<String, Object>> rankList = new ArrayList<>();
        if (clazz != null) {
            rankList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(zoneLoaderClient.getZoneLoader())
                    .expiration(1800)
                    .keyPrefix("CLAZZ_SMCOUNT_RANK")
                    .keys(clazz.getId(), student.getId())
                    .proxy()
                    .studyMasterCountRank(clazz, student.getId());
        }
        return MapMessage.successMessage().add("rankList", rankList);
    }

    /**
     * 中学奖品中心(只返回全部分类)
     */
    @RequestMapping(value = "juniorreward.vpage", method = RequestMethod.GET)
    public void juniorReward(Model model, HttpServletResponse response) {
        try {
            response.sendRedirect(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/common/reward");
        } catch (Exception e) {
            logger.error("juniorreward redirect error", e);
        }
//        User user = currentUser();
//        if(studentUnLogin()){
//            return "studentmobile/logininvalid";
//        }
//        //分类
//        RewardCategoryLoader rewardCategoryLoader = rewardLoaderClient.getRewardCategoryLoader();
//        Collection<RewardCategory> categories = rewardCategoryLoader.loadRewardCategories(RewardProductType.JPZX_SHIWU, user.fetchUserType());
//        //剩余学豆
//        Integral integral = integralLoaderClient.loadIntegral(user.getId());
//        model.addAttribute("categories", categories);
//        model.addAttribute("usable_integral", integral == null ? 0 : integral.getUsableIntegral());
//        return "mobile/student/junior/prize/list";
    }

    /**
     * 获取奖品中心的活动列表
     *
     * @return
     */
    @RequestMapping(value = "/reward/activities.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadRewardActivities() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        List<RewardActivity> onGoingAs = new ArrayList<>();
        List<RewardActivity> finisheAs = new ArrayList<>();

        // 筛选出来上线的
        rewardLoaderClient.loadRewardActivities()
                .stream()
                .filter(RewardActivity::getOnline)
                // 按时间倒序，最新的在最前面
                // 这个改成按照排序值来排序
                //.sorted((a1, a2) -> a2.getCreateDatetime().compareTo(a1.getCreateDatetime()))
                .sorted((a1, a2) -> {
                    Integer a2Ow = SafeConverter.toInt(a2.getOrderWeights());
                    Integer a1Ow = SafeConverter.toInt(a1.getOrderWeights());

                    return a2Ow.compareTo(a1Ow);
                })
                .collect(Collectors.toList())
                .forEach(a -> {
                    if (Objects.equals(a.getStatus(), RewardActivity.Status.ONGOING.name())) {
                        onGoingAs.add(a);
                    } else if (Objects.equals(a.getStatus(), RewardActivity.Status.FINISHED.name())) {
                        finisheAs.add(a);
                    }
                    // 算下完成百分比
                    a.calculateProgress();
                });

        // 重新排序，按照进度从低到高
        // onGoingAs.sort((a1, a2) -> a1.getProgress().compareTo(a2.getProgress()));

        resultMsg.add("onGoingActivities", onGoingAs);
        resultMsg.add("finishedActivities", finisheAs);
        return resultMsg;
    }

    /**
     * 获取活动详情信息
     *
     * @return
     */
    @RequestMapping(value = "/reward/activity.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityDetail() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }

        // 查询图片，做轮播用
        List<String> images = rewardLoaderClient.loadActivityImages(activityId)
                .stream()
                .map(RewardActivityImage::getLocation)
                .collect(Collectors.toList());

        activity.setImages(images);
        activity.calculateProgress();

        // 兼容旧数据，如果没有完成时间，用创建时间补充上
        if (activity.getFinishTime() == null)
            activity.setFinishTime(new Date());

        MapMessage resultMsg = MapMessage.successMessage();
        boolean attended;
        UserIntegral integral;
        if (user.isStudent()) {
            StudentDetail studentDetail = currentStudentDetail();
            integral = studentDetail.getUserIntegral();
        } else {
            TeacherDetail teacherDetail = (TeacherDetail) user;
            integral = teacherDetail.getUserIntegral();
        }

        // 判断今天是否参加过活动了
        long todayRecordSize = rewardLoaderClient.loadUserRecordsInDay(user.getId(), new Date(), activityId);
        attended = todayRecordSize >= 10;

        // 学豆参数
        resultMsg.add("integral", integral.getUsable());

        // 判断进度详情有没有更新
        // 查找最近的一条捐赠记录，用其UpdateTime和活动的详情更新时间做比较
        RewardActivityRecord latestRecord =
                rewardLoaderClient.loadActivityUserRecords(user.getId())
                        .stream()
                        .filter(r -> Objects.equals(r.getActivityId(), activityId))
                        .max(Comparator.comparing(AbstractDatabaseEntity::getCreateDatetime))
                        .orElse(null);

        if (latestRecord != null) {
            if (activity.getDetailUpdattime() != null
                    && latestRecord.getUpdateDatetime().before(activity.getDetailUpdattime())) {
                resultMsg.add("detailUpdated", true);
                // 没有改变，就是摸一下
                rewardServiceClient.updateActivityRecord(latestRecord);
            }
        }

        if (user.isTeacher()) {//如果是老师需要换算成园丁豆
            activity.setOrderWeights(activity.getOrderWeights() / 10);
        }
        resultMsg.add("activityDetail", activity);
        resultMsg.add("participatedIn", attended);

        // 读取10条最近的捐赠记录
        List<RewardActivityRecord> recentRecords = rewardLoaderClient.loadRecentActivityRecords(activityId);
        // 获得捐赠的用户
        List<Long> recordUserIds = recentRecords.stream().map(r -> r.getUserId()).collect(Collectors.toList());
        Map<Long, User> recordUsersMap = userLoaderClient.loadUsers(recordUserIds);

        // 设置头像
        Date now = new Date();
        recentRecords.forEach(r -> {
            User tmpUser = recordUsersMap.get(r.getUserId());
            if (tmpUser != null) {
                r.setUserHeaderImg(tmpUser.getProfile().getImgUrl());
            }

            // 处理时间
            Date createTime = r.getCreateDatetime();
            long passMinute = DateUtils.minuteDiff(now, createTime);
            if (passMinute <= 5) {
                r.setTimeExpression("刚刚");
            } else if (passMinute < 60) {
                r.setTimeExpression(passMinute + "分钟前");
            } else {
                Date todayEnd = DateUtils.getTodayEnd();
                long passDay = DateUtils.dayDiff(todayEnd, createTime);
                if (passDay >= 1) {
                    r.setTimeExpression(DateUtils.dateToString(createTime, "M月d日 HH:mm"));
                } else {
                    r.setTimeExpression(DateUtils.dateToString(createTime, "今天 HH:mm"));
                }
            }
        });

        resultMsg.add("donationRecords", recentRecords);
        return resultMsg;
    }

    /**
     * 参加捐赠活动
     *
     * @return
     */
    @RequestMapping(value = "/reward/activity/donate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage donateActivity() {

        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }

        // 如果已经捐赠完成，则报错返回
        if (Objects.equals(activity.getStatus(), RewardActivity.Status.FINISHED.name())
                || (activity.getTargetMoney() != 0 && activity.getRaisedMoney() >= activity.getTargetMoney())) {
            return MapMessage.errorMessage("活动已经结束，不能进行捐赠！");
        }

        int donationAmount = getRequestInt("donationAmount");
        if (donationAmount <= 0) {
            return MapMessage.errorMessage("捐赠金额不能为零!");
        }

        User user = currentUser();

        if (user.isTeacher() && ((TeacherDetail)user).isPrimarySchool()) {//老师捐的是园丁豆需要换算为学豆
            donationAmount = donationAmount * 10;
        }

        RewardActivityRecord record = new RewardActivityRecord();
        record.setActivityId(activityId);
        record.setPrice((double) donationAmount);
        record.setUserId(user.getId());
        record.setUserName(user.getProfile().getRealname());
        record.setComment("捐赠学豆");

        MapMessage resultMsg = rewardServiceClient.createActivityRecord(record);
        return resultMsg;
    }

    /**
     * ajax 获取奖品列表 标签页
     */
    @RequestMapping(value = "rewardList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rewardList() {

        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long oneLevelTagId = getRequestLong("oneLevelTagId", 0L);
        Long twoLevelTagId = getRequestLong("twoLevelTagId", 0L);
        Long categoryId = getRequestLong("categoryId", 0L);
        String orderBy = getRequestParameter("orderBy", "studentOrderValue");
        String upDown = getRequestParameter("upDown", "down");
        boolean canExchangeFlag = getRequestBool("canExchangeFlag");
        int pageNum = getRequestInt("pageNum", 0);
        int pageSize = getRequestInt("pageSize", 21);
        String productType = getRequestString("productType");
        String categoryIdsStr = getRequestString("categoryIds");

        List<Long> categoryIds;
        if (StringUtils.isEmpty(categoryIdsStr) && categoryId != 0L) {
            categoryIds = Collections.singletonList(categoryId);
        } else {
            categoryIds = Arrays.stream(categoryIdsStr.split(","))
                    .filter(StringUtils::isNumeric)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

        User user = currentStudentDetail();
        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = categoryId;
        context.oneLevelTagId = oneLevelTagId;
        context.twoLevelTagId = twoLevelTagId;
        context.orderBy = orderBy;
        context.upDown = upDown;
        context.pageNumber = pageNum;
        context.pageSize = pageSize;
        context.canExchangeFlag = canExchangeFlag;
        if (oneLevelTagId == 0L && twoLevelTagId == 0L) {
            context.loadPage = "all";
        } else {
            context.loadPage = "tag";
        }
        context.terminal = RewardDisplayTerminal.Mobile;
        context.productType = productType;
        context.categoryIds = categoryIds;

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                grayFuncMngCallBack);

        return MapMessage.successMessage()
                .add("pageNum", pagination.getNumber())
                .add("pageSize", pagination.getSize())
                .add("rows", pagination.getContent())
                .add("totalPage", pagination.getTotalPages())
                .add("totalSize", pagination.getTotalElements());
    }

    //  返回奖品中心首页页面（原生控制入口）
    @RequestMapping(value = "reward.vpage")
    public String rewardftl() {
        return "redirect:/view/mobile/student/center/reward";
    }

    /**
     * 宝阁(原奖品中心)首页
     *
     * @return
     */
    @RequestMapping(value = "rewardmain.vpage")
    @ResponseBody
    public MapMessage newReward() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        // 获得趣味课堂显示条目的数量，默认是4个
        int showCourseNum = getRequestInt("showCourseNum", 4);

        MapMessage resultMsg = new MapMessage();
        StudentDetail studentDetail = currentStudentDetail();
        // 学豆参数
        UserIntegral integral = studentDetail.getUserIntegral();
        resultMsg.add("integral", integral.getUsable());

        LoadRewardProductContext context = new LoadRewardProductContext();
        // 默认用排序值排序
        context.orderBy = getRequestParameter("orderBy", "studentOrderValue");
        context.loadPage = "all";
        context.terminal = RewardDisplayTerminal.Mobile;
        context.twoLevelTagId = 0L;
        context.categoryId = 0L;
        context.pageNumber = 0;
        context.pageSize = Integer.MAX_VALUE;

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(studentDetail, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                grayFuncMngCallBack);
        List<RewardProductDetail> allProducts = pagination.getContent();

        // 创意pk，筛选出头饰类产品
        SingletonMap<Long, List<RewardProductDetail>> headWearProductsMap = filterRewardProduct(allProducts,
                RewardProductType.JPZX_TIYAN,
                studentDetail,
                SubCategory.HEAD_WEAR.getName(),
                6);

        if (headWearProductsMap != null) {
            List<RewardProductDetail> headWearProducts = headWearProductsMap.getValue();
            resultMsg.add("headWearProducts", headWearProducts)
                    .add("headWearCategoryId", headWearProductsMap.getKey());
        }

        final List<RewardProductDetail> courseProducts = new ArrayList<>();
        StringBuilder courseCategoryId = new StringBuilder();

        // 趣味课堂显示，微课，精品文章，兑换码三个子分类
        Arrays.stream(new SubCategory[]{
                SubCategory.MINI_COURSE,
                SubCategory.CHOICEST_ARTICLE,
                SubCategory.COUPON})
                .forEach(sc -> {

                    // 筛选相应子类别的商品
                    SingletonMap<Long, List<RewardProductDetail>> productsMap =
                            filterRewardProduct(
                                    allProducts,
                                    RewardProductType.JPZX_TIYAN,
                                    studentDetail,
                                    sc.getName(),
                                    showCourseNum);

                    if (productsMap == null)
                        return;

                    courseProducts.addAll(productsMap.getValue());
                    // 把两个类别拼在一起，用逗号分隔
                    courseCategoryId.append(",").append(SafeConverter.toString(productsMap.getKey()));

                });

        List<RewardProductDetail> orderedCourseProducts = RewardProductDetailUtils
                .orderProducts(courseProducts, context.orderBy, context.upDown)
                .stream()
                .limit(showCourseNum)
                .collect(Collectors.toList());

        resultMsg.add("miniCourseProducts", orderedCourseProducts)
                .add("miniCourseCategoryId", courseCategoryId);

        // 学习奖励，对应的是原来的实物奖品
        List<RewardProductDetail> shiwuProduts = allProducts.stream()
                .filter(p -> Objects.equals(
                        p.getProductType(),
                        RewardProductType.JPZX_SHIWU.name()))
                .limit(6)
                .collect(Collectors.toList());

        resultMsg.add("learningReward", shiwuProduts);

        int tipShowFlag = rewardServiceClient.tryShowTipFlag(currentUser());
        resultMsg.add("tipShowFlag", tipShowFlag);
        return resultMsg;
    }

    /**
     * 奖品中心 - 能量箱(订单记录)页面
     *
     * @return
     */
    @RequestMapping(value = "/rewardcenter/orders.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage orders() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = new MapMessage();
        StudentDetail studentDetail = currentStudentDetail();
        // 学豆参数
        UserIntegral integral = studentDetail.getUserIntegral();
        resultMsg.add("integral", integral.getUsable());

        List<RewardOrder> allOrders = rewardLoaderClient.getRewardOrderLoader()
                .loadUserRewardOrders(studentDetail.getId());

        Set<Long> productIds = allOrders.stream()
                .map(RewardOrder::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 再加上兑换券的商品
        productIds.addAll(
                rewardLoaderClient.getRewardCouponDetailLoader()
                        .loadUserRewardCouponDetails(studentDetail.getId())
                        .stream()
                        .map(c -> c.getProductId())
                        .collect(Collectors.toList()
                        ));

        Map<Long, RewardProductDetail> productMap = rewardLoaderClient.generateUserRewardProductDetails(studentDetail, productIds)
                .stream().collect(Collectors.toMap(pd -> pd.getId(), pd -> pd));

        Map<Long, RewardProduct> rewardProductMap = rewardLoaderClient.loadRewardProductMap();
        Map<String, UserPrivilege> userPrivilegeMap = findUserPrivilegeMap(currentUserId());
        Map<Long, TobyAccessoryCVRecord> tobyAccessoryCVRecordMap = rewardCenterClient.loadTobyAccessoryListByUserId(currentUserId());
        Map<Long, TobyCountenanceCVRecord> tobyCountenanceCVRecordMap = rewardCenterClient.loadTobyCountenanceListByUserId(currentUserId());
        Map<Long, TobyImageCVRecord> tobyImageCVRecordMap = rewardCenterClient.loadTobyImageListByUserId(currentUserId());
        Map<Long, TobyPropsCVRecord> tobyPropsCVRecordMap = rewardCenterClient.loadTobyPropsListByUserId(currentUserId());

        List<RewardCouponDetail> coupons = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(studentDetail.getId());

        Map<Long, RewardCouponDetail> orderCouponMap = coupons
                .stream()
                .filter(coupon -> Objects.nonNull(coupon.getOrderId()) && !Objects.equals(coupon.getOrderId(), 0L))
                .collect(Collectors.toMap(RewardCouponDetail::getOrderId, Function.identity(), (o1, o2) -> o2));

        // 处理历史订单记录
        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIds);
        String exchangeDatePattern = "yyyy.MM.dd";
        List<RewardOrderMapper> ordersMappers = allOrders.stream()
                .map(o -> {
                    RewardOrderMapper mapper = new RewardOrderMapper();
                    RewardProduct rewardProduct = rewardProductMap.get(o.getProductId());
                    if (rewardProduct != null) {
                        if (userPrivilegeMap != null && userPrivilegeMap.containsKey(rewardProduct.getRelateVirtualItemId())) {
                            UserPrivilege userPrivilege = userPrivilegeMap.get(rewardProduct.getRelateVirtualItemId());
                            mapper.setLeftValidTime(Math.max((int) DateUtils.dayDiff(userPrivilege.getExpiryDate(), new Date()), 0));
                            mapper.setEndDateStr(DateUtils.dateToString(userPrivilege.getExpiryDate(), exchangeDatePattern));
                        }
                        else if (tobyAccessoryCVRecordMap != null && tobyAccessoryCVRecordMap.containsKey(rewardProduct.getId())) {
                            TobyAccessoryCVRecord record = tobyAccessoryCVRecordMap.get(rewardProduct.getId());
                            mapper.setEndDateStr(DateUtils.dateToString(new Date(record.getExpiryTime()), exchangeDatePattern));
                        }
                        else if (tobyCountenanceCVRecordMap != null && tobyCountenanceCVRecordMap.containsKey(rewardProduct.getId())) {
                            TobyCountenanceCVRecord record = tobyCountenanceCVRecordMap.get(rewardProduct.getId());
                            mapper.setEndDateStr(DateUtils.dateToString(new Date(record.getExpiryTime()), exchangeDatePattern));
                        }
                        else if (tobyImageCVRecordMap != null && tobyImageCVRecordMap.containsKey(rewardProduct.getId())) {
                            TobyImageCVRecord record = tobyImageCVRecordMap.get(rewardProduct.getId());
                            mapper.setEndDateStr(DateUtils.dateToString(new Date(record.getExpiryTime()), exchangeDatePattern));
                        }
                        else if (tobyPropsCVRecordMap != null && tobyPropsCVRecordMap.containsKey(rewardProduct.getId())) {
                            TobyPropsCVRecord record = tobyPropsCVRecordMap.get(rewardProduct.getId());
                            mapper.setEndDateStr(DateUtils.dateToString(new Date(record.getExpiryTime()), exchangeDatePattern));
                        }
                    }
                    mapper.setId(o.getId());
                    mapper.setProductId(o.getProductId());
                    mapper.setProductName(o.getProductName());
                    mapper.setProductCategory(o.getProductCategory());
                    mapper.setPrice(o.getTotalPrice());
                    mapper.setDiscount(o.getDiscount());
                    mapper.setStatus(RewardOrderStatus.parse(o.getStatus()).getDesc());
                    mapper.setExchangeDateStr(DateUtils.dateToString(o.getCreateDatetime(), exchangeDatePattern));
                    mapper.setCreateDatetime(o.getCreateDatetime());
                    mapper.setProductType(o.getProductType());
                    mapper.setSpendType(o.getSpendType());
                    mapper.setSkuName(o.getSkuName());
                    if (o.getSource() != null) {
                        mapper.setSource(o.getSource().name());
                    }
                    mapper.setOneLevelCategoryType(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o));

                    //如果是优惠券类型给一个类型
                    if (orderCouponMap.containsKey(o.getId())) {
                        RewardCouponDetail coupon = orderCouponMap.get(o.getId());
                        mapper.setCouponNo(coupon.getCouponNo());
                        mapper.setConvertible(true);
                    }

                    // 添加产品类型字段
                    RewardProductDetail productDetail = productMap.get(mapper.getProductId());
                    if (productDetail != null) {
                        mapper.setProductType(productDetail.getProductType());
                        mapper.setConvertible(true);
                        mapper.setCouponResource(productDetail.getCouponResource());

                        //实物下架或者库存为0不可看
                        if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_SHIWU.intType())) {
                            if (!productDetail.getOnline() || productDetail.isZeroStock()) {
                                mapper.setConvertible(false);
                            }
                            //如果是教学资源和微课永久可看
                        } else if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_MINI_COURSE.intType())
                                || Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType())) {
                            mapper.setConvertible(true);
                        } else {
                            if (!productDetail.getOnline()) {
                                mapper.setConvertible(false);
                            }
                        }
                        if (productDetail.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(o.getExtAttributes())) {
                            mapper.setCouponUrl("/usermobile/giftMall/usercoupon/redirect.vpage?orderId=" + o.getId());
                        }
                    } else {
                        // 尽管商品已经找不到了，看之前下单存下来的类型字段
                        if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_MINI_COURSE.intType())
                                || Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType())) {
                            mapper.setConvertible(true);
                        }
                    }

                    // 实物、兑换成功状态，并且是非抽奖来源的才可以取消兑换
                    if (Objects.equals(newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(o), OneLevelCategoryType.JPZX_SHIWU.intType())
                            && Objects.equals(o.getStatus(), RewardOrderStatus.SUBMIT.name())
                            && !Objects.equals(o.getSource(), RewardOrder.Source.gift)
                            && !Objects.equals(o.getSource(), RewardOrder.Source.power_pillar)
                            && !Objects.equals(o.getSource(), RewardOrder.Source.claw)) {
                        mapper.setReturnable(true);
                    } else {
                        mapper.setReturnable(false);
                    }

                    List<RewardImage> images = rewardImages.get(o.getProductId());
                    if (CollectionUtils.isNotEmpty(images)) {
                        RewardImage selectImage = rewardLoaderClient.pickDisplayImage(studentDetail, images);
                        if (selectImage != null) {
                            mapper.setProductImg(selectImage.getLocation());
                        } else
                            mapper.setProductImg(images.get(0).getLocation());
                    }

                    return mapper;
                })
                .collect(Collectors.toList());

        // 汇总兑换券的数据
        List<RewardOrderMapper> couponMappers = coupons
                .stream()
                .filter(c -> Objects.isNull(c.getOrderId()) || Objects.equals(c.getOrderId(), 0L))
                .map(c -> {
                    RewardOrderMapper mapper = new RewardOrderMapper();
                    mapper.setCreateDatetime(c.getCreateDatetime());
                    mapper.setProductId(c.getProductId());
                    mapper.setId(c.getId());
                    mapper.setProductType(SubCategory.COUPON.name());
                    mapper.setCouponNo(c.getCouponNo());
                    mapper.setConvertible(true);

                    RewardProductDetail productDetail = productMap.get(mapper.getProductId());
                    if (productDetail != null) {
                        if (!productDetail.getOnline()) {
                            mapper.setConvertible(false);
                        }
                        List<RewardImage> images = rewardImages.get(c.getProductId());
                        if (CollectionUtils.isNotEmpty(images)) {
                            RewardImage selectImage = rewardLoaderClient.pickDisplayImage(studentDetail, images);
                            if (selectImage != null) {
                                mapper.setProductImg(selectImage.getLocation());
                            } else
                                mapper.setProductImg(images.get(0).getLocation());
                        }
                        mapper.setCouponResource(productDetail.getCouponResource());
                        // 价格
                        mapper.setPrice(productDetail.getDiscountPrice());
                        mapper.setProductName(productDetail.getProductName());
                        mapper.setOneLevelCategoryType(newRewardLoaderClient.getOneLevelCategoryType(productDetail.getOneLevelCategoryId()));
                    } else {
                        mapper.setConvertible(false);
                    }

                    // 转换日期
                    mapper.setExchangeDateStr(DateUtils.dateToString(c.getCreateDatetime(), exchangeDatePattern));
                    return mapper;
                })
                .collect(Collectors.toList());

        ordersMappers.addAll(couponMappers);
        // 按创建时间倒序，越晚越靠前
        ordersMappers.sort((o1, o2) -> o1.getCreateDatetime().compareTo(o2.getCreateDatetime()) * -1);

        resultMsg.add("allOrders", ordersMappers);

        //return "studentmobilev3/prizecenter/energybox";
        return resultMsg;
    }

    private Map<String, UserPrivilege> findUserPrivilegeMap(long userId) {
        List<UserPrivilege> privileges = privilegeLoaderClient.findUserPrivileges(userId, PrivilegeType.Head_Wear);
        if (privileges == null || privileges.isEmpty()) {
            return null;
        }
        return privileges.stream()
                .collect(Collectors.toMap(UserPrivilege::getPrivilegeId, Function.identity(), (v1, v2) -> v2));

    }

    /**
     * 奖品中心 - 愿望池
     *
     * @return
     */
    @RequestMapping(value = "/rewardcenter/mywish.vpage")
    @ResponseBody
    public MapMessage myWish() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        List<Map<String, Object>> wishList = rewardLoaderClient.getWishDetails(currentUser());
        for (Map<String, Object> wishDetail : wishList) {
            StudentDetail studentDetail = currentStudentDetail();
            UserIntegral userIntegral = studentDetail.getUserIntegral();

            // 计算差多少钱，可以买
            double diff = Math.max(SafeConverter.toDouble(wishDetail.get("price")) - userIntegral.getUsable(), 0);
            wishDetail.put("diff", diff);

            if (diff <= 0)
                wishDetail.put("canBuy", true);
        }

        return MapMessage.successMessage().add("wishList", wishList);
    }

    /**
     * 过滤奖品列表
     *
     * @param originProducts 原产品列表
     * @param productType
     * @param studentDetail
     * @param categoryName
     * @param limit
     * @return
     */
    public SingletonMap filterRewardProduct(List<RewardProductDetail> originProducts,
                                            RewardProductType productType,
                                            StudentDetail studentDetail,
                                            String categoryName,
                                            int limit) {

        RewardCategory filterCategory = rewardLoaderClient.loadRewardCategories(productType, UserType.STUDENT)
                .stream()
                .filter(e -> (studentDetail.isPrimaryStudent() && Boolean.TRUE.equals(e.getPrimaryVisible())) || (studentDetail.isJuniorStudent() && Boolean.TRUE.equals(e.getJuniorVisible())))
                .filter(c -> Objects.equals(c.getCategoryName(), categoryName))
                .findAny()
                .orElse(null);

        if (filterCategory == null)
            return null;

        Map<Long, RewardProductCategoryRef> filterRefsMap = rewardLoaderClient.findRewardProductCategoryRefsByCategoryId(filterCategory.getId())
                .stream()
                .collect(Collectors.toMap(k -> k.getProductId(), v -> v));

        if (filterRefsMap != null) {
            List result = originProducts.stream()
                    .filter(p -> filterRefsMap.containsKey(p.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());

            return new SingletonMap<>(filterCategory.getId(), result);
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initRewardDetailProcessor();
    }

    /**
     * 直接打通上下文，单独提出来一个类还要再写注入，懒~
     */
    private void initRewardDetailProcessor() {
        // 头饰的详情页处理
        rewardDetailManager.register(String.valueOf(OneLevelCategoryType.JPZX_HEADWEAR.intType()), (resultMsg, product, studentDetail) -> {
            String headWearId = product.getRelateVirtualItemId();
            if (StringUtils.isEmpty(headWearId)) {
                resultMsg.add("exchanged", false);
                return;
            }

            Date now = new Date();
            // 查看在有效期内，是否拥有该头饰
            boolean owned = privilegeLoaderClient.findUserPrivileges(studentDetail.getId(), PrivilegeType.Head_Wear)
                    .stream()
                    .anyMatch(p -> Objects.equals(headWearId, p.getPrivilegeId())
                            && (p.getExpiryDate() == null || now.before(p.getExpiryDate())));

            // studentInfo可能不存在
            StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(studentDetail.getId());
            boolean using = studentInfo != null && Objects.equals(studentInfo.getHeadWearId(), headWearId);

            if (owned) {
                if (using)
                    resultMsg.add("inuse", true);
                else
                    resultMsg.add("exchanged", true);
            } else {
                resultMsg.add("exchanged", false);
            }

            // 这里传入头饰的图片，供预览用
            Privilege headWearP = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(headWearId);
            if (headWearP != null) {
                resultMsg.add("headWearImgUrl", headWearP.getImg());
            }
        });

        rewardDetailManager.register(String.valueOf(OneLevelCategoryType.JPZX_TOBY.intType()), (resultMsg, product, studentDetail) -> {
            String headWearId = product.getRelateVirtualItemId();
            if (StringUtils.isEmpty(headWearId)) {
                resultMsg.add("exchanged", false);
                return;
            }
            resultMsg.add("exchanged", true);
        });

        // 针对微课或者是精品文章这种，只能下一次订单的分类
        RewardDetailProcessor onceBoughtTypeProcessor = (resultMsg, product, studentDetail) -> {
            // 微课和精品文章分类的话，查询历史的订单记录
            boolean bought = rewardLoaderClient.loadUserRewardOrders(studentDetail.getId())
                    .stream()
                    .filter(o -> Objects.equals(o.getProductId(), product.getId()))
                    .anyMatch(o -> Objects.equals(
                            o.getStatus(),
                            RewardOrderStatus.DELIVER.name()));

            // 如果有历史购买记录，则显示正在学习
            if (bought) {
                resultMsg.add("exchanged", true);
                resultMsg.add("courseVideoUrl", product.getRelateVirtualItemContent());
            } else
                resultMsg.add("exchanged", false);
        };

        rewardDetailManager.register(String.valueOf(OneLevelCategoryType.JPZX_MINI_COURSE.intType()), onceBoughtTypeProcessor);

        // 兑换券的处理
        rewardDetailManager.register(String.valueOf(OneLevelCategoryType.JPZX_COUPON.intType()), ((resultMsg, product, studentDetail) -> {
            // 查询是否已经兑换过
            boolean exchanged = rewardLoaderClient.getRewardCouponDetailLoader()
                    .loadUserRewardCouponDetails(studentDetail.getId())
                    .stream()
                    .anyMatch(c -> Objects.equals(c.getProductId(), product.getId()));

            resultMsg.add("exchanged", exchanged);
        }));
    }

    /**
     * 至少有一个家长绑定了手机号
     */
    private Boolean parentIsBindMobile(Long studentId) {
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isEmpty(studentParents)) {
            return false;
        } else {
            String bindMobile = studentParents.stream().map(i -> {
                String mobile = sensitiveUserDataServiceClient.loadUserMobile(i.getParentUser().getId());
                return mobile;
            }).filter(StringUtils::isNotBlank).findFirst().orElse(null);
            return StringUtils.isNotBlank(bindMobile);
        }
    }

    @Named
    public static class RewardDetailManager {

        private Map<String, RewardDetailProcessor> processors = new HashMap<>();

        public void register(String name, RewardDetailProcessor processor) {
            processors.put(name, processor);
        }

        public RewardDetailProcessor get(String name) {
            return processors.get(name);
        }
    }

    /***
     * 奖品详情页，不同子分类商品的数据处理
     */
    public interface RewardDetailProcessor {
        void process(MapMessage resultMsg, RewardProductDetail product, StudentDetail studentDetail);
    }

    /**
     * 小学奖品中心详情页接口
     *
     * @return
     */
    @RequestMapping("/rewardcenter/detail.vpage")
    @ResponseBody
    public MapMessage newRewardDetail() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        Long productId = getRequestLong("productId");
        StudentDetail studentDetail = currentStudentDetail();

        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();
        RewardProduct product = productMap.get(productId);

        RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(currentUser(), product.getId());
        if (detail == null) {
            return MapMessage.errorMessage(ApiConstants.RES_RESULT_PRODUCT_NOT_EXIST)
                    .setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();

        resultMsg.add("result", MapMessage.successMessage());
        resultMsg.add("detail", detail);
        resultMsg.add("categories", detail.getOneLevelCategoryType());

        // 学豆参数
        UserIntegral integral = studentDetail.getUserIntegral();
        resultMsg.add("isGraduat", rewardServiceClient.isGraduateStopConvert(currentUser()));
        if (integral == null) {
            resultMsg.add("usableIntegral", 0);
        } else
            resultMsg.add("usableIntegral", integral.getUsable());

        // 查看商品是否在心愿池里面
        List<Map<String, Object>> wishList = rewardLoaderClient.getWishDetails(currentUser());
        Map<String, Object> wishOrder = wishList.stream()
                .filter(o -> Objects.equals(productId, SafeConverter.toLong(o.get("productId"))))
                .findFirst()
                .orElse(null);

        if (wishOrder != null) {
            resultMsg.add("inWishList", true);
            resultMsg.add("wishOrderId", SafeConverter.toLong(wishOrder.get("wishOrderId")));
        }

        // 实物类的不能在移动端兑换，直接返回
        // 现在可以兑换了
        if (newRewardLoaderClient.isSHIWU(detail.getOneLevelCategoryId())) {
            // 以前随便取一个sku是为了校验商品数据，现在app端能兑换实物了。需要查询到全部
            List<RewardSku> skus = rewardLoaderClient.loadProductSku(productId);
            if (CollectionUtils.isEmpty(skus)) {
                return MapMessage.errorMessage(ApiConstants.RES_RESULT_PRODUCT_NOT_EXIST)
                        .setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            } else {
                skus.forEach(detail::addSku);
            }

            resultMsg.add("showInventory", true);//如果是实物显示库存
            int inventory = CollectionUtils.isNotEmpty(skus) ? skus.stream().mapToInt(RewardSku::getInventorySellable).sum() : 0;
            resultMsg.add("inventory", inventory);

            // 下线城市的灰度地区
            boolean isOffline = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "Reward", "OfflineShiWu", true);
            resultMsg.add("rewardOffline", isOffline);

            // 灰度检查
            if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(
                    studentDetail, "Reward", "Shiwu")) {
                resultMsg.add("open", true);
            } else
                resultMsg.add("open", false);

            // 添加月份参数
            Calendar cl = DateUtils.toCalendar(new Date());
            int month = cl.get(Calendar.MONTH);
            int realMonth = month + 1;

            if (realMonth > 12)
                realMonth = 1;

            resultMsg.add("month", realMonth);

            return resultMsg;
        }

        // 根据商品所属的不同类别，进行处理
        RewardDetailProcessor rewardDetailProcessor = rewardDetailManager.get(String.valueOf(newRewardLoaderClient.getOneLevelCategoryType(detail.getOneLevelCategoryId())));
        if (Objects.nonNull(rewardDetailProcessor)) {
            rewardDetailProcessor.process(resultMsg, detail, studentDetail);
        }
        return resultMsg;
    }

    /**
     * 单个奖品详情页，中学
     */
    @RequestMapping(value = "rewarddetail.vpage", method = RequestMethod.GET)
    public String rewardDetail(Model model) {
        if (studentUnLogin()) {
            model.addAttribute("result", MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return "studentmobile/logininvalid";
        }

        String pagePath = "mobile/student/junior/prize/detail";

        Long productId = getRequestLong("productId");
        StudentDetail studentDetail = currentStudentDetail();

        RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(studentDetail, productId);
        if (detail == null || !detail.getOnline()) {
            model.addAttribute("result", MapMessage.errorMessage(ApiConstants.RES_RESULT_PRODUCT_NOT_EXIST)
                    .setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return pagePath;
        }

        RewardSku sku = rewardLoaderClient.loadProductSku(productId).stream().findAny().orElse(null);
        if (sku == null) {
            model.addAttribute("result", MapMessage.errorMessage(ApiConstants.RES_RESULT_PRODUCT_NOT_EXIST)
                    .setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return pagePath;
        } else {
            detail.addSku(sku);
        }

        model.addAttribute("result", MapMessage.successMessage());
        model.addAttribute("detail", detail);

        return pagePath;
    }

    /**
     * 查看兑换优惠券的详情页
     *
     * @return
     */
    @RequestMapping(value = "coupondetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage couponDetail() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        Long productId = getRequestLong("productId");

        StudentDetail studentDetail = currentStudentDetail();
        RewardCouponDetail couponDetail = rewardLoaderClient.loadRewardCouponDetail(studentDetail.getId(), productId);

        resultMsg.add("couponDetail", couponDetail);

        //return "studentmobilev3/center/coupondetail";
        return resultMsg;
    }

    @RequestMapping(value = "integralchip.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage integralChip() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Integer pageNumber = getRequestInt("pageNumber", 1);
        // 获取银币前三个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient.loadUserIntegralHistories(
                currentUser(), 3, pageNumber - 1, 10);

        StudentDetail studentDetail = currentStudentDetail();
        // 学豆参数
        UserIntegral integral = studentDetail.getUserIntegral();
        Long usableIntegral = integral == null ? 0 : integral.getUsable();

        return MapMessage.successMessage()
                .add("pagination", pagination)
                .add("integral", pagination.getUsableIntegral())
                .add("currentPage", pageNumber)
                .add("usableIntegral", usableIntegral);
    }

    /**
     * 重定向到node的一个静态页了
     * 奖励排行榜详情页
     */
    @RequestMapping(value = "rewardrankdetail.vpage", method = RequestMethod.GET)
    public String rewardRankDetail(Model model) {
        if (studentUnLogin()) {
            return "studentmobilev3/logininvalid";
        }
        return "studentmobilev3/center/beanreward";
    }

    /**
     * 星星排行榜详情页
     */
    @RequestMapping(value = "starrankdetail.vpage", method = RequestMethod.GET)
    public String starRankDetail(Model model) {
        // TODO 星星奖励改名为  奖励榜 故为了老版本, 将其重定向到  奖励榜
        return "redirect:/studentMobile/center/rewardrankdetail.vpage?app_version=" + getRequestString("app_version");
    }

    /**
     * 立即查看
     */
    @RequestMapping(value = "homeworkreport.vpage", method = RequestMethod.GET)
    public String homeworkReport() {
        return "studentmobile/center/homeworkreport";
    }

    /**
     * 登录信息失效处理
     */
    @RequestMapping(value = "logininvalid.vpage", method = RequestMethod.GET)
    public String logininvalid() {
        return "studentmobile/logininvalid";
    }

    /**
     * 本学期老师奖励
     */
    @RequestMapping(value = "teacherreward.vpage", method = RequestMethod.GET)
    public String teacherReward(Model model) {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }
        StudentDetail studentDetail = currentStudentDetail();
        List<TeacherRewardMapper> mappers = new ArrayList<>();
        int integralCount = 0;

        SchoolYear schoolYear = SchoolYear.newInstance();

        List<SmartClazzIntegralHistory> all = smartClazzServiceClient.getSmartClazzService()
                .findSmartClazzIntegralHistoryListByUserId(studentDetail.getId())
                .getUninterruptibly()
                .stream()
                .filter(SmartClazzIntegralHistory::isDisplayTrue)
                .collect(Collectors.toList());
        List<SmartClazzIntegralHistory> histories = all.stream()
                .filter(e -> e.getSubject() == Subject.ENGLISH)
                .collect(Collectors.toList());
        List<SmartClazzIntegralHistory> mathHistories = all.stream()
                .filter(e -> e.getSubject() == Subject.MATH)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(histories)) {
            histories.addAll(mathHistories);
        } else {
            histories = mathHistories;
        }
        for (SmartClazzIntegralHistory history : histories) {
            if (history.getCreateDatetime().before(schoolYear.currentTermDateRange().getStartDate())) {
                continue;
            }
            TeacherRewardMapper mapper = new TeacherRewardMapper();
            mapper.setDate(DateUtils.dateToString(history.getCreateDatetime(), DateUtils.FORMAT_SQL_DATE));
            mapper.setRewardContent(history.getComment());
            mapper.setType("integral");
            mappers.add(mapper);
            integralCount += history.getIntegral();
        }
        Collections.sort(mappers, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        model.addAttribute("rewardlist", mappers);
        model.addAttribute("integralCount", integralCount);
        return "studentmobile/center/teacherreward";
    }

    /**
     * 作业检查后，给学生push消息，消息页面展示
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "integralReward.vpage", method = RequestMethod.GET)
    public String integralReward(Model model) {

        String redirectUrl = "studentmobile/center/integralReward";

        String homeworkId = getRequestString(REQ_INTEGRAL_REWARD_HOMEWORK_ID);
        String homeworkType = getRequestString(REQ_INTEGRAL_REWARD_HOMEWORK_TYPE);
        String ver = getRequestString("app_version");

        try {

            if (HomeworkType.of(homeworkType).equals(HomeworkType.UNKNOWN)) {
                model.addAttribute("result", MapMessage.errorMessage("作业类型错误").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }
            if (StringUtils.isBlank(homeworkId)) {
                model.addAttribute("result", MapMessage.errorMessage("作业Id错误").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return redirectUrl;
            }

            User student = currentStudent();
            if (student == null) {
                model.addAttribute("result", MapMessage.errorMessage("请登录学生号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
                return redirectUrl;
            }

            Integer integralReward = 0;
            Map<String, Integer> rewardCountInParentApp = newHomeworkPartLoaderClient.getRewardCountInParentApp(student.getId());
            if (MapUtils.isNotEmpty(rewardCountInParentApp)) {
                integralReward = SafeConverter.toInt(rewardCountInParentApp.get(homeworkId));
            }
            if (integralReward != null) {
                model.addAttribute("integralPrize", integralReward);
            }

            boolean verNew = false;
            if (VersionUtil.compareVersion(ver, "1.9.9.0") >= 0) {
                verNew = true;
            }
            model.addAttribute("verNew", verNew);
        } catch (Exception e) {

        }
        return redirectUrl;
    }

    //中学的学豆历史
    @RequestMapping(value = "juniorintegral.vpage", method = RequestMethod.GET)
    public String juniorIntegralHistory() {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }

        return "mobile/student/junior/prize/integral_list";

    }


    //中学奖品中心兑换记录
    @RequestMapping(value = "juniorexchangehistory.vpage", method = RequestMethod.GET)
    public String juniorExchangeHistory() {
        if (studentUnLogin()) {
            return "studentmobile/logininvalid";
        }

        return "mobile/student/junior/prize/exchange";
    }

    /**
     * 新版UGC 跳转到答题页面
     *
     * @return
     */
    @RequestMapping(value = "ugcrecord.vpage", method = RequestMethod.GET)
    public String ugcRecord(Model model) {
        User user = currentUser();
        if (studentUnLogin()) {
            // 跳转到登陆页面
            return "redirect:/student/index.vpage";
        }

        Long recordId = getRequestLong("recordId");

        if (recordId == 0) {
            model.addAttribute("result",
                    MapMessage.errorMessage(ApiConstants.RES_RESULT_CAMPAIGN_NOT_EXIST_MSG)
                            .setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mobile/student/ugc/record";
        }

        UgcRecordMapper mapper = miscLoaderClient.loadEnableUserUgcRecordByRecordId(user, recordId);
        if (mapper != null) {
            model.addAttribute("record", mapper);
        }
        model.addAttribute("result", MapMessage.successMessage());
        return "mobile/student/ugc/record";
    }

    /**
     * 新版UGC 上传答题结果
     *
     * @return
     */
    @RequestMapping(value = "saveugcanswer.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUgcAnswer(@RequestBody Map body) {
        User user = currentUser();
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        Long recordId = SafeConverter.toLong(body.get("recordId"));
        //noinspection unchecked
        List<Map<String, Object>> answerMapList = (List<Map<String, Object>>) body.get("answerMapList");
        try {
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .keys(user.getId())
                    .proxy()
                    .saveUgcAnswer(user, recordId, answerMapList, UgcAnswers.Source.MOBILE);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    /**
     * 内容中是否有违禁词
     *
     * @return
     */
    @RequestMapping(value = "containsbadwords.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage containsBadwords() {
        String content = getRequestString("content");
        return MapMessage.successMessage().add("contains", badWordCheckerClient.checkConversationBadWord(content));
    }

    /**
     * 模考报名消息，点击查看详情页面
     */
    @RequestMapping(value = "newexam/index.vpage", method = RequestMethod.GET)
    public String newExam(@RequestParam String id, Model model) {
        model.addAttribute("id", id);
        return "mobile/student/junior/examination";
    }

    /**
     * 查询用户当月的集赞统计
     */
    @RequestMapping(value = "/like/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage likeList() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(currentUserId());
            if (null == clazz) {
                return MapMessage.errorMessage("您还没有加入班级");
            }

            Map<String, Integer> likedCountMap = new HashMap<>();
            Integer likerCount = fillLikeData(clazz.getId(), likedCountMap);

            int total = 0;
            List<StudentLikeLogPerDayMapper> mappers = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : likedCountMap.entrySet()) {
                if (entry.getValue() > 0) {
                    StudentLikeLogPerDayMapper mapper = new StudentLikeLogPerDayMapper();
                    mapper.setCount(entry.getValue());
                    mapper.setDate(entry.getKey());
                    mappers.add(mapper);

                    total += entry.getValue();
                }
            }

            mappers = mappers.stream().sorted((m1, m2) -> m2.getDate().compareTo(m1.getDate())).collect(Collectors.toList());

            return MapMessage.successMessage().add("records", mappers).add("total", total).add("likerCount", likerCount);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 查询用户的特权
     */
    @RequestMapping(value = "/privilege.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage privilege() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        try {
            List<UserPrivilege> privileges;

            String type = getRequestParameter("type", null);
            if (StringUtils.isBlank(type)) {
                privileges = privilegeLoaderClient.getPrivilegeLoader()
                        .findUserPrivileges(currentUserId())
                        .getUninterruptibly();
            } else {
                PrivilegeType t = PrivilegeType.of(type);
                if (null == t) {
                    return MapMessage.errorMessage("未知的特权类型");
                }

                privileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), t);
            }

            List<StudentPrivilegeMapper> mappers = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(privileges)) {
                StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
                // 已经拥有的头饰
                // 这里提交后 diff 结果看起来差异看大, 只是利用 idea 重构抽取的方法
                alreadyOwned(privileges, mappers, studentInfo);
            }

            List<StudentPrivilegeMapper> notOwnedPrivileges = new ArrayList<>();
            //是查询所有头饰，把未获得的也返回
            for (Privilege p : privilegeBufferServiceClient.getPrivilegeBuffer().dump().getData()) {
                if (mappers.stream().filter(m -> m.getPrivilegeId().equals(p.getId())).count() > 0) {
                    continue;
                }
                if (StringUtils.isNotBlank(type) && !p.getType().name().equals(type)) {
                    continue;
                }
                /*if (Privilege.速算王国.getId().equals(p.getId())) {
                    continue;   //速算王国头饰不出现在未获得列表
                }*/
                if (!p.getDisplayInCenter()) {
                    continue;
                }

                StudentPrivilegeMapper mapper = new StudentPrivilegeMapper();
                mapper.setCurrent(false);
                mapper.setImgUrl(p.getImg());
                mapper.setName(p.getName());
                mapper.setPrivilegeId(p.getId());
                mapper.setType(p.getType().name());
                mapper.setOrigin(p.getOrigin().name());
                mapper.setAcquireCondition(p.getAcquireCondition());

                // 关联上奖品id
                RewardProduct product = rewardLoaderClient.loadRewardProductByVirtualItemId(p.getId());
                if (product != null)
                    mapper.setRelateProductId(product.getId());

                notOwnedPrivileges.add(mapper);
            }

            return MapMessage.successMessage().add("privileges", mappers).add("notOwned", notOwnedPrivileges);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private static final List<Long> excludeTagId = Lists.newArrayList(
            RewardTagEnum.可爱.getId(),
            RewardTagEnum.文艺.getId(),
            RewardTagEnum.酷炫.getId(),
            RewardTagEnum.公益.getId()
    );

    @RequestMapping(value = "/other_head_wear.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage otherHeadWear() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        boolean have = getRequestBool("have", false);

        try {
            StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());

            Set<String> excludeVirtualItemId = getExcludeHeadWear(user);
            PrivilegeType privilegeType = PrivilegeType.Head_Wear;
            List<UserPrivilege> havePrivileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), privilegeType);
            Map<String, UserPrivilege> havePrivilegeMap = havePrivileges.stream().collect(Collectors.toMap(UserPrivilege::getPrivilegeId, Function.identity(), (o1, o2) -> o2));

            // 特权ID和商品的映射
            Map<String, RewardProduct> virtualIdProductMap = rewardLoaderClient.loadRewardProductMap()
                    .values().stream()
                    .collect(Collectors.toMap(RewardProduct::getRelateVirtualItemId, Function.identity(), (o1, o2) -> o2));

            // 所有特权
            List<Privilege> allPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().findAll();

            Date now = new Date();

            List<StudentPrivilegeMapper> headWard = allPrivilege.stream()
                    .filter(i -> Objects.equals(i.getType(), privilegeType))
                    .filter(i -> !excludeVirtualItemId.contains(i.getId()))
                    .map(privilegeItem -> {
                        UserPrivilege userPrivilege = havePrivilegeMap.get(privilegeItem.getId());
                        String id = privilegeItem.getId();
                        StudentPrivilegeMapper mapper = new StudentPrivilegeMapper();
                        mapper.setCurrent(studentInfo != null && Objects.equals(studentInfo.getHeadWearId(), id));
                        mapper.setImgUrl(privilegeItem.getImg());
                        mapper.setName(privilegeItem.getName());
                        mapper.setPrivilegeId(id);
                        mapper.setCode(privilegeItem.getCode());
                        mapper.setType(privilegeItem.getType().name());
                        mapper.setOrigin(privilegeItem.getOrigin().name());
                        mapper.setAcquireCondition(privilegeItem.getAcquireCondition());

                        mapper.setLeftValidTime(0);
                        mapper.setEffective(false);

                        if (userPrivilege != null) {
                            if (userPrivilege.getExpiryDate() == null) {
                                mapper.setLeftValidTime(-1);
                                mapper.setEffective(true);
                            } else if (userPrivilege.getExpiryDate().before(now)) {
                                mapper.setLeftValidTime(0);
                                mapper.setEffective(false);
                            } else {
                                mapper.setLeftValidTime(Math.max((int) DateUtils.dayDiff(userPrivilege.getExpiryDate(), now), 0));
                                mapper.setEffective(true);
                            }
                        }
                        mapper.setHave(mapper.getEffective());

                        RewardProduct product = virtualIdProductMap.get(id);
                        if (product != null) {
                            mapper.setRelateProductId(product.getId());
                        }
                        return mapper;
                    })
                    .filter(i -> have ? i.getEffective() : true)
                    .collect(Collectors.toList());

            Iterator<StudentPrivilegeMapper> iterator = headWard.iterator();
            while (iterator.hasNext()) {
                StudentPrivilegeMapper next = iterator.next();
                // 处理无效(没拥有)的头饰(已拥有的除外)
                if (next.getEffective()) continue;

                //剔除规则 1.没有关联商品不能出售 2.已下线\学生不可见 3.不是可兑换的
                if (!Objects.equals(next.getOrigin(), "EXCHANGE")) {
                    iterator.remove();
                } else {
                    RewardProduct product = virtualIdProductMap.get(next.getPrivilegeId());
                    if (product == null) {
                        iterator.remove();
                    } else if ((!product.getOnlined()) || (!product.getStudentVisible())) {
                        log.debug("productId:{},virtualId:{} removed", product.getId(), product.getRelateVirtualItemId());
                        iterator.remove();
                    }
                }
            }

            // 填充 sku 商品价格列表
            Set<Long> relateProductId = headWard.stream().map(StudentPrivilegeMapper::getRelateProductId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, List<RewardSku>> skuMap = rewardLoaderClient.loadProductRewardSkus(relateProductId);

            for (StudentPrivilegeMapper item : headWard) {
                List<RewardSku> skus = skuMap.get(item.getRelateProductId());
                if (skus == null) continue;
                item.setSkus(skus);

                RewardProduct rewardProduct = virtualIdProductMap.get(item.getPrivilegeId());
                if (rewardProduct == null) continue;

                // 计算价格列表
                BigDecimal price = new BigDecimal(rewardProduct.getPriceOldS()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
                int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

                StudentPrivilegeMapper.PriceDay priceDay1 = new StudentPrivilegeMapper.PriceDay();
                priceDay1.setDay(rewardProduct.getExpiryDate());
                priceDay1.setPrice(rewardProduct.getPriceOldS());
                priceDay1.setQuantity(1);

                StudentPrivilegeMapper.PriceDay priceDay2 = new StudentPrivilegeMapper.PriceDay();
                priceDay2.setDay(rewardProduct.getExpiryDate() * 2);
                priceDay2.setPrice(SafeConverter.toDouble(totalPrice));
                priceDay2.setQuantity(2);
                item.setPriceDay(Arrays.asList(priceDay1, priceDay2));
            }
            return MapMessage.successMessage().add("headWard", headWard);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    private Set<String> getExcludeHeadWear(StudentDetail user) {
        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = 23L; // 头饰装扮
        context.orderBy = "studentOrderValue";
        context.upDown = "down";
        context.pageNumber = 0;
        context.pageSize = Integer.MAX_VALUE;
        context.canExchangeFlag = false;
        context.loadPage = "tag";
        context.tags = excludeTagId;
        context.terminal = RewardDisplayTerminal.Mobile;
        context.productType = RewardProductType.JPZX_TIYAN.name(); // 头饰都是体验
        context.categoryIds = new ArrayList<>();

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService().loadUserSchool(u).getUninterruptibly(),
                grayFuncMngCallBack);
        List<RewardProductDetail> rewardProductDetails = pagination.getContent();
        return rewardProductDetails.stream().map(RewardProductDetail::getRelateVirtualItemId).collect(Collectors.toSet());
    }

    /**
     * 返回已拥有的头饰
     *
     * @param privileges  所有特权集合
     * @param mappers     存放返回的结果集(已拥有的头饰)
     * @param studentInfo 用户当前装扮
     */
    private void alreadyOwned(List<UserPrivilege> privileges, List<StudentPrivilegeMapper> mappers, StudentInfo studentInfo) {
        privileges.stream()
                .filter(p -> PrivilegeType.Head_Wear.name().equals(p.getType()))
                .sorted((p1, p2) -> {
                    // 按失效时间倒序排列
                    if (p1.getExpiryDate() == null && p2.getExpiryDate() != null)
                        return -1;
                    else if (p1.getExpiryDate() != null && p2.getExpiryDate() == null)
                        return 1;
                    else if (p1.getExpiryDate() == null && p2.getExpiryDate() == null)
                        return 0;
                    else if (p1.getExpiryDate().before(p2.getExpiryDate()))
                        return -1;
                    else
                        return 0;
                })
                .forEach(p -> {
                    StudentPrivilegeMapper mapper = new StudentPrivilegeMapper();
                    mapper.setPrivilegeId(p.getPrivilegeId());
                    mapper.setType(p.getType());

                    // 关联上奖品id
                    RewardProduct product = rewardLoaderClient.loadRewardProductByVirtualItemId(
                            p.getPrivilegeId());
                    if (product != null)
                        mapper.setRelateProductId(product.getId());

                    Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(p.getPrivilegeId());

                    if (null != privilege) {
                        mapper.setName(privilege.getName());
                        mapper.setImgUrl(privilege.getImg());
                        mapper.setOrigin(privilege.getOrigin().name());
                        mapper.setCode(privilege.getCode());
                    } else
                        return;

                    // 计算有效期，剩余天数
                    if (p.getExpiryDate() == null) {
                        // 永久使用
                        mapper.setLeftValidTime(-1);
                        mapper.setEffective(true);
                    } else {
                        Date now = new Date();
                        if (p.getExpiryDate().before(now)) {
                            // 如果是奖励来源的头饰，失效后直接移除
                            // 自学来源也属于奖励类型头饰，和奖励来源处理一致
                            if (privilege.getOrigin() == PrivilegeOrigin.REWARD || privilege.getOrigin() == PrivilegeOrigin.SELF_STUDY) {
                                // 公益活动和兑换的处理一样，暂时不移除
                                if (!Objects.equals(privilege.getCode(), Privilege.SpecialPrivileges.公益活动奖励.getCode())) {
                                    return;
                                }
                            }
                            // 失效超过30天的，过滤掉
                            if (DateUtils.dayDiff(now, p.getExpiryDate()) > 30) {
                                return;
                            }

                            mapper.setLeftValidTime(0);
                            mapper.setEffective(false);
                        } else {
                            mapper.setLeftValidTime(Math.max((int) DateUtils.dayDiff(p.getExpiryDate(), now), 0));
                            mapper.setEffective(true);
                        }
                    }

                    if (null != studentInfo
                            && StringUtils.isNotEmpty(studentInfo.getHeadWearId())
                            && studentInfo.getHeadWearId().equals(p.getPrivilegeId())
                            && mapper.getEffective()) {
                        mapper.setCurrent(true);
                    } else {
                        mapper.setCurrent(false);
                    }

                    mappers.add(mapper);
                });
    }

    /**
     * 更新头饰
     */
    @RequestMapping(value = "/headwear/change.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chagePrivilege() {
        if (studentUnLogin()) {
            return MapMessage.errorMessage("请重新登录");
        }

        String headWearId = getRequestString("headWearId");
        if (StringUtils.isEmpty(headWearId))
            return MapMessage.errorMessage("非法参数!");

        try {
            if ("default".equals(headWearId)) {
                return personalZoneServiceClient.getPersonalZoneService().resetHeadWear(currentUserId());
            }

            Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(headWearId);
            if (null == privilege) {
                return MapMessage.errorMessage("未知的特权");
            }

            boolean exist = false;
            List<UserPrivilege> userPrivileges = privilegeLoaderClient.getPrivilegeLoader()
                    .findUserPrivileges(currentUserId())
                    .getUninterruptibly();
            for (UserPrivilege up : userPrivileges) {
                if (PrivilegeType.Head_Wear.name().equals(up.getType()) && headWearId.equals(up.getPrivilegeId())) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                return MapMessage.errorMessage("您还没有获得要更换的头饰");
            }

            return personalZoneServiceClient.getPersonalZoneService().changeHeadWear(currentUserId(), headWearId);
        } catch (Exception ex) {
            logger.error("Change privilege failed,uid:{},hwid:{},msg:{}", currentUserId(), headWearId, ex.getMessage(), ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/course.vpage", method = RequestMethod.GET)
    public MapMessage course() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        Long oneLevelTagId = getRequestLong("oneLevelTagId", 0L);
        Long twoLevelTagId = getRequestLong("twoLevelTagId", 0L);
        int pageNumber = getRequestInt("pageNumber", 0);
        int pageSize = getRequestInt("pageSize", Integer.MAX_VALUE);

        // 如果没有传标签ID 取第一大类的第一个小类
        List<RewardTagNode> tagNodes = RewardTagUtils.get(RewardTagUtils.TypeEnum.course);
        if (oneLevelTagId == 0L && twoLevelTagId == 0L) {
            oneLevelTagId = tagNodes.get(0).getId();
            twoLevelTagId = tagNodes.get(0).getChildren().get(0).getId();
        }

        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = 0L;
        context.orderBy = "studentOrderValue";
        context.upDown = "down";
        context.pageNumber = pageNumber;
        context.pageSize = pageSize;
        context.canExchangeFlag = false;
        context.loadPage = "tag";
        context.oneLevelTagId = oneLevelTagId;
        context.twoLevelTagId = twoLevelTagId;
        context.terminal = RewardDisplayTerminal.Mobile;
        context.productType = "";
        context.categoryIds = new ArrayList<>();

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService().loadUserSchool(u).getUninterruptibly(),
                grayFuncMngCallBack);

        return MapMessage.successMessage().add("tag", tagNodes)
                .add("pageNum", pagination.getNumber())
                .add("pageSize", pagination.getSize())
                .add("rows", pagination.getContent())
                .add("totalPage", pagination.getTotalPages())
                .add("totalSize", pagination.getTotalElements());
    }

    @ResponseBody
    @RequestMapping(value = "/smallshop.vpage", method = RequestMethod.GET)
    public MapMessage smallShop() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        List<RewardTagNode> tagNodes = RewardTagUtils.get(RewardTagUtils.TypeEnum.smallShop);

        Long oneLevelTagId = getRequestLong("oneLevelTagId", 0L);
        Long twoLevelTagId = getRequestLong("twoLevelTagId", 0L);
        int pageNumber = getRequestInt("pageNumber", 0);
        int pageSize = getRequestInt("pageSize", Integer.MAX_VALUE);

        // 如果没有传标签ID 取第一大类的第一个小类
        if (oneLevelTagId == 0L && twoLevelTagId == 0L) {
            oneLevelTagId = tagNodes.get(0).getId();
            twoLevelTagId = tagNodes.get(0).getChildren().get(0).getId();
        }
        return getSmallShop(user, tagNodes, oneLevelTagId, twoLevelTagId, pageNumber, pageSize, detail -> true);
    }

    private MapMessage getSmallShop(StudentDetail user, List<RewardTagNode> tagNodes, Long oneLevelTagId, Long twoLevelTagId, int pageNumber, int pageSize, Predicate<RewardProductDetail> filter) {
        // 不知道什么意思,反正就这么写 heheheh
        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = 0L;
        context.orderBy = "studentOrderValue";
        context.upDown = "down";
        context.pageNumber = pageNumber;
        context.pageSize = pageSize;
        context.canExchangeFlag = false;
        context.oneLevelTagId = oneLevelTagId;
        context.twoLevelTagId = twoLevelTagId;
        if (oneLevelTagId == 0L && twoLevelTagId == 0L) {
            context.loadPage = "all";
        } else {
            context.loadPage = "tag";
        }
        context.terminal = RewardDisplayTerminal.Mobile;
        context.productType = RewardProductType.JPZX_SHIWU.name(); // 小卖部都是实物
        context.categoryIds = new ArrayList<>();

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService().loadUserSchool(u).getUninterruptibly(),
                grayFuncMngCallBack, filter);

        Long activitySize = rewardCenterClient.getDonationCount(user.getId());
        return MapMessage.successMessage().add("tag", tagNodes)
                .add("pageNum", pagination.getNumber())
                .add("pageSize", pagination.getSize())
                .add("rows", new ArrayList<>(pagination.getContent()))
                .add("totalPage", pagination.getTotalPages())
                .add("totalSize", pagination.getTotalElements())
                .add("activitySize", activitySize);
    }

    @ResponseBody
    @RequestMapping(value = "/smallshop/search.vpage", method = RequestMethod.GET)
    public MapMessage smallShopSearch() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        String name = getRequestString("name");
        int pageNumber = getRequestInt("pageNumber", 0);
        int pageSize = getRequestInt("pageSize", Integer.MAX_VALUE);

        Predicate<RewardProductDetail> filter = new Predicate<RewardProductDetail>() {
            @Override
            public boolean test(RewardProductDetail detail) {
                return detail.getProductName().contains(name);
            }
        };
        return getSmallShop(user, null, 0L, 0L, pageNumber, pageSize, filter);
    }

    @ResponseBody
    @RequestMapping(value = "/smallshop/hot.vpage", method = RequestMethod.GET)
    public MapMessage smallShopHot() {
        /*StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        int pageNumber = getRequestInt("pageNumber", 0);
        int pageSize = getRequestInt("pageSize", 10);

        MapMessage smallShop = getSmallShop(user, null, 0L, 0L, pageNumber, pageSize, detail -> true);
        List<RewardProductDetail> hotResult = (List<RewardProductDetail>) smallShop.get("rows");
        hotResult.sort(soldQuantityComparator);
        return smallShop;*/
        return MapMessage.successMessage();
    }

    @ResponseBody
    @RequestMapping(value = "/superchange.vpage", method = RequestMethod.GET)
    public MapMessage superChange() {
        StudentDetail user = currentStudentDetail();
        if (user == null) {
            return MapMessage.errorMessage("请登录");
        }
        Long oneLevelTagId = getRequestLong("oneLevelTagId", 0L);
        Long twoLevelTagId = getRequestLong("twoLevelTagId", 0L);
        int pageNumber = getRequestInt("pageNumber", 0);
        int pageSize = getRequestInt("pageSize", Integer.MAX_VALUE);

        boolean have = getRequestBool("have", false); // 是否只显示已拥有
        return getSuperChange(user, oneLevelTagId, twoLevelTagId, pageNumber, pageSize, have);
    }

    private MapMessage getSuperChange(StudentDetail user, Long oneLevelTagId, Long twoLevelTagId, int pageNumber, int pageSize, boolean have) {
        // 如果没有传标签ID 取第一大类的第一个小类
        List<RewardTagNode> tagNodes = RewardTagUtils.get(RewardTagUtils.TypeEnum.headwear);
        if (oneLevelTagId == 0L && twoLevelTagId == 0L) {
            oneLevelTagId = tagNodes.get(0).getId();
            twoLevelTagId = tagNodes.get(0).getChildren().get(0).getId();
        }

        // mappers 存放的是已拥有的头饰
        List<StudentPrivilegeMapper> mappers = new ArrayList<>();
        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
        List<UserPrivilege> userPrivileges = privilegeLoaderClient.findUserPrivileges(currentUserId(), PrivilegeType.Head_Wear);
        alreadyOwned(userPrivileges, mappers, studentInfo);
        Set<Long> relateProductIdSet = mappers.stream().filter(StudentPrivilegeMapper::getEffective).map(StudentPrivilegeMapper::getRelateProductId).collect(Collectors.toSet());

        // 当前装扮的头饰
        StudentPrivilegeMapper currentPrivilege = null;
        if (studentInfo != null) {
            currentPrivilege = mappers.stream().filter(Objects::nonNull).filter(mapper -> Objects.equals(mapper.getPrivilegeId(), studentInfo.getHeadWearId())).findFirst().orElse(null);
        }

        Predicate<RewardProductDetail> filter = detail -> true;
        if (have) {
            filter = detail -> relateProductIdSet.contains(detail.getId());// 过滤器
        }

        // 不知道什么意思,反正就这么写 heheheh
        LoadRewardProductContext context = new LoadRewardProductContext();
        context.categoryId = 23L; // 头饰装扮
        context.orderBy = "studentOrderValue";
        context.upDown = "down";
        context.pageNumber = pageNumber;
        context.pageSize = pageSize;
        context.canExchangeFlag = false;
        context.loadPage = "tag";
        context.oneLevelTagId = oneLevelTagId;
        context.twoLevelTagId = twoLevelTagId;
        context.terminal = RewardDisplayTerminal.Mobile;
        context.productType = RewardProductType.JPZX_TIYAN.name(); // 头饰都是体验
        context.categoryIds = new ArrayList<>();

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService().loadUserSchool(u).getUninterruptibly(),
                grayFuncMngCallBack, filter);

        List<Privilege> privilegeList = privilegeBufferServiceClient.getPrivilegeBuffer().dump().getData();
        Map<String, Privilege> privilegeMap = privilegeList.stream().collect(Collectors.toMap(Privilege::getId, Function.identity(), (o1, o2) -> o2));

        List<RewardProductDetail> content = new ArrayList<>(pagination.getContent());
        List<Long> productIds = content.stream().map(RewardProductDetail::getId).collect(Collectors.toList());
        Map<Long, List<RewardSku>> longListMap = rewardLoaderClient.loadProductRewardSkus(productIds);

        for (RewardProductDetail item : content) {
            boolean isHave = relateProductIdSet.contains(item.getId());
            item.setHave(isHave);
            List<RewardSku> skus = longListMap.get(item.getId());
            if (CollectionUtils.isEmpty(skus)) continue;
            item.setSkus(skus);

            // 计算价格列表
            BigDecimal price = new BigDecimal(item.getPrice()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
            int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

            RewardProductDetail.PriceDay priceDay1 = new RewardProductDetail.PriceDay();
            priceDay1.setDay(item.getExpiryDate());
            priceDay1.setPrice(item.getPrice());
            priceDay1.setQuantity(1);

            RewardProductDetail.PriceDay priceDay2 = new RewardProductDetail.PriceDay();
            priceDay2.setDay(item.getExpiryDate() * 2);
            priceDay2.setPrice(SafeConverter.toDouble(totalPrice));
            priceDay2.setQuantity(2);
            item.setPriceDay(Arrays.asList(priceDay1, priceDay2));

            // 来源
            Privilege privilege = privilegeMap.get(item.getRelateVirtualItemId());
            if (privilege != null) {
                item.setOrigin(privilege.getOrigin());
                item.setHeadWearImg(privilege.getImg());
            }
        }

        return MapMessage.successMessage().add("tag", tagNodes)
                .add("pageNum", pagination.getNumber())
                .add("pageSize", pagination.getSize())
                .add("rows", content)
                .add("totalPage", pagination.getTotalPages())
                .add("totalSize", pagination.getTotalElements())
                .add("avatar", getUserAvatarImgUrl(user.fetchImageUrl()))
                .add("currentPrivilege", currentPrivilege);
    }

    private Integer fillLikeData(Long clazzId, Map<String, Integer> likedCountMap) {
        // 当月用户集赞信息
        UserLikedSummary likedSummary = userLikeServiceClient.loadUserLikedSummary(currentUserId(), new Date());
        if (likedSummary == null) {
            return 0;
        }

        Map<String, Integer> dailyCount = likedSummary.getDailyCount();
        for (String dateKey : dailyCount.keySet()) {
            Date date = DateUtils.stringToDate(dateKey, DateUtils.FORMAT_SQL_DATE);
            likedCountMap.put(DateUtils.dateToString(date, "MM月dd日"), dailyCount.get(dateKey));
        }

        return likedSummary.getLikers().size();
    }

    private List<RewardProductDetail> sortAndLimit(List<RewardProductDetail> list, int limit) {
        list.sort(soldQuantityComparator);
        list = limitList(list, limit);
        return list;
    }

    private List<RewardProductDetail> limitList(List<RewardProductDetail> list, int limit) {
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list;
    }

}
