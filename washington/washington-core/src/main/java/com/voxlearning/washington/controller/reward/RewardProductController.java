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

package com.voxlearning.washington.controller.reward;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.TeacherCouponMapper;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.ProductFilterEntity;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardBufferLoaderClient;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductTag;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.reward.mapper.product.CategoryMapper;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Reward product controller implementation.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @author haitian.gan
 * @since Jul 22, 2014
 */
@Controller
@RequestMapping("/reward/product")
public class RewardProductController extends AbstractRewardController {

    @Inject private CouponLoaderClient couponLoaderClient;
    @Inject private NewRewardBufferLoaderClient newRewardBufferLoaderClient;
    @Inject private RewardCenterClient rewardCenterClient;

    /**
     * 列表页 一起作业专属
     */
    @RequestMapping(value = "/exclusive/index.vpage", method = RequestMethod.GET)
    public String customizedProductList(Model model) {
        User user = currentRewardUser();

        if (user.isTeacher()) {
            // 黑名单控制
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "Reward", "Close")) {
                return "redirect:/";
            }

            // 记录访问轨迹
            rewardServiceClient.recordAccessTrace(user.getId());
        }
        //显示提示语（位表示法表示不同的提示语是否需要显示）
        int tipShowFlag = rewardServiceClient.tryShowTipFlag(user);
        model.addAttribute("tipShowFlag", tipShowFlag);
        model.addAttribute("tagType", "exclusive");
        boolean showDeductNotify = false;
        if (user.isTeacher()) {
            showDeductNotify = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(currentTeacherDetail(),
                    "Reward", "ExchangeReduction", true);
        }
        model.addAttribute("showDeductNotify", showDeductNotify);
        return "reward/product/index";
    }

    @RequestMapping(value = "/new/category.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getNewHomePageDatal() {

        User user = currentRewardUser();
        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        List<CategoryMapper> mappers = newRewardLoaderClient.getCategoryMapper(user, userVisibleType, RewardDisplayTerminal.PC.name());
        return MapMessage.successMessage().add("categoryList", mappers);
    }

    @RequestMapping(value = "/new/productList.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getProductList() {

        User user = currentRewardUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录！");
        }

        Long categoryId = getRequestLong("categoryId", 0);
        Integer categoryType = getRequestInt("categoryType", 0);
        Long oneLevelFilterId = getRequestLong("oneLevelFilterId", 0);

        Boolean showAffordable = getRequestBool("showAffordable", false);
        Integer pageNum = getRequestInt("pageNum", 0);
        Integer pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "teacherOrderValue";
        }
        String upDown = getRequestString("upDown");

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

        String tip = null;
        if (user.isStudent() && Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            tip = processCountenanceTag(user.getId(), categoryId);
        }

        Double couponDiecount = getTeacherCouponDiscount(user);
        RewardProductDetailPagination pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.PC.name(),
                categoryType, categoryId, oneLevelFilterId, 0L,showAffordable, pageNum, pageSize, orderBy, upDown, couponDiecount);

        List<ProductFilterEntity> filter = buildFilterItem(userVisibleType, categoryId, categoryType, oneLevelFilterId);

        return MapMessage.successMessage()
                .add("pageNum", pagination.getNumber())
                .add("pageSize", pagination.getSize())
                .add("rows", pagination.getContent())
                .add("totalPage", pagination.getTotalPages())
                .add("totalSize", pagination.getTotalElements())
                .add("filter", filter)
                .add("publicGoodTip", tip);
    }

    public String processCountenanceTag(long userId, Long categoryId) {
        if (Objects.equals(categoryId, Long.valueOf(SetIdLogicRelation.TEACHER_PUBLIC_GOOD.getNumber()))) {
            Long publicGoodsTime = rewardCenterClient.getDonationCount(userId);
            if (publicGoodsTime < 5) {
                return "本专区商品需参加过5次公益活动捐赠才有兑换资格";
            }
            return "您已参加过5次公益活动捐赠，获得专区商品兑换资格";
        }
        return StringUtils.EMPTY;
    }


    private List<ProductFilterEntity> buildFilterItem(Integer userVisibleType, Long categoryId, Integer categoryType, Long oneLevelFilterId) {
        List<ProductFilterEntity> filter;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            filter = buildSetFilterItem(categoryId);
            checkSetFilterItem(filter, oneLevelFilterId);
        } else {
            filter = getCategoryFilterItem(userVisibleType, categoryId);
            checkCategoryFilterItem(filter, oneLevelFilterId);
        }
        return filter;
    }

    //选中分类类型为分类集合的过滤条件（无一级过滤条件）
    private void checkSetFilterItem(List<ProductFilterEntity> filter, Long twoLevelFilterId) {
        if (CollectionUtils.isEmpty(filter)) {
            return;
        }

        //选择了二级过滤条件
        if (Objects.equals(twoLevelFilterId, 0L)) {
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(true));
            //选择了二级过滤条件
        } else {
            filter.stream()
                    .forEach(item -> {
                        if (Objects.equals(twoLevelFilterId, item.getId())) {
                            item.setStatus(true);
                        }
                    });
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(false));
        }
    }

    //选中分类类型为一级分类的过滤条件
    private void checkCategoryFilterItem(List<ProductFilterEntity> filter, Long oneLevelFilterId) {
        if (CollectionUtils.isEmpty(filter)) {
            return;
        }

        //过滤条件都不选择
        if (Objects.equals(oneLevelFilterId, 0L)) {
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(true));
            return;
            //选择一级过滤不选择条件
        } else {
            filter.stream()
                    .forEach(item -> {
                        if (Objects.equals(oneLevelFilterId, item.getId())) {
                            item.setStatus(true);
                        }
                    });
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(false));
        }
    }

    //构建分类类型为分类集合的过滤器
    private List<ProductFilterEntity> buildSetFilterItem(Long setId) {
        List<ProductFilterEntity> result = new ArrayList<>();
        result.addAll(getTagFilterItem(setId, ProductTag.ParentType.SET.getType()));
        return result;
    }

    //构建分类类型为一级分类的过滤器
    private List<ProductFilterEntity> getCategoryFilterItem(Integer userVisibleType, Long oneLevelCategoryId) {
        List<ProductFilterEntity> result = new ArrayList<>();

        //List<ProductCategory> categoryList = newRewardLoaderClient.loadProductCategoryByParentId(oneLevelCategoryId);
        List<ProductCategory> categoryList = newRewardBufferLoaderClient.getProductCategoryBuffer().loadByParentId(oneLevelCategoryId);
        if (CollectionUtils.isNotEmpty(categoryList)) {
            result = categoryList.stream()
                    .filter(category -> RewardUserVisibilityType.isVisible(category.getVisible(), userVisibleType))
                    .sorted(Comparator.comparing(ProductCategory::getDisplayOrder).reversed())
                    .map(category -> {
                        ProductFilterEntity oneLevelFilter = new ProductFilterEntity();
                        oneLevelFilter.setId(category.getId());
                        oneLevelFilter.setName(category.getName());
                        return oneLevelFilter;
                    }).collect(toList());
        }
        return result;
    }

    private List<ProductFilterEntity> getTagFilterItem(Long parentId, Integer parentType) {
        List<ProductFilterEntity> result = Collections.EMPTY_LIST;
        List<ProductTag> tagList = newRewardLoaderClient.loadProductTagByParent(parentId, parentType);
        if (CollectionUtils.isNotEmpty(tagList)) {
            result = tagList.stream()
                    .sorted(Comparator.comparing(ProductTag::getDisplayOrder).reversed())
                    .map(tag -> {
                        ProductFilterEntity twoLevelFilterItem = new ProductFilterEntity();
                        twoLevelFilterItem.setId(tag.getId());
                        twoLevelFilterItem.setName(tag.getName());
                        return twoLevelFilterItem;
                    }).collect(toList());
        }
        return result;
    }

    /**
     * 奖品详情页 实物奖品
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String loadProductDetail(Model model) {
        User user = currentRewardUser();

        Long productId = getRequestLong("productId", 0L);
        RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (detail == null || !SafeConverter.toBoolean(detail.getOnline())) {
            return "redirect:/reward/index.vpage";
        }
        detail.setOneLevelCategoryType(newRewardLoaderClient.getOneLevelCategoryType(detail.getOneLevelCategoryId()));
        detail.setTags(newRewardLoaderClient.getTagName(user, productId));
        if (!newRewardLoaderClient.isSHIWU(detail.getOneLevelCategoryId())) {
            return "redirect:/reward/index.vpage";
        }

        // 详情信息里面已经不会存储sku信息，需要现查~~~
        List<RewardSku> skus = rewardLoaderClient.loadProductSku(detail.getId());
        detail.setSkus(skus);

        int inventory = CollectionUtils.isNotEmpty(skus) ? skus.stream().mapToInt(RewardSku::getInventorySellable).sum() : 0;
        model.addAttribute("inventory", inventory);
        model.addAttribute("detail", detail);
        model.addAttribute("tagType", getRequestStringCleanXss("tagType"));
        model.addAttribute("productId", productId);

        if(user.isStudent()){
            // 下线城市的灰度地区
            boolean isOffline = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable((StudentDetail) user,"Reward","OfflineShiWu", true);
            model.addAttribute("rewardOffline",isOffline);
        }

        if (user.isTeacher()) {
            TeacherCouponMapper mapper = this.getTeacherCouponMapper(user);
            Integer teacherNewLevel = mapper==null ? null:mapper.getTeacherNewLevel();
            List<TeacherCouponEntity> teacherCouponList = mapper==null ? null:mapper.getTeacherCouponList();
            model.addAttribute("teacherNewLevel", teacherNewLevel);
            model.addAttribute("teacherCouponList", teacherCouponList);
            model.addAttribute("fakeTeacher", teacherLoaderClient.isFakeTeacher(user.getId()));
        }

        return "reward/product/detail";
    }

    private Double getTeacherCouponDiscount(User user) {
        TeacherCouponMapper couponMapper = this.getTeacherCouponMapper(user);
        Double couponDiscount = 1d;

        //有优惠券的特权老师
        if (Objects.nonNull(couponMapper) && CollectionUtils.isNotEmpty(couponMapper.getTeacherCouponList())) {
            Integer level = couponMapper.getTeacherNewLevel();
            if (Objects.equals(level, TeacherExtAttribute.NewLevel.INTERMEDIATE.getLevel())) {
                couponDiscount = 0.9;
            } else if (Objects.equals(level, TeacherExtAttribute.NewLevel.SENIOR.getLevel())) {
                couponDiscount = 0.85;
            } else if (Objects.equals(level, TeacherExtAttribute.NewLevel.SUPER.getLevel())) {
                couponDiscount = 0.8;
            }
        }
        return couponDiscount;
    }

    private TeacherCouponMapper getTeacherCouponMapper(User user) {
        TeacherCouponMapper mapper = null;
        if (user.isTeacher()) {
            mapper = new TeacherCouponMapper();
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(user.getId());
            mapper.setTeacherNewLevel(teacherExtAttribute==null || teacherExtAttribute.getNewLevel()==null ? 0:teacherExtAttribute.getNewLevel());
            List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserRewardCoupons(user.getId());
            if (CollectionUtils.isNotEmpty(couponShowMappers)) {
                List<TeacherCouponEntity> couponEntityList = couponShowMappers
                        .stream()
                        .filter(maper -> Objects.equals(maper.getCouponUserStatus().getDesc(), CouponUserStatus.NotUsed.getDesc()))
                        .map(maper -> {
                            String couponUserRefId = maper.getCouponUserRefId();
                            Double discount = maper.getTypeValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            TeacherCouponEntity entity = new TeacherCouponEntity(discount, couponUserRefId);
                            return entity;
                        }).collect(toList());
                mapper.setTeacherCouponList(couponEntityList);
            }
        }
        return mapper;
    }

    /**
     * 奖品详情页 虚拟奖品
     */
    @RequestMapping(value = "experience/detail.vpage", method = RequestMethod.GET)
    public String loadByProductDetail(Model model) {
        User user = currentRewardUser();

        Long productId = getRequestLong("productId", 0L);
        RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (detail == null || !SafeConverter.toBoolean(detail.getOnline())) {
            return "redirect:/reward/index.vpage";
        }
        detail.setOneLevelCategoryType(newRewardLoaderClient.getOneLevelCategoryType(detail.getOneLevelCategoryId()));
        detail.setTags(newRewardLoaderClient.getTagName(user, productId));

        if (newRewardLoaderClient.isSHIWU(detail.getOneLevelCategoryId())) {
            return "redirect:/reward/index.vpage";
        }
        model.addAttribute("detail", detail);

        // 加入子商品类别，以区分兑换券、课件的商品详情页
        model.addAttribute("oneLevelCategoryId", detail.getOneLevelCategoryId());
        model.addAttribute("showInventory", newRewardLoaderClient.isCoupon(detail.getOneLevelCategoryId()));

        // 添加联系信息
        List<Map<String, Object>> mobileList = newRewardLoaderClient.loadMobileInfo(user);
        model.addAttribute("mobileList", mobileList);

        List<RewardCouponDetail> coupons = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(detail.getId());
        coupons = coupons.stream().filter(source ->
                !Boolean.TRUE.equals(source.getExchanged())
        ).collect(Collectors.toList());
        model.addAttribute("inventory",  CollectionUtils.isNotEmpty(coupons) ? coupons.size() : 0);
        model.addAttribute("hasCoupon", CollectionUtils.isNotEmpty(coupons));
        model.addAttribute("tagType", getRequestStringCleanXss("tagType"));

        if (user.isTeacher()) {
            TeacherCouponMapper mapper = this.getTeacherCouponMapper(user);
            Integer teacherNewLevel = mapper==null ? null:mapper.getTeacherNewLevel();
            List<TeacherCouponEntity> teacherCouponList = mapper==null ? null:mapper.getTeacherCouponList();
            model.addAttribute("teacherNewLevel", teacherNewLevel);
            model.addAttribute("teacherCouponList", teacherCouponList);
            model.addAttribute("fakeTeacher", teacherLoaderClient.isFakeTeacher(user.getId()));
        }

        return "reward/product/experience/detail";
    }

    /**
     * ajax 我是否可以兑换这个体验奖品
     */
    @RequestMapping(value = "canexchanged.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage canExchanged() {
        //我的优惠劵
        final Long productId = getRequestLong("productId");
        List<RewardCouponDetail> coupons = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(currentUserId());
        coupons = coupons.stream()
                .filter(source -> Objects.equals(productId, source.getProductId()))
                .collect(Collectors.toList());

        RewardCouponDetail myCoupon = MiscUtils.firstElement(coupons);
        if (myCoupon != null) {
            return MapMessage.errorMessage("你已经兑换过了");
        } else {
            return MapMessage.successMessage();
        }
    }

    /**
     * 从广告位跳转到体验产品专题页(学生权限)
     */
    @RequestMapping(value = "couponspecial.vpage", method = RequestMethod.GET)
    public String couponSpecial(Model model) throws Exception {
        User user = currentRewardUser();
        if (UserType.STUDENT != user.fetchUserType()) {
            return "redirect:/reward/index.vpage";
        }
        String type = getRequestParameter("type", null);
        if (type == null) {
            return "redirect:/reward/index.vpage";
        }
        CouponProductionName couponProductionName = CouponProductionName.typeOf(type);
        if (null == couponProductionName) {
            return "redirect:/reward/index.vpage";
        }
        RewardProduct product = rewardLoaderClient.loadProductByProductName(couponProductionName);
        if (product == null) {
            return "redirect:/reward/index.vpage";
        }
        final RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, product.getId());
        //无权限查看
        if (detail == null || !RewardProductType.JPZX_TIYAN.name().equals(detail.getProductType())) {
            return "redirect:/reward/index.vpage";
        }
        model.addAttribute("detail", detail);

        List<Map<String, Object>> mobileList = newRewardLoaderClient.loadMobileInfo(user);
        model.addAttribute("mobileList", mobileList);

        List<RewardCouponDetail> coupons = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(user.getId());
        coupons = coupons.stream().filter(source ->
                Objects.equals(detail.getId(), source.getProductId())
        ).collect(Collectors.toList());
        model.addAttribute("myCoupon", MiscUtils.firstElement(coupons));

        return "reward/specialpage/" + type;
    }

    @RequestMapping(value = "/orderconfirm.vpage")
    public String addressDetail(Model model) {
        try{
            User user = currentRewardUser();
            Validate.isTrue(UserType.TEACHER == user.fetchUserType());

            Long productId = getRequestLong("productId");
            RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
            Validate.notNull(detail);
            detail.setTags(newRewardLoaderClient.getTagName(user, detail.getId()));

            MapMessage message = userServiceClient.generateUserShippingAddress(user.getId());

            model.addAttribute("productDetail",detail);
            model.addAttribute("address",message.get("address"));
            model.addAttribute("receiverPhone",message.get("receiverPhone"));

            return "reward/product/orderconfirm";
        }catch (Exception e){
            return "redirect:/reward/index.vpage";
        }
    }
}
