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

package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.TeacherCouponMapper;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.ProductFilterEntity;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.client.PublicGoodLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardServiceClient;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductSet;
import com.voxlearning.utopia.service.reward.mapper.*;
import com.voxlearning.utopia.service.reward.mapper.product.CategoryMapper;
import com.voxlearning.utopia.service.reward.mapper.product.MobileHomePageCategoryMapper;
import com.voxlearning.utopia.service.reward.util.DuibaTool;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanCopier;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.reward.constant.RewardOrderStatus.DELIVER;
import static com.voxlearning.utopia.service.reward.constant.RewardOrderStatus.FAILED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * 目前在用的有中小学老师,以及中学学生.
 * Created by jiangpeng on 16/5/26.
 */

@Controller
@RequestMapping(value = "/usermobile/giftMall")
@Slf4j
public class MobileUserRewardCenterController extends AbstractMobileController {

    final static String CHECK_RESULT_TYPE = "status";

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    @Inject private RewardCenterClient rewardCenterClient;
    @Inject private CouponLoaderClient couponLoaderClient;
    @Inject private CouponServiceClient couponServiceClient;
    @Inject private PublicGoodLoaderClient pbLoaderCli;
    @Inject private NewRewardServiceClient newRewardServiceClient;
    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    /** Copier from RewardActivity to PGActivityMapper **/
    private BeanCopier publicGoodsCopier;

    @Override
    public void afterPropertiesSet(){
        publicGoodsCopier = BeanCopier.create(RewardActivity.class, PGActivityMapper.class,false);
    }

    @RequestMapping(value = "/homePageData.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getNewHomePageDatal() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        List<MobileHomePageCategoryMapper> mobileCategoryMappers = newRewardLoaderClient.getMobileHomePageCategoryMapper(user);

        List<PGActivityMapper> pgActivityMappers = getPGActivityMapper(user);

        MapMessage result = MapMessage.successMessage()
                .add("publicGoods", CollectionUtils.isEmpty(pgActivityMappers) ? null:pgActivityMappers.get(0))
                .add("integral", rewardCenterClient.getIntegral(user))
                .add("userId", user.getId());
        result.add("productRegion", mobileCategoryMappers);
        return result;
    }

    @RequestMapping(value = "/productList.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getNewProductList() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long categoryId = getRequestLong("categoryId", 0);
        Integer categoryType = getRequestInt("categoryType", 0);
        Long oneLevelFilterId = getRequestLong("oneLevelFilterId", 0);
        Long twoLevelFilterId = getRequestLong("twoLevelFilterId", 0);

        Boolean showAffordable = getRequestBool("showAffordable", false);
        Integer pageNum = getRequestInt("pageNum", 1);
        Integer pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        if (StringUtils.isBlank(orderBy)) {
            orderBy = "teacherOrderValue";
        }
        String upDown = getRequestString("upDown");Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        //如果没有分类类型，给一个默认值
        if (Objects.equals(categoryId, 0L)) {
            List<CategoryMapper> mappers = newRewardLoaderClient.getCategoryMapper(user, userVisibleType, RewardDisplayTerminal.Mobile.name());
            CategoryMapper categoryMapper = mappers.stream().findFirst().orElse(null);
            if (Objects.nonNull(categoryMapper)) {
                categoryId = categoryMapper.getId();
                categoryType = categoryMapper.getType();
            }
        }

        if (Objects.equals(categoryId, 0L)) {
            return MapMessage.successMessage("分类类型错误！");
        }

        Double couponDiecount = getTeacherCouponDiscount(user);
        RewardProductDetailPagination pagination = newRewardLoaderClient.loadOnePageProductList(user, RewardDisplayTerminal.Mobile.name(),
                categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, pageNum, pageSize, orderBy, upDown, couponDiecount);

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
                .add("rows", pagination.getContent())
                .add("filter", filter)
                .add("hasNext", CollectionUtils.isNotEmpty(pagination.getContent()) && pagination.getContent().size()>=pageSize);
    }

    @RequestMapping(value = "/productNavigation.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getProductNavigation() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long categoryId = getRequestLong("categoryId", 0L);
        Integer categoryType = getRequestInt("categoryType", 0);

        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        Predicate<CategoryMapper> filter = mapper -> !(Objects.equals(mapper.getId(), categoryId) && Objects.equals(mapper.getType(), categoryType));

        List<CategoryMapper> categoryMappers = newRewardLoaderClient.getCategoryMapper(user, userVisibleType, RewardDisplayTerminal.Mobile.name());
        categoryMappers = categoryMappers.stream().filter(filter).collect(toList());

        return MapMessage.successMessage()
                .add("productNavigation", categoryMappers);
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


    private List<PGActivityMapper> getPGActivityMapper(User user) {
        Map<Long,List<PublicGoodCollect>> collectMap = pbLoaderCli.loadCollectByUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(c -> c.getActivityId()));
        // 筛选出来上线的
        return rewardLoaderClient.loadRewardActivities()
                .stream()
                .filter(RewardActivity::getOnline)
                .filter(act -> {
                    // 学生的没限制，家长只给看完成的老项目和所有的新项目
                    if (user.isStudent() || user.isTeacher()) {
                        return true;
                    } else if (user.isParent()) {
                        PublicGoodModel model = PublicGoodModel.parse(act.getModel());
                        return act.isFinished() || model != PublicGoodModel.NONE;
                    }else
                        return false;
                })
                // 按时间倒序，最新的在最前面
                // 这个改成按照排序值来排序
                // 未完成的排在前面，如果状态相同先看排序值，再按创建时间倒序
                .sorted((a1, a2) -> {
                    Integer a2Ow = SafeConverter.toInt(a2.getOrderWeights());
                    Integer a1Ow = SafeConverter.toInt(a1.getOrderWeights());

                    if(Objects.equals(a1.getStatus(),a2.getStatus())){
                        if(Objects.equals(a1Ow,a2Ow)){
                            return a2.getCreateDatetime().compareTo(a1.getCreateDatetime());
                        }else
                            return Integer.compare(a2Ow,a1Ow);
                    }else if(a1.isOnGoing()){
                        return -1;
                    }else{
                        return 1;
                    }
                })
                .map(activity -> {
                    PGActivityMapper mapper = new PGActivityMapper();
                    publicGoodsCopier.copy(activity,mapper,null);

                    // 正在进行中的Collect ID，如果没有建设中的教室
                    // 则选取最近完成的教室
                    AtomicReference<String> onGoingId = new AtomicReference<>();
                    String lastFinishId = collectMap.getOrDefault(activity.getId(),Collections.emptyList())
                            .stream()
                            .filter(c -> {
                                if(c.isFinished()){
                                    return true;
                                }else{
                                    onGoingId.set(c.getId());
                                    return false;
                                }
                            })
                            .sorted((c1,c2) -> Long.compare(c2.getFinishTime(),c1.getFinishTime()))
                            .map(c -> c.getId())
                            .findFirst()
                            .orElse(null);

                    if (onGoingId.get() == null) onGoingId.set(lastFinishId);
                    mapper.setCollectId(onGoingId.get());

                    // 如果是家长则显示孩子们的参加信息
                    if(user.isParent()){
                        List<Map<String,Object>> childList = new ArrayList<>();
                        Optional.ofNullable(pbLoaderCli.loadParentChildRef(user.getId()))
                                .map(pc -> pc.getChildMap())
                                .map(childMap -> childMap.keySet())
                                .orElse(Collections.emptySet())
                                .stream()
                                .map(stuId -> raikouSystem.loadUser(stuId))
                                .forEach(u -> childList.add(MapUtils.m("name",u.fetchRealname(),"avatarImg",getUserAvatarImgUrl(u))));

                        mapper.setChildList(childList);
                    }else if(user.isStudent()){
                        // 获得老师参与记录
                        if (activity.isOldModel()) {
                            mapper.setTchJoinList(Collections.emptyList());
                        } else {
                            mapper.setTchJoinList(pbLoaderCli.getTeacherJoinStatus(activity.getId(), user.getId()));
                        }
                    }else if (user.isTeacher()) {
                        mapper.setTargetMoney(mapper.getTargetMoney() / 10);
                        mapper.setRaisedMoney(mapper.getRaisedMoney() / 10);
                    }
                    return mapper;
                })
                .collect(toList());
    }

    @RequestMapping(value = "/integral/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getIntegral() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }
        return MapMessage.successMessage().add("integral", rewardCenterClient.getIntegral(user));
    }

    /**
     * 热门商品推荐。
     * 置上热门商品的标签ID，转发给获取商品接口
     *
     * @return
     */
    @RequestMapping(value = "/hot.vpage")
    @ResponseBody
    public MapMessage loadHotProducts() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        int displayNums = getRequestInt("displayNums");

        LoadRewardProductContext context = createDefaultContext();
        context.oneLevelTagId = 6L;
        context.loadPage = "tag";
        context.categoryId = 0L;

        List<RewardProductDetail> products = loadProducts(user, context, detail -> true);
        // 截出需要显示的数量
        if (displayNums != 0) {
            products = products.stream().limit(displayNums)
                    .collect(toList());
        }

        return MapMessage.successMessage()
                .add("productList", products);
    }

    /**
     * 创建老师APP端，默认的查询上下文
     *
     * @return
     */
    private LoadRewardProductContext createDefaultContext() {

        LoadRewardProductContext context = new LoadRewardProductContext();
        context.twoLevelTagId = 0L;
        context.orderBy = "teacherOrderValue";
        context.upDown = "down";
        context.pageNumber = 0;
        context.pageSize = 21;
        context.canExchangeFlag = false;
        context.loadPage = "all";
        context.terminal = RewardDisplayTerminal.Mobile;
        context.showAffordable = false;

        return context;
    }

    @RequestMapping(value = "/category/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage allCategory() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        // 记录访问的轨迹
        if(Objects.equals(user.getUserType(), UserType.TEACHER.getType()))
            rewardServiceClient.recordAccessTrace(user.getId());

        // 老师目前只取实物商品
        List<RewardCategory> allCategories = new ArrayList<>(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_SHIWU, user.fetchUserType()));
        // 老师端也添加虚拟类别
        allCategories.addAll(rewardLoaderClient.loadRewardCategories(RewardProductType.JPZX_TIYAN, user.fetchUserType()));

        List<Map<String, Object>> categoryList = allCategories.stream()
                .filter(e -> !user.isStudent() ||
                        (currentStudentDetail() != null && currentStudentDetail().isPrimaryStudent() && Boolean.TRUE.equals(e.getPrimaryVisible())) ||
                        (currentStudentDetail() != null && currentStudentDetail().isJuniorStudent() && Boolean.TRUE.equals(e.getJuniorVisible())))
                // 按照Display Order排序
                .sorted(Comparator.comparingInt(RewardCategory::getDisplayOrder))
                .map(t -> MapUtils.m("id",t.getId(),"name",t.getCategoryName()))
                .collect(toList());

        // 在起始位置加上全部选项
        // 加在第一个位置
        categoryList.add(0,MapUtils.m("id", 0, "name", "全部"));

        int tipShowFlag = rewardServiceClient.tryShowTipFlag(user);

        String unit = fetchUnit(user);
        return MapMessage.successMessage()
                .add("categoryList", categoryList)
                .add("integral_text", unit)
                .add("tipShowFlag", tipShowFlag);

    }

    /**
     * 获得所有商品的排序类型
     *
     * @return
     */
    @RequestMapping(value = "/getsorttype.vpage")
    @ResponseBody
    public MapMessage getSortType() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Map<String, KeyValuePair<String, String>> sortTypes = new LinkedHashMap<>();

        sortTypes.put("销量", new KeyValuePair<>("soldQuantity", "down"));
        sortTypes.put("上架时间", new KeyValuePair<>("createDatetime", "down"));
        sortTypes.put("价格由高到低", new KeyValuePair<>("price", "up"));
        sortTypes.put("价格由低到高", new KeyValuePair<>("price", "down"));

        return MapMessage.successMessage()
                .add("sortTypes", sortTypes);
    }

    @RequestMapping(value = "/products/get_by_category.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadProducts() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long categoryId = getRequestLong("categoryId", 0L);
        // 当前可兑换的选项
        Boolean showAffordable = getRequestBool("showAffordable", false);

        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 20);
        String orderBy = getRequestString("orderBy");
        String upDown = getRequestString("upDown");

        LoadRewardProductContext context = createDefaultContext();
        context.categoryId = categoryId;
        context.pageNumber = pageNum - 1;
        context.pageSize = pageSize;
        context.showAffordable = showAffordable;
        context.orderBy = orderBy;
        context.upDown = upDown;

        List<RewardProductDetail> productList = loadProducts(user, context, detail -> true);

        return MapMessage.successMessage()
                .add("productList", productList);
    }

    /**
     * 获取商品列表
     *
     * @param user
     * @return
     */
    private List<RewardProductDetail> loadProducts(User user, LoadRewardProductContext context, Predicate<RewardProductDetail> filter) {

        RewardProductDetailPagination pagination = rewardLoaderClient.loadRewardProducts(user, context,
                u -> asyncUserServiceClient.getAsyncUserService()
                        .loadUserSchool(u)
                        .getUninterruptibly(),
                null, filter);

        pagination.getContent().forEach(t -> {
            t.setImage(combineCdbUrl(t.getImage()));
            t.getImages().forEach(p -> p.setLocation(combineCdbUrl(p.getLocation())));
        });

        return pagination.getContent();
    }

    /**
     * 下单前的地址确认页
     * @return
     */
    @RequestMapping(value = "/address/detail.vpage")
    @ResponseBody
    public MapMessage addressDetail(){
        try{
            User user = currentRewardUser();
            Validate.notNull(user,"未登陆，不能操作");

            Validate.isTrue(user.getUserType() == UserType.TEACHER.getType(),"只有老师才能操作!");

            Long productId = getRequestLong("productId");
            RewardProductDetail rewardProductDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
            Validate.notNull(rewardProductDetail,"商品不存在!");

            rewardProductDetail.getImages().forEach(t -> t.setLocation(combineCdbUrl(t.getLocation())));
            rewardProductDetail.setImage(combineCdbUrl(rewardProductDetail.getImage()));

            MapMessage message = userServiceClient.generateUserShippingAddress(user.getId());
            if(!message.isSuccess())
                return message;

            return MapMessage.successMessage()
                    .add("productDetail",rewardProductDetail)
                    .add("address",message.get("address"))
                    .add("receiverPhone",message.get("receiverPhone"));

        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/product/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage productDetail() {

        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        Long productId = getRequestLong("productId");
        Long skuId = getRequestLong("skuId");
        RewardProductDetail rewardProductDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (rewardProductDetail == null) {
            return MapMessage.errorMessage("错误商品");
        }
        rewardProductDetail.setOneLevelCategoryType(newRewardLoaderClient.getOneLevelCategoryType(rewardProductDetail.getOneLevelCategoryId()));
        rewardProductDetail.setTags(newRewardLoaderClient.getTagName(user, productId));
        List<RewardSku> skus = rewardLoaderClient.loadProductSku(productId);
        if (CollectionUtils.isEmpty(skus)) {
            return MapMessage.errorMessage("错误商品");
        }
        skus = skus.stream().sorted(Comparator.comparing(RewardSku::getInventorySellable).reversed()).collect(toList());
        RewardSku sku = skus.stream().filter(skuItem -> Objects.equals(skuItem.getId(), skuId)).findFirst().orElse(null);
        if (sku == null) {
            sku = skus.get(0);
        } else {
            rewardProductDetail.addSku(sku);
        }

        rewardProductDetail.setSellAbleCount(sku.getInventorySellable());
        MapMessage resultMap = MapMessage.successMessage();

        TeacherCouponEntity coupon = null;
        if (user.isTeacher()) {
            TeacherCouponMapper mapper = this.getTeacherCouponMapper(user);
            Integer teacherNewLevel = mapper==null ? null:mapper.getTeacherNewLevel();
            List<TeacherCouponEntity> teacherCouponList = mapper==null ? null:mapper.getTeacherCouponList();
            if (CollectionUtils.isNotEmpty(teacherCouponList)) {
                coupon = teacherCouponList.get(0);
            }
            resultMap.add("teacherNewLevel", teacherNewLevel);
            resultMap.add("teacherCouponList", teacherCouponList);
        }

        MapMessage checkMessage = checkCanBuy(user, rewardProductDetail, 1, sku, coupon);
        Map<String, Object> statusMap = new LinkedHashMap<>();
        if (!checkMessage.isSuccess()) {
            String msg = checkMessage.getInfo();
            String status = (String) checkMessage.get(CHECK_RESULT_TYPE);
            statusMap.put("message", msg);
            statusMap.put("status", status);
        } else {
            statusMap.put("message", "立即兑换");
            statusMap.put("status", "ok");
            if (user.fetchUserType() == UserType.TEACHER && teacherLoaderClient.isFakeTeacher(user.getId())) {
                statusMap.put("fakeTeacherMsg", "您的账号使用存在异常，该功能受限。如有疑议，请联系客服400-160-1717");
            }
        }
        resultMap.add("productStatus", statusMap);
        rewardProductDetail.setImage(combineCdbUrl(rewardProductDetail.getImage()));
        rewardProductDetail.getImages().forEach(t -> t.setLocation(combineCdbUrl(t.getLocation())));

        resultMap.add("productDetail", rewardProductDetail);

        // 加入sku
        resultMap.add("skuList", skus);

        // 最少是一件起兑
        Integer minBuyNums = rewardProductDetail.getMinBuyNums();
        if (minBuyNums == null)
            minBuyNums = 1;

        // 加入几件起兑的参数
        resultMap.add("minBuyNums", minBuyNums);

        // 查看商品是否在心愿池里面
        List<Map<String, Object>> wishList = rewardLoaderClient.getWishDetails(user);
        Map<String, Object> wishOrder = wishList.stream()
                .filter(o -> Objects.equals(productId, SafeConverter.toLong(o.get("productId"))))
                .findFirst()
                .orElse(null);

        if (wishOrder != null) {
            resultMap.add("inWishList", true);
            resultMap.add("wishOrderId", SafeConverter.toLong(wishOrder.get("wishOrderId")));
        }

        UserIntegral integral = null;
        if (UserType.TEACHER.equals(user.fetchUserType())) {
            integral = ((TeacherDetail) user).getUserIntegral();
        } else if (UserType.STUDENT.equals(user.fetchUserType())) {
            integral = ((StudentDetail) user).getUserIntegral();
        }

        // 学豆参数
        if (integral == null) {
            resultMap.add("usableIntegral", 0);
        } else
            resultMap.add("usableIntegral", integral.getUsable());

        //是否显示库存
        boolean showInventory = newRewardLoaderClient.isSHIWU(rewardProductDetail.getOneLevelCategoryId()) ? true:false;
        resultMap.add("showInventory", showInventory);

        // 传回商品的类型，供前端显示不同的样式
        resultMap.add("categories", rewardProductDetail.getOneLevelCategoryType());

        int inventory = showInventory ? sku.getInventorySellable() : 0;
        if (newRewardLoaderClient.isCoupon(rewardProductDetail.getOneLevelCategoryId())) {
            List<RewardCouponDetail> couponDetailList = rewardLoaderClient.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productId).stream().filter(e -> !Boolean.TRUE.equals(e.getExchanged()))
                    .collect(toList());
            inventory = CollectionUtils.isNotEmpty(couponDetailList) ? couponDetailList.size() : 0;
        }
        resultMap.add("inventory", inventory);
        // 兑换券和流量包需要验证手机
        boolean needVerifyMobile = false;
        if (newRewardLoaderClient.isFlowPacket(rewardProductDetail.getOneLevelCategoryId())) {
            needVerifyMobile = true;
        } else if (newRewardLoaderClient.isCoupon(rewardProductDetail.getOneLevelCategoryId())
                || newRewardLoaderClient.isTeachingResources(rewardProductDetail.getOneLevelCategoryId())) {
            needVerifyMobile = true;

            boolean exchanged = false;
            // 查看是否兑换过
            if (newRewardLoaderClient.isCoupon(rewardProductDetail.getOneLevelCategoryId()) || newRewardLoaderClient.isTeachingResources(rewardProductDetail.getOneLevelCategoryId())) {

                //查看订单表是否已存在
                RewardOrder histryOrder = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId()).stream().filter(order -> Objects.equals(order.getProductId(), productId)).findAny().orElse(null);
                if (Objects.nonNull(histryOrder)) {
                    exchanged = true;
                }

                //如果订单表不存在，查看优惠券表是否存在已兑换（老逻辑教学资源和优惠券兑换记录都在这个表里）
                if (!exchanged) {
                    exchanged = rewardLoaderClient.getRewardCouponDetailLoader()
                            .loadUserRewardCouponDetails(user.getId())
                            .stream()
                            .anyMatch(c -> Objects.equals(c.getProductId(), productId));
                }
            }
            resultMap.add("exchanged", exchanged);
        }

        if (needVerifyMobile) {
            String am = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "/reward/product/experience/detail", SafeConverter.toString(user.getId()));
            if (StringUtils.isNoneBlank(am)) {
                List<Map<String, Object>> mobileList = new ArrayList<>();
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("callName", "我");
                studentMap.put("mobile", am);
                mobileList.add(studentMap);

                resultMap.add("mobileList", mobileList);
            }
        }
        return resultMap;
    }

    /**
     * 此处有坑,虽然有商品sku概念,但需求方并不设置多个sku,而是统一设置一个随机.在此只取第一个sku,作为默认sku.
     * 考虑到效率，现在detail数据拼装中，已经不再查sku。在详情页中直接查询置入
     *
     * @param productId
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
    @RequestMapping(value = "/product/exchange.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage exchangeProduct() {
        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }

        //假老师处理
        if (user.isTeacher() && teacherLoaderClient.isFakeTeacher(user.getId())) {
            return MapMessage.errorMessage("您的账号使用存在异常，该功能受限。如有疑议，请联系客服400-606-1717");
        }

        Long productId = getRequestLong("productId");
        Integer quantity = getRequestInt("quantity");
        String couponUserRefId = getRequestString("couponUserRefId");
        if (quantity <= 0) {
            return MapMessage.errorMessage("请输入正确的奖品数量");
        }
        RewardProductDetail productDetail = newRewardLoaderClient.generateRewardProductDetail(user, productId);
        if (productDetail == null) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }

        RewardSku sku = null;
        if (newRewardLoaderClient.isSHIWU(productDetail.getOneLevelCategoryId())) {
            Long skuId = getRequestLong("skuId");
            sku = rewardLoaderClient.loadProductSku(productId)
                    .stream()
                    .filter(s -> Objects.equals(skuId, s.getId()))
                    .findFirst()
                    .orElse(null);
            if (sku == null) {
                return MapMessage.errorMessage("错误商品!");
            }
        }

        TeacherCouponEntity coupon = null;
        if (user.isTeacher() && StringUtils.isNotBlank(couponUserRefId)) {
            List<CouponShowMapper> couponShowMapperList = couponLoaderClient.loadUserRewardCoupons(user.getId());
            if (CollectionUtils.isEmpty(couponShowMapperList)) {//无兑换券
                return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
            }
            for (CouponShowMapper mapper : couponShowMapperList) {
                if (Objects.equals(mapper.getCouponUserStatus().getDesc(), CouponUserStatus.NotUsed.getDesc()) && Objects.equals(mapper.getCouponUserRefId(), couponUserRefId)) {
                    coupon = new TeacherCouponEntity(mapper.getTypeValue().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(), mapper.getCouponUserRefId());
                    break;
                }
            }
            //兑换券已失效
            if (coupon == null) {
                return MapMessage.errorMessage("该优惠券已失效，请检查并重新兑换!");
            }
        }

        MapMessage checkMessage = checkCanBuy(user, productDetail, quantity, sku, coupon);//创建订单统一check了，这里为什么又check一次
        if (!checkMessage.isSuccess()) {
            return checkMessage;
        }

        MapMessage message = newRewardServiceClient.createRewardOrder(user, productDetail, sku, quantity, null, RewardOrder.Source.app, coupon);
        if (message.isSuccess()) {
            //使用掉优惠券
            if (user.isTeacher() && coupon != null) {
                couponServiceClient.updateCouponUserRefStatus(couponLoaderClient.loadCouponUserRefById(coupon.getCouponUserRefId()), CouponUserStatus.Used);
            }
        }

        return message;

    }

    /**
     * 愿望池
     *
     * @return
     */
    @RequestMapping(value = "/mywish.vpage")
    @ResponseBody
    public MapMessage myWish() {
        User user = currentUser();
        if (user == null) {
            return noLoginResult;
        }

        List<Map<String, Object>> wishList = rewardLoaderClient.getWishDetails(user);
        return MapMessage.successMessage().add("wishList", wishList);
    }

    /**
     * 显示已发货的订单，需要按照物流信息分组
     *
     * @return
     */
    @RequestMapping(value = "/order/delivered.vpage")
    @ResponseBody
    public MapMessage deliveredOrders() {
        User user = currentUser();
        if (user == null) {
            return noLoginResult;
        }

        // 订单状态的过滤条件
        Predicate<RewardOrder> isDeliverOrder = r -> Objects.equals(DELIVER.name(),r.getStatus());
        Predicate<RewardOrder> isFailedOrder = r -> Objects.equals(FAILED.name(),r.getStatus());

        List<RewardCouponDetail> coupons = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(user.getId());

        Map<Long, RewardCouponDetail> orderCouponMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(coupons)) {
            orderCouponMap = coupons
                    .stream()
                    .filter(coupon -> Objects.nonNull(coupon.getOrderId()) && !Objects.equals(coupon.getOrderId(), 0L))
                    .collect(Collectors.toMap(RewardCouponDetail::getOrderId, Function.identity(), (o1, o2) -> o2));
        }

        List<RewardOrder> orderList = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId())
                .stream()
                .filter(isDeliverOrder.or(isFailedOrder))// 失败的情况，也进入已发货
                .collect(toList());

        Collection<Long> productIds = orderList.stream().map(o -> o.getProductId()).collect(toSet());

        Map<Long, List<RewardImage>> rewardImages = rewardLoaderClient.loadProductRewardImages(productIds);
        List<Map<String, Object>> orderMapLists = new LinkedList<>();
        Map<Long, RewardProductDetail> productMap = rewardLoaderClient.generateUserRewardProductDetails(user, productIds).stream().collect(Collectors.toMap(pd -> pd.getId(), pd -> pd));
        for (RewardOrder order : orderList) {

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("orderId", order.getId());

            List<RewardImage> images = rewardImages.get(order.getProductId());
            if (CollectionUtils.isNotEmpty(images)) {
                map.put("image", combineCdbUrl(images.get(0).getLocation()));
            }

            map.put("productName", order.getProductName());
            map.put("skuName", order.getSkuName());
            map.put("price", order.getTotalPrice());//给总价。。。。
            map.put("discount", order.getDiscount());//给折扣
            map.put("quantity", order.getQuantity());
            map.put("unit", order.getUnit());

            RewardOrderStatus status = RewardOrderStatus.valueOf(order.getStatus());
            map.put("status", status.name());

            RewardProductDetail productDetail = productMap.get(order.getProductId());
            String couponResource = null;
            String couponUrl = null;
            if (productDetail != null) {
                couponResource = productDetail.getCouponResource() != null ? productDetail.getCouponResource().name() : null;
                if (productDetail.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(order.getExtAttributes())) {
                    couponUrl = "/usermobile/giftMall/usercoupon/redirect.vpage?orderId=" + order.getId();
                }
            }
            map.put("couponResource", couponResource);
            map.put("couponUrl", couponUrl);
            map.put("ct", order.getCreateDatetime());
            map.put("createTime", DateUtils.dateToString(order.getCreateDatetime(), "yyyy-MM-dd"));
            // 组id，年加月份
            map.put("groupId", DateUtils.dateToString(order.getCreateDatetime(), "yyyyMM"));

            map.put("logisticId", order.getLogisticsId());

            // 如果是流量包类型，成功后显示文字为兑换成功
            Integer oneLevelCategoryType = newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(order);
            if (Objects.equals(oneLevelCategoryType, OneLevelCategoryType.JPZX_FLOW_PACKET.intType())) {
                if (Objects.equals(status, DELIVER)) {
                    map.put("statusName", "兑换成功");
                }
                else if (Objects.equals(status, FAILED)) {
                    map.put("statusName", "充值失败");
                } else {
                    map.put("statusName", status.getDesc());
                }
            } else {
                map.put("statusName", null);
            }

            //如果是优惠券类型给一个类型
            if (orderCouponMap.containsKey(order.getId())) {
                RewardCouponDetail coupon = orderCouponMap.get(order.getId());
                map.put("type", "coupon");
                map.put("couponNo", coupon.getCouponNo());
                // 兑换时间做为订单的基准时间
                Date exchangeDate = coupon.getExchangedDate();

                // 分组id
                map.put("statusName", "已发货");
            }

            //标记教学资源类型
            if (Objects.equals(oneLevelCategoryType, OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType())) {
                map.put("type", "courseWare");
            }

            orderMapLists.add(map);
        }

        String unit = fetchUnit(user);

        List<Long> couponPIds = coupons.stream().map(c -> c.getProductId()).collect(toList());
        // 兑换券相关的图片列表
        Map<Long, List<RewardImage>> couponImages = rewardLoaderClient.loadProductRewardImages(couponPIds);
        // 生成兑换记录
        List<Map<String, Object>> couponOrders = coupons.stream()
                .filter(c -> Objects.isNull(c.getOrderId()) || Objects.equals(c.getOrderId(), 0L))
                .map(c -> {
                    if (c.getOrderId() == null || c.getOrderId() == 0L) {
                        RewardProductDetail product = newRewardLoaderClient.generateRewardProductDetail(user, c.getProductId());
                        Map<String, Object> coupon = new HashMap<>();
                        coupon.put("type", "coupon");
                        if (product != null) {
                            coupon.put("productName", product.getProductName());
                            coupon.put("quantity", 1);
                            coupon.put("price", product.getDiscountPrice());
                            coupon.put("discount", 1.0);
                            List<RewardImage> images = couponImages.get(product.getId());
                            if (CollectionUtils.isNotEmpty(images)) {
                                RewardImage selectImage = rewardLoaderClient.pickDisplayImage(user, images);
                                if (selectImage != null) {
                                    coupon.put("image", combineCdbUrl(selectImage.getLocation()));
                                } else
                                    coupon.put("image", combineCdbUrl(images.get(0).getLocation()));
                            }

                            coupon.put("unit", unit);


                            if (newRewardLoaderClient.isTeachingResources(product.getOneLevelCategoryId()))
                                coupon.put("type", "courseWare");
                        }

                        if (c.getOrderId() != null && c.getOrderId() != 0L) {
                            RewardOrder rewardOrder = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(c.getOrderId());
                            coupon.put("price", rewardOrder.getTotalPrice());
                            coupon.put("discount", rewardOrder.getDiscount());
                        }

                        coupon.put("couponNo", c.getCouponNo());
                        // 兑换时间做为订单的基准时间
                        Date exchangeDate = c.getExchangedDate();

                        coupon.put("ct", exchangeDate);
                        coupon.put("createTime", DateUtils.dateToString(exchangeDate, "yyyy-MM-dd"));
                        // 分组id
                        coupon.put("groupId", DateUtils.dateToString(exchangeDate, "yyyyMM"));
                        coupon.put("statusName", "已发货");

                        coupon.put("couponResource", null);
                        coupon.put("couponUrl", null);
                        return coupon;
                    }
                    return null;
                }).collect(toList());

        if (CollectionUtils.isNotEmpty(couponOrders)) {
            orderMapLists.addAll(couponOrders.stream().filter(sub -> sub != null).collect(toList()));
        }

        // 分组返回数据 以月份分组
        List<HistoryOrderMapper> dataList = new ArrayList<>();
        Map<String, List<Map<String, Object>>> orderMap = orderMapLists.stream()
                .collect(Collectors.groupingBy(o -> SafeConverter.toString(o.get("groupId"))));

        orderMap.forEach((groupId, orders) -> {

            HistoryOrderMapper mapper = new HistoryOrderMapper();
            mapper.setGroupId(groupId);

            // 组内订单按时间从近到远排序
            orders.sort((o1,o2) -> {
                Date d1 = (Date)o1.get("ct");
                Date d2 = (Date)o2.get("ct");
                return d2.compareTo(d1);
            });

            Map<String, Object> order = orders.stream()
                    .filter(o -> {
                        Long logisticId = SafeConverter.toLong(o.get("logisticId"));
                        return logisticId != 0L;
                    })
                    .findFirst()
                    .orElse(null);

            if (order != null) {
                Long logisticId = SafeConverter.toLong(order.get("logisticId"));
                RewardLogistics logistics = crmRewardService.$loadRewardLogistics(logisticId);

                if (logistics != null) {
                    mapper.setLogisticNo(logistics.getLogisticNo());
                    mapper.setCompanyName(logistics.getCompanyName());
                    mapper.setDeliverDate(DateUtils.dateToString(logistics.getUpdateDatetime(), "yyyy-MM"));
                    mapper.setOrderTime(logistics.getUpdateDatetime());
                }
            } else {
                // 任意取一个订单的创建日期的月份
                Date createTime = (Date) orders.get(0).get("ct");
                if (createTime != null) {
                    mapper.setDeliverDate(DateUtils.dateToString(createTime, "yyyy-MM"));
                    mapper.setOrderTime(createTime);
                }
            }

            mapper.setOrders(orders);
            dataList.add(mapper);
        });

        if (user.isTeacher()) {
            String rewardInfo = "感谢您的辛勤付出，" + (currentTeacherDetail() != null && currentTeacherDetail().isPrimarySchool() ? "50园丁豆": "500学豆") + "奖励已发送！";
            List<RewardLogistics> studentLogistics = crmRewardService.$findRewardLogisticsList(user.getId(), RewardLogistics.Type.STUDENT)
                    .stream().filter(e -> Boolean.TRUE.equals(e.getIsBack()))
                    .collect(toList());
            if (CollectionUtils.isNotEmpty(studentLogistics)) {
                studentLogistics.forEach(e -> {
                    HistoryOrderMapper mapper = new HistoryOrderMapper();
                    mapper.setSubstituteReceive(true);
                    mapper.setGroupId(DateUtils.dateToString(e.getCreateDatetime(), "yyyyMM"));
                    mapper.setLogisticNo(e.getLogisticNo());
                    mapper.setCompanyName(e.getCompanyName());
                    mapper.setDeliverDate(DateUtils.dateToString(e.getUpdateDatetime(), "yyyy-MM"));
                    mapper.setOrderTime(e.getUpdateDatetime());
                    Map<String,Object> tempOrder = new HashMap<>();
                    tempOrder.put("image", "http://17zy-content-video.oss-cn-beijing.aliyuncs.com/Prize/chanpin/%E4%BB%A3%E6%94%B6.png");
                    tempOrder.put("productName", "【学生包裹】" + e.getSchoolName());
                    tempOrder.put("status", DELIVER.name());
                    tempOrder.put("statusName", DELIVER.getDesc());
                    tempOrder.put("ct", e.getCreateDatetime());
                    tempOrder.put("createTime", DateUtils.dateToString(e.getCreateDatetime(), "yyyy-MM-dd"));
                    // 组id，年加月份
                    tempOrder.put("groupId", DateUtils.dateToString(e.getCreateDatetime(), "yyyyMM"));
                    tempOrder.put("logisticId", e.getId());
                    mapper.setRewardInfo(rewardInfo);
                    mapper.setOrders(Arrays.asList(tempOrder));
                    dataList.add(mapper);
                });
            }
        }

        // 从近到远排序 fixme 有坑，deliverDate 和orderTime是根据updateDatetime转过来的，，但是updateDatetime存在为空的情况，造成排序不准只能用groupId来排。。。
        dataList.sort((d1, d2) -> {
            if (d1.getGroupId() != null && d2.getGroupId() != null)
                return d2.getGroupId().compareTo(d1.getGroupId());
            else
                return 0;// 为空的情况比较不出来，就算相等
        });

        return MapMessage.successMessage().add("orderData", dataList);
    }


    /**
     * 获取用户兑换历史
     *
     * @return
     */
    @RequestMapping(value = "/order/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage orderList() {
        User user = currentRewardUser();
        if (user == null) {
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

        List<RewardOrder> rewardOrders = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId());
        /*if (CollectionUtils.isEmpty(rewardOrders))
            return MapMessage.successMessage().add("orderList", new ArrayList<>());*/

        List<RewardOrder> orderList;
        if (type.equals("wait")) {
            orderList = rewardOrders.stream()
                    .filter(t -> !DELIVER.name().equals(t.getStatus()))
                    .filter(t -> !FAILED.name().equals(t.getStatus()))// 失败的不显示
                    .collect(toList());
        } else if (type.equals("deliver")) {
            orderList = rewardOrders.stream()
                    .filter(t -> DELIVER.name().equals(t.getStatus())
                            || FAILED.name().equals(t.getStatus()))// 失败的情况，也进入已发货
                    .collect(toList());
        } else
            orderList = new ArrayList<>();

        String unit = fetchUnit(user);
        Set<Long> productIds = orderList.stream().map(RewardOrder::getProductId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RewardProductDetail> productMap = rewardLoaderClient.generateUserRewardProductDetails(user, productIds).stream().collect(Collectors.toMap(pd -> pd.getId(), pd -> pd));
        Page<Map<String, Object>> page = rewardLoaderClient.rewardOrdersToPage(orderList, pageable);
        page.getContent().forEach(t -> {
            if (new Double(0).equals(t.get("price"))) {
                t.put("type", "try");
            } else
                t.put("type", "normal");

            RewardOrderStatus status = RewardOrderStatus.valueOf(SafeConverter.toString(t.get("status")));
            t.put("statusName", status.getDesc());

            String image = (String) t.remove("image");
            t.put("image", combineCdbUrl(image));
            Date createDate = (Date) t.remove("createDatetime");
            t.put("createDatetime", DateUtils.dateToString(createDate, DateUtils.FORMAT_SQL_DATETIME));
            // 加入原始值，做排序用
            t.put("ct", createDate);
            t.remove("unit");
            t.put("substituteReceive", false);
            t.put("unit", unit);
            RewardProductDetail productDetail = productMap.get(t.get("productId"));
            if (productDetail != null) {
                t.put("couponResource", productDetail.getCouponResource());
                String couponUrl = null;
                if (productDetail.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(SafeConverter.toString(t.get("ext")))) {
                    couponUrl = "/usermobile/giftMall/usercoupon/redirect.vpage?orderId=" + t.get("orderId");
                }
                t.put("couponUrl", couponUrl);
            } else {
                t.put("couponResource", null);
                t.put("couponUrl", null);
            }
            t.remove("ext");
        });

        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(page.getContent());

        if (user.isTeacher()) {
            List<RewardLogistics> rewardLogisticsList = crmRewardService.$findRewardLogisticsList(user.getId(), RewardLogistics.Type.STUDENT).stream().filter(e -> Boolean.FALSE.equals(e.getIsBack())).collect(toList());
            String rewardInfo = "发货后您将获得" + (currentTeacherDetail() != null && currentTeacherDetail().isPrimarySchool() ? "50园丁豆": "500学豆") + "奖励";
            for(RewardLogistics rewardLogistics : rewardLogisticsList) {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("image", "http://17zy-content-video.oss-cn-beijing.aliyuncs.com/Prize/chanpin/%E4%BB%A3%E6%94%B6.png");
                studentMap.put("type", "normal");
                studentMap.put("substituteReceive", true);
                studentMap.put("productName", "【学生包裹】" + rewardLogistics.getSchoolName());
                studentMap.put("createDatetime", DateUtils.dateToString(rewardLogistics.getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                studentMap.put("ct", rewardLogistics.getCreateDatetime());
                studentMap.put("statusName", RewardOrderStatus.PREPARE.getDesc());
                studentMap.put("status", RewardOrderStatus.PREPARE.name());
                studentMap.put("rewardInfo", rewardInfo);
                result.add(studentMap);
            }
        }
        // 重新按时间排序
        result.sort((c1, c2) -> {
            Date ct1 = (Date) c1.get("ct");
            Date ct2 = (Date) c2.get("ct");
            return ct2.compareTo(ct1);
        });
        Page<Map<String, Object>> resultPage = new PageImpl<>(result, pageable, result.size());
        return MapMessage.successMessage().add("orderList", resultPage.getContent());
    }

    /**
     * 用户取消兑换
     *
     * @return
     */
    @RequestMapping(value = "/order/cancel.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage cancelOrder() {

        User user = currentRewardUser();
        if (user == null) {
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

        return rewardServiceClient.deleteRewardOrder(user, order);
    }

    @RequestMapping(value = "/notify.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage notifyShow() {
        User user = currentRewardUser();
        if (user == null) {
            return noLoginResult;
        }
        MapMessage resultMap = MapMessage.successMessage();
        String unit = fetchUnit(user);
        if (user.isTeacher()) {
            TeacherCouponMapper mapper = this.getTeacherCouponMapper(user);
            resultMap.add("teacherNewLevel", mapper==null ? null:mapper.getTeacherNewLevel());
            resultMap.add("teacherCouponList", mapper==null ? null:mapper.getTeacherCouponList());
        }
        return resultMap.add("integral_text", unit);
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

    @RequestMapping(value = "/coupon/redirect.vpage", method = {RequestMethod.GET})
    public String redirectThirdpartyCoupon() {
        User user = currentRewardUser();
        long productId = getRequestLong("productId");
        if (user == null) {
            return "redirect:view/mobile/common/reward_detail?productId=" + productId;
        }
        long usable = 0;
        boolean isXueDou = true;
        if (user.isTeacher()) {
            usable = currentTeacherDetail().getUserIntegral().getUsable();
            isXueDou = !currentTeacherDetail().isPrimarySchool();
        }
        if (user.isStudent()) {
            usable = currentStudentDetail().getUserIntegral().getUsable();
        }

        RewardProduct rewardProductDetail = crmRewardService.$loadRewardProduct(productId);
        if (rewardProductDetail == null || StringUtils.isBlank(rewardProductDetail.getUsedUrl())) {
            return "redirect:view/mobile/common/reward_detail?productId=" + productId;
        }
        Map<String, String> param = new HashMap<>();
        param.put("uid", user.getId().toString());
        param.put("credits", "" + usable);
        param.put("redirect", rewardProductDetail.getUsedUrl());
        DuibaTool.DuibaApp app = RuntimeMode.current().le(Mode.TEST) ? (isXueDou ? DuibaTool.DuibaApp.TEST_XUEDOU : DuibaTool.DuibaApp.TEST_YUANDINGDOU) :
                (isXueDou ? DuibaTool.DuibaApp.ONLINE_XUEDOU : DuibaTool.DuibaApp.ONLINE_YUANDINGDOU);
        return "redirect:" + DuibaTool.buildUrlWithSign("https://www.duiba.com.cn/autoLogin/autologin?", param, app);
    }

    //兑吧用户兑换详情跳转
    @RequestMapping(value = "/usercoupon/redirect.vpage", method = {RequestMethod.GET})
    public String redirectThirdpartyExchangedCoupon() {
        User user = currentRewardUser();
        Long orderId = getRequestLong("orderId");
        if (orderId == 0L || user == null) {
            return "redirect:view/mobile/common/reward_detail?productId=";
        }

        RewardOrder order = rewardLoaderClient.getRewardOrderLoader().loadRewardOrder(orderId);
        if (order == null || StringUtils.isBlank(order.getExtAttributes())) {
            return "redirect:view/mobile/common/reward_detail?productId=";
        }
        Map<String ,Object> map = JsonUtils.fromJson(order.getExtAttributes());
        if (MapUtils.isEmpty(map) || map.get("couponUrl") == null) {
            return "redirect:view/mobile/common/reward_detail?productId=";
        }
        long usable = 0;
        boolean isXueDou = true;
        if (user.isTeacher()) {
            usable = currentTeacherDetail().getUserIntegral().getUsable();
            isXueDou = !currentTeacherDetail().isPrimarySchool();
        }
        if (user.isStudent()) {
            usable = currentStudentDetail().getUserIntegral().getUsable();
        }
        Map<String, String> param = new HashMap<>();
        param.put("uid", user.getId().toString());
        param.put("credits", "" + usable);
        param.put("redirect", SafeConverter.toString(map.get("couponUrl")));
        DuibaTool.DuibaApp app = RuntimeMode.current().le(Mode.TEST) ? (isXueDou ? DuibaTool.DuibaApp.TEST_XUEDOU : DuibaTool.DuibaApp.TEST_YUANDINGDOU) :
                (isXueDou ? DuibaTool.DuibaApp.ONLINE_XUEDOU : DuibaTool.DuibaApp.ONLINE_YUANDINGDOU);
        return "redirect:" + DuibaTool.buildUrlWithSign("https://www.duiba.com.cn/autoLogin/autologin?", param, app);
    }

    private MapMessage checkCanBuy(User user, RewardProductDetail rewardProductDetail, Integer quantity, RewardSku sku, TeacherCouponEntity coupon) {

        if (UserType.TEACHER.equals(user.fetchUserType()))
            return checkTeacherCanBuy(user, rewardProductDetail, quantity, sku, coupon);
        else if (UserType.STUDENT.equals(user.fetchUserType()))
            return checkStudentCanBuy(user, rewardProductDetail, quantity, sku);
        else
            return MapMessage.errorMessage("暂不支持当前用户兑换").add(CHECK_RESULT_TYPE, "default");

    }

    private MapMessage checkStudentCanBuy(User user, RewardProductDetail rewardProductDetail, Integer quantity, RewardSku sku) {

        if (!rewardProductDetail.getOnline())
            return MapMessage.errorMessage("对不起,您兑换的奖品目前缺货,暂时无法兑换。").add(CHECK_RESULT_TYPE, "noEnoughProducts");

        //判断用户学豆是否足够
        Long userIntegral = rewardCenterClient.getIntegral(user);
        BigDecimal total = new BigDecimal(rewardProductDetail.getDiscountPrice()).multiply(new BigDecimal(quantity));
        int totalPrice = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (userIntegral < totalPrice) {
            String unit = fetchUnit(user);
            return MapMessage.errorMessage(unit + "不足").add(CHECK_RESULT_TYPE, "noEnoughIntegral");
        }

        //判断单品是否存在以及是否售完
        if (sku == null) {
            return MapMessage.errorMessage("单品不存在。").add(CHECK_RESULT_TYPE, "default");
        }
        if (sku.getInventorySellable() < quantity) {
            return MapMessage.errorMessage("对不起,您兑换的奖品目前缺货,暂时无法兑换。").add(CHECK_RESULT_TYPE, "noEnoughProducts");
        }

        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(user.getId());

        TeacherDetail detail = userAggregationLoaderClient.loadStudentTeacherForRewardSending(user.getId());
        if (detail == null) {
            return MapMessage.errorMessage("你的班级还没有收货老师，无法寄送哦！").add(CHECK_RESULT_TYPE, "noTeacher");
        }

        //此处新增逻辑， 有这个灰度的老师的学生兑换， 不判断学生绑定手机的情况  2015-05-29
        if (!grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(detail, "Reward", "OrderCheck")) {
            if (!authentication.isMobileAuthenticated() && parentLoaderClient.loadStudentKeyParent(user.getId()) == null) {
                return MapMessage.errorMessage("你还没有绑定手机，无法寄送哦！").add(CHECK_RESULT_TYPE, "studentNoBindMobile");
            }
        }
        // 毕业班不允许兑换
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
        if (isGraduate(studentDetail)) {
            return MapMessage.errorMessage("毕业班学生暂时不能兑换奖品哦！").add(CHECK_RESULT_TYPE, "studentGraduate");
        }
        return MapMessage.successMessage();
    }

    // 6月1日 - 8月10日 毕业班不允许兑换
    //中学是6月1号开始就不允许兑换
    private boolean isGraduate(StudentDetail studentDetail) {
        if (studentDetail.getClazz() == null || studentDetail.getClazz().isTerminalClazz())
            return true;

        String currentYear = DateUtils.dateToString(new Date(), "yyyy");
        Date startDate = DateUtils.stringToDate(currentYear + "-06-01 00:00:00");
        if (RuntimeMode.le(Mode.TEST)) {
            startDate = DateUtils.stringToDate(currentYear + "-05-01 00:00:00");
        }
        if (studentDetail.isPrimaryStudent()) {
            Date endDate = DateUtils.stringToDate(currentYear + "-08-10 23:59:59");
            DateRange range = new DateRange(startDate, endDate);
            if (range.contains(new Date())) {
                // 5年制5年级 或者6年制6年级
                Clazz clazz = studentDetail.getClazz();
                if (clazz != null && ((clazz.getEduSystem() == EduSystemType.P5 && clazz.getClazzLevel().getLevel() == 5)
                        || (clazz.getEduSystem() == EduSystemType.P6 && clazz.getClazzLevel().getLevel() == 6))) {
                    return true;
                }
            }
        }
        if (studentDetail.isJuniorStudent()) {
            Date endDate = DateUtils.stringToDate(currentYear + "-08-10 23:59:59");
            DateRange range = new DateRange(startDate, endDate);
            if (range.contains(new Date())) {
                Clazz clazz = studentDetail.getClazz();
                if (clazz != null && clazz.getClazzLevel().getLevel() == 9) {
                    return true;
                }
            }
        }
        return false;
    }

    private MapMessage checkTeacherCanBuy(User user, RewardProductDetail rewardProductDetail, Integer quantity, RewardSku sku, TeacherCouponEntity coupon) {

        if (!rewardProductDetail.getOnline())
            return MapMessage.errorMessage("对不起,您兑换的奖品目前缺货,暂时无法兑换。").add(CHECK_RESULT_TYPE, "noEnoughProducts");

        if (user == null || !user.fetchUserType().equals(UserType.TEACHER))
            return MapMessage.errorMessage("暂不支持当前用户兑换").add(CHECK_RESULT_TYPE, "default");
        TeacherDetail teacherDetail;
        if (user instanceof TeacherDetail)
            teacherDetail = (TeacherDetail) user;
        else
            teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
        //判断用户学豆是否足够
        Long userIntegral = rewardCenterClient.getIntegral(user);
        int totalPrice = rewardServiceClient.getDiscountPrice(quantity, rewardProductDetail, coupon);
        if (userIntegral < totalPrice) {
            String unit = fetchUnit(user);
            return MapMessage.errorMessage(unit + "不足").add(CHECK_RESULT_TYPE, "noEnoughIntegral");
        }

        //判断单品是否存在以及是否售完(实物逻辑)
        if (newRewardLoaderClient.isSHIWU(rewardProductDetail.getOneLevelCategoryId())) {
            if (sku == null) {
                return MapMessage.errorMessage("单品不存在。").add(CHECK_RESULT_TYPE, "default");
            }
            if (sku.getInventorySellable() < quantity) {
                return MapMessage.errorMessage("对不起,您兑换的奖品目前缺货,暂时无法兑换。").add(CHECK_RESULT_TYPE, "noEnoughProducts");
            }
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
        if (!newRewardLoaderClient.checkCanExchangePrivilegeProduct(rewardProductDetail.getId(), teacherDetail)) {
            return MapMessage.errorMessage("当前级别不可兑换").add(CHECK_RESULT_TYPE, "default");
        }
        //收获地址为空
        UserShippingAddress shippingAddress = userLoaderClient.loadUserShippingAddress(teacherDetail.getId());
        if (shippingAddress == null || StringUtils.isBlank(shippingAddress.getDetailAddress())) {
            return MapMessage.errorMessage("你还没有填写收货地址，暂时不能兑换奖品哦~").add(CHECK_RESULT_TYPE, "noAddress");
        }
        //老师等级判断
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherDetail.getId());
        int teacherLevel = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getLevel());

        if (rewardProductDetail.getTeacherLevel() > 0 && teacherLevel < rewardProductDetail.getTeacherLevel()) {
            return MapMessage.errorMessage("对不起，你的等级不能兑换该奖品").add(CHECK_RESULT_TYPE, "default");
        }
        return MapMessage.successMessage();
    }
}
